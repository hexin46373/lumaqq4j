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
package edu.tsinghua.lumaqq.template;


import java.util.Map;
import edu.tsinghua.lumaqq.ui.helper.DateTool;

/**
 * 模板
 *
 * @author luma
 */
@SuppressWarnings("unchecked")
public class BreadcrumbDivGenerator implements IGenerator {
  protected static String nl;
  public static synchronized BreadcrumbDivGenerator create(String lineSeparator)
  {
    nl = lineSeparator;
    BreadcrumbDivGenerator result = new BreadcrumbDivGenerator();
    nl = null;
    return result;
  }

  protected final String NL = nl == null ? (System.getProperties().getProperty("line.separator")) : nl;
  protected final String TEXT_1 = "<DIV id=breadcrumbs>" + NL + "\t<DIV class=xleft>";
  protected final String TEXT_2 = "</DIV>" + NL + "\t<DIV class=xright></DIV>" + NL + "\t<DIV class=clear>" + NL + "\t\t<HR></HR>" + NL + "\t</DIV>" + NL + "</DIV>";

	public String generate(Object argument)
  {
    final StringBuffer stringBuffer = new StringBuffer();
    
	Map<String, Object> params = (Map<String, Object>)argument;
	Long time = (Long)params.get(PUBLISH_TIME);

    stringBuffer.append(TEXT_1);
    stringBuffer.append( PUBLISH_TIME );
    stringBuffer.append( DateTool.format(time) );
    stringBuffer.append(TEXT_2);
    return stringBuffer.toString();
  }
}