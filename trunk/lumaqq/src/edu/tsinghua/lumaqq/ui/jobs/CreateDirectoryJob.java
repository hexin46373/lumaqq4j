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
import edu.tsinghua.lumaqq.disk.DiskContentProvider;
import edu.tsinghua.lumaqq.qq.QQ;
import edu.tsinghua.lumaqq.qq.Util;
import edu.tsinghua.lumaqq.qq.beans.Directory;
import edu.tsinghua.lumaqq.qq.events.QQEvent;
import edu.tsinghua.lumaqq.qq.packets.in.disk.CreateReplyPacket;

/**
 * 创建一个目录的任务
 *
 * @author luma
 */
public class CreateDirectoryJob extends AbstractDiskJob {
	private String name;
	private int parentId;
	private int id;
	
	public CreateDirectoryJob(String name, int parentId) {
		this.name = name;
		this.parentId = parentId;
	}

	@Override
	protected String getHint() {
		return disk_hint_create;
	}

	@Override
	protected void onExit() {
		if(exitCode != SUCCESS) {
			main.getDiskManager().addErrorHint(DiskContentProvider.SHARED_DISK, getFinishHint());
			if(exitCode == NEED_PASSWORD || exitCode == AUTH_FAIL)
				main.switchToDiskPasswordPanel();
		} else {
			Directory dir = new Directory();
			dir.owner = main.getMyModel().qq;
			dir.id = id;
			dir.name = name;
			dir.parentId = parentId;
			main.getDiskManager().addDirectory(dir);				
			main.refreshDiskViewer();
		}
	}

	@Override
	protected void onAuthSuccess() {
		main.getClient().createDirectory(localIp, name, parentId);
	}
	
	@Override
	public void qqEvent(QQEvent e) {
		super.qqEvent(e);
		switch(e.type) {
			case QQEvent.QQ_DISK_CREATE_SUCCESS:
				processCreateSuccess(e);
				break;
			case QQEvent.QQ_DISK_OPERATION_TIMEOUT:
				switch(e.operation) {
					case QQ.QQ_DISK_CMD_CREATE:
						processOperationTimeout();
						break;
				}
				break;
		}
	}

	private void processCreateSuccess(QQEvent e) {
		CreateReplyPacket packet = (CreateReplyPacket)e.getSource();
		capacity = packet.capacity;
		unused = packet.unused;
		id = Util.getInt(packet.id, 0);
		exitCode = SUCCESS;
		wake();
	}
}
