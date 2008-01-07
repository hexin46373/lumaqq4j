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
import edu.tsinghua.lumaqq.qq.packets.out.disk.PasswordOpPacket;

/**
 * 设置网络硬盘密码的任务
 *
 * @author luma
 */
public class SetDiskPasswordJob extends AbstractDiskJob {
	private String oldPassword, newPassword;
	
	public SetDiskPasswordJob(String op, String np) {
		oldPassword = op;
		newPassword = np;
	}

	@Override
	protected String getHint() {
		return disk_hint_set_password;
	}

	@Override
	protected void onExit() {
		if(exitCode == SUCCESS) {
			main.setDiskOpHint(getFinishHint());
			MessageDialog.openInformation(main.getShell(), message_box_common_info_title, message_box_set_disk_password_success);
		} else {
			if(exitCode == NEED_PASSWORD || exitCode == AUTH_FAIL)
				main.switchToDiskPasswordPanel();
			MessageDialog.openError(main.getShell(), message_box_common_fail_title, message_box_set_disk_password_fail);
		}
	}

	@Override
	protected void onAuthSuccess() {
		main.getClient().setDiskPassword(localIp, oldPassword, newPassword);
	}
	
	@Override
	public void qqEvent(QQEvent e) {
		super.qqEvent(e);
		switch(e.type) {
			case QQEvent.QQ_DISK_PASSWORD_OP_SUCCESS:
				processDiskPasswordOpSuccess(e);
				break;
			case QQEvent.QQ_DISK_PASSWORD_OP_FAIL:
				processDiskPasswordOpFail(e);
				break;
			case QQEvent.QQ_DISK_OPERATION_TIMEOUT:
				switch(e.operation) {
					case QQ.QQ_DISK_CMD_PASSWORD_OP:
						processOperationTimeout();
						break;
				}
		}
	}

	private void processDiskPasswordOpFail(QQEvent e) {
		exitCode = FAIL;
		wake();
	}

	private void processDiskPasswordOpSuccess(QQEvent e) {
		PasswordOpPacket packet = (PasswordOpPacket)e.getSource();
		main.getDiskManager().setPassword(packet.getNewPassword());
		exitCode = SUCCESS;
		wake();
	}
}
