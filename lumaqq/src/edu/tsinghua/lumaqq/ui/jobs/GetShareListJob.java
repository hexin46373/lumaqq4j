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
import edu.tsinghua.lumaqq.qq.packets.in.disk.GetShareListReplyPacket;

/**
 * 得到共享列表的任务
 *
 * @author luma
 */
public class GetShareListJob extends AbstractDiskJob {
	private Directory dir;
	private List<Integer> friends;
	
	public GetShareListJob(Directory d) {
		dir = d;
	}

	@Override
	protected String getHint() {
		return disk_hint_get_share_list;
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
		main.getClient().getShareList(localIp, dir.id);
	}
	
	@Override
	public void qqEvent(QQEvent e) {
		super.qqEvent(e);
		switch(e.type) {
			case QQEvent.QQ_DISK_GET_SHARE_LIST_SUCCESS:
				processGetShareListSuccess(e);
				break;
			case QQEvent.QQ_DISK_OPERATION_TIMEOUT:
				switch(e.operation) {
					case QQ.QQ_DISK_CMD_GET_SHARE_LIST:
						processOperationTimeout();
						break;
				}
				break;
		}
	}

	private void processGetShareListSuccess(QQEvent e) {
		GetShareListReplyPacket packet = (GetShareListReplyPacket)e.getSource();
		friends = packet.friends;
		exitCode = SUCCESS;
		wake();
	}

	/**
	 * @return the friends
	 */
	public List<Integer> getFriends() {
		return friends;
	}
}
