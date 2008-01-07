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

import java.net.InetSocketAddress;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;

import edu.tsinghua.lumaqq.qq.QQ;
import edu.tsinghua.lumaqq.qq.QQPort;
import edu.tsinghua.lumaqq.qq.Util;
import edu.tsinghua.lumaqq.qq.events.IQQListener;
import edu.tsinghua.lumaqq.qq.events.QQEvent;
import edu.tsinghua.lumaqq.qq.net.IConnection;
import edu.tsinghua.lumaqq.qq.packets.DiskInPacket;
import edu.tsinghua.lumaqq.qq.packets.ErrorPacket;
import edu.tsinghua.lumaqq.qq.packets.in.disk.AuthenticateReplyPacket;
import edu.tsinghua.lumaqq.ui.MainShell;
import edu.tsinghua.lumaqq.ui.helper.FileTool;
import edu.tsinghua.lumaqq.ui.helper.NetTool;

/**
 * 抽象网络硬盘任务
 *
 * @author luma
 */
public abstract class AbstractDiskJob extends AbstractJob implements IQQListener {	
	protected IConnection conn;
	protected String localIp;
	protected int capacity;
	protected int unused;
	
	protected static final int AUTH_FAIL = 3;
	protected static final int NEED_PASSWORD = 4;
	protected static final int CANNOT_CONNECT = 5;
	protected static final int OTHER_ERROR = 6;
	protected static final int CANNOT_OPEN_FILE = 7;
	protected static final int WRITE_FILE_ERROR = 8;
	protected static final int FILE_ABORT = 9;
	protected static final int READ_FILE_ERROR = 10;
	protected static final int DISK_NOT_OPEN = 11;
	
	@Override
	public void prepare(MainShell m) {
		super.prepare(m);
		localIp = NetTool.getLocalIp();
		capacity = unused = 0;
		exitCode = SUCCESS;
		main.getClient().addQQListener(this);
	}
	
	public void clear() {
		if(conn != null) {
			main.getClient().releaseConnection(conn.getId());
			conn = null;
		}
		main.getClient().removeQQListener(this);
		super.clear();
	}

	/**
	 * @return
	 * 		提示信息
	 */
	protected abstract String getHint();
	
	/**
	 * 退出时调用
	 */
	protected abstract void onExit();
	
	/**
	 * 认证成功时调用
	 */
	protected abstract void onAuthSuccess();
	
	/**
	 * @return
	 * 		任务结束时的提示信息
	 */
	protected String getFinishHint() {
		switch(exitCode) {
			case AUTH_FAIL:
				return disk_hint_auth_fail;
			case CANNOT_CONNECT:
				return disk_hint_network_error;
			case TIMEOUT:
				return disk_hint_timeout;
			case NEED_PASSWORD:
				return disk_hint_need_password;
			case FAIL:
				return disk_hint_fail;
			case OTHER_ERROR:
				return errorMessage;
			case CANNOT_OPEN_FILE:
				return disk_hint_cannot_open_file;
			case WRITE_FILE_ERROR:
				return disk_hint_write_error;
			case READ_FILE_ERROR:
				return disk_hint_read_error;
			case DISK_NOT_OPEN:
				return disk_hint_disk_not_open;
			default:
				return getSuccessHint();
		}
	}

	protected String getSuccessHint() {
		return NLS.bind(disk_hint_capacity, FileTool.getSizeString(unused), FileTool.getSizeString(capacity));
	}
	
	@Override
	protected void preLoop() {
		main.getDisplay().syncExec(new Runnable() {
			public void run() {
				main.setDiskOpHint(getHint());
			}
		});
		
		// 如果存在这个port，关闭它
		if(main.getClient().getConnectionPool().hasConnection(QQPort.DISK.name)) {
			main.getClient().releaseConnection(QQPort.DISK.name);
		}
		
		// 创建一个新port
		try {
			int index = Util.random().nextInt(QQ.QQ_SERVER_DISK.length);
			conn = QQPort.DISK.create(main.getClient(), new InetSocketAddress(QQ.QQ_SERVER_DISK[index], QQ.QQ_SERVER_DISK_PORT), null, true);
		} catch(Exception e) {
			exitCode = CANNOT_CONNECT;
			finished = true;
			return;
		} 
	}
	
	@Override
	protected void postLoop() {
		exit();
	}

	/**
	 * @return
	 * 		true表示可以继续运行，false表示有些条件不足，无法运行
	 */
	protected boolean preRun() {
		return true;
	}

	/**
	 * 退出
	 */
	protected void exit() {
		main.getDisplay().syncExec(new Runnable() {
			public void run() {
				onExit();
				main.setDiskOpHint(getFinishHint());
				if(exitCode == DISK_NOT_OPEN) {
					if(MessageDialog.openQuestion(main.getShell(), message_box_common_question_title, disk_hint_do_you_want_to_apply)) {
						main.getDiskJobQueue().addJob(new ApplyDiskJob());
					}					
				}
			}
		});
	}

	public void qqEvent(QQEvent e) {
		switch(e.type) {
			case QQEvent.QQ_DISK_BEGIN_DISK_SESSION:
				processBeginDiskSession(e);
				break;
			case QQEvent.QQ_DISK_AUTHENTICATE_SUCCESS:
				processAuthenticateSuccess(e);
				break;
			case QQEvent.QQ_DISK_AUTHENTICATE_FAIL:
				processAuthenticateFail(e);
				break;
			case QQEvent.QQ_DISK_OPERATION_ERROR:
				processOperationError(e);
				break;
			case QQEvent.QQ_NETWORK_ERROR:
			case QQEvent.QQ_CONNECTION_BROKEN:
				processNetworkError(e);
				break;
			case QQEvent.QQ_DISK_OPERATION_TIMEOUT:
				switch(e.operation) {
					case QQ.QQ_DISK_CMD_AUTHENTICATE:
						processOperationTimeout();
						break;
				}
				break;
		}
	}
	
	/**
	 * 处理其他错误
	 * 
	 * @param e
	 */
	private void processOperationError(QQEvent e) {
		errorMessage = ((DiskInPacket)e.getSource()).replyMessage;
		exitCode = OTHER_ERROR;
		wake();
	}

	protected void processBeginDiskSession(QQEvent e) {
		main.getClient().authenticateDisk(localIp, main.getMyModel().nick, main.getDiskManager().getPassword());
	}
	
	protected void processOperationTimeout() {
		exitCode = TIMEOUT;
		wake();
	}
	
	protected void processNetworkError(QQEvent e) {
		ErrorPacket packet = (ErrorPacket)e.getSource();
		if(packet.connectionId.equals(QQPort.DISK.name)) {
			exitCode = CANNOT_CONNECT;
			wake();
		}
	}
	
	protected void processAuthenticateFail(QQEvent e) {
		exitCode = AUTH_FAIL;
		wake();
	}
	
	protected void processAuthenticateSuccess(QQEvent e) {
		AuthenticateReplyPacket packet = (AuthenticateReplyPacket)e.getSource();
		if(packet.capacity <= 0) {
			exitCode = DISK_NOT_OPEN;
			wake();
		} else if(packet.isNeedPassword()) {
			exitCode = NEED_PASSWORD;
			wake();
		} else {
			capacity = packet.capacity;
			unused = packet.capacity - packet.used;
			main.getDiskManager().setCapacity(capacity);
			main.getDiskManager().setUnused(unused);
			onAuthSuccess();
		}
	}
}
