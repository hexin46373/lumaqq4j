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
import java.util.ArrayList;
import java.util.List;

import edu.tsinghua.lumaqq.qq.QQ;
import edu.tsinghua.lumaqq.qq.annotation.DocumentalPacket;
import edu.tsinghua.lumaqq.qq.annotation.LinkedEvent;
import edu.tsinghua.lumaqq.qq.annotation.PacketName;
import edu.tsinghua.lumaqq.qq.annotation.RelatedPacket;
import edu.tsinghua.lumaqq.qq.beans.AdvancedUserInfo;
import edu.tsinghua.lumaqq.qq.beans.QQUser;
import edu.tsinghua.lumaqq.qq.packets.BasicInPacket;
import edu.tsinghua.lumaqq.qq.packets.PacketParseException;
import edu.tsinghua.lumaqq.qq.packets.out.AdvancedSearchUserPacket;

/**
 * <pre>
 * 高级搜索的回复包
 * 1. 头部
 * 2. 回复码，1字节，0x00表示还有数据，0x01表示没有更多数据了，当为0x01时，后面没有内容了
 *    当为0x00时，后面才有内容
 * 3. 页号，从1开始，2字节，如果页号后面没有内容了，那也说明是搜索结束了
 * 4. QQ号，4字节
 * 5. 性别，1字节，表示下拉框索引
 * 6. 年龄，2字节
 * 7. 在线，1字节，0x01表示在线，0x00表示离线
 * 8. 昵称长度，1字节
 * 9. 昵称
 * 10. 省份索引，2字节
 * 11. 城市索引，2字节，这个索引是以"不限"为0开始算的，shit
 * 13. 头像索引，2字节
 * 14. 如果有更多结果，重复4 - 13部分
 * 15. 尾部
 * </pre>
 * 
 * @author luma
 */
@DocumentalPacket
@PacketName("高级方式搜索用户回复包")
@RelatedPacket({AdvancedSearchUserPacket.class})
@LinkedEvent({QQ_ADVANCED_SEARCH_USER_SUCCESS, QQ_ADVANCED_SEARCH_USER_END})
public class AdvancedSearchUserReplyPacket extends BasicInPacket {
    public byte replyCode;
    public int page;
    public List<AdvancedUserInfo> users;
    public boolean finished;
    
    /**
     * @param buf
     * @param length
     * @throws PacketParseException
     */
    public AdvancedSearchUserReplyPacket(ByteBuffer buf, int length, QQUser user) throws PacketParseException {
        super(buf, length, user);
    }
    
    /* (non-Javadoc)
     * @see edu.tsinghua.lumaqq.qq.packets.OutPacket#getPacketName()
     */
	@Override
    public String getPacketName() {
        return "Advanced Search User Reply Packet";
    }

    /* (non-Javadoc)
     * @see edu.tsinghua.lumaqq.qq.packets.InPacket#parseBody(java.nio.ByteBuffer)
     */
	@Override
    protected void parseBody(ByteBuffer buf) throws PacketParseException {
        replyCode = buf.get();        
        if(replyCode == QQ.QQ_REPLY_OK) {
            page = buf.getChar();
            
            // read all user info
            users = new ArrayList<AdvancedUserInfo>();
            
            while(buf.hasRemaining()) {
	            AdvancedUserInfo aui = new AdvancedUserInfo();
	            aui.readBean(buf);
	            users.add(aui);
            }
            
            finished = users.isEmpty();
        } else 
            finished = true;
    }
}
