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
package edu.tsinghua.lumaqq.ui.dialogs;

import static edu.tsinghua.lumaqq.resource.Messages.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import edu.tsinghua.lumaqq.events.FriendSelectionEvent;
import edu.tsinghua.lumaqq.events.IFriendSelectionListener;
import edu.tsinghua.lumaqq.models.Model;
import edu.tsinghua.lumaqq.models.ModelRegistry;
import edu.tsinghua.lumaqq.models.User;
import edu.tsinghua.lumaqq.qq.QQ;
import edu.tsinghua.lumaqq.qq.beans.Directory;
import edu.tsinghua.lumaqq.resource.Resources;
import edu.tsinghua.lumaqq.ui.FriendSelectionShell;
import edu.tsinghua.lumaqq.ui.MainShell;
import edu.tsinghua.lumaqq.ui.helper.UITool;
import edu.tsinghua.lumaqq.ui.jobs.GetShareListJob;
import edu.tsinghua.lumaqq.ui.jobs.IJob;
import edu.tsinghua.lumaqq.ui.jobs.IJobListener;
import edu.tsinghua.lumaqq.ui.jobs.JobEvent;
import edu.tsinghua.lumaqq.ui.jobs.SetShareListJob;
import edu.tsinghua.lumaqq.ui.provider.ListContentProvider;

/**
 * 设置网络硬盘目录共享对话框
 *
 * @author luma
 */
public class DiskShareDialog extends Dialog implements IFriendSelectionListener, IJobListener {
	private class TableLabelProvider extends LabelProvider implements ITableLabelProvider {
		public Image getColumnImage(Object element, int columnIndex) {
			switch(columnIndex) {
				case 0:
					User u = ModelRegistry.getUser((Integer)element);
					if(u == null)
						return Resources.getInstance().getSmallHead(0);
					else
						return Resources.getInstance().getSmallHead(u.headId);
				default:
					return null;
			}
		}

		public String getColumnText(Object element, int columnIndex) {
			switch(columnIndex) {
				case 0:
					User u = ModelRegistry.getUser((Integer)element);
					if(u == null)
						return String.valueOf((Integer)element);
					else
						return u.displayName + " (" + u.qq + ')';
				default:
					return "";
			}
		}		
	}
	
	private MainShell main;
	private FriendSelectionShell fss;
	private List<Integer> oldList;
	private List<Integer> newList;
	private Directory dir;
	
	private boolean share;
	private TableViewer viewer;
	private Button chkShare;
	
	private static final int REMOVE_ID = 9999;
	
	public DiskShareDialog(MainShell main, Directory dir) {
		super(main.getShell());
		this.main = main;
		this.dir = dir;
		setBlockOnOpen(false);
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(disk_share_title);
	}
	
	@Override
	protected void handleShellCloseEvent() {
		if(fss != null && !fss.isDisposed())
			fss.setVisible(false);
		super.handleShellCloseEvent();
	}
	
	private void createFriendSelectionShell(Shell newShell) {
		fss = new FriendSelectionShell(newShell, false);
		fss.setModel(main.getBlindHelper().getFriendGroupList());
		fss.addFriendSelectionListener(this);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite control = (Composite)super.createDialogArea(parent);
		
		UITool.setDefaultBackground(control.getBackground());
		
		UITool.createLabel(control, disk_wulala);
		
		chkShare = UITool.createCheckbox(control, disk_share_set_share);
		chkShare.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				share = chkShare.getSelection();
				if(share) {
					if(fss == null || fss.isDisposed())
						createFriendSelectionShell(getShell());
					fss.setVisible(true);
					viewer.getTable().setEnabled(true);
				} else {
					if(fss != null && !fss.isDisposed())
						fss.setVisible(false);					
					viewer.getTable().setEnabled(false);
				}				
			}
		});
		viewer = new TableViewer(control, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION | SWT.V_SCROLL);
		viewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
		newList = new ArrayList<Integer>();
		viewer.setContentProvider(new ListContentProvider<Integer>(newList));
		viewer.setLabelProvider(new TableLabelProvider());
		new TableColumn(viewer.getTable(), SWT.LEFT);
		viewer.getTable().addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				Table t = (Table)e.getSource();
				t.getColumn(0).setWidth(t.getClientArea().width);
			}
		});
		viewer.getTable().setHeaderVisible(false);
		viewer.getTable().setLinesVisible(false);
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				getButton(REMOVE_ID).setEnabled(!event.getSelection().isEmpty());
			}
		});
		viewer.setInput(this);
		
		viewer.getTable().setEnabled(false);
		chkShare.setEnabled(false);
		
		return control;
	}
	
	@Override
	protected Control createButtonBar(Composite parent) {
		Control c = super.createButtonBar(parent);
		getButton(IDialogConstants.OK_ID).setEnabled(false);
		getButton(REMOVE_ID).setEnabled(false);
		return c;
	}
	
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		createButton(parent, REMOVE_ID, button_delete, false).addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection s = (IStructuredSelection)viewer.getSelection();
				for(Iterator<?> i = s.iterator(); i.hasNext(); ) {
					Integer qq = (Integer)i.next();
					User u = ModelRegistry.getUser(qq);
					if(u != null)
						fss.deselect(u);
					newList.remove(qq);
				}
				viewer.refresh();
			}
		});
	}

	@Override
	protected Point getInitialSize() {
		return new Point(500, 400);
	}
	
	@Override
	public int open() {
		IJob job = new GetShareListJob(dir);
		job.addJobListener(this);
		main.getDiskJobQueue().addJob(job);
		return super.open();
	}

	/**
	 * @return the share
	 */
	public boolean isShare() {
		return share;
	}

	/**
	 * @param share the share to set
	 */
	public void setShare(boolean share) {
		this.share = share;
	}

	public void friendSelected(FriendSelectionEvent e) {
		for(Model u : e.getModels())
			newList.add(((User)u).qq);
		viewer.refresh();
	}

	public void friendDeselected(FriendSelectionEvent e) {
		for(Model u : e.getModels())
			newList.remove(new Integer(((User)u).qq));
		viewer.refresh();
	}
	
	@Override
	protected void okPressed() {
		// 检查列表是否为空
		if(share && newList.isEmpty()) {
			MessageDialog.openWarning(getShell(), message_box_common_warning_title, disk_hint_cannot_empty);
			return;
		}
		
		if(!share)
			newList.clear();
		
		getButton(IDialogConstants.OK_ID).setEnabled(false);
		viewer.getTable().setEnabled(false);
		fss.setVisible(false);
		
		for(Integer qq : newList)
			oldList.remove(qq);
		IJob job = null;
		if(share) {
			job = new SetShareListJob(dir, false, oldList, newList);
			job.addJobListener(this);
			main.getDiskJobQueue().addJob(job);
		} else {
			job = new SetShareListJob(dir, true, oldList, newList);
			job.addJobListener(this);
			main.getDiskJobQueue().addJob(job);
		}
	}
	
	@Override
	protected void cancelPressed() {
		getShell().close();
	}

	public void jobSuccess(final JobEvent e) {
		if(getShell() == null || getShell().isDisposed())
			return;
		
		main.getDisplay().syncExec(new Runnable() {
			public void run() {
				if(e.job instanceof GetShareListJob) {
					GetShareListJob job = (GetShareListJob)e.job;
					oldList = job.getFriends();
					newList.addAll(oldList);
					viewer.refresh();
					viewer.getTable().setEnabled(true);
					getButton(IDialogConstants.OK_ID).setEnabled(true);
					chkShare.setEnabled(true);
					share = false;
					if(!oldList.isEmpty()) {
						chkShare.setSelection(true);
						share = true;
						if(fss == null || fss.isDisposed())
							createFriendSelectionShell(getShell());
						for(Integer qq : oldList) {
							User u = ModelRegistry.getUser(qq);
							if(u != null)
								fss.select(u);
						}
						fss.setVisible(true);
					}
				} else {
					MessageDialog.openInformation(getShell(), message_box_common_info_title, disk_hint_set_share_success);
					close();		
					if(share)
						dir.property |= QQ.QQ_DISK_FLAG_SHARED;
					else
						dir.property &= ~QQ.QQ_DISK_FLAG_SHARED;
					main.getDiskViewer().refresh();
				}
			}
		});
	}

	public void jobFailed(final JobEvent e) {
		if(getShell() == null || getShell().isDisposed())
			return;
		
		main.getDisplay().syncExec(new Runnable() {
			public void run() {
				if(e.job instanceof GetShareListJob) {
					MessageDialog.openError(getShell(), message_box_common_fail_title, disk_hint_cannot_get_share_list);
					close();				
				} else {
					MessageDialog.openError(getShell(), message_box_common_fail_title, disk_hint_cannot_set_share_list);
					close();	
				}
			}
		});
	}
}
