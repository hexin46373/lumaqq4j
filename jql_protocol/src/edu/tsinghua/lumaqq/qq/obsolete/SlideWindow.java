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

/**
 * <pre>
 * 滑窗
 * QQ每次只发送一个分片，本来是无需使用滑窗的。但是为了以后的扩展性考虑，还是
 * 定义一个滑窗，滑窗最大尺寸是32
 * </pre>
 * 
 * @author luma
 */
public class SlideWindow {
    // 滑窗位标志，如果滑窗大小是8，那么只使用低8位表示是否这个窗口位置已经收到
    private int mask;
    // 窗口大小
    private int windowSize;
    // 当前窗口下限
    private int low;
    // 当前窗口上限，为下限和windowSize之和减1
    private int high;
    // 最大窗口值
    private int max;
    // 最近一次put操作所带来的下限的调整值
    private int increment;
    
    /**
     * 构造函数，设置一些初始值
     * @param size 滑窗大小
     * @param m 最大窗口值
     */
    public SlideWindow(int size, int m) {
        this(size, 0, m);
    }
    
    /**
     * 构造函数，初始化一些值
     * @param size 滑窗大小
     * @param l 初始下限值
     * @param m 最大窗口值
     */
    public SlideWindow(int size, int l, int m) {
        mask = 0;
        windowSize = (size > 32) ? 32 : size;
        low = l;
        high = low + windowSize - 1;
        max = m;
        if(high > max)
            high = max;
    }
    
    /**
     * 调整下限值
     * @param increment 下限加上increment
     */
    private void adjustLow(int increment) {
        mask >>>= increment;
        low += increment;
        high += increment;
        if(high > max)
            high = max;
        this.increment = increment;
    }
    
    /**
     * <pre>
     * 填充一个窗口，如果指定的窗口在当然窗口范围之外，则失败，或者如果这个窗口
     * 之前填充过，也失败，否则成功
     * 如果填充的窗口是下限位置，则提升下限
     * </pre>
     * @param window 窗口索引
     * @return 成功返回true，失败返回false
     */
    public boolean put(int window) {
        // 如果该窗口落在当前期望的范围，接受，否则返回false
        if(window >= low && window <= high) {
            // mask置位
            int offset = window - low;
            // 检查该位是否已经被置位，如果是，返回false
            if((mask & (1 << offset)) != 0)
                return false;
            // 否则置位
            mask |= 1 << offset;
            // 如果该窗口是下限窗口，调整下限
            if(offset == 0) {
                int size = high - low;
                int i, j;
                for(i = 0, j = 1; i <= size && (mask & j) != 0; i++)
                    j <<= 1;
                adjustLow(i);
            }
            return true;
        } else
            return false;
    }
    
    /**
     * @return 已经收到或者发送成功的最大窗口值
     */
    public int getMaxPassed() {
        if(low > max)
            return max;
        else
            return low - 1;
    }
    
    /**
     * @return true表示所有期望收到的都已经收到
     */
    public boolean isFinished() {
        return low > max;
    }
    
    /**
     * @return Returns the low.
     */
    public int getLow() {
        return low;
    }
    
    /**
     * @return Returns the high.
     */
    public int getHigh() {
        return high;
    }
    
    /**
     * @return Returns the mask.
     */
    public int getMask() {
        return mask;
    }
    
    /**
     * @return Returns the windowSize.
     */
    public int getWindowSize() {
        return windowSize;
    }
    
    /**
     * @return Returns the increment.
     */
    public int getIncrement() {
        return increment;
    }
}
