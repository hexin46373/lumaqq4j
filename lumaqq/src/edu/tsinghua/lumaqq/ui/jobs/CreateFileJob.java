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

import org.eclipse.osgi.util.NLS;

import edu.tsinghua.lumaqq.qq.QQ;
import edu.tsinghua.lumaqq.qq.beans.File;
import edu.tsinghua.lumaqq.qq.events.QQEvent;
import edu.tsinghua.lumaqq.qq.packets.in.disk.CreateReplyPacket;
import edu.tsinghua.lumaqq.qq.packets.in.disk.FinalizeReplyPacket;
import edu.tsinghua.lumaqq.qq.packets.in.disk.GetSizeReplyPacket;
import edu.tsinghua.lumaqq.ui.helper.FileTool;

/**
 * 上传文件的任务
 *
 * @author luma
 */
public class CreateFileJob extends AbstractDiskJob implements ICancelableJob {
	private int offset;
	private int length;
	private String filename;
	private int parentId;
	private RandomAccessFile rafile;
	private int filesize;
	private boolean resume;
	
	private File file;
	
	private Runnable hintRunnable = new Runnable() {
		public void run() {
			int percent = offset * 100 / filesize;
			main.setDiskOpHint(NLS.bind(disk_hint_upload_file, new Object[] { filename, String.valueOf(percent), String.valueOf(offset) }));
		}
	};
	
	public CreateFileJob(int parentId, String path, File f) {
		this(parentId, path, 0);
		file = f;
		resume = true;
	}
	
	public CreateFileJob(int parentId, String path) {
		this(parentId, path, 0);
		resume = false;
	}
	
	public CreateFileJob(int parentId, String path, int offset) {
		this.parentId = parentId;
		this.offset = offset;
		try {
			rafile = new RandomAccessFile(path, "r");
			filesize = (int)rafile.length();
			length = filesize - offset;
			filename = FileTool.getFilename(path);
		} catch(IOException e) {
			exitCode = CANNOT_OPEN_FILE;
			rafile = null;
		}
	}
	
	@Override
	protected boolean preRun() {
		if(rafile != null && length > 0) {
			main.getDisplay().syncExec(new Runnable() {
				public void run() {
					main.hookCancelableJob(getThis());
					main.setFileAbortButtonStatus(true);
				}
			});
			return true;
		} else
			return false;
	}
	
	private CreateFileJob getThis() {
		return this;
	}
	
	@Override
	protected String getHint() {
		return NLS.bind(disk_hint_upload_file, new Object[] { filename, "0", "0" });
	}
	
	@Override
	protected String getFinishHint() {
		switch(exitCode) {
			case FILE_ABORT:
				return NLS.bind(disk_hint_upload_file_abort, filename);
			default:
				return super.getFinishHint();				
		}
	}

	@Override
	protected void onExit() {
		main.hookCancelableJob(null);
		main.setFileAbortButtonStatus(false);
		if(exitCode != SUCCESS) {
			if(exitCode == NEED_PASSWORD || exitCode == AUTH_FAIL)
				main.switchToDiskPasswordPanel();
		} 
		main.refreshDiskViewer();
	}

	@Override
	protected void onAuthSuccess() {
		if(resume)
			main.getClient().getFileSize(localIp, file.id);
		else
			main.getClient().createFile(localIp, filename, parentId, filesize);
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

	public void cancel(int flag) {
		exitCode = FILE_ABORT;
		wake();
	}
	
	@Override
	public synchronized void qqEvent(QQEvent e) {
		super.qqEvent(e);
		switch(e.type) {
			case QQEvent.QQ_DISK_CREATE_SUCCESS:
				processCreateSuccess(e);
				break;
			case QQEvent.QQ_DISK_UPLOAD_SUCCESS:
				processUploadSuccess(e);
				break;
			case QQEvent.QQ_DISK_FINALIZE_SUCCESS:
				processFinalizeSuccess(e);
				break;
			case QQEvent.QQ_DISK_PREPARE_SUCCESS:
				processPrepareSuccess(e);
				break;
			case QQEvent.QQ_DISK_GET_SIZE_SUCCESS:
				processGetSizeSuccess(e);
				break;
			case QQEvent.QQ_DISK_OPERATION_TIMEOUT:
				switch(e.operation) {
					case QQ.QQ_DISK_CMD_UPLOAD:
						processOperationTimeout();
						break;
				}
				break;
		}
	}

	private void processGetSizeSuccess(QQEvent e) {
		GetSizeReplyPacket packet = (GetSizeReplyPacket)e.getSource();
		offset = packet.size;
		length = filesize - offset;
		if(length <= 0) {
			exitCode = SUCCESS;
			wake();
		} else
			main.getClient().prepareUpload(localIp, file.id);
	}

	private void processPrepareSuccess(QQEvent e) {
		upload();
	}

	private void upload() {
		byte[] b = new byte[Math.min(length, 4096)];
		try {
			rafile.seek(offset);
			rafile.readFully(b);
		} catch(IOException ex) {
			exitCode = READ_FILE_ERROR;
			wake();
			return;
		}
		main.getClient().uploadFile(localIp, file.id, offset, b.length, b);
		offset += b.length;
		length -= b.length;
		main.getDisplay().syncExec(hintRunnable);
	}

	private void processFinalizeSuccess(QQEvent e) {
		FinalizeReplyPacket packet = (FinalizeReplyPacket)e.getSource();
		file.creationTime = packet.modifiedTime;
		file.modifiedTime = packet.modifiedTime;
		file.property &= ~QQ.QQ_DISK_FLAG_NOT_FINALIZED;
		exitCode = SUCCESS;
		wake();
	}

	private void processCreateSuccess(QQEvent e) {
		CreateReplyPacket packet = (CreateReplyPacket)e.getSource();
		file = new File();
		file.owner = main.getMyModel().qq;
		file.id = packet.id;
		file.dirId = parentId;
		file.name = filename;
		file.size = filesize;
		file.property = QQ.QQ_DISK_FLAG_NOT_FINALIZED;
		main.getDiskManager().addFile(file);
		main.getClient().prepareUpload(localIp, file.id);
	}

	private void processUploadSuccess(QQEvent e) {
		if(length <= 0)
			main.getClient().finalizeUpload(localIp, file.id);
		else
			upload();
	}
}
