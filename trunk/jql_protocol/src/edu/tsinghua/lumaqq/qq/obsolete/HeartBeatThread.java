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


/**
 * <pre>
 * Heart beat 线程，每3秒一跳，跳时检查当前状态，若有需要重发的包，则重发
 * 如果累计30跳都没有任何回应，当作网络错误停止传输。如果其他方面有回应，但是
 * 就是不回应heart beat，在15跳之后停止当前操作，直到对方回应heart beat后才
 * 继续其他的操作
 * </pre>
 * 
 * @author luma
 */
public class HeartBeatThread extends Thread {
    // 从属的FileSender对象
    private FileSender sender;
    // 停止标志
    private volatile boolean stop;
    // 当前序号
    private char current;
    // 已收到的heart beat回复的最大序号
    private char maxReply;
    // 是否其他操作进行正常
    private boolean anybodyLiving;
    // 临时用途，由于在Watcher中，fdp，fcp，buffer是公用的，因为为了保证线程
    //    之间不冲突，在这里为heart beat线程定义三个专用的
    private FileControlPacket fcp;
    private FileDataPacket fdp;
    private ByteBuffer buffer;
    
    /**
     * 构造函数
     * @param sender
     */
    public HeartBeatThread(FileSender sender) {
        this.sender = sender;
        stop = false;
        current = 0;
        maxReply = 0;
        anybodyLiving = false;
        fdp = new FileDataPacket(sender);
        fcp = new FileControlPacket(sender);
        buffer = ByteBuffer.allocateDirect(QQ.QQ_MAX_PACKET_SIZE);
    }
    
	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	public void run() {
	    while(!stop) {
	        // 等待3秒
	        synchronized(this) {
	            try {
	                if(!stop)
	                    wait(3000);
                } catch (InterruptedException e) {
                    // 没有什么要做的
                }
	        }
	        // 发送heart beat
            sender.sendHeartBeat(fdp, buffer, current);
	        // 如果sender目前挂起，返回；如果没有挂起，则检查是否超时等等	                
            if(sender.isSuspend())
                continue;
	        else if(anybodyLiving) {
	            // 检查是否已经过了15跳，对方还没有返回一个回复
	            if(current - maxReply >= 15) {
	                sender.setSuspend(true);
	                anybodyLiving = false;
	                continue;
	            }
	        } else {
	            // 检查是否已经过了30跳，对方还没有返回一个回复
	            if(current - maxReply >= 30) {
	                sender.abort();
	                continue;
	            }
	        }
	        // 以上情况不成立时属于正常情况，增加heart beat序号
	        current++;
	        // 检查sender目前状态，做出相应操作
	        switch(sender.fileTransferStatus) {
	            case FileWatcher.FT_SAYING_HELLO:
		            sender.sayHello(fcp, buffer);
	            	break;
	            case FileWatcher.FT_SENDING:
			        sender.sendFragment(fdp, buffer);
			        break;
			    case FileWatcher.FT_SENDING_EOF:
			        sender.sendEOF(fdp, buffer);
			    	break;
			    case FileWatcher.FT_SENDING_BASIC:
			        sender.sendBasic(fdp, buffer);
	        } 
	    }
	}
	
    /**
     * @param stop The stop to set.
     */
    public void setStop(boolean stop) {
        synchronized(this) {
	        this.stop = stop;
	        notify();
        }
    }
    
    /**
     * @param maxReply The maxReply to set.
     */
    public void setMaxReply(char maxReply) {
        this.maxReply = maxReply;
    }
    
    /**
     * @param anybodyLiving The anybodyLiving to set.
     */
    public void setAnybodyLiving(boolean anybodyLiving) {
        this.anybodyLiving = anybodyLiving;
    }
}
