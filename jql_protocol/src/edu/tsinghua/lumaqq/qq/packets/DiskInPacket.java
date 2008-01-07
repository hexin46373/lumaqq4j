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
package edu.tsinghua.lumaqq.qq.packets;

import java.nio.ByteBuffer;

import edu.tsinghua.lumaqq.qq.QQ;
import edu.tsinghua.lumaqq.qq.Util;
import edu.tsinghua.lumaqq.qq.annotation.BasePacket;
import edu.tsinghua.lumaqq.qq.annotation.DocumentalPacket;
import edu.tsinghua.lumaqq.qq.beans.QQUser;

/**
 * <pre>
 * 网络硬盘协议族输入包
 * 1. 包长，4字节，exclusive
 * 2. 版本标识，2字节
 * Note: 虽然2部分目前起名叫做版本标识，但是不能很肯定，确实就是这个意思，对于输入包来说，其版本标识
 * 既可能和输出包不一样，也可能一样，而且两种情况似乎有不同的含义。对于目前来说，假设这个字段是版本标识，
 * 则当其为服务器端标识时，标识通知客户端开始会话。当其和输出包标志一致时，表示回复包
 * 3. 未知的2字节
 * 4. 命令，2字节
 * 5. 回复码，4字节
 * 6. 回复消息长度，4字节
 * 7. 回复消息
 * 8. 未知内容
 * Note: 5-8部分共72字节，8部分长度为72 - (5,6,7)长度
 * 9. 包体
 * 
 * Note: 此协议族包体不加密，并且只用于TCP连接
 * </pre>
 *
 * @author luma
 */
@DocumentalPacket
@BasePacket(name="网络硬盘协议族输入包", klass=DiskInPacket.class)
public abstract class DiskInPacket extends InPacket {
	public int replyCode;
	public String replyMessage;
	
	public DiskInPacket(ByteBuffer buf, int length, QQUser user) throws PacketParseException {
		super(buf, length, user);
	}

	public DiskInPacket(char command, QQUser user) {
		super((byte)0, QQ.QQ_DISK_CLIENT_VERSION, command, user);
	}

	@Override
	protected int getLength(int bodyLength) {
		return bodyLength + QQ.QQ_LENGTH_DISK_FAMILY_IN_HEADER;
	}

	@Override
	protected int getHeadLength() {
		return QQ.QQ_LENGTH_DISK_FAMILY_IN_HEADER;
	}

	@Override
	protected int getTailLength() {
		return 0;
	}

	@Override
	protected void putHead(ByteBuffer buf) {
		// length
		buf.putInt(0);
		// source
		buf.putChar(source);
		// unknown
		buf.putChar((char)0);
		// command
		buf.putChar(command);
		// reply code
		buf.putInt(replyCode);
		// reply message length
		byte[] b = (replyMessage == null) ? null : Util.getBytes(replyMessage);
		int len = (b == null) ? 0 : b.length;
		buf.putInt(len);
		// reply message
		if(b != null)
			buf.put(b);
		len = 64 - len;
		// fill to 72 bytes
		for(int i = 0; i < len; i++)
			buf.put((byte)0);
	}

	@Override
	protected void putBody(ByteBuffer buf) {
	}

	@Override
	protected byte[] getBodyBytes(ByteBuffer buf, int length) {
	    // 得到包体长度
	    int bodyLen = length - QQ.QQ_LENGTH_DISK_FAMILY_IN_HEADER;
	    // 得到包体内容
	    byte[] body = new byte[bodyLen]; 
	    buf.get(body);
	    return body;
	}

	@Override
	public int getFamily() {
		return QQ.QQ_PROTOCOL_FAMILY_DISK;
	}

	@Override
	protected void putTail(ByteBuffer buf) {
	}

	@Override
	protected byte[] encryptBody(byte[] b, int offset, int length) {
		byte[] ret = new byte[length];
		System.arraycopy(b, offset, ret, 0, length);
		return ret;
	}

	@Override
	protected byte[] decryptBody(byte[] body, int offset, int length) {
		byte[] ret = new byte[length];
		System.arraycopy(body, offset, ret, 0, length);
		return ret;
	}

	@Override
	protected int getCryptographStart() {
		return -1;
	}

	@Override
	protected void parseHeader(ByteBuffer buf) throws PacketParseException {
		buf.getInt();
		source = buf.getChar();
		buf.getChar();
		command = buf.getChar();
		replyCode = buf.getInt();
		int len = buf.getInt();
		replyMessage = Util.getString(buf, len);
		buf.position(buf.position() + 64 - len);
	}

	@Override
	protected void parseTail(ByteBuffer buf) throws PacketParseException {
	}

	@Override
	public int hashCode() {
		return (source << 16) | command;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (obj instanceof Packet) {
			Packet packet = (Packet)obj;
			return source == packet.source && command == packet.command;
		} else
			return super.equals(obj);
	}
}
