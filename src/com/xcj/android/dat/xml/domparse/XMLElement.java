package com.xcj.android.dat.xml.domparse;

import java.util.Vector;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XMLElement {

	Element m_element;

	Vector<Node> childNode;

	protected XMLElement(Element node) {
		m_element = node;
		if (m_element.hasChildNodes()) {
			childNode = new Vector<Node>();
			NodeList list = m_element.getChildNodes();
			for (int i = 0; i < list.getLength(); i++) {
				Node n = list.item(i);
				if (n.getNodeType() == Node.ELEMENT_NODE) {
					childNode.addElement(n);
				}
			}
		}

	}

	public String getName() {
		return m_element.getNodeName();
	}

	public String getText() {

		return getNodeValue(m_element);
	}

	public int getElementCount() {
		if (childNode != null)
			return childNode.size();
		else
			return 0;
		// return m_element.getChildNodes().getLength();
	}

	/**
	 * 获得XMLElement中第一个元素. 注.每次获得的XMLElement都重新生成XMLElement对象，
	 * 
	 * @return
	 */
	public XMLElement getFirstElement() {
		if (childNode.isEmpty()) {
			return null;
		} else {
			return new XMLElement((Element) childNode.firstElement());
		}
	}

	/**
	 * 获得XMLElement中指定位置的Element. 注.每次获得的XMLElement都重新生成XMLElement对象，
	 * 
	 * @param index
	 * @return
	 */
	public XMLElement getElement(int index) {
		if (index >= 0 && index < getElementCount()) {
			return new XMLElement((Element) childNode.elementAt(index));
		}
		return null;
	}

	public XMLElement getElement(String name) {

		NodeList list = m_element.getElementsByTagName(name);
		if (list != null && list.getLength() > 0) {
			return new XMLElement((Element) list.item(0));
		}
		return null;
	}

	public String getAttribute(String name) {
		return m_element.getAttribute(name);
	}

	public XMLElement getElementByPath(String path) {
		Element e = getElementImplByPath(path);
		if (e != null) {
			return new XMLElement(e);
		} else {
			return null;
		}
	}

	public String getTextByPath(String path) {
		Element element = getElementImplByPath(path);
		if (element != null) {
			return getNodeValue(element);
		} else {
			return "";
		}
	}

	public String getAttributeByPath(String path, String attname) {
		Element e = getElementImplByPath(path);
		if (e != null) {
			return e.getAttribute(attname);
		} else {
			return null;
		}
	}

	public int getInt() {
		try {
			String value = getNodeValue(m_element);
			return Integer.parseInt(value);
		} catch (Exception e) {
			// TODO: handle exception
			return 0;
		}
	}

	public boolean getBoolean() {
		try {
			String value = getNodeValue(m_element);
			return Boolean.parseBoolean(value);
		} catch (Exception e) {
			// TODO: handle exception
			return false;
		}
	}

	public int getIntByPath(String path) {
		String value = getTextByPath(path);
		if (value == null) {
			return 0;
		} else {
			try {
				return Integer.parseInt(value);
			} catch (Exception e) {
				// TODO: handle exception
				return 0;
			}
		}

	}

	public boolean getBooleanByPath(String path) {
		String value = getTextByPath(path);
		if (value == null) {
			return false;
		} else {
			try {
				return Boolean.parseBoolean(value);
			} catch (Exception e) {
				// TODO: handle exception
				return false;
			}
		}
	}

	private Element getElementImplByPath(String path) {
		String[] paths = path.split("/");

		if (paths.length > 0) {
			Element node = m_element;
			for (int i = 0; i < paths.length; i++) {
				if (node != null && node.getNodeName().equals(paths[i])) {
					if (i < paths.length - 1) {
						NodeList list = node.getElementsByTagName(paths[i + 1]);
						if (list != null && list.getLength() > 0) {
							for (int j = 0; j < list.getLength(); j++) {
								if (j == 0)
									node = (Element) list.item(j);
								else {
									NodeList nodeList = ((Element) list.item(j)).getChildNodes();
									for (int k = 0; k < nodeList.getLength(); k++) {
										node.appendChild(nodeList.item(k));
									}
								}
								
							}
							
						}
					}
				} else {
					return null;
				}
			}
			return node;
			// if(m_element.getNodeName().equals(paths[0])){
			// Element root = m_element;
			// if(path.length()>1){
			// return root;
			// }
			// int index = 1;
			// while (root!=null && index < paths.length){
			// NodeList list = root.getElementsByTagName(paths[index]);
			// if(list != null && list.getLength() > 0 ){
			// root = (Element)list.item(0);
			// index++;
			// if ( index == paths.length ) return root;
			// }else{
			// return null;
			// }
			// }
			// return null;
			// }
		}
		return null;
	}

	private String getNodeValue(Node node) {
		NodeList list = node.getChildNodes();

		StringBuffer valueStr = new StringBuffer();
		for (int i = 0; i < list.getLength(); i++) {
			short nodetype = list.item(i).getNodeType();
			if (i > 0)
				valueStr.append(",");
			if (nodetype == Node.TEXT_NODE) {
				valueStr.append(list.item(i).getNodeValue());

			} else if (nodetype == Node.CDATA_SECTION_NODE) {
				valueStr.append(list.item(i).getNodeValue());
			}
		}
		return valueStr.toString();
	}
}
