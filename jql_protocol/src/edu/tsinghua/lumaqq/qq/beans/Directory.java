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
 * QQ硬盘中一个目录的描述Bean
 *
 * @author luma
 */
public class Directory {
	// id
	public int id;
	// 属性
	public int property;
	// 父id
	public int parentId;
	// 创建时间
	public long creationTime;
	// 修改时间
	public long modifiedTime;
	// 目录名
	public String name;
	
	// 非包中字段，为了处理方便而设，这个字段在DiskFamilyProcessor中被设置
	public int owner;
	
	public void readBean(ByteBuffer buf) {
		id = buf.getInt();
		property = buf.getInt();
		parentId = buf.getInt();
		creationTime = (long)buf.getInt() * 1000L;
		modifiedTime = (long)buf.getInt() * 1000L;
		int len = buf.getChar();
		name = Util.getString(buf, len);
	}
	
	/**
	 * @return
	 * 		true表示这是一个系统文件夹
	 */
	public boolean isSystemDir() {
		return id <= QQ.QQ_DISK_DIR_MAX_SYSTEM_ID;
	}
	
	public boolean isShared() {
		return (property & QQ.QQ_DISK_FLAG_SHARED) != 0;
	}
}
