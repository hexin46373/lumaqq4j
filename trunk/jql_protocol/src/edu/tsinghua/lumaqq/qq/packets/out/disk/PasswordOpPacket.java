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
import edu.tsinghua.lumaqq.qq.annotation.DocumentalPacket;
import edu.tsinghua.lumaqq.qq.annotation.PacketName;
import edu.tsinghua.lumaqq.qq.annotation.RelatedPacket;
import edu.tsinghua.lumaqq.qq.beans.QQUser;
import edu.tsinghua.lumaqq.qq.packets.DiskOutPacket;
import edu.tsinghua.lumaqq.qq.packets.PacketParseException;
import edu.tsinghua.lumaqq.qq.packets.in.disk.PasswordOpReplyPacket;

/**
 * <pre>
 * 网络硬盘密码操作
 * 1. 头部
 * 2. 我的QQ号，4字节
 * 3. 子命令，4字节
 * 4. 旧密码加密串长度，2字节
 * 5. 旧密码加密串
 * 6. 新密码加密串长度，2字节
 * 7. 新密码加密串
 * </pre>
 *
 * @author luma
 */
@DocumentalPacket
@PacketName("网络硬盘密码操作请求包")
@RelatedPacket({PasswordOpReplyPacket.class})
public class PasswordOpPacket extends DiskOutPacket {
	private int subCommand;
	private String password;
	private String newPassword;

	public PasswordOpPacket(ByteBuffer buf, int length, QQUser user) throws PacketParseException {
		super(buf, length, user);
	}

	public PasswordOpPacket(QQUser user) {
		super(QQ.QQ_DISK_CMD_PASSWORD_OP, user);
	}
	
	@Override
	public String getPacketName() {
		return "Password Op Packet";
	}

	@Override
	protected void putBody(ByteBuffer buf) {
		buf.putInt(user.getQQ());
		buf.putInt(subCommand);
		
		byte[] key = new byte[16];
		System.arraycopy(user.getAuthToken(), 0, key, 0, 16);
		if(password == null || password.equals(""))
			buf.putChar((char)0);
		else {
			byte[] md5 = DigestUtils.md5(password);
			byte[] b = crypter.encrypt(md5, key);
			buf.putChar((char)b.length);
			buf.put(b);
		}
		
		switch(subCommand) {
			case QQ.QQ_DISK_SUB_CMD_AUTHENTICATE:
			case QQ.QQ_DISK_SUB_CMD_CANCEL_PASSWORD:
				buf.putChar((char)0);
				break;
			default:
				if(newPassword == null || newPassword.equals(""))
					buf.putChar((char)0);
				else {
					byte[] md5 = DigestUtils.md5(newPassword);
					byte[] b = crypter.encrypt(md5, key);
					buf.putChar((char)b.length);
					buf.put(b);
				}
				break;
		}
	}

	/**
	 * @return the newPassword
	 */
	public String getNewPassword() {
		return newPassword;
	}

	/**
	 * @param newPassword the newPassword to set
	 */
	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
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

	/**
	 * @return the subCommand
	 */
	public int getSubCommand() {
		return subCommand;
	}

	/**
	 * @param subCommand the subCommand to set
	 */
	public void setSubCommand(int subCommand) {
		this.subCommand = subCommand;
	}
}
