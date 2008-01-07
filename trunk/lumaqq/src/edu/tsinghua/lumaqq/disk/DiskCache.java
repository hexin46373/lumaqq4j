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
package edu.tsinghua.lumaqq.disk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.tsinghua.lumaqq.qq.beans.Directory;
import edu.tsinghua.lumaqq.qq.beans.File;

/**
 * 某个好友的共享网络硬盘缓冲
 *
 * @author luma
 */
class DiskCache {
	private List<Directory> dirs;
	private List<File> files;
	
	private Map<Integer, Directory> dirHash;
	private Map<String, File> fileHash;
	private int rootId;
	
	private List<Object> temp;
	
	public DiskCache(List<Directory> dirs, List<File> files) {
		this.dirs = dirs;
		this.files = files;
		dirHash = new HashMap<Integer, Directory>();
		fileHash = new HashMap<String, File>();
		rootId = Integer.MAX_VALUE;
		for(Directory dir : dirs) {
			dirHash.put(dir.id, dir);
			if(dir.parentId != -1)
				rootId = Math.min(rootId, dir.parentId);
		}
		if(rootId == Integer.MAX_VALUE)
			rootId = 0;
		for(File file : files) {
			fileHash.put(file.id, file);
		}
		temp = new ArrayList<Object>();
	}
	
	File getFile(String name, int dirId) {
		for(File f : files) {
			if(f.name.equals(name))
				return f;
		}
		return null;
	}
	
	File getFile(String id) {
		return fileHash.get(id);
	}
	
	int getParentId(int id) {
		Directory dir = dirHash.get(id);
		if(dir == null)
			return 0;
		else
			return dir.parentId;
	}
	
	public Directory getDirectory(int id) {
		return dirHash.get(id);
	}
	
	/**
	 * 添加一个目录
	 * 
	 * @param dir
	 */
	public void addDirectory(Directory dir) {
		dirs.add(dir);
		dirHash.put(dir.id, dir);
	}
	
	/**
	 * 添加一个文件
	 * 
	 * @param file
	 */
	public void addFile(File file) {
		files.add(file);
		fileHash.put(file.id, file);
	}
	
	/**
	 * 删除一个文件
	 * 
	 * @param f
	 */
	public void removeFile(File f) {
		fileHash.remove(f.id);
		files.remove(f);
	}
	
	/**
	 * 删除目录
	 * 
	 * @param dir
	 */
	public void removeDir(Directory dir) {
		dirHash.remove(dir.id);
		dirs.remove(dir);
		int size = files.size();
		for(int i = size - 1; i >= 0; i--) {
			File f = files.get(i);
			if(f.dirId == dir.id) 
				removeFile(f);
		}
		for(int i = 0; i < dirs.size(); ) {
			Directory d = dirs.get(i);
			if(d.parentId == dir.id)
				removeDir(d);
			else
				i++;
		}
	}
	
	/**
	 * 添加目录和文件项项，不删除
	 * 
	 * @param dirs
	 * @param files
	 */
	public void add(List<Directory> dirs, List<File> files) {
		if(dirs != null) {
			for(Directory dir : dirs) {
				if(!dirHash.containsKey(dir.id)) {
					dirHash.put(dir.id, dir);
					if(dir.parentId != -1)
						rootId = Math.min(rootId, dir.parentId);			
					this.dirs.add(dir);
				}
			}			
		}
		if(files != null) {
			for(File file : files) {
				if(!fileHash.containsKey(file.id)) {
					fileHash.put(file.id, file);
					this.files.add(file);
				}
			}			
		}
	}
	
	/**
	 * 是否存在这个文件
	 * 
	 * @param file
	 * @return
	 */
	boolean hasFile(File file) {
		return fileHash.containsValue(file);
	}
	
	/**
	 * 是否存在这个文件
	 * 
	 * @param id
	 * @return
	 */
	boolean hasFile(String id) {
		return fileHash.containsKey(id);
	}
	
	/**
	 * 查找是否存在这个dir对象
	 * 
	 * @param id
	 * @return
	 */
	boolean hasDir(int id) {
		return dirHash.containsKey(id);
	}
	
	/**
	 * 查找是否存在这个dir对象
	 * 
	 * @param dir
	 * @return
	 */
	boolean hasDir(Directory dir) {
		return dirHash.containsValue(dir);
	}
	
	/**
	 * @return
	 * 		根目录
	 */
	public Object[] getRoots() {
		temp.clear();
		findByParentId(rootId, temp);
		return temp.toArray();
	}
	
	/**
	 * 根据父目录id得到子目录和文件
	 * 
	 * @param parentId
	 * @return
	 */
	public Object[] getElements(int parentId) {
		temp.clear();
		findByParentId(parentId, temp);
		return temp.toArray();
	}
	
	/**
	 * 查找所有的目录和文件，找到父目录id一致的对象
	 * 
	 * @param parentId
	 * @param list
	 */
	private void findByParentId(int parentId, List<Object> list) {
		for(Directory dir : dirs) {
			if(dir.parentId == parentId)
				list.add(dir);
		}
		for(File f : files) {
			if(f.dirId == parentId)
				list.add(f);
		}
	}
}
