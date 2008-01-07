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
import edu.tsinghua.lumaqq.qq.packets.in.disk.ListMyDiskDirReplyPacket;

/**
 * 列我的网络硬盘目录列表的任务
 *
 * @author luma
 */
public class ListMyDiskDirJob extends AbstractDiskJob {
	private List<Directory> dirs;
	private List<File> files;
	
	private int dirId;
	private int unknown;
	
	public ListMyDiskDirJob() {
		dirId = QQ.QQ_DISK_DIR_ROOT;
	}
	
	public ListMyDiskDirJob(int dirId, int unknown) {
		this.dirId = dirId;
		this.unknown = unknown;
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
			case QQEvent.QQ_DISK_GET_MY_DISK_DIR_SUCCESS:
				processGetMyDiskDirSuccess(e);
				break;
		}
	}
	
	private void processGetMyDiskDirSuccess(QQEvent e) {
		ListMyDiskDirReplyPacket packet = (ListMyDiskDirReplyPacket)e.getSource();
		dirs = packet.dirs;
		files = packet.files;
		exitCode = SUCCESS;
		wake();
	}

	@Override
	protected String getHint() {
		return disk_hint_list_dir;
	}

	@Override
	protected void onExit() {
		if(exitCode != SUCCESS) {
			if(dirId == QQ.QQ_DISK_DIR_ROOT)
				main.getDiskManager().addErrorHint(main.getMyModel().qq, getFinishHint());
			else
				main.getDiskManager().addErrorHint(dirId, getFinishHint());
			if(exitCode == NEED_PASSWORD || exitCode == AUTH_FAIL)
				main.switchToDiskPasswordPanel();
		} else {
			if(dirId == QQ.QQ_DISK_DIR_ROOT) {
				main.getDiskManager().addDiskCache(main.getMyModel().qq, dirs, files);				
			} else {
				main.getDiskManager().updateDiskCache(main.getMyModel().qq, dirId, dirs, files);
			}
		}
		main.refreshDiskViewer();			
	}

	@Override
	protected void onAuthSuccess() {
		main.getClient().listMyDiskDir(localIp, dirId, unknown);
	}
}
