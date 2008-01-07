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
package edu.tsinghua.lumaqq.qq.beans;

import java.nio.ByteBuffer;
import java.util.StringTokenizer;

import edu.tsinghua.lumaqq.qq.QQ;
import edu.tsinghua.lumaqq.qq.Util;
import edu.tsinghua.lumaqq.qq.packets.in.ReceiveIMPacket;


/**
 * QQLive描述信息Bean
 * 
 * @author luma
 * @see ReceiveIMPacket
 */
public class QQLive {
	public char type;
	public String title;
	public String description;
	public String url;
	
	public void readBean(ByteBuffer buf) {
		type = buf.getChar();
		int len = buf.getChar() & 0xFF;
		switch(type) {
			case QQ.QQ_LIVE_IM_TYPE_DISK:
				String s = Util.getString(buf, len);
				StringTokenizer st = new StringTokenizer(s, "\u0002");
				if(st.hasMoreTokens())
					title = st.nextToken();
				if(st.hasMoreTokens())
					description = st.nextToken();
				if(st.hasMoreTokens())
					url = st.nextToken();
				break;
		}
	}
}
