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
import edu.tsinghua.lumaqq.qq.packets.in.disk.ListMyDiskDirReplyPacket;

/**
 * <pre>
 * 列出我的网络硬盘目录
 * 1. 头部
 * 2. 我的QQ号，4字节
 * 3. 目录id，4字节
 * 4. 文件属性标志，4字节
 * Note: 文件属性可以用来过滤得到的结果，只会得到具有你指定属性的目录和文件。
 * </pre>
 *
 * @author luma
 */
@DocumentalPacket
@PacketName("获取我的网络硬盘目录列表请求包")
@RelatedPacket({ListMyDiskDirReplyPacket.class})
public class ListMyDiskDirPacket extends DiskOutPacket {
	private int dirId;
	private int flag;
	
	public ListMyDiskDirPacket(ByteBuffer buf, int length, QQUser user) throws PacketParseException {
		super(buf, length, user);
	}

	public ListMyDiskDirPacket(QQUser user) {
		super(QQ.QQ_DISK_CMD_LIST_MY_DISK_DIR, user);
		dirId = QQ.QQ_DISK_DIR_ROOT;
		flag = 0;
	}
	
	@Override
	public String getPacketName() {
		return "List My Disk Dir Packet";
	}

	@Override
	protected void putBody(ByteBuffer buf) {
		buf.putInt(user.getQQ());
		buf.putInt(dirId);
		buf.putInt(flag);
	}

	/**
	 * @return the dirId
	 */
	public int getDirId() {
		return dirId;
	}

	/**
	 * @param dirId the dirId to set
	 */
	public void setDirId(int dirId) {
		this.dirId = dirId;
	}

	/**
	 * @return the unknown
	 */
	public int getFlag() {
		return flag;
	}

	/**
	 * @param unknown the unknown to set
	 */
	public void setFlag(int unknown) {
		this.flag = unknown;
	}
}
