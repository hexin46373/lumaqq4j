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

import java.nio.ByteBuffer;

import edu.tsinghua.lumaqq.qq.QQ;
import edu.tsinghua.lumaqq.qq.Util;
import edu.tsinghua.lumaqq.qq.packets.PacketParseException;

/**
 * <pre>
 * 文件操作信息包，这个包里面携带了文件的一些基本信息，格式为
 * 1. 头部
 * 2. 未知字节，0x00
 * 3. 2字节操作类型，后续格式为
 *    1. 操作类型为0x1时，可能是表示开始传送文件基本信息，格式为
 *       i.  三个未知字节，全0
 *    2. 操作类型为0x2时，可能是表示开始传送文件的数据，格式和0x1时相同
 *    3. 操作类型为0x3时，可能时表示传送结束，格式和0x1时相同
 *    4. 如果操作类型为0x7，表示文件相关操作，根据子操作类型内容有所不同，格式为
 *       a. 当子操作类型为0x1时，表示文件基本信息传输，格式为
 * 	  	 	i.   两个字节，为全0，作用未知
 * 	     	ii.  子操作类型字节0x1
 *       	iii. 文件长度，四字节
 *       	iv.  文件数据分片数，一次传送1000字节，所以这个域是根据iii计算出来的，4字节
 *       	v.   每个分片的最大长度，4字节，一般都是1000字节一个分片，只有最后一个不是最大字节数
 *       	vi.  16个字节的md5，对于较小的文件，这个就是全部文件的md5，对于超过10002432字节的
 *               文件，这个MD5是前10002432字节的MD5。太变态了，为什么选这么一个没规律的数字。找
 *               的我头都晕了。
 *          vii. 文件名的MD5，16字节
 *       	viii. 文件名字节长度，2字节
 *       	ix.  八个未知字节，全0
 *       	x.   文件名
 *       b. 如果子操作类型为0x2，表示文件数据传输，格式为
 *       	i.   消息包的顺序号
 *       	ii.  子操作类型字节0x2
 *       	iii. 分片序号，4字节
 *       	iv.  当前分片在文件中的绝对偏移， 4字节
 *       	v.   当前包含的数据字节数，2字节
 *       	vi.  实际的数据
 *       c. 如果子操作类型为0x3，表示文件传送结束，格式为
 *       	i.   最大分片序号加1，例如如果文件分成两个分片发送，那么这里就是3，2字节
 *       	ii.  子操作类型0x3
 *    5. 操作类型为0x8时，为0x7操作的回复包，根据子操作类型不同而不同，格式为
 * 	  	 a. 如果子操作类型为0x1，表示确认文件基本信息已经收到，格式为
 *    	 	i.   未知两个字节，0x0000
 *    	 	ii.  子操作类型字段，0x1，1字节
 *    	 	iii. 未知的四字节，全0
 *    	 b. 如果子操作类型为0x2，表示已经收到了文件分片，格式为
 *    	 	i.   包顺序号
 *    	 	ii.  子操作类型字段，0x2，1字节
 *    	 	iii. 分片序号，4字节
 *    	 c. 如果子操作类型是0x3，表示已经收到了结束信息，格式为
 *    	 	i.   最大的分片序号再加1，2字节
 *    	 	ii.  子操作类型字段，0x3，1字节
 * 
 * 注意：这种包都不是加密的
 * </pre>
 * 
 * @author luma
 */
public class FileDataPacket extends FilePacket {
    // 命令
    private char command;
    // 包携带的信息类型
	private byte infoType;
	// 文件基本信息字段
	private int fileSize;
	private int fragments;
	private String fileName;
	// 文件数据信息字段
	private int packetIndex;
	private int fragmentIndex;
	private int fragmentOffset;
	private int fragmentLength;
	private int fragmentMaxSize;
	private byte[] fileMD5;
	private byte[] fileNameMD5;
	private byte[] fragmentData;
	// heart beat 字段
	private char heartBeatSequence;
	
	/**
	 * 构造一个发送包
     * @param watcher FileWatcher对象
	 */
	public FileDataPacket(FileWatcher watcher) {
		super(watcher);
	}

	/* (non-Javadoc)
	 * @see edu.tsinghua.lumaqq.qq.filetrans.FilePacket#initContent(java.nio.ByteBuffer)
	 */
	protected void fill(ByteBuffer out) {
	    // 调用父类的initContent填充包头
	    super.fill(out);
	    // 填充包体
	    putBody(out);
	}
	
	/* (non-Javadoc)
	 * @see edu.tsinghua.lumaqq.qq.filetrans.FilePacket#fill(java.nio.ByteBuffer, int)
	 */
	protected void fill(ByteBuffer out, int from) {
	    // 调用父类的initContent填充包头
	    super.fill(out, from);
	    // 填充包体
	    putBody(out);
	}
	
	/**
	 * 填充包体
	 * @param out
	 */
	private void putBody(ByteBuffer out) {
	    // 未知字节
	    out.put((byte)0);
	    // 命令
	    out.putChar(command);
	    // 判断命令类型和信息类型，调用不同的填充函数
	    switch(command) {
	        case QQ.QQ_FILE_CMD_FILE_OP:
				if(infoType == QQ.QQ_FILE_BASIC_INFO)
					initBasicInfo(out);
				else if(infoType == QQ.QQ_FILE_DATA_INFO)
					initDataInfo(out);
				else if(infoType == QQ.QQ_FILE_EOF)
					initEOFInfo(out);			
				break;
	        case QQ.QQ_FILE_CMD_FILE_OP_ACK:
				if(infoType == QQ.QQ_FILE_BASIC_INFO)
					initBasicInfoAck(out);
				else if(infoType == QQ.QQ_FILE_DATA_INFO)
					initDataInfoAck(out);
				else if(infoType == QQ.QQ_FILE_EOF)
					initEOFInfoAck(out);
				break;
		    case QQ.QQ_FILE_CMD_HEART_BEAT:
		    case QQ.QQ_FILE_CMD_HEART_BEAT_ACK:
		        out.putChar(heartBeatSequence);
		    	out.put((byte)0);
				break;
		    case QQ.QQ_FILE_CMD_TRANSFER_FINISHED:
		        out.putChar((char)0);
		    	out.put((byte)0);
			    break;
			default:
			    log.error("未知命令");
				break;
	    }
	}

	// 初始化结束信息回复包
	private void initEOFInfoAck(ByteBuffer out) {
		// 最大分片序号加1
		out.putChar((char)packetIndex);
		// 子操作类型字节
		out.put(infoType);
		// 四个未知字节，全0
		out.putInt(0);
	}

	// 初始化数据信息回复包
	private void initDataInfoAck(ByteBuffer out) {
		// 刚收到的分片序号
		out.putChar((char)packetIndex);
		// 子操作类型
		out.put(infoType);
		// 之前已经收到的最大分片序号
		out.putInt(fragmentIndex);
	}

	// 初始化基本信息回复包
	private void initBasicInfoAck(ByteBuffer out) {
		// 两个未知字节全0
		out.putChar((char)0);
		// 子操作类型
		out.put(infoType);
		// 四个未知字节，全0
		out.putInt(0);
	}

    /**
	 * 初始化文件结束信息包
	 */
	private void initEOFInfo(ByteBuffer out) {
		// 最大分片序号加1
		out.putChar((char)(watcher.getFragments() + 1));
		// 子操作类型类型字节
		out.put(infoType);
	}

	/**
	 * 初始化数据信息包
	 */
	private void initDataInfo(ByteBuffer out) {
		// 当前分片序号
		out.putChar((char)packetIndex);
		// 子操作类型
		out.put(infoType);
		// 已经发送成功的最大分片序号
		out.putInt(fragmentIndex);
		// 分片的绝对偏移
		out.putInt(fragmentOffset);
		// 当前分片的长度
		out.putChar((char)fragmentLength);
		// 当前分片数据
		out.put(fragmentData);
	}

	/**
	 * 初始化基本信息包
	 */
	private void initBasicInfo(ByteBuffer out) {
		// 未知2字节，0x0000
		out.putChar((char)0);
		// 子操作类型
		out.put(infoType);
		// 文件长度
		out.putInt(watcher.getFileSize());
		// 文件分片数
		out.putInt(watcher.getFileSize() / watcher.getMaxFragmentSize() + 1);
		// 分片长度
		out.putInt(watcher.getMaxFragmentSize());
		// 两个md5
		out.put(watcher.getFileMD5());
		out.put(watcher.getFileNameMD5());
		// 文件名长度，2字节
		byte[] b = watcher.getFileName().getBytes();
		out.putChar((char)b.length);
		// 未知的8字节，全0
		out.putLong(0);
		// 文件名
		out.put(b);
	}

	/* (non-Javadoc)
	 * @see edu.tsinghua.lumaqq.qq.filetrans.FilePacket#parseContent(java.nio.ByteBuffer)
	 */
	protected void parse(ByteBuffer in) throws PacketParseException {
	    super.parse(in);
	    // 跳过一个未知字节
	    in.get();
	    // 读取command
	    command = in.getChar();
	    switch(command) {
			case QQ.QQ_FILE_CMD_FILE_OP:
			    parseFileOp(in);	
				break;
			case QQ.QQ_FILE_CMD_FILE_OP_ACK:
			    parseFileOpAck(in);
				break;
			case QQ.QQ_FILE_CMD_HEART_BEAT:
			    parseHeartBeat(in);
				break;
			case QQ.QQ_FILE_CMD_HEART_BEAT_ACK:
			    parseHeartBeatAck(in);
				break;
			case QQ.QQ_FILE_CMD_TRANSFER_FINISHED:
			    parseTransferFinished(in);
				break;
	    }
	}
	
    /**
     * 解析文件操作的回复包
     * @param in
     */
    private void parseFileOpAck(ByteBuffer in) {
        // 读取分片序号
        packetIndex = in.getChar();
        // 读取信息类型
        infoType = in.get();
		if(infoType == QQ.QQ_FILE_BASIC_INFO)
		    parseBasicInfoAck(in);
		else if(infoType == QQ.QQ_FILE_DATA_INFO)
		    parseDataInfoAck(in);
		else if(infoType == QQ.QQ_FILE_EOF)
		    parseFileEOFAck(in);
    }

    /**
	 * 解析文件操作包
     * @param in
     */
    private void parseFileOp(ByteBuffer in) {
        // 读取分片序号
        packetIndex = in.getChar();
        // 读取信息类型
        infoType = in.get();
		if(infoType == QQ.QQ_FILE_BASIC_INFO)
		    parseBasicInfo(in);
		else if(infoType == QQ.QQ_FILE_DATA_INFO) 
		    parseDataInfo(in);   
    }

    // 处理文件传输完毕包
    private void parseTransferFinished(ByteBuffer in) {    
        // 没有什么要做的
    }

    // 解析heart beat回复包
    private void parseHeartBeatAck(ByteBuffer in) {
        // heart beat序号
        heartBeatSequence = in.getChar();
    }

    // 解析heart beat包
    private void parseHeartBeat(ByteBuffer in) {
        // heart beat 序号
        heartBeatSequence = in.getChar();
    }

    // 解析文件结束回复包
    private void parseFileEOFAck(ByteBuffer in) {
        // 没有什么要做的
    }

    // 解析文件数据信息回复包
    private void parseDataInfoAck(ByteBuffer in) {
		// 之前已经收到的最大分片序号
		fragmentIndex = in.getInt();
    }

    // 解析文件基本信息回复包
    private void parseBasicInfoAck(ByteBuffer in) {
        // 没有什么要做的
    }

    // 解析文件数据信息包
    private void parseDataInfo(ByteBuffer in) {
		// 已经发送的分片最大序号
		fragmentIndex = in.getInt();
		// 分片偏移
		fragmentOffset = in.getInt();
		// 分片长度
		fragmentLength = in.getChar();
		// 分片内容
		fragmentData = new byte[fragmentLength];
		in.get(fragmentData);
    }

    // 解析文件基本信息包
    private void parseBasicInfo(ByteBuffer in) {
		// 文件长度
		fileSize = in.getInt();
		// 分片数
		fragments = in.getInt();
		// 分片长度
		fragmentMaxSize = in.getInt();
		// 文件MD5
		fileMD5 = new byte[16];
		in.get(fileMD5);
		// 文件名MD5
		fileNameMD5 = new byte[16];
		in.get(fileNameMD5);
		// 文件名长度
		int len = in.getChar();
		// 8个未知字节，全0
		in.getLong();
		// 文件名
		byte[] b = new byte[len];
		in.get(b);
		fileName = Util.getString(b);
    }
	
	/**
	 * @return Returns the fileSize.
	 */
	public int getFileSize() {
		return fileSize;
	}
	
	/**
	 * @param fileSize The fileSize to set.
	 */
	public void setFileSize(int fileSize) {
		this.fileSize = fileSize;
		fragments = fileSize / watcher.getMaxFragmentSize() + 1;
	}
	
	/**
	 * @return Returns the fileName.
	 */
	public String getFileName() {
		return fileName;
	}
	
	/**
	 * @param fileName The fileName to set.
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	/**
	 * @return Returns the fragment.
	 */
	public int getPacketIndex() {
		return packetIndex;
	}
	
	/**
	 * @param packetIndex The fragment to set.
	 */
	public void setPacketIndex(int packetIndex) {
		this.packetIndex = packetIndex;
	}
	
	/**
	 * @return Returns the fragmentOffset.
	 */
	public int getFragmentOffset() {
		return fragmentOffset;
	}
	
	/**
	 * @param fragmentOffset The fragmentOffset to set.
	 */
	public void setFragmentOffset(int fragmentOffset) {
		this.fragmentOffset = fragmentOffset;
	}
	
	/**
	 * @return Returns the fragmentSize.
	 */
	public int getFragmentLength() {
		return fragmentLength;
	}
	
	/**
	 * @param fragmentSize The fragmentSize to set.
	 */
	public void setFragmentLength(int fragmentSize) {
		this.fragmentLength = fragmentSize;
	}
	
	/**
	 * @return Returns the framgentData.
	 */
	public byte[] getFragmentData() {
		return fragmentData;
	}
	
	/**
	 * @param framgentData The framgentData to set.
	 */
	public void setFragmentData(byte[] framgentData) {
		this.fragmentData = framgentData;
	}
	
	/**
	 * @return Returns the infoType.
	 */
	public byte getInfoType() {
		return infoType;
	}
	
	/**
	 * @param infoType The infoType to set.
	 */
	public void setInfoType(byte infoType) {
		this.infoType = infoType;
	}
	
	/**
	 * @return Returns the fragments.
	 */
	public int getFragments() {
		return fragments;
	}
	
	/**
	 * @return Returns the fragmentReceived.
	 */
	public int getFragmentIndex() {
		return fragmentIndex;
	}
	
	/**
	 * @param fragmentIndex The fragmentReceived to set.
	 */
	public void setFragmentIndex(int fragmentIndex) {
		this.fragmentIndex = fragmentIndex;
	}
	
    /**
     * @return Returns the heartBeatSequence.
     */
    public char getHeartBeatSequence() {
        return heartBeatSequence;
    }
    
    /**
     * @param heartBeatSequence The heartBeatSequence to set.
     */
    public void setHeartBeatSequence(char heartBeatSequence) {
        this.heartBeatSequence = heartBeatSequence;
    }
    
    /**
     * @return Returns the firstMD5.
     */
    public byte[] getFileMD5() {
        return fileMD5;
    }
    
    /**
     * @param firstMD5 The firstMD5 to set.
     */
    public void setFileMD5(byte[] firstMD5) {
        this.fileMD5 = firstMD5;
    }
    
    /**
     * @return Returns the fragmentMaxSize.
     */
    public int getFragmentMaxSize() {
        return fragmentMaxSize;
    }
    
    /**
     * @param fragmentMaxSize The fragmentMaxSize to set.
     */
    public void setFragmentMaxSize(int fragmentMaxSize) {
        this.fragmentMaxSize = fragmentMaxSize;
    }
    
    /**
     * @return Returns the secondMD5.
     */
    public byte[] getFileNameMD5() {
        return fileNameMD5;
    }
    
    /**
     * @param secondMD5 The secondMD5 to set.
     */
    public void setFileNameMD5(byte[] secondMD5) {
        this.fileNameMD5 = secondMD5;
    }

    /* (non-Javadoc)
     * @see edu.tsinghua.lumaqq.qq.filetrans.FilePacket#initFixedContent()
     */
    protected void initFixedFields() {
        tag = QQ.QQ_HEADER_03_FAMILY;
        source = QQ.QQ_CLIENT_VERSION;
        refreshKey();
        sender = watcher.getMyQQ();
        receiver = watcher.getHisQQ();
    }
    
    /**
     * @return Returns the command.
     */
    public char getCommand() {
        return command;
    }
    
    /**
     * @param command The command to set.
     */
    public void setCommand(char command) {
        this.command = command;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        if(command == QQ.QQ_FILE_CMD_FILE_OP || command == QQ.QQ_FILE_CMD_FILE_OP_ACK)
            return (command << 24) | (infoType << 16) | packetIndex;
        else
            return (command << 24) | heartBeatSequence;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if(obj instanceof FileDataPacket) {
            FileDataPacket packet = (FileDataPacket)obj;
            if(packet.command == QQ.QQ_FILE_CMD_FILE_OP || packet.command == QQ.QQ_FILE_CMD_FILE_OP_ACK)
                return packet.command == command && packet.infoType == infoType && packet.packetIndex == packetIndex;
            else
                return packet.command == command && packet.heartBeatSequence == heartBeatSequence;
        } else
            return false;
    }
}
