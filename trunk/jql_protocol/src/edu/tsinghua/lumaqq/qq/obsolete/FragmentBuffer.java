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
import java.io.RandomAccessFile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <pre>
 * 用于保存发送文件数据的缓冲区，它的尺寸和滑窗相同，文件分片序号从1开始
 * 如果用户请求得到一个不在当前范围内的分片，则不做处理
 * </pre>
 * 
 * @author luma
 */
public class FragmentBuffer {
    // Log
    private static Log log = LogFactory.getLog(FragmentBuffer.class);
    
    private int size;
    private int fragmentSize;
    private RandomAccessFile file;
    private int fileSize;
    // 当前缓冲的最小和最大分片序号
    private int low, high;
    // 文件最大的序号
    private int max;
    // 缓冲区
    private byte[][] buffer;
    
    /**
     * 构造函数
     * @param file RandomAccessFile对象
     * @param size buffer大小
     * @param fz 分片大小
     * @param max 文件最大的分片序号
     */
    public FragmentBuffer(RandomAccessFile file, int size, int fz, int max) {
        this.file = file;
        this.size = size;
        this.fragmentSize = fz;
        this.low = 0;
        this.high = low + size - 1;
        this.max = max;
        if(high > max)
            high = max;
        try {
            this.fileSize = (int)file.length();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        this.buffer = new byte[size][];
        for(int i = 0; i < size; i++)
            loadFragment(low + i);
    }
    
    /**
     * 得到一个分片的数组
     * @param index 分片的绝对序号
     * @return 分片数据字节数组
     */
    public byte[] getFragment(int index) {
        if(index < low || index > high) return null;
        if(index == max) {
            byte[] ret = new byte[fileSize % fragmentSize];
            System.arraycopy(buffer[index - low], 0, ret, 0, ret.length);
            return ret;
        } else           
            return buffer[index - low];
    }
    
    /**
     * 载入一个分片数据到缓冲区
     * @param index 分片的绝对序号
     */
    private void loadFragment(int index) {
        try {
            int relative = index - low; 
            // 如果这个分片的缓冲区还没有被分配，则分配之
            if(buffer[relative] == null)
                buffer[relative] = new byte[fragmentSize];
            // 读入分片数据
            file.seek(index * fragmentSize);
            file.read(buffer[relative]);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
    
    /**
     * 抛弃从low到index（包括）为止的所有分片，调整low和high的值并载入新的分片
     * @param index 分片的绝对序号
     */
    public void releaseTo(int index) {
        // 检查index范围
        if(index < low)
            return;
        // 如果index大于等于high，也就是所有的分片都抛弃，则直接处理 
        if(index >= high) {
            low = high + 1;
            high = low + size - 1;
            if(high > max)
                high = max;
            for(int i = low; i <= high; i++)
                loadFragment(i);
            return;
        }
        // 得到要抛弃的分片数
        int num = index - low + 1;
        // 把未抛弃的前移
        for(int i = 0; i < size - num; i++) {
            int swap = i + num;
            byte[] temp = buffer[i];
            buffer[i] = buffer[swap];
            buffer[swap] = temp;
        }
        // 调整low和high
        low += num;
        high = low + size - 1;
        if(high > max)
            high = max;
        // 载入新的分片
        for(int i = low + size - num; i <= high; i++)
            loadFragment(i);
    }
    
    /**
     * 抛弃最前面的num个分片
     * @param num
     */
    public void release(int num) {
        releaseTo(low + num - 1);
    }
}
