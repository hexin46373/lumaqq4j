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
package edu.tsinghua.lumaqq.qq.obsolete;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.tsinghua.lumaqq.qq.QQ;

/**
 * <pre>
 * 文件发送者的包处理器
 * </pre>
 * 
 * @author luma
 */
public class FileSenderPacketProcessor {
    // Log对象
    static Log log = LogFactory.getLog(FileSenderPacketProcessor.class);
    // FileSenderPacketProcessor从属的FileSender对象
    private FileSender sender;
    
    /**
     * 构造函数
     * @param sender
     */
    public FileSenderPacketProcessor(FileSender sender) {
        this.sender = sender;
    }

    /**
     * @param sk
     * @throws BadQQPacketException
     */
    public void processSelectedKeys(ByteBuffer buffer) throws Exception {   
		buffer.flip();
		if(buffer.get() == QQ.QQ_HEADER_03_FAMILY) {
		    buffer.rewind();
		    sender.fdp.parse(buffer);
		    processFileDataPacket(sender.fdp);
		} else {
		    buffer.rewind();
		    sender.fcp.parse(buffer);
		    processFileControlPacket(sender.fcp);
		}
    }
    
	// 处理文件数据信息包
	private void processFileDataPacket(FileDataPacket fdp) throws Exception {
		switch(fdp.getCommand()) {
		    case QQ.QQ_FILE_CMD_HEART_BEAT:
		        log.debug("收到Heart Beat 序号: " + (int)fdp.getHeartBeatSequence());
		        // 返回heart beat的确认包
		    	fdp.setCommand(QQ.QQ_FILE_CMD_HEART_BEAT_ACK);
		    	fdp.setHeartBeatSequence(fdp.getHeartBeatSequence());
		    	fdp.fill(sender.buffer);
		    	sender.buffer.flip();
		    	sender.send();
		        break;
		    case QQ.QQ_FILE_CMD_HEART_BEAT_ACK:
		        log.debug("收到Heart Beat回复，序号: " + (int)fdp.getHeartBeatSequence());
		    	sender.hbThread.setMaxReply(fdp.getHeartBeatSequence());
		        break;
		    case QQ.QQ_FILE_CMD_TRANSFER_FINISHED:
		        // 返回回复包
		    	fdp.setCommand(QQ.QQ_FILE_CMD_TRANSFER_FINISHED);
		    	fdp.fill(sender.buffer);
		    	sender.buffer.flip();
		    	sender.send();
		    	if(!sender.monitor.checkDuplicate(fdp)) {
			    	// 检查是否已经收到了全部分片，如果不是，表明对方取消了文件传送
			    	if(sender.window.isFinished()) {
			    	    log.debug("文件传输正常结束");
			    	    sender.finish();		    	    
			    	} else {
				        log.debug("文件传输取消");
			    	    sender.abort();
			    	}
		    	}
		        break;
		    case QQ.QQ_FILE_CMD_FILE_OP_ACK:
		    	processFileOpAck(fdp);
		        break;
		}
	}
    
	// 处理文件操作的回复包
    private void processFileOpAck(FileDataPacket fdp) {
        switch(fdp.getInfoType()) {
            case QQ.QQ_FILE_BASIC_INFO:
                processFileBasicInfoAck(fdp);
                break;
            case QQ.QQ_FILE_DATA_INFO:
                processFileDataInfoAck(fdp);
                break;
            case QQ.QQ_FILE_EOF:
                processFileEOFAck(fdp);
                break;
        }
    }

    // 处理文件EOF信息回复包
    private void processFileEOFAck(FileDataPacket fdp) {
        log.debug("收到文件EOF信息回复");
        if(!sender.monitor.checkDuplicate(fdp)) {
            sender.sendFinish(fdp, sender.buffer);
        }
    }

    // 处理文件数据信息回复包
    private void processFileDataInfoAck(FileDataPacket fdp) {
        // 首先检查是否重复
        if(!sender.monitor.checkDuplicate(fdp)) {
            // 然后检查序号是否在预期的范围内，因为heart beat线程也可能访问滑窗对象，所以要同步
            synchronized(sender.window) {                
	            if(sender.window.put(fdp.getFragmentIndex())) {
	                log.debug("收到分片" + fdp.getFragmentIndex() + "的确认");
	                // 如果put成功，相应的调整分片缓冲
	                sender.fb.release(sender.window.getIncrement());
	                // 触发事件
	                sender.fireFileInProgressEvent();
	                // 如果所有的分片都发送完了，调整当然状态，并发送EOF信息
	                //    如果否，发送后面的分片
	                if(sender.window.isFinished()) {
	                    sender.fileTransferStatus = FileWatcher.FT_SENDING_EOF;
	                    sender.sendEOF(fdp, sender.buffer);
	                } else
	                    sender.sendFragment(fdp, sender.buffer);
	            }                
            }
        }
    }

    // 处理文件基本信息回复包
    private void processFileBasicInfoAck(FileDataPacket fdp) {
        log.debug("收到文件基本信息回复");
        if(!sender.monitor.checkDuplicate(fdp)) {
	        sender.fileTransferStatus = FileWatcher.FT_SENDING;
	    	sender.sendFragment(fdp, sender.buffer);            
        }
    }

    // 处理文件控制信息包
	private void processFileControlPacket(FileControlPacket fcp) throws IOException {    
		switch(fcp.getCommand()) {
			case QQ.QQ_FILE_CMD_SENDER_SAY_HELLO:
			    log.debug("收到Sender Hello, HelloByte: " + fcp.getHelloByte());
				break;
			case QQ.QQ_FILE_CMD_SENDER_SAY_HELLO_ACK:
			    log.debug("收到Sender Hello Ack, HelloByte: " + fcp.getHelloByte());
			    break;
			case QQ.QQ_FILE_CMD_RECEIVER_SAY_HELLO:
			    log.debug("收到Receiver Hello, HelloByte: " + fcp.getHelloByte());
				processReceiverSayHello(fcp);
			    break;
			case QQ.QQ_FILE_CMD_RECEIVER_SAY_HELLO_ACK:
			    log.debug("收到Reciver Hello Ack， HelloByte: " + fcp.getHelloByte());
			    break;
		}
	}

    /**
     * @param fcp
     */
    private void processReceiverSayHello(FileControlPacket fcp) {
        // 发送回复
        fcp.setCommand(QQ.QQ_FILE_CMD_RECEIVER_SAY_HELLO_ACK);
        fcp.setHelloByte(fcp.getHelloByte());
        fcp.fill(sender.buffer);
        sender.buffer.flip();
        sender.send();
        // 如果这个包已经收到过，不处理
        if(!sender.monitor.checkDuplicate(fcp)) {
	        // 标记状态为传送中
            sender.fileTransferStatus = FileWatcher.FT_SENDING_BASIC;
        }
    }
}
