/*
* LumaQQ - Java QQ Client
*
* Copyright (C) 2004 notXX
*                    luma <stubma@163.com>
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
package edu.tsinghua.lumaqq.qq.packets;

import java.nio.ByteBuffer;

import edu.tsinghua.lumaqq.qq.QQ;
import edu.tsinghua.lumaqq.qq.Util;
import edu.tsinghua.lumaqq.qq.annotation.BasePacket;
import edu.tsinghua.lumaqq.qq.annotation.DocumentalPacket;
import edu.tsinghua.lumaqq.qq.beans.QQUser;

/**
 * <pre>
 * 基本协议族的输入包基类:
 * 1. 包头标志，1字节，0x02
 * 2. 服务器端版本代码, 2字节
 * 3. 命令，2字节
 * 4. 包序号，2字节
 * 5. 包体
 * 6. 包尾标志，1字节，0x03
 * <pre>
 * 
 * @author notxx
 * @author luma
 */
@DocumentalPacket
@BasePacket(name="基本协议族输入包", klass=BasicInPacket.class)
public abstract class BasicInPacket extends InPacket {
	/**
	 * @param command
	 * @param user
	 */
	public BasicInPacket(char command, QQUser user) {
		super(QQ.QQ_HEADER_BASIC_FAMILY, QQ.QQ_SERVER_VERSION_0100, command, user);
	}
	
	/**
	 * 构造一个指定参数的包.从buf的当前位置开始解析直到limit
	 * 
	 * @param buf ByteBuffer对象
	 * @throws PacketParseException
	 *                    内容解析出错.
	 */
	public BasicInPacket(ByteBuffer buf, QQUser user) throws PacketParseException {
	    super(buf, user);
	}
	
	/**
	 * 构造一个InPacket，从buf的当前位置解析length个字节
	 * 
	 * @param buf ByteBuffer对象
	 * @param length 字节数
	 * @throws PacketParseException
	 *  				  内容解析出错
	 */
	public BasicInPacket(ByteBuffer buf, int length, QQUser user) throws PacketParseException {
	    super(buf, length, user);
	}
	
	/* (non-Javadoc)
     * @see edu.tsinghua.lumaqq.qq.packets.Packet#parseHeader(java.nio.ByteBuffer)
     */
	@Override
    protected void parseHeader(ByteBuffer buf) throws PacketParseException {
	    if(!user.isUdp())
	        buf.getChar();
	    header = buf.get();
		source = buf.getChar();
		command = buf.getChar();
		sequence = buf.getChar();
    }
    
    /* (non-Javadoc)
     * @see edu.tsinghua.lumaqq.qq.packets.Packet#parseTail(java.nio.ByteBuffer)
     */
	@Override
    protected void parseTail(ByteBuffer buf) {
        buf.get();
    }
    
    /* (non-Javadoc)
     * @see edu.tsinghua.lumaqq.qq.packets.Packet#putHead(java.nio.ByteBuffer)
     */
	@Override
    protected void putHead(ByteBuffer buf) {
	    if(!user.isUdp())
	        buf.putChar((char)0);
		buf.put(getHeader());
		buf.putChar(source);
		buf.putChar(command);
		buf.putChar(sequence);
    }
    
    /* (non-Javadoc)
     * @see edu.tsinghua.lumaqq.qq.packets.Packet#putBody(java.nio.ByteBuffer)
     */
	@Override
    protected void putBody(ByteBuffer buf) {
    }
    
    /* (non-Javadoc)
     * @see edu.tsinghua.lumaqq.qq.packets.Packet#putTail(java.nio.ByteBuffer)
     */
	@Override
    protected void putTail(ByteBuffer buf) {
		buf.put(QQ.QQ_TAIL_BASIC_FAMILY);
    }

    /* (non-Javadoc)
     * @see edu.tsinghua.lumaqq.qq.packets.Packet#getPacketName()
     */
	@Override
    public String getPacketName() {
        return "Unknown Incoming Packet - 0x" + Integer.toHexString(command).toUpperCase();
    }
    
	/* (non-Javadoc)
	 * @see edu.tsinghua.lumaqq.qq.packets.Packet#decryptBody(byte[], int, int)
	 */
	@Override
	protected byte[] decryptBody(byte[] buf, int offset, int len) {
	    byte[] temp = null;
	    switch(command) {
	        case QQ.QQ_CMD_REQUEST_LOGIN_TOKEN:
	            byte[] undecrypted = new byte[len];
	        	System.arraycopy(buf, offset, undecrypted, 0, len);
	            return undecrypted;
	        case QQ.QQ_CMD_LOGIN:
		        temp = crypter.decrypt(buf, offset, len, user.getPasswordKey());
		        if(temp == null)
		            temp = crypter.decrypt(buf, offset, len, user.getInitKey());
		        return temp;
		    default:
		        if(user.getSessionKey() != null)
			        temp = crypter.decrypt(buf, offset, len, user.getSessionKey());
		        if(temp == null)
		            temp = crypter.decrypt(buf, offset, len, user.getPasswordKey());
		        return temp;
	    }
	}
	
	/* (non-Javadoc)
     * @see edu.tsinghua.lumaqq.qq.packets.Packet#encryptBody(byte[], int, int)
     */
	@Override
    protected byte[] encryptBody(byte[] b, int offset, int length) {
        return null;
    }
    
    /* (non-Javadoc)
     * @see edu.tsinghua.lumaqq.qq.packets.Packet#getBodyBytes(java.nio.ByteBuffer, int)
     */
	@Override
    protected byte[] getBodyBytes(ByteBuffer buf, int length) {
	    // 得到包体长度
	    int bodyLen = length - QQ.QQ_LENGTH_BASIC_FAMILY_IN_HEADER - QQ.QQ_LENGTH_BASIC_FAMILY_TAIL;
	    if(!user.isUdp()) bodyLen -= 2;	    
	    // 得到加密的包体内容
	    byte[] body = new byte[bodyLen]; 
	    buf.get(body);
	    return body;
    }
    
    /* (non-Javadoc)
     * @see edu.tsinghua.lumaqq.qq.packets.Packet#getLength(int)
     */
	@Override
    protected int getLength(int bodyLength) {
        return QQ.QQ_LENGTH_BASIC_FAMILY_IN_HEADER + QQ.QQ_LENGTH_BASIC_FAMILY_TAIL + bodyLength + (user.isUdp() ? 0 : 2);
    }
    
    /* (non-Javadoc)
     * @see edu.tsinghua.lumaqq.qq.packets.Packet#getHeadLength()
     */
	@Override
    protected int getHeadLength() {
        return QQ.QQ_LENGTH_BASIC_FAMILY_IN_HEADER + (user.isUdp() ? 0 : 2);
    }
    
    /* (non-Javadoc)
     * @see edu.tsinghua.lumaqq.qq.packets.Packet#getTailLength()
     */
	@Override
    protected int getTailLength() {
        return QQ.QQ_LENGTH_BASIC_FAMILY_TAIL;
    }    
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
	@Override
    public String toString() {
        return "包类型: " + Util.getCommandString(command) + " 序号: " + (int)sequence; 
    }
    
    /* (non-Javadoc)
     * @see edu.tsinghua.lumaqq.qq.packets.Packet#getCryptographStart()
     */
	@Override
    protected int getCryptographStart() {
        return -1;
    }
	
	@Override
	public int getFamily() {
		return QQ.QQ_PROTOCOL_FAMILY_BASIC;
	}
}