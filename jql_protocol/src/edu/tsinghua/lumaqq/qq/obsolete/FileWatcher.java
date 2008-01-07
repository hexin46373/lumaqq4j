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

import java.io.File;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.tsinghua.lumaqq.qq.QQ;
import edu.tsinghua.lumaqq.qq.Util;
import edu.tsinghua.lumaqq.qq.beans.QQUser;

/**
 * 文件守望者，负责管理一次文件传输
 * 
 * @author luma
 */
public abstract class FileWatcher {
	// 文件传输的一些状态常量
	public static final int FT_NONE = 0;
	public static final int FT_NEGOTIATING = 1;
	public static final int FT_SENDING = 2;
	public static final int FT_RECEIVING = 3;
	public static final int FT_SAYING_HELLO = 4;
	public static final int FT_SENDING_EOF = 5;
	public static final int FT_SENDING_BASIC = 6;
	
    // Log对象
    protected static Log log = LogFactory.getLog(FileWatcher.class);
	// 第一个监听端口
	protected int myMajorPort;
	// 第二个监听端口
	protected int myMinorPort;
	// 真实IP
	protected byte[] myLocalIp;
	// 外部IP
	protected byte[] myInternetIp;
	// 我的外部端口
	protected int myInternetPort;
	// 对方第一个监听端口
	protected int hisMajorPort;
	// 对方第二个监听端口
	protected int hisMinorPort;
	// 对方真实IP
	protected byte[] hisLocalIp;
	// 对方外部IP
	protected byte[] hisInternetIp;
	// 对方外部端口
	protected int hisInternetPort;
	// 我的文件会话密钥
	protected byte[] myFileSessionKey;
	// 对方的文件会话密钥
	protected byte[] hisFileSessionKey;
	// 文件中转服务器通讯密钥
	protected byte[] fileAgentKey;
	// 文件中转认证令牌
	protected byte[] fileAgentToken;
	// 对方的QQ号
	protected int hisQQ;
	// 我的QQ号
	protected int myQQ;
	// 我的头像号
	protected byte myFace;
	// 会话序列号，因为同时传多个文件是允许的，所以不同的会话之间序号不一样
	//     这个序号就是发送文件请求包中的那个序号，如果用户接受了，这个序号在
	//     以后的会话中将起到标识作用
	protected char sessionSequence;
	// 收发方的网络位置情况
	protected int condition;
	// 文件的大小
	protected int fileSize;
	// 发送的文件名称，或者接收的文件名称
	protected String fileName;
	// 文件的分片数
	protected int fragments;
	// 文件保存到本地的全路径名，如果我是发送者，那么这个和fileName表示的是同一个文件，
	//     如果我是接收者，那么可能不同
	protected String localFileName;
	// 随机存取文件，如果我是发送者，这个是用来读取发送文件的，如果我是接收者，这个用来
	//    保存文件
	protected RandomAccessFile localFile;
	// 滑窗，发送或者接收，只要一个就够了，因为要么接收，要么发送
	protected SlideWindow window;
	// 分片的最大字节数
	protected int maxFragmentSize;
	// 文件的两个md5
	protected byte[] fileMD5, fileNameMD5;
	// 文件数据信息包和文件控制信息包对象
	protected FileDataPacket fdp;
	protected FileControlPacket fcp;
	// Selector对象
	protected Selector selector;
	// 是否关闭文件守望者
	protected boolean shutdown;
	// 接收发送缓冲区
	protected ByteBuffer buffer;
	// 连接的协议是否是UDP
	protected boolean useUdp;
	// 连接的方式是否是直连
	protected boolean major;
	// 文件传输操作的当前状态
	protected int fileTransferStatus;
	// 包监视器
	protected FilePacketMonitor monitor;
	// FileListener数组
	protected List<IFileListener> listeners;
		
	/**
	 * @param udp 是否是udp方式
	 * @param parent FileWatcher所属的发送消息窗口
	 * @param me QQUser对象
	 */
	public FileWatcher() {
		QQUser me = null;
		myQQ = me.getQQ();
		myFileSessionKey = me.getFileSessionKey();
		myInternetIp = me.getIp();
		myInternetPort = me.getPort();
        try {
            myLocalIp = Util.getIpByteArrayFromString(InetAddress.getLocalHost().getHostAddress());
        } catch (UnknownHostException e) {
            log.error(e.getMessage());
        }
		myFace = (byte)(me.getContactInfo().head & 0xFF);
		fdp = new FileDataPacket(this);
		fcp = new FileControlPacket(this);
		monitor = new FilePacketMonitor();
		shutdown = false;
		fileTransferStatus = FT_NONE;
		listeners = new Vector<IFileListener>();
        buffer = ByteBuffer.allocateDirect(QQ.QQ_MAX_PACKET_SIZE);
	}
	
	/**
	 * 初始化滑窗
	 * @param size 窗口大小
	 * @param l 窗口初始下限
	 * @param m 窗口最大值
	 */
	public void initSlideWindow(int size, int l, int m) {
	    if(window == null)
	        window = new SlideWindow(size, l, m);
	}  
	
	/**
	 * 添加文件传输事件监听器
	 * @param listener
	 */
	public void addFileListener(IFileListener listener) {
	    listeners.add(listener);
	}
	
	/**
	 * 移除文件传输事件监听器
	 * @param listener
	 */
	public void removeFileListener(IFileListener listener) {
	    listeners.remove(listener);
	}
	
	/**
	 * 触发文件传输被取消事件
	 */
	protected void fireFileAbortedEvent() {
	    FileEvent e = new FileEvent(this);
		for(IFileListener listener : listeners)
			listener.fileAborted(e);
	}
	
	/**
	 * 触发文件传输完成事件
	 */
	protected void fireFileFinishedEvent() {
	    FileEvent e = new FileEvent(this);
		for(IFileListener listener : listeners)
			listener.fileFinished(e);
	}	
	
	/**
	 * 触发文件传送中事件
	 */
	protected void fireFileInProgressEvent() {
	    FileEvent e = new FileEvent(this);
		for(IFileListener listener : listeners)
			listener.fileInProgress(e);
	}	
	
	/**
	 * 触发连接建立事件
	 */
	protected void fireFileConnectedEvent() {
	    FileEvent e = new FileEvent(this);
		for(IFileListener listener : listeners)
			listener.fileConnected(e);
	}	
	
	/**
	 * 根据各方的IP情况，得到双方在网络上的处境
	 */
	protected void checkCondition() {
	    if(Util.isIpEquals(myInternetIp, hisInternetIp)) {
	        setCondition(QQ.QQ_SAME_LAN);
	    } else {
	        boolean amIBehindFirewall = !Util.isIpEquals(myInternetIp, myLocalIp);
	        boolean isHeBehindFirewall = !Util.isIpEquals(hisInternetIp, hisLocalIp);
	        if(amIBehindFirewall && isHeBehindFirewall)
	            setCondition(QQ.QQ_ALL_BEHIND_FIREWALL);
	        else if(amIBehindFirewall)
	            setCondition(QQ.QQ_I_AM_BEHIND_FIREWALL);
	        else if(isHeBehindFirewall)
	            setCondition(QQ.QQ_HE_IS_BEHIND_FIREWALL);
	        else
	            setCondition(QQ.QQ_NONE_BEHIND_FIREWALL);
	    }
	}
	
	/**
	 * 关闭守望者
	 */
	public abstract void shutdown();
	
	/**
	 * 取消文件传输
	 */
	public abstract void abort();
	
	/**
	 * 结束文件传输，处理一些善后事宜
	 */
	public abstract void finish();
	
	/**
	 * 打开本地文件准备读和写
	 */
	public abstract boolean openLocalFile();
	
	/**
	 * <pre>
	 * 启动守望者，对于接收者，他将选择一条链路进行连接准备传输
	 * 对于发送者，他将启动两个端口等待对方应答
	 * </pre>
	 */
	public abstract void start();     
	
    /**
     * @return Returns the hisFirstPort.
     */
    public int getHisMajorPort() {
        return hisMajorPort;
    }
    
    /**
     * @param hisFirstPort The hisFirstPort to set.
     */
    public void setHisMajorPort(int hisFirstPort) {
        this.hisMajorPort = hisFirstPort;
    }
    
    /**
     * @return Returns the hisSecondPort.
     */
    public int getHisMinorPort() {
        return hisMinorPort;
    }
    
    /**
     * @param hisSecondPort The hisSecondPort to set.
     */
    public void setHisMinorPort(int hisSecondPort) {
        this.hisMinorPort = hisSecondPort;
    }
    
    /**
     * @return Returns the myFirstPort.
     */
    public int getMyMajorPort() {
        return myMajorPort;
    }
    
    /**
     * @param myFirstPort The myFirstPort to set.
     */
    public void setMyMajorPort(int myFirstPort) {
        this.myMajorPort = myFirstPort;
    }
    
    /**
     * @return Returns the mySecondPort.
     */
    public int getMyMinorPort() {
        return myMinorPort;
    }
    
    /**
     * @param mySecondPort The mySecondPort to set.
     */
    public void setMyMinorPort(int mySecondPort) {
        this.myMinorPort = mySecondPort;
    }
    
    /**
     * @return Returns the myExternalPort.
     */
    public int getMyInternetPort() {
        return myInternetPort;
    }
    
    /**
     * @param myExternalPort The myExternalPort to set.
     */
    public void setMyInternetPort(int myExternalPort) {
        this.myInternetPort = myExternalPort;
    }
    
    /**
     * @return Returns the hisFileSessionKey.
     */
    public byte[] getHisFileSessionKey() {
        return hisFileSessionKey;
    }
    
    /**
     * @param hisFileSessionKey The hisFileSessionKey to set.
     */
    public void setHisFileSessionKey(byte[] hisFileSessionKey) {
        this.hisFileSessionKey = hisFileSessionKey;
    }    
  
    /**
     * @return Returns the hisQQ.
     */
    public int getHisQQ() {
        return hisQQ;
    }
    
    /**
     * @param hisQQ The hisQQ to set.
     */
    public void setHisQQ(int hisQQ) {
        this.hisQQ = hisQQ;
    }
    
    /**
     * @return Returns the myFileSessionKey.
     */
    public byte[] getMyFileSessionKey() {
        return myFileSessionKey;
    }
    
    /**
     * @param myFileSessionKey The myFileSessionKey to set.
     */
    public void setMyFileSessionKey(byte[] myFileSessionKey) {
        this.myFileSessionKey = myFileSessionKey;
    }
    
    /**
     * @return Returns the myQQ.
     */
    public int getMyQQ() {
        return myQQ;
    }
    
    /**
     * @param myQQ The myQQ to set.
     */
    public void setMyQQ(int myQQ) {
        this.myQQ = myQQ;
    }
    
    /**
     * @return Returns the sessionSequence.
     */
    public char getSessionSequence() {
        return sessionSequence;
    }
    
    /**
     * @param sessionSequence The sessionSequence to set.
     */
    public void setSessionSequence(char sessionSequence) {
        this.sessionSequence = sessionSequence;
    }
    
    /**
     * @return Returns the face.
     */
    public byte getMyFace() {
        return myFace;
    }
    
    /**
     * @param face The face to set.
     */
    public void setMyFace(byte face) {
        this.myFace = face;
    }
    
    /**
     * @return Returns the hisExternalIp.
     */
    public byte[] getHisInternetIp() {
        return hisInternetIp;
    }
    
    /**
     * @param hisInternetIp The hisExternalIp to set.
     */
    public void setHisInternetIp(byte[] hisInternetIp) {
        this.hisInternetIp = hisInternetIp;
    }
    
    /**
     * @return Returns the hisInternalIp.
     */
    public byte[] getHisLocalIp() {
        return hisLocalIp;
    }
    
    /**
     * @param hisLocalIp The hisInternalIp to set.
     */
    public void setHisLocalIp(byte[] hisLocalIp) {
        this.hisLocalIp = hisLocalIp;
    }
    
    /**
     * @return Returns the myExternalIp.
     */
    public byte[] getMyInternetIp() {
        return myInternetIp;
    }
    
    /**
     * @param myInternetIp The myExternalIp to set.
     */
    public void setMyInternetIp(byte[] myInternetIp) {
        this.myInternetIp = myInternetIp;
    }
    
    /**
     * @return Returns the myInternalIp.
     */
    public byte[] getMyLocalIp() {
        return myLocalIp;
    }
    
    /**
     * @param myLocalIp The myInternalIp to set.
     */
    public void setMyLocalIp(byte[] myLocalIp) {
        this.myLocalIp = myLocalIp;
    }

    /**
     * @return Returns the hisExternalPort.
     */
    public int getHisInternetPort() {
        return hisInternetPort;
    }
    
    /**
     * @param hisExternalPort The hisExternalPort to set.
     */
    public void setHisInternetPort(int hisExternalPort) {
        this.hisInternetPort = hisExternalPort;
    }
    
    /**
     * @return Returns the connectType.
     */
    public int getCondition() {
        return condition;
    }
    
    /**
     * @param connectType The connectType to set.
     */
    public void setCondition(int connectType) {
        this.condition = connectType;
    }
    
    /**
     * @return Returns the fileName.
     */
    public String getFileName() {
        return fileName;
    }
    
    /**
     * @param fileName The fileName to set.
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    /**
     * @return Returns the fileSize.
     */
    public int getFileSize() {
        return fileSize;
    }
    
    /**
     * @param fileSize The fileSize to set.
     */
    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }
    
    /**
     * @return Returns the fragments.
     */
    public int getFragments() {
        return fragments;
    }
    
    /**
     * @param fragments The fragments to set.
     */
    public void setFragments(int fragments) {
        this.fragments = fragments;
    }
    
    /**
     * @return Returns the localFileName.
     */
    public String getLocalFileName() {
        return localFileName;
    }
    
    /**
     * @param localFileName The localFileName to set.
     */
    public void setLocalFileName(String localFileName) {
        this.localFileName = localFileName;
    }
    
    /**
     * @return Returns the maxFragmentSize.
     */
    public int getMaxFragmentSize() {
        return maxFragmentSize;
    }
    
    /**
     * @param maxFragmentSize The maxFragmentSize to set.
     */
    public void setMaxFragmentSize(int maxFragmentSize) {
        this.maxFragmentSize = maxFragmentSize;
    }
    
    /**
     * @return Returns the firstMD5.
     */
    public byte[] getFileMD5() {
        return fileMD5;
    }
    
    /**
     * @param firstMD5 The firstMD5 to set.
     */
    public void setFileMD5(byte[] firstMD5) {
        this.fileMD5 = firstMD5;
    }
    
    /**
     * @return Returns the secondMD5.
     */
    public byte[] getFileNameMD5() {
        return fileNameMD5;
    }
    
    /**
     * @param secondMD5 The secondMD5 to set.
     */
    public void setFileNameMD5(byte[] secondMD5) {
        this.fileNameMD5 = secondMD5;
    }
    
    /**
     * @return Returns the fileTransferStatus.
     */
    public int getFileTransferStatus() {
        return fileTransferStatus;
    }
    
    /**
     * @param fileTransferStatus The fileTransferStatus to set.
     */
    public void setFileTransferStatus(int fileTransferStatus) {
        this.fileTransferStatus = fileTransferStatus;
    }
    
    /**
     * @return 本地文件的路径
     */
    public String getLocalFilePath() {
        File file = new File(localFileName);
        return file.getParentFile().getAbsolutePath();
    }
    
    /**
     * @return 滑窗对象
     */
    public SlideWindow getSlideWindow() {
        return window;
    }
    
    /**
     * @return
     */
    public boolean isUseUdp() {
        return useUdp;
    }
    
    /**
     * @return Returns the fileAgentKey.
     */
    public byte[] getFileAgentKey() {
        return fileAgentKey;
    }
    
    /**
     * @param fileAgentKey The fileAgentKey to set.
     */
    public void setFileAgentKey(byte[] fileAgentKey) {
        this.fileAgentKey = fileAgentKey;
    }
    
    /**
     * @return Returns the fileAgentToken.
     */
    public byte[] getFileAgentToken() {
        return fileAgentToken;
    }
    
    /**
     * @param fileAgentToken The fileAgentToken to set.
     */
    public void setFileAgentToken(byte[] fileAgentToken) {
        this.fileAgentToken = fileAgentToken;
    }
}
