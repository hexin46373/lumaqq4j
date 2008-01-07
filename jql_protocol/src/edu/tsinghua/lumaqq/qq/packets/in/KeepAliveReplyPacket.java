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

import edu.tsinghua.lumaqq.qq.Util;
import edu.tsinghua.lumaqq.qq.annotation.DocumentalPacket;
import edu.tsinghua.lumaqq.qq.annotation.LinkedEvent;
import edu.tsinghua.lumaqq.qq.annotation.PacketName;
import edu.tsinghua.lumaqq.qq.annotation.RelatedPacket;
import edu.tsinghua.lumaqq.qq.beans.QQUser;
import edu.tsinghua.lumaqq.qq.packets.BasicInPacket;
import edu.tsinghua.lumaqq.qq.packets.PacketParseException;
import edu.tsinghua.lumaqq.qq.packets.out.KeepAlivePacket;

/**
 * <pre>
 * Keep Alive的应答包，格式为
 * 1. 头部
 * 2. 6个域，分别是"0", "0", 所有在线用户数，我的IP，我的端口，未知含义字段，用ascii码31分隔
 * 3. 尾部
 * </pre>
 *
 * @author luma
 */
@DocumentalPacket
@PacketName("Keep Alive回复包")
@RelatedPacket({KeepAlivePacket.class})
@LinkedEvent({QQ_KEEP_ALIVE_SUCCESS})
public class KeepAliveReplyPacket extends BasicInPacket {
    public static final String DIVIDER = Character.toString((char)31);
	public int onlines;
	public String ip;
	public int port;
	
	private static final int FIELD_COUNT = 6;
	
    /**
     * 构造函数
     * @param buf 缓冲区
     * @param length 包长度
     * @throws PacketParseException 解析错误
     */
    public KeepAliveReplyPacket(ByteBuffer buf, int length, QQUser user) throws PacketParseException {
        super(buf, length, user);
    }   
    
    /* (non-Javadoc)
     * @see edu.tsinghua.lumaqq.qq.packets.InPacket#getPacketName()
     */
	@Override
    public String getPacketName() {
        return "Keep Alive Reply Packet";
    }
	
    /* (non-Javadoc)
     * @see edu.tsinghua.lumaqq.qq.packets.InPacket#parseBody(java.nio.ByteBuffer)
     */
	@Override
    protected void parseBody(ByteBuffer buf) throws PacketParseException {
        // 用分隔符分隔各个字段
        byte[] b = buf.array();
        String[] result = new String(b).split(DIVIDER);
        // 检查字段数是否正确
        if(result.length != FIELD_COUNT)
            throw new PacketParseException("Keep Alive回复字段数不对");
        // 解析各字段
        onlines = Util.getInt(result[2], 0);
        if(onlines == 0)
    		throw new PacketParseException("解析在线好友数出错，错误出处：KeepAliveReplyPacket");
        ip = result[3];
        port = Util.getInt(result[4], 0);
    }
}
