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
package edu.tsinghua.lumaqq.qq.beans;

import java.nio.ByteBuffer;

import edu.tsinghua.lumaqq.qq.QQ;
import edu.tsinghua.lumaqq.qq.Util;

/**
 * QQ硬盘中一个文件的描述Bean
 *
 * @author luma
 */
public class File {
	public String id;
	public int property;
	public int size;
	public int dirId;
	public long creationTime;
	public long modifiedTime;
	public String name;
	public String name2;
	public String content;
	
	// 非包中字段，为了处理方便而设，这个字段在DiskFamilyProcessor中被设置
	public int owner;
	
	public void readBean(ByteBuffer buf) {
		id = Util.getString(buf, 30);
		property = buf.getInt();
		size = buf.getInt();
		size = buf.getInt(); // 不明白为什么有两个size，而且值都相同
		dirId = buf.getInt();
		creationTime = (long)buf.getInt() * 1000L;
		modifiedTime = (long)buf.getInt() * 1000L;
		int len = buf.getChar();
		name = Util.getString(buf, len);
		len = buf.getChar();
		name2 = Util.getString(buf, len);
		len = buf.getChar();
		content = Util.getString(buf, len);
	}
	
	public boolean isFinalized() {
		return (property & QQ.QQ_DISK_FLAG_NOT_FINALIZED) == 0;
	}
}
