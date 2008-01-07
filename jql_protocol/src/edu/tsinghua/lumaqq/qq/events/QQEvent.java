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
package edu.tsinghua.lumaqq.qq.events;

import java.util.EventObject;

import edu.tsinghua.lumaqq.qq.annotation.DocumentalEvent;


/**
 * <pre>
 * QQ事件类。QQ事件是比包事件更高一级的封装。其代表了某个具体动作的发生，包事件是粗粒度的，
 * QQ事件是细粒度的。每个QQ事件都有一个相应的包与之关联，这些包中的字段是否可用，要根据
 * QQ事件来判断。按理来说是应该写个事件参考手册的。但是懒，就不写了。
 * </pre>
 * 
 * @author luma
 */
public class QQEvent extends EventObject {
	private static final long serialVersionUID = 3256718502722024499L;

	/**
     * <code>QQ_LOGIN_SUCCESS</code>事件在登录成功是发生，Source是LoginReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_LOGIN_SUCCESS = 1;
    
    /**
     * <code>QQ_LOGIN_ERROR</code>事件在登录错误时发生，Source是LoginReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_LOGIN_FAIL = 2;
    
    /**
     * <code>QQ_LOGIN_UNKNOWN_ERROR</code>事件在登录时发生未知错误时发生，Source是LoginReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_LOGIN_UNKNOWN_ERROR = 3;
    
    /**
     * <code>QQ_LOGIN_REDIRECT_NULL</code>事件在重定向到一个0地址时发生，source是
     * LoginReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_LOGIN_REDIRECT_NULL = 95;
    
    /**
     * <code>QQ_KEEP_ALIVE_SUCCESS</code>事件在Keep Alive包收到确认时发生，source是KeepAliveReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_KEEP_ALIVE_SUCCESS = 4;
    
    /**
     * <code>QQ_CONNECTION_LOST</code>事件在连接失去时发生，这种情况一般时Keep Alive包没有反应
     * 时触发的，source无用处
     */
	@DocumentalEvent
    public static final int QQ_CONNECTION_LOST = 5;    
    
    /**
     * <code>QQ_FRIEND_CHANGE_STATUS</code>事件发生在某个好友的状态改变时，source是FriendChangeStatusPacket
     */
	@DocumentalEvent
    public static final int QQ_FRIEND_CHANGE_STATUS = 7;
    
    /**
     * <code>QQ_GET_USER_INFO_SUCCESS</code>事件发生在得到用户信息成功时，source是GetUserInfoReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_GET_USER_INFO_SUCCESS = 8;
    
    /**
     * <code>QQ_CHANGE_STATUS_SUCCESS</code>事件发生你自己的状态改变成功时，source是ChangeStatusReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_CHANGE_STATUS_SUCCESS = 10;
    
    /**
     * <code>QQ_CHANGE_STATUS_FAIL</code>事件发生你自己的状态改变失败时，source是ChangeStatusReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_CHANGE_STATUS_FAIL = 11;
    
    /**
     * <code>QQ_GET_FRIEND_LIST_SUCCESS</code>事件发生在得到好友列表成功
     * 时，source是GetFriendListReplyPacket，需要检查回复包的标志来判断是否
     * 还有更多好友需要得到
     */
	@DocumentalEvent
    public static final int QQ_GET_FRIEND_LIST_SUCCESS = 12;
    
    /**
     * <code>QQ_GET_FRIEND_ONLINE_SUCCESS</code>事件在得到在线好友列表成功时发生，source是
     * GetOnlineOpReplyPacket，用户应该检查position字段判断是否还有更多在线好友
     */
	@DocumentalEvent
    public static final int QQ_GET_FRIEND_ONLINE_SUCCESS = 14;
    
    /**
     * <code>QQ_RECEIVE_SYS_MESSAGE</code>事件在收到一条系统广播消息时发生，
     * source是ReceiveIMPacket
     */
	@DocumentalEvent
    public static final int QQ_RECEIVE_SYS_MESSAGE = 15;
    
    /**
     * <code>QQ_SEND_IM_SUCCESS</code>事件在发送消息成功时发生，表示消息已经成功发送，source是
     * SendIMPacket，注意不是SendIMReplyPacket，这个没什么用
     */
	@DocumentalEvent
    public static final int QQ_SEND_IM_SUCCESS = 16;
    
    /**
     * <code>QQ_KICKED_OUT_BY_SYSTEM</code>事件在收到你的QQ号在其他地方登陆导致你被系统踢出时发生，
     * source是SystemNotificationPacket。系统通知和系统消息是不同的两种事件，系统通知是对你一个人发
     * 出的（或者是和你相关的），系统消息是一种广播式的，每个人都会收到，要分清楚这两种事件。此外
     * 系统通知的载体是SystemNotificationPacket，而系统消息是ReceiveIMPacket，ReceiveIMPacket的功
     * 能和格式很多。这也是一个区别。注意其后的我被其他人加为好友，验证被通过被拒绝等等，都是系统
     * 通知范畴
     */
	@DocumentalEvent
    public static final int QQ_KICKED_OUT_BY_SYSTEM = 17;
    
    /**
     * <code>QQ_RECEIVE_NORMAL_IM</code>事件在收到一个普通消息是发生，
     * source是ReceiveIMPacket
     */
	@DocumentalEvent
    public static final int QQ_RECEIVE_NORMAL_IM = 18;
    
    /**
     * <code>QQ_MODIFY_INFO_FAIL</code>事件在修改用户信息失败时发生，
     * source是ModifyInfoPacket，注意不是ModifyInfoReplyPacket，因为
     * Reply包毫无价值
     */
	@DocumentalEvent
    public static final int QQ_MODIFY_INFO_FAIL = 19;
    
    /**
     * <code>QQ_MODIFY_INFO_SUCCESS</code>事件在修改用户信息成功是发生，source是
     * ModifyInfoPacket
     */
	@DocumentalEvent
    public static final int QQ_MODIFY_INFO_SUCCESS = 20;    
    
    /**
     * <code>QQ_ADDED_BY_OTHERS</code>事件发生在有人将我加为好友时，source是SystemNotificationPacket
     */
	@DocumentalEvent
    public static final int QQ_ADDED_BY_OTHERS = 21;
    
    /**
     * <code>QQ_ADDED_BY_OTHERS_EX</code>事件发生在有人将我加为好友时，source是SystemNotificationPacket
     */
	@DocumentalEvent
    public static final int QQ_ADDED_BY_OTHERS_EX = 164;
    
    /**
     * <code>QQ_REQUEST_ADD_ME</code>事件发生在有人请求加我为好友时，上面的是我没有设置验证
     * 是发生的，这个事件是我如果设了验证时发生的，两者不会都发生。source是SystemNotificationPacket
     */
	@DocumentalEvent
    public static final int QQ_REQUEST_ADD_ME = 22;
    
    /**
     * <code>QQ_REQUEST_ADD_ME_EX</code>事件发生在有人请求加我为好友时，source是SystemNotificationPacket。
     * 这是QQ_REQUEST_ADD_ME的扩展事件，在2005中使用
     */
	@DocumentalEvent
    public static final int QQ_REQUEST_ADD_ME_EX = 144;
    
    /**
     * <code>QQ_REQUEST_ADD_OTHER_APPROVED_AND_ADD</code>事件发生在有人请求加我为好友时，我同意并且加他
     * 为好友，source是SystemNotificationPacket
     */
	@DocumentalEvent
    public static final int QQ_REQUEST_ADD_OTHER_APPROVED_AND_ADD = 9;
    
    /**
     * <code>QQ_REQUEST_ADD_OTHER_APPROVED</code>事件发生在我请求加一个人，
     * 那个人同意我加的时候，source是SystemNotificationPacket
     */
	@DocumentalEvent
    public static final int QQ_REQUEST_ADD_OTHER_APPROVED = 23;
    
    /**
     * <code>QQ_REQUEST_ADD_OTHER_REJECTED</code>事件发生在我请求加一个人，那个人拒绝时，
     * source是SystemNotificationPacket
     */
	@DocumentalEvent
    public static final int QQ_REQUEST_ADD_OTHER_REJECTED = 24;
    
    /**
     * <code>QQ_ADD_FRIEND_SUCCESS</code>事件发生在我添加一个好友成功时，
     * source是AddFriendExReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_ADD_FRIEND_SUCCESS = 25;
    
    /**
     * <code>QQ_ADD_FRIEND_NEED_AUTH</code>事件发生在我添加一个好友，但是对方需要认证时，
     * source是AddFriendExReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_ADD_FRIEND_NEED_AUTH = 26;
    
    /**
     * <code>QQ_ADD_FRIEND_DENY</code>事件发生在我添加一个好友，但是对方设置了
     * 禁止别人把我添加为好友，source是AddFriendExReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_ADD_FRIEND_DENY = 27;
    
    /**
     * <code>QQ_ADD_FRIEND_ALREADY</code>事件发生在我添加一个好友，但是
     * 对方已经是我的好友，source是AddFriendExReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_ADD_FRIEND_ALREADY = 165;
    
    /**
     * <code>QQ_DELETE_FRIEND_SUCCESS</code>事件在删除好友成功时发生，
     * source是DeleteFriendPacket，注意不是DeleteFriendReplyPacket，
     * 因为Reply包毫无价值
     */
	@DocumentalEvent
    public static final int QQ_DELETE_FRIEND_SUCCESS = 28;
    
    /**
     * <code>QQ_DELETE_FRIEND_FAIL</code>事件在删除好友失败是发生，
     * source是DeleteFriendPacket
     */
	@DocumentalEvent
    public static final int QQ_DELETE_FRIEND_FAIL = 29;
    
    /**
     * <code>QQ_ADD_FRIEND_AUTH_SEND_SUCCESS</code>事件在你发送认证信息给别人成功时发生，
     * source是AddFriendAuthResponsePacket，注意不是AddFriendAuthReplyPacket，这个包没用
     */
    @DocumentalEvent
    public static final int QQ_ADD_FRIEND_AUTH_SEND_SUCCESS = 30;
    
    /**
     * <code>QQ_ADD_FRIEND_AUTH_SEND_FAIL</code>事件在你发送认证信息给别人失败时发生，
     * source是AddFriendAuthResponsePacket
     */
    @DocumentalEvent
    public static final int QQ_ADD_FRIEND_AUTH_SEND_FAIL = 31;
    
    /**
     * <code>QQ_ADD_FRIEND_FAIL</code>事件发生在请求信息发送失败时，source是AddFriendPacket
     */
	@DocumentalEvent
    public static final int QQ_ADD_FRIEND_FAIL = 32;
    
    /**
     * <code>QQ_OPERATION_TIMEOUT</code>事件在操作超时时发生，也就是请求包没有能收到回复，
     * source是要发送的那个包，通知QQEvent的operation字段表示了操作的类型。要注意超时事件和
     * Fail事件的不同，超时是指包没有收到任何确认，fail是指确认收到了，并且根据确认包的内容，
     * 操作失败了
     */
	@DocumentalEvent
    public static final int QQ_OPERATION_TIMEOUT = 33;
    
    /**
     * <code>QQ_REMOVE_SELF_SUCCESS</code>事件在把自己从别人的好友列表中删除成功时发生，
     * source是RemoveSelfReplyPacket，不过没什么用
     */
	@DocumentalEvent
    public static final int QQ_REMOVE_SELF_SUCCESS = 34;
    
    /**
     * <code>QQ_REMOVE_SELF_FAIL</code>事件在把自己从别人的好友列表中删除失败时发生，
     * source是RemoveSelfReplyPacket，不过没什么用
     */
	@DocumentalEvent
    public static final int QQ_REMOVE_SELF_FAIL = 35;
    
    /**
     * <code>QQ_SEARCH_USER_SUCCESS</code>事件在搜索在线用户成功时发生，source是SearchUserReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_SEARCH_USER_SUCCESS = 36;
    
    /**
     * <code>QQ_SEARCH_USER_END</code>事件在一次搜索已经全部完成时发生，source是SearchUserReplyPacket,
     * 但是这个纯粹是通知事件，没有实际数据返回
     */
	@DocumentalEvent
    public static final int QQ_SEARCH_USER_END = 37;
    
    /**
     * <code>QQ_SEND_FILE_REQUEST_SEND_SUCCESS</code>事件在传送文件请求发送成功时发生，source是
     * SendIMPacket
     */
	@DocumentalEvent
    public static final int QQ_SEND_FILE_REQUEST_SEND_SUCCESS = 38;
    
    /**
     * <code>QQ_ACCEPT_SEND_FILE</code>事件在对方接受了自己的传输文件请求时发生，source是ReceiveIMPacket，
     * 接到这个包就可以抽出好友的端口开始初始化连接了
     */
	@DocumentalEvent
    public static final int QQ_ACCEPT_SEND_FILE = 39;
    
    /**
     * <code>QQ_REJECT_SEND_FILE</code>事件在对方拒绝了自己的传输文件请求时发生，source是ReceiveIMPacket
     */
	@DocumentalEvent
    public static final int QQ_REJECT_SEND_FILE = 40;
    
    /**
     * <code>QQ_REQUEST_SEND_FILE</code>事件发生在对方请求向我传送文件时，source是ReceiveIMPacket
     */
	@DocumentalEvent
    public static final int QQ_REQUEST_SEND_FILE = 41;
    
    /**
     * <code>QQ_UPLOAD_GROUP_NAME_SUCCESS</code>事件在上传分组名称成功时发生，source是GroupNameOpReplyPacket,
     * 但是基本没有什么可用信息，通知事件而已
     */
	@DocumentalEvent
    public static final int QQ_UPLOAD_GROUP_NAME_SUCCESS	= 42;
    
    /**
     * <code>QQ_DOWNLOAD_GROUP_NAME_SUCCESS</code>事件在下载分组名称成功时发生，source是GroupNameOpReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_DOWNLOAD_GROUP_NAME_SUCCESS = 43;
    
    /**
     * <code>QQ_UPLOAD_GROUP_FRIEND_SUCCESS</code>事件在上传分组中的好友列表成功时发生，source是
     * UploadGroupFriendPacket，不过没有什么可用信息，通知事件而已
     */
	@DocumentalEvent
    public static final int QQ_UPLOAD_GROUP_FRIEND_SUCCESS = 44;
    
    /**
     * <code>QQ_DOWNLOAD_GROUP_FRIEND_SUCCESS</code>事件在下载分组中的好友列表成功时发生，source是
     * DownloadGroupFriendReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_DOWNLOAD_GROUP_FRIEND_SUCCESS = 45;
    
    /**
     * <code>QQ_DOWNLOAD_GROUP_FRIEND_FAIL</code>事件在下载分组中的好友列表失败时发生，source是
     * DownloadGroupFriendReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_DOWNLOAD_GROUP_FRIEND_FAIL = 107;
    
    /**
     * <code>QQ_UPLOAD_GROUP_FRIEND_FAIL</code>事件在上传分组好友列表失败时发生，source是
     * UploadGroupFriendReplyPacket，通知事件而已
     */
	@DocumentalEvent
    public static final int QQ_UPLOAD_GROUP_FRIEND_FAIL = 46;
    
    /**
     * <code>QQ_UPLOAD_FRIEND_REMARK_SUCCESS</code>在上传好友备注信息成功时发生，source是
     * FriendDataOpPacket
     */
	@DocumentalEvent
    public static final int QQ_UPLOAD_FRIEND_REMARK_SUCCESS = 47;
    
    /**
     * <code>QQ_DOWNLOAD_FRIEND_REMARK_SUCCESS</code>在下载好友备注信息成功时发生，source是
     * FriendDataOpReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_DOWNLOAD_FRIEND_REMARK_SUCCESS = 48;
    
    /**
     * <code>QQ_BATCH_DOWNLOAD_FRIEND_REMARK_SUCCESS</code>在批量下载好友备注信息成功时发生，source是
     * FriendDataOpReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_BATCH_DOWNLOAD_FRIEND_REMARK_SUCCESS = 116;
    
    /**
     * <code>QQ_REMOVE_FRIEND_FROM_LIST_SUCCESS</code>在把好友从服务器端列表
     * 中删除成功时发生，source是FriendDataOpPacket
     */
	@DocumentalEvent
    public static final int QQ_REMOVE_FRIEND_FROM_LIST_SUCCESS = 106;
    
    /**
     * <code>QQ_CANCEL_SEND_FILE</code>在用户取消传送文件操作时发生，source是ReceiveIMPacket
     */
	@DocumentalEvent
    public static final int QQ_CANCEL_SEND_FILE = 49;   
    
    /**
     * <code>QQ_NOTIFY_FILE_TRANSFER_ARGS</code>在发送文件方通知另一方其IP和端口信息时发生，
     * source是ReceiveIMPacket
     */
	@DocumentalEvent
    public static final int QQ_NOTIFY_FILE_TRANSFER_ARGS = 50;
    
    /**
     * <code>QQ_REQUEST_ME_CONNECT</code>发生在文件传输的连接建立过程完成时，source
     * 是ReceiveIMPacket
     */
	@DocumentalEvent
    public static final int QQ_REQUEST_ME_CONNECT = 51;
    
    /**
     * <code>QQ_UNKNOWN_TYPE_IM</code>表示收到了一条目前我不能处理的消息，sourc是ReceiveIMPacket
     */
	@DocumentalEvent
    public static final int QQ_UNKNOWN_TYPE_IM = 52;
    
    /**
     * <code>QQ_CREATE_CLUSTER_SUCCESS</code>在创建一个群成功时发生，source是ClusterCommandReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_CREATE_CLUSTER_SUCCESS = 53;
    
    /**
     * <code>QQ_CREATE_CLUSTER_FAIL</code>在创建一个群失败时发生，source是ClusterCommandReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_CREATE_CLUSTER_FAIL = 54;
    
    /**
     * <code>QQ_ACTIVATE_CLUSTER_SUCCESS</code>在激活一个群成功时发生，source是ClusterCommandReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_ACTIVATE_CLUSTER_SUCCESS = 55;
    
    /**
     * <code>QQ_ACTIVATE_CLUSTER_FAIL</code>在激活一个群失败时发生，source是ClusterCommandReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_ACTIVATE_CLUSTER_FAIL = 56;
    
    /**
     * <code>QQ_GET_CLUSTER_INFO_SUCCESS</code>在得到群信息成功时发生，source是ClusterCommandReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_GET_CLUSTER_INFO_SUCCESS = 57;
    
    /**
     * <code>QQ_GET_CLUSTER_INFO_FAIL</code>在得到群信息失败时发生，source是ClusterCommandReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_GET_CLUSTER_INFO_FAIL = 58;
    
    /**
     * <code>QQ_GET_ONLINE_MEMBER_SUCCESS</code>在得到在线群成员成功时发生，source是ClusterCommandReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_GET_ONLINE_MEMBER_SUCCESS = 59;
    
    /**
     * <code>QQ_GET_ONLINE_MEMBER_FAIL</code>在得到在线群成员失败时发生，source是ClusterCommandReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_GET_ONLINE_MEMBER_FAIL = 60;
    
    /**
     * <code>QQ_GET_MEMBER_INFO_SUCCESS</code>在得到群成员信息成功时发生，source是ClusterCommandReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_GET_MEMBER_INFO_SUCCESS = 61;
    
    /**
     * <code>QQ_GET_MEMBER_INFO_FAIL</code>在得到群成员信息失败时发生，source是ClusterCommandReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_GET_MEMBER_INFO_FAIL = 62;
    
    /**
     * <code>QQ_EXIT_CLUSTER_SUCCESS</code>在退出群成功时发生，source是ClusterCommandReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_EXIT_CLUSTER_SUCCESS = 63;
    
    /**
     * <code>QQ_EXIT_CLUSTER_FAIL</code>在退出群失败时发生，source是ClusterCommandReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_EXIT_CLUSTER_FAIL = 64;
    
    /**
     * <code>QQ_JOIN_CLUSTER_SUCCESS</code>在加入群成功时发生，source是ClusterCommandReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_JOIN_CLUSTER_SUCCESS = 65;
    
    /**
     * <code>QQ_JOIN_CLUSTER_NEED_AUTH</code>在我申请加入群，但是这个群需要认证时发生，source
     * 是ClusterCommandReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_JOIN_CLUSTER_NEED_AUTH = 66;
    
    /**
     * <code>QQ_JOIN_CLUSTER_DENIED</code>在我申请加入群，但是这个群禁止加入成员时发生，source是
     * ClusterCommandReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_JOIN_CLUSTER_DENIED = 90;
    
    /**
     * <code>QQ_JOIN_CLUSTER_FAIL</code>在加入群失败时发生，source是ClusterCommandReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_JOIN_CLUSTER_FAIL = 67;
    
    /**
     * <code>QQ_JOIN_CLUSTER_AUTH_SEND_SUCCESS</code>在发送群认证信息成功时发生，source
     * 是ClusterCommandReplyPacket，这只是一个简单的服务器确认事件，表示认证信息已经被
     * 服务器收到，并非认证已经通过，所以是send success
     */
	@DocumentalEvent
    public static final int QQ_JOIN_CLUSTER_AUTH_SEND_SUCCESS = 68;
    
    /**
     * <code>QQ_JOIN_CLUSTER_AUTH_SEND_FAIL</code>在加入群认证信息失败时发生，source
     * 是ClusterCommandReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_JOIN_CLUSTER_AUTH_SEND_FAIL = 69;
    
    /**
     * <code>QQ_MODIFY_CLUSTER_INFO_SUCCESS</code>在修改群信息成功时发生，source是ClusterCommandReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_MODIFY_CLUSTER_INFO_SUCCESS = 70;
    
    /**
     * <code>QQ_MODIFY_CLUSTER_INFO_FAIL</code>在修改群信息失败时发生，source是ClusterCommandReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_MODIFY_CLUSTER_INFO_FAIL = 71;
    
    /**
     * <code>QQ_RECEIVE_CLUSTER_IM</code>在收到一条固定群消息时发生，source是ReceiveIMPacket
     */
	@DocumentalEvent
    public static final int QQ_RECEIVE_CLUSTER_IM = 72;
    
    /**
     * <code>QQ_RECEIVE_TEMP_CLUSTER_IM</code>事件在收到一条临时群消息时发生，source
     * 是ReceiveIMPacket
     */
	@DocumentalEvent
    public static final int QQ_RECEIVE_TEMP_CLUSTER_IM = 96;
    
    /**
     * <code>QQ_RECEIVE_UNKNOWN_CLUSTER_IM</code>事件在收到一条未知类型群消息时发生，
     * source是ReceiveIMPacket
     */
	@DocumentalEvent
    public static final int QQ_RECEIVE_UNKNOWN_CLUSTER_IM = 97;
    
    /**
     * <code>QQ_RECEIVE_TEMP_SESSION_IM</code>事件在收到一条临时会话消息时发生，
     * source是ReceiveIMPacket
     */
	@DocumentalEvent
    public static final int QQ_RECEIVE_TEMP_SESSION_IM = 86;
    
    /**
     * <code>QQ_I_AM_ADDED_TO_CLUSTER</code>事件在别人把我加为群中成员时发生，别人可以是一开始就
     * 创建了群并我加入到群中，也可以是先创建了群，后来才加的我，反正都是触发这一个事件，source
     * 是ReceiveIMPacket
     */
	@DocumentalEvent
    public static final int QQ_I_AM_ADDED_TO_CLUSTER = 73;
    
    /**
     * <code>QQ_I_AM_REMOVED_FROM_CLUSTER</code>事件在群的创建者把我删除后发生，
     * source是ReceiveIMPacket。这个事件会在某个人退出群后，或者管理员删除某个人后
     * 发生，在第一种情况下，这个事件传达给管理员，在第二种情况下，这个事件传达给这个用户。
     * 所以，必须判断包中的sender QQ号，如果等于自己的QQ号，说明是自己被删除了，如果不
     * 等于，说明我自己是管理员，有个成员主动退出了
     */
	@DocumentalEvent
    public static final int QQ_I_AM_REMOVED_FROM_CLUSTER = 74;
    
    /**
     * <code>QQ_REQUEST_JOIN_CLUSTER</code>事件发生在有人想加入我创建的群时，source是ReceiveIMPacket
     */
	@DocumentalEvent
    public static final int QQ_REQUEST_JOIN_CLUSTER = 75;
    
    /**
     * <code>QQ_REQUEST_JOIN_CLUSTER_APPROVED</code>事件发生在别人同意了我加入他创建的群时，
     * source是ReceiveIMPacket
     */
	@DocumentalEvent
    public static final int QQ_REQUEST_JOIN_CLUSTER_APPROVED = 76;
    
    /**
     * <code>QQ_REQUEST_JOIN_CLUSTER_REJECTED</code>事件发生在别人拒绝了我加入他创建的群时，
     * source是ReceiveIMPacket
     */
	@DocumentalEvent
    public static final int QQ_REQUEST_JOIN_CLUSTER_REJECTED = 77;
    
    /**
     * <code>QQ_SEARCH_CLUSTER_SUCCESS</code>事件在搜索群成功时发生，source是ClusterCommandReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_SEARCH_CLUSTER_SUCCESS = 78;
    
    /**
     * <code>QQ_SEARCH_CLUSTER_FAIL</code>事件在搜索群失败时发生，这也许是搜索出错，也许是没有搜到
     * 任何结果等等，source是ClusterCommandReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_SEARCH_CLUSTER_FAIL = 79;
    
    /**
     * <code>QQ_REQUEST_KEY_SUCCESS</code>事件在请求密钥成功之后发生，其source是RequestKeyReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_REQUEST_KEY_SUCCESS = 82;
    
    /**
     * <code>QQ_REQUEST_KEY_FAIL</code>事件在请求密钥失败之后发生，其source是RequestKeyPacket
     */
	@DocumentalEvent
    public static final int QQ_REQUEST_KEY_FAIL = 83;
    
    /**
     * <code>QQ_SMS_SENT</code>事件发生在短消息发送出去之后，其source是SendSMSReplyPacket
     * 注意这个事件并不说明发送到底成功与否，我们需要检查SendSMSReplyPacket中的信息来判断
     */
	@DocumentalEvent
    public static final int QQ_SMS_SENT = 84;

    /** 
     * <code>QQ_RECEIVE_SMS</code>事件发生在收到手机短信后，其source是ReceiveIMPacket
     */
	@DocumentalEvent
    public static final int QQ_RECEIVE_SMS = 85;
    
    /**
     * <code>QQ_MODIFY_CLUSTER_MEMBER_SUCCESS</code>事件发生在修改群成员列表成功时，
     * 其source是ClusterCommandReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_MODIFY_CLUSTER_MEMBER_SUCCESS = 87;
    
    /**
     * <code>QQ_MODIFY_CLUSTER_MEMBER_FAIL</code>事件发生在修改群成员列表失败时，其
     * source是ClusterCommandReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_MODIFY_CLUSTER_MEMBER_FAIL = 88;
    
    /**
     * <code>QQ_RECEIVE_DUPLICATED_IM</code>事件在收到一个重复的消息时发生，其source是
     * ReceiveIMPacket。添加这个事件是为了解决有些消息的回复服务器收不到的问题
     */
	@DocumentalEvent
    public static final int QQ_RECEIVE_DUPLICATED_IM = 89;
    
    /**
     * <code>QQ_GET_LOGIN_TOKEN_SUCCESS</code>事件在请求得到登录令牌成功时发生，源是
     * RequestLoginTokenReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_GET_LOGIN_TOKEN_SUCCESS = 91;
    
    /**
     * <code>QQ_GET_LOGIN_TOKEN_FAIL</code>事件在请求得到登录令牌失败时发生，源是
     * RequestLoginTokenReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_GET_LOGIN_TOKEN_FAIL = 92;
    
    /**
     * <code>QQ_ADVANCED_SEARCH_USER_SUCCESS</code>事件在高级搜索成功时发生，源是
     * AdvancedSearchUserReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_ADVANCED_SEARCH_USER_SUCCESS = 93;
    
    /**
     * <code>QQ_ADVANCED_SEARCH_USER_END</code>事件在高级搜索结束时发生，源是
     * AdvancedSearchUserReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_ADVANCED_SEARCH_USER_END = 94;
    
    /**
     * <code>QQ_GET_TEMP_CLUSTER_INFO_SUCCESS</code>事件在得到临时群
     * 信息成功时发生，source是ClusterCommandReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_GET_TEMP_CLUSTER_INFO_SUCCESS = 98;
    
    /**
     * <code>QQ_GET_TEMP_CLUSTER_INFO_FAIL</code>事件在得到临时群
     * 信息失败时发生，source是ClusterCommandReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_GET_TEMP_CLUSTER_INFO_FAIL = 99;
    
    /**
     * <code>QQ_SEND_TEMP_CLUSTER_IM_SUCCESS</code>事件在发送临时群
     * 消息成功时发生，source是ClusterCommandReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_SEND_TEMP_CLUSTER_IM_SUCCESS = 102;
    
    /**
     * <code>QQ_SEND_TEMP_CLUSTER_IM_FAIL</code>事件在发送临时群
     * 消息失败时发生，source是ClusterCommandReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_SEND_TEMP_CLUSTER_IM_FAIL = 103;
    
    /**
     * <code>QQ_EXIT_TEMP_CLUSTER_SUCCESS</code>事件在退出临时群
     * 成功时发生，source是ClusterCommandReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_EXIT_TEMP_CLUSTER_SUCCESS = 104;
    
    /**
     * <code>QQ_EXIT_TEMP_CLUSTER_FAIL</code>事件在退出临时群
     * 失败时发生，source是ClusterCommandReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_EXIT_TEMP_CLUSTER_FAIL = 105;
    
    /**
     * <code>QQ_ACTIVATE_TEMP_CLUSTER_SUCCESS</code>事件在激活临时群
     * 成功时发生，source是ClusterCommandReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_ACTIVATE_TEMP_CLUSTER_SUCCESS = 108;
    
    /**
     * <code>QQ_ACTIVATE_TEMP_CLUSTER_FAIL</code>事件在激活临时群
     * 失败时发生，source是ClusterCommandReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_ACTIVATE_TEMP_CLUSTER_FAIL = 109;
    
    /**
     * <code>QQ_CREATE_TEMP_CLUSTER_SUCCESS</code>事件在创建临时群
     * 成功时发生，source是ClusterCommandReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_CREATE_TEMP_CLUSTER_SUCCESS = 110;
    
    /**
     * <code>QQ_CREATE_TEMP_CLUSTER_FAIL</code>事件在创建临时群
     * 失败时发生，source是ClusterCommandReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_CREATE_TEMP_CLUSTER_FAIL = 111;
    
    /**
     * <code>QQ_MODIFY_TEMP_CLUSTER_MEMBER_SUCCESS</code>事件在修改临时群成员
     * 成功时发生，source是ClusterCommandReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_MODIFY_TEMP_CLUSTER_MEMBER_SUCCESS = 112;
    
    /**
     * <code>QQ_MODIFY_TEMP_CLUSTER_MEMBER_FAIL</code>事件在修改临时群成员
     * 失败时发生，source是ClusterCommandReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_MODIFY_TEMP_CLUSTER_MEMBER_FAIL = 113;
    
    /**
     * <code>QQ_SEND_CLUSTER_IM_EX_SUCCESS</code>事件在发送扩展群消息成功时
     * 发生，source是ClusterCommandReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_SEND_CLUSTER_IM_EX_SUCCESS = 114;
    
    /**
     * <code>QQ_SEND_CLUSTER_IM_EX_FAIL</code>事件在发送扩展群消息失败时
     * 发生，source是ClusterCommandReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_SEND_CLUSTER_IM_EX_FAIL = 115;
    
    /**
     * <code>QQ_REQUEST_AGENT_REDIRECT</code>事件在请求中转服务器重定向时发生，source
     * 是RequestAgentReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_REQUEST_AGENT_REDIRECT = 117;
    
    /**
     * <code>QQ_REQUEST_AGENT_OK</code>事件在请求中转服务器成功时发生，source
     * 是RequestAgentReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_REQUEST_AGENT_SUCCESS = 118;
    
    /**
     * <code>QQ_REQUEST_AGENT_FAIL</code>事件在请求中转服务器成功时发生，source
     * 是RequestAgentReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_REQUEST_AGENT_FAIL = 123;
    
    /**
     * <code>QQ_REQUEST_BEGIN_SUCCESS</code>事件在请求开始传送成功时
     * 发生，source是RequestBeginReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_REQUEST_BEGIN_SUCCESS = 119;
    
    /**
     * <code>QQ_TRANSFER_FACE_INFO_SUCCESS</code>事件在传送表情文件信息或数据成功时
     * 发生，source是TransferReplyPacket。到底是对信息的回复还是对数据的回复，需要判断
     * 包中的session id，如果和当前session id相同，则是对信息的回复，否则是对数据的回复
     */
	@DocumentalEvent
    public static final int QQ_TRANSFER_FACE_SUCCESS = 120;
    
    /**
     * <code>QQ_REQUEST_FACE_SUCCESS</code>事件在请求自定义表情文件成功时发生，
     * source是RequestFaceReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_REQUEST_FACE_SUCCESS = 121;
    
    /** 
     * <code>QQ_RECEIVE_FACE_INFO</code>事件在接收到表情文件信息时发生，source
     * 是TransferReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_RECEIVE_FACE_INFO = 122;
    
    /** 
     * <code>QQ_RECEIVE_FACE_DATA</code>事件在接收到表情文件数据时发生，source
     * 是TransferReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_RECEIVE_FACE_DATA = 124;
    
    /** 
     * <code>QQ_GET_SUBJECT_LIST_SUCCESS</code>事件在子群操作成功时发生，
     * source是ClusterCommandReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_SUB_CLUSTER_OP_SUCCESS = 127;
    
    /** 
     * <code>QQ_GET_SUBJECT_LIST_FAIL</code>事件在子群操作失败时发生，
     * source是ClusterCommandReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_SUB_CLUSTER_OP_FAIL = 128;
    
    /** 
     * <code>QQ_UPDATE_ORGANIZATION_SUCCESS</code>事件在更新组织架构成功时发生，
     * source是ClusterCommandReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_UPDATE_ORGANIZATION_SUCCESS = 129;
    
    /** 
     * <code>QQ_UPDATE_ORGANIZATION_FAIL</code>事件在更新组织架构失败时发生，
     * source是ClusterCommandReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_UPDATE_ORGANIZATION_FAIL = 130;
    
    /** 
     * <code>QQ_COMMIT_ORGANIZATION_SUCCESS</code>事件在提交组织架构成功时发生，
     * source是ClusterCommandReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_COMMIT_ORGANIZATION_SUCCESS = 131;
    
    /** 
     * <code>QQ_COMMIT_ORGANIZATION_FAIL</code>事件在提交组织架构失败时发生，
     * source是ClusterCommandReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_COMMIT_ORGANIZATION_FAIL = 132;
    
    /** 
     * <code>QQ_COMMIT_MEMBER_ORGANIZATION_SUCCESS</code>事件在提交成员分组
     * 成功时发生，source是ClusterCommandReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_COMMIT_MEMBER_ORGANIZATION_SUCCESS = 133;
    
    /** 
     * <code>QQ_COMMIT_MEMBER_ORGANIZATON_FAIL</code>事件在提交成员分组
     * 失败时发生，source是ClusterCommandReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_COMMIT_MEMBER_ORGANIZATION_FAIL = 134;
    
    /** 
     * <code>QQ_MODIFY_TEMP_CLUSTER_INFO_SUCCESS</code>事件在修改临时群
     * 信息成功时发生，source是ClusterCommandReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_MODIFY_TEMP_CLUSTER_INFO_SUCCESS = 135;
    
    /** 
     * <code>QQ_MODIFY_TEMP_CLUSTER_INFO_FAIL</code>事件在修改临时群
     * 信息失败时发生，source是ClusterCommandReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_MODIFY_TEMP_CLUSTER_INFO_FAIL = 136;
    
    /** 
     * <code>QQ_MODIFY_CARD_SUCCESS</code>事件在修改群名片
     * 信息成功时发生，source是ClusterCommandReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_MODIFY_CARD_SUCCESS = 137;
    
    /** 
     * <code>QQ_MODIFY_CARD_FAIL</code>事件在修改群名片
     * 信息失败时发生，source是ClusterCommandReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_MODIFY_CARD_FAIL = 138;
    
    /** 
     * <code>QQ_GET_CARD_BATCH_SUCCESS</code>事件在批量得到群名片
     * 真实姓名成功时发生，source是ClusterCommandReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_GET_CARD_BATCH_SUCCESS = 139;
    
    /** 
     * <code>QQ_GET_CARD_BATCH_FAIL</code>事件在批量得到群名片
     * 真实姓名失败时发生，source是ClusterCommandReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_GET_CARD_BATCH_FAIL = 140;
    
    /** 
     * <code>QQ_GET_CARD_SUCCESS</code>事件在得到单个成员群名片
     * 成功时发生，source是ClusterCommandReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_GET_CARD_SUCCESS = 141;
    
    /** 
     * <code>QQ_GET_CARD_FAIL</code>事件在得到单个成员群名片
     * 失败时发生，source是ClusterCommandReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_GET_CARD_FAIL = 142;
    
    /**
     * <code>QQ_RECEIVE_ADVERTISEMENT</code>事件发生收到广告时，
     * source是SystemNotificationPacket
     */
	@DocumentalEvent
    public static final int QQ_RECEIVE_ADVERTISEMENT = 143;
    
    /**
     * <code>QQ_AUTHORIZE_SEND_SUCCESS</code>在验证消息发送成功时发生，
     * source是AuthorizeReplyPacket 
     */
	@DocumentalEvent
    public static final int QQ_AUTHORIZE_SEND_SUCCESS = 145;
    
    /**
     * <code>QQ_AUTHORIZE_SEND_FAIL</code>在验证消息发送失败时发生，
     * source是AuthorizeReplyPacket 
     */
	@DocumentalEvent
    public static final int QQ_AUTHORIZE_SEND_FAIL = 146;
    
    /**
     * <code>QQ_ADMIN_ENTITLED</code>事件在群创建者把自己设为管理员时发生,
     * source是ReceiveIMPacket
     */
	@DocumentalEvent
    public static final int QQ_ADMIN_ENTITLED = 147;
    
    /**
     * <code>QQ_ADMIN_WITHDRAWED</code>事件在群创建者把自己的管理员身份撤销时发生,
     * source是ReceiveIMPacket
     */
	@DocumentalEvent
    public static final int QQ_ADMIN_WITHDRAWED = 148;
    
    /**
     * <code>QQ_SET_ROLE_SUCCESS</code>事件在群创建者设置管理员成功时发生,
     * source是ClusterCommandReplyPacket，这个事件不一定是针对自己的，需要
     * 检查接收者是否为自己
     */
	@DocumentalEvent
    public static final int QQ_SET_ROLE_SUCCESS = 149;
    
    /**
     * <code>QQ_SET_ROLE_FAIL</code>事件在群创建者设置管理员失败时发生,
     * source是ClusterCommandReplyPacket这个事件不一定是针对自己的，需要
     * 检查接收者是否为自己
     */
	@DocumentalEvent
    public static final int QQ_SET_ROLE_FAIL = 150;
    
    /**
     * <code>QQ_TRANSFER_ROLE_SUCCESS</code>事件在群创建者转让身份成功时发生,
     * source是ClusterCommandReplyPacket, 这个事件不一定是针对自己的，需要
     * 检查接收者是否为自己
     */
	@DocumentalEvent
    public static final int QQ_TRANSFER_ROLE_SUCCESS = 151;
    
    /**
     * <code>QQ_TRANSFER_ROLE_FAIL</code>事件在群创建者转让身份失败时发生,
     * source是ClusterCommandReplyPacket, 这个事件不一定是针对自己的，需要
     * 检查接收者是否为自己
     */
	@DocumentalEvent
    public static final int QQ_TRANSFER_ROLE_FAIL = 152;
    
    /**
     * <code>QQ_MODIFY_SIGNATURE_SUCCESS</code>事件在修改个性签名成功时发生,
     * source是SignatureOpReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_MODIFY_SIGNATURE_SUCCESS = 153;
    
    /**
     * <code>QQ_MODIFY_SIGNATURE_FAIL</code>事件在修改个性签名失败时发生,
     * source是SignatureOpReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_MODIFY_SIGNATURE_FAIL = 154;
    
    /**
     * <code>QQ_DELETE_SIGNATURE_SUCCESS</code>事件在删除个性签名成功时发生,
     * source是SignatureOpReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_DELETE_SIGNATURE_SUCCESS = 155;
    
    /**
     * <code>QQ_DELETE_SIGNATURE_FAIL</code>事件在删除个性签名失败时发生,
     * source是SignatureOpReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_DELETE_SIGNATURE_FAIL = 156;
    
    /**
     * <code>QQ_GET_SIGNATURE_SUCCESS</code>事件在得到个性签名成功时发生,
     * source是SignatureOpReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_GET_SIGNATURE_SUCCESS = 157;
    
    /**
     * <code>QQ_GET_SIGNATURE_FAIL</code>事件在得到个性签名失败时发生,
     * source是SignatureOpReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_GET_SIGNATURE_FAIL = 158;
    
    /**
     * <code>QQ_FRIEND_SIGNATURE_CHANGED</code>事件在收到系统的个性签名改变通知时发生，
     * source是ReceiveIMPacket
     */
	@DocumentalEvent
    public static final int QQ_FRIEND_SIGNATURE_CHANGED = 159;
    
    /**
     * <code>QQ_GET_USER_PROPERTY_SUCCESS</code>事件在得到用户属性成功时发生，
     * source是UserPropertyOpReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_GET_USER_PROPERTY_SUCCESS = 160;
    
    /**
     * <code>QQ_GET_FRIEND_LEVEL_SUCCESS</code>事件在得到用户级别成功时发生，
     * source是FriendLevelOpPacket
     */
	@DocumentalEvent
    public static final int QQ_GET_FRIEND_LEVEL_SUCCESS = 162;
    
    /** 
     * <code>QQ_GET_WEATHER_SUCCESS</code>在得到天气预报成功时发生，source
     * 是WeatherOpReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_GET_WEATHER_SUCCESS = 166;
    
    /** 
     * <code>QQ_GET_WEATHER_FAIL</code>在得到天气预报失败时发生，source
     * 是WeatherOpReplyPacket，但是这种情况下这个包无可用信息
     */
	@DocumentalEvent
    public static final int QQ_GET_WEATHER_FAIL = 167;
    
    /** 
     * <code>QQ_SEND_TEMP_SESSOIN_IM_SUCCESS</code>在发送临时会话消息成功时
     * 发生，其source是TempSessionOpReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_SEND_TEMP_SESSOIN_IM_SUCCESS = 168;
    
    /** 
     * <code>QQ_SEND_TEMP_SESSION_IM_FAIL</code>在发送临时会话消息失败时
     * 发生，其source是TempSessionOpReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_SEND_TEMP_SESSION_IM_FAIL = 169;
    
    /** 
     * <code>QQ_PRIVACY_DATA_OP_SUCCESS</code>在隐私数据操作成功时发生，
     * source是PrivacyDataOpReplyPacket。为了知道具体是什么操作，用户需要
     * 判断source中的subCommand字段
     */
	@DocumentalEvent
    public static final int QQ_PRIVACY_DATA_OP_SUCCESS = 170;
    
    /** 
     * <code>QQ_PRIVACY_DATA_OP_SUCCESS</code>在隐私数据操作失败时发生，
     * source是PrivacyDataOpReplyPacket。为了知道具体是什么操作，用户需要
     * 判断source中的subCommand字段
     */
	@DocumentalEvent
    public static final int QQ_PRIVACY_DATA_OP_FAIL = 171;
    
    /** 
     * <code>QQ_RECEIVE_QQLIVE</code>在收到QQ直播消息时发生，source是ReceiveIMPacket
     */
	@DocumentalEvent
    public static final int QQ_RECEIVE_QQLIVE = 172;
    
    /** 
     * <code>QQ_RECEIVE_MEMBER_LOGIN_HINT</code>在收到会员登录提示时发生，source是ReceiveIMPacket
     */
	@DocumentalEvent
    public static final int QQ_RECEIVE_MEMBER_LOGIN_HINT = 173;
    
    /** 
     * <code>QQ_FRIEND_CUSTOM_HEAD_CHANGED</code>收到好友自定义头像变化通知时
     * 发生，source是ReceiveIMPacket
     */
	@DocumentalEvent
    public static final int QQ_FRIEND_CUSTOM_HEAD_CHANGED = 174;
    
    /** 
     * <code>QQ_FRIEND_PROPERTY_CHANGED</code>收到好友属性变化通知时
     * 发生，source是ReceiveIMPacket
     */
	@DocumentalEvent
    public static final int QQ_FRIEND_PROPERTY_CHANGED = 175;
    
    /** 
     * <code>QQ_GET_CUSTOM_HEAD_INFO_SUCCESS</code>在收到自定义头像信息
     * 成功时发生，source是GetCustomHeadInfoReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_GET_CUSTOM_HEAD_INFO_SUCCESS = 176;
    
    /** 
     * <code>QQ_GET_CUSTOM_HEAD_DATA_SUCCESS</code>在收到自定义头像数据
     * 成功时发生，source是GetCustomHeadDataReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_GET_CUSTOM_HEAD_DATA_SUCCESS = 177;
    
    /**
     * <code>QQ_03_OPERATION_TIMEOUT</code>在03协议族操作超时时发生，也就是请求包没有能收到回复，
     * source是要发送的那个包，通知QQEvent的operation字段表示了操作的类型。要注意超时事件和
     * Fail事件的不同，超时是指包没有收到任何确认，fail是指确认收到了，并且根据确认包的内容，
     * 操作失败了。由于不同的协议族中的命令可能值相同，所以目前只好为不同协议族的超时加上不同的事件
     */
	@DocumentalEvent
    public static final int QQ_03_OPERATION_TIMEOUT = 178;
    
    /**
     * <code>QQ_05_OPERATION_TIMEOUT</code>在05协议族操作超时时发生，也就是请求包没有能收到回复，
     * source是要发送的那个包，通知QQEvent的operation字段表示了操作的类型。要注意超时事件和
     * Fail事件的不同，超时是指包没有收到任何确认，fail是指确认收到了，并且根据确认包的内容，
     * 操作失败了。由于不同的协议族中的命令可能值相同，所以目前只好为不同协议族的超时加上不同的事件
     */
	@DocumentalEvent
    public static final int QQ_05_OPERATION_TIMEOUT = 179;
    
    /**
     * <code>QQ_DISK_OPERTION_TIMEOUT</code>在disk协议族操作超时时发生，也就是请求包没有能收到回复，
     * source是要发送的那个包，通知QQEvent的operation字段表示了操作的类型。要注意超时事件和
     * Fail事件的不同，超时是指包没有收到任何确认，fail是指确认收到了，并且根据确认包的内容，
     * 操作失败了。由于不同的协议族中的命令可能值相同，所以目前只好为不同协议族的超时加上不同的事件
     */
	@DocumentalEvent
    public static final int QQ_DISK_OPERATION_TIMEOUT = 180;
    
    /** 
     * <code>QQ_DISK_BEGIN_DISK_SESSION</code>在服务器要求客户端开始网络硬盘会话时发生，
     * source是BeginSessionPacket
     */
	@DocumentalEvent
    public static final int QQ_DISK_BEGIN_DISK_SESSION = 181;
    
    /** 
     * <code>QQ_DISK_AUTHENTICATE_SUCCESS</code>在网络硬盘认证成功时发生，
     * source是AuthenticateReplyPacket。这个事件实际上不能认为认证已经成功，
     * 还需要检查AuthenticateReplyPacket中的状态码，如果状态码为0，则成功
     */
	@DocumentalEvent
    public static final int QQ_DISK_AUTHENTICATE_SUCCESS = 182;
    
    /** 
     * <code>QQ_DISK_AUTHENTICATE_FAIL</code>在网络硬盘认证失败时发生，
     * source是DiskInPacket的子类，具体是哪个要判断命令
     */
	@DocumentalEvent
    public static final int QQ_DISK_AUTHENTICATE_FAIL = 183;
    
    /** 
     * <code>QQ_DISK_GET_DISK_SERVER_LIST_SUCCESS</code>在得到网络硬盘服务器列表成功时发生 ，
     * source是GetServerListReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_DISK_GET_DISK_SERVER_LIST_SUCCESS = 184;
    
    /** 
     * <code>QQ_GET_SHARED_DISK_SUCCESS</code>在得到共享网络硬盘成功时发生 ，
     * source是GetSharedDiskReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_DISK_GET_SHARED_DISK_SUCCESS = 185;
    
    /** 
     * <code>QQ_DISK_GET_SHARED_DISK_DIR_SUCCESS</code>在得到共享网络硬盘目录列表成功时发生，
     * source是ListDirReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_DISK_GET_SHARED_DISK_DIR_SUCCESS = 186;
    
    /** 
     * <code>QQ_DISK_GET_MY_DISK_DIR_SUCCESS</code>在得到我的网络硬盘目录列表成功时发生，
     * source是ListDirReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_DISK_GET_MY_DISK_DIR_SUCCESS = 187;
    
    /** 
     * <code>QQ_DISK_PASSWORD_OP_SUCCESS</code>在网络硬盘密码操作成功时发生，
     * source是PasswordOpPacket
     */
	@DocumentalEvent
    public static final int QQ_DISK_PASSWORD_OP_SUCCESS = 188;
    
    /** 
     * <code>QQ_DISK_PASSWORD_OP_FAIL</code>在网络硬盘密码操作失败时发生，
     * source是PasswordOpPacket
     */
	@DocumentalEvent
    public static final int QQ_DISK_PASSWORD_OP_FAIL = 189;
    
    /** 
     * <code>QQ_DISK_OPERATION_ERROR</code>在网络硬盘操作失败时发生，
     * source是DiskInPacket
     */
	@DocumentalEvent
    public static final int QQ_DISK_OPERATION_ERROR = 190;
    
    /** 
     * <code>QQ_DISK_DOWNLOAD_SUCCESS</code>在网下载网络硬盘文件成功时发生，
     * source是DownloadReplyPacket。注意这个事件只会在下载小文件是发生，因为对于
     * 大文件来说，这些包被拆成了分片，因此不会触发这个事件
     */
	@DocumentalEvent
    public static final int QQ_DISK_DOWNLOAD_SUCCESS = 191;
    
    /** 
     * <code>QQ_DISK_DOWNLOAD_FRAGMENT_SUCCESS</code>在下载网络硬盘文件成功时发生，
     * source是DiskInPacketFragment。这个事件并不能说明文件被下载完成，因为它只包含了
     * 文件的一个分片，这个事件会在下载大文件时发生，程序需要自己判断是否已经下载完所有分片
     */
	@DocumentalEvent
    public static final int QQ_DISK_DOWNLOAD_FRAGMENT_SUCCESS = 192;
    
    /** 
     * <code>QQ_DISK_RENAME_SUCCESS</code>在重命名文件或目录成功时发生，
     * source是RenamePacket
     */
	@DocumentalEvent
    public static final int QQ_DISK_RENAME_SUCCESS = 193;
    
    /** 
     * <code>QQ_DISK_DELETE_SUCCESS</code>在删除文件或目录成功时发生，
     * source是DeleteReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_DISK_DELETE_SUCCESS = 194;
    
    /** 
     * <code>QQ_DISK_CREATE_SUCCESS</code>在创建目录成功时发生，
     * source是CreateFolderReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_DISK_CREATE_SUCCESS = 195;
    
    /** 
     * <code>QQ_DISK_MOVE_SUCCESS</code>在移动文件或目录成功时发生，
     * source是MovePacket
     */
	@DocumentalEvent
    public static final int QQ_DISK_MOVE_SUCCESS = 196;
    
    /** 
     * <code>QQ_DISK_UPLOAD_SUCCESS</code>在上传文件成功时发生，
     * source是UploadFileReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_DISK_UPLOAD_SUCCESS = 197;
    
    /** 
     * <code>QQ_DISK_FINALIZE_SUCCESS</code>在结束上传成功时发生，
     * source是FinalizeReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_DISK_FINALIZE_SUCCESS = 198;
    
    /** 
     * <code>QQ_DISK_PREPARE_SUCCESS</code>在转变上传下载成功时发生，
     * source是PrepareReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_DISK_PREPARE_SUCCESS = 199;
    
    /** 
     * <code>QQ_DISK_GET_SIZE_SUCCESS</code>在得到文件大小成功时发生，
     * source是GetSizeReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_DISK_GET_SIZE_SUCCESS = 200;
    
    /** 
     * <code>QQ_DISK_GET_SHARE_LIST_SUCCESS</code>在得到共享列表成功时发生，
     * source是GetShareListReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_DISK_GET_SHARE_LIST_SUCCESS = 201;
    
    /** 
     * <code>QQ_DISK_SET_SHARE_LIST_SUCCESS</code>在设置共享列表成功时发生，
     * source是SetShareListReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_DISK_SET_SHARE_LIST_SUCCESS = 202;
    
    /** 
     * <code>QQ_RECEIVE_DISK_NOTIFICATION</code>在收到网络硬盘改变通知时发生，
     * source是ReceiveIMPacket
     */
	@DocumentalEvent
    public static final int QQ_RECEIVE_DISK_NOTIFICATION = 161;

    /** 
     * <code>QQ_DISK_APPLY_SUCCESS</code>在申请网络硬盘成功时发生，
     * source是ApplyReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_DISK_APPLY_SUCCESS = 163;
    
    /** 
     * <code>QQ_DISMISS_CLUSTER_SUCCESS</code>在解散群成功时发生，
     * source是ClusterCommandReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_DISMISS_CLUSTER_SUCCESS = 125;
    
    /** 
     * <code>QQ_DISMISS_CLUSTER_FAIL</code>在解散群失败时发生，
     * source是ClusterCommandReplyPacket
     */
	@DocumentalEvent
    public static final int QQ_DISMISS_CLUSTER_FAIL = 126;
    
    /*
     * 下一个可用的事件序号从203开始
     */
    
    /**
     * <code>QQ_CONNECTION_BROKEN</code>事件发生在连接被远程关闭时, Source是ErrorPacket
     */
	@DocumentalEvent
    public static final int QQ_CONNECTION_BROKEN = 9999;
    
    /**
     * <code>QQ_PROXY_ERROR</code>事件是一个代理事件，和QQ协议本身没有关系，其在代理发生错误时触发，
     * 其Source是ErrorPacket
     */
	@DocumentalEvent
    public static final int QQ_PROXY_ERROR = 10000;
    
    /**
     * <code>QQ_NETWORK_ERROR</code>事件在网络出错时发生，和QQ协议本身没有关系，其Source为
     * ErrorPacket
     */
	@DocumentalEvent
    public static final int QQ_NETWORK_ERROR = 10001;
	
    /**
     * <code>QQ_RUNTIME_ERROR</code>事件在发生运行时错误时触发，和QQ协议本身没有关系，其Source为
     * ErrorPacket。其portName为空，不可用。其errorMessage为崩溃报告。
     */
	@DocumentalEvent
	public static final int QQ_RUNTIME_ERROR = 10002;
    
    // QQ事件类型
    public int type;
    public int operation;

    /**
     * @param source
     */
    public QQEvent(Object source) {
        super(source);
    }
    
    /**
     * 缺省构造函数
     */
    public QQEvent() {
        this(new Object());
    }
}
