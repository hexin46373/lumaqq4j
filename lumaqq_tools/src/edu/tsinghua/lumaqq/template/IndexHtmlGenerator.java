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
import java.util.List;
import edu.tsinghua.lumaqq.tools.FamilyBean;

/**
 * 模板
 *
 * @author luma
 */
@SuppressWarnings("unchecked")
public class IndexHtmlGenerator implements IGenerator {
  protected static String nl;
  public static synchronized IndexHtmlGenerator create(String lineSeparator)
  {
    nl = lineSeparator;
    IndexHtmlGenerator result = new IndexHtmlGenerator();
    nl = null;
    return result;
  }

  protected final String NL = nl == null ? (System.getProperties().getProperty("line.separator")) : nl;
  protected final String TEXT_1 = "<HTML>" + NL + "" + NL + "<HEAD>" + NL + "\t<TITLE>LumaQQ Protocol Manual</TITLE>" + NL + "\t<STYLE type=text/css media=all>@import url( ./css/base.css );@import url( ./css/theme.css );</STYLE>" + NL + "\t<LINK media=print href=\"./css/print.css\" type=text/css rel=stylesheet></LINK>" + NL + "\t<META http-equiv=Content-Type content=\"text/html; charset=GBK\"></META>" + NL + "</HEAD>" + NL + "" + NL + "<BODY class=composite>" + NL + "" + NL + "<DIV id=banner>" + NL + "<A id=organizationLogo href=\"http://lumaqq.linuxsir.org/\"><IMG alt=\"LumaQQ\" src=\"./images/lumaqq.gif\"></IMG></A>" + NL + "\t<DIV class=clear>" + NL + "\t<HR></HR>" + NL + "\t</DIV>" + NL + "</DIV>" + NL;
  protected final String TEXT_2 = NL;
  protected final String TEXT_3 = NL + NL + "<DIV id=leftColumn>" + NL + "\t<DIV id=navcolumn>" + NL + "\t\t<DIV id=LumaQQ_protocol>" + NL + "\t\t\t<H5>";
  protected final String TEXT_4 = "</H5>" + NL + "\t\t\t<UL>" + NL + "\t\t\t";
  protected final String TEXT_5 = NL + "\t\t\t\t<LI class=none><A href=\"";
  protected final String TEXT_6 = "\">";
  protected final String TEXT_7 = "</A></LI>" + NL + "\t\t\t";
  protected final String TEXT_8 = NL + "\t\t\t</UL>" + NL + "\t\t</DIV>" + NL + "\t</DIV>" + NL + "</DIV>" + NL + "" + NL + "<DIV id=bodyColumn>" + NL + "\t<DIV class=contentBox>" + NL + "\t\t<DIV class=section>" + NL + "\t\t\t<H2>";
  protected final String TEXT_9 = "</H2>" + NL + "\t\t\t<P>";
  protected final String TEXT_10 = "</P>" + NL + "\t\t</DIV>" + NL + "\t</DIV>" + NL + "</DIV>" + NL;
  protected final String TEXT_11 = NL;
  protected final String TEXT_12 = NL + NL + "</BODY>" + NL + "" + NL + "</HTML>";

	public String generate(Object argument)
  {
    final StringBuffer stringBuffer = new StringBuffer();
    
	Map<String, Object> params = (Map<String, Object>)argument;
	String menuName = (String)params.get(MENU_NAME);
	List<FamilyBean> beans = (List<FamilyBean>)params.get(FAMILY_BEAN_LIST);
	BreadcrumbDivGenerator breadcrumbDivGen = new BreadcrumbDivGenerator();
	FooterDivGenerator footerDivGen = new FooterDivGenerator();

    stringBuffer.append(TEXT_1);
    stringBuffer.append(TEXT_2);
    stringBuffer.append( breadcrumbDivGen.generate(argument) );
    stringBuffer.append(TEXT_3);
    stringBuffer.append( menuName );
    stringBuffer.append(TEXT_4);
     for(FamilyBean bean : beans) { 
    stringBuffer.append(TEXT_5);
    stringBuffer.append( "./family/" + bean.html );
    stringBuffer.append(TEXT_6);
    stringBuffer.append( bean.name );
    stringBuffer.append(TEXT_7);
     } 
    stringBuffer.append(TEXT_8);
    stringBuffer.append( INTRO_TITLE );
    stringBuffer.append(TEXT_9);
    stringBuffer.append( INTRO );
    stringBuffer.append(TEXT_10);
    stringBuffer.append(TEXT_11);
    stringBuffer.append( footerDivGen.generate(argument) );
    stringBuffer.append(TEXT_12);
    return stringBuffer.toString();
  }
}