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

import edu.tsinghua.lumaqq.qq.QQ;
import edu.tsinghua.lumaqq.qq.annotation.DocumentalPacket;
import edu.tsinghua.lumaqq.qq.annotation.LinkedEvent;
import edu.tsinghua.lumaqq.qq.annotation.PacketName;
import edu.tsinghua.lumaqq.qq.annotation.RelatedPacket;
import edu.tsinghua.lumaqq.qq.beans.QQUser;
import edu.tsinghua.lumaqq.qq.packets.BasicInPacket;
import edu.tsinghua.lumaqq.qq.packets.PacketParseException;
import edu.tsinghua.lumaqq.qq.packets.out.AddFriendExPacket;


/**
 * <pre>
 * 这个添加好友的应答包，格式是
 * 1. 头部
 * 2. 要添加的好友的QQ号,4字节
 * 3. 回复码，1字节
 * 4. 附加条件码，1字节，比如是不是需要认证，等等
 * 注：仅当3部分为0x00时，4部分才存在
 * 5. 尾部
 * </pre>
 *
 * @author luma
 */
@DocumentalPacket
@PacketName("添加好友回复包(2005)")
@RelatedPacket({AddFriendExPacket.class})
@LinkedEvent({QQ_ADD_FRIEND_SUCCESS, 
	QQ_ADD_FRIEND_NEED_AUTH, 
	QQ_ADD_FRIEND_DENY, 
	QQ_ADD_FRIEND_ALREADY,
	QQ_ADD_FRIEND_FAIL})
public class AddFriendExReplyPacket extends BasicInPacket {
    public byte replyCode;
    public byte authCode;
    public int friendQQ;
    
    /**
     * 构造函数
     * @param buf 缓冲区
     * @param length 包长度
     * @throws PacketParseException 解析错误
     */
    public AddFriendExReplyPacket(ByteBuffer buf, int length, QQUser user) throws PacketParseException {
        super(buf, length, user);
    }
    
    /* (non-Javadoc)
     * @see edu.tsinghua.lumaqq.qq.packets.OutPacket#getPacketName()
     */
	@Override
    public String getPacketName() {
        return "Add Friend Ex Reply Packet";
    }
    
    /* (non-Javadoc)
     * @see edu.tsinghua.lumaqq.qq.packets.InPacket#parseBody(java.nio.ByteBuffer)
     */
	@Override
    protected void parseBody(ByteBuffer buf) throws PacketParseException {
		friendQQ = buf.getInt();
		replyCode = buf.get();
		if(replyCode == QQ.QQ_REPLY_OK)
			authCode = buf.get();
    }
}
