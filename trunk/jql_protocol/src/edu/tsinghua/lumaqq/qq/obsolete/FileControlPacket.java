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

import java.nio.ByteBuffer;

import edu.tsinghua.lumaqq.qq.QQ;
import edu.tsinghua.lumaqq.qq.packets.PacketParseException;

/**
 * <pre>
 * 文件控制信息包，文件控制信息包有一个很普遍的格式，只有少数包的格式有所不同
 * 1. 16个字节的session key和qq号的md5形式，用来做为文件传输时的会话密钥
 * 2. 命令类型，2字节
 * 3. 序号，2字节
 * 4. 发送时间，4字节
 * 5. 一个未知字节0
 * 6. 发送者头像，1字节
 * 7. 19个未用字节，全0
 * 8. 固定字节0x65
 * 9. 1个未知字节，可能是连接方式
 * 10. 外部IP，4字节
 * 11. 外部端口，2字节
 * 13. 第一个监听端口，2字节
 * 14. 真实IP，4字节
 * 15. 第二个监听端口，2字节
 * 
 * 一共61字节
 * 
 * 对于有些命令类型，不存在10－15部分，而是在9部分之后带一个未知字节，这个未知字节似乎
 * 需要原样返回，这样的命令有0x0031, 0x0032, 0x0033, 0x0034
 * </pre>
 * 
 * @author luma
 */
public class FileControlPacket extends FilePacket {
    // 每个包都有的字段
	private long time;
	private byte[] internetIp;
	private int internetPort;
	private int majorPort;
	private byte[] localIp;
	private int minorPort;
	// hello 包的字段
	private byte helloByte;
	// 命令
	private char command;
	// 内容缓冲区，因为文件控制信息包是需要加密的
	private ByteBuffer contentBuffer;
	
	/**
	 * 构造一个文件控制信息包对象
     * @param watcher FileWatcher对象
	 */
	public FileControlPacket(FileWatcher watcher) {
		super(watcher);
		contentBuffer = ByteBuffer.allocate(61);
	}
	
	/* (non-Javadoc)
	 * @see edu.tsinghua.lumaqq.qq.filetrans.FilePacket#initContent(java.nio.ByteBuffer)
	 */
	protected void fill(ByteBuffer out) {
	    super.fill(out);
	    out.put(getBody());
	}
	
	/* (non-Javadoc)
	 * @see edu.tsinghua.lumaqq.qq.filetrans.FilePacket#fill(java.nio.ByteBuffer, int)
	 */
	protected void fill(ByteBuffer out, int from) {
	    super.fill(out, from);
	    out.put(getBody());
	}
	
	/**
	 * 得到包体字节数组
	 * @return 已加密的包体数组
	 */
	private byte[] getBody() {
	    contentBuffer.clear();
		// 16个字节的文件传输会话密钥
	    contentBuffer.put(watcher.getMyFileSessionKey());
		// 命令类型
	    contentBuffer.putChar(command);
		// 序号
	    contentBuffer.putChar(watcher.sessionSequence);
		// 发送时间
	    contentBuffer.putInt((int)(System.currentTimeMillis() / 1000));
		// 未知字节0
	    contentBuffer.put((byte)0);
		// 发送者头像
	    contentBuffer.put(watcher.myFace);
		// 19个未用字节，全0
		contentBuffer.putLong(0);
		contentBuffer.putLong(0);
		contentBuffer.putChar((char)0);
		contentBuffer.put((byte)0);
		// 固定字节0x65
		contentBuffer.put((byte)0x65);
		// 未知字节0
		contentBuffer.put((byte)0);
		// 最后部分
		if(command != QQ.QQ_FILE_CMD_SENDER_SAY_HELLO 
		        && command != QQ.QQ_FILE_CMD_SENDER_SAY_HELLO_ACK 
		        && command != QQ.QQ_FILE_CMD_RECEIVER_SAY_HELLO 
		        && command != QQ.QQ_FILE_CMD_RECEIVER_SAY_HELLO_ACK) {
			// 外部ip
		    contentBuffer.put(watcher.myInternetIp);
			// 外部端口
		    contentBuffer.putChar((char)watcher.myInternetPort);
			// 主监听端口
		    contentBuffer.putChar((char)watcher.myMajorPort);
			// 真实IP和副监听端口
			if(command != QQ.QQ_FILE_CMD_YES_I_AM_BEHIND_FIREWALL && watcher.myLocalIp != null) {
			    contentBuffer.put(watcher.myLocalIp);
			    contentBuffer.putChar((char)watcher.myMinorPort);					
			} else {
			    contentBuffer.putInt(0);
			    contentBuffer.putChar((char)0);
			}
		} else {
			// 暂时这样搞
		    contentBuffer.put(helloByte);
		}
		// 加密
		byte[] backArray = contentBuffer.array();
		return crypter.encrypt(backArray, 0, contentBuffer.position(), watcher.getHisFileSessionKey());
	}
	
	/* (non-Javadoc)
	 * @see edu.tsinghua.lumaqq.qq.filetrans.FilePacket#parseContent(java.nio.ByteBuffer)
	 */
	protected void parse(ByteBuffer in) throws PacketParseException {
	    super.parse(in);
			
	    // 解密
	    byte[] buf = new byte[in.remaining()];
	    in.get(buf);
	    buf = crypter.decrypt(buf, watcher.getMyFileSessionKey());
	    if(buf == null)
	        throw new PacketParseException("文件控制信息包解析错误");
	    contentBuffer.clear();
	    contentBuffer.put(buf);
		// 对方的会话密钥
	    contentBuffer.position(QQ.QQ_LENGTH_KEY);
		// 命令类型
	    command = contentBuffer.getChar();
		// 序号
	    contentBuffer.getChar();
		// 发送时间
		time = (long)contentBuffer.getInt() * 1000L;
		// 未知字节
		// 发送者头像，1字节
		// 19个未用字节
		// 固定字节0x65
		// 未知字节0
		contentBuffer.position(contentBuffer.position() + 23);
		// 最后部分
		if(command != QQ.QQ_FILE_CMD_SENDER_SAY_HELLO 
		        && command != QQ.QQ_FILE_CMD_SENDER_SAY_HELLO_ACK
		        && command != QQ.QQ_FILE_CMD_RECEIVER_SAY_HELLO 
		        && command != QQ.QQ_FILE_CMD_RECEIVER_SAY_HELLO_ACK) {
			// 外部IP，4字节
		    internetIp = new byte[4];
		    contentBuffer.get(internetIp);
			// 外部端口，2字节
		    internetPort = contentBuffer.getChar();
			// 主监听端口，2字节
		    majorPort = contentBuffer.getChar();
			// 真实IP，4字节
		    localIp = new byte[4];
		    contentBuffer.get(localIp);
			// 副监听端口，2字节
		    minorPort = contentBuffer.getChar();
		} else
		    helloByte = contentBuffer.get();
	}	
	
    /**
     * @return Returns the helloByte.
     */
    public byte getHelloByte() {
        return helloByte;
    }
    
    /**
     * @param helloByte The helloByte to set.
     */
    public void setHelloByte(byte helloByte) {
        this.helloByte = helloByte;
    }
    
    /**
     * @return Returns the time.
     */
    public long getTime() {
        return time;
    }
    
    /**
     * @param time The time to set.
     */
    public void setTime(long time) {
        this.time = time;
    }

    /* (non-Javadoc)
     * @see edu.tsinghua.lumaqq.qq.filetrans.FilePacket#initFixedContent()
     */
    protected void initFixedFields() {
        tag = QQ.QQ_HEADER_P2P_FAMILY;
        source = QQ.QQ_CLIENT_VERSION;
        refreshKey();
        sender = watcher.getMyQQ();
        receiver = watcher.getHisQQ();
    }
    
    /**
     * @return Returns the command.
     */
    public char getCommand() {
        return command;
    }
    
    /**
     * @param command The command to set.
     */
    public void setCommand(char command) {
        this.command = command;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return (int)command;
    }    
    
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if(obj instanceof FileControlPacket)
            return ((FileControlPacket)obj).command == command;
        else
            return false;
    }
    
    /**
     * @return Returns the majorPort.
     */
    public int getMajorPort() {
        return majorPort;
    }
    
    /**
     * @param majorPort The majorPort to set.
     */
    public void setMajorPort(int directPort) {
        this.majorPort = directPort;
    }
    
    /**
     * @return Returns the internetIp.
     */
    public byte[] getInternetIp() {
        return internetIp;
    }
    
    /**
     * @param internetIp The internetIp to set.
     */
    public void setInternetIp(byte[] internetIp) {
        this.internetIp = internetIp;
    }
    
    /**
     * @return Returns the internetPort.
     */
    public int getInternetPort() {
        return internetPort;
    }
    
    /**
     * @param internetPort The internetPort to set.
     */
    public void setInternetPort(int internetPort) {
        this.internetPort = internetPort;
    }
    
    /**
     * @return Returns the localIp.
     */
    public byte[] getLocalIp() {
        return localIp;
    }
    
    /**
     * @param localIp The localIp to set.
     */
    public void setLocalIp(byte[] localIp) {
        this.localIp = localIp;
    }
    
    /**
     * @return Returns the minorPort.
     */
    public int getMinorPort() {
        return minorPort;
    }
    
    /**
     * @param minorPort The minorPort to set.
     */
    public void setMinorPort(int localPort) {
        this.minorPort = localPort;
    }
}
