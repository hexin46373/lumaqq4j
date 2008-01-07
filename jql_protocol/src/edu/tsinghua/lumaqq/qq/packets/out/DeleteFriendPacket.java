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
package edu.tsinghua.lumaqq.qq.packets.out;

import java.nio.ByteBuffer;

import edu.tsinghua.lumaqq.qq.QQ;
import edu.tsinghua.lumaqq.qq.annotation.DocumentalPacket;
import edu.tsinghua.lumaqq.qq.annotation.PacketName;
import edu.tsinghua.lumaqq.qq.annotation.RelatedPacket;
import edu.tsinghua.lumaqq.qq.beans.QQUser;
import edu.tsinghua.lumaqq.qq.packets.BasicOutPacket;
import edu.tsinghua.lumaqq.qq.packets.PacketParseException;
import edu.tsinghua.lumaqq.qq.packets.in.DeleteFriendReplyPacket;


/**
 * <pre>
 * 这个包用来删除一个好友，格式为:
 * 1. 头部
 * 2. 要删除的好友的QQ号的字符串形式
 * 3. 尾部
 * </pre>
 *
 * @author luma
 */
@DocumentalPacket
@PacketName("删除好友请求包")
@RelatedPacket({DeleteFriendReplyPacket.class})
public class DeleteFriendPacket extends BasicOutPacket {
    private int to;
    
    /**
     * 构造函数
     */
    public DeleteFriendPacket(QQUser user) {
        super(QQ.QQ_CMD_DELETE_FRIEND, true, user);
    }

    /**
     * @param buf
     * @param length
     * @throws PacketParseException
     */
    public DeleteFriendPacket(ByteBuffer buf, int length, QQUser user)
            throws PacketParseException {
        super(buf, length, user);
    }
    
    /* (non-Javadoc)
     * @see edu.tsinghua.lumaqq.qq.packets.OutPacket#getPacketName()
     */
	@Override
    public String getPacketName() {
        return "Delete Friend Packet";
    }
    
    /* (non-Javadoc)
     * @see edu.tsinghua.lumaqq.qq.packets.OutPacket#putBody(java.nio.ByteBuffer)
     */
	@Override
    protected void putBody(ByteBuffer buf) {
	    // 好友的QQ号的字符串形式
	    buf.put(String.valueOf(to).getBytes());
    }
    
    /**
     * @return Returns the to.
     */
    public int getTo() {
        return to;
    }
    
    /**
     * @param to The to to set.
     */
    public void setTo(int to) {
        this.to = to;
    }
}
