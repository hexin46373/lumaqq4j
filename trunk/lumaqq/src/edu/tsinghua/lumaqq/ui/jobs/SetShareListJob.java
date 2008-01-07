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
import edu.tsinghua.lumaqq.qq.events.QQEvent;

/**
 * 设置目录共享的任务
 *
 * @author luma
 */
public class SetShareListJob extends AbstractDiskJob {
	private Directory dir;
	private List<Integer> remove;
	private List<Integer> add;
	private boolean unshare;
	
	public SetShareListJob(Directory d, boolean unshare, List<Integer> remove, List<Integer> add) {
		dir = d;
		this.remove = remove;
		this.add = add;
		this.unshare = unshare;
	}

	@Override
	protected String getHint() {
		return disk_hint_set_share_list;
	}

	@Override
	protected void onExit() {
		if(exitCode != SUCCESS) {
			if(exitCode == NEED_PASSWORD || exitCode == AUTH_FAIL)
				main.switchToDiskPasswordPanel();
		} 
	}

	@Override
	protected void onAuthSuccess() {
		if(unshare)
			main.getClient().unshareDirectory(localIp, dir.id, remove, add);
		else
			main.getClient().shareDirectory(localIp, dir.id, remove, add);
	}
	
	@Override
	public void qqEvent(QQEvent e) {
		super.qqEvent(e);
		switch(e.type) {
			case QQEvent.QQ_DISK_SET_SHARE_LIST_SUCCESS:
				processSetSharedListSuccess(e);
				break;
			case QQEvent.QQ_DISK_OPERATION_TIMEOUT:
				switch(e.operation) {
					case QQ.QQ_DISK_CMD_SET_SHARE_LIST:
						processOperationTimeout();
						break;
				}
				break;
		}
	}

	private void processSetSharedListSuccess(QQEvent e) {
		exitCode = SUCCESS;
		wake();
	}
}
