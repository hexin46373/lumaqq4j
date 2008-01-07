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
package edu.tsinghua.lumaqq.qq.packets.in.disk;

import static edu.tsinghua.lumaqq.qq.events.QQEvent.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import edu.tsinghua.lumaqq.qq.QQ;
import edu.tsinghua.lumaqq.qq.annotation.DocumentalPacket;
import edu.tsinghua.lumaqq.qq.annotation.LinkedEvent;
import edu.tsinghua.lumaqq.qq.annotation.PacketName;
import edu.tsinghua.lumaqq.qq.annotation.RelatedPacket;
import edu.tsinghua.lumaqq.qq.beans.Directory;
import edu.tsinghua.lumaqq.qq.beans.File;
import edu.tsinghua.lumaqq.qq.beans.QQUser;
import edu.tsinghua.lumaqq.qq.packets.DiskInPacket;
import edu.tsinghua.lumaqq.qq.packets.PacketParseException;
import edu.tsinghua.lumaqq.qq.packets.out.disk.ListSharedDiskDirPacket;

/**
 * <pre>
 * 列目录的回复包
 * 1. 头部
 * 2. 目录个数，4字节
 * 3. 目录ID，4字节
 * 4. 未知4字节
 * 5. 父目录id，4字节
 * 6. 目录创建时间，4字节
 * 7. 目录修改时间，4字节
 * 8. 目录名长度，2字节
 * 9. 目录名
 * 10. 如果有更多目录，重复3-8部分
 * 11. 文件个数，4字节
 * 12. 文件ID字符串，30字节
 * 13. 未知4字节
 * 14. 文件长度，4字节
 * 15. 文件长度，4字节
 * Note: 14, 15内容一样，非常费解
 * 16. 父目录id，4字节
 * 17. 文件创建时间，4字节
 * 18. 文件修改时间，4字节
 * 19. 文件名长度，2字节
 * 20. 文件名
 * 21. 文件名2长度，2字节
 * 22. 文件名2
 * Note: 文件名2是用来干什么的，目前还不清楚。目前只有自定义头像使用了这个字段
 * 23. 某个字符串的长度，2字节
 * 24. 某个字符串
 * Note: 某个字符串是什么，还没搞清楚
 * 25. 如果有更多文件，重复12-24部分
 * </pre>
 *
 * @author luma
 */
@DocumentalPacket
@PacketName("获取共享网络硬盘目录列表回复包")
@RelatedPacket({ListSharedDiskDirPacket.class})
@LinkedEvent({QQ_DISK_GET_SHARED_DISK_DIR_SUCCESS})
public class ListSharedDiskDirReplyPacket extends DiskInPacket {
	public List<Directory> dirs;
	public List<File> files;
	
	// 协议中并无此字段，这个字段是为了事件处理的方便添加的，其会在processor中被填充
	private int diskOwner;

	public ListSharedDiskDirReplyPacket(ByteBuffer buf, int length, QQUser user) throws PacketParseException {
		super(buf, length, user);
	}
	
	@Override
	public String getPacketName() {
		return "List Shared Disk Dir Reply Packet";
	}

	@Override
	protected void parseBody(ByteBuffer buf) throws PacketParseException {
		if(replyCode == QQ.QQ_REPLY_OK) {
			int count = buf.getInt();
			dirs = new ArrayList<Directory>();
			while(count-- > 0) {
				Directory dir = new Directory();
				dir.readBean(buf);
				dirs.add(dir);
			}
			
			count = buf.getInt();
			files = new ArrayList<File>();
			while(count-- > 0) {
				File file = new File();
				file.readBean(buf);
				files.add(file);
			}			
		}		
	}

	/**
	 * @return the diskOwner
	 */
	public int getDiskOwner() {
		return diskOwner;
	}

	/**
	 * @param diskOwner the diskOwner to set
	 */
	public void setDiskOwner(int diskOwner) {
		this.diskOwner = diskOwner;
		for(File f : files)
			f.owner = diskOwner;
		for(Directory d : dirs)
			d.owner = diskOwner;
	}
}
