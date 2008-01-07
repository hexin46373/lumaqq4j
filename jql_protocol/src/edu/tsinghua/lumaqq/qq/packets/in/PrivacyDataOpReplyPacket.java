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
package edu.tsinghua.lumaqq.qq.packets.in;

import static edu.tsinghua.lumaqq.qq.events.QQEvent.*;
import java.nio.ByteBuffer;

import edu.tsinghua.lumaqq.qq.annotation.DocumentalPacket;
import edu.tsinghua.lumaqq.qq.annotation.LinkedEvent;
import edu.tsinghua.lumaqq.qq.annotation.PacketName;
import edu.tsinghua.lumaqq.qq.annotation.RelatedPacket;
import edu.tsinghua.lumaqq.qq.beans.QQUser;
import edu.tsinghua.lumaqq.qq.packets.BasicInPacket;
import edu.tsinghua.lumaqq.qq.packets.PacketParseException;
import edu.tsinghua.lumaqq.qq.packets.out.PrivacyDataOpPacket;

/**
 * <pre>
 * 隐私选项操作包
 * 1. 头部
 * 2. 子命令，1字节
 * 3. 操作，1字节
 * 4. 回复码，1字节
 * 5. 尾部
 * </pre>
 * 
 * @author luma
 */
@DocumentalPacket
@PacketName("个人隐私操作回复包")
@RelatedPacket({PrivacyDataOpPacket.class})
@LinkedEvent({QQ_PRIVACY_DATA_OP_SUCCESS, QQ_PRIVACY_DATA_OP_FAIL})
public class PrivacyDataOpReplyPacket extends BasicInPacket {
	public byte subCommand;
	public byte opCode;
	public byte replyCode;
	
	public PrivacyDataOpReplyPacket(ByteBuffer buf, int length, QQUser user) throws PacketParseException {
		super(buf, length, user);
	}
	
	@Override
	public String getPacketName() {
		return "Privacy Data Op Reply Packet";
	}

	@Override
	protected void parseBody(ByteBuffer buf) throws PacketParseException {
		subCommand = buf.get();
		opCode = buf.get();
		replyCode = buf.get();
	}
}
