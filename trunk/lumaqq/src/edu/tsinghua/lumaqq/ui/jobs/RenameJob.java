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
import edu.tsinghua.lumaqq.qq.QQ;
import edu.tsinghua.lumaqq.qq.beans.Directory;
import edu.tsinghua.lumaqq.qq.beans.File;
import edu.tsinghua.lumaqq.qq.events.QQEvent;

/**
 * 重命名文件或者目录的任务
 *
 * @author luma
 */
public class RenameJob extends AbstractDiskJob {
	private File file;
	private Directory dir;
	private boolean renameFile;
	private String newName;
	
	public RenameJob(File f, String newName) {
		this.file = f;
		this.renameFile = true;
		this.newName = newName;
	}
	
	public RenameJob(Directory d, String newName) {
		this.dir = d;
		this.renameFile = false;
		this.newName = newName;
	}

	@Override
	protected String getHint() {
		return disk_hint_rename;
	}

	@Override
	protected void onExit() {
		if(exitCode != SUCCESS) {
			if(exitCode == NEED_PASSWORD || exitCode == AUTH_FAIL)
				main.switchToDiskPasswordPanel();
		} else {
			if(renameFile)
				file.name = newName;
			else
				dir.name = newName;
			main.refreshDiskViewer();
		}
	}

	@Override
	protected String getSuccessHint() {
		return disk_hint_rename_success;
	}
	
	@Override
	protected void onAuthSuccess() {
		if(renameFile)
			main.getClient().renameFile(localIp, file.id, newName);
		else
			main.getClient().renameDirectory(localIp, String.valueOf(dir.id), newName);
	}
	
	@Override
	public void qqEvent(QQEvent e) {
		super.qqEvent(e);
		switch(e.type) {
			case QQEvent.QQ_DISK_RENAME_SUCCESS:
				processRenameSuccess(e);
				break;
			case QQEvent.QQ_DISK_OPERATION_TIMEOUT:
				switch(e.operation) {
					case QQ.QQ_DISK_CMD_RENAME:
						processOperationTimeout();
						break;
				}
				break;
		}
	}

	private void processRenameSuccess(QQEvent e) {
		exitCode = SUCCESS;
		wake();
	}
}
