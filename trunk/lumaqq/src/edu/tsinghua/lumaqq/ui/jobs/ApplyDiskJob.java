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
import org.eclipse.jface.dialogs.MessageDialog;

import edu.tsinghua.lumaqq.qq.QQ;
import edu.tsinghua.lumaqq.qq.events.QQEvent;
import edu.tsinghua.lumaqq.qq.packets.in.disk.AuthenticateReplyPacket;

/**
 * 申请网络硬盘的任务
 *
 * @author luma
 */
public class ApplyDiskJob extends AbstractDiskJob {
	@Override
	protected String getHint() {
		return disk_hint_apply;
	}

	@Override
	protected void onExit() {
	}
	
	@Override
	protected void processAuthenticateSuccess(QQEvent e) {
		AuthenticateReplyPacket packet = (AuthenticateReplyPacket)e.getSource();
		if(packet.capacity <= 0) {
			onAuthSuccess();
		} else {
			exitCode = SUCCESS;
			wake();
		}
	}

	@Override
	protected void onAuthSuccess() {
		main.getClient().applyDisk(localIp);
	}
	
	@Override
	public void qqEvent(QQEvent e) {
		super.qqEvent(e);
		switch(e.type) {
			case QQEvent.QQ_DISK_APPLY_SUCCESS:
				processApplySuccess(e);
				break;
			case QQEvent.QQ_DISK_OPERATION_TIMEOUT:
				switch(e.operation) {
					case QQ.QQ_DISK_CMD_APPLY:
						processOperationTimeout();
						break;
				}
				break;
		}
	}

	private void processApplySuccess(QQEvent e) {
		exitCode = SUCCESS;
		wake();
		main.getDisplay().asyncExec(new Runnable() {
			public void run() {
				MessageDialog.openInformation(main.getShell(), message_box_common_info_title, disk_hint_apply_success);
			}
		});
	}
}
