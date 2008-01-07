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

import static edu.tsinghua.lumaqq.resource.Messages.*;
import static edu.tsinghua.lumaqq.disk.DiskContentProvider.*;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import edu.tsinghua.lumaqq.models.ModelRegistry;
import edu.tsinghua.lumaqq.models.User;
import edu.tsinghua.lumaqq.qq.QQ;
import edu.tsinghua.lumaqq.qq.beans.Directory;
import edu.tsinghua.lumaqq.qq.beans.File;
import edu.tsinghua.lumaqq.resource.Resources;
import edu.tsinghua.lumaqq.ui.MainShell;
import edu.tsinghua.lumaqq.ui.helper.FileTool;

public class DiskLabelProvider extends LabelProvider {
	private MainShell main;
	
	public DiskLabelProvider(MainShell main) {
		this.main = main;
	}
	
	@Override
	public Image getImage(Object element) {
		Resources res = Resources.getInstance();
		if(element instanceof Integer) {
			int i = (Integer)element;
			switch(i) {
				case MY_DISK:
					return res.getImage(Resources.icoMyDisk);
				case SHARED_DISK:
					return res.getImage(Resources.icoSharedDisk);
				case 10000:
					return res.getImage(Resources.icoSharedResource);
				default:
					User u = ModelRegistry.getUser(i);
					if(u == null)
						return res.getImage(Resources.icoLumaQQ);
					else 
						return res.getSmallHead(u.headId);
			}
		} else if(element instanceof Directory) {
			Directory dir = (Directory)element;
			switch(dir.id) {
				case QQ.QQ_DISK_DIR_MY_DOC:
					return res.getImage(Resources.icoMyDoc);
				case QQ.QQ_DISK_DIR_MY_PICTURE:
					return res.getImage(Resources.icoMyPicture);
				case QQ.QQ_DISK_DIR_MY_MULTIMEDIA:
					return res.getImage(Resources.icoMyMultimedia);
				case QQ.QQ_DISK_DIR_MY_ASSISTANT:
					return res.getImage(Resources.icoMyAssistant);
				case QQ.QQ_DISK_DIR_MY_FAVORITE:
					return res.getImage(Resources.icoMyDisk);
				case QQ.QQ_DISK_DIR_MY_NOTEBOOK:
					return res.getImage(Resources.icoMyNotebook);
				case QQ.QQ_DISK_DIR_MY_CUSTOM_FACE:
					return res.getImage(Resources.icoSmiley);
				case QQ.QQ_DISK_DIR_MY_CUSTOM_HEAD:
					return res.getImage(Resources.icoMyCustomHead);
				case QQ.QQ_DISK_DIR_MY_ALBUM:
					return res.getImage(Resources.icoMyAlbum);
				default:
					return res.getImage(Resources.icoFolder);					
			}
		} else if(element instanceof File) {
			File file = (File)element;
			return res.getExtensionImage(FileTool.getExtension(file.name));
		} else
			return null;
	}
	
	@Override
	public String getText(Object element) {
		if(element instanceof Integer) {
			int i = (Integer)element;
			switch(i) {
				case MY_DISK:
					return disk_my;
				case SHARED_DISK:
					return disk_shared_disk;
				case 10000:
					return disk_public;
				default:
					User u = ModelRegistry.getUser(i);
					if(u == null)
						return String.valueOf(i);
					else 
						return u.displayName + '(' + i + ')';
			}
		} else if(element instanceof Directory) {
			Directory dir = (Directory)element;
			String name = "";
			switch(dir.id) {
				case QQ.QQ_DISK_DIR_MY_DOC:
					name = disk_my_doc;
					break;
				case QQ.QQ_DISK_DIR_MY_PICTURE:
					name = disk_my_picture;
					break;
				case QQ.QQ_DISK_DIR_MY_MULTIMEDIA:
					name = disk_my_multimedia;
					break;
				case QQ.QQ_DISK_DIR_MY_ASSISTANT:
					name = disk_my_assistant;
					break;
				case QQ.QQ_DISK_DIR_MY_FAVORITE:
					name = disk_my_favorite;
					break;
				case QQ.QQ_DISK_DIR_MY_NOTEBOOK:
					name = disk_my_notebook;
					break;
				case QQ.QQ_DISK_DIR_MY_CUSTOM_HEAD:
					name = disk_my_custom_head;
					break;
				case QQ.QQ_DISK_DIR_MY_CUSTOM_FACE:
					name = disk_my_custom_face;
					break;
				case QQ.QQ_DISK_DIR_MY_ALBUM:
					name = disk_my_album;
					break;
				default:
					name = dir.name;				
					break;
			}
			if(dir.owner == main.getMyModel().qq && dir.isShared())
				name += ' ' + disk_shared;
			return name;
		} else if(element instanceof File) {
			if(((File)element).isFinalized())
				return ((File)element).name;
			else
				return ((File)element).name + ' ' + disk_not_finalized;
		} else if(element instanceof String) {
			return (String)element;
		} else
			return "";
	}
}
