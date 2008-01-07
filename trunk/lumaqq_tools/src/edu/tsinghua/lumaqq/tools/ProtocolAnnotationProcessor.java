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

import static com.sun.mirror.util.DeclarationVisitors.NO_OP;
import static com.sun.mirror.util.DeclarationVisitors.getDeclarationScanner;

import java.io.File;

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.declaration.TypeDeclaration;

import edu.tsinghua.lumaqq.template.IndexHtmlGenerator;
import edu.tsinghua.lumaqq.ui.helper.FileTool;

/**
 * ProtocolFamily标注处理类
 *
 * @author luma
 */
public class ProtocolAnnotationProcessor implements AnnotationProcessor {
	private AnnotationProcessorEnvironment env;
	private CollectParameterVisitor preVisitor;
	private GeneratorVisitor postVisitor;
	
	public ProtocolAnnotationProcessor() {
		preVisitor = new CollectParameterVisitor();
	}

	public void process() {
		String base = "temp";
		// 复制css文件
		String dir = base + File.separator + "css";
		FileTool.mkdirs(dir);
		FileTool.copyFile("css/print.css", dir + File.separator + "print.css");
		FileTool.copyFile("css/base.css", dir + File.separator + "base.css");
		FileTool.copyFile("css/theme.css", dir + File.separator + "theme.css");
		// 复制logo
		dir = base + File.separator + "images";
		FileTool.mkdirs(dir);
		FileTool.copyFile("images/lumaqq.gif", dir + File.separator + "lumaqq.gif");
		
		for(TypeDeclaration typeDecl : env.getSpecifiedTypeDeclarations())
			typeDecl.accept(getDeclarationScanner(preVisitor, NO_OP));
		postVisitor = new GeneratorVisitor(preVisitor);
		for(TypeDeclaration typeDecl : env.getSpecifiedTypeDeclarations())
			typeDecl.accept(getDeclarationScanner(postVisitor, NO_OP));
		
		// 产生index.html
		System.out.print("Generating index.html...");
		IndexHtmlGenerator gen = new IndexHtmlGenerator();
		FileTool.saveFile(gen.generate(postVisitor.getArgument()), base + File.separator + "index.html");
		System.out.println("Done");
		
		// 打印未使用的事件
		for(EventBean eb : preVisitor.events.values()) {
			if(eb.packets.isEmpty())
				System.out.println("Event " + eb.name + " isn't documented now");
		}
	}

	/**
	 * @return the env
	 */
	public AnnotationProcessorEnvironment getEnv() {
		return env;
	}

	/**
	 * @param env the env to set
	 */
	public void setEnv(AnnotationProcessorEnvironment env) {
		this.env = env;
	}
}
