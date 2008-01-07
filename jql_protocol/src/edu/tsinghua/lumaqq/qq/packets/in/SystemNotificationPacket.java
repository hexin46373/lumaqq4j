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
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import edu.tsinghua.lumaqq.qq.QQ;
import edu.tsinghua.lumaqq.qq.Util;
import edu.tsinghua.lumaqq.qq.annotation.DocumentalPacket;
import edu.tsinghua.lumaqq.qq.annotation.LinkedEvent;
import edu.tsinghua.lumaqq.qq.annotation.PacketName;
import edu.tsinghua.lumaqq.qq.beans.QQUser;
import edu.tsinghua.lumaqq.qq.packets.BasicInPacket;
import edu.tsinghua.lumaqq.qq.packets.PacketParseException;


/**
 * <pre>
 * 系统消息包，系统消息和ReceiveIMPacket里面的系统通知有什么区别呢？
 * 系统消息是表示你被别人加为好友了之类的消息，所以有源有目的，其他人
 * 收不到的，系统通知是系统发给大家的消息。好了，废话这么多，系统消息的
 * 格式是:
 * 1. 头部，头部说明了系统消息的类型，目前已知的有四种
 * 2. 对于一般的系统通知，其格式为:
 * 	  以0x1F相隔的多个字段，对于已知的类型，分别是消息类型，源，目的，附加内容，对于未知的
 *    消息类型，前面三个是一样的，后面的就未知了
 *    
 *    对于QQ_SYS_BEING_ADDED_EX，消息正文的格式为：
 *    i. 后面内容的字节长度
 *    ii. 未知内容，不知用什么才能解密
 *    
 *    对于QQ_SYS_ADD_FRIEND_REQUEST_EX，其消息正文的格式为
 *    i. 消息正文字节长度
 *    ii. 消息正文
 *    iii. 是否允许对方加自己为好友，0x01表示允许，0x02表示不允许
 *    
 *    对于QQ_SYS_ADD_FRIEND_APPROVED_AND_ADD，其消息正文的格式为
 *    i. 未知的1字节，0x00
 * 3. 尾部
 * </pre>
 *
 * Note: 只有使用2005的0x00A8发送认证消息，才会收到QQ_SYS_ADD_FRIEND_REQUEST_EX消息
 * @author luma
 */
@DocumentalPacket
@PacketName("系统通知消息包")
@LinkedEvent({QQ_ADDED_BY_OTHERS,
	QQ_REQUEST_ADD_ME,
	QQ_REQUEST_ADD_OTHER_APPROVED,
	QQ_REQUEST_ADD_OTHER_REJECTED,
	QQ_RECEIVE_ADVERTISEMENT,
	QQ_ADDED_BY_OTHERS_EX,
	QQ_REQUEST_ADD_ME_EX,
	QQ_REQUEST_ADD_OTHER_APPROVED_AND_ADD})
public class SystemNotificationPacket extends BasicInPacket {   
    // 分隔符
    public static final String DIVIDER = Character.toString((char)0x1F);
    // 消息类型
    public int type;
    // 从哪里来，是源的QQ号
    public int from;
    // 到哪里去，目的的QQ号
    public int to;
    // 附加的消息，比如如果别人拒绝了你加他为好友，并说了理由，那就在这里了
    public String message;
    
    // only for QQ_SYS_ADD_FRIEND_REQUEST_EX
    public boolean allowAddReverse;
    
    /**
     * 构造函数
     * @param buf 缓冲区
     * @param length 包长度
     * @throws PacketParseException 解析错误
     */
    public SystemNotificationPacket(ByteBuffer buf, int length, QQUser user) throws PacketParseException {
        super(buf, length, user);
    }   
    
    /* (non-Javadoc)
     * @see edu.tsinghua.lumaqq.qq.packets.InPacket#getPacketName()
     */
	@Override
    public String getPacketName() {
        return "System Notification Packet";
    }

    /* (non-Javadoc)
     * @see edu.tsinghua.lumaqq.qq.packets.InPacket#parseBody(java.nio.ByteBuffer)
     */
	@Override
    protected void parseBody(ByteBuffer buf) throws PacketParseException {
        byte[] b = buf.array();
        String s = null;
        try {
            s = new String(b, QQ.QQ_CHARSET_DEFAULT);
        } catch (UnsupportedEncodingException e) {
            s = new String(b);
        }
        String[] fields = s.split(DIVIDER);
        type = Util.getInt(fields[0], 0);
        from = Util.getInt(fields[1], 0);
        to = Util.getInt(fields[2], 0);
        if(fields.length > 3) {
        	switch(type) {
        		case QQ.QQ_SYS_ADD_FRIEND_REQUEST_EX:
            		byte[] fByte = Util.getBytes(fields[3]);
            		int len = fByte[0] & 0xFF;
            		message = Util.getString(fByte, 1, len);
            		allowAddReverse = fByte[fByte.length - 1] == QQ.QQ_FLAG_ALLOW_ADD_REVERSE;
            		break;
        		case QQ.QQ_SYS_BEING_ADDED_EX:
        		case QQ.QQ_SYS_ADD_FRIEND_APPROVED_AND_ADD:
        			message = "";
        			break;
        		default:
        			message = fields[3];      
        			break;
        	}
        } else
            message = "";
        if(from == 0 || to == 0)
            throw new PacketParseException("系统通知字段解析出错");
    }
}
