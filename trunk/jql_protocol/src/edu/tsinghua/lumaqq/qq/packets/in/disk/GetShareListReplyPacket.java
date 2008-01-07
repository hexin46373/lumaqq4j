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
import edu.tsinghua.lumaqq.qq.packets.out.disk.GetShareListPacket;

/**
 * <pre>
 * 得到共享列表的回复包
 * 1. 头部
 * 2. 好友数目，4字节
 * 3. 好友QQ号，4字节
 * 4. 未知4字节
 * 5. 如果设置了对更多好友共享，重复3-4部分
 * </pre>
 *
 * @author luma
 */
@DocumentalPacket
@PacketName("获取共享网络硬盘属主列表回复包")
@RelatedPacket({GetShareListPacket.class})
@LinkedEvent({QQ_DISK_GET_SHARE_LIST_SUCCESS})
public class GetShareListReplyPacket extends DiskInPacket {
	public List<Integer> friends;

	public GetShareListReplyPacket(ByteBuffer buf, int length, QQUser user) throws PacketParseException {
		super(buf, length, user);
	}
	
	@Override
	public String getPacketName() {
		return "Get Share List Reply Packet";
	}

	@Override
	protected void parseBody(ByteBuffer buf) throws PacketParseException {
		if(replyCode == QQ.QQ_REPLY_OK) {
			int count = buf.getInt();
			friends = new ArrayList<Integer>();
			while(count-- > 0) {
				friends.add(buf.getInt());
				buf.getInt();
			}		
		}
	}
}
