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
import edu.tsinghua.lumaqq.qq.Util;
import edu.tsinghua.lumaqq.qq.annotation.DocumentalPacket;
import edu.tsinghua.lumaqq.qq.annotation.LinkedEvent;
import edu.tsinghua.lumaqq.qq.annotation.PacketName;
import edu.tsinghua.lumaqq.qq.annotation.RelatedPacket;
import edu.tsinghua.lumaqq.qq.beans.QQUser;
import edu.tsinghua.lumaqq.qq.packets.BasicInPacket;
import edu.tsinghua.lumaqq.qq.packets.PacketParseException;
import edu.tsinghua.lumaqq.qq.packets.out.LoginPacket;


/**
 * <pre>
 * QQ登陆应答包
 * 1. 头部
 * 2. 回复码, 1字节
 * 2部分如果是0x00
 * 3. session key, 16字节
 * 4. 用户QQ号，4字节
 * 5. 我的外部IP，4字节
 * 6. 我的外部端口，2字节
 * 7. 服务器IP，4字节
 * 8. 服务器端口，2字节
 * 9. 本次登录时间，4字节，为从1970-1-1开始的毫秒数除1000
 * 10. 未知的2字节
 * 11. 用户认证令牌,24字节
 * 12. 一个未知服务器1的ip，4字节
 * 13. 一个未知服务器1的端口，2字节
 * 14. 一个未知服务器2的ip，4字节
 * 15. 一个未知服务器2的端口，2字节
 * 16. 两个未知字节
 * 17. 两个未知字节
 * 18. client key，32字节，这个key用在比如登录QQ家园之类的地方
 * 19. 12个未知字节
 * 20. 上次登陆的ip，4字节
 * 21. 上次登陆的时间，4字节
 * 22. 39个未知字节
 * 2部分如果是0x01，表示重定向
 * 3. 用户QQ号，4字节
 * 4. 重定向到的服务器IP，4字节
 * 5. 重定向到的服务器的端口，2字节
 * 2部分如果是0x05，表示登录失败
 * 3. 一个错误消息
 * </pre>
 *
 * @author luma
 */
@DocumentalPacket
@PacketName("登录请求回复包")
@RelatedPacket({LoginPacket.class})
@LinkedEvent({QQ_LOGIN_SUCCESS, QQ_LOGIN_FAIL, QQ_LOGIN_UNKNOWN_ERROR, QQ_LOGIN_REDIRECT_NULL})
public class LoginReplyPacket extends BasicInPacket {
	public byte[] sessionKey;
	public byte[] ip;
	public byte[] serverIp;
	public byte[] lastLoginIp;
	public byte[] redirectIp;
	public int port;
	public int serverPort;
	public int redirectPort;
	public long loginTime;
	public long lastLoginTime;
	public byte replyCode;
	public String replyMessage;
	public byte[] clientKey;
	// 认证令牌，用在一些需要认证身份的地方，比如网络硬盘
	public byte[] authToken;
	
    /**
     * 构造函数
     * @param buf 缓冲区
     * @param length 包长度
     * @throws PacketParseException 解析错误
     */
    public LoginReplyPacket(ByteBuffer buf, int length, QQUser user) throws PacketParseException {
        super(buf, length, user);
    }   
    
    /* (non-Javadoc)
     * @see edu.tsinghua.lumaqq.qq.packets.InPacket#getPacketName()
     */
	@Override
    public String getPacketName() {
        return "Login Reply Packet";
    }
        
    /* (non-Javadoc)
     * @see edu.tsinghua.lumaqq.qq.packets.InPacket#parseBody(java.nio.ByteBuffer)
     */
	@Override
    protected void parseBody(ByteBuffer buf) throws PacketParseException {
        replyCode = buf.get();
        switch(replyCode) {
            case QQ.QQ_REPLY_OK:
                // 001-016字节是session key
            	sessionKey = new byte[QQ.QQ_LENGTH_KEY];
                buf.get(sessionKey);
                // 017-020字节是用户QQ号
                buf.getInt();
                // 021-024字节是服务器探测到的用户IP
                ip = new byte[4];
                buf.get(ip);
                // 025-026字节是服务器探测到的用户端口
                port = buf.getChar();
                // 027-030字节是服务器自己的IP
                serverIp = new byte[4];                    
                buf.get(serverIp);
                // 031-032字节是服务器的端口
                serverPort = buf.getChar();
                // 033-036字节是本次登陆时间，为什么要乘1000？因为这个时间乘以1000才对，-_-!...
                loginTime = (long)buf.getInt() * 1000L;
                // 037-038, 未知的2字节
                buf.getChar();
                // 039-062, 认证令牌
                authToken = new byte[24];
                buf.get(authToken);
                // 063-066字节是一个未知服务器1的ip
                // 067-068字节是一个未知服务器1的端口
                // 069-072是一个未知服务器2的ip
                // 073-074是一个未知服务器2的端口
                // 075-076是两个未知字节
                // 077-078是两个未知字节
                buf.position(buf.position() + 16);
                // 079-110是client key，这个key用在比如登录QQ家园之类的地方
                clientKey = new byte[32];
                buf.get(clientKey);
                // 111-122是12个未知字节
                buf.position(buf.position() + 12);
                // 123-126是上次登陆的ip
                lastLoginIp = new byte[4];
                buf.get(lastLoginIp);
                // 127-130是上次登陆的时间
                lastLoginTime = (long)buf.getInt() * 1000L;
                // 39个未知字节
                // do nothing
                break;
            case QQ.QQ_REPLY_LOGIN_FAIL:
				// 登录失败，我们得到服务器发回来的消息
                byte[] b = buf.array();
			    replyMessage = Util.getString(b, 1, b.length - 1, QQ.QQ_CHARSET_DEFAULT);
                break;
            case QQ.QQ_REPLY_LOGIN_REDIRECT:
				// 登陆重定向，可能是为了负载平衡
				// 001-004字节是用户QQ号
				buf.getInt();
				// 005-008字节是重定向到的服务器IP
				redirectIp = new byte[4];
				buf.get(redirectIp);
				// 009-010字节是重定向到的服务器的端口
				redirectPort = buf.getChar();
                break;
        }
    }
}
