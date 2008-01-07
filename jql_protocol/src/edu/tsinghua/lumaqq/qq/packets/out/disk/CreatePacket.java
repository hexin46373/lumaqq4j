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
package edu.tsinghua.lumaqq.qq.packets.out.disk;

import java.nio.ByteBuffer;

import edu.tsinghua.lumaqq.qq.QQ;
import edu.tsinghua.lumaqq.qq.Util;
import edu.tsinghua.lumaqq.qq.annotation.DocumentalPacket;
import edu.tsinghua.lumaqq.qq.annotation.PacketName;
import edu.tsinghua.lumaqq.qq.annotation.RelatedPacket;
import edu.tsinghua.lumaqq.qq.beans.QQUser;
import edu.tsinghua.lumaqq.qq.packets.DiskOutPacket;
import edu.tsinghua.lumaqq.qq.packets.PacketParseException;
import edu.tsinghua.lumaqq.qq.packets.in.disk.CreateReplyPacket;

/**
 * <pre>
 * 创建文件或者目录请求包
 * 1. 头部
 * 2. 父目录id，4字节
 * 3. 属性，4字节
 * 4. 长度，4字节
 * 5. 长度，4字节
 * Note: 2个长度，不知原因。对于目录，长度是0
 * 6. 名称长度，2字节
 * 7. 名称
 * 8. 未知4字节
 * </pre>
 *
 * @author luma
 */
@DocumentalPacket
@PacketName("创建文件或目录请求包")
@RelatedPacket({CreateReplyPacket.class})
public class CreatePacket extends DiskOutPacket {
	private int parentId;
	private int property;
	private String name;
	private int length;

	public CreatePacket(ByteBuffer buf, int length, QQUser user) throws PacketParseException {
		super(buf, length, user);
	}

	public CreatePacket(QQUser user) {
		super(QQ.QQ_DISK_CMD_CREATE, user);
		property = QQ.QQ_DISK_FLAG_DIRECTORY;
		length = 0;
		ack = false;
		sendCount = 1;
	}

	@Override
	public String getPacketName() {
		return "Create Packet";
	}
	
	@Override
	protected void putBody(ByteBuffer buf) {
		buf.putInt(parentId);
		buf.putInt(property);
		buf.putInt(length);
		buf.putInt(length);
		byte[] b = Util.getBytes(name);
		buf.putChar((char)b.length);
		buf.put(b);
		buf.putInt(0);
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the parentId
	 */
	public int getParentId() {
		return parentId;
	}

	/**
	 * @param parentId the parentId to set
	 */
	public void setParentId(int parentId) {
		this.parentId = parentId;
	}

	/**
	 * @return the property
	 */
	public int getProperty() {
		return property;
	}

	/**
	 * @param property the property to set
	 */
	public void setProperty(int property) {
		this.property = property;
	}

	/**
	 * @return the length
	 */
	public int getLength() {
		return length;
	}

	/**
	 * @param length the length to set
	 */
	public void setLength(int length) {
		this.length = length;
	}
}
