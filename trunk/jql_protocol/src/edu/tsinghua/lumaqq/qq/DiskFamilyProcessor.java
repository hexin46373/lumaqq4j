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
package edu.tsinghua.lumaqq.qq;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.tsinghua.lumaqq.qq.events.IPacketListener;
import edu.tsinghua.lumaqq.qq.events.PacketEvent;
import edu.tsinghua.lumaqq.qq.events.QQEvent;
import edu.tsinghua.lumaqq.qq.net.IConnectionPolicy;
import edu.tsinghua.lumaqq.qq.packets.DiskInPacket;
import edu.tsinghua.lumaqq.qq.packets.ErrorPacket;
import edu.tsinghua.lumaqq.qq.packets.InPacket;
import edu.tsinghua.lumaqq.qq.packets.OutPacket;
import edu.tsinghua.lumaqq.qq.packets.in.disk.DiskInPacketFragment;
import edu.tsinghua.lumaqq.qq.packets.in.disk.ListMyDiskDirReplyPacket;
import edu.tsinghua.lumaqq.qq.packets.in.disk.ListSharedDiskDirReplyPacket;
import edu.tsinghua.lumaqq.qq.packets.in.disk.UnknownDiskInPacket;
import edu.tsinghua.lumaqq.qq.packets.out.disk.ListSharedDiskDirPacket;

/**
 * disk协议族包事件处理器
 * 
 * @author luma
 */
public class DiskFamilyProcessor implements IPacketListener {
    // Log对象
    static Log log = LogFactory.getLog(DiskFamilyProcessor.class);
    
    // QQ客户端
    private QQClient client;
    private IConnectionPolicy policy;
    
    public DiskFamilyProcessor(QQClient client) {
        this.client = client;
    }
    
	public void packetArrived(PacketEvent e) {
    	if(e.getSource() instanceof ErrorPacket) {
    		processError((ErrorPacket)e.getSource());
    		return;
    	}
    	
    	DiskInPacket in = (DiskInPacket) e.getSource();
        if(in instanceof UnknownDiskInPacket) {
            log.debug("收到一个未知格式包");
            return;
        }
        
        // 清楚重发包
        client.removeResendPacket(in);
		log.debug("Disk Family, Command: " + (int)in.getCommand() + "的确认已经收到，将不再发送");
		
		policy = client.getConnectionPolicy(in);
		if(policy == null)
			return;
		if(in instanceof DiskInPacketFragment) {
			switch(in.getCommand()) {
				case QQ.QQ_DISK_CMD_DOWNLOAD:
					processDownloadReplyFragment(in);
					break;
			}
		} else {
			switch(in.getCommand()) {
				case QQ.QQ_DISK_CMD_BEGIN_SESSION:
					processBeginSession(in);
					break;
				case QQ.QQ_DISK_CMD_AUTHENTICATE:
					processAuthenticateReply(in);
					break;
				case QQ.QQ_DISK_CMD_GET_SERVER_LIST:
					processGetServerListReply(in);
					break;
				case QQ.QQ_DISK_CMD_GET_SHARED_DISK:
					processGetSharedDiskReply(in);
					break;
				case QQ.QQ_DISK_CMD_LIST_MY_DISK_DIR:
					processListMyDiskDirReply(in);
					break;
				case QQ.QQ_DISK_CMD_LIST_SHARED_DISK_DIR:
					processListSharedDiskDirReply(in);
					break;
				case QQ.QQ_DISK_CMD_PASSWORD_OP:
					processPasswordOpReply(in);
					break;
				case QQ.QQ_DISK_CMD_DOWNLOAD:
					processDownloadReply(in);
					break;
				case QQ.QQ_DISK_CMD_RENAME:
					processRenameReply(in);
					break;
				case QQ.QQ_DISK_CMD_DELETE:
					processDeleteReply(in);
					break;
				case QQ.QQ_DISK_CMD_CREATE:
					processCreateReply(in);
					break;
				case QQ.QQ_DISK_CMD_MOVE:
					processMoveReply(in);
					break;
				case QQ.QQ_DISK_CMD_UPLOAD:
					processUploadReply(in);
					break;
				case QQ.QQ_DISK_CMD_FINALIZE:
					processFinalizeReply(in);
					break;
				case QQ.QQ_DISK_CMD_PREPARE:
					processPrepareReply(in);
					break;
				case QQ.QQ_DISK_CMD_GET_SIZE:
					processGetSizeReply(in);
					break;
				case QQ.QQ_DISK_CMD_GET_SHARE_LIST:
					processGetShareListReply(in);
					break;
				case QQ.QQ_DISK_CMD_SET_SHARE_LIST:
					processSetShareListReply(in);
					break;
				case QQ.QQ_DISK_CMD_APPLY:
					processApplyReply(in);
					break;
			}			
		}
	}

	/**
	 * 处理申请回复包
	 * 
	 * @param in
	 */
	private void processApplyReply(DiskInPacket in) {
		switch(in.replyCode) {
			case QQ.QQ_REPLY_OK:
				log.debug("申请成功");
				QQEvent e = new QQEvent(in);
				e.type = QQEvent.QQ_DISK_APPLY_SUCCESS;
				client.fireQQEvent(e);
				break;
			default:
				handleErrorReplyCode(in);
				break;
		}
	}

	/**
	 * 处理设置共享列表回复包
	 * 
	 * @param in
	 */
	private void processSetShareListReply(DiskInPacket in) {
		switch(in.replyCode) {
			case QQ.QQ_REPLY_OK:
				log.debug("设置共享列表成功");
				QQEvent e = new QQEvent(in);
				e.type = QQEvent.QQ_DISK_SET_SHARE_LIST_SUCCESS;
				client.fireQQEvent(e);
				break;
			default:
				handleErrorReplyCode(in);
				break;
		}
	}

	/**
	 * 处理得到共享列表回复包
	 * 
	 * @param in
	 */
	private void processGetShareListReply(DiskInPacket in) {
		switch(in.replyCode) {
			case QQ.QQ_REPLY_OK:
				log.debug("得到共享列表成功");
				QQEvent e = new QQEvent(in);
				e.type = QQEvent.QQ_DISK_GET_SHARE_LIST_SUCCESS;
				client.fireQQEvent(e);
				break;
			default:
				handleErrorReplyCode(in);
				break;
		}
	}

	/**
	 * 处理得到大小回复包
	 * 
	 * @param in
	 */
	private void processGetSizeReply(DiskInPacket in) {
		switch(in.replyCode) {
			case QQ.QQ_REPLY_OK:
				log.debug("得到大小成功");
				QQEvent e = new QQEvent(in);
				e.type = QQEvent.QQ_DISK_GET_SIZE_SUCCESS;
				client.fireQQEvent(e);
				break;
			default:
				handleErrorReplyCode(in);
				break;
		}
	}

	/**
	 * 处理准备上传下载回复包
	 * 
	 * @param in
	 */
	private void processPrepareReply(DiskInPacket in) {
		switch(in.replyCode) {
			case QQ.QQ_REPLY_OK:
				log.debug("准备上传下载成功");
				QQEvent e = new QQEvent(in);
				e.type = QQEvent.QQ_DISK_PREPARE_SUCCESS;
				client.fireQQEvent(e);
				break;
			default:
				handleErrorReplyCode(in);
				break;
		}
	}

	/**
	 * 处理结束上传回复包
	 *  
	 * @param in
	 */
	private void processFinalizeReply(DiskInPacket in) {
		switch(in.replyCode) {
			case QQ.QQ_REPLY_OK:
				log.debug("结束上传成功");
				QQEvent e = new QQEvent(in);
				e.type = QQEvent.QQ_DISK_FINALIZE_SUCCESS;
				client.fireQQEvent(e);
				break;
			default:
				handleErrorReplyCode(in);
				break;
		}
	}

	/**
	 * 处理上传回复包
	 * 
	 * @param in
	 */
	private void processUploadReply(DiskInPacket in) {
		switch(in.replyCode) {
			case QQ.QQ_REPLY_OK:
				log.debug("上传成功");
				QQEvent e = new QQEvent(in);
				e.type = QQEvent.QQ_DISK_UPLOAD_SUCCESS;
				client.fireQQEvent(e);
				break;
			default:
				handleErrorReplyCode(in);
				break;
		}
	}

	/**
	 * 处理移动操作回复包
	 * 
	 * @param in
	 */
	private void processMoveReply(DiskInPacket in) {
		switch(in.replyCode) {
			case QQ.QQ_REPLY_OK:
				log.debug("移动成功");
				QQEvent e = new QQEvent(policy.retrieveSent(in));
				e.type = QQEvent.QQ_DISK_MOVE_SUCCESS;
				client.fireQQEvent(e);
				break;
			default:
				handleErrorReplyCode(in);
				break;
		}
	}

	/**
	 * 处理创建目录回复包
	 * 
	 * @param in
	 */
	private void processCreateReply(DiskInPacket in) {
		switch(in.replyCode) {
			case QQ.QQ_REPLY_OK:
				log.debug("创建成功");
				QQEvent e = new QQEvent(in);
				e.type = QQEvent.QQ_DISK_CREATE_SUCCESS;
				client.fireQQEvent(e);
				break;
			default:
				handleErrorReplyCode(in);
				break;
		}
	}

	/**
	 * 处理删除回复包
	 * 
	 * @param in
	 */
	private void processDeleteReply(DiskInPacket in) {
		switch(in.replyCode) {
			case QQ.QQ_REPLY_OK:
				log.debug("删除成功");
				QQEvent e = new QQEvent(in);
				e.type = QQEvent.QQ_DISK_DELETE_SUCCESS;
				client.fireQQEvent(e);
				break;
			default:
				handleErrorReplyCode(in);
				break;
		}
	}

	/**
	 * 处理重命名回复包
	 * 
	 * @param in
	 */
	private void processRenameReply(DiskInPacket in) {
		switch(in.replyCode) {
			case QQ.QQ_REPLY_OK:
				log.debug("重命名成功");
				QQEvent e = new QQEvent(policy.retrieveSent(in));
				e.type = QQEvent.QQ_DISK_RENAME_SUCCESS;
				client.fireQQEvent(e);
				break;
			default:
				handleErrorReplyCode(in);
				break;
		}
	}

	/**
	 * 处理下载文件回复包片断
	 * 
	 * @param in
	 */
	private void processDownloadReplyFragment(DiskInPacket in) {
		log.debug("得到文件分片成功");
		QQEvent e = new QQEvent(in);
		e.type = QQEvent.QQ_DISK_DOWNLOAD_FRAGMENT_SUCCESS;
		client.fireQQEvent(e);
	}

	/**
	 * 处理下载回复包
	 * 
	 * @param in
	 */
	private void processDownloadReply(DiskInPacket in) {
		switch(in.replyCode) {
			case QQ.QQ_REPLY_OK:
				log.debug("下载文件成功");
				QQEvent e = new QQEvent(in);
				e.type = QQEvent.QQ_DISK_DOWNLOAD_SUCCESS;
				client.fireQQEvent(e);
				break;
			default:
				handleErrorReplyCode(in);
				break;
		}
	}

	/**
	 * 处理列我的网络硬盘目录回复包
	 * 
	 * @param in
	 */
	private void processListMyDiskDirReply(DiskInPacket in) {
		switch(in.replyCode) {
			case QQ.QQ_REPLY_OK:
				log.debug("得到目录列表成功");
				ListMyDiskDirReplyPacket packet = (ListMyDiskDirReplyPacket)in;
				packet.setDiskOwner(client.getUser().getQQ());
				QQEvent e = new QQEvent(in);
				e.type = QQEvent.QQ_DISK_GET_MY_DISK_DIR_SUCCESS;
				client.fireQQEvent(e);
				break;
			default:
				handleErrorReplyCode(in);
				break;
		}
	}

	/**
	 * 处理密码操作回复包
	 * 
	 * @param in
	 */
	private void processPasswordOpReply(DiskInPacket in) {
		switch(in.replyCode) {
			case QQ.QQ_REPLY_OK:
				log.debug("网络硬盘密码操作成功");
				QQEvent e = new QQEvent(policy.retrieveSent(in));
				e.type = QQEvent.QQ_DISK_PASSWORD_OP_SUCCESS;
				client.fireQQEvent(e);
				break;
			default:
				log.debug("网络硬盘密码操作失败");
				e = new QQEvent(policy.retrieveSent(in));
				e.type = QQEvent.QQ_DISK_PASSWORD_OP_FAIL;
				client.fireQQEvent(e);
				break;
		}
	}

	/**
	 * 处理列目录回复
	 * 
	 * @param in
	 */
	private void processListSharedDiskDirReply(DiskInPacket in) {
		switch(in.replyCode) {
			case QQ.QQ_REPLY_OK:
				log.debug("得到目录列表成功");
				ListSharedDiskDirPacket request = (ListSharedDiskDirPacket)policy.retrieveSent(in);
				ListSharedDiskDirReplyPacket packet = (ListSharedDiskDirReplyPacket)in;
				packet.setDiskOwner(request.getDiskOwner());
				QQEvent e = new QQEvent(packet);
				e.type = QQEvent.QQ_DISK_GET_SHARED_DISK_DIR_SUCCESS;
				client.fireQQEvent(e);
				break;
			default:
				handleErrorReplyCode(in);
				break;
		}
	}

	/**
	 * 处理得到共享网络硬盘回复
	 * 
	 * @param in
	 */
	private void processGetSharedDiskReply(DiskInPacket in) {
		switch(in.replyCode) {
			case QQ.QQ_REPLY_OK:
				log.debug("得到共享网络硬盘成功");
				QQEvent e = new QQEvent(in);
				e.type = QQEvent.QQ_DISK_GET_SHARED_DISK_SUCCESS;
				client.fireQQEvent(e);
				break;
			default:
				handleErrorReplyCode(in);
				break;
		}
	}

	/**
	 * 处理得到服务器列表回复
	 * 
	 * @param in
	 */
	private void processGetServerListReply(DiskInPacket in) {
		switch(in.replyCode) {
			case QQ.QQ_REPLY_OK:
				log.debug("得到服务器列表成功");
				QQEvent e = new QQEvent(in);
				e.type = QQEvent.QQ_DISK_GET_DISK_SERVER_LIST_SUCCESS;
				client.fireQQEvent(e);
				break;
			default:
				handleErrorReplyCode(in);
				break;
		}
	}

	/**
	 * 处理认证回复
	 * 
	 * @param in
	 */
	private void processAuthenticateReply(DiskInPacket in) {
		switch(in.replyCode) {
			case QQ.QQ_REPLY_OK:
				log.debug("网络硬盘认证成功");
				QQEvent e = new QQEvent(in);
				e.type = QQEvent.QQ_DISK_AUTHENTICATE_SUCCESS;
				client.fireQQEvent(e);
				break;
			default:
				handleErrorReplyCode(in);
				break;
		}
	}
	
	/**
	 * 处理错误码
	 * 
	 * @param in
	 */
	private void handleErrorReplyCode(DiskInPacket in) {
		switch(in.replyCode) {
			case QQ.QQ_REPLY_AUTH_FAIL:
				log.debug("网络硬盘认证失败");
				QQEvent e = new QQEvent(in);
				e.type = QQEvent.QQ_DISK_AUTHENTICATE_FAIL;
				client.fireQQEvent(e);
				break;
			default:
				log.debug("未知回复码: " + Integer.toHexString(in.replyCode).toUpperCase() + " 回复消息: " + in.replyMessage);
				e = new QQEvent(in);
				e.type = QQEvent.QQ_DISK_OPERATION_ERROR;
				client.fireQQEvent(e);
				break;
		}
	}

	/**
	 * 处理开始网络硬盘会话包
	 * 
	 * @param in
	 */
	private void processBeginSession(DiskInPacket in) {
		log.debug("服务器要求客户端开始网络硬盘会话");
		QQEvent e = new QQEvent(in);
		e.type = QQEvent.QQ_DISK_BEGIN_DISK_SESSION;
		client.fireQQEvent(e);
	}

	/**
	 * 处理错误通知包
	 * 
	 * @param error
	 */
	private void processError(ErrorPacket error) {
    	QQEvent e = null;
		switch(error.errorCode) {
			case ErrorPacket.ERROR_TIMEOUT:
				OutPacket packet = error.timeoutPacket;
				e = new QQEvent(packet);
				e.type = QQEvent.QQ_DISK_OPERATION_TIMEOUT;
				e.operation = packet.getCommand();
				break;
            case ErrorPacket.ERROR_PROXY:
            	e = new QQEvent(error);
            	e.type = QQEvent.QQ_PROXY_ERROR;
            	break;
            case ErrorPacket.ERROR_NETWORK:
            	e = new QQEvent(error);
            	e.type = QQEvent.QQ_NETWORK_ERROR;
            	break;
            case ErrorPacket.ERROR_CONNECTION_BROKEN:
            	e = new QQEvent(error);
            	e.type = QQEvent.QQ_CONNECTION_BROKEN;
            	break;
		}
		
		if(e != null)
			client.fireQQEvent(e);
	}

	public boolean accept(InPacket in) {
		return in.getFamily() == QQ.QQ_PROTOCOL_FAMILY_DISK;
	}
}
