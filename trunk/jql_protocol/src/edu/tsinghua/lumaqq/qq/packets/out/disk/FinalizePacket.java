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
import edu.tsinghua.lumaqq.qq.packets.in.disk.FinalizeReplyPacket;

/**
 * <pre>
 * 结束上传
 * 1. 头部
 * 2. id，30字节，不足填0
 * 
 * Note: 这个包用来结束文件上传，使服务器消除文件的待上传状态
 * </pre>
 *
 * @author luma
 */
@DocumentalPacket
@PacketName("结束上传文件请求包")
@RelatedPacket({FinalizeReplyPacket.class})
public class FinalizePacket extends DiskOutPacket {
	private String id;

	public FinalizePacket(ByteBuffer buf, int length, QQUser user) throws PacketParseException {
		super(buf, length, user);
	}

	public FinalizePacket(QQUser user) {
		super(QQ.QQ_DISK_CMD_FINALIZE, user);
	}
	
	@Override
	public String getPacketName() {
		return "Finalize Packet";
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
}
