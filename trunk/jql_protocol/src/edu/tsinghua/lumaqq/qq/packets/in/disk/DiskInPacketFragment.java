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

import edu.tsinghua.lumaqq.qq.annotation.DocumentalPacket;
import edu.tsinghua.lumaqq.qq.annotation.LinkedEvent;
import edu.tsinghua.lumaqq.qq.annotation.PacketName;
import edu.tsinghua.lumaqq.qq.beans.QQUser;
import edu.tsinghua.lumaqq.qq.packets.DiskInPacket;
import edu.tsinghua.lumaqq.qq.packets.PacketParseException;

/**
 * <pre>
 * 网络协议族包片断。包片断只是某个包的一部分，由于网络协议族的包可能非常大，而缺省的缓冲区
 * 只有QQ.QQ_MAX_PACKET_SIZE大，所以解析这样超大的包成了一个问题。所以会把某些超大的包
 * 拆成包片断处理。通常来说，包片断是包体的一部分。由于包片断没有包头，所以解析器必须担负起
 * 设置包命令等字段的任务。
 * </pre>
 *
 * @author luma
 */
@DocumentalPacket
@PacketName("网络硬盘协议包片断")
@LinkedEvent({QQ_DISK_DOWNLOAD_FRAGMENT_SUCCESS})
public class DiskInPacketFragment extends DiskInPacket {
	public byte[] body;

	public DiskInPacketFragment(ByteBuffer buf, int length, QQUser user) throws PacketParseException {
		super(buf, length, user);
	}

	@Override
	protected void parseBody(ByteBuffer buf) throws PacketParseException {
		body = new byte[buf.remaining()];
		buf.get(body);
	}
	
	@Override
	protected byte[] getBodyBytes(ByteBuffer buf, int length) {
		byte[] b = new byte[length];
		buf.get(b);
		return b;
	}
	
	@Override
	protected void parseHeader(ByteBuffer buf) throws PacketParseException {
	}
	
	@Override
	protected void parseTail(ByteBuffer buf) throws PacketParseException {
	}
	
	@Override
	protected void putHead(ByteBuffer buf) {
	}
	
	@Override
	protected void putTail(ByteBuffer buf) {
	}
	
	@Override
	protected void putBody(ByteBuffer buf) {
		buf.put(body);
	}
}
