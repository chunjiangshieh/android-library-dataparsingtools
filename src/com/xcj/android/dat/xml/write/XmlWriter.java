/*head = new Hashtable<String, String>();
args = new Hashtable<String, String>();
XmlWriter.appendChildNodeStart(mStrBuff, "ROOT");
XmlWriter.toWriteXml(mStrBuff, "HEAD", head);				
XmlWriter.toWriteXml(mStrBuff, "BODY", args);			
XmlWriter.appendChildNodeEnd(mStrBuff, "ROOT");
String postData = mStrBuff.toString();*/
package com.xcj.android.dat.xml.write;

import java.util.Enumeration;
import java.util.Hashtable;


/**
 * 生成Xml字串.
 * @author chunjiang.shieh
 *
 */
public class XmlWriter {
	
	
	/**
	 * 根据结点名称，以及子结点的名称与内容，生成xml结构写入指定StringBuffer中
	 * @param sbf
	 * @param args
	 */
	public static void toWriteXml(StringBuffer sbf,String nodeName,Hashtable<String,String> childNode){
		appendChildNodeStart(sbf,nodeName);
		Enumeration<String> enu = childNode.keys();
		String name = null;
		String value = null;
		while (enu.hasMoreElements()) {
			name  = enu.nextElement();
			value = childNode.get(name);
			appendChildNode(sbf,name,value);
		}
		appendChildNodeEnd(sbf,nodeName);
	}
	
	/**
	 * 根据结点名称，以及子结点的名称与内容，生成xml结构写入指定StringBuffer中
	 * @param sbf
	 * @param args
	 */
	public static void toWriteChildNodeXml(StringBuffer sbf,Hashtable<String,String> childNode){
		
		Enumeration<String> enu = childNode.keys();
		String name = null;
		String value = null;
		while (enu.hasMoreElements()) {
			name  = enu.nextElement();
			value = childNode.get(name);
			appendChildNode(sbf,name,value);
		}
	
	}
	
	
	
	
	

	/**
	 * 添加结点开启
	 * @param sb
	 * @param name
	 */
	public static void appendChildNodeStart(StringBuffer sb,String name){		
		sb.append("<");
		sb.append(name);
		sb.append(">");
	}
	
	/**
	 * 添加结点结束
	 * @param sb
	 * @param name
	 */
	public static void appendChildNodeEnd(StringBuffer sb,String name){
		sb.append("</");
		sb.append(name);
		sb.append(">");
	}
	
	
	/**
	 * 添加子节点
	 * @param sb
	 * @param name
	 * @param value
	 */
	public static void appendChildNode(StringBuffer sb,String name,String value){
		if(name != null && value != null){
			sb.append("<");
			sb.append(name);
			sb.append(">");
			sb.append(value);
			sb.append("</");
			sb.append(name);
			sb.append(">");	
		}
	}
}
