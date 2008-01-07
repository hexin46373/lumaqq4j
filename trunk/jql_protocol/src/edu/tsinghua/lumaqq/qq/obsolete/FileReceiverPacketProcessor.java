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

import static org.apache.commons.codec.digest.DigestUtils.md5;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.tsinghua.lumaqq.qq.QQ;
import edu.tsinghua.lumaqq.qq.Util;

/**
 * <pre>
 * 文件接收者的包处理类
 * </pre>
 * 
 * @author luma
 */
public class FileReceiverPacketProcessor {
    // Log对象
    static Log log = LogFactory.getLog(FileReceiverPacketProcessor.class);
    // FileReceiverPacketProcessor从属的FileReceiver对象
    private FileReceiver receiver;
    
    /**
     * @param fr
     */
    public FileReceiverPacketProcessor(FileReceiver fr) {
        receiver = fr;
    }
    
	/**
     * 处理网络事件
	 * @throws IOException
     */
    public void processSelectedKeys(ByteBuffer buffer) throws Exception {
		// 读取头部标志，判断包类型
		buffer.flip();
		if(buffer.get() == QQ.QQ_HEADER_03_FAMILY) {
		    buffer.rewind();
		    receiver.fdp.parse(buffer);
		    processFileDataPacket(receiver.fdp);
		} else {
		    buffer.rewind();
		    receiver.fcp.parse(buffer);
		    processFileControlPacket(receiver.fcp);
		}
    }
    
	// 处理文件控制信息包
	private void processFileControlPacket(FileControlPacket fcp) throws IOException {    
		switch(fcp.getCommand()) {
			case QQ.QQ_FILE_CMD_PING:
			    log.debug("收到Init Connection包");
				processInitConnection(fcp);
				break;
			case QQ.QQ_FILE_CMD_SENDER_SAY_HELLO:
			    log.debug("收到Sender Hello, HelloByte: " + fcp.getHelloByte());
			    processSenderSayHello(fcp);
				break;
			case QQ.QQ_FILE_CMD_SENDER_SAY_HELLO_ACK:
			    log.debug("收到Sender Hello Ack, HelloByte: " + fcp.getHelloByte());
			    break;
			case QQ.QQ_FILE_CMD_RECEIVER_SAY_HELLO:
			    log.debug("收到Receiver Hello, HelloByte: " + fcp.getHelloByte());
			    break;
			case QQ.QQ_FILE_CMD_RECEIVER_SAY_HELLO_ACK:
			    log.debug("收到Reciver Hello Ack， HelloByte: " + fcp.getHelloByte());
			    break;
		}
	}
	
	/**
     * @param packet
	 * @throws IOException
     */
    private void processSenderSayHello(FileControlPacket packet) throws IOException {
        packet.setCommand(QQ.QQ_FILE_CMD_SENDER_SAY_HELLO_ACK);
        packet.setHelloByte(packet.getHelloByte());
        packet.fill(receiver.buffer);
        receiver.buffer.flip();
        receiver.send();
        log.debug("Sender Hello Ack 已发送");
        packet.setCommand(QQ.QQ_FILE_CMD_RECEIVER_SAY_HELLO);
        packet.fill(receiver.buffer);
        receiver.buffer.flip();
        receiver.send();
        log.debug("Receiver Hello 已发送");
        // 设置提示信息
        if(!receiver.monitor.checkDuplicate(packet)) {
	        receiver.setFileTransferStatus(FileWatcher.FT_RECEIVING);
	        receiver.fireFileConnectedEvent();
        }
    }

    // 处理003D命令
	private void processInitConnection(FileControlPacket packet) throws IOException {
	    packet.setCommand(QQ.QQ_FILE_CMD_PONG);
	    packet.fill(receiver.buffer);
	    receiver.buffer.flip();
	    receiver.send();
	    log.debug("Init Connection Ack 已发送");
	}
    
	// 处理文件数据信息包
	private void processFileDataPacket(FileDataPacket fdp) throws Exception {
		switch(fdp.getCommand()) {
		    case QQ.QQ_FILE_CMD_HEART_BEAT:
		        log.debug("收到Heart Beat 序号: " + (int)fdp.getHeartBeatSequence());
		        // 返回heart beat的确认包
		    	fdp.setCommand(QQ.QQ_FILE_CMD_HEART_BEAT_ACK);
		    	fdp.fill(receiver.buffer);
		    	receiver.buffer.flip();
		    	receiver.send();
		        break;
		    case QQ.QQ_FILE_CMD_HEART_BEAT_ACK:
		        log.debug("收到Heart Beat回复，序号: " + (int)fdp.getHeartBeatSequence());
		        break;
		    case QQ.QQ_FILE_CMD_TRANSFER_FINISHED:
		        // 返回回复包
		    	fdp.setCommand(QQ.QQ_FILE_CMD_TRANSFER_FINISHED);
		    	fdp.fill(receiver.buffer);
		    	receiver.buffer.flip();
		    	receiver.send();
		    	if(!receiver.monitor.checkDuplicate(fdp)) {
			    	// 检查是否已经收到了全部分片，如果不是，表明对方取消了文件传送
			    	if(receiver.window.isFinished()) {
			    	    log.debug("文件传输正常结束");
			    	    receiver.finish();	    	     			    	    
 			    	} else {
			    	    log.debug("文件传输取消");
			    	    receiver.abort();
 			    	}
		    	}
		        break;
		    case QQ.QQ_FILE_CMD_FILE_OP:
		        processFileOp(fdp);
		        break;
		    case QQ.QQ_FILE_CMD_FILE_OP_ACK:
		        break;
		}
	}
	
	// 处理文件命令，文件命令又包含三种子命令
    private void processFileOp(FileDataPacket fdp) throws IOException {
        switch(fdp.getInfoType()) {
            case QQ.QQ_FILE_BASIC_INFO:
                log.debug("收到文件基本信息，文件名: " + fdp.getFileName() + " 大小:" + fdp.getFileSize() + " 分片数: " + fdp.getFragments());
                processFileBasicInfo(fdp);
                break;
            case QQ.QQ_FILE_DATA_INFO:
                processFileDataInfo(fdp);                 
                break;
            case QQ.QQ_FILE_EOF:
                log.debug("收到文件EOF信息包");
                processFileEOF(fdp);
                break;
        }
    }
    

    // 处理收到文件结尾信息事件
    private void processFileEOF(FileDataPacket packet) throws IOException {
    	// 检查是否已经收到了最后一个分片，如果不是，不回复这个EOF包
    	if(receiver.window.isFinished()) {
    	    packet.setCommand(QQ.QQ_FILE_CMD_FILE_OP_ACK);
    	    packet.setInfoType(QQ.QQ_FILE_EOF);
    	    packet.fill(receiver.buffer);
    	    receiver.buffer.flip();
    	    receiver.send();
    	    log.debug("File EOF 确认已发送");
    	    if(!receiver.monitor.checkDuplicate(packet))
    	        receiver.fireFileInProgressEvent();
    	}
    }

    // 处理收到文件数据信息事件
    private void processFileDataInfo(FileDataPacket packet) throws IOException {
    	// 写入分片数据到文件
        // 推入分片，如果返回false，说明这个分片不在期望接收范围内，不处理
        if(receiver.window.put(packet.getFragmentIndex())) {
            log.debug("得到分片数据，偏移: " + packet.getFragmentOffset() + " 分片号: " + packet.getFragmentIndex());
            receiver.saveFragment(packet.getFragmentData(), packet.getFragmentOffset());
	        // 触发事件，以便GUI能更新进度条
	        receiver.fireFileInProgressEvent();
	        // 不管是否已经收到了这个分片，都发送回复包
	        packet.setCommand(QQ.QQ_FILE_CMD_FILE_OP_ACK);
	    	packet.setInfoType(QQ.QQ_FILE_DATA_INFO);
		    packet.fill(receiver.buffer);
		    receiver.buffer.flip();
		    receiver.send();
		    log.debug("分片" + packet.getFragmentIndex() + "的确认已发送");
        }
    }

    // 处理收到文件基本信息事件
    private void processFileBasicInfo(FileDataPacket fdp) throws IOException {
        // 检查文件名MD5信息是否正确，不正确则不回复
        // TODO 可能实际的QQ处理不同，也许是返回一个错误码，这个还有待研究
        String fn = fdp.getFileName();
        byte[] fnByte = Util.getBytes(fn);
        if(Util.compareMD5(fdp.getFileNameMD5(), md5(fnByte))) {
            log.debug("文件名MD5正确，开始传送文件数据");
	        // 保存得到的文件基本信息
            receiver.setFileName(fn);
            receiver.setFileSize(fdp.getFileSize());
            receiver.setFragments(fdp.getFragments() - 1);
            receiver.setMaxFragmentSize(fdp.getFragmentMaxSize());
            receiver.setFileMD5(fdp.getFileMD5());
            receiver.setFileNameMD5(fdp.getFileNameMD5());
            receiver.initSlideWindow(1, 0, receiver.fragments);
	    	// 发送回复包
	    	fdp.setCommand(QQ.QQ_FILE_CMD_FILE_OP_ACK);
	    	fdp.setInfoType(QQ.QQ_FILE_BASIC_INFO);
		    fdp.fill(receiver.buffer);
		    receiver.buffer.flip();
		    receiver.send();
        }
    }
}
