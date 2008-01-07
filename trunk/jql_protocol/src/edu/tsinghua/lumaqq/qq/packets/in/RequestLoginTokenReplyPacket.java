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
import edu.tsinghua.lumaqq.qq.packets.out.RequestLoginTokenPacket;

/**
 * <pre>
 * 请求登录令牌的回复包，这个包的source字段和其他包不同，为QQ.QQ_SERVER_0000
 * 1. 头部
 * 2. 回复码，1字节，0x00表示成功
 * 3. 登录令牌长度，1字节
 * 4. 登录令牌
 * 5. 尾部
 * </pre>
 * 
 * @author luma
 */
@DocumentalPacket
@PacketName("获取登录令牌回复包")
@RelatedPacket({RequestLoginTokenPacket.class})
@LinkedEvent({QQ_GET_LOGIN_TOKEN_SUCCESS, QQ_GET_LOGIN_TOKEN_FAIL})
public class RequestLoginTokenReplyPacket extends BasicInPacket {
    public byte replyCode;
    public byte[] loginToken;
    
    /**
     * @param buf
     * @throws PacketParseException
     */
    public RequestLoginTokenReplyPacket(ByteBuffer buf, QQUser user)
            throws PacketParseException {
        super(buf, user);
    }
    
    /* (non-Javadoc)
     * @see edu.tsinghua.lumaqq.qq.packets.InPacket#getPacketName()
     */
	@Override
    public String getPacketName() {
        return "Request Login Token Reply Packet";
    }
    
    /**
     * @param buf
     * @param length
     * @throws PacketParseException
     */
    public RequestLoginTokenReplyPacket(ByteBuffer buf, int length, QQUser user) throws PacketParseException {
        super(buf, length, user);
    }

    /* (non-Javadoc)
     * @see edu.tsinghua.lumaqq.qq.packets.InPacket#parseBody(java.nio.ByteBuffer)
     */
	@Override
    protected void parseBody(ByteBuffer buf) throws PacketParseException {
        // 回复码
        replyCode = buf.get();
        if(replyCode == QQ.QQ_REPLY_OK) {
	        // 登录令牌长度
	        int len = buf.get() & 0xFF;
	        // 登录令牌
	        loginToken = new byte[len];
	        buf.get(loginToken);            
        }
    }
}
