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

import static edu.tsinghua.lumaqq.resource.Messages.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.tsinghua.lumaqq.qq.QQ;
import edu.tsinghua.lumaqq.qq.beans.Directory;
import edu.tsinghua.lumaqq.qq.beans.File;
import edu.tsinghua.lumaqq.ui.MainShell;
import edu.tsinghua.lumaqq.ui.jobs.GetSharedDiskJob;
import edu.tsinghua.lumaqq.ui.jobs.ListMyDiskDirJob;
import edu.tsinghua.lumaqq.ui.jobs.ListSharedDiskDirJob;

/**
 * 网络硬盘管理类
 *
 * @author luma
 */
public class DiskManager {
	private List<Integer> diskOwners;
	private Map<Integer, DiskCache> sharedDisks;
	
	private MainShell main;
	
	private int capacity, unused;
	private String password;
	
	// 用来存放错误提示
	private Map<Integer, String> hints;
	// 用来标识是否已经得到了数据
	private Map<Integer, Integer> onsite;
	
	private static final int ONSITE = 0;
	private static final int REQUESTING = 1;
	private static final int FAIL = 2;
	
	public DiskManager(MainShell main) {
		this.main = main;
		hints = new HashMap<Integer, String>();
		onsite = new HashMap<Integer, Integer>();
		sharedDisks = new HashMap<Integer, DiskCache>();
		capacity = unused = 0;
	}
	
	/**
	 * 根据文件名，在我的网络硬盘里面找寻一个文件
	 * 
	 * @param name
	 * @param dirId
	 * @return
	 */
	public File getMyFile(String name, int dirId) {
		DiskCache cache = sharedDisks.get(main.getMyModel().qq);
		return cache.getFile(name, dirId);
	}
	
	/**
	 * 根据id得到我网络硬盘里面的文件
	 * 
	 * @param id
	 * @return
	 */
	public File getMyFile(String id) {
		DiskCache cache = sharedDisks.get(main.getMyModel().qq);
		return cache.getFile(id);
	}
	
	/**
	 * 检查是否在目录下能创建子目录
	 * 
	 * @param dir
	 * @return
	 */
	public boolean isChildCreatable(Directory dir) {
		if(dir.owner != main.getMyModel().qq)
			return false;
		
		DiskCache cache = sharedDisks.get(main.getMyModel().qq);
		if(cache == null)
			return false;
		int id = dir.id;
		while(id > 0) {
			if(id == QQ.QQ_DISK_DIR_MY_ASSISTANT)
				return false;
			else
				id = cache.getParentId(id);
		}
		return true;
	}
	
	/**
	 * 一个目录是否允许移动
	 * 
	 * @param dir
	 * @return
	 */
	public boolean isMovable(Directory dir) {
		return dir.id > QQ.QQ_DISK_DIR_MAX_SYSTEM_ID;
	}
	
	/**
	 * 一个文件是否允许移动
	 * 
	 * @param file
	 * @return
	 */
	public boolean isMovable(File file) {
		if(file.owner != main.getMyModel().qq)
			return false;
		
		DiskCache cache = sharedDisks.get(file.owner);
		if(cache == null)
			return false;
		Directory dir = cache.getDirectory(file.dirId);
		if(dir == null)
			return false;
		if(!isChildCreatable(dir))
			return false;
		return true;
	}
	
	/**
	 * 添加目录
	 * 
	 * @param dir
	 */
	public void addDirectory(Directory dir) {
		DiskCache cache = sharedDisks.get(dir.owner);
		if(cache != null)
			cache.addDirectory(dir);
	}
	
	/**
	 * 添加文件
	 * 
	 * @param file
	 */
	public void addFile(File file) {
		DiskCache cache = sharedDisks.get(file.owner);
		if(cache != null)
			cache.addFile(file);
	}
	
	/**
	 * 删除文件
	 * 
	 * @param f
	 */
	public void removeFile(File f) {
		DiskCache cache = sharedDisks.get(f.owner);
		if(cache != null)
			cache.removeFile(f);
	}
	
	/**
	 * 删除目录
	 * 
	 * @param dir
	 */
	public void removeDirectory(Directory dir) {
		DiskCache cache = sharedDisks.get(dir.owner);
		if(cache != null)
			cache.removeDir(dir);
	}
	
	/**
	 * 删除一个共享网络硬盘
	 * 
	 * @param qq
	 */
	public void removeSharedDisk(int qq) {
		sharedDisks.remove(qq);
	}
	
	/**
	 * 创建一些缺省的目录
	 */
	private List<Directory> createMyDefaultDir() {
		List<Directory> dirs = new ArrayList<Directory>();
		Directory favDir = new Directory();
		favDir.id = QQ.QQ_DISK_DIR_MY_FAVORITE;
		favDir.parentId = QQ.QQ_DISK_DIR_MY_ASSISTANT;
		favDir.name = disk_my_favorite;
		favDir.owner = main.getMyModel().qq;
		dirs.add(favDir);
		
		Directory album = new Directory();
		album.id = QQ.QQ_DISK_DIR_MY_ALBUM;
		album.parentId = QQ.QQ_DISK_DIR_MY_ASSISTANT;
		album.name = disk_my_album;
		album.owner = main.getMyModel().qq;
		dirs.add(album);
		return dirs;
	}
	
	/**
	 * 查看是否某个操作正在进行
	 * 
	 * @param i
	 * @return
	 */
	private synchronized boolean isRequesting(int i) {
		Integer flag = onsite.get(i);
		return flag != null && flag == REQUESTING;
	}
	
	/**
	 * 是否可用
	 * 
	 * @param i
	 * @return
	 */
	private synchronized boolean isOnsite(int i) {
		Integer flag = onsite.get(i);
		return flag != null && flag == ONSITE;
	}
	
	/**
	 * 是否没有获得过
	 * 
	 * @param i
	 * @return
	 */
	private synchronized boolean isUnavailable(int i) {
		return !onsite.containsKey(i);
	}
	
	/**
	 * 刷新
	 * 
	 * @param i
	 */
	public synchronized void refresh(Object obj) {
		if(obj instanceof Integer) {
			int i = (Integer)obj;
			if(i == DiskContentProvider.MY_DISK)
				i = main.getMyModel().qq;
			if(!isRequesting(i)) {
				switch(i) {
					case DiskContentProvider.SHARED_DISK:
						diskOwners = null;
						onsite.remove(i);
						getDiskOwners();
						break;
					default:
						sharedDisks.remove(i);
						onsite.remove(i);
						getRoots(i);
						break;
				}			
			}			
		} else if(obj instanceof Directory) {
			Directory dir = (Directory)obj;
			if(!isRequesting(dir.id)) {
				onsite.remove(dir.id);
				getChildren(dir);
			}
		}
	}
	
	/**
	 * 添加一个错误提示，同时从请求hash中删除属主，这样的话可以开始下一次请求
	 * 
	 * @param i
	 * @param hint
	 */
	public synchronized void addErrorHint(int i, String hint) {
		hints.put(i, hint);
		onsite.put(i, FAIL);
	}
	
	/**
	 * 添加网络硬盘缓冲
	 * 
	 * @param qq
	 * 		网络硬盘属主
	 * @param dirs
	 * 		目录列表
	 * @param files
	 * 		文件列表
	 */
	public synchronized void addDiskCache(int qq, List<Directory> dirs, List<File> files) {
		DiskCache cache = new DiskCache(dirs, files);
		if(qq == main.getMyModel().qq)
			cache.add(createMyDefaultDir(), null);
		sharedDisks.put(qq, cache);
		onsite.put(qq, ONSITE);
	}
	
	/**
	 * 更新网络硬盘缓冲
	 * 
	 * @param qq
	 * @param dirId
	 * @param dirs
	 * @param files
	 */
	public synchronized void updateDiskCache(int qq, int dirId, List<Directory> dirs, List<File> files) {
		DiskCache cache = sharedDisks.get(qq);
		if(cache == null) {
			cache = new DiskCache(dirs, files);
			sharedDisks.put(qq, cache);
			onsite.put(qq, ONSITE);
		} else
			cache.add(dirs, files);
		onsite.put(dirId, ONSITE);
	}
	
	/**
	 * 得到共享网络硬盘列表
	 * 
	 * @return
	 * 		共享网络硬盘数组
	 */
	public synchronized Object[] getDiskOwners() {
		if(isOnsite(DiskContentProvider.SHARED_DISK))
			return diskOwners.toArray();
		else if(isUnavailable(DiskContentProvider.SHARED_DISK)) {
			main.getDiskJobQueue().addJob(new GetSharedDiskJob());
			onsite.put(DiskContentProvider.SHARED_DISK, REQUESTING);
			hints.put(DiskContentProvider.SHARED_DISK, disk_waiting);
			return new Object[] { disk_waiting };
		} else
			return new Object[] { hints.get(DiskContentProvider.SHARED_DISK) };
	}
	
	/**
	 * 得到某个网络硬盘的根节点
	 * 
	 * @param qq
	 * 		网络硬盘属主
	 * @return
	 * 		根节点数组
	 */
	public synchronized Object[] getRoots(int qq) {
		if(qq == DiskContentProvider.MY_DISK)
			qq = main.getMyModel().qq;
		
		if(isOnsite(qq)) 
			return sharedDisks.get(qq).getRoots();
		else if(isUnavailable(qq)) {
			if(qq == main.getMyModel().qq)
				main.getDiskJobQueue().addJob(new ListMyDiskDirJob());
			else
				main.getDiskJobQueue().addJob(new ListSharedDiskDirJob(qq));
			onsite.put(qq, REQUESTING);
			hints.put(qq, disk_waiting);
			return new Object[] { disk_waiting };
		} else
			return new Object[] { hints.get(qq) };
	}
	
	/**
	 * 得到某个目录的孩子节点
	 * 
	 * @param qq
	 * 		所属的网络硬盘
	 * @param dirId
	 * 		目录id
	 * @return
	 * 		孩子对象数组
	 */
	public synchronized Object[] getChildren(int qq, int dirId) {
		if(qq == DiskContentProvider.MY_DISK)
			qq = main.getMyModel().qq;
		
		if(isOnsite(qq))
			return sharedDisks.get(qq).getElements(dirId);
		else if(isUnavailable(qq)) {
			if(qq == main.getMyModel().qq)
				main.getDiskJobQueue().addJob(new ListMyDiskDirJob());
			else
				main.getDiskJobQueue().addJob(new ListSharedDiskDirJob(qq));
			onsite.put(qq, REQUESTING);
			hints.put(qq, disk_waiting);
			return new Object[] { disk_waiting };	
		} else {
			return new Object[] { hints.get(qq) };
		}
	}
	
	/**
	 * 得到某个目录的孩子对象
	 * 
	 * @param dir
	 * 		Directory对象
	 * @return
	 * 		孩子对象数组
	 */
	public Object[] getChildren(Directory dir) {
		switch(dir.id) {
			case QQ.QQ_DISK_DIR_MY_FAVORITE:
			case QQ.QQ_DISK_DIR_MY_ALBUM:
				DiskCache c = sharedDisks.get(main.getMyModel().qq);
				if(isOnsite(dir.id))
					return c.getElements(dir.id);
				else if(isUnavailable(dir.id)) {
					main.getDiskJobQueue().addJob(new ListMyDiskDirJob(dir.id, (dir.id == QQ.QQ_DISK_DIR_MY_FAVORITE) ? QQ.QQ_DISK_FLAG_FAVORITE : QQ.QQ_DISK_FLAG_ALBUM));
					onsite.put(dir.id, REQUESTING);
					hints.put(dir.id, disk_waiting);
					return new Object[] { disk_waiting };
				} else
					return new Object[] { hints.get(dir.id) };
			default:
				DiskCache cache = sharedDisks.get(dir.owner);
				if(cache == null)
					return new Object[0];
				else
					return cache.getElements(dir.id);
		}
	}
	
	/**
	 * 清空某个网络硬盘目录和文件缓冲
	 * 
	 * @param qq
	 */
	public synchronized void clear(int qq) {
		sharedDisks.remove(qq);
		onsite.remove(qq);
	}

	/**
	 * @param diskOwners the diskOwners to set
	 */
	public synchronized void setDiskOwners(List<Integer> diskOwners) {
		this.diskOwners = diskOwners;
		onsite.put(DiskContentProvider.SHARED_DISK, ONSITE);
	}

	/**
	 * @return the capacity
	 */
	public int getCapacity() {
		return capacity;
	}

	/**
	 * @param capacity the capacity to set
	 */
	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}

	/**
	 * @return the unused
	 */
	public int getUnused() {
		return unused;
	}

	/**
	 * @param unused the unused to set
	 */
	public void setUnused(int unused) {
		this.unused = unused;
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
