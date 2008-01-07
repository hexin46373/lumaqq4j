/*
* LumaQQ - Java QQ Client
*
* Copyright (C) 2004 luma <stubma@163.com>
*                    whg2001 <whg_2001@sohu.com>
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
package edu.tsinghua.lumaqq.ui;

import static edu.tsinghua.lumaqq.resource.Messages.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

import edu.tsinghua.lumaqq.models.Cluster;
import edu.tsinghua.lumaqq.models.ClusterType;
import edu.tsinghua.lumaqq.models.Group;
import edu.tsinghua.lumaqq.models.ModelRegistry;
import edu.tsinghua.lumaqq.models.ModelUtils;
import edu.tsinghua.lumaqq.models.Organization;
import edu.tsinghua.lumaqq.models.Status;
import edu.tsinghua.lumaqq.models.User;
import edu.tsinghua.lumaqq.qq.QQ;
import edu.tsinghua.lumaqq.qq.QQPort;
import edu.tsinghua.lumaqq.qq.beans.CardStub;
import edu.tsinghua.lumaqq.qq.beans.ContactInfo;
import edu.tsinghua.lumaqq.qq.beans.FriendOnlineEntry;
import edu.tsinghua.lumaqq.qq.beans.Member;
import edu.tsinghua.lumaqq.qq.beans.QQFriend;
import edu.tsinghua.lumaqq.qq.beans.QQLive;
import edu.tsinghua.lumaqq.qq.beans.QQOrganization;
import edu.tsinghua.lumaqq.qq.beans.QQUser;
import edu.tsinghua.lumaqq.qq.beans.SMSReply;
import edu.tsinghua.lumaqq.qq.beans.SimpleClusterInfo;
import edu.tsinghua.lumaqq.qq.events.IQQListener;
import edu.tsinghua.lumaqq.qq.events.QQEvent;
import edu.tsinghua.lumaqq.qq.packets.ErrorPacket;
import edu.tsinghua.lumaqq.qq.packets.in.AddFriendExReplyPacket;
import edu.tsinghua.lumaqq.qq.packets.in.ClusterCommandReplyPacket;
import edu.tsinghua.lumaqq.qq.packets.in.FriendChangeStatusPacket;
import edu.tsinghua.lumaqq.qq.packets.in.GetOnlineOpReplyPacket;
import edu.tsinghua.lumaqq.qq.packets.in.GetUserInfoReplyPacket;
import edu.tsinghua.lumaqq.qq.packets.in.KeepAliveReplyPacket;
import edu.tsinghua.lumaqq.qq.packets.in.ReceiveIMPacket;
import edu.tsinghua.lumaqq.qq.packets.in.RequestKeyReplyPacket;
import edu.tsinghua.lumaqq.qq.packets.in.SendSMSReplyPacket;
import edu.tsinghua.lumaqq.qq.packets.in.SystemNotificationPacket;
import edu.tsinghua.lumaqq.qq.packets.out.DeleteFriendPacket;
import edu.tsinghua.lumaqq.qq.packets.out.RequestKeyPacket;
import edu.tsinghua.lumaqq.qq.packets.out.SendIMPacket;
import edu.tsinghua.lumaqq.ui.config.cluster.ClusterInfoWindow;
import edu.tsinghua.lumaqq.ui.config.user.UserInfoWindow;
import edu.tsinghua.lumaqq.ui.helper.CheckUpdateThread;
import edu.tsinghua.lumaqq.ui.helper.ShellRegistry;
import edu.tsinghua.lumaqq.ui.jobs.BackgroundJobExecutor;
import edu.tsinghua.lumaqq.ui.jobs.DownloadCustomHeadJob;
import edu.tsinghua.lumaqq.ui.jobs.DownloadFriendRemarkJob;
import edu.tsinghua.lumaqq.ui.jobs.DownloadGroupJob;
import edu.tsinghua.lumaqq.ui.jobs.DownloadSignatureJob;
import edu.tsinghua.lumaqq.ui.jobs.GetCustomHeadInfoJob;
import edu.tsinghua.lumaqq.ui.jobs.GetFriendLevelJob;
import edu.tsinghua.lumaqq.ui.jobs.GetFriendListJob;
import edu.tsinghua.lumaqq.ui.jobs.GetUserPropertyJob;
import edu.tsinghua.lumaqq.ui.jobs.IExecutor;
import edu.tsinghua.lumaqq.ui.jobs.IJob;

/**
 * QQ事件处理器
 * 
 * @author luma 王华刚
 */
class QQEventProcessor implements IQQListener {
    // Log对象
    static Log log = LogFactory.getLog(QQEventProcessor.class);
    // 主窗口对象
	private MainShell main;
	
	// 临时哈希表
	private Map<User, User> tempHash;
	
	// 登录重试次数，我这里处理最多重试6次
	// 如果第一次登录失败，则随机选择服务器重试3次
	// 如果依然失败，再随机选择服务器，并且强制使用tcp方式，再重试3次
	// 如果依然不行，报登录超时错误
	private int retryCount;
    
	/**
	 * 构造函数
	 */
	public QQEventProcessor(MainShell main) {
		this.main = main;
		tempHash = new HashMap<User, User>();
		retryCount = 0;
	}
	
	/**
	 * 重置变量状态
	 */
	public void clear() {
		main.getCurrentOnlines().clear();
		tempHash.clear();
		main.getTipHelper().setFirstTime(true);
	}
	
	/* (non-Javadoc)
	 * @see edu.tsinghua.lumaqq.qq.events.QQListener#qqEvent(edu.tsinghua.lumaqq.qq.events.QQEvent)
	 */
	public void qqEvent(QQEvent e) {
		switch(e.type) {
		    case QQEvent.QQ_GET_LOGIN_TOKEN_FAIL:
		        processLoginTimeout();
		    	break;
			case QQEvent.QQ_LOGIN_SUCCESS:
				processLoginSuccess();
				break;	
			case QQEvent.QQ_GET_FRIEND_ONLINE_SUCCESS:
				processGetFriendOnlineSuccess(e);
				break;
			case QQEvent.QQ_CHANGE_STATUS_SUCCESS:
				processChangeStatusSuccess();
				break;
			case QQEvent.QQ_CHANGE_STATUS_FAIL:
				processChangeStatusFail();
				break;
			case QQEvent.QQ_FRIEND_CHANGE_STATUS:
				processFriendChangeStatus(e);
				break;
			case QQEvent.QQ_RECEIVE_NORMAL_IM:
				processReceiveNormalIM(e);
				break;
		    case QQEvent.QQ_UNKNOWN_TYPE_IM:
		        processUnknownTypeIM(e);
		    	break;
			case QQEvent.QQ_REQUEST_SEND_FILE:
				processRequestSendFile(e);
				break;
			case QQEvent.QQ_KICKED_OUT_BY_SYSTEM:
				processKickedOutBySystem();
				break;
			case QQEvent.QQ_ADDED_BY_OTHERS:
			case QQEvent.QQ_ADDED_BY_OTHERS_EX:
			case QQEvent.QQ_REQUEST_ADD_ME:
			case QQEvent.QQ_REQUEST_ADD_ME_EX:
			case QQEvent.QQ_REQUEST_ADD_OTHER_APPROVED:
			case QQEvent.QQ_REQUEST_ADD_OTHER_REJECTED:
			case QQEvent.QQ_REQUEST_ADD_OTHER_APPROVED_AND_ADD:
				processReceiveSystemNotification(e);
				break;
			case QQEvent.QQ_LOGIN_FAIL:
				processLoginFail(e);
				break;
			case QQEvent.QQ_LOGIN_UNKNOWN_ERROR:
				processLoginUnknownError();
				break;
			case QQEvent.QQ_LOGIN_REDIRECT_NULL:
			    processLoginRedirectNull();
				break;
			case QQEvent.QQ_ADD_FRIEND_SUCCESS:
			case QQEvent.QQ_ADD_FRIEND_ALREADY:
				processAddFriendSuccess(e);
				break;
			case QQEvent.QQ_DELETE_FRIEND_SUCCESS:
			case QQEvent.QQ_DELETE_FRIEND_FAIL:
				processDeleteFriendSuccess(e);
				break;
			/*case QQEvent.QQ_DELETE_FRIEND_FAIL:
				processDeleteFriendFail(e);
				break;*/
			case QQEvent.QQ_KEEP_ALIVE_SUCCESS:
				processKeepAliveSuccess(e);
				break;
			case QQEvent.QQ_CREATE_CLUSTER_SUCCESS:
				processClusterCreateSuccess(e);
				break;
			case QQEvent.QQ_CREATE_TEMP_CLUSTER_SUCCESS:
			    processClusterCreateTempClusterSuccess(e);
				break;
			case QQEvent.QQ_GET_MEMBER_INFO_SUCCESS:
				processClusterGetMemberInfoSuccess(e);
				break;
			case QQEvent.QQ_GET_ONLINE_MEMBER_SUCCESS:
			    processClusterGetOnlineMemberSuccess(e);	
				break;
			case QQEvent.QQ_EXIT_CLUSTER_SUCCESS:
			case QQEvent.QQ_EXIT_TEMP_CLUSTER_SUCCESS:
			case QQEvent.QQ_DISMISS_CLUSTER_SUCCESS:
				processClusterExitClusterSuccess(e);
				break;
			case QQEvent.QQ_RECEIVE_SMS:
				processReceiveSMS(e);
				break;
			case QQEvent.QQ_RECEIVE_DISK_NOTIFICATION:
				processReceiveDiskNotification(e);
				break;
			case QQEvent.QQ_RECEIVE_CLUSTER_IM:
			case QQEvent.QQ_RECEIVE_TEMP_CLUSTER_IM:
			case QQEvent.QQ_RECEIVE_UNKNOWN_CLUSTER_IM:
				processReceiveClusterIM(e);
				break;
			case QQEvent.QQ_RECEIVE_TEMP_SESSION_IM:
				processReceiveTempSessionIM(e);
				break;
			case QQEvent.QQ_I_AM_ADDED_TO_CLUSTER:
				processIAmAddedToCluster(e);
				break;
			case QQEvent.QQ_ADMIN_ENTITLED:
				processAdminEntitled(e);
				break;
			case QQEvent.QQ_ADMIN_WITHDRAWED:
				processAdminWithdrawed(e);
				break;
			case QQEvent.QQ_SET_ROLE_SUCCESS:
				processSetRoleSuccess(e);
				break;
			case QQEvent.QQ_TRANSFER_ROLE_SUCCESS:
				processTransferRoleSuccess(e);
				break;
			case QQEvent.QQ_SET_ROLE_FAIL:
			case QQEvent.QQ_TRANSFER_ROLE_FAIL:
				processRoleOpFail(e);
				break;
			case QQEvent.QQ_I_AM_REMOVED_FROM_CLUSTER:
				processIAmRemovedFromCluster(e);
				break;
			case QQEvent.QQ_REQUEST_JOIN_CLUSTER:
				processRequestJoinCluster(e);
				break;
			case QQEvent.QQ_REQUEST_JOIN_CLUSTER_APPROVED:
				processRequestJoinClusterApproved(e);
				break;
			case QQEvent.QQ_REQUEST_JOIN_CLUSTER_REJECTED:
				processRequestJoinClusterRejected(e);
				break;
			case QQEvent.QQ_OPERATION_TIMEOUT:
			    switch(e.operation) {
			        case QQ.QQ_CMD_LOGIN:
			        case QQ.QQ_CMD_REQUEST_LOGIN_TOKEN:
						processLoginTimeout();
			        	break;
			    }
				break;
			case QQEvent.QQ_CONNECTION_LOST:
				processLoginTimeout();
				break;
		    case QQEvent.QQ_PROXY_ERROR:
		        processProxyError(e);
		    	break;
		    case QQEvent.QQ_NETWORK_ERROR:
		        processNetworkError(e);
		    	break;
		    case QQEvent.QQ_RUNTIME_ERROR:
		    	processRuntimeError(e);
		    	break;
		    case QQEvent.QQ_REQUEST_KEY_SUCCESS:
		        processRequestKeySuccess(e);
		    	break;
		    case QQEvent.QQ_REQUEST_KEY_FAIL:
		        processRequestKeyFail(e);
		    	break;
			case QQEvent.QQ_GET_CLUSTER_INFO_SUCCESS:
				processClusterGetInfoSuccess(e);
				break;
			case QQEvent.QQ_GET_TEMP_CLUSTER_INFO_SUCCESS:
			    processClusterGetTempClusterInfoSuccess(e);
				break;
			case QQEvent.QQ_GET_CLUSTER_INFO_FAIL:
			case QQEvent.QQ_GET_MEMBER_INFO_FAIL:
			case QQEvent.QQ_GET_ONLINE_MEMBER_FAIL:
			case QQEvent.QQ_GET_TEMP_CLUSTER_INFO_FAIL:
			    processClusterCommandFail(e);
				break;
			case QQEvent.QQ_SEND_TEMP_CLUSTER_IM_FAIL:
			case QQEvent.QQ_SEND_CLUSTER_IM_EX_FAIL:
			    processSendClusterIMFail(e);
				break;
			case QQEvent.QQ_SEND_IM_SUCCESS:
				processSendIMSucess(e);
				break;
			case QQEvent.QQ_SMS_SENT:
				processSMSSent(e);
				break;
			case QQEvent.QQ_GET_USER_INFO_SUCCESS:
				processGetUserInfoSuccess(e);
				break;
			case QQEvent.QQ_SUB_CLUSTER_OP_SUCCESS:
				processClusterSubClusterOpSuccess(e);
				break;
			case QQEvent.QQ_UPDATE_ORGANIZATION_SUCCESS:
				processClusterUpdateOrganizationSuccess(e);
				break;
			case QQEvent.QQ_GET_CARD_BATCH_SUCCESS:
				processClusterGetCardBatchSuccess(e);
				break;
			case QQEvent.QQ_FRIEND_SIGNATURE_CHANGED:
				processFriendSignatureChanged(e);
				break;
			case QQEvent.QQ_FRIEND_PROPERTY_CHANGED:
				processFriendPropertyChanged(e);
				break;
			case QQEvent.QQ_FRIEND_CUSTOM_HEAD_CHANGED:
				processFriendCustomHeadChanged(e);
				break;
		}
	}

	/**
	 * 处理运行时错误
	 * 
	 * @param e
	 */
	private void processRuntimeError(final QQEvent e) {
		main.getDisplay().syncExec(new Runnable() {
			public void run() {
				main.handleRuntimeError(e);
			}				
		});	
	}

	/**
	 * 处理收到网络硬盘通知事件
	 * 
	 * @param e
	 */
	private void processReceiveDiskNotification(QQEvent e) {
		ReceiveIMPacket packet = (ReceiveIMPacket)e.getSource();
		final QQLive live = packet.qqlive;
		main.getDisplay().syncExec(new Runnable() {
			public void run() {
				DiskNotificationTipWindow win = new DiskNotificationTipWindow(main, live);
				win.open();
			}
		});
	}

	/**
	 * 处理好友自定义头像改变事件
	 * 
	 * @param e
	 */
	private void processFriendCustomHeadChanged(QQEvent e) {
		ReceiveIMPacket packet = (ReceiveIMPacket)e.getSource();
		main.getDiskJobQueue().addJob(new DownloadCustomHeadJob(packet.headChanges));
	}

	/**
	 * 处理好友属性改变事件
	 * 
	 * @param e
	 */
	private void processFriendPropertyChanged(QQEvent e) {
		ReceiveIMPacket packet = (ReceiveIMPacket)e.getSource();
		if(!packet.propertyChange.hasCustomHead()) {
			final User u = ModelRegistry.getUser(packet.propertyChange.qq);
			if(u == null)
				return;
			u.hasCustomHead = false;
			if(u.qq == main.getMyModel().qq)
				main.getMyModel().hasCustomHead = false;
			main.getDisplay().syncExec(new Runnable() {
				public void run() {
					main.getBlindHelper().refreshGroup(u.group);
				}
			});
		}
	}

	/**
	 * 处理手机短信发送事件
	 * 
	 * @param e
	 */
	private void processSMSSent(QQEvent e) {
		// 把发送成功的接收者添加到最近联系人中
		SendSMSReplyPacket packet = (SendSMSReplyPacket)e.getSource();
		for(SMSReply reply : packet.replies) {
			if(reply.replyCode == QQ.QQ_REPLY_SMS_OK) {
				if(reply.isQQ) {
					final User u = ModelRegistry.getUser(reply.qq);
					if(u != null) {
						main.getDisplay().syncExec(new Runnable() {
							public void run() {
								main.getBlindHelper().updateLatest(u);
							}
						});
						return;
					}
				}				
			}
		}
	}

	/**
	 * 处理普通消息发送成功事件
	 * 
	 * @param e
	 */
	private void processSendIMSucess(QQEvent e) {
		// 把接受者添加到最近联系人中
		SendIMPacket packet = (SendIMPacket)e.getSource();
		final User f = ModelRegistry.getUser(packet.getReceiver());
		if(f == null)
			return;
		main.getDisplay().syncExec(new Runnable() {
			public void run() {
				main.getBlindHelper().updateLatest(f);
			}
		});
	}

	/**
	 * 处理收到临时会话消息
	 * 
	 * @param e
	 */
	private void processReceiveTempSessionIM(QQEvent e) {
		final ReceiveIMPacket packet = (ReceiveIMPacket)e.getSource();
		main.getDisplay().syncExec(new Runnable() {
			public void run() {
			    main.getMessageHelper().putTempSessionIM(packet);
			}
		});
	}

	/**
	 * 处理收到短信
	 * 
	 * @param e
	 */
	private void processReceiveSMS(QQEvent e) {
		final ReceiveIMPacket packet = (ReceiveIMPacket)e.getSource();
		main.getDisplay().syncExec(new Runnable() {
			public void run() {
			    main.getMessageHelper().putSMS(packet);
			}
		});
	}

	/**
	 * 处理好友个性签名改变事件
	 * 
	 * @param e
	 */
	private void processFriendSignatureChanged(QQEvent e) {
		ReceiveIMPacket packet = (ReceiveIMPacket)e.getSource();
		final User u = ModelRegistry.getUser(packet.signatureOwner);
		if(u == null)
			return;
		
		u.signature = packet.signature;
		u.signatureModifiedTime = packet.modifiedTime;
		u.hasSignature = true;
		if(u.qq == main.getMyModel().qq)
			main.getMyModel().signature = u.signature;
		main.getDisplay().syncExec(new Runnable() {
			public void run() {
				main.getBlindHelper().refreshGroup(u.group);
			}
		});
	}

	/**
	 * 处理转让身份成功事件
	 * 
	 * @param e
	 */
	private void processTransferRoleSuccess(QQEvent e) {
		final ClusterCommandReplyPacket packet = (ClusterCommandReplyPacket)e.getSource();
		final Cluster c = ModelRegistry.getCluster(packet.clusterId);
		if(c == null)
			return;
		c.creator = packet.memberQQ;
		main.getDisplay().syncExec(new Runnable() {
			public void run() {
				boolean hasInfo = main.getShellRegistry().hasClusterInfoWindow(c); 
				// 如果存在这个群的资料窗口，则刷新数据
				if(hasInfo) {
					ClusterInfoWindow cis = main.getShellRegistry().getClusterInfoWindow(c);
					cis.setClusterModel(c);
				}
				// 如果存在这个群的信息窗口，则刷新数据
				boolean hasIM = !main.getOptionHelper().isUseTabIMWindow() && main.getShellRegistry().hasSendClusterIMWindow(c);
				if(hasIM)
				    main.getShellRegistry().getSendClusterIMWindow(c).refreshClusterInfo();
				// 如果标签页窗口是打开的
				boolean hasTabIM = main.getOptionHelper().isUseTabIMWindow() && main.getShellRegistry().isSendIMTabWindowOpened(); 
				if(hasTabIM) {
					SendIMTabWindow sitw = main.getShellRegistry().getSendIMTabWindow();
					sitw.refreshTab(c);						
				}
				
				if(hasIM || hasTabIM || hasInfo) {
					MessageDialog.openInformation(main.getShell(), 
							message_box_common_info_title,
							message_box_role_transferred);					
				}
			}
		});
	}

	/**
	 * 处理设置角色失败事件
	 * 
	 * @param e
	 */
	private void processRoleOpFail(QQEvent e) {
		final ClusterCommandReplyPacket packet = (ClusterCommandReplyPacket)e.getSource();
		final Cluster c = ModelRegistry.getCluster(packet.clusterId);
		if(c == null)
			return;
		boolean hasInfo = main.getShellRegistry().hasClusterInfoWindow(c); 
		boolean hasIM = !main.getOptionHelper().isUseTabIMWindow() && main.getShellRegistry().hasSendClusterIMWindow(c);
		boolean hasTabIM = main.getOptionHelper().isUseTabIMWindow() && main.getShellRegistry().isSendIMTabWindowOpened(); 
		if(hasIM || hasTabIM || hasInfo) {
			main.getDisplay().syncExec(new Runnable() {
				public void run() {
					MessageDialog.openError(main.getShell(), 
							message_box_common_fail_title,
							packet.errorMessage);
				}
			});
		}
	}

	/**
	 * 处理设置角色成功事件
	 * 
	 * @param e
	 */
	private void processSetRoleSuccess(QQEvent e) {
		final ClusterCommandReplyPacket packet = (ClusterCommandReplyPacket)e.getSource();
		final Cluster c = ModelRegistry.getCluster(packet.clusterId);
		if((packet.role & QQ.QQ_ROLE_ADMIN) != 0)
			c.addAdmin(packet.memberQQ);
		else
			c.removeAdmin(packet.memberQQ);
		
		main.getDisplay().syncExec(new Runnable() {
			public void run() {
				// 刷新所有群相关窗口，显示提示框				
				if(refreshClusterRelatedWindow(c)) {
					if((packet.role & QQ.QQ_ROLE_ADMIN) != 0)
						MessageDialog.openInformation(main.getShell(), 
								message_box_common_info_title,
								message_box_admin_set);
					else
						MessageDialog.openInformation(main.getShell(), 
								message_box_common_info_title,
								message_box_admin_unset);					
				}
			}
		});
	}

	/**
	 * 刷新所有群相关窗口
	 * 
	 * @param c
	 * 		Cluster
	 * @return
	 * 		如果群的某些窗口是打开的，返回true
	 */
	protected boolean refreshClusterRelatedWindow(Cluster c) {
		boolean hasInfo = main.getShellRegistry().hasClusterInfoWindow(c); 
		// 如果存在这个群的资料窗口，则刷新数据
		if(hasInfo) {
			ClusterInfoWindow cis = main.getShellRegistry().getClusterInfoWindow(c);
			cis.setClusterModel(c);
		}
		// 如果存在这个群的信息窗口，则刷新数据
		boolean hasIM = !main.getOptionHelper().isUseTabIMWindow() && main.getShellRegistry().hasSendClusterIMWindow(c);
		if(hasIM)
		    main.getShellRegistry().getSendClusterIMWindow(c).refreshClusterInfo();
		// 如果标签页窗口是打开的
		boolean hasTabIM = main.getOptionHelper().isUseTabIMWindow() && main.getShellRegistry().isSendIMTabWindowOpened(); 
		if(hasTabIM) {
			SendIMTabWindow sitw = main.getShellRegistry().getSendIMTabWindow();
			sitw.refreshTab(c);						
		}
		
		return hasIM || hasTabIM || hasInfo;
	}

	/**
     * 处理批量得到群名片成功事件 
     * 
     * @param e
     */
    private void processClusterGetCardBatchSuccess(QQEvent e) {
    	ClusterCommandReplyPacket packet = (ClusterCommandReplyPacket)e.getSource();
    	final Cluster c = ModelRegistry.getCluster(packet.clusterId);
    	if(c == null)
    		return;
    	if(c.cardUpdated)
    		return;
    	
    	// 保存群名片名称
    	for(CardStub stub : packet.cardStubs) {
    		User m = c.getMember(stub.qq);
    		if(m == null)
    			continue;
    		m.cardName = stub.name;
    		if(m.hasCardName())
    			m.displayName = m.cardName;
    	}
    	
    	// 如果还有更多名片，继续请求
    	if(packet.nextStart != 0) 
    		main.getClient().getCardBatch(c.clusterId, packet.nextStart);
    	else {
    		c.cardUpdated = true;
    		main.getDisplay().syncExec(new Runnable() {
    			public void run() {
    				main.getBlindHelper().refreshGroup(c.group);
    			}
    		});
    	}
	}

	/**
     * 处理更新组织架构成功事件
     * 
     * @param e
     */
    private void processClusterUpdateOrganizationSuccess(QQEvent e) {
    	ClusterCommandReplyPacket packet = (ClusterCommandReplyPacket)e.getSource();
    	Cluster c = ModelRegistry.getCluster(packet.clusterId);
    	c.removeAllOrganizations();
    	for(QQOrganization qqOrg : packet.organizations) {
    		Organization org = new Organization();
    		org.id = qqOrg.id;
    		org.path = qqOrg.path;
    		org.name = qqOrg.name;
    		c.addOrganization(org);
    	}
    	main.getClient().getClusterInfo(c.clusterId);
	}

	/**
     * 处理子群操作成功事件
     * 
     * @param e
     */
    private void processClusterSubClusterOpSuccess(QQEvent e) {
    	ClusterCommandReplyPacket packet = (ClusterCommandReplyPacket)e.getSource();
    	switch(packet.subClusterOpByte) {
    		case QQ.QQ_CLUSTER_SUB_CMD_GET_DIALOG_LIST:
    			final Cluster dialogContainer = ModelRegistry.getDialogContainer();
    			if(dialogContainer != null) {
    				dialogContainer.removeAllSubClusters();
    				for(SimpleClusterInfo info : packet.subClusters) {
    					Cluster c = new Cluster();
    					c.clusterType = ClusterType.DIALOG;
    					c.clusterId = info.id;
    					c.name = info.name;    					
    					dialogContainer.addSubCluster(c);
    					ModelRegistry.addCluster(c);
    					main.getClient().getTempClusterInfo(c.clusterType.toQQConstant(), c.clusterId, c.parentClusterId);
    				}
    				main.getDisplay().syncExec(new Runnable() {
    					public void run() {
    						main.getBlindHelper().refreshGroup(dialogContainer.group);    						
    					}
    				});
    			}
    			break;
    		case QQ.QQ_CLUSTER_SUB_CMD_GET_SUBJECT_LIST:
    			final Cluster parent = ModelRegistry.getCluster(packet.clusterId);
    			if(parent != null) {
    				parent.removeAllSubClusters();
    				for(SimpleClusterInfo info : packet.subClusters) {
    					Cluster c = new Cluster();
    					c.clusterType = ClusterType.SUBJECT;    					
    					c.clusterId = info.id;
    					c.name = info.name;
    					parent.addSubCluster(c);
    					ModelRegistry.addCluster(c);
    					main.getClient().getTempClusterInfo(c.clusterType.toQQConstant(), c.clusterId, c.parentClusterId);    					
    				}
    				main.getDisplay().syncExec(new Runnable() {
    					public void run() {
    		   				main.getBlindHelper().refreshGroup(parent.group);  
    					}
    				}); 
    			}
    			break;
    	}
	}

	/**
     * 处理创建临时群成功事件
     * 
     * @param e
     */
    private void processClusterCreateTempClusterSuccess(QQEvent e) {
		final ClusterCommandReplyPacket packet = (ClusterCommandReplyPacket)e.getSource();
		main.getDisplay().syncExec(new Runnable() {
			public void run() {
			    Cluster c = main.getBlindHelper().addTempCluster(packet.type, packet.clusterId, packet.parentClusterId);
			    if(c != null && main.getUIHelper().hasPromotingDiscuss(packet.getSequence())) {
			    	main.getUIHelper().removePromotingDiscuss(packet.getSequence());
			    	main.getShellLauncher().openClusterIMWindow(c);
			    }
			}
		});
    }

    /**
     * 处理重定向到0地址事件
     */
    private void processLoginRedirectNull() {
		main.getDisplay().syncExec(new Runnable() {
			public void run() {
			    // 自动随机更换其他服务器登录
			    main.logout();
			    main.checkLogin(true, false);
			}				
		});
    }

    /**
     * 初始网络错误
     * 
     * @param e
     */
    private void processNetworkError(final QQEvent e) {
    	final ErrorPacket packet = (ErrorPacket)e.getSource();
    	if(!packet.connectionId.equals(QQPort.MAIN.name))
    		return;
    	
        if(main.getClient().getUser().getStatus() != QQ.QQ_STATUS_OFFLINE) {
	        main.getDisplay().syncExec(new Runnable() {
	            public void run() {
					main.restartLogin(e);
	            }
	        });            
        }
    }

    /**
	 * 处理请求密钥失败事件
     * @param e
     */
    private void processRequestKeyFail(QQEvent e) {
        RequestKeyPacket packet = (RequestKeyPacket)e.getSource();
        main.getClient().requestKey(packet.getRequest());
    }

    /**
	 * 处理请求密钥成功事件
	 * 
     * @param e
     * 		QQEvent
     */
    private void processRequestKeySuccess(QQEvent e) {
        RequestKeyReplyPacket packet = (RequestKeyReplyPacket)e.getSource();
        QQUser me = main.getClient().getUser();
        switch(packet.subCommand) {
        	case QQ.QQ_SUB_CMD_REQUEST_FILE_AGENT_KEY:
        		me.setFileAgentKey(packet.key);
        		me.setFileAgentToken(packet.token);
        		break;
        	case QQ.QQ_SUB_CMD_REQUEST_UNKNOWN03_KEY:
        		me.setUnknown03Key(packet.key);
        		me.setUnknown03Token(packet.token);
        		break;
        	case QQ.QQ_SUB_CMD_REQUEST_UNKNOWN06_KEY:
        		me.setUnknown06Key(packet.key);
        		me.setUnknown06Token(packet.token);
        		break;
        	case QQ.QQ_SUB_CMD_REQUEST_UNKNOWN07_KEY:
        		me.setUnknown07Key(packet.key);
        		me.setUnknown07Token(packet.token);
        		break;
        	case QQ.QQ_SUB_CMD_REQUEST_UNKNOWN08_KEY:
        		me.setUnknown08Key(packet.key);
        		me.setUnknown08Token(packet.token);
        		break;
        }
    }

    /**
	 * 处理代理出错事件
	 * 
     * @param e
     * 			QQEvent
     */
    private void processProxyError(QQEvent e) {
    	final ErrorPacket packet = (ErrorPacket)e.getSource();
    	if(!packet.connectionId.equals(QQPort.MAIN.name))
    		return;
    	
        if(main.getClient().getUser().getStatus() != QQ.QQ_STATUS_OFFLINE) {
	        main.getDisplay().syncExec(new Runnable() {
	            public void run() {
	                main.stopWaitingPanelAnimation();
	                main.getClient().getUser().setStatus(QQ.QQ_STATUS_OFFLINE);
	                main.logout();
	                MessageDialog.openError(main.getShell(), message_box_proxy_error_title, packet.errorMessage);
	            }
	        });            
        }
    }

    /**
	 * 处理未知IM事件，这个目前用来自动拒绝TCP方式的文件传输，等到TCP实现时，不再需要
	 * 
     * @param e
     * 		QQEvent
     */
    private void processUnknownTypeIM(QQEvent e) {
        ReceiveIMPacket packet = (ReceiveIMPacket)e.getSource();
        if(packet.header.type == QQ.QQ_RECV_IM_FRIEND || packet.header.type == QQ.QQ_RECV_IM_STRANGER) {
            if(packet.normalHeader.type == QQ.QQ_IM_TYPE_TCP_REQUEST)
                main.getClient().rejectSendFile(packet.normalHeader.sender, packet.normalHeader.sequence, false);
        }
    }

	/**
	 * 处理对方拒绝我加入群的事件
	 * 
	 * @param e
	 * 		QQEvent
	 */
	private void processRequestJoinClusterRejected(QQEvent e) {
		final ReceiveIMPacket packet = (ReceiveIMPacket)e.getSource();
		main.getDisplay().syncExec(new Runnable() {
			public void run() {
				main.getMessageHelper().putClusterNotification(packet);
			}				
		});
	}

	/**
	 * 处理对方同意我加入群的事件
	 * 
	 * @param e
	 * 			QQEvent
	 */
	private void processRequestJoinClusterApproved(QQEvent e) {
		final ReceiveIMPacket packet = (ReceiveIMPacket)e.getSource();
		Cluster c = ModelRegistry.getCluster(packet.header.sender);
		if(c == null) {
			main.getDisplay().syncExec(new Runnable() {
				public void run() {
				    main.getBlindHelper().addCluster(packet.header.sender, false);
					main.getMessageHelper().putClusterNotification(packet);
				}				
			});
		}
	}

	/**
	 * 处理别人申请加入群的事件
	 * 
	 * @param e
	 * 			QQEvent
	 */
	private void processRequestJoinCluster(QQEvent e) {
		final ReceiveIMPacket packet = (ReceiveIMPacket)e.getSource();
		Cluster c = ModelRegistry.getCluster(packet.header.sender);
		if(c != null) {
			main.getDisplay().syncExec(new Runnable() {
				public void run() {
				    main.getMessageHelper().putClusterNotification(packet);
				}				
			});
		}
	}

	/**
	 * 处理我被从群中删除事件
	 * 
	 * @param e
	 * 		QQEvent
	 */
	private void processIAmRemovedFromCluster(QQEvent e) {
		final ReceiveIMPacket packet = (ReceiveIMPacket)e.getSource();
		Cluster c = ModelRegistry.getCluster(packet.header.sender);
		if(c != null) {
			main.getDisplay().syncExec(new Runnable() {
				public void run() {
				    // 如果sender等于自己的QQ号，则表示管理员把自己删除了，如果不是，
				    // 表示这个sender主动退出该群
				    if(packet.sender == main.getMyModel().qq)
				        main.getBlindHelper().removeCluster(packet.header.sender);
				    // 推入这个群通知
					main.getMessageHelper().putClusterNotification(packet);
				}				
			});
		}
	}
	
    /**
     * 处理我被赋予管理员身份事件
     * 
     * @param e
     */
    private void processAdminEntitled(QQEvent e) {
		final ReceiveIMPacket packet = (ReceiveIMPacket)e.getSource();
		final Cluster c = ModelRegistry.getCluster(packet.header.sender);
		if(c != null) {
			// 修改相关数据
			c.addAdmin(packet.memberQQ);
			
			main.getDisplay().syncExec(new Runnable() {
				public void run() {
				    // 如果接收者是我，表示我被设为管理员，推入这个群通知
					if(packet.memberQQ == main.getMyModel().qq)
						main.getMessageHelper().putClusterNotification(packet);
					// 刷新群窗口
					refreshClusterRelatedWindow(c);
				}
			});
		}
	}    

	/**
	 * 处理我被取消管理员身份事件
	 * 
	 * @param e
	 */
	private void processAdminWithdrawed(QQEvent e) {
		final ReceiveIMPacket packet = (ReceiveIMPacket)e.getSource();
		final Cluster c = ModelRegistry.getCluster(packet.header.sender);
		if(c != null) {
			// 修改相关数据
			c.removeAdmin(packet.memberQQ);
			
			main.getDisplay().syncExec(new Runnable() {
				public void run() {
				    // 如果接收者是我，表示我被取消管理员，推入这个群通知
					if(packet.memberQQ == main.getMyModel().qq)
						main.getMessageHelper().putClusterNotification(packet);
					// 刷新群窗口
					refreshClusterRelatedWindow(c);
				}
			});
		}
	}

	/**
	 * 处理我被其他人添加到某个群的事件
	 * 
	 * @param e
	 * 			QQEvent
	 */
	private void processIAmAddedToCluster(QQEvent e) {
		final ReceiveIMPacket packet = (ReceiveIMPacket)e.getSource();
		Cluster c = ModelRegistry.getCluster(packet.header.sender);
		if(c == null) {
			main.getDisplay().syncExec(new Runnable() {
				public void run() {
				    main.getBlindHelper().addCluster(packet.header.sender, false);
					main.getMessageHelper().putClusterNotification(packet);
				}				
			});
		}
	}

	/**
	 * 处理接收到群发消息
	 * 
	 * @param e
	 * 		QQEvent
	 */
	private void processReceiveClusterIM(QQEvent e) {
		final ReceiveIMPacket packet = (ReceiveIMPacket)e.getSource();
		main.getDisplay().syncExec(new Runnable() {
			public void run() {
				main.getMessageHelper().putClusterIM(packet);
			}				
		});
	}

	/**
	 * 处理退出群成功事件
	 * 
	 * @param e
	 * 		QQEvent
	 */
	private void processClusterExitClusterSuccess(QQEvent e) {
		final ClusterCommandReplyPacket packet = (ClusterCommandReplyPacket)e.getSource();
		main.getDisplay().syncExec(new Runnable() {
			public void run() {
			    main.getBlindHelper().removeCluster(packet.clusterId);
			}				
		});
	}

	/**
	 * 处理得到成员信息成功事件
	 * 
	 * @param e
	 * 		QQEvent
	 */
	private void processClusterGetMemberInfoSuccess(QQEvent e) {
		final ClusterCommandReplyPacket packet = (ClusterCommandReplyPacket)e.getSource();
		main.getDisplay().syncExec(new Runnable() {
			public void run() {
				boolean showNick = main.getOptionHelper().isShowNick();
				Cluster c = ModelRegistry.getCluster(packet.clusterId);	
				if(c != null) {
					// 对每个得到信息的成员，设置他们model的属性
					for(QQFriend friend : packet.memberInfos) {
						ContactInfo info = ModelUtils.createContact(friend);
						User f = ModelRegistry.getUser(friend.qqNum);						
						if(f != null) {
							f.nick = friend.nick;							
							f.headId = friend.head;
							f.member = friend.isMember();
							f.userFlag = friend.userFlag;
							f.female = !friend.isGG();
							f.info = info;
							if(!f.hasCardName())
								f.displayName = showNick ? f.nick : (f.hasRemarkName() ? f.getRemarkName() : f.nick);
						}
						f = c.getMember(friend.qqNum);
						if(f != null) {
							f.nick = friend.nick;
							f.headId = friend.head;
							f.member = friend.isMember();
							f.info = info;
							f.userFlag = friend.userFlag;
							f.female = !friend.isGG();
							if(!f.hasCardName())
								f.displayName = showNick ? f.nick : (f.hasRemarkName() ? f.getRemarkName() : f.nick);
						}
					}
					// 如果存在这个群的资料窗口，则刷新数据
					if(main.getShellRegistry().hasClusterInfoWindow(c)) {
						ClusterInfoWindow cis = main.getShellRegistry().getClusterInfoWindow(c);
						cis.setClusterModel(c);
					}
					// 如果存在这个群的信息窗口，则刷新数据
					if(main.getShellRegistry().hasSendClusterIMWindow(c))
					    main.getShellRegistry().getSendClusterIMWindow(c).refreshClusterInfo();
					// 遍历当前打开的群信息窗口，如果存在这个群的子群，则刷新在线状态
					Iterator<Integer> i = main.getShellRegistry().getSendClusterIMWindowModelIterator();
					while(i.hasNext()) {
						Cluster model = ModelRegistry.getCluster(i.next());
						if(model == null)
							continue;
					    if(model.parentClusterId == c.clusterId)
					        main.getShellRegistry().getSendClusterIMWindow(model).refreshClusterInfo();
					}
					// 得到在线成员
					main.getClient().getClusterOnlineMember(packet.clusterId);
					// 重画
					main.getBlindHelper().refreshGroup(c.group);
				}
			}					
		});
	}

	/**
	 * 处理被系统踢出事件
	 */
	private void processKickedOutBySystem() {
		main.getDisplay().asyncExec(new Runnable() {
			public void run() {
			    main.getClient().getUser().setStatus(QQ.QQ_STATUS_OFFLINE);
				main.logout();
				MessageDialog.openWarning(main.getShell(), message_box_common_warning_title, message_box_login_twice);
			}
		});
	}

	/**
	 * 处理创建群成功事件
	 * 
	 * @param e
	 * 			QQEvent
	 */
	private void processClusterCreateSuccess(QQEvent e) {
		final ClusterCommandReplyPacket packet = (ClusterCommandReplyPacket)e.getSource();
		main.getDisplay().syncExec(new Runnable() {
			public void run() {
			    main.getBlindHelper().addCluster(packet.clusterId, true);
			}
		});
	}

	/**
	 * 处理请求发送文件事件
	 * 
	 * @param e
	 * 		QQEvent
	 */
	private void processRequestSendFile(QQEvent e) {
		final ReceiveIMPacket packet = (ReceiveIMPacket)e.getSource();
		if(packet.fileArgs.transferType == QQ.QQ_TRANSFER_FILE) {
			main.getDisplay().syncExec(new Runnable() {
				public void run() {
					main.getMessageHelper().putSendFileRequestIM(packet);
				}
			});		    
		} else {
		    log.debug("对方可能发出了如来神掌，我闪～");
		}
	}

	/**
	 * 处理登陆未知错误事件
	 */
	private void processLoginUnknownError() {
		main.getDisplay().syncExec(new Runnable() {
			public void run() {
				main.getClient().getUser().setStatus(QQ.QQ_STATUS_OFFLINE);
				main.getUIHelper().stopStatusAnimation();
				main.getUIHelper().setTrayIconByStatus();
				main.logout();
				MessageDialog.openError(main.getShell(), message_box_login_fail_title, message_box_login_unknown_error);
			}				
		});
	}

	/**
	 * 处理登陆超时事件
	 * 
	 * @param e
	 * 			QQEvent
	 */
	private void processLoginTimeout() {
		main.getDisplay().syncExec(new Runnable() {
			public void run() {
			    // 登录超时后自动重连
			    retryCount++;
			    if(retryCount > 6) {
					main.getClient().getUser().setStatus(QQ.QQ_STATUS_OFFLINE);
					main.getUIHelper().stopStatusAnimation();
					main.getUIHelper().setTrayIconByStatus();
					main.logout();
					MessageDialog.openError(main.getShell(), message_box_login_fail_title, message_box_login_timeout);
			    } else {
			    	// 奇数次：随机使用服务器
			    	// 偶数次：随机使用服务器，强制使用TCP登录
				    boolean forceTcp = retryCount % 2 == 0;
				    QQUser me = main.getClient().getUser();
				    byte status = me.getStatus();
				    me.setStatus(QQ.QQ_STATUS_OFFLINE);
				    main.logout();
				    me.setStatus(status);
				    main.checkLogin(true, forceTcp);				        
			    }				    
			}				
		});
	}

	/**
	 * 处理keep alive成功事件，主要就是保存一下当前在线人数
	 * 
	 * @param e
	 * 		QQEvent
	 */
	private void processKeepAliveSuccess(QQEvent e) {
	    // 设置上线数
		main.setCurrentOnlineNumber(String.valueOf(((KeepAliveReplyPacket)e.getSource()).onlines));		
	}

	/*
	 * 这段代码不再使用，现在我们把删除失败当作是好友已经被删除
	// 处理删除好友失败事件
	private void processDeleteFriendFail(QQEvent e) {
		// 得到被删除的QQ号
		DeleteFriendPacket packet = (DeleteFriendPacket)e.getSource();
		final Integer key = new Integer(packet.getTo());
		// 除去删除走向映射，得到这个好友的组索引和组内索引
		main.removeDeleteToMap(key);
		// 打开消息框通知用户删除失败
		main.getDisplay().syncExec(
			new Runnable() {
				public void run() {
					MessageBox box = new MessageBox(main.getShell(), SWT.ICON_ERROR | SWT.OK);
					box.setText(LumaQQ.getResourceString("message.box.common.fail.title"));
					box.setMessage(LumaQQ.getResourceString("error.delete.friend.fail", new Object[] { key.toString() }));
					box.open();
				}				
			}
		);
	}*/

	/**
	 * 处理添加好友成功事件
	 * 
	 * @param e
	 */
	private void processAddFriendSuccess(QQEvent e) {
		final AddFriendExReplyPacket packet = (AddFriendExReplyPacket)e.getSource();
		main.getDisplay().asyncExec(new Runnable() {
			public void run() {
				main.getBlindHelper().addFriend(packet.friendQQ);
			}					
		});
	}
	
	/**
	 * 处理删除好友成功事件
	 * 
	 * @param e
	 * 		QQEvent
	 */
	private void processDeleteFriendSuccess(QQEvent e) {
		// 得到被删除的QQ号
		final DeleteFriendPacket packet = (DeleteFriendPacket)e.getSource();
		
		// 更新服务器端好友列表
		// 这个功能不再使用，因为LumaQQ会允许不自动更新分组信息，所以，这里搞成自动的会带来问题
		//main.getClient().removeFriendFromList(packet.getTo());
		
		// 从model中删除
		main.getDisplay().syncExec(new Runnable() {
			public void run() {
				// 得到好友删除后的去向，可能是拖到陌生人或者黑名单里面了，也可能就是直接删了，那样就是null
				Group g = main.removeDeleteToMap(packet.getTo());
				User f = ModelRegistry.getUser(packet.getTo());
				if(f != null && !f.group.isBlackList())
					main.getBlindHelper().simpleMove(f, f.group, g);
			}					
		});
	}

	/**
	 * 处理登陆密码错误事件
	 * @param e 
	 */
	private void processLoginFail(final QQEvent e) {
		main.getDisplay().syncExec(new Runnable() {
			public void run() {
				main.restartLogin(e);
			}				
		});		
	}

	/**
	 * 处理收到消息事件
	 * 
	 * @param e
	 * 		QQEvent
	 */
	private void processReceiveNormalIM(QQEvent e) {
		// 得到包，推入消息队列
		final ReceiveIMPacket packet = (ReceiveIMPacket)e.getSource();
		main.getDisplay().syncExec(new Runnable() {
			public void run() {
			    main.getMessageHelper().putNormalIM(packet);
			}				
		});
	}
	
	/**
	 * 处理收到系统通知事件
	 * 
	 * @param e
	 * 		QQEvent
	 */
	private void processReceiveSystemNotification(QQEvent e) {
		final SystemNotificationPacket packet = (SystemNotificationPacket)e.getSource();
		main.getDisplay().syncExec(new Runnable() {
			public void run() {
				main.getMessageHelper().putSystemNotificationAndUpdateAnimate(packet);
			}				
		});
	}

    /**
     * 处理好友状态改变事件
     * 
     * @param e	
     * 			QQEvent
     */
    private void processFriendChangeStatus(QQEvent e) {
        final FriendChangeStatusPacket packet = (FriendChangeStatusPacket) e.getSource();
        final User f = ModelRegistry.getUser(packet.friendQQ);
        if(f != null) {
            main.getDisplay().syncExec(new Runnable() {
                public void run() {
                    // 状态确实改变时才需要以下操作
                	if(packet.userFlag != 0)
                		f.userFlag = packet.userFlag;
                    if(f.status.toQQConstant() != packet.status) {
                    	f.status = Status.valueOf(packet.status);
                    	main.getBlindHelper().refreshGroup(f.group);
                    	
                    	// 检查最近联系人中是否有这个好友，如果有，更新状态
                    	main.getBlindHelper().synchronizedLatestStatus(f, true);
                    	
                    	// 如果需要，显示上线提示
                        if(f.isOnline() && main.getOptionHelper().isShowOnlineTip())
                            main.getTipHelper().getOnlineTipShell().addFriendModel(f);
                    }
                }
            });
        }
    }

	/**
	 * 处理状态改变失败事件
	 */
	private void processChangeStatusFail() {
		main.getDisplay().syncExec(new Runnable() {
			public void run() {
				MessageDialog.openWarning(main.getShell(), message_box_common_fail_title, error_change_status_fail);
			}				
		});
	}

	/**
	 * 处理改变状态成功事件
	 */
	private void processChangeStatusSuccess() {
		main.getDisplay().syncExec(new Runnable() {
			public void run() {
				main.getUIHelper().stopStatusAnimation();
			    main.getUIHelper().setTrayIconByStatus();
			}				
		}); 
	}

	/**
	 * 处理得到在线好友列表的事件，判断好友的状态是否改变了，如果改变了，就刷新view
	 * 对于会员，我们把文字颜色设成红色
	 * 
	 * @param e
	 * 		QQEvent
	 */
	private void processGetFriendOnlineSuccess(QQEvent e) {
		// 得到包
		GetOnlineOpReplyPacket packet = (GetOnlineOpReplyPacket)e.getSource();
		// 循环检查各个好友的状态，如果改变了就设置
		for(FriendOnlineEntry entry : packet.onlineFriends) {
			// 搜索出这个好友的model
			final User f = ModelRegistry.getUser(entry.status.qqNum);
			if(f != null) {
				// 得到状态，判断是否会员
				tempHash.put(f, f);		
				// 如果状态改变了，更新view
				if(f.status.toQQConstant() != entry.status.status) {
					f.ip = entry.status.ip;
					f.port = entry.status.port;
					f.userFlag = entry.userFlag;
					f.status = Status.valueOf(entry.status.status);
					main.getBlindHelper().synchronizedLatestStatus(f, false);
					if(!main.getTipHelper().isFirstTime() && f.isOnline() && main.getOptionHelper().isShowOnlineTip()) {
						main.getDisplay().syncExec(new Runnable() {
							public void run() {
								main.getTipHelper().getOnlineTipShell().addFriendModel(f);					
							}
						});
					}
				}
			}
		}
		// 比较上一次的在线好友列表，如果有些好友不在这一次中，则肯定是下线或者隐身了，改变他们的状态
		// 当前这个操作必须在得到所有online好友时进行，方法是判断包的position字段
		if(packet.finished) {
			for(final User f : main.getCurrentOnlines()) {
				if(!tempHash.containsKey(f)) {
					f.status = Status.OFFLINE;
					main.getBlindHelper().synchronizedLatestStatus(f, false);
				}
			}
			main.getCurrentOnlines().clear();
			main.getCurrentOnlines().addAll(tempHash.values());
			tempHash.clear();
			main.getTipHelper().setFirstTime(false);
			main.getDisplay().syncExec(new Runnable() {
				public void run() {
					main.getBlindHelper().refreshAll();			
				}							
			});
			
			// 对打开的每一个群信息窗口，请求得到在线成员列表
			// 对于打开的每一个多人对话信息窗口，刷新成员的在线状态，因为多人对话的成员按理来说都是
			// 我的好友，所以，得到好友在线信息后就可以刷新他们了
			Iterator<Integer> i = main.getShellRegistry().getSendClusterIMWindowModelIterator();
			while(i.hasNext()) {
			    final Cluster model = ModelRegistry.getCluster(i.next());
			    if(model == null)
			    	continue;
			    if(model.clusterType == ClusterType.NORMAL)
			        main.getClient().getClusterMemberInfo(model.clusterId, model.getMemberQQList());
			    else if(model.parentClusterId == 0) {
			        main.getDisplay().asyncExec(new Runnable() {
                        public void run() {
					        main.getShellRegistry().getSendClusterIMWindow(model).refreshClusterInfo();
                        }
			        });
			    }
			}
		} else
		    main.getClient().getFriendOnline(packet.position);
	}

	/**
	 * 处理登陆成功事件
	 */
	private void processLoginSuccess() {
	    if(main.getClient().getUser().isLoggedIn()) {
	    	main.getMessageQueue().setPostpone(true);	    	
	        retryCount = 0;

			final boolean autoDownloadGroup = main.getOptionHelper().isAutoDownloadGroup();
			final boolean autoDownloadFriendRemark = main.getOptionHelper().isAutoDownloadFriendRemark();
			final boolean userEmpty = ModelRegistry.getUserCount() == 0;
			IExecutor executor = new BackgroundJobExecutor(main);			    
			if(!autoDownloadGroup) {
				main.getMessageQueue().setPostpone(false);
			    main.getMessageHelper().processPostponedIM();
			    if(userEmpty) {
				    executor.addJob(new GetFriendListJob());
					executor.addJob(new DownloadGroupJob());
			    }
				executor.addJob(new GetUserPropertyJob());
				executor.addJob(new DownloadSignatureJob());
				executor.addJob(new GetFriendLevelJob());
				IJob job = new GetCustomHeadInfoJob();
				job.setSuccessLink(new DownloadCustomHeadJob());
				executor.addJob(job);
			}
			if(autoDownloadFriendRemark || autoDownloadGroup) {				
				// 下载好友分组
				if(userEmpty)
				    executor.addJob(new GetFriendListJob());
				if(userEmpty || autoDownloadGroup)
					executor.addJob(new DownloadGroupJob());
				
				// 下载好友属性，个性签名，备注，等级
				executor.addJob(new GetUserPropertyJob());
				executor.addJob(new DownloadSignatureJob());
				executor.addJob(new GetFriendLevelJob());
				if(autoDownloadFriendRemark)
				    executor.addJob(new DownloadFriendRemarkJob());
				IJob job = new GetCustomHeadInfoJob();
				job.setSuccessLink(new DownloadCustomHeadJob());
				executor.addJob(job);
				
				// 第一次同步之后，设置不再自动同步
				main.getOptionHelper().setAutoDownloadGroup(false);
				main.getOptionHelper().setAutoDownloadFriendRemark(false);
			} 
			
			// 启动任务
			if(executor.getAllJobCount() > 0)
				executor.execute();
			
			// 请求在线好友列表
			main.getClient().getFriendOnline();
			
			// 设置界面状态
			main.getDisplay().syncExec(new Runnable() {
				public void run() {
					main.getUIHelper().stopStatusAnimation();
					main.getUIHelper().setTrayIconByStatus();
					if(!autoDownloadFriendRemark && !autoDownloadGroup && !userEmpty) {
						main.stopWaitingPanelAnimation();
						main.switchPanel(MainShell.PANEL_MAIN);
					}
					if(main.getOptionHelper().isShowLastLoginTip())
						main.getShellLauncher().openLastLoginTipWindow(main);
				}
			});
			
			// 检查更新
			if(main.getOptionHelper().isAutoCheckUpdate())
				new CheckUpdateThread(main).start();
	    }
	}

    /**
     * @param e
     */
    private void processClusterGetTempClusterInfoSuccess(QQEvent e) {
		final ClusterCommandReplyPacket packet = (ClusterCommandReplyPacket)e.getSource();
		main.getDisplay().syncExec(new Runnable() {
			public void run() {
				// 搜索这个群
				Cluster c = ModelRegistry.getCluster(packet.clusterId);
				// 群存在操作才有意义
				if(c != null) {					
					c.creator = packet.info.creator;
					c.authType = packet.info.authType;
					c.category = packet.info.category;
					c.oldCategory = packet.info.oldCategory;
					c.description = packet.info.description;
					c.notice = packet.info.notice;
					c.versionId = packet.info.versionId;
					c.externalId = packet.info.externalId;					
					c.clusterType = ClusterType.valueOfTemp(packet.type);
					c.name = packet.info.name;

					// 同步成员列表
					synchronizeMember(c, packet.members, true, c.parentClusterId);							
					// 如果存在这个群的资料窗口，则刷新数据
					if(main.getShellRegistry().hasClusterInfoWindow(c)) {
						ClusterInfoWindow cis = main.getShellRegistry().getClusterInfoWindow(c);
						cis.setClusterModel(c);
					}
					// 如果存在这个群的信息窗口，则刷新数据
					if(!main.getOptionHelper().isUseTabIMWindow() && main.getShellRegistry().hasSendClusterIMWindow(c))
					    main.getShellRegistry().getSendClusterIMWindow(c).refreshClusterInfo();
					// 如果标签页窗口是打开的
					if(main.getOptionHelper().isUseTabIMWindow() && main.getShellRegistry().isSendIMTabWindowOpened()) {
						SendIMTabWindow sitw = main.getShellRegistry().getSendIMTabWindow();
						sitw.refreshTab(c);						
					}
					// 刷新群组
					main.getBlindHelper().refreshGroup(main.getBlindHelper().getClusterGroup());
				}									
			}
		});
    }
    
    /**
	 * 初始发送群消息失败事件
	 * 
     * @param e
     */
    private void processSendClusterIMFail(QQEvent e) {
		final ClusterCommandReplyPacket packet = (ClusterCommandReplyPacket)e.getSource();
		main.getDisplay().syncExec(new Runnable() {
			public void run() {
			    switch(packet.replyCode) {
			        case QQ.QQ_REPLY_TEMP_CLUSTER_REMOVED:
			        case QQ.QQ_REPLY_NOT_CLUSTER_MEMBER:
			        case QQ.QQ_REPLY_NOT_TEMP_CLUSTER_MEMBER:
					    main.getBlindHelper().removeCluster(packet.clusterId);
			        	break;
			    }
			}				
		});			
    }
    
	/**
	 * 处理群命令失败事件，如果错误信息表示自己已经不是群成员，则删除这个群
	 * @param e
	 */
	private void processClusterCommandFail(QQEvent e) {
		final ClusterCommandReplyPacket packet = (ClusterCommandReplyPacket)e.getSource();
		main.getDisplay().syncExec(new Runnable() {
			public void run() {
			    switch(packet.replyCode) {
			        case QQ.QQ_REPLY_TEMP_CLUSTER_REMOVED:
			        case QQ.QQ_REPLY_NOT_CLUSTER_MEMBER:
			        case QQ.QQ_REPLY_NOT_TEMP_CLUSTER_MEMBER:
						main.getBlindHelper().removeCluster(packet.clusterId);
			        	break;
			    }
			}				
		});			
	}	

    /**
     * 处理得到群在线成员成功事件
     * 
     * @param e
     */
    private void processClusterGetOnlineMemberSuccess(QQEvent e) {
		final ClusterCommandReplyPacket packet = (ClusterCommandReplyPacket)e.getSource();
		main.getDisplay().syncExec(new Runnable() {
			public void run() {
			    Cluster c = ModelRegistry.getCluster(packet.clusterId);
			    if(c == null)
			        return;
			    
			    // 重设组内成员状态
			    c.resetMemberStatus();			    
				for(Integer qq : packet.onlineMembers) {
					User u = c.getMember(qq);
					if(u != null)
						u.status = Status.ONLINE;
			    }
			    
				// 如果存在这个群的信息窗口，则刷新数据
				if(!main.getOptionHelper().isUseTabIMWindow() && main.getShellRegistry().hasSendClusterIMWindow(c))
				    main.getShellRegistry().getSendClusterIMWindow(c).refreshClusterInfo();
				// 如果标签页窗口是打开的
				if(main.getOptionHelper().isUseTabIMWindow() && main.getShellRegistry().isSendIMTabWindowOpened()) {
					SendIMTabWindow sitw = main.getShellRegistry().getSendIMTabWindow();
					sitw.refreshTab(c);						
				}
				// 重画
				main.getBlindHelper().refreshGroup(c.group);
			}				
		});	
    }
    
	/**
	 * <pre>
	 * 处理得到群信息成功事件，处理完毕后会相继请求群成员信息和在线成员列表，这个方法
	 * 在新增加一个群时会被调用。此外，如果群聊窗口打开后，20分钟请求一次群信息
	 * </pre>
	 * 
	 * @param e
	 * 		QQEvent
	 */
	private void processClusterGetInfoSuccess(QQEvent e) {
		final ClusterCommandReplyPacket packet = (ClusterCommandReplyPacket)e.getSource();
		main.getDisplay().syncExec(new Runnable() {
			public void run() {
				// 搜索这个群
				Cluster c = ModelRegistry.getCluster(packet.clusterId);
				// 群存在操作才有意义
				if(c != null) {
				    if(packet.externalId == 0) {
				        main.getBlindHelper().removeCluster(packet.clusterId);
				        return;
				    }
				    
					c.creator = packet.info.creator;
					c.authType = packet.info.authType;
					c.category = packet.info.category;
					c.oldCategory = packet.info.oldCategory;
					c.description = packet.info.description;
					c.notice = packet.info.notice;
					c.versionId = packet.info.versionId;
					c.externalId = packet.externalId;	
				    c.clusterType = ClusterType.NORMAL;
				    c.name = packet.info.name;
					// 同步成员列表
					synchronizeMember(c, packet.members, false, 0);						
					// 如果存在这个群的资料窗口，则刷新数据
					if(main.getShellRegistry().hasClusterInfoWindow(c)) {
						ClusterInfoWindow cis = main.getShellRegistry().getClusterInfoWindow(c);
						cis.setClusterModel(c);
					}
					// 如果存在这个群的信息窗口，则刷新数据
					if(!main.getOptionHelper().isUseTabIMWindow() && main.getShellRegistry().hasSendClusterIMWindow(c))
					    main.getShellRegistry().getSendClusterIMWindow(c).refreshClusterInfo();
					// 如果标签页窗口是打开的
					if(main.getOptionHelper().isUseTabIMWindow() && main.getShellRegistry().isSendIMTabWindowOpened()) {
						SendIMTabWindow sitw = main.getShellRegistry().getSendIMTabWindow();
						sitw.refreshTab(c);						
					}
					// 刷新群组
					main.getBlindHelper().refreshGroup(c.group);
					// 同步最近联系人组
					main.getBlindHelper().synchronizeLatest(c);
					// 请求得到成员信息					
					main.getClient().getClusterMemberInfo(packet.clusterId, packet.members);
				}									
			}
		});
	}
	
	/**
	 * 处理得到用户信息成功事件
	 * 
	 * @param e
	 * 		QQEvent
	 */
	private void processGetUserInfoSuccess(QQEvent e) {
	    Display display = main.getDisplay();
	    User myModel = main.getMyModel();
	    ShellRegistry shellRegistry = main.getShellRegistry();
	    
		final ContactInfo info = ((GetUserInfoReplyPacket)e.getSource()).contactInfo;
		int qqNum = info.qq;
		if(qqNum == 0) {
			log.error("得到用户信息，但是解析QQ号失败，该信息被抛弃");
			return;
		}
		// 找到用户的model
		User user = ModelRegistry.getUser(qqNum);
		if(user == null) {
			user = new User();
			user.qq = qqNum;
		}
		user.info = info;
		user.nick = info.nick;
		user.headId = info.head;
		// 似乎userFlag是分成两个部分的，而GetUserInfo只得到低16位的部分
		// 也许低16位是用来表示用户的固有属性，而高16位是用来表示用户的动态属性
		// 所以这里做了一些处理。不过上述理论纯属猜测
		user.userFlag &= 0xFFFF0000;
		user.userFlag |= (info.userFlag & 0x0000FFFF);
		user.female = info.gender.equals(gender_mm);
		if(main.getOptionHelper().isShowNick())
			user.displayName = user.nick;
		else
			user.displayName = user.hasRemarkName() ? user.getRemarkName() : user.nick;
		// 这里还要检查一下被得到信息的用户是不是自己
		if(qqNum == myModel.qq) {
			myModel.info = info;
			myModel.headId = info.head;
			myModel.nick = info.nick;
			myModel.displayName = myModel.nick;
			myModel.userFlag = info.userFlag;
			myModel.female = info.gender.equals(gender_mm);
			main.getClient().getUser().setContactInfo(info);
		}
		
		final User finalUser = user;
		if(shellRegistry.hasUserInfoWindow(user)) {
			// 在最后，我们根据用户的查看资料窗口是否打开来决定是否进行更新操作
			display.syncExec(new Runnable() {
				public void run() {
				    UserInfoWindow uis = main.getShellRegistry().getUserInfoWindow(finalUser);
					if(uis != null)
						uis.setFriendModel(finalUser);
					SendIMWindow siw = main.getShellRegistry().getSendIMWindow(finalUser);
					if(siw != null) 
						siw.refresh(finalUser);
					if(main.getOptionHelper().isUseTabIMWindow() && main.getShellRegistry().isSendIMTabWindowOpened()) {
						SendIMTabWindow sitw = main.getShellRegistry().getSendIMTabWindow();
						sitw.refreshTab(finalUser);						
					}
				}				
			});			
		}
		if(shellRegistry.hasTempSessionIMWindow(user)) {
			display.syncExec(new Runnable() {
				public void run() {
					TempSessionIMWindow win = main.getShellRegistry().getTempSessionIMWindow(finalUser);
					if(win != null)
						win.refresh(finalUser);
				}
			});
		}
		
		// 刷新
		if(finalUser.group != null) {
			display.syncExec(new Runnable() {
				public void run() {				
					main.getBlindHelper().refreshGroup(finalUser.group);
                	main.getBlindHelper().synchronizeLatest(finalUser);
				}
			});			
		}
	}
	
	/**
	 * 同步成员列表
	 */
	private void synchronizeMember(Cluster c, List<Member> newMembers, boolean tempCluster, int parentClusterId) {		
	    // 去掉重复的成员
		c.clearAdmins();
		c.clearStockholders();
	    List<User> existMembers = new ArrayList<User>();
	    List<Member> newMembersBak = new ArrayList<Member>(newMembers);
		for(Iterator<Member> i = newMembersBak.iterator(); i.hasNext(); ) {
			Member member = i.next();
			User u = c.removeMember(member.qq);
			if(u != null) {
				u.organizationId = member.organization;
				existMembers.add(u);
				i.remove();
				if(member.isAdmin())
					c.addAdmin(member.qq);
				else if(member.isStockholder())
					c.addStockholder(member.qq);
			}
		}
	    
	    // Map中剩下的成员为需要删除掉的成员
		c.removeAllMembers();
	    // 恢复重复的成员
		for(User member: existMembers)
			c.addMember(member);
	    // 添加新成员
        Cluster parent = null;
	    if(tempCluster && parentClusterId != 0) {
	    	parent = ModelRegistry.getCluster(parentClusterId);	   
	    	if(c.group == null) 
	    		parent.addSubCluster(c);
	    }
		for(Member member : newMembersBak) {
	        // 找到这个成员的model，如果是固定群，先从好友里面找
	        // 如果是讨论组，从父群里面找，如果是多人对话，从好友里面找
			int qq = member.qq;
			User f = null;
	        if(qq == main.getMyModel().qq)
	            f = main.getMyModel();
	        else if(tempCluster) {
	            if(parentClusterId == 0)
		            f = ModelRegistry.getUser(qq);
	            else if(parent != null)
	                f = parent.getMember(qq);
	        } else
	            f = ModelRegistry.getUser(qq);
	            
	        // 没找到就新建一个
	        if(f == null) {
				f = new User();
				f.qq = qq;
				f.nick = String.valueOf(qq);				
				f.remark = main.getConfigHelper().getRemark(f.qq);
				f.displayName = main.getOptionHelper().isShowNick() ? f.nick : ((f.remark == null) ? f.nick : f.remark.getName());
				f.organizationId = member.organization;
	        } else {
	        	try {
					f = (User)f.clone();
					f.organizationId = member.organization;
				} catch(CloneNotSupportedException e) {
					continue;
				}
	        }
			c.addMember(f);		
			if(member.isAdmin())
				c.addAdmin(member.qq);
			else if(member.isStockholder())
				c.addStockholder(member.qq);
	    }
	}
}
