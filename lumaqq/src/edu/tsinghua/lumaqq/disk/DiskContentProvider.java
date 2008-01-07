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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import edu.tsinghua.lumaqq.qq.beans.Directory;
import edu.tsinghua.lumaqq.qq.beans.File;
import edu.tsinghua.lumaqq.ui.MainShell;

/**
 * 网络硬盘树型结构内容管理者
 *
 * @author luma
 */
public class DiskContentProvider implements ITreeContentProvider {
	public static final int MY_DISK = 0;
	public static final int SHARED_DISK = 1;
	
	private List<Object> temp;
	private DiskManager manager;
	private MainShell main;
		
	public DiskContentProvider(MainShell main) {
		temp = new ArrayList<Object>();
		this.main = main;
		manager = main.getDiskManager();
	}

	public Object[] getChildren(Object parentElement) {
		if(parentElement instanceof Integer) {
			int i = (Integer)parentElement;
			switch(i) {
				case MY_DISK:
					return manager.getRoots(main.getMyModel().qq);
				case SHARED_DISK:
					return manager.getDiskOwners();
				default:
					return manager.getRoots(i);
			}
		} else if(parentElement instanceof Directory) {
			return manager.getChildren((Directory)parentElement);
		} else
			return new Object[0];
	}

	public Object getParent(Object element) {
		return  null;
	} 

	public boolean hasChildren(Object element) {
		return !(element instanceof File);
	}

	public Object[] getElements(Object inputElement) {
		temp.clear();
		temp.add(MY_DISK);
		temp.add(SHARED_DISK);
		return temp.toArray();
	}

	public void dispose() {
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}
}
