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

import java.lang.annotation.Annotation;

import com.sun.mirror.declaration.AnnotationMirror;
import com.sun.mirror.declaration.Declaration;

/**
 * 一些帮助方法
 *
 * @author luma
 */
public class Helper {	
	/**
	 * 判断是否存在指定名称的直接声名的annotation
	 * 
	 * @param decl
	 * @param qName
	 * @return
	 */
	public static boolean hasAnnotationMirror(Declaration decl, String qName) {
		for(AnnotationMirror mir : decl.getAnnotationMirrors()) {
			if(mir.getAnnotationType().getDeclaration().getQualifiedName().equals(qName))
				return true;
		}
		return false;
	}
	
	/**
	 * 检查是否存在指定的annotation
	 * 
	 * @param <A>
	 * 		Annotation类型
	 * @param decl
	 * 		Declaration子类
	 * @param c
	 * 		Annotation的Class对象
	 * @return
	 * 		true表示存在
	 */
	public static <A extends Annotation> boolean hasAnnotation(Declaration decl, Class<A> c) {
		return decl.getAnnotation(c) != null;
	}
	
	/**
	 * 得到annotation
	 * 
	 * @param <A>
	 * @param decl
	 * @param c
	 * @return
	 */
	public static <A extends Annotation> Annotation getAnnotation(Declaration decl, Class<A> c) {
		return decl.getAnnotation(c);
	}
}
