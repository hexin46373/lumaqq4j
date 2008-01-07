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
package edu.tsinghua.lumaqq.qq.packets.out.disk;

import java.nio.ByteBuffer;

import org.apache.commons.codec.digest.DigestUtils;

import edu.tsinghua.lumaqq.qq.QQ;
import edu.tsinghua.lumaqq.qq.Util;
import edu.tsinghua.lumaqq.qq.annotation.DocumentalPacket;
import edu.tsinghua.lumaqq.qq.annotation.PacketName;
import edu.tsinghua.lumaqq.qq.annotation.RelatedPacket;
import edu.tsinghua.lumaqq.qq.beans.QQUser;
import edu.tsinghua.lumaqq.qq.packets.DiskOutPacket;
import edu.tsinghua.lumaqq.qq.packets.PacketParseException;
import edu.tsinghua.lumaqq.qq.packets.in.disk.AuthenticateReplyPacket;

/**
 * <pre>
 * 网络硬盘登录请求包
 * 1. 头部
 * 2. 我的QQ号，4字节
 * 3. 我的认证令牌，24字节
 * 4. 未知的12字节
 * 5. 密码加密串长度，2字节
 * 6. 密码加密串
 * Note: 密码加密串的生成方法是: 用authToken的前16个字节，加密密码的MD5
 * 7. 我的昵称，12字节，不足填0
 * 8. 未知的20字节
 * </pre>
 *
 * @author luma
 */
@DocumentalPacket
@PacketName("网络硬盘登录请求包")
@RelatedPacket({AuthenticateReplyPacket.class})
public class AuthenticatePacket extends DiskOutPacket {
	private String nick;
	private String password;
	
	public AuthenticatePacket(QQUser user) {
		super(QQ.QQ_DISK_CMD_AUTHENTICATE, user);
	}

	public AuthenticatePacket(ByteBuffer buf, int length, QQUser user) throws PacketParseException {
		super(buf, length, user);
	}
	
	@Override
	public String getPacketName() {
		return "Disk Authenticate Packet";
	}

	@Override
	protected void putBody(ByteBuffer buf) {
		// qq号
		buf.putInt(user.getQQ());
		// 认证令牌
		buf.put(user.getAuthToken());
		// 未知12字节
		buf.putLong(0);
		buf.putInt(0);
		// 密码加密串
		if(password == null || password.equals(""))
			buf.putChar((char)0);
		else {
			byte[] md5 = DigestUtils.md5(password);
			byte[] key = new byte[16];
			System.arraycopy(user.getAuthToken(), 0, key, 0, 16);
			byte[] b = crypter.encrypt(md5, key);
			buf.putChar((char)b.length);
			buf.put(b);
		}
		// 昵称
		byte[] b = Util.getBytes(nick);
		if(b.length > 12)
			buf.put(b, 0, 12);
		else {
			buf.put(b);
			int emptyLen = 12 - b.length;
			for(int i = 0; i < emptyLen; i++)
				buf.put((byte)0);
		}
		// 未知20字节
		for(int i = 0; i < 5; i++)
			buf.putInt(0);
	}

	/**
	 * @return the nick
	 */
	public String getNick() {
		return nick;
	}

	/**
	 * @param nick the nick to set
	 */
	public void setNick(String nick) {
		this.nick = nick;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}
}
