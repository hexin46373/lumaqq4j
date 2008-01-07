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
package edu.tsinghua.lumaqq.test;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import edu.tsinghua.lumaqq.widgets.qstyle.DieAway;

/**
 * @author luma
 */
@SuppressWarnings("unused")
public class DieAwayTest {
    private Shell shell;
    private Display display;
    private Image image;
    private DieAway die;
    
    public static void main(String[] args) {
        DieAwayTest t = new DieAwayTest();
        t.open();
    }
    
	/**
	 * 打开对话框
	 */
	public void open()	{		
		// event loop
	    shell = new Shell(new Display());
	    display = shell.getDisplay();
	    shell.setLayout(new GridLayout());
	    shell.setSize(400, 300);
	    shell.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
	    
	    die = new DieAway(shell);
	    
	    Button btn = new Button(shell, SWT.PUSH);
	    btn.setText("Close hint window");
	    btn.addSelectionListener(new SelectionAdapter() {
	    	public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
	    		if(!die.isDisposed())
	    			die.close();
	    	}
	    });
	    shell.addMouseListener(new MouseListener() {

			public void mouseDoubleClick(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void mouseDown(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void mouseUp(MouseEvent e) {
				if(die.isDisposed())
					die = new DieAway(shell);				
				die.open();
			}
	    	
	    });
	    shell.layout();
	    shell.open();
		while(!shell.isDisposed()) 
			if(!display.readAndDispatch()) 
			    display.sleep();
	}
}
