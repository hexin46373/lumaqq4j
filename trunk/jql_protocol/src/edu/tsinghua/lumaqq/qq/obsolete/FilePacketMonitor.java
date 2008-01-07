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

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map;

/**
 * <pre>
 * 包监视器，用于检测重复包。不是每个包都需要进行重复检测，比如文件数据分片的包等等。
 * 这个monitor采用和PacketMonitor一样的策略清空缓冲区。对于重复的包，并非不理睬，
 * 只是有些处理就不需要了，但是即使是重复包，回复也是需要的
 * </pre>
 * 
 * @author luma
 */
public class FilePacketMonitor {
    // 用于重复包检测哈希表和链表
    private Map<Integer, Integer> hash;
    private LinkedList<Integer> list;
    // 阈值，超过时清理hash中的数据
    private static final int THRESHOLD = 100;
    
    // 私有构造函数，因为这是一个Singleton类
    public FilePacketMonitor() {
        this.hash = new Hashtable<Integer, Integer>();
        this.list = new LinkedList<Integer>();
    }
    
    /**
     * 检查文件控制信息包是否重复，如果不重复，这个包的哈希值将加入到缓冲中
     * @param fcp
     * @return true表示重复
     */
    public boolean checkDuplicate(FileControlPacket fcp) {
        return checkDuplicate(fcp.hashCode());
    }
    
    /**
     * 检查文件数据信息包是否重复，如果不重复，这个包的哈希值将加入到缓冲中
     * @param fdp
     * @return true表示重复
     */
    public boolean checkDuplicate(FileDataPacket fdp) {
        return checkDuplicate(fdp.hashCode());
    }
    
    /**
     * 检查文件中转包是否重复，如果不重复，这个包的哈希值将加入到缓冲区中
     * @param fap 文件中转包对象
     * @return true表示重复
     */
    public boolean checkDuplicate(FileAgentPacket fap) {
        return checkDuplicate(fap.hashCode());
    }
    
    /**
     * 检查某哈希值是否存在，如果不存在，添加到缓冲中，并检查是否超出了阈值
     * @param h
     * @return true表示重复
     */
    private boolean checkDuplicate(int h) {
        if(hash.containsKey(h))
            return true;
        else {
            // 添加这个哈希
            hash.put(h, h);
            list.addLast(h);
        }
        // 检查是否超过了阈值
        if(list.size() >= THRESHOLD) {
            // 清理掉一半
            for(int i = 0; i < (THRESHOLD / 2); i++) {
                Object key = list.removeFirst();
                hash.remove(key);
            }
        }            
        return false;
    }
}
