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
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.tsinghua.lumaqq.qq.Crypter;
import edu.tsinghua.lumaqq.qq.QQ;
import edu.tsinghua.lumaqq.qq.packets.PacketParseException;

/**
 * <pre>
 * 文件传输时发送的包的基类，文件传送的包的包头为12个字节，格式为
 * 1. 一个字节表示包的类型，似乎控制数据是0x00，文件数据是0x03
 * 2. 发送方QQ版本，或者是服务器版本，这个字段和QQ的普通用途包是一样的
 * 3. 随机密钥，1字节，如果这个字节是0x23，那么密钥就是0x23232323，这个密钥用来加密
 *    发送者和接受者的QQ号
 * 4. 发送者QQ号的加密形式，4字节。加密算法: QQ号取反再与密钥异或
 * 5. 接受者QQ号的加密形式，4字节
 * </pre>
 * 
 *  * @author luma
 */
public abstract class FilePacket {
    // Log对象
    protected static Log log = LogFactory.getLog(FilePacket.class);
	// QQ版本
	protected char source;
	// 包的类型标志
	protected byte tag;
    // 加密解密类
    protected static Crypter crypter = new Crypter();
    // 随机数，用于生成密钥
    protected static Random random = new Random();
    // 随机密钥
    protected int key;
    // 发送者和接受者QQ号
    protected int sender, receiver;
    // file watcher
    protected FileWatcher watcher;
    
    /**
     * 构造一个FilePacket
     * @param watcher FileWatcher对象
     */
    protected FilePacket(FileWatcher watcher) {
        this.watcher = watcher;
    }
    
    /**
     * 内部刷新Key
     */
    protected int refreshKey() {
    	int seed = random.nextInt(256);
    	key = 0 | seed;
    	key |= seed << 8;
    	key |= seed << 16;
    	key |= seed << 24;
    	return key;
    }
    
    /**
     * 根据种子生成一个新的Key，内部的Key不会被刷新
     * @param b 种子
     */
    protected int generateKey(byte b) {
        int seed = b & 0xFF;
    	int key = 0 | seed;
    	key |= seed << 8;
    	key |= seed << 16;
    	key |= seed << 24;
    	return key;
    }

    /**
     * 解析包头，子类应该覆盖该方法，并首先调用父类的这个方法
     * 解析从in的当前位置开始，解析完毕后in的position将位于
     * 这个包之后
     * @param in ByteBuffer对象，做为输入缓冲区
     * @throws BadQQPacketException 如果包的长度不对
     */
    protected void parse(ByteBuffer in) throws PacketParseException {
        // 检查包长度是否满足最短长度
        if(in.limit() < QQ.QQ_LENGTH_FILE_PACKET_HEADER)
            throw new PacketParseException("文件信息包长度有误");
        // 解析包头部
        tag = in.get();
        source = in.getChar();
        key = generateKey(in.get());
        sender = decryptQQ(in.getInt(), key);
        receiver = decryptQQ(in.getInt(), key);
    }
    
    /**
     * <pre>
     * 初始化包内容，子类应该覆盖这个方法，并首先调用父类的这个方法
     * 输入ByteBuffer原来的内容将被清空，完成后，buffer的position
     * 等于包长
     * </pre>
     * @param out ByteBuffer对象，做为输出缓冲区
     */
    protected void fill(ByteBuffer out) {
        out.clear();
        putHead(out);
    }
    
    /**
     * <pre>
     * 初始化包内容，子类应该覆盖这个方法，并首先调用父类的这个方法
     * 输入ByteBuffer的原来内容不被清空，新的内容从from位置开始
     * 填充，完成后，buffer的position等于包长加上from
     * </pre>
     * @param out ByteBuffer对象，做为输出缓冲区
     * @param from 开始填充的位置
     */
    protected void fill(ByteBuffer out, int from) {
        out.position(from);
        putHead(out);
    }
    
    /**
     * 从out的当前位置开始填充包头部
     * @param out
     */
    private void putHead(ByteBuffer out) {
        // 初始化固定部分
        initFixedFields();
        // 填充头部
        out.put(tag)
        	.putChar(source)
        	.put((byte)(key & 0xFF))
        	.putInt(encryptQQ(sender, key))
        	.putInt(encryptQQ(receiver, key));
    }
    
    /**
     * 初始化一些对于固定类型的包来说固定值的字段
     */
    protected abstract void initFixedFields();

    /**
     * 加密QQ号
     * @param qq QQ号
     * @param key 密钥
     * @return 加密后的QQ号
     */
    protected int encryptQQ(int qq, int key) {
        qq = ~qq;
        qq ^= key;
        return qq;
    }
    
    /**
     * 解密QQ号
     * @param qq QQ号
     * @param key 密钥
     * @return 解密后的QQ号
     */
    protected int decryptQQ(int qq, int key) {
        qq ^= key;
        qq = ~qq;
        return qq;
    }
    
	/**
	 * @return Returns the source.
	 */
	public char getSource() {
		return source;
	}
	
	/**
	 * @param source The source to set.
	 */
	public void setSource(char source) {
		this.source = source;
	}
	
	/**
	 * @return Returns the type.
	 */
	public byte getTag() {
		return tag;
	}
	
	/**
	 * @param type The type to set.
	 */
	public void setTag(byte type) {
		this.tag = type;
	}

	/**
     * 调试用方法
     * @return 随机密钥
     */
    public int getKey() {
        return key;
    }
    
    /**
     * @return Returns the receiver.
     */
    public int getReceiver() {
        return receiver;
    }
    
    /**
     * @param receiver The receiver to set.
     */
    public void setReceiver(int receiver) {
        this.receiver = receiver;
    }
    
    /**
     * @return Returns the sender.
     */
    public int getSender() {
        return sender;
    }
    
    /**
     * @param sender The sender to set.
     */
    public void setSender(int sender) {
        this.sender = sender;
    }
}
