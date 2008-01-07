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
import java.util.List;

import edu.tsinghua.lumaqq.qq.QQ;
import edu.tsinghua.lumaqq.qq.annotation.DocumentalPacket;
import edu.tsinghua.lumaqq.qq.annotation.PacketName;
import edu.tsinghua.lumaqq.qq.annotation.RelatedPacket;
import edu.tsinghua.lumaqq.qq.beans.QQUser;
import edu.tsinghua.lumaqq.qq.packets.DiskOutPacket;
import edu.tsinghua.lumaqq.qq.packets.PacketParseException;
import edu.tsinghua.lumaqq.qq.packets.in.disk.SetShareListReplyPacket;

/**
 * <pre>
 * 设置共享列表
 * 1. 头部
 * 2. 某个加密串的长度，4字节
 * 3. 某个加密串
 * Note: 加密串怎么得到，还不清楚。但是好像不影响功能
 * 4. 属主，4字节
 * 5. 要共享的目录id，4字节
 * 6. 目录属性，4字节
 * 7. 操作的好友数目，4字节
 * 8. 好友QQ号，4字节
 * 9. 属性，4字节
 * Note: 9部分用来标识操作的类型，比如如果共享位被置位，则表示添加这个好友，否则是删除这个好友
 * 10. 如果有更多好友，重复8-9部分
 * </pre>
 *
 * @author luma
 */
@DocumentalPacket
@PacketName("修改共享设置请求包")
@RelatedPacket({SetShareListReplyPacket.class})
public class SetShareListPacket extends DiskOutPacket {
	private int owner;
	private int dirId;
	private int property;
	private List<Integer> remove;
	private List<Integer> add;

	public SetShareListPacket(ByteBuffer buf, int length, QQUser user) throws PacketParseException {
		super(buf, length, user);
	}

	public SetShareListPacket(QQUser user) {
		super(QQ.QQ_DISK_CMD_SET_SHARE_LIST, user);
	}
	
	@Override
	public String getPacketName() {
		return "Set Share List Packet";
	}

	@Override
	protected void putBody(ByteBuffer buf) {
		buf.putInt(0);
		buf.putInt(owner);
		buf.putInt(dirId);
		buf.putInt(property);
		
		int size = (remove == null ? 0 : remove.size()) + (add == null ? 0 : add.size());
		buf.putInt(size);
		for(Integer qq : add) {
			buf.putInt(qq);
			buf.putInt(QQ.QQ_DISK_FLAG_DIRECTORY | QQ.QQ_DISK_FLAG_SHARED);
		}
		for(Integer qq : remove) {
			buf.putInt(qq);
			buf.putInt(QQ.QQ_DISK_FLAG_DIRECTORY);
		}
	}

	/**
	 * @return the add
	 */
	public List<Integer> getAdd() {
		return add;
	}

	/**
	 * @param add the add to set
	 */
	public void setAdd(List<Integer> add) {
		this.add = add;
	}

	/**
	 * @return the dirId
	 */
	public int getDirId() {
		return dirId;
	}

	/**
	 * @param dirId the dirId to set
	 */
	public void setDirId(int dirId) {
		this.dirId = dirId;
	}

	/**
	 * @return the owner
	 */
	public int getOwner() {
		return owner;
	}

	/**
	 * @param owner the owner to set
	 */
	public void setOwner(int owner) {
		this.owner = owner;
	}

	/**
	 * @return the remove
	 */
	public List<Integer> getRemove() {
		return remove;
	}

	/**
	 * @param remove the remove to set
	 */
	public void setRemove(List<Integer> remove) {
		this.remove = remove;
	}

	/**
	 * @return the property
	 */
	public int getProperty() {
		return property;
	}

	/**
	 * @param property the property to set
	 */
	public void setProperty(int property) {
		this.property = property;
	}
}
