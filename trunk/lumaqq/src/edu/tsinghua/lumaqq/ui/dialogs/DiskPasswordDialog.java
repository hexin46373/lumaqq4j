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
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import edu.tsinghua.lumaqq.resource.Colors;
import edu.tsinghua.lumaqq.ui.MainShell;
import edu.tsinghua.lumaqq.ui.helper.UITool;

/**
 * 网络硬盘密码设置对话框
 *
 * @author luma
 */
public class DiskPasswordDialog extends Dialog {
	private Text textOld;
	private Text textNew;
	private Text textConfirm;
	private Button rdoSet;
	private Button rdoCancel;
	
	private String oldPassword;
	private String newPassword;
	private String confirmNewPassword;
	private boolean setPassword;
	
	private MainShell main;

	public DiskPasswordDialog(MainShell main) {
		super(main.getShell());
		this.main = main;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Password");
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite control = (Composite)super.createDialogArea(parent);
		
		UITool.setDefaultBackground(control.getBackground());
		
		Group group = UITool.createGroup(control, "");
		rdoSet = UITool.createRadio(group, disk_password_set);
		rdoSet.setSelection(true);
		rdoSet.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(rdoSet.getSelection()) {
					resetControl(false);
				}
			}
		});
		rdoCancel = UITool.createRadio(group, disk_password_cancel);
		rdoCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(rdoCancel.getSelection()) {
					resetControl(true);
				}
			}			
		});
		
		group = UITool.createGroup(control, "");
		UITool.createLabel(group, disk_password_old);
		textOld = UITool.createSingleText(group, new GridData(GridData.FILL_HORIZONTAL), SWT.SINGLE | SWT.BORDER | SWT.PASSWORD);
		textOld.setBackground(Colors.WHITE);
		textOld.setTextLimit(16);
		textOld.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				oldPassword = textOld.getText();
			}
		});
		UITool.createLabel(group, disk_password_new);
		textNew = UITool.createSingleText(group, new GridData(GridData.FILL_HORIZONTAL), SWT.SINGLE | SWT.BORDER | SWT.PASSWORD);
		textNew.setTextLimit(16);
		textNew.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				newPassword = textNew.getText();
			}
		});
		UITool.createLabel(group, disk_password_confirm);
		textConfirm = UITool.createSingleText(group, new GridData(GridData.FILL_HORIZONTAL), SWT.SINGLE | SWT.BORDER | SWT.PASSWORD);
		textConfirm.setTextLimit(16);
		textConfirm.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				confirmNewPassword = textConfirm.getText();
			}
		});
		
		resetControl(false);
		
		return control;
	}
	
	private void resetControl(boolean cancelPassword) {
		setPassword = !cancelPassword;
		textNew.setEnabled(!cancelPassword);
		textNew.setBackground(cancelPassword ? textNew.getParent().getBackground() : Colors.WHITE);
		textConfirm.setEnabled(!cancelPassword);
		textConfirm.setBackground(cancelPassword ? textConfirm.getParent().getBackground() : Colors.WHITE);
	}
	
	@Override
	protected void okPressed() {
		String savePassword = main.getDiskManager().getPassword();
		if(savePassword != null && !savePassword.equals(oldPassword)) {
			MessageDialog.openWarning(getShell(), message_box_common_warning_title, message_box_old_password_incorrect);
		} else if(setPassword) {
			if(newPassword == null || confirmNewPassword == null || newPassword.length() < 6 || confirmNewPassword.length() < 6)
				MessageDialog.openWarning(getShell(), message_box_common_warning_title, NLS.bind(message_box_password_length, "6", "16"));
			else if(!newPassword.equals(confirmNewPassword))
				MessageDialog.openWarning(getShell(), message_box_common_warning_title, message_box_new_password_confirm);
			else
				super.okPressed();
		} else
			super.okPressed();
	}

	/**
	 * @return the confirmNewPassword
	 */
	public String getConfirmNewPassword() {
		return confirmNewPassword;
	}

	/**
	 * @return the newPassword
	 */
	public String getNewPassword() {
		return newPassword;
	}

	/**
	 * @return the oldPassword
	 */
	public String getOldPassword() {
		return oldPassword;
	}

	/**
	 * @return the setPassword
	 */
	public boolean isSetPassword() {
		return setPassword;
	}
}
