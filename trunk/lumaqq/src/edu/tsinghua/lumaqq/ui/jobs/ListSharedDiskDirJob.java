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
package edu.tsinghua.lumaqq.ui.jobs;

import static edu.tsinghua.lumaqq.resource.Messages.*;

import java.util.List;

import edu.tsinghua.lumaqq.qq.QQ;
import edu.tsinghua.lumaqq.qq.beans.Directory;
import edu.tsinghua.lumaqq.qq.beans.File;
import edu.tsinghua.lumaqq.qq.events.QQEvent;
import edu.tsinghua.lumaqq.qq.packets.in.disk.ListSharedDiskDirReplyPacket;

/**
 * 列共享网络硬盘目录列表的任务
 *
 * @author luma
 */
public class ListSharedDiskDirJob extends AbstractDiskJob {
	private int diskOwner;
	private List<Directory> dirs;
	private List<File> files;
	
	public ListSharedDiskDirJob(int diskOwner) {
		this.diskOwner = diskOwner;
	}
	
	@Override
	protected String getHint() {
		return disk_hint_list_dir;
	}	
	
	@Override
	protected void onAuthSuccess() {
		main.getClient().listSharedDiskDir(localIp, diskOwner);
	}
	
	@Override
	protected void onExit() {
		if(exitCode != SUCCESS) {
			main.getDiskManager().addErrorHint(diskOwner, getFinishHint());
			if(exitCode == NEED_PASSWORD || exitCode == AUTH_FAIL)
				main.switchToDiskPasswordPanel();
		} else {
			main.getDiskManager().addDiskCache(diskOwner, dirs, files);
		}
		main.refreshDiskViewer();
	}

	public void qqEvent(QQEvent e) {
		super.qqEvent(e);
		switch(e.type) {
			case QQEvent.QQ_DISK_OPERATION_TIMEOUT:
				switch(e.operation) {
					case QQ.QQ_DISK_CMD_LIST_SHARED_DISK_DIR:
						processOperationTimeout();
						break;
				}
				break;
			case QQEvent.QQ_DISK_GET_SHARED_DISK_DIR_SUCCESS:
				processGetSharedDiskDirSuccess(e);
				break;
		}
	}
	
	private void processGetSharedDiskDirSuccess(QQEvent e) {
		ListSharedDiskDirReplyPacket packet = (ListSharedDiskDirReplyPacket)e.getSource();
		dirs = packet.dirs;
		files = packet.files;
		exitCode = SUCCESS;
		wake();
	}
}
