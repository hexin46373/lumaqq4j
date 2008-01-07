/*
* LumaQQ - Java QQ Client
*
* Copyright (C) 2004 luma <stubma@163.com>
*                    notXX
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
 * 包头是05系列的输出包，格式为
 * 1. 包头标识，1字节
 * 2. source，2字节
 * 3. 包长度，2字节
 * 4. 包命令，2字节
 * 5. 包序号，2字节
 * 6. 用户QQ号，4字节
 * 7. 包体
 * 8. 包尾，1字节
 * 
 * 值得注意的是：这种包的包体并非完全加密型，而是部分加密型
 * </pre>
 * 
 * @author luma
 */
@DocumentalPacket
@BasePacket(name="05协议族输出包", klass=_05OutPacket.class)
public abstract class _05OutPacket extends OutPacket {
    private int qqNum;

    /**
     * @param header
     * @param command
     * @param ack
     * @param user
     */
    public _05OutPacket(char command, boolean ack, QQUser user) {
        super(QQ.QQ_HEADER_05_FAMILY, command, ack, user);
    }

    /**
     * @param buf
     * @param length
     * @param user
     * @throws PacketParseException
     */
    public _05OutPacket(ByteBuffer buf, int length, QQUser user) throws PacketParseException {
        super(buf, length, user);
    }
    
    /**
     * @param buf
     * @param user
     * @throws PacketParseException
     */
    public _05OutPacket(ByteBuffer buf, QQUser user) throws PacketParseException {
	    this(buf, buf.limit() - buf.position(), user);
    }
    
    /* (non-Javadoc)
     * @see edu.tsinghua.lumaqq.qq.packets.Packet#getLength(int)
     */
	@Override
    protected int getLength(int bodyLength) {
        return QQ.QQ_LENGTH_05_FAMILY_HEADER + QQ.QQ_LENGTH_05_FAMILY_TAIL + bodyLength;
    }

    /* (non-Javadoc)
     * @see edu.tsinghua.lumaqq.qq.packets.Packet#validateHeader()
     */
	@Override
    protected boolean validateHeader() {
        return qqNum == user.getQQ();
    }
    
    /* (non-Javadoc)
     * @see edu.tsinghua.lumaqq.qq.packets.Packet#getHeadLength()
     */
	@Override
    protected int getHeadLength() {
        return QQ.QQ_LENGTH_05_FAMILY_HEADER;
    }
    
    /* (non-Javadoc)
     * @see edu.tsinghua.lumaqq.qq.packets.Packet#getTailLength()
     */
	@Override
    protected int getTailLength() {
        return QQ.QQ_LENGTH_05_FAMILY_TAIL;
    }
    
    /* (non-Javadoc)
     * @see edu.tsinghua.lumaqq.qq.packets.OutPacket#getPacketName()
     */
	@Override
    public String getPacketName() {
        return "Unknown Outcoming Packet - 05 Family";
    }
    
    /* (non-Javadoc)
     * @see edu.tsinghua.lumaqq.qq.packets.Packet#putHead(java.nio.ByteBuffer)
     */
	@Override
    protected void putHead(ByteBuffer buf) {
        buf.put(QQ.QQ_HEADER_05_FAMILY);
        buf.putChar(source);
        buf.putChar((char)0);
        buf.putChar(command);
        buf.putChar(sequence);
        buf.putInt(user.getQQ());
    }
    
    /* (non-Javadoc)
     * @see edu.tsinghua.lumaqq.qq.packets.Packet#putTail(java.nio.ByteBuffer)
     */
	@Override
    protected void putTail(ByteBuffer buf) {
        buf.put(QQ.QQ_TAIL_05_FAMILY);
    }
    
    /* (non-Javadoc)
     * @see edu.tsinghua.lumaqq.qq.packets.OutPacket#postFill(java.nio.ByteBuffer, int)
     */
	@Override
    protected void postFill(ByteBuffer buf, int startPos) {
        buf.putChar(startPos + 3, (char)(buf.position() - startPos));
    }
    
    /* (non-Javadoc)
     * @see edu.tsinghua.lumaqq.qq.packets.Packet#parseHeader(java.nio.ByteBuffer)
     */
	@Override
    protected void parseHeader(ByteBuffer buf) throws PacketParseException {
        header = buf.get();
        source = buf.getChar();
        buf.getChar();
        command = buf.getChar();
        sequence = buf.getChar();
        qqNum = buf.getInt();
    }
    
    /* (non-Javadoc)
     * @see edu.tsinghua.lumaqq.qq.packets.Packet#parseTail(java.nio.ByteBuffer)
     */
	@Override
    protected void parseTail(ByteBuffer buf) throws PacketParseException {
        buf.get();
    }
    
    /* (non-Javadoc)
     * @see edu.tsinghua.lumaqq.qq.packets.Packet#decryptBody(byte[], int, int)
     */
	@Override
    protected byte[] decryptBody(byte[] body, int offset, int length) {
        // 解密密文部分
        int start = getCryptographStart();
        if(start == -1) {
            byte[] ret = new byte[length];
            System.arraycopy(body, offset, ret, 0, length);
            return ret;
        }
        byte[] decrypted = crypter.decrypt(body, start + offset, length - start, key != null ? key : user.getFileAgentKey());
        
        // 创建返回数组
        byte[] ret = new byte[start + decrypted.length];
        // 拷贝前面的明文部分
        System.arraycopy(body, offset, ret, 0, start);
        // 拷贝已解密部分
        System.arraycopy(decrypted, 0, ret, start, decrypted.length);
        return ret;
    }
    
    /* (non-Javadoc)
     * @see edu.tsinghua.lumaqq.qq.packets.Packet#encryptBody(byte[], int, int)
     */
	@Override
    protected byte[] encryptBody(byte[] b, int offset, int length) {
        // 加密需要加密的部分
        int start = getCryptographStart();
        if(start == -1) {
            byte[] ret = new byte[length];
            System.arraycopy(b, offset, ret, 0, length);
            return ret;
        }
        byte[] enc = crypter.encrypt(b, start + offset, length - start, key != null ? key : user.getFileAgentKey());
        
        // 创建返回数组
        byte[] ret = new byte[start + enc.length];
        // 拷贝前面的明文部分
        System.arraycopy(b, offset, ret, 0, start);
        // 拷贝后面已经加密的部分
        System.arraycopy(enc, 0, ret, start, enc.length);
        return ret;
    }
    
    /* (non-Javadoc)
     * @see edu.tsinghua.lumaqq.qq.packets.Packet#getBodyBytes(java.nio.ByteBuffer, int)
     */
	@Override
    protected byte[] getBodyBytes(ByteBuffer buf, int length) {
	    // 得到包体长度
	    int bodyLen = length - QQ.QQ_LENGTH_05_FAMILY_HEADER - QQ.QQ_LENGTH_05_FAMILY_TAIL; 
	    // 得到包体内容
	    byte[] body = new byte[bodyLen]; 
	    buf.get(body);
	    return body;
    } 
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
	@Override
    public String toString() {
        return "包类型: " + Util.get05CommandString(command) + " 序号: " + (int)sequence; 
    }
	
	@Override
	public int getFamily() {
		return QQ.QQ_PROTOCOL_FAMILY_05;
	}
}
