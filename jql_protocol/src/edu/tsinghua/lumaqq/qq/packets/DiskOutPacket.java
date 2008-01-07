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
 * QQ硬盘协议族输出包
 * 1. 包长，4字节，exclusive
 * 2. 版本标识，2字节
 * 3. 未知的2字节
 * 4. 命令，2字节
 * 5. 未知的60字节
 * 6. 我的ip的字符串形式，15字节，不足填0
 * 7. 未知的69字节
 * 8. 包体
 * 
 * Note: 此协议族包体不加密，并且只用于TCP连接
 * </pre>
 *
 * @author luma
 */
@DocumentalPacket
@BasePacket(name="网络硬盘协议族输出包", klass=DiskOutPacket.class)
public abstract class DiskOutPacket extends OutPacket {
	private String localIp;
	
	public DiskOutPacket(ByteBuffer buf, int length, QQUser user) throws PacketParseException {
		super(buf, length, user);
	}

	public DiskOutPacket(char command, QQUser user) {
		super((byte)0, command, true, user);
		source = QQ.QQ_DISK_CLIENT_VERSION;
	}

	@Override
	protected void postFill(ByteBuffer buf, int startPos) {
		buf.putInt(startPos, buf.position() - startPos - 4);
	}

	@Override
	protected int getLength(int bodyLength) {
		return bodyLength + QQ.QQ_LENGTH_DISK_FAMILY_OUT_HEADER;
	}

	@Override
	protected boolean validateHeader() {
		return true;
	}

	@Override
	protected int getHeadLength() {
		return QQ.QQ_LENGTH_DISK_FAMILY_OUT_HEADER;
	}

	@Override
	protected int getTailLength() {
		return 0;
	}

	@Override
	protected void putHead(ByteBuffer buf) {
		buf.putInt(0);
		buf.putChar(source);
		buf.putChar((char)0);
		buf.putChar(command);
		// 未知的60字节
		for(int i = 0; i < 15; i++)
			buf.putInt(0);
		// ip
		byte[] b = Util.getBytes(localIp);
		buf.put(b);
		int emptyLen = 15 - b.length;
		for(int i = 0; i < emptyLen; i++)
			buf.put((byte)0);
		// 未知69字节
		for(int i = 0; i < 17; i++)
			buf.putInt(0);
		buf.put((byte)0);
	}

	@Override
	protected byte[] getBodyBytes(ByteBuffer buf, int length) {
	    // 得到包体长度
	    int bodyLen = length - QQ.QQ_LENGTH_DISK_FAMILY_OUT_HEADER;
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
		buf.position(buf.position() + 60);
		localIp = Util.getString(buf, (byte)0, 15);
		buf.position(buf.position() + 69);
	}

	@Override
	protected void parseTail(ByteBuffer buf) throws PacketParseException {
	}
	
	@Override
	public int hashCode() {
		return (source << 16) | command;
	}

	/**
	 * @return the localIp
	 */
	public String getLocalIp() {
		return localIp;
	}

	/**
	 * @param localIp the localIp to set
	 */
	public void setLocalIp(String localIp) {
		this.localIp = localIp;
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
