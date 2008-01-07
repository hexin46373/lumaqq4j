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

import java.util.Iterator;
import java.util.List;

import edu.tsinghua.lumaqq.disk.DiskContentProvider;
import edu.tsinghua.lumaqq.qq.QQ;
import edu.tsinghua.lumaqq.qq.events.QQEvent;
import edu.tsinghua.lumaqq.qq.packets.in.disk.GetSharedDiskReplyPacket;

/**
 * 得到共享网络硬盘列表的任务
 *
 * @author luma
 */
public class GetSharedDiskJob extends AbstractDiskJob {    
	private List<Integer> diskOwners;
	
	@Override
	protected String getHint() {
		return disk_hint_get_shared_disk;
	}
	
	@Override
	protected void onExit() {
		if(exitCode != SUCCESS) {
			main.getDiskManager().addErrorHint(DiskContentProvider.SHARED_DISK, getFinishHint());
			if(exitCode == NEED_PASSWORD || exitCode == AUTH_FAIL)
				main.switchToDiskPasswordPanel();
		} else
			main.getDiskManager().setDiskOwners(diskOwners);
		main.refreshDiskViewer();
	}
	
	@Override
	protected void onAuthSuccess() {
		main.getClient().getSharedDisk(localIp);
	}

	public void qqEvent(QQEvent e) {
		super.qqEvent(e);
		switch(e.type) {
			case QQEvent.QQ_DISK_GET_SHARED_DISK_SUCCESS:
				processGetSharedDisk(e);
				break;
			case QQEvent.QQ_DISK_OPERATION_TIMEOUT:
				switch(e.operation) {
					case QQ.QQ_DISK_CMD_GET_SHARED_DISK:
						processOperationTimeout();
						break;
				}
				break;
		}
	}

	private void processGetSharedDisk(QQEvent e) {
		GetSharedDiskReplyPacket packet = (GetSharedDiskReplyPacket)e.getSource();
		diskOwners = packet.diskOwners;		
		for(Iterator<Integer> i = diskOwners.iterator(); i.hasNext(); ) {
			if(i.next() == main.getMyModel().qq)
				i.remove();
		}
		exitCode = SUCCESS;
		wake();
	}
}
