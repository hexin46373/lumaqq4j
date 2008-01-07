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
package edu.tsinghua.lumaqq.template;

public interface IGenerator {
	// 通用参数
	/** 菜单名称 */
	public static final String MENU_NAME = "Menu Name";	
	/** 发布时间，Long */
	public static final String PUBLISH_TIME = "发布时间: ";
	/** 基类列表, List<FamilyBean> */
	public static final String FAMILY_BEAN_LIST = "Family Bean List";
	
	// 用于产生family.html
	public static final String FAMILY_BEAN = "Family Bean";
	
	// 用于产生packet.html
	public static final String PACKET_BEAN = "Packet Bean";
	
	// 用于产生event.html
	public static final String EVENT_BEAN = "Event Bean";
		
	// 一些字符串常量
	public static final String RELATED_EVENT = "相关事件";
	public static final String LINKED_PACKET = "此事件可由以下包触发";
	public static final String RELATED_PACKET = "相关包";
	public static final String EVENT_LIST = "触发事件";
	public static final String PACKET_LIST = "包列表";
	public static final String INTRO_TITLE = "欢迎使用LumaQQ开发者参考手册";
	public static final String INTRO = "LumaQQ开发者参考手册是LumaQQ的核心文档，其使用JDK 5.0的Annotation技术自动生成，其内容在逐步完善中。目前包含了协议格式参考和事件参考。如果你正基于JQL做二次开发，那么也许这本手册可以对你有点帮助。如果你想了解这本手册是如何生成的，可以参考LumaQQ_tools项目，你可以从CVS里获得相关源代码。";
	public static final String NO_RELATED_EVENT = "没有相关事件";
	public static final String NO_RELATED_PACKET = "没有相关包";
	public static final String NO_EVENT_LIST = "没有事件与此包关联";
	
	/**
	 * 生成
	 * 
	 * @param argument
	 * 		参数, Map<String, Object>对象
	 * @return
	 * 		内容
	 */
	public String generate(Object argument);
}
