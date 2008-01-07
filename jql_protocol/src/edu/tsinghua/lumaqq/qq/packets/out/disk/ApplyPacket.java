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
import edu.tsinghua.lumaqq.qq.packets.in.disk.ApplyReplyPacket;

/**
 * <pre>
 * 申请开通网络硬盘
 * 1. 头部
 * 2. 未知4字节
 * </pre>
 * 
 * @author luma
 */
@DocumentalPacket
@PacketName("开通网络硬盘请求包")
@RelatedPacket({ApplyReplyPacket.class})
public class ApplyPacket extends DiskOutPacket {
	public ApplyPacket(ByteBuffer buf, int length, QQUser user) throws PacketParseException {
		super(buf, length, user);
	}

	public ApplyPacket(QQUser user) {
		super(QQ.QQ_DISK_CMD_APPLY, user);	
	}
	
	@Override
	public String getPacketName() {
		return "Apply Packet";
	}

	@Override
	protected void putBody(ByteBuffer buf) {
		buf.putInt(0);
	}
}
