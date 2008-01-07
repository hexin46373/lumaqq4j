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

import edu.tsinghua.lumaqq.qq.annotation.DocumentalPacket;
import edu.tsinghua.lumaqq.qq.annotation.LinkedEvent;
import edu.tsinghua.lumaqq.qq.annotation.PacketName;
import edu.tsinghua.lumaqq.qq.annotation.RelatedPacket;
import edu.tsinghua.lumaqq.qq.beans.QQUser;
import edu.tsinghua.lumaqq.qq.beans.UserInfo;
import edu.tsinghua.lumaqq.qq.packets.BasicInPacket;
import edu.tsinghua.lumaqq.qq.packets.PacketParseException;
import edu.tsinghua.lumaqq.qq.packets.out.SearchUserPacket;

/**
 * <pre>
 * 搜索在线用户的回复包，格式为
 * 1. 头部
 * 2. 有两种形式
 *    第一种为搜索到了用户
 * 	  以0x1F相隔的用户数据，其中，一个用户的数据分4个域，域之间用0x1E相隔，四个域为
 * 	  i.   用户QQ号的字符串形式
 *    ii.  用户昵称
 *    iii. 用户所在地区
 *    iv.  用户的头像号码
 *    第二种是没有更多的匹配了，表示本次搜索的全部匹配已取得
 *    i. 字符串"-1"
 * 3. 尾部
 * </pre>
 * 
 * @author luma
 */
@DocumentalPacket
@PacketName("搜索用户回复包")
@RelatedPacket({SearchUserPacket.class})
@LinkedEvent({QQ_SEARCH_USER_SUCCESS, QQ_SEARCH_USER_END})
public class SearchUserReplyPacket extends BasicInPacket {
	public List<UserInfo> users;
	public boolean finished;
	
    /**
     * 构造函数
     * @param buf 缓冲区
     * @param length 包长度
     * @throws PacketParseException 解析错误
     */
    public SearchUserReplyPacket(ByteBuffer buf, int length, QQUser user) throws PacketParseException {
        super(buf, length, user);       
    }   
    
    /* (non-Javadoc)
     * @see edu.tsinghua.lumaqq.qq.packets.InPacket#getPacketName()
     */
	@Override
    public String getPacketName() {
        return "Search User Reply Packet";
    }
	
    /* (non-Javadoc)
     * @see edu.tsinghua.lumaqq.qq.packets.InPacket#parseBody(java.nio.ByteBuffer)
     */
	@Override
    protected void parseBody(ByteBuffer buf) throws PacketParseException {
    	// 判断搜索是否已经结束
    	if(!buf.hasRemaining() || buf.get() == 0x2D && buf.get() == 0x31) {
    		finished = true;
    		return;
    	}
    	buf.rewind();
        // 只要还有数据就继续读取下一个friend结构
    	users = new ArrayList<UserInfo>();
        while(buf.hasRemaining()) {
        	UserInfo ui = new UserInfo();
            ui.readBean(buf);
            
            // 添加到list
            users.add(ui);
        }
    }
}
