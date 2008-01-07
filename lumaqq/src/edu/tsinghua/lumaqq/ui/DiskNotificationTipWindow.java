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
package edu.tsinghua.lumaqq.ui;

import static edu.tsinghua.lumaqq.resource.Messages.*;
import org.eclipse.swt.graphics.Image;

import edu.tsinghua.lumaqq.qq.beans.QQLive;
import edu.tsinghua.lumaqq.resource.Resources;

/**
 * 网络硬盘通知消息提示窗口
 *
 * @author luma
 */
public class DiskNotificationTipWindow extends BaseTipWindow {
	private QQLive live;

	public DiskNotificationTipWindow(MainShell main, QQLive qqlive) {
		super(main);
		this.live = qqlive;
	}

	@Override
	protected String getTitle() {
		return live.title;
	}
	
	@Override
	protected void onButton() {
		close();
	}

	@Override
	protected Image getImage() {
		return res.getImage(Resources.icoLumaQQ);
	}

	@Override
	protected String getButtonLabel() {
		return button_close;
	}

	@Override
	protected String getTip() {
		return live.description;
	}
}
