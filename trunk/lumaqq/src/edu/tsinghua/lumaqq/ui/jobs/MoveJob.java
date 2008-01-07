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
 * 移动文件或目录的任务
 *
 * @author luma
 */
public class MoveJob extends AbstractDiskJob {
	private File file;
	private Directory dir;
	private int toId;
	private boolean moveFile;
	
	public MoveJob(File f, int to) {
		file = f;
		toId = to;
		moveFile = true;
	}
	
	public MoveJob(Directory d, int to) {
		dir = d;
		toId = to;
		moveFile = false;
	}

	@Override
	protected String getHint() {
		return disk_hint_move;
	}

	@Override
	protected void onExit() {
		if(exitCode != SUCCESS) {
			if(exitCode == NEED_PASSWORD || exitCode == AUTH_FAIL)
				main.switchToDiskPasswordPanel();
		} else {
			if(moveFile)
				file.dirId = toId;
			else
				dir.parentId = toId;
			main.refreshDiskViewer();
		}
	}

	@Override
	protected void onAuthSuccess() {
		if(moveFile)
			main.getClient().moveFile(localIp, file.id, file.dirId, toId);
		else
			main.getClient().moveDirectory(localIp, dir.id, dir.parentId, toId);
	}
	
	@Override
	public void qqEvent(QQEvent e) {
		super.qqEvent(e);
		switch(e.type) {
			case QQEvent.QQ_DISK_MOVE_SUCCESS:
				processMoveSuccess(e);
				break;
			case QQEvent.QQ_DISK_OPERATION_TIMEOUT:
				switch(e.operation) {
					case QQ.QQ_DISK_CMD_MOVE:
						processOperationTimeout();
						break;
				}
				break;
		}
	}

	private void processMoveSuccess(QQEvent e) {
		exitCode = SUCCESS;
		wake();
	}
}
