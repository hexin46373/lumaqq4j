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

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.AnnotationProcessorFactory;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;

/**
 * 协议标注处理类工厂
 *
 * @author luma
 */
public class ProtocolAnnotationProcessorFactory implements
		AnnotationProcessorFactory {
	private static final Collection<String> supportedAnnotation = Collections.singletonList("*");
	private static final Collection<String> supportedOptions = Collections.emptyList();
	private static final ProtocolAnnotationProcessor ap = new ProtocolAnnotationProcessor();

	public Collection<String> supportedOptions() {
		return supportedOptions;
	}

	public Collection<String> supportedAnnotationTypes() {
		return supportedAnnotation;
	}

	public AnnotationProcessor getProcessorFor(Set<AnnotationTypeDeclaration> types, AnnotationProcessorEnvironment env) {
		ap.setEnv(env);
		return ap;
	}
}
