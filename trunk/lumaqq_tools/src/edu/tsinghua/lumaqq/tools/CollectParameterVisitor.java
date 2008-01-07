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
package edu.tsinghua.lumaqq.tools;

import static edu.tsinghua.lumaqq.tools.Helper.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.sun.mirror.declaration.AnnotationMirror;
import com.sun.mirror.declaration.AnnotationTypeElementDeclaration;
import com.sun.mirror.declaration.AnnotationValue;
import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.Declaration;
import com.sun.mirror.declaration.FieldDeclaration;
import com.sun.mirror.type.ClassType;
import com.sun.mirror.util.SimpleDeclarationVisitor;

import edu.tsinghua.lumaqq.qq.annotation.BasePacket;
import edu.tsinghua.lumaqq.qq.annotation.DocumentalEvent;
import edu.tsinghua.lumaqq.qq.annotation.DocumentalPacket;
import edu.tsinghua.lumaqq.qq.annotation.LinkedEvent;
import edu.tsinghua.lumaqq.qq.annotation.PacketName;
import edu.tsinghua.lumaqq.qq.annotation.RelatedPacket;

/**
 * 遍历类文件，找寻需要生成文档的包和事件。把信息收集到Map中
 *
 * @author luma
 */
public class CollectParameterVisitor extends SimpleDeclarationVisitor {
	// QName为key
	public Map<String, FamilyBean> families;
	// QName为key
	public Map<String, PacketBean> packets;
	// 事件id为key
	public Map<Integer, EventBean> events;
	
	public CollectParameterVisitor() {
		families = new HashMap<String, FamilyBean>();
		packets = new HashMap<String, PacketBean>();
		events = new HashMap<Integer, EventBean>();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void visitClassDeclaration(ClassDeclaration decl) {
		// 检查是否需要把包加入手册中
		if(hasAnnotation(decl, DocumentalPacket.class) && !hasAnnotation(decl, Deprecated.class)) {
			if(hasAnnotation(decl, BasePacket.class) && hasAnnotationMirror(decl, BasePacket.class.getCanonicalName())) {
				/*
				 * 是一个基类包
				 */
				// 格式
				FamilyBean bean = getFamilyBean(decl.getQualifiedName());
				bean.format = decl.getDocComment();
				// 包名
				BasePacket bp = (BasePacket)getAnnotation(decl, BasePacket.class);
				bean.name = bp.name();
			} else {
				/*
				 * 是一个子包
				 */
				// 包格式
				PacketBean bean = getPacketBean(decl.getQualifiedName());
				bean.format = decl.getDocComment();
				// 得到包名
				PacketName pn = (PacketName)getAnnotation(decl, PacketName.class);
				bean.name = pn.value();
				// 添加相关包
				AnnotationMirror mir = getAnnotationMirror(decl, RelatedPacket.class.getCanonicalName());
				if(mir != null) {
					List<AnnotationValue> classes = (List<AnnotationValue>)getValue(mir, "value");
					for(AnnotationValue v : classes)
						bean.relatedPackets.add(getPacketBean(((ClassType)v.getValue()).getDeclaration().getQualifiedName()));
				}
				// 相关事件
				LinkedEvent le = (LinkedEvent)getAnnotation(decl, LinkedEvent.class);
				if(le != null) {
					for(int eventId : le.value()) {
						EventBean eb = getEventBean(eventId);
						bean.events.add(eb);
						eb.packets.add(bean);
						for(int relatedId : le.value()) {
							if(relatedId != eventId)
								eb.relatedEvents.add(getEventBean(relatedId));
						}
					}
				}
				// 所属基类
				mir = findAnnotationMirror(decl, BasePacket.class.getCanonicalName());
				if(mir != null) {
					ClassType v = (ClassType)getValue(mir, "klass");
					getFamilyBean(v.getDeclaration().getQualifiedName()).packets.add(bean);
				}
			}
		}
	}
	
	private AnnotationMirror findAnnotationMirror(ClassDeclaration decl, String qName) {
		AnnotationMirror mir = getAnnotationMirror(decl, qName);
		if(mir == null && decl.getSuperclass() != null)
			return findAnnotationMirror(decl.getSuperclass().getDeclaration(), qName);
		else
			return mir;
	}
	
	@Override
	public void visitFieldDeclaration(FieldDeclaration decl) {
		if(hasAnnotation(decl, DocumentalEvent.class)) {
			// 保存注释和名称
			Integer id = (Integer)decl.getConstantValue();
			EventBean bean = getEventBean(id);
			bean.name = decl.getSimpleName();
			bean.info = decl.getDocComment();
		}
	}
	
	/**
	 * 根据QName得到EventBean
	 * 
	 * @param qName
	 * @return
	 */
	private EventBean getEventBean(int id) {
		EventBean bean = events.get(id);
		if(bean == null) {
			bean = new EventBean();
			bean.id = id;
			bean.html = UUID.randomUUID().toString() + ".html";
			events.put(id, bean);
		}
		return bean;
	}
	
	/**
	 * 根据QName得到PacketBean
	 * 
	 * @param qName
	 * @return
	 */
	private PacketBean getPacketBean(String qName) {
		PacketBean bean = packets.get(qName);
		if(bean == null) {
			bean = new PacketBean();
			bean.qName = qName;
			bean.html = UUID.randomUUID().toString() + ".html";
			packets.put(qName, bean);
		}
		return bean;
	}
	
	/**
	 * 根据QName得到FamilyBean
	 * 
	 * @param qName
	 * @return
	 */
	private FamilyBean getFamilyBean(String qName) {
		FamilyBean bean = families.get(qName);
		if(bean == null) {
			bean = new FamilyBean();
			bean.qName = qName;
			bean.html = UUID.randomUUID().toString() + ".html";
			families.put(qName, bean);
		}
		return bean;
	}
	
	/**
	 * 根据参数名得到参数值
	 * 
	 * @param mir
	 * @param valueName
	 * @return
	 */
	private Object getValue(AnnotationMirror mir, String valueName) {
		Map<AnnotationTypeElementDeclaration, AnnotationValue> values = mir.getElementValues();
		for(AnnotationTypeElementDeclaration element : values.keySet()) {
			if(element.getSimpleName().equals(valueName))
				return values.get(element).getValue();
		}
		return null;
	}
	
	/**
	 * 根据类名查找AnnotationMirror对象
	 * 
	 * @param decl
	 * @param qName
	 * @return
	 */
	private AnnotationMirror getAnnotationMirror(Declaration decl, String qName) {
		for(AnnotationMirror mir : decl.getAnnotationMirrors()) {
			if(mir.getAnnotationType().getDeclaration().getQualifiedName().equals(qName))
				return mir;
		}
		return null;
	}
}
