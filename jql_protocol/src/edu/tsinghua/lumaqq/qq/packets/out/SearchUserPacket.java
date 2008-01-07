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
import edu.tsinghua.lumaqq.qq.Util;
import edu.tsinghua.lumaqq.qq.annotation.DocumentalPacket;
import edu.tsinghua.lumaqq.qq.annotation.PacketName;
import edu.tsinghua.lumaqq.qq.annotation.RelatedPacket;
import edu.tsinghua.lumaqq.qq.beans.QQUser;
import edu.tsinghua.lumaqq.qq.packets.BasicOutPacket;
import edu.tsinghua.lumaqq.qq.packets.PacketParseException;
import edu.tsinghua.lumaqq.qq.packets.in.SearchUserReplyPacket;

/**
 * <pre>
 * 搜索在线用户的包，格式为
 * 1. 头部
 * 2. 1个字节，表示搜索类型，比如搜索全部在线用户是0x31，自定义搜索是0x30
 * 3. 1字节分隔符: 0x1F
 * 4. 搜索参数
 * 	  i.  对于搜索全部在线用户的请求，是一个页号，用字符串表示，从0开始
 *    ii. 对于自定义搜索类型，是4个域，用0x1F分隔，依次是
 * 		   a. 要搜索的用户的QQ号的字符串形式
 * 		   b. 要搜索的用户的昵称
 * 		   c. 要搜索的用户的email
 *         d. 页号的字符串形式，这后面没有分隔符了，是用0x0结尾的         
 * 5. 尾部
 * </pre> 
 * 
 * @author luma
 */
@DocumentalPacket
@PacketName("搜索用户请求包")
@RelatedPacket({SearchUserReplyPacket.class})
public class SearchUserPacket extends BasicOutPacket {
	private byte searchType;
	private String page;
	private String qqStr;
	private String nick;
	private String email;
	
	/** 分隔符 */
	private static final byte DELIMIT = 0x1F;
	/** 如果字段为空，用0x2D替代，即'-'字符 */
	private static final byte NULL = 0x2D;
	
    /**
     * 构造函数
     */
    public SearchUserPacket(QQUser user) {
        super(QQ.QQ_CMD_SEARCH_USER, true, user);
		page = "0";
		searchType = QQ.QQ_SEARCH_ALL;
		qqStr = nick = email = "";
    }
	
    /**
     * @param buf
     * @param length
     * @throws PacketParseException
     */
    public SearchUserPacket(ByteBuffer buf, int length, QQUser user)
            throws PacketParseException {
        super(buf, length, user);
    }
    
    /* (non-Javadoc)
     * @see edu.tsinghua.lumaqq.qq.packets.OutPacket#getPacketName()
     */
	@Override
    public String getPacketName() {
        return "Search User Packet";
    }
    
    /* (non-Javadoc)
     * @see edu.tsinghua.lumaqq.qq.packets.OutPacket#putBody(java.nio.ByteBuffer)
     */
	@Override
    protected void putBody(ByteBuffer buf) {
		// 开始组装内容
		if(searchType == QQ.QQ_SEARCH_ALL) {
			buf.put(searchType);
			buf.put(DELIMIT);
			buf.put(page.getBytes());				
		} else if(searchType == QQ.QQ_SEARCH_CUSTOM) {
			buf.put(searchType);
			buf.put(DELIMIT);
			// QQ号
			if(qqStr == null || qqStr.equals("")) buf.put(NULL);
			else buf.put(qqStr.getBytes());
			buf.put(DELIMIT);			
			// 昵称
			if(nick == null || nick.equals("")) buf.put(NULL);
			else
				buf.put(Util.getBytes(nick));
			buf.put(DELIMIT);			
			// email
			if(email == null || email.equals("")) buf.put(NULL);
			else
				buf.put(email.getBytes());
			buf.put(DELIMIT);	
			// 结尾
			buf.put(page.getBytes());
			buf.put((byte)0x0);
		}
    }
    
    /**
	 * @param page The page to set.
	 */
	public void setPage(int page) {
		this.page = String.valueOf(page);
	}
	
	/**
	 * @param searchType The searchType to set.
	 */
	public void setSearchType(byte searchType) {
		this.searchType = searchType;
	}
	
	/**
	 * @param nick The nick to set.
	 */
	public void setNick(String nick) {
		this.nick = nick;
	}
	
	/**
	 * @param qqNum The qqNum to set.
	 */
	public void setQQStr(int qqNum) {
		this.qqStr = String.valueOf(qqNum);
	}
	
	/**
	 * @param qqStr
	 */
	public void setQQStr(String qqStr) {
		this.qqStr = qqStr;
	}
	
	/**
	 * @param email The email to set.
	 */
	public void setEmail(String email) {
		this.email = email;
	}
}
