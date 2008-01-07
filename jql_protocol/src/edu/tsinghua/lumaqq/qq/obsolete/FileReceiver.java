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
 * 文件接收类
 * </pre>
 * 
 * @author luma
 */
public class FileReceiver extends FileWatcher implements Runnable {
    // UDP channel
    private DatagramChannel dcMinor;
    private DatagramChannel dcMajor;
    private DatagramChannel dc;
    // 包处理类
    private FileReceiverPacketProcessor processor;
	
	/**
     * @param client
     */
    public FileReceiver() {
        super();
        processor = new FileReceiverPacketProcessor(this);
	    try {
            // 创建Selector
            selector = Selector.open();
        } catch (IOException e) {
            log.error("无法创建Selector");
            return;
        }
    }

    /**
	 * 根据发送和接收者的情况，选择一种连接方式开始初始化
	 */
	public void start() {
        // 设置状态
        fileTransferStatus = FT_NEGOTIATING;
	    // 启动FileReceiver
	    new Thread(this).start();
	}
	
	/**
	 * 选择一条链路进行传输
	 */
	public void selectPort() {
	    // 检查双方在网络中的情况
	    checkCondition();
        // 根据网络情况设置不同的连接策略
	    switch(condition) {
		    case QQ.QQ_SAME_LAN:
		        useUdp = true;
		    	major = false;
		    	break;
		    case QQ.QQ_NONE_BEHIND_FIREWALL:
		    case QQ.QQ_HE_IS_BEHIND_FIREWALL:
		    case QQ.QQ_I_AM_BEHIND_FIREWALL:
		        useUdp = true;
		        major = true;       
		        break;
	    }
        // 设置需要使用的连接
        dc = major ? dcMajor : dcMinor;
        // 发送0x003C命令到对方的本地端口
        fcp.setCommand(QQ.QQ_FILE_CMD_NOTIFY_IP_ACK);
        fcp.fill(buffer);
        buffer.flip();
        send();
        fileTransferStatus = FT_RECEIVING;
        log.debug("Notify IP Ack 已发送往" + dc.socket().getRemoteSocketAddress());
	}
	
    /**
     * 启动直接端口的监听
     */
    public void startMajorPort() {        
	    try {
            dcMajor = DatagramChannel.open();
            dcMajor.configureBlocking(false);
            try {
	            dcMajor.socket().bind(new InetSocketAddress(Util.getIpStringFromBytes(myInternetIp), 0));                
            } catch (SocketException e) {
                dcMajor.socket().bind(new InetSocketAddress(Util.getIpStringFromBytes(myLocalIp), 0));
            }
            myMajorPort = dcMajor.socket().getLocalPort();
            dcMajor.connect(new InetSocketAddress(Util.getIpStringFromBytes(hisInternetIp), hisMajorPort));
            dcMajor.register(selector, SelectionKey.OP_READ, new Boolean(true));
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
    
	/**
	 * 开始在本地端口接收发送
	 */
	public void startMinorPort() {
	    try {
            dcMinor = DatagramChannel.open();
            dcMinor.configureBlocking(false);
            DatagramSocket ds = dcMinor.socket();
            ds.bind(new InetSocketAddress(Util.getIpStringFromBytes(myLocalIp), 0));
            myMinorPort = ds.getLocalPort();
            dcMinor.connect(new InetSocketAddress(Util.getIpStringFromBytes(hisLocalIp), hisMinorPort));
            dcMinor.register(selector, SelectionKey.OP_READ, new Boolean(false));
        } catch (Exception e) {
            log.error(e.getMessage());
        }
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
			            if(dc != null) dc.close();
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
			
			// 读取缓冲区内容，交给processor处理
			try {
			    Boolean b = (Boolean)sk.attachment();
			    if(b.booleanValue() == major) {
				    buffer.clear();
				    dc.read(buffer);
	                processor.processSelectedKeys(buffer);			        
			    } else {
			        sk.cancel();
			        sk.channel().close();
			    }
            } catch (Exception e) {
                log.error(e.getMessage());
            }			    
        }
    }    
	
	/**
	 * 关闭守望者
	 */
	public void shutdown() {
	    if(localFile != null) {
		    try {
	            localFile.close();
	        } catch (IOException e) {
	            log.error(e.getMessage());
	        }	        
	    }
	    shutdown = true;
	    if(selector != null)
	        selector.wakeup();
	}
	
    /**
     * 在文件传输的中间取消掉传输
     */
    public void abort() {
        if(fileTransferStatus == FT_NONE)
            return;
        else if(fileTransferStatus == FT_NEGOTIATING)
		    ;	       
        else {
	        fdp.setCommand(QQ.QQ_FILE_CMD_TRANSFER_FINISHED);
	        fdp.fill(buffer);
	        buffer.flip();
	        try {
	            if(useUdp) 
	                dc.write(buffer);
	        } catch (IOException e) {
	            log.error(e.getMessage());
	        }            
        } 
        shutdown();
        fireFileAbortedEvent();
    }    
    
    /**
     * 在传输完成后做一些善后工作
     */
    public void finish() {
        // 设置文件传输状态为false，关闭文件守望者
        shutdown();
        setFileTransferStatus(FT_NONE);
        fireFileFinishedEvent();
    }
    
	/**
	 * 写入分片数据到文件中
	 * @param buf 数据缓冲
	 * @param offset 数据在实际文件中的绝对偏移
	 */
	public void saveFragment(byte[] buf, long offset) {
	    try {
            localFile.seek(offset);
            localFile.write(buf);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
	}
	
	/**
	 * 写入分片数据到文件中
	 * @param buf 包含数据的缓冲区
	 * @param from 从缓冲区的from位置开始为分片数据
	 * @param len 从缓冲区的from位置开始的len字节为分片数据
	 * @param offset 这段分片在实际文件中的绝对偏移
	 */
	public void saveFragment(byte[] buf, int from, int len, long offset) {
	    try {
            localFile.seek(offset);
            localFile.write(buf, from, len);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
	}
	
	/**
	 * 打开本地文件准备写
	 * @return true表示成功，false表示失败
	 */
	public boolean openLocalFile() {
	    try {
            localFile = new RandomAccessFile(localFileName, "rw");
            localFile.setLength(fileSize);
            return true;
        } catch (Exception e) {
            return false;
        }
	}
	
	/**
	 * 发送ByteBuffer中的内容
	 */
	public void send() {
	    send(buffer);
	}
	
	/**
	 * 发送指定buffer中的内容
	 * @param buffer
	 */
	public void send(ByteBuffer buffer) {
	    try {
	        if(useUdp)
	            dc.write(buffer);
	        log.debug("包已发送往: " + dc.socket().getRemoteSocketAddress());
        } catch (IOException e) {
            log.error(e.getMessage());
        }
	}
	
	/**
	 * 首先连接对方，由于这个方法会在另外一个线程中被调用，所以不使用本地的变量以免冲突
	 */
	public void notifyNATPort() {
	    ByteBuffer buffer = ByteBuffer.allocateDirect(200);
	    FileControlPacket fcp = new FileControlPacket(this);
	    fcp.setCommand(QQ.QQ_FILE_CMD_YES_I_AM_BEHIND_FIREWALL);
	    fcp.fill(buffer);
	    buffer.flip();
	    send(buffer);
	    buffer.rewind();
	    send(buffer);
	}
}
