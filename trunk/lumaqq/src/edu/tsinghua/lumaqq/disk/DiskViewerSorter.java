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
package edu.tsinghua.lumaqq.disk;

import java.text.Collator;
import java.util.Locale;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import edu.tsinghua.lumaqq.qq.beans.Directory;
import edu.tsinghua.lumaqq.qq.beans.File;

/**
 * 网络硬盘视图排序器
 *
 * @author luma
 */
public class DiskViewerSorter extends ViewerSorter {
	private static Collator collator = Collator.getInstance(Locale.getDefault());

	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		if(e1 instanceof Integer && e2 instanceof Integer)
			return ((Integer)e1) - ((Integer)e2);
		else if(e1 instanceof Directory){
			if(e2 instanceof Directory)
				return collator.compare(((Directory)e1).name, ((Directory)e2).name);
			else if(e2 instanceof File)
				return -1;
			else
				return 0;
		} else if(e1 instanceof File) {
			if(e2 instanceof Directory)
				return 1;
			else if(e2 instanceof File)
				return collator.compare(((File)e1).name, ((File)e2).name);
			else
				return 0;
		} else
			return 0;
	}
}
