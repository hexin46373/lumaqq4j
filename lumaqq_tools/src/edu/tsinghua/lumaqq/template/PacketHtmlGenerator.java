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
import edu.tsinghua.lumaqq.tools.PacketBean;
import edu.tsinghua.lumaqq.tools.EventBean;

/**
 * 模板
 *
 * @author luma
 */
@SuppressWarnings("unchecked")
public class PacketHtmlGenerator implements IGenerator {
  protected static String nl;
  public static synchronized PacketHtmlGenerator create(String lineSeparator)
  {
    nl = lineSeparator;
    PacketHtmlGenerator result = new PacketHtmlGenerator();
    nl = null;
    return result;
  }

  protected final String NL = nl == null ? (System.getProperties().getProperty("line.separator")) : nl;
  protected final String TEXT_1 = "<HTML>" + NL;
  protected final String TEXT_2 = NL;
  protected final String TEXT_3 = NL + NL + "<BODY class=composite>" + NL;
  protected final String TEXT_4 = NL;
  protected final String TEXT_5 = NL;
  protected final String TEXT_6 = NL;
  protected final String TEXT_7 = NL + NL + "<DIV id=leftColumn>" + NL + "\t<DIV id=navcolumn>" + NL + "\t\t<DIV id=LumaQQ_protocol>" + NL + "\t\t\t<H5>";
  protected final String TEXT_8 = "</H5>" + NL + "\t\t\t<UL>" + NL + "\t\t\t";
  protected final String TEXT_9 = NL + "\t\t\t\t<LI class=none><A href=\"";
  protected final String TEXT_10 = "\">";
  protected final String TEXT_11 = "</A></LI>" + NL + "\t\t\t";
  protected final String TEXT_12 = NL + "\t\t\t</UL>" + NL + "\t\t</DIV>" + NL + "\t</DIV>" + NL + "</DIV>" + NL + "" + NL + "<DIV id=bodyColumn>" + NL + "\t<DIV class=contentBox>" + NL + "\t\t<DIV class=section>" + NL + "\t\t\t<H2>";
  protected final String TEXT_13 = "</H2>" + NL + "\t\t\t<P>";
  protected final String TEXT_14 = "</P>" + NL + "\t\t</DIV>" + NL + "\t\t<DIV class=section>" + NL + "\t\t\t<H2>";
  protected final String TEXT_15 = "</H2>" + NL + "\t\t\t<P>" + NL + "\t\t\t\t";
  protected final String TEXT_16 = NL + "\t\t\t\t";
  protected final String TEXT_17 = NL + "\t\t\t\t";
  protected final String TEXT_18 = NL + "\t\t\t\t<UL>" + NL + "\t\t\t\t";
  protected final String TEXT_19 = NL + "\t\t\t\t\t<LI><A href=\"";
  protected final String TEXT_20 = "\">";
  protected final String TEXT_21 = "</A></LI>" + NL + "\t\t\t\t";
  protected final String TEXT_22 = NL + "\t\t\t\t</UL>" + NL + "\t\t\t\t";
  protected final String TEXT_23 = NL + "\t\t\t</P>" + NL + "\t\t</DIV>" + NL + "\t\t<DIV class=section>" + NL + "\t\t\t<H2>";
  protected final String TEXT_24 = "</H2>" + NL + "\t\t\t<P>" + NL + "\t\t\t\t";
  protected final String TEXT_25 = NL + "\t\t\t\t";
  protected final String TEXT_26 = NL + "\t\t\t\t";
  protected final String TEXT_27 = NL + "\t\t\t\t<UL>" + NL + "\t\t\t\t";
  protected final String TEXT_28 = NL + "\t\t\t\t\t<LI><A href=\"";
  protected final String TEXT_29 = "\">";
  protected final String TEXT_30 = "</A></LI>" + NL + "\t\t\t\t";
  protected final String TEXT_31 = NL + "\t\t\t\t</UL>" + NL + "\t\t\t\t";
  protected final String TEXT_32 = NL + "\t\t\t</P>" + NL + "\t\t</DIV>" + NL + "\t</DIV>" + NL + "</DIV>" + NL;
  protected final String TEXT_33 = NL;
  protected final String TEXT_34 = NL + NL + "</BODY>" + NL + "" + NL + "</HTML>";

	public String generate(Object argument)
  {
    final StringBuffer stringBuffer = new StringBuffer();
    
	Map<String, Object> params = (Map<String, Object>)argument;
	PacketBean bean = (PacketBean)params.get(PACKET_BEAN);
	String menuName = (String)params.get(MENU_NAME);
	List<FamilyBean> beans = (List<FamilyBean>)params.get(FAMILY_BEAN_LIST);
	HeaderDivGenerator headerDivGen = new HeaderDivGenerator();
	BannerDivGenerator bannerDivGen = new BannerDivGenerator();
	BreadcrumbDivGenerator breadcrumbDivGen = new BreadcrumbDivGenerator();
	FooterDivGenerator footerDivGen = new FooterDivGenerator();

    stringBuffer.append(TEXT_1);
    stringBuffer.append(TEXT_2);
    stringBuffer.append( headerDivGen.generate(argument) );
    stringBuffer.append(TEXT_3);
    stringBuffer.append(TEXT_4);
    stringBuffer.append( bannerDivGen.generate(argument) );
    stringBuffer.append(TEXT_5);
    stringBuffer.append(TEXT_6);
    stringBuffer.append( breadcrumbDivGen.generate(argument) );
    stringBuffer.append(TEXT_7);
    stringBuffer.append( menuName );
    stringBuffer.append(TEXT_8);
     for(FamilyBean fb : beans) { 
    stringBuffer.append(TEXT_9);
    stringBuffer.append( "../family/" + fb.html );
    stringBuffer.append(TEXT_10);
    stringBuffer.append( fb.name );
    stringBuffer.append(TEXT_11);
     } 
    stringBuffer.append(TEXT_12);
    stringBuffer.append( bean.name );
    stringBuffer.append(TEXT_13);
    stringBuffer.append( bean.format );
    stringBuffer.append(TEXT_14);
    stringBuffer.append( EVENT_LIST );
    stringBuffer.append(TEXT_15);
     if(bean.events.isEmpty()) { 
    stringBuffer.append(TEXT_16);
    stringBuffer.append( NO_EVENT_LIST );
    stringBuffer.append(TEXT_17);
     } else { 
    stringBuffer.append(TEXT_18);
     for(EventBean eb : bean.events) { 
    stringBuffer.append(TEXT_19);
    stringBuffer.append( "../event/" + eb.html );
    stringBuffer.append(TEXT_20);
    stringBuffer.append( eb.name );
    stringBuffer.append(TEXT_21);
     } 
    stringBuffer.append(TEXT_22);
     } 
    stringBuffer.append(TEXT_23);
    stringBuffer.append( RELATED_PACKET );
    stringBuffer.append(TEXT_24);
     if(bean.relatedPackets.isEmpty()) { 
    stringBuffer.append(TEXT_25);
    stringBuffer.append( NO_RELATED_PACKET );
    stringBuffer.append(TEXT_26);
     } else { 
    stringBuffer.append(TEXT_27);
     for(PacketBean pb : bean.relatedPackets) { 
    stringBuffer.append(TEXT_28);
    stringBuffer.append( pb.html );
    stringBuffer.append(TEXT_29);
    stringBuffer.append( pb.name );
    stringBuffer.append(TEXT_30);
     } 
    stringBuffer.append(TEXT_31);
     } 
    stringBuffer.append(TEXT_32);
    stringBuffer.append(TEXT_33);
    stringBuffer.append( footerDivGen.generate(argument) );
    stringBuffer.append(TEXT_34);
    return stringBuffer.toString();
  }
}