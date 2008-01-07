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

import edu.tsinghua.lumaqq.qq.QQ;
import edu.tsinghua.lumaqq.qq.Util;
import edu.tsinghua.lumaqq.qq.annotation.DocumentalPacket;
import edu.tsinghua.lumaqq.qq.annotation.PacketName;
import edu.tsinghua.lumaqq.qq.annotation.RelatedPacket;
import edu.tsinghua.lumaqq.qq.beans.QQUser;
import edu.tsinghua.lumaqq.qq.packets.DiskOutPacket;
import edu.tsinghua.lumaqq.qq.packets.PacketParseException;
import edu.tsinghua.lumaqq.qq.packets.in.disk.RenameReplyPacket;

/**
 * <pre>
 * 更名请求包
 * 1. 头部
 * 2. id，30字节，不足者填0
 * Note: 对于文件来说，id就是30字节，不会不足。而目录的id是整型的，填充2部分时，要把整型变成十进制字符串
 * 3. 属性，4字节，对于文件就是0，对于目录，就把目录位置1就行
 * 4. 新名称长度，2字节
 * 5. 新名称
 * </pre>
 *
 * @author luma
 */
@DocumentalPacket
@PacketName("文件目录改名请求包")
@RelatedPacket({RenameReplyPacket.class})
public class RenamePacket extends DiskOutPacket {
	private String newName;
	private String id;
	private boolean renameFile;
	
	public RenamePacket(ByteBuffer buf, int length, QQUser user) throws PacketParseException {
		super(buf, length, user);
	}

	public RenamePacket(QQUser user) {
		super(QQ.QQ_DISK_CMD_RENAME, user);
	}
	
	@Override
	public String getPacketName() {
		return "Rename Packet";
	}

	@Override
	protected void putBody(ByteBuffer buf) {
		byte[] b = Util.getBytes(id);
		if(b.length >= 30)
			buf.put(b, 0, 30);
		else {
			buf.put(b);
			for(int i = 0; i < 30 - b.length; i++)
				buf.put((byte)0);
		}
		buf.putInt(renameFile ? 0 : QQ.QQ_DISK_FLAG_DIRECTORY);
		b = Util.getBytes(newName);
		buf.putChar((char)b.length);
		buf.put(b);
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the newName
	 */
	public String getNewName() {
		return newName;
	}

	/**
	 * @param newName the newName to set
	 */
	public void setNewName(String newName) {
		this.newName = newName;
	}

	/**
	 * @return the renameFile
	 */
	public boolean isRenameFile() {
		return renameFile;
	}

	/**
	 * @param renameFile the renameFile to set
	 */
	public void setRenameFile(boolean renameFile) {
		this.renameFile = renameFile;
	}
}
