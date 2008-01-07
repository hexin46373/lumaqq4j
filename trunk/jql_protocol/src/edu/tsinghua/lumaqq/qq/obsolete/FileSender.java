/*
* LumaQQ - Java QQ Client
*
* Copyright (C) 2004 luma <stubma@163.com>
*
* This program is free software; you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation; either version 2 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
*/
package edu.tsinghua.lumaqq.qq.obsolete;

import static org.apache.commons.codec.digest.DigestUtils.md5;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;

import edu.tsinghua.lumaqq.qq.QQ;
import edu.tsinghua.lumaqq.qq.Util;

/**
 * <pre>
 * 文件发送类
 * </pre>
 * 
 * @author luma
 */
public class FileSender extends FileWatcher implements Runnable {
    private DatagramChannel dc;
    private DatagramChannel dcMinor;
    private DatagramChannel dcMajor;
    // 分片缓冲区
    protected FragmentBuffer fb;
    // 包处理器
    private FileSenderPacketProcessor processor;
    // heart beat线程
    protected HeartBeatThread hbThread;
    // 是否暂停发送，如果heart beat长久得不到回应，将暂停发送
    private boolean suspend;
    // 当前文件数据信息包的顺序号
    private char packetSN;

    /**
     * @param client
     */
    public FileSender() {
        super();
        processor = new FileSenderPacketProcessor(this);     
        suspend = false;
        packetSN = 0;
    }
    
	/**
	 * 打开本地文件准备读
	 * @return true表示成功，false表示失败
	 */
	public boolean openLocalFile() {
	    if(localFile != null) return true;
	    try {
            localFile = new RandomAccessFile(localFileName, "rw");
            // 如果我是发送者，附加计算一下文件和文件名的MD5
            fileMD5 = Util.getFileMD5(localFile);
            fileNameMD5 = md5(Util.getBytes(fileName));
            return true;
        } catch (Exception e) {
            return false;
        }
	}	

    /* (non-Javadoc)
     * @see edu.tsinghua.lumaqq.qq.filetrans.FileWatcher#shutdown()
     */
    public void shutdown() {
	    if(localFile != null) {
		    try {
	            localFile.close();
	        } catch (IOException e) {
	            log.error(e.getMessage());
	        }	        
	    }
	    if(hbThread != null)
	        hbThread.setStop(true);
	    shutdown = true;
	    if(selector != null)
	        selector.wakeup();
    }
	
    /* (non-Javadoc)
     * @see edu.tsinghua.lumaqq.qq.filetrans.FileWatcher#abort()
     */
    public void abort() {
        if(fileTransferStatus == FT_NONE)
            return;
        else if(fileTransferStatus == FT_NEGOTIATING)
        	;
        else 
            sendFinish(fdp, buffer);
        shutdown();
        fireFileAbortedEvent();
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run() {
	    while(true) {
			try {
			    int n = selector.select();
			    if(n > 0)
			        processSelectedKeys();
			    if (shutdown) {
			        selector.close();
			        if(useUdp) {
			           if(dcMinor != null && dcMinor.isOpen()) dcMinor.close();
			           if(dcMajor != null && dcMajor.isOpen()) dcMajor.close();
			        }
				    log.debug("文件守望者正常退出，Session Sequence: " + (int)sessionSequence);
					return;			        
			    }
			} catch (IOException e) {
			    log.error(e.getMessage());
			}
		}
    }
    
    /**
     * 处理Key事件
     */
    private void processSelectedKeys() {
        for (Iterator<SelectionKey> i = selector.selectedKeys().iterator(); i.hasNext();) {
			// 得到下一个Key
			SelectionKey sk = i.next();
			i.remove();
			
			// 交给processor处理
			try {
		    	// 如果当前处理协商阶段，那么我们期望收到0x003C命令，所以特殊处理
		    	//    如果不是，说明连接已经建立，正在传送中，对于不用来传送文件的
		    	//    那条链路我们释放掉
			    if(fileTransferStatus == FileWatcher.FT_NEGOTIATING)
			        processNegotiation(sk);
			    else {
			        Boolean b = (Boolean)sk.attachment();
			        DatagramChannel channel = (DatagramChannel)sk.channel();
			        if(b.booleanValue() == major) {
			            buffer.clear();
				        channel.read(buffer);		        			            
			        } else {
			            sk.cancel();
			            channel.close();
			            return;
			        }
	                processor.processSelectedKeys(buffer);			        
			    }
            } catch (Exception e) {
                log.error(e.getMessage());
            }			    
        }
    }    

    /**
     * @param sk
     * @throws IOException
     */
    private void processNegotiation(SelectionKey sk) throws Exception {
        // 得到channel，读取数据
        buffer.clear();
        DatagramChannel channel = (DatagramChannel)sk.channel();
        InetSocketAddress address = (InetSocketAddress)channel.receive(buffer);
        buffer.flip();
        // 检查是否控制信息包
        if(buffer.get() == QQ.QQ_HEADER_P2P_FAMILY) {
            buffer.rewind();
            fcp.parse(buffer);
            // 检查是否重复包
            if(!monitor.checkDuplicate(fcp)) {
                switch(fcp.getCommand()) {
                    case QQ.QQ_FILE_CMD_NOTIFY_IP_ACK:
                        log.debug("收到Notify IP Ack, 对方真实IP: " + Util.getIpStringFromBytes(fcp.getLocalIp()) + " 第二个端口为" + fcp.getMinorPort());
                        // 得到对方的本地ip和端口
                        hisLocalIp = fcp.getLocalIp();
                        hisMinorPort = fcp.getMinorPort();
                        // 检查双方网络处境
                        checkCondition();
                        if(condition == QQ.QQ_SAME_LAN || condition == QQ.QQ_NONE_BEHIND_FIREWALL || condition == QQ.QQ_I_AM_BEHIND_FIREWALL) {
                            /*
                             * 1. 如果双方位于同一个局域网
                             * 2. 或者我们都有固定的IP，且都不在防火墙后
                             * 3. 或者我在防火墙后，对方不在
                             * 这些情况在收到Ack后都可以直接开始连接
                             */
                            establishConnection(sk, channel, address);
                        } else if(condition == QQ.QQ_HE_IS_BEHIND_FIREWALL) {
                            /*
                             * 现在我是发送方，而对方却在防火墙内，这个时候我需要发一个包
                             * 通知他主动连接我
                             */
                            ;
                        }
                    	break;
                    case QQ.QQ_FILE_CMD_YES_I_AM_BEHIND_FIREWALL:
                        log.debug("对方位于防火墙后，开始主动连接我");
                    	establishConnection(sk, channel, address);
                    	break;                    	
                    case QQ.QQ_FILE_CMD_PONG:
                        log.debug("收到Pong");
                    	// 仅当我在防火墙后时，理会这个Ack
                    	if(!Util.isIpEquals(myInternetIp, myLocalIp)) {
                    	    condition = QQ.QQ_I_AM_BEHIND_FIREWALL;
	                        establishConnection(sk, channel, address);
                    	}              
                    	break;
                }
            }
        }
    }
    
    /**
     * 往对方直接端口发送Init Connection包
     */
    public void sendInitConnectionToDirect() {
        InetSocketAddress address = new InetSocketAddress(Util.getIpStringFromBytes(hisInternetIp), hisMajorPort);
        fcp.setCommand(QQ.QQ_FILE_CMD_PING);
        fcp.fill(buffer);
        try {
	        buffer.flip();
            dcMajor.send(buffer, address);
            buffer.rewind();
            dcMajor.send(buffer, address);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        log.debug("Init Connection 已发送");
    }

    /**
     * 建立文件传输连接
     * @param sk
     * @param channel
     * @param address
     * @throws IOException
     */
    private void establishConnection(SelectionKey sk, DatagramChannel channel, InetSocketAddress address) throws IOException {
        // 根据是直接端口还是本地端口设置连接方式
        Boolean b = (Boolean)sk.attachment();
        major = b.booleanValue();
        // 连接
        channel.connect(address);
        // 把这个channel的引用交给dc
        dc = channel;
        // say hello
        sayHello(fcp, buffer);
        // 设置当前状态为传送中
        fileTransferStatus = FT_SAYING_HELLO;
        // 初始化滑窗和分片缓冲区
        initSlideWindow(1, 0, fragments);
        initFragmentBuffer(localFile, 1, maxFragmentSize, fragments);
        // 启动heart beat线程
        hbThread = new HeartBeatThread(this);
        hbThread.start();    
    }

    /* (non-Javadoc)
     * @see edu.tsinghua.lumaqq.qq.filetrans.FileWatcher#finish()
     */
    public void finish() {
        // 设置文件传输状态为false，关闭文件守望者
        shutdown();
        setFileTransferStatus(FT_NONE);
        fireFileFinishedEvent();
    }

    /* (non-Javadoc)
     * @see edu.tsinghua.lumaqq.qq.filetrans.FileWatcher#start()
     */
    public void start() {
	    try {
            // 创建Selector
            selector = Selector.open();
        } catch (IOException e) {
            log.error("无法创建Selector");
            return;
        }
        useUdp = true;
        // 启动端口
        startMajorPort();
        startMinorPort();
        // 启动selector
        new Thread(this).start();
    }  
    
    /**
     * 启动直接端口的监听
     */
    private void startMajorPort() {        
	    try {
            dcMajor = DatagramChannel.open();
            dcMajor.configureBlocking(false);
            try {
	            dcMajor.socket().bind(new InetSocketAddress(Util.getIpStringFromBytes(myInternetIp), 0));                
            } catch (SocketException e) {
                dcMajor.socket().bind(new InetSocketAddress(Util.getIpStringFromBytes(myLocalIp), 0));
            }
            myMajorPort = dcMajor.socket().getLocalPort();
            dcMajor.register(selector, SelectionKey.OP_READ, new Boolean(true));
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
    
    /**
     * 启动本地端口的监听
     */
    private void startMinorPort() {
	    try {	    
            dcMinor = DatagramChannel.open();
            dcMinor.configureBlocking(false);
            DatagramSocket ds = dcMinor.socket();
            ds.bind(new InetSocketAddress(Util.getIpStringFromBytes(myLocalIp), 0));
            myMinorPort = ds.getLocalPort();
            dcMinor.register(selector, SelectionKey.OP_READ, new Boolean(false));
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
    
    /**
     * 初始化分片缓冲区
     * @param file RandomAccessFile对象
     * @param size buffer大小
     * @param fz 分片大小
     * @param max 文件最大的分片序号
     */
    public void initFragmentBuffer(RandomAccessFile file, int size, int fz, int max) {
        if(fb == null)
            fb = new FragmentBuffer(file, size, fz, max);
    }
    
    /**
     * 发送sender的buffer内容 
     */
    public void send() {
        try {
            if(useUdp)
                dc.write(buffer);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
    
    /**
     * 发送buffer中的内容
     */
    public void send(ByteBuffer buffer) {
        try {
            if(useUdp)
                dc.write(buffer);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
    
    /**
     * 发送heart beat 
     * @param seq 序号
     */
    protected void sendHeartBeat(FileDataPacket fdp, ByteBuffer buffer, char seq) {
        fdp.setCommand(QQ.QQ_FILE_CMD_HEART_BEAT);
        fdp.setHeartBeatSequence(seq);
        fdp.fill(buffer);
        buffer.flip();
        send(buffer);
        log.debug("Heart Beat " + (int)seq + " 已发送");
    }
    
    /**
     * say hello
     */
    protected void sayHello(FileControlPacket fcp, ByteBuffer buffer) {
        fcp.setCommand(QQ.QQ_FILE_CMD_SENDER_SAY_HELLO);
        if(condition == QQ.QQ_SAME_LAN)
            fcp.setHelloByte(QQ.QQ_SAME_IN_TO_SAME_IN_HELLO);
        else if(condition == QQ.QQ_HE_IS_BEHIND_FIREWALL)
            fcp.setHelloByte(QQ.QQ_OUT_TO_IN_HELLO);
        else if(condition == QQ.QQ_I_AM_BEHIND_FIREWALL)
            fcp.setHelloByte(QQ.QQ_IN_TO_OUT_HELLO);
        else if(condition == QQ.QQ_NONE_BEHIND_FIREWALL)
            fcp.setHelloByte(QQ.QQ_OUT_TO_OUT_HELLO);
        fcp.fill(buffer);
        buffer.flip();
        send(buffer);
        buffer.rewind();
        send(buffer);
        log.debug("Hello 包已经发送");
    }
    
    /**
     * 发送窗口中还没有收到确认的分片
     */
    protected void sendFragment(FileDataPacket fdp, ByteBuffer buffer) {
        // 由于这是heart beat线程调用的，所以需要同步
        synchronized(window) {
	        if(window.isFinished()) {
	            fileTransferStatus = FT_SENDING_EOF;
	            sendEOF(fdp, buffer);
	        } else {
	            fdp.setCommand(QQ.QQ_FILE_CMD_FILE_OP);
	            fdp.setInfoType(QQ.QQ_FILE_DATA_INFO);
	            int low = window.getLow();
	            int high = window.getHigh();
	            int mask = window.getMask();
	            for(int i = low, j = 1; i <= high; i++) {
	                // 根据mask判断这个分片是否已经得到了确认，如果没有，则重发
	                if((mask & j) == 0) {
	                    fdp.setPacketIndex(packetSN++);
	                    fdp.setFragmentIndex(i);
	                    if(high == fragments)
	                        fdp.setFragmentLength(fileSize % maxFragmentSize);
	                    else
	                        fdp.setFragmentLength(maxFragmentSize);
	                    fdp.setFragmentData(fb.getFragment(i));	                        
	                    fdp.setFragmentOffset(i * maxFragmentSize);
	                    fdp.fill(buffer);
	                    buffer.flip();
	                    send(buffer);
	                }
	                j <<= 1;
	            }                
            }
        }
    }
    
    /**
     * 发送文件EOF信息
     */
    protected void sendEOF(FileDataPacket fdp, ByteBuffer buffer) {
        fdp.setCommand(QQ.QQ_FILE_CMD_FILE_OP);
        fdp.setInfoType(QQ.QQ_FILE_EOF);
        fdp.setPacketIndex(packetSN++);
        fdp.fill(buffer);
        buffer.flip();
        send(buffer);
    }
    
    /**
     * 发送文件基本信息
     */
    protected void sendBasic(FileDataPacket fdp, ByteBuffer buffer) {
        fdp.setCommand(QQ.QQ_FILE_CMD_FILE_OP);
        fdp.setInfoType(QQ.QQ_FILE_BASIC_INFO);
        fdp.setPacketIndex(packetSN++);
        fdp.fill(buffer);
        buffer.flip();
        send(buffer);
    }
    
    /**
     * 发送传输结束信息
     */
    protected void sendFinish(FileDataPacket fdp, ByteBuffer buffer) {
        fdp.setCommand(QQ.QQ_FILE_CMD_TRANSFER_FINISHED);
        fdp.fill(buffer);
        buffer.flip();
        for(int i = 0; i < 4; i++) {
            buffer.rewind();
            send(buffer);
        }
    }
    
    /**
     * @return Returns the suspend.
     */
    public boolean isSuspend() {
        return suspend;
    }
    
    /**
     * @param suspend The suspend to set.
     */
    public void setSuspend(boolean suspend) {
        this.suspend = suspend;
    }
}
