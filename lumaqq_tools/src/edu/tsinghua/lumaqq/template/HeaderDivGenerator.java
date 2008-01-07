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


/**
 * 模板
 *
 * @author luma
 */
@SuppressWarnings("unchecked")
public class HeaderDivGenerator implements IGenerator {
  protected static String nl;
  public static synchronized HeaderDivGenerator create(String lineSeparator)
  {
    nl = lineSeparator;
    HeaderDivGenerator result = new HeaderDivGenerator();
    nl = null;
    return result;
  }

  protected final String NL = nl == null ? (System.getProperties().getProperty("line.separator")) : nl;
  protected final String TEXT_1 = "<HEAD>" + NL + "\t<TITLE>LumaQQ Protocol Manual</TITLE>" + NL + "\t<STYLE type=text/css media=all>@import url( ../css/base.css );@import url( ../css/theme.css );</STYLE>" + NL + "\t<LINK media=print href=\"../css/print.css\" type=text/css rel=stylesheet></LINK>" + NL + "\t<META http-equiv=Content-Type content=\"text/html; charset=GBK\"></META>" + NL + "</HEAD>";

	public String generate(Object argument)
  {
    final StringBuffer stringBuffer = new StringBuffer();
    stringBuffer.append(TEXT_1);
    return stringBuffer.toString();
  }
}