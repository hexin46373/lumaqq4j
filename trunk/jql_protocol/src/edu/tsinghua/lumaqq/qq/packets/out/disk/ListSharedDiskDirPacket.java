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
import edu.tsinghua.lumaqq.qq.annotation.DocumentalPacket;
import edu.tsinghua.lumaqq.qq.annotation.PacketName;
import edu.tsinghua.lumaqq.qq.annotation.RelatedPacket;
import edu.tsinghua.lumaqq.qq.beans.QQUser;
import edu.tsinghua.lumaqq.qq.packets.DiskOutPacket;
import edu.tsinghua.lumaqq.qq.packets.PacketParseException;
import edu.tsinghua.lumaqq.qq.packets.in.disk.ListSharedDiskDirReplyPacket;

/**
 * <pre>
 * 列出网络硬盘目录
 * 1. 头部
 * 2. 要列出目录的网络硬盘拥有者QQ号，4字节
 * </pre>
 *
 * @author luma
 */
@DocumentalPacket
@PacketName("获取共享网络硬盘目录列表请求包")
@RelatedPacket({ListSharedDiskDirReplyPacket.class})
public class ListSharedDiskDirPacket extends DiskOutPacket {
	private int diskOwner;


	public ListSharedDiskDirPacket(ByteBuffer buf, int length, QQUser user) throws PacketParseException {
		super(buf, length, user);
	}

	public ListSharedDiskDirPacket(QQUser user) {
		super(QQ.QQ_DISK_CMD_LIST_SHARED_DISK_DIR, user);
	}

	@Override
	public String getPacketName() {
		return "List Shared Disk Dir Packet";
	}
	
	@Override
	protected void putBody(ByteBuffer buf) {
		// disk owner
		buf.putInt(diskOwner);
	}

	/**
	 * @return the diskOwner
	 */
	public int getDiskOwner() {
		return diskOwner;
	}

	/**
	 * @param diskOwner the diskOwner to set
	 */
	public void setDiskOwner(int diskOwner) {
		this.diskOwner = diskOwner;
	}
}
