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

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.FieldDeclaration;
import com.sun.mirror.util.SimpleDeclarationVisitor;

import edu.tsinghua.lumaqq.qq.annotation.BasePacket;
import edu.tsinghua.lumaqq.qq.annotation.DocumentalEvent;
import edu.tsinghua.lumaqq.qq.annotation.DocumentalPacket;
import edu.tsinghua.lumaqq.template.EventHtmlGenerator;
import edu.tsinghua.lumaqq.template.FamilyHtmlGenerator;
import edu.tsinghua.lumaqq.template.IGenerator;
import edu.tsinghua.lumaqq.template.PacketHtmlGenerator;
import edu.tsinghua.lumaqq.ui.helper.FileTool;

/**
 * 根据已经收集到的信息，生成手册的所有文件
 *
 * @author luma
 */
public class GeneratorVisitor extends SimpleDeclarationVisitor {
	private CollectParameterVisitor v;
	private Map<String, Object> argument;
	private static Collator collator = Collator.getInstance(Locale.getDefault());
	
	public GeneratorVisitor(CollectParameterVisitor v) {
		this.v = v;
		argument = new HashMap<String, Object>();
		argument.put(IGenerator.PUBLISH_TIME, new Long(System.currentTimeMillis()));
		argument.put(IGenerator.MENU_NAME, "LumaQQ参考");
		List<FamilyBean> beans = new ArrayList<FamilyBean>();
		beans.addAll(v.families.values());
		Collections.sort(beans, new Comparator<FamilyBean>() {
			public int compare(FamilyBean o1, FamilyBean o2) {
				return collator.compare(o1.name, o2.name);
			}
		});
		argument.put(IGenerator.FAMILY_BEAN_LIST, beans);
		
		FileTool.mkdirs("./temp/family");
		FileTool.mkdirs("./temp/packet");
		FileTool.mkdirs("./temp/event");
	}

	@Override
	public void visitClassDeclaration(ClassDeclaration decl) {
		if(hasAnnotation(decl, DocumentalPacket.class) && !hasAnnotation(decl, Deprecated.class)) {
			if(hasAnnotation(decl, BasePacket.class) && hasAnnotationMirror(decl, BasePacket.class.getCanonicalName())) {
				FamilyBean bean = v.families.get(decl.getQualifiedName());
				if(bean != null) {
					System.out.print("Generating family " + bean.name + " page...");
					argument.put(IGenerator.FAMILY_BEAN, bean);
					FamilyHtmlGenerator gen = new FamilyHtmlGenerator();
					FileTool.saveFile(gen.generate(argument), "./temp/family/" + bean.html);
					System.out.println("Done");
				}
			} else {
				PacketBean bean = v.packets.get(decl.getQualifiedName());
				if(bean != null) {
					System.out.print("Generating packet " + bean.name + " page...");
					argument.put(IGenerator.PACKET_BEAN, bean);
					PacketHtmlGenerator gen = new PacketHtmlGenerator();
					FileTool.saveFile(gen.generate(argument), "./temp/packet/" + bean.html);
					System.out.println("Done");
				}
			}
		}
	}
	
	@Override
	public void visitFieldDeclaration(FieldDeclaration decl) {
		if(hasAnnotation(decl, DocumentalEvent.class)) {
			Integer id = (Integer)decl.getConstantValue();
			EventBean bean = v.events.get(id);
			if(bean != null) {
				System.out.print("Generating event " + bean.name + " page...");
				argument.put(IGenerator.EVENT_BEAN, bean);
				EventHtmlGenerator gen = new EventHtmlGenerator();
				FileTool.saveFile(gen.generate(argument), "./temp/event/" + bean.html);
				System.out.println("Done");
			}
		}
	}

	/**
	 * @return the argument
	 */
	public Map<String, Object> getArgument() {
		return argument;
	}
}
