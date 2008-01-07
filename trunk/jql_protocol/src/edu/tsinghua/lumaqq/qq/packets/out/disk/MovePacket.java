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
import edu.tsinghua.lumaqq.qq.packets.in.disk.MoveReplyPacket;

/**
 * <pre>
 * 移动文件或目录的请求包
 * 1. 头部
 * 2. id, 30字节，不足填0
 * Note: 对于目录来说，要把id转换为10进制字符串
 * 3. 属性，4字节
 * 4. 当前目录id，4字节
 * 5. 移动到的目录id，4字节
 * </pre>
 *
 * @author luma
 */
@DocumentalPacket
@PacketName("移动文件请求包")
@RelatedPacket({MoveReplyPacket.class})
public class MovePacket extends DiskOutPacket {
	private String id;
	private int fromId;
	private int toId;
	private int property;

	public MovePacket(ByteBuffer buf, int length, QQUser user) throws PacketParseException {
		super(buf, length, user);
	}

	public MovePacket(QQUser user) {
		super(QQ.QQ_DISK_CMD_MOVE, user);
		property = 0;
	}
	
	@Override
	public String getPacketName() {
		return "Move Packet";
	}

	@Override
	protected void putBody(ByteBuffer buf) {
		byte[] b = Util.getBytes(id);
		if(b.length >= 30)
			buf.put(b, 0, 30);
		else {
			buf.put(b);
			for(int i = 0; i < 30 - b.length; i++)
				buf.put((byte)0);
		}
		buf.putInt(property);
		buf.putInt(fromId);
		buf.putInt(toId);
	}

	/**
	 * @return the fromId
	 */
	public int getFromId() {
		return fromId;
	}

	/**
	 * @param fromId the fromId to set
	 */
	public void setFromId(int fromId) {
		this.fromId = fromId;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
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
	 * @return the toId
	 */
	public int getToId() {
		return toId;
	}

	/**
	 * @param toId the toId to set
	 */
	public void setToId(int toId) {
		this.toId = toId;
	}
}
