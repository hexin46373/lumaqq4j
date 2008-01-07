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
import edu.tsinghua.lumaqq.qq.annotation.PacketName;
import edu.tsinghua.lumaqq.qq.annotation.RelatedPacket;
import edu.tsinghua.lumaqq.qq.beans.QQUser;
import edu.tsinghua.lumaqq.qq.packets.BasicOutPacket;
import edu.tsinghua.lumaqq.qq.packets.PacketParseException;
import edu.tsinghua.lumaqq.qq.packets.in.AddFriendReplyPacket;


/**
 * <pre>
 * 这个是添加好友的时候用的包，这个和AddFriendAuth有什么关系呢，AddFriend
 * 是最初始的请求包，要加一个好友的时候，首先发这个包，如果对方不需要验证，那
 * 就是成功了，如果需要验证，就还要继续发请求。这个包格式是
 * 1. 头部
 * 2. 要加的人的QQ号的字符串形式
 * 3. 尾部
 * </pre>
 *
 * @author luma
 * @deprecated 2005已经不再使用这个命令
 */
@Deprecated
@RelatedPacket({AddFriendReplyPacket.class})
@PacketName("添加好友请求包")
public class AddFriendPacket extends BasicOutPacket {
    private int to;
    
    /**
     * 构造函数
     */
    public AddFriendPacket(QQUser user) {
        super(QQ.QQ_CMD_ADD_FRIEND, true, user);
    }

    /**
     * @param buf
     * @param length
     * @throws PacketParseException
     */
    public AddFriendPacket(ByteBuffer buf, int length, QQUser user)
            throws PacketParseException {
        super(buf, length, user);
    }
    
    /* (non-Javadoc)
     * @see edu.tsinghua.lumaqq.qq.packets.OutPacket#putBody(java.nio.ByteBuffer)
     */
	@Override
    protected void putBody(ByteBuffer buf) {
	    // 要加的QQ号的字符串形式
	    buf.put(String.valueOf(to).getBytes());
    }
    
    /* (non-Javadoc)
     * @see edu.tsinghua.lumaqq.qq.packets.OutPacket#getPacketName()
     */
	@Override
    public String getPacketName() {
        return "Add Friend Packet";
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
