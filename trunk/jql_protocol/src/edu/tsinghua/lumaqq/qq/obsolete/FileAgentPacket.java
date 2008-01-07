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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.tsinghua.lumaqq.qq.Crypter;
import edu.tsinghua.lumaqq.qq.QQ;
import edu.tsinghua.lumaqq.qq.packets.PacketParseException;

/**
 * <pre>
 * 文件中转包，这个包的格式和FilePacket的子类格式相同甚少，因此是一个独立的类，它
 * 需要使用FileDataPacket，因为它是中转包，会包含中转数据。当它不用来中转数据时，
 * 也有自己的格式。具体格式如下：
 * 
 * 文件中转请求包(从客户端发出)以0x04开头，内容如下:
 * 1. 0x04
 * 2. 客户端版本号，2字节
 * 3. 整个的包长，2字节
 * 4. 命令，2字节
 * 5. 序号，2字节
 * 6. 我的QQ号，4字节
 * 7. 未知的8字节，无规律
 * 8. 根据命令的不同，后面的内容不同:
 *    a. 如果命令是0x0001时，表示请求中转服务，格式为
 *        i.   认证令牌长度，2字节
 *        ii.  认证令牌
 *        iii. 未知2字节，0x07D0，十进制值2000 (从这个字段开始加密)
 * 		  iv.  文件传输另一方的QQ号，4字节
 * 	  b. 如果命令是0x0002，表示接收者向中转服务器报到，格式为:
 * 		  i.   文件会话ID，4字节
 *  	  ii.  认证令牌长度，2字节(从这里开始加密)
 *        iii. 认证令牌
 *    c. 如果命令是0x0003，为封装文件数据的包，格式为(均不加密)
 *        i.   文件会话ID，4字节
 *        ii.  未知的4字节
 *        iii. 被封装的数据包长度，2字节
 *        iv.  数据包，也就是FileDataPacket的格式
 *    d.  如果命令是0x0004，为中转结束包，格式为:
 *        i.   文件会话ID，4字节
 *        ii.  我的QQ号 (从这个字段开始加密)
 *        iii. 未知的两字节，一般为全0，可能表示成功
 *    e.  如果命令是0x0005，是对服务器0x0005的回复，其中7部分全0。格式为:
 *        i.   文件会话ID，4字节
 *        ii.  未知的4字节，全0 (加密)
 *    f.  如果命令是0x0006，6部分全0。格式为:
 *        i.   文件会话ID，4字节
 *        ii.  未知的2字节，全0 (加密)
 * 9. 尾部0x3
 * 
 * 文件中转回复包(从服务器端发出)以0x04开头，格式为:
 * 1. 0x04
 * 2. 中转服务器版本号
 * 3. 整个的包长，2字节
 * 4. 命令，2字节
 * 5. 序号，2字节
 * 6. 我的QQ号，4字节
 * 7. 未知的8字节，无规律
 * 8. 根据命令的不同，后面的内容不同:
 *    A.  如果命令是0x0001，表示对中转请求的回应，且6部分和请求相同。格式为(加密):
 *    	  a.   应答码，2字节，根据应答码不同，内容不同
 *            I. 如果是0x0000，表示接受中转请求，后面的格式为:
 *                 i.   中转服务器IP，little-endian格式，4字节
 *                 ii.  中转服务器端口，2字节
 *                 iii. 文件会话ID，4字节 
 *                 iv.  4字节重定向IP，全0
 *                 v.   2字节重定向端口，全0
 *                 vi.  未知的两字节，全0
 *            II. 如果是0x0001，表示重定向，格式为
 *                 i.   中转服务器IP，4字节，全0
 *                 ii.  中转服务器端口，2字节，全0
 *                 iii. 文件会话ID，4字节，全0
 *                 iv.  4字节重定向IP，little-endian格式
 *                 v.   2字节重定向端口
 *                 vi.  未知的两字节，全0
 *	  B.  如果命令是0x0002，表示中转服务器对接收者报到的应答，格式为:
 *		  a.  文件会话ID，4字节
 *		  b.  未知的4字节，全0(从这里开始加密)
 *    C.  如果命令是0x0003，表示中转的数据，格式为(均不加密):
 *        a.  文件会话ID，4字节
 *        b.  未知的4字节
 *        c.  被封装的数据包长度，2字节
 *        d.  数据包，也就是FileDataPacket的格式
 *    D.  如果命令是0x0005，则是通知客户端可以开始传送数据，格式为:
 *        a.  文件会话ID，4字节
 *        b.  已经传送完成的文件数，两字节 (从这个字段开始加密)
 *        c.  后面的消息长度，2字节
 *        d.  消息，一般是"it's time for transfering data"
 *    E.  如果命令是0x0006，则是对客户端0x0006的回复，7部分全0，格式为:
 *        a.  文件会话ID，4字节
 *        b.  未知的两字节，全0
 *        c.  未知的4字节，0x00013880，十进制值80000 
 *        d.  未知的2字节，0x003C，十进制值60
 *        e.  未知的4字节，全0
 * 9. 尾部，0x3            
 * </pre>
 * 
 * @author luma
 */
@SuppressWarnings("unused")
public class FileAgentPacket {
    // Log对象
    protected static Log log = LogFactory.getLog(FilePacket.class);
    // 文件守望者
    private FileWatcher watcher;
	// 命令
	protected char command;
	// 序号
	protected char sequence;
    // 加密解密类
    protected static Crypter crypter = new Crypter();
    // 被封装的FileDataPacket
    protected FileDataPacket fdp;
    // 加密部分缓冲区
    protected static ByteBuffer buffer = ByteBuffer.allocate(50);

    // 所有的包都会有的字段
    private char sessionId;
    
    // 请求中转服务回复包字段，如果是重定向，则ip为重定向ip;如果是接受，ip为中转服务器ip
    //     这里的IP已经被转换成big-endian格式
    private char replyCode;
    private byte[] agentIp;
    private int agentPort;
    
    /**
     * 构造函数
     * @param watcher 文件守望者对象
     */
    public FileAgentPacket(FileWatcher watcher) {
        this.watcher = watcher;
        this.fdp = new FileDataPacket(watcher);
    }
    
    /**
     * 填充包内容到out中，out中原来的内容将被清空，填充完毕后，out的
     * position等于包长
     * @param out ByteBuffer对象
     */
    public void fill(ByteBuffer out) {
        out.clear();
        // 填充头部
        putHead(out);
        // 填充包体
        putBody(out);
        // 填充包尾
        putTail(out);
    }
    
    /**
     * 填充包内容到out中，out中原来的内容
     * @param out
     * @param from
     */
    public void fill(ByteBuffer out, int from) {
        out.position(from);
        // 填充头部
        putHead(out);
        // 填充包体
        putBody(out);
        // 填充包尾
        putTail(out);
    }
    
    /**
     * 从in的当前位置开始解析文件中转包，解析后的position将位于这个包之后
     * @param in
     * @throws PacketParseException
     */
    public void parse(ByteBuffer in) throws PacketParseException {
        // 解析头部
        parseHead(in);
        // 解析包体
        parseBody(in);
    }

    /**
     * 从in的当前未知开始解析包体
     * @param in
     * @throws PacketParseException
     */
    private void parseBody(ByteBuffer in) throws PacketParseException {
        switch(command) {
    	case QQ.QQ_FILE_CMD_REQUEST_AGENT:
    	    /* 0x0001 */
    	    parseRequestAgent(in);
    	    break;
    	case QQ.QQ_FILE_CMD_CHECK_IN:
    	    /* 0x0002 */
    	    parseCheckIn(in);
    		break;
    	case QQ.QQ_FILE_CMD_FORWARD:
    	    /* 0x0003 */
    	    parseForward(in);
    	    break;
    	case QQ.QQ_FILE_CMD_IT_IS_TIME:
    	    /* 0x0005 */
    	    parseItIsTime(in);
    	    break;
    	case QQ.QQ_FILE_CMD_I_AM_READY:
    	    /* 0x0006 */
    	    parseIAmReady(in);
    	    break;
    	default:
    	    log.error("不支持的命令类型");
    	    break;
        }
    }

    /**
     * 从in的当前未知开始解析报到的回复包
     * @param in
     */
    private void parseCheckIn(ByteBuffer in) {
        // 会话ID
        sessionId = (char)in.getInt();
    }

    /**
     * 从in的当前未知开始解析I Am Ready回复包
     * @param in
     */
    private void parseIAmReady(ByteBuffer in) {
        // 会话ID
        sessionId = (char)in.getInt();
        // TODO 后面的目前看来没有什么用处，先忽略
    }

    /**
     * 从in的当前未知开始解析ItIsTime通知包
     * @param in
     */
    private void parseItIsTime(ByteBuffer in) {
        // 会话ID
        sessionId = (char)in.getInt();
        // TODO 后面的目前看来没有什么用处，先忽略
    }

    /**
     * 从in的当前未知开始解析数据转发包
     * @param in
     * @throws PacketParseException
     */
    private void parseForward(ByteBuffer in) throws PacketParseException {
        // 会话ID
        sessionId = (char)in.getInt();
        // 未知的4字节
        in.getInt();
        // 被转发的包长度
        in.getChar();
        // 被转发的包
        fdp.parse(in);
    }

    /**
     * 从in的当前位置开始解析请求中转回复包包体
     * @param in ByteBuffer对象
     * @throws PacketParseException
     */
    private void parseRequestAgent(ByteBuffer in) throws PacketParseException {
        // 应答码
        replyCode = in.getChar();
        if(replyCode == QQ.QQ_FILE_AGENT_SERVICE_APPROVED) {
            // 得到中转服务器ip和端口
            agentIp = new byte[4];
            agentIp[3] = in.get();
            agentIp[2] = in.get();
            agentIp[1] = in.get();
            agentIp[0] = in.get();
            agentPort = in.getChar();
            // 文件会话id
            sessionId = (char)in.getInt();
        } else if(replyCode == QQ.QQ_FILE_AGENT_SERVICE_REDIRECTED) {
            in.position(in.position() + 10);
            // 得到重定向服务器ip和端口
            agentIp = new byte[4];
            agentIp[3] = in.get();
            agentIp[2] = in.get();
            agentIp[1] = in.get();
            agentIp[0] = in.get();
            agentPort = in.getChar();
        } else
            throw new PacketParseException("请求中转回复包的应答类型不支持");
    }

    /**
     * 从in的当前位置开始解析头部
     * @param in
     * @throws PacketParseException
     */
    private void parseHead(ByteBuffer in) throws PacketParseException {
        // 检查包头标志
        if(in.get() != QQ.QQ_HEADER_04_FAMILY)
            throw new PacketParseException("错误的中转包头");
        // 跳过source
        in.getChar();
        // 得到包长，检查包长是否正确(通过检查最后一个字节是不是0x3)
        int len = in.getChar();
        if(in.get(in.position() + len - 4) != QQ.QQ_TAIL_BASIC_FAMILY)
            throw new PacketParseException("中转包长度有误");
        // 命令
        command = in.getChar();
        // 序号
        sequence = in.getChar();
        // 检查QQ号字段
        if(in.getInt() != watcher.getMyQQ())
            throw new PacketParseException("不是给我的包，抛弃");
        // 未知的8字节
        in.getLong();
    }

    /**
     * 从out的当前未知填充包尾
     * @param out
     */
    private void putTail(ByteBuffer out) {
        out.put(QQ.QQ_TAIL_BASIC_FAMILY);
    }

    /**
     * 从out的当前未知开始填充包体
     * @param out ByteBuffer对象
     */
    private void putBody(ByteBuffer out) {
        switch(command) {
        	case QQ.QQ_FILE_CMD_REQUEST_AGENT:
        	    /* 0x0001 */
        	    initRequestAgent(out);
        	    break;
        	case QQ.QQ_FILE_CMD_CHECK_IN:
        	    /* 0x0002 */
        	    initCheckIn(out);
        		break;
        	case QQ.QQ_FILE_CMD_FORWARD:
        	    /* 0x0003 */
        	    initForward(out);
        	    break;
        	case QQ.QQ_FILE_CMD_FORWARD_FINISHED:
        	    /* 0x0004 */
        	    initForwardFinished(out);
        	    break;
        	case QQ.QQ_FILE_CMD_IT_IS_TIME:
        	    /* 0x0005 */
        	    initItIsTime(out);
        	    break;
        	case QQ.QQ_FILE_CMD_I_AM_READY:
        	    /* 0x0006 */
        	    initIAmReady(out);
        	    break;
        	default:
        	    log.error("不支持的命令类型");
        	    break;
        }
    }

    /**
     * 从out当前位置开始填充报到包
     * @param out ByteBuffer对象
     */
    private void initCheckIn(ByteBuffer out) {
        out.putInt(watcher.getSessionSequence());
        
        buffer.clear();
        buffer.putChar((char)watcher.getFileAgentToken().length);
        buffer.put(watcher.getFileAgentToken());
        byte[] backArray = buffer.array();
        out.put(crypter.encrypt(backArray, 0, buffer.position(), watcher.getFileAgentKey()));
    }

    /**
     * 从out当前位置开始填充我准备好了的请求包
     * @param out ByteBuffer对象
     */
    private void initIAmReady(ByteBuffer out) {
        out.putInt(watcher.getSessionSequence());
        
        buffer.clear();
        buffer.putChar((char)0);
        byte[] backArray = buffer.array();
        out.put(crypter.encrypt(backArray, 0, buffer.position(), watcher.getFileAgentKey()));
    }

    /**
     * 从out当前位置开始填充准备传输回复包
     * @param out ByteBuffer对象
     */
    private void initItIsTime(ByteBuffer out) {
        out.putInt(watcher.getSessionSequence());
        
        buffer.clear();
        buffer.putInt(0);
        byte[] backArray = buffer.array();
        out.put(crypter.encrypt(backArray, 0, buffer.position(), watcher.getFileAgentKey()));
    }

    /**
     * 从out当前位置开始填充中转结束包的其余部分
     * @param out ByteBuffer对象
     */
    private void initForwardFinished(ByteBuffer out) {
        out.putInt(watcher.getSessionSequence());
        
        buffer.clear();
        buffer.putInt(watcher.getMyQQ())
        	.putChar((char)0);
        byte[] backArray = buffer.array();
        out.put(crypter.encrypt(backArray, 0, buffer.position(), watcher.getFileAgentKey()));
    }

    /**
     * 从out当前位置开始填充中转包的其余部分
     * @param out ByteBuffer对象
     */
    private void initForward(ByteBuffer out) {
        // TODO 未知的4字节先用0
        out.putInt(watcher.getSessionSequence())
        	.putInt(0)
        	.putChar((char)0); // 数据包长度，暂时设为0
        // 保存当前位置
        int pos = out.position();
        // 写入数据包
        fdp.fill(out, pos);
        // 修改数据包长度字段
        out.putChar(pos - 2, (char)(out.position() - pos));
    }

    /**
     * 从out当前位置开始填充请求中转包的其余部分
     * @param out ByteBuffer对象
     */
    private void initRequestAgent(ByteBuffer out) {
        out.putChar((char)watcher.getFileAgentToken().length)
        	.put(watcher.getFileAgentToken());
        
        buffer.clear();
        buffer.putChar((char)0x07D0)
        	.putInt(watcher.getHisQQ());
        byte[] backArray = buffer.array();
        out.put(crypter.encrypt(backArray, 0, buffer.position(), watcher.getFileAgentKey()));
    }

    /**
     * 从out的当前未知开始填充包头
     * @param out ByteBuffer对象
     */
    private void putHead(ByteBuffer out) {
        // TODO 暂时未知的8字节用0
        out.put(QQ.QQ_HEADER_04_FAMILY)
        	.putChar(QQ.QQ_CLIENT_VERSION)
        	.putChar((char)0)
        	.putChar(command)
        	.putChar(sequence)
        	.putInt(watcher.getMyQQ())
        	.putLong(0);
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
    
    /**
     * @return Returns the sequence.
     */
    public char getSequence() {
        return sequence;
    }
    
    /**
     * @param sequence The sequence to set.
     */
    public void setSequence(char sequence) {
        this.sequence = sequence;
    }
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if(obj instanceof FileAgentPacket)       
            return ((FileAgentPacket)obj).hashCode() == hashCode();
        else
            return false;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return (sequence << 16) | command; 
    }
}
