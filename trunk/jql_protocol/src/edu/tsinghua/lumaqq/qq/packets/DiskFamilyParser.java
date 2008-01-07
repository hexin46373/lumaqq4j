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
package edu.tsinghua.lumaqq.qq.packets;

import java.nio.ByteBuffer;

import edu.tsinghua.lumaqq.qq.QQ;
import edu.tsinghua.lumaqq.qq.beans.QQUser;
import edu.tsinghua.lumaqq.qq.packets.in.disk.ApplyReplyPacket;
import edu.tsinghua.lumaqq.qq.packets.in.disk.AuthenticateReplyPacket;
import edu.tsinghua.lumaqq.qq.packets.in.disk.BeginSessionPacket;
import edu.tsinghua.lumaqq.qq.packets.in.disk.CreateReplyPacket;
import edu.tsinghua.lumaqq.qq.packets.in.disk.DeleteReplyPacket;
import edu.tsinghua.lumaqq.qq.packets.in.disk.DiskInPacketFragment;
import edu.tsinghua.lumaqq.qq.packets.in.disk.DownloadReplyPacket;
import edu.tsinghua.lumaqq.qq.packets.in.disk.FinalizeReplyPacket;
import edu.tsinghua.lumaqq.qq.packets.in.disk.GetServerListReplyPacket;
import edu.tsinghua.lumaqq.qq.packets.in.disk.GetShareListReplyPacket;
import edu.tsinghua.lumaqq.qq.packets.in.disk.GetSharedDiskReplyPacket;
import edu.tsinghua.lumaqq.qq.packets.in.disk.GetSizeReplyPacket;
import edu.tsinghua.lumaqq.qq.packets.in.disk.ListMyDiskDirReplyPacket;
import edu.tsinghua.lumaqq.qq.packets.in.disk.ListSharedDiskDirReplyPacket;
import edu.tsinghua.lumaqq.qq.packets.in.disk.MoveReplyPacket;
import edu.tsinghua.lumaqq.qq.packets.in.disk.PasswordOpReplyPacket;
import edu.tsinghua.lumaqq.qq.packets.in.disk.PrepareReplyPacket;
import edu.tsinghua.lumaqq.qq.packets.in.disk.RenameReplyPacket;
import edu.tsinghua.lumaqq.qq.packets.in.disk.SetShareListReplyPacket;
import edu.tsinghua.lumaqq.qq.packets.in.disk.UnknownDiskInPacket;
import edu.tsinghua.lumaqq.qq.packets.in.disk.UploadFileReplyPacket;
import edu.tsinghua.lumaqq.qq.packets.out.UnknownOutPacket;
import edu.tsinghua.lumaqq.qq.packets.out.disk.ApplyPacket;
import edu.tsinghua.lumaqq.qq.packets.out.disk.AuthenticatePacket;
import edu.tsinghua.lumaqq.qq.packets.out.disk.CreatePacket;
import edu.tsinghua.lumaqq.qq.packets.out.disk.DeletePacket;
import edu.tsinghua.lumaqq.qq.packets.out.disk.FinalizePacket;
import edu.tsinghua.lumaqq.qq.packets.out.disk.GetServerListPacket;
import edu.tsinghua.lumaqq.qq.packets.out.disk.GetShareListPacket;
import edu.tsinghua.lumaqq.qq.packets.out.disk.GetSharedDiskPacket;
import edu.tsinghua.lumaqq.qq.packets.out.disk.GetSizePacket;
import edu.tsinghua.lumaqq.qq.packets.out.disk.ListMyDiskDirPacket;
import edu.tsinghua.lumaqq.qq.packets.out.disk.ListSharedDiskDirPacket;
import edu.tsinghua.lumaqq.qq.packets.out.disk.MovePacket;
import edu.tsinghua.lumaqq.qq.packets.out.disk.PasswordOpPacket;
import edu.tsinghua.lumaqq.qq.packets.out.disk.PreparePacket;
import edu.tsinghua.lumaqq.qq.packets.out.disk.RenamePacket;
import edu.tsinghua.lumaqq.qq.packets.out.disk.SetShareListPacket;
import edu.tsinghua.lumaqq.qq.packets.out.disk.UnknownDiskOutPacket;
import edu.tsinghua.lumaqq.qq.packets.out.disk.UploadFilePacket;

/**
 * disk协议族包解析器
 *
 * @author luma
 */
public class DiskFamilyParser implements IParser {
	private int offset, length;
	private char command, source;
	private int remaining;
	private PacketHistory history;
	
	public DiskFamilyParser() {
		history = new PacketHistory();
		remaining = 0;
	}

	public boolean accept(ByteBuffer buf) {
		// 大于0说明一个超长包还没有解析完成
		if(remaining > 0)
			return buf.hasRemaining();
		
		// 可用长度小于包头长度的，拒绝
		offset = buf.position();
        int bufferLength = buf.limit() - offset;
        if(bufferLength < QQ.QQ_LENGTH_DISK_FAMILY_IN_HEADER)
        	return false;
        length = buf.getInt(offset) + 4;
        // 包长大于最大大小的，接受
        if(length > QQ.QQ_MAX_PACKET_SIZE) {
        	remaining = length;
        	source = buf.getChar(offset + 4);
        	command = buf.getChar(offset + 8);
        	return true;
        }
        // 可用长度小于包长的，拒绝
        if(bufferLength < length)
        	return false;
        
        return true;
	}

	public int getLength(ByteBuffer buf) {
		if(length > QQ.QQ_MAX_PACKET_SIZE) {
			return Math.min(remaining, buf.remaining());
		} else
			return length;
	}

	public InPacket parseIncoming(ByteBuffer buf, int length, QQUser user) throws PacketParseException {
		if(this.length > QQ.QQ_MAX_PACKET_SIZE) {
			// 相等说明这是第一个片断，所有有包头，要跳过
			int actLen = length;
			if(remaining == this.length) {
				buf.position(offset + QQ.QQ_LENGTH_DISK_FAMILY_IN_HEADER);
				actLen -= QQ.QQ_LENGTH_DISK_FAMILY_IN_HEADER;
			}
			// 如果解析失败，返回null
			DiskInPacketFragment fragment = null;
			try {
				fragment = new DiskInPacketFragment(buf, actLen, user);
				fragment.command = command;
				fragment.source = source;
				fragment.replyCode = QQ.QQ_REPLY_OK;
				remaining -= length;
			} catch(PacketParseException e) {
				fragment = null;
			}
			return fragment;
		} else {
			// 如果不是超长包，正常步骤解析
			try {
				switch(buf.getChar(offset + 8)) {
					case QQ.QQ_DISK_CMD_BEGIN_SESSION:
						return new BeginSessionPacket(buf, length, user);
					case QQ.QQ_DISK_CMD_AUTHENTICATE:
						return new AuthenticateReplyPacket(buf, length, user);
					case QQ.QQ_DISK_CMD_GET_SERVER_LIST:
						return new GetServerListReplyPacket(buf, length, user);
					case QQ.QQ_DISK_CMD_GET_SHARED_DISK:
						return new GetSharedDiskReplyPacket(buf, length, user);
					case QQ.QQ_DISK_CMD_LIST_SHARED_DISK_DIR:
						return new ListSharedDiskDirReplyPacket(buf, length, user);
					case QQ.QQ_DISK_CMD_LIST_MY_DISK_DIR:
						return new ListMyDiskDirReplyPacket(buf, length, user);
					case QQ.QQ_DISK_CMD_PASSWORD_OP:
						return new PasswordOpReplyPacket(buf, length, user);
					case QQ.QQ_DISK_CMD_DOWNLOAD:
						return new DownloadReplyPacket(buf, length, user);
					case QQ.QQ_DISK_CMD_RENAME:
						return new RenameReplyPacket(buf, length, user);
					case QQ.QQ_DISK_CMD_DELETE:
						return new DeleteReplyPacket(buf, length, user);
					case QQ.QQ_DISK_CMD_CREATE:
						return new CreateReplyPacket(buf, length, user);
					case QQ.QQ_DISK_CMD_MOVE:
						return new MoveReplyPacket(buf, length, user);
					case QQ.QQ_DISK_CMD_UPLOAD:
						return new UploadFileReplyPacket(buf, length, user);
					case QQ.QQ_DISK_CMD_FINALIZE:
						return new FinalizeReplyPacket(buf, length, user);
					case QQ.QQ_DISK_CMD_PREPARE:
						return new PrepareReplyPacket(buf, length, user);
					case QQ.QQ_DISK_CMD_GET_SIZE:
						return new GetSizeReplyPacket(buf, length, user);
					case QQ.QQ_DISK_CMD_GET_SHARE_LIST:
						return new GetShareListReplyPacket(buf, length, user);
					case QQ.QQ_DISK_CMD_SET_SHARE_LIST:
						return new SetShareListReplyPacket(buf, length, user);
					case QQ.QQ_DISK_CMD_APPLY:
						return new ApplyReplyPacket(buf, length, user);
					default:
						return new UnknownDiskInPacket(buf, length, user);
				}				
			} catch(PacketParseException e) {
				buf.position(offset);
				return new UnknownDiskInPacket(buf, length, user);
			}
		}
	}

	public OutPacket parseOutcoming(ByteBuffer buf, int length, QQUser user) throws PacketParseException {
		try {
			switch(buf.getChar(offset + 8)) {
				case QQ.QQ_DISK_CMD_AUTHENTICATE:
					return new AuthenticatePacket(buf, length, user);
				case QQ.QQ_DISK_CMD_GET_SERVER_LIST:
					return new GetServerListPacket(buf, length, user);
				case QQ.QQ_DISK_CMD_GET_SHARED_DISK:
					return new GetSharedDiskPacket(buf, length, user);
				case QQ.QQ_DISK_CMD_LIST_SHARED_DISK_DIR:
					return new ListSharedDiskDirPacket(buf, length, user);
				case QQ.QQ_DISK_CMD_LIST_MY_DISK_DIR:
					return new ListMyDiskDirPacket(buf, length, user);
				case QQ.QQ_DISK_CMD_PASSWORD_OP:
					return new PasswordOpPacket(buf, length, user);
				case QQ.QQ_DISK_CMD_RENAME:
					return new RenamePacket(buf, length, user);
				case QQ.QQ_DISK_CMD_DELETE:
					return new DeletePacket(buf, length, user);
				case QQ.QQ_DISK_CMD_CREATE:
					return new CreatePacket(buf, length, user);
				case QQ.QQ_DISK_CMD_MOVE:
					return new MovePacket(buf, length, user);
				case QQ.QQ_DISK_CMD_UPLOAD:
					return new UploadFilePacket(buf, length, user);
				case QQ.QQ_DISK_CMD_FINALIZE:
					return new FinalizePacket(buf, length, user);
				case QQ.QQ_DISK_CMD_PREPARE:
					return new PreparePacket(buf, length, user);
				case QQ.QQ_DISK_CMD_GET_SIZE:
					return new GetSizePacket(buf, length, user);
				case QQ.QQ_DISK_CMD_GET_SHARE_LIST:
					return new GetShareListPacket(buf, length, user);
				case QQ.QQ_DISK_CMD_SET_SHARE_LIST:
					return new SetShareListPacket(buf, length, user);
				case QQ.QQ_DISK_CMD_APPLY:
					return new ApplyPacket(buf, length, user);
				default:
					return new UnknownDiskOutPacket(buf, length, user);
			}
		} catch(PacketParseException e) {
			buf.position(offset);
			return new UnknownOutPacket(buf, length, user);
		}
	}

	public boolean isDuplicatedNeedReply(InPacket in) {
		return false;
	}

	public int relocate(ByteBuffer buf) {
		if(length > QQ.QQ_MAX_PACKET_SIZE) {
			int skip = Math.min(remaining, buf.remaining());
			remaining -= skip;
			return offset + skip;
		}
		
		int offset = buf.position();
		if(buf.remaining() < 4)
			return offset;
		int len = buf.getInt(offset) + 4;
		if(len == 0 || offset + len > buf.limit())
			return offset;
		else
			return offset + len;
	}

	public PacketHistory getHistory() {
		return history;
	}

	public boolean isDuplicate(InPacket in) {
		if(in instanceof DiskInPacketFragment || in instanceof UploadFileReplyPacket)
			return false;
		else
			return history.check(in, true);
	}
}
