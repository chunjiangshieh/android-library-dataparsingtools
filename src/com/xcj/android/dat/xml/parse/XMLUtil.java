package com.xcj.android.dat.xml.parse;

/**
 * XML 解析
 * @author chunjiang.shieh
 *
 */
public class XMLUtil {
	/**
	 * Float型子结点内容
	 * @param node
	 * @param childNodeName
	 * @return
	 */
	public static float getChildFloat(Node node,String childNodeName){
		String txt = getChildText(node,childNodeName);
		if(txt == null || txt.length() == 0 ){
			return 0;
		}else{
			return Float.parseFloat(txt);
		}
	}

	/**
	 * Int型子结点内容
	 * @param node
	 * @param childNodeName
	 * @return
	 */
	public static int getChildInt(Node node,String childNodeName){
		String txt = getChildText(node,childNodeName);
		if(txt == null || txt.length() == 0 ){
			return 0;
		}else{
			return Integer.parseInt(txt);
		}
	}

	/**
	 * String子结点内容
	 * @param node
	 * @param childNodeName
	 * @return
	 */
	public static String getChildText(Node node,String childNodeName){
		Node child = node.getFirstChildrenByName(childNodeName);
		if(child != null){
			return child.getText();
		}else{
			return null;
		}
	}
}
