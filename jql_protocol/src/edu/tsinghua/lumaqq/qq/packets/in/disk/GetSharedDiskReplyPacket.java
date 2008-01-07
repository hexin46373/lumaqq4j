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
import java.util.ArrayList;
import java.util.List;

import edu.tsinghua.lumaqq.qq.QQ;
import edu.tsinghua.lumaqq.qq.annotation.DocumentalPacket;
import edu.tsinghua.lumaqq.qq.annotation.LinkedEvent;
import edu.tsinghua.lumaqq.qq.annotation.PacketName;
import edu.tsinghua.lumaqq.qq.annotation.RelatedPacket;
import edu.tsinghua.lumaqq.qq.beans.QQUser;
import edu.tsinghua.lumaqq.qq.packets.DiskInPacket;
import edu.tsinghua.lumaqq.qq.packets.PacketParseException;
import edu.tsinghua.lumaqq.qq.packets.out.disk.GetSharedDiskPacket;

/**
 * <pre>
 * 请求共享网络硬盘的回复包
 * 1. 头部
 * 2. 共享网络硬盘个数，4字节
 * 3. 共享网络硬盘的拥有者QQ号，4字节
 * 4. 如果有更多共享网络硬盘，重复3部分
 * Note: 对于3部分来说，如果值是10000，则表示是腾讯提供的一些共享资源
 * </pre>
 *
 * @author luma
 */
@DocumentalPacket
@PacketName("获取共享网络硬盘列表回复包")
@RelatedPacket({GetSharedDiskPacket.class})
@LinkedEvent({QQ_DISK_GET_SHARED_DISK_SUCCESS})
public class GetSharedDiskReplyPacket extends DiskInPacket {
	public List<Integer> diskOwners;

	public GetSharedDiskReplyPacket(ByteBuffer buf, int length, QQUser user) throws PacketParseException {
		super(buf, length, user);
	}
	
	@Override
	public String getPacketName() {
		return "Get Shared Disk Reply Packet";
	}

	@Override
	protected void parseBody(ByteBuffer buf) throws PacketParseException {
		if(replyCode == QQ.QQ_REPLY_OK) {
			int count = buf.getInt();
			diskOwners = new ArrayList<Integer>();
			while(count-- > 0) {
				diskOwners.add(buf.getInt());
			}			
		}
	}
}
