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
package edu.tsinghua.lumaqq.qq.packets.in.disk;

import static edu.tsinghua.lumaqq.qq.events.QQEvent.*;
import java.nio.ByteBuffer;

import edu.tsinghua.lumaqq.qq.QQ;
import edu.tsinghua.lumaqq.qq.annotation.DocumentalPacket;
import edu.tsinghua.lumaqq.qq.annotation.LinkedEvent;
import edu.tsinghua.lumaqq.qq.annotation.PacketName;
import edu.tsinghua.lumaqq.qq.annotation.RelatedPacket;
import edu.tsinghua.lumaqq.qq.beans.QQUser;
import edu.tsinghua.lumaqq.qq.packets.DiskInPacket;
import edu.tsinghua.lumaqq.qq.packets.PacketParseException;
import edu.tsinghua.lumaqq.qq.packets.out.disk.RenamePacket;

/**
 * <pre>
 * 重命名回复包
 * 1. 头部
 * 2. 名称修改成功时间，4字节
 * </pre>
 *
 * @author luma
 */
@DocumentalPacket
@PacketName("文件目录改名回复包")
@RelatedPacket({RenamePacket.class})
@LinkedEvent({QQ_DISK_RENAME_SUCCESS})
public class RenameReplyPacket extends DiskInPacket {
	public long modifiedTime;
	
	public RenameReplyPacket(ByteBuffer buf, int length, QQUser user) throws PacketParseException {
		super(buf, length, user);
	}

	@Override
	public String getPacketName() {
		return "Rename Reply Packet";
	}
	
	@Override
	protected void parseBody(ByteBuffer buf) throws PacketParseException {
		if(replyCode == QQ.QQ_REPLY_OK) 
			modifiedTime = (long)buf.getInt() * 1000L;
	}
}
