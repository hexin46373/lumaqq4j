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
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;

import org.eclipse.osgi.util.NLS;

import edu.tsinghua.lumaqq.qq.QQ;
import edu.tsinghua.lumaqq.qq.QQPort;
import edu.tsinghua.lumaqq.qq.Util;
import edu.tsinghua.lumaqq.qq.beans.File;
import edu.tsinghua.lumaqq.qq.events.QQEvent;
import edu.tsinghua.lumaqq.qq.packets.in.disk.DiskInPacketFragment;
import edu.tsinghua.lumaqq.qq.packets.in.disk.DownloadReplyPacket;

/**
 * 下载网络硬盘文件的任务
 *
 * @author luma
 */
public class DownloadFileJob extends AbstractDiskJob implements ICancelableJob {
	private File file;
	private String dir;
	private int offset, length;
	private RandomAccessFile rafile;
	private long recent;
	
	private Runnable hintRunnable = new Runnable() {
		public void run() {
			int percent = offset * 100 / file.size;
			main.setDiskOpHint(NLS.bind(disk_hint_download_file, new Object[] { file.name, String.valueOf(percent), String.valueOf(offset) }));
		}
	};
	
	/**
	 * @param f
	 * 		文件描述对象
	 * @param d
	 * 		存放目录路径
	 * @param resume
	 * 		true表示断点续传
	 */
	public DownloadFileJob(File f, String d, boolean resume) {
		file = f;
		dir = d;
		if(!dir.endsWith(java.io.File.separator))
			dir += java.io.File.separatorChar;
		
		try {
			// 检查这个文件是否已经存在
			if(resume) {
				java.io.File diskfile = new java.io.File(dir + file.name);
				if(diskfile.exists()) {
					if(diskfile.length() < file.size) {
						rafile = new RandomAccessFile(dir + file.name, "rw");
						offset = (int)diskfile.length();
						length = file.size - offset;
					}
				} else {
					rafile = new RandomAccessFile(dir + file.name, "rw");
					offset = 0;
					length = file.size;
					rafile.setLength(0);
				}
			} else {
				rafile = new RandomAccessFile(dir + file.name, "rw");
				offset = 0;
				length = file.size;
				rafile.setLength(0);
			}
		} catch(IOException e) {
			exitCode = CANNOT_OPEN_FILE;
			rafile = null;
		}
	}
	
	@Override
	protected String getSuccessHint() {
		return NLS.bind(disk_hint_download_file_success, file.name);
	}
	
	@Override
	protected String getFinishHint() {
		switch(exitCode) {
			case FILE_ABORT:
				return NLS.bind(disk_hint_download_file_abort, file.name);
			default:
				return super.getFinishHint();				
		}
	}
	
	@Override
	public void clear() {
		try {
			if(rafile != null)
				rafile.close();
		} catch(IOException e) {
		}
		super.clear();
	}
	
	@Override
	protected void onLoop() {
		long gap = System.currentTimeMillis() - recent;
		if(gap > 60000) {
			// 释放port
			main.getClient().releaseConnection(QQPort.DISK.name);
			
			// 创建一个新port
			try {
				int index = Util.random().nextInt(QQ.QQ_SERVER_DISK.length);
				conn = QQPort.DISK.create(main.getClient(), new InetSocketAddress(QQ.QQ_SERVER_DISK[index], QQ.QQ_SERVER_DISK_PORT), null, true);
			} catch(Exception e) {
				exitCode = CANNOT_CONNECT;
				exit();
				return;
			} 
			
			recent = System.currentTimeMillis();
		}
	}
	
	@Override
	protected boolean preRun() {
		if(rafile == null)
			return false;
		else {
			recent = System.currentTimeMillis();
			main.getDisplay().syncExec(new Runnable() {
				public void run() {
					main.hookCancelableJob(getThis());
					main.setFileAbortButtonStatus(true);
				}
			});
			return true;
		}
	}
	
	private DownloadFileJob getThis() {
		return this;
	}

	@Override
	protected String getHint() {
		return NLS.bind(disk_hint_download_file, new Object[] { file.name, "0", "0" });
	}

	@Override
	protected void onExit() {
		if(exitCode != SUCCESS) {
			if(exitCode == NEED_PASSWORD || exitCode == AUTH_FAIL)
				main.switchToDiskPasswordPanel();
		}
		main.hookCancelableJob(null);
		main.setFileAbortButtonStatus(false);
	}

	@Override
	protected void onAuthSuccess() {
		main.getClient().downloadFile(localIp, file.owner, file.id, offset, length);
	}
	
	@Override
	public synchronized void qqEvent(QQEvent e) {
		super.qqEvent(e);
		switch(e.type) {
			case QQEvent.QQ_DISK_DOWNLOAD_SUCCESS:
				processDiskDownloadSuccess(e);
				break;
			case QQEvent.QQ_DISK_DOWNLOAD_FRAGMENT_SUCCESS:
				processDiskDownloadFragmentSuccess(e);
				break;
			case QQEvent.QQ_DISK_OPERATION_TIMEOUT:
				switch(e.operation) {
					case QQ.QQ_DISK_CMD_DOWNLOAD:
						processOperationTimeout();
						break;
				}
				break;
		}
	}

	private void processDiskDownloadSuccess(QQEvent e) {
		DownloadReplyPacket packet = (DownloadReplyPacket)e.getSource();
		try {
			rafile.seek(offset);
			rafile.write(packet.bytes);
		} catch(IOException e1) {
			exitCode = WRITE_FILE_ERROR;
			wake();
			return;
		}
		
		offset += packet.bytes.length;
		length -= packet.bytes.length;
		if(length == 0) {
			exitCode = SUCCESS;
			wake();
		} else {
			main.getDisplay().syncExec(hintRunnable);
		}
	}

	private void processDiskDownloadFragmentSuccess(QQEvent e) {
		recent = System.currentTimeMillis();
		DiskInPacketFragment packet = (DiskInPacketFragment)e.getSource();
		try {
			rafile.seek(offset);
			rafile.write(packet.body);
		} catch(IOException e1) {
			exitCode = WRITE_FILE_ERROR;
			wake();
			return;
		}
		
		offset += packet.body.length;
		length -= packet.body.length;
		if(length == 0) {
			exitCode = SUCCESS;
			wake();
		} else {
			main.getDisplay().syncExec(hintRunnable);
		}
	}

	public synchronized void cancel(int flag) {
		exitCode = FILE_ABORT;
		wake();
	}
}
