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
import edu.tsinghua.lumaqq.qq.packets.in.AuthorizeReplyPacket;

/**
 * <pre>
 * 用来发送验证消息
 * 1. 头部
 * 2. 子命令，1字节
 * 3. 要添加的QQ号，4字节
 * 4. 是否允许对方加自己为好友，1字节
 * 5. 把好友加到第几组，我的好友组是0，然后以此类推，1字节
 * 6. 验证消息字节长度，1字节
 * 7. 验证消息
 * 8. 尾部
 * </pre>
 * 
 * @author luma
 */
@DocumentalPacket
@RelatedPacket({AuthorizeReplyPacket.class})
@PacketName("发送好友认证信息包")
public class AuthorizePacket extends BasicOutPacket {
	private byte subCommand;
	private int to;
	private boolean allowAddReverse;
	private int destGroup;
	private String message;
	
    /**
     * 构造函数
     */
    public AuthorizePacket(QQUser user) {
        super(QQ.QQ_CMD_AUTHORIZE, true, user);
        subCommand = 0x02;
        allowAddReverse = true;
        destGroup = 0;
        message = QQ.EMPTY_STRING;
    }

    /**
     * @param buf
     * @param length
     * @throws PacketParseException
     */
    public AuthorizePacket(ByteBuffer buf, int length, QQUser user)
            throws PacketParseException {
        super(buf, length, user);
    }
    
	@Override
	protected void putBody(ByteBuffer buf) {
		buf.put(subCommand);
		buf.putInt(to);
		buf.put(allowAddReverse ? QQ.QQ_FLAG_ALLOW_ADD_REVERSE : QQ.QQ_FLAG_NOT_ALLOW_ADD_REVERSE);
		buf.put((byte)destGroup);
		byte[] b = Util.getBytes(message);
		buf.put((byte)b.length);
		buf.put(b);
	}
	
	@Override
	public String getPacketName() {
		return "Authorize Packet";
	}

	/**
	 * @return Returns the allowAddReverse.
	 */
	public boolean isAllowAddReverse() {
		return allowAddReverse;
	}

	/**
	 * @param allowAddReverse The allowAddReverse to set.
	 */
	public void setAllowAddReverse(boolean allowAddReverse) {
		this.allowAddReverse = allowAddReverse;
	}

	/**
	 * @return Returns the destGroup.
	 */
	public int getDestGroup() {
		return destGroup;
	}

	/**
	 * @param destGroup The destGroup to set.
	 */
	public void setDestGroup(int destGroup) {
		this.destGroup = destGroup;
	}

	/**
	 * @return Returns the message.
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @param message The message to set.
	 */
	public void setMessage(String message) {
		this.message = message;
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

	/**
	 * @return Returns the subCommand.
	 */
	public byte getSubCommand() {
		return subCommand;
	}

	/**
	 * @param subCommand The subCommand to set.
	 */
	public void setSubCommand(byte subCommand) {
		this.subCommand = subCommand;
	}
}
