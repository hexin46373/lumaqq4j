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
import edu.tsinghua.lumaqq.qq.packets.in.disk.DeleteReplyPacket;

/**
 * 删除文件或目录的任务
 *
 * @author luma
 */
public class DeleteJob extends AbstractDiskJob {
	private File file;
	private Directory dir;
	private int qq;
	private int op;
	
	private static final int FILE = 0;
	private static final int DIRECTORY = 1;
	private static final int USER = 2;
	
	public DeleteJob(int qq) {
		this.qq = qq;
		op = USER;
	}
	
	public DeleteJob(File f) {
		file = f;
		op = FILE;
	}
	
	public DeleteJob(Directory d) {
		dir = d;
		op = DIRECTORY;
	}

	@Override
	protected String getHint() {
		return disk_hint_delete;
	}

	@Override
	protected void onExit() {
		if(exitCode != SUCCESS) {
			if(exitCode == NEED_PASSWORD || exitCode == AUTH_FAIL)
				main.switchToDiskPasswordPanel();
		} else {
			switch(op) {
				case FILE:
					main.getDiskManager().removeFile(file);
					break;
				case DIRECTORY:
					main.getDiskManager().removeDirectory(dir);
					break;
				case USER:
					main.getDiskManager().removeSharedDisk(qq);
					break;
			}
			main.refreshDiskViewer();
		}
	}

	@Override
	protected void onAuthSuccess() {
		switch(op) {
			case FILE:
				main.getClient().deleteFile(localIp, file.id);
				break;
			case DIRECTORY:
				main.getClient().deleteDirectory(localIp, dir.id);
				break;
			case USER:
				main.getClient().deleteShared(localIp, qq);
				break;
		}
	}
	
	@Override
	public void qqEvent(QQEvent e) {
		super.qqEvent(e);
		switch(e.type) {
			case QQEvent.QQ_DISK_DELETE_SUCCESS:
				processDeleteSuccess(e);
				break;
			case QQEvent.QQ_DISK_OPERATION_TIMEOUT:
				switch(e.operation) {
					case QQ.QQ_DISK_CMD_DELETE:
						processOperationTimeout();
						break;
				}
				break;
		}
	}

	private void processDeleteSuccess(QQEvent e) {
		DeleteReplyPacket packet = (DeleteReplyPacket)e.getSource();
		capacity = packet.capacity;
		unused = packet.unused;
		exitCode = SUCCESS;
		wake();
	}
}
