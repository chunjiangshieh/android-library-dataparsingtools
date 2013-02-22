package com.xcj.android.dat.xml.parse;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Class representing a single node in an XML tree.
 */

public class Node {
	/**
	 * The list of children of this node
	 */
	public Vector children;

	/**
	 * The parent of this node
	 */
	private Node parent;

	/**
	 * The name of this node
	 */
	private String tagName;

	/**
	 * The text contained in this node.
	 */
	private StringBuffer text;

	/**
	 * The attributes set in this nodes tag.
	 */
	public Hashtable attributes;

	public int type;

	/**
	 * default constructor
	 */
	public Node() {
		type = -1;
	}

	/**
	 * Constructor. Copies details of the parents and node name
	 * 
	 * @param _parent
	 *            The parent of this node.
	 * @param _name
	 *            The name of this node.
	 */

	public Node(Node _parent, String _name, Hashtable _attributes) {
		tagName = _name;
		attributes = _attributes;

		parent = _parent;
		if (parent != null) {
			parent.addChild(this);
		}
	}

	public Node(Node _parent, String _name, Hashtable _attributes, int _type) {
		tagName = _name;
		attributes = _attributes;
		parent = _parent;
		if (parent != null) {
			parent.addChild(this);
		}
		type = _type;
	}

	/**
	 * set node's name
	 * 
	 * @param name
	 */
	public void setName(String name) {
		tagName = name;
	}

	/**
	 * set parent
	 * 
	 * @param node
	 */
	public void setParent(Node node) {
		parent = node;
		if (parent != null) {
			parent.addChild(this);
		}
	}

	/**
	 * Method to add some text to th text area of the tag.
	 * 
	 * @param newText
	 *            The text to add.
	 */
	public void addText(String newText) {
		if (text == null)
			text = new StringBuffer();

		text.append(newText);
	}

	/**
	 * Method to set the text area of the tag to a specific value.
	 * 
	 * @param newText
	 *            The text to use.
	 */
	public void setText(String newText) {
		text = new StringBuffer(newText);
	}

	/**
	 * Method to get the text from this node
	 */
	public String getText() {
		String textString = "";
		if (text != null) {
			textString = text.toString();
		}
		return textString;
	}

	/**
	 * Method to get the name of this node.
	 */
	public String getName() {
		return tagName;
	}

	/**
	 * Method to add a child to this node
	 * 
	 * @param childNode
	 *            The node to add.
	 */
	public void addChild(Node childNode) {
		if (children == null){
			children = new Vector();
		}
		children.addElement(childNode);
	}

	/**
	 * Method to remove a child from this node
	 * 
	 * @param childNode
	 *            The node to remove.
	 */
	public boolean removeChild(Node childNode) {
		boolean removed = children.removeElement(childNode);
		if (removed)
			return true;
		if (children == null)
			return false;
		synchronized (children) {
			Enumeration childIterator = children.elements();
			while (childIterator.hasMoreElements()) {
				Node thisChild = (Node) childIterator.nextElement();
				removed = thisChild.removeChild(childNode);
				if (removed)
					return true;
			}
		}
		return false;
	}

	/**
	 * Method to get all the children (and childrens children ) of a specific
	 * name.
	 * 
	 * @param name
	 *            The name of the nodes to fetch.
	 * @return A vector of all the nodes of that name.
	 */
	public Vector getChildrenByName(String name) {
		Vector namedChildren = new Vector();
		getChildrenByName(name, namedChildren);
		return namedChildren;
	}

	public Node getFirstChildrenByName(String name) {
		Vector namedChildren = new Vector();

		name = name.toLowerCase();
		getChildrenByName(name, namedChildren);
		try {
			synchronized (children) {
				Enumeration childIterator = children.elements();
				while (childIterator.hasMoreElements()) {
					Node thisChild = (Node) childIterator.nextElement();

					String nodeName = thisChild.getName().toLowerCase();
					if (nodeName.equals(name)) {
						return thisChild;
					}
					// thisChild.getChildrenByName( name, store );
				}
			}
		} catch (Exception e) {
		}
		return null;
	}

	/**
	 * Method to get all the children (and childrens children ) of a specific
	 * name into a given Collection object.
	 * 
	 * @param name
	 *            The name of the nodes to fetch.
	 * @return A vector of all the nodes of that name.
	 */
	protected void getChildrenByName(String name, Vector store) {
		if (children == null)
			return;

		try {
			synchronized (children) {
				Enumeration childIterator = children.elements();
				while (childIterator.hasMoreElements()) {
					Node thisChild = (Node) childIterator.nextElement();

					String nodeName = thisChild.getName();
					if (nodeName.equals(name)) {
						store.addElement(thisChild);
					}
					// thisChild.getChildrenByName( name, store );
				}
			}
		} catch (Exception e) {
		}
	}


	/**
	 * Method to get the parent of this node
	 */

	public Node getParent() {
		return parent;
	}

	/**
	 * Method to return the data as a byte stream ready to send over the wire
	 * 
	 * @return The data to send as a byte array
	 */

	public byte[] getBytes() {
		String data = toString();
		return data.getBytes();
	}

	/**
	 * Method to get the XML representation of this node.
	 * 
	 * @return A string holding the XML representation.
	 */
	public String toString() {
		String nodeName = getName();
		StringBuffer xmlRepresentation = new StringBuffer("<");
		xmlRepresentation.append(nodeName);
		if (attributes != null) {
			synchronized (attributes) {
				Enumeration attrIter = attributes.keys();
				while (attrIter.hasMoreElements()) {
					String key = (String) attrIter.nextElement();
					String value = (String) attributes.get(key);
					xmlRepresentation.append(' ');
					xmlRepresentation.append(key);
					xmlRepresentation.append("=\'");
					xmlRepresentation.append(value);
					xmlRepresentation.append("\'");
				}
			}
		}
		if ((children == null || children.size() == 0)
				&& (text == null || text.length() == 0)) {
			xmlRepresentation.append("/>");
			return xmlRepresentation.toString();
		}
		xmlRepresentation.append(">");
		if (text != null) {
			xmlRepresentation.append(text.toString());
		}
		if (children != null) {
			synchronized (children) {
				Enumeration iter = children.elements();
				while (iter.hasMoreElements()) {
					Object nextObject = iter.nextElement();
					String nodeRepresentation = nextObject.toString();
					xmlRepresentation.append(nodeRepresentation);
				}
			}
		}
		xmlRepresentation.append("</");
		xmlRepresentation.append(nodeName);
		xmlRepresentation.append(">");
		return xmlRepresentation.toString();
	}

	/**
	 * Method to get an attribute
	 * 
	 * @param attributeName
	 *            The name of the attribute to get
	 * @return The value of the attribute
	 */
	public String getAttribute(String attributeName) {
		if (attributes == null || attributeName == null)
			return null;

		return (String) attributes.get(attributeName);
	}


	public long getAttributeLong(String attributeName){
		if (attributes == null || attributeName == null)
			return -1L;
		String str =  (String)attributes.get(attributeName);
		return Long.parseLong(str);
	}

	/**
	 * Method to set an attribute value
	 * 
	 * @param attributeName
	 *            The name of the attribute to set
	 * @param value
	 *            The value of the attribute
	 */
	public void setAttribute(String attributeName, String value) {
		if (attributeName == null || value == null)
			return;
		if (attributes == null)
			attributes = new Hashtable();
		attributes.put(attributeName, value);
	}

	/**
	 * Method to remove an attribute.
	 * 
	 * @param attributeName
	 *            The attribute name to remove.
	 */
	public void removeAttribute(String attributeName) {
		if (attributes == null)
			return;

		attributes.remove(attributeName);
	}

	/**
	 * Method to replace a child block with another. The tag name and namespace
	 * (xmlns attribute) must be the same.
	 * <p>
	 * If there is no current child which matches the name and
	 * 
	 * 
	 * @param replacementNode
	 *            The replacement node.
	 */
	public void replaceNode(Node replacementNode) {
		String replacementNodeName = replacementNode.getName();

		String nameSpace = null;
		if (replacementNode.attributes != null)
			;
		nameSpace = (String) replacementNode.attributes.get("xmlns");

		if (children == null) {
			addChild(replacementNode);
			return;
		}

		synchronized (children) {
			Enumeration childIter = children.elements();
			while (childIter.hasMoreElements()) {
				Node thisNode = (Node) childIter.nextElement();

				String thisNodeName = thisNode.getName();
				if (thisNodeName.equals(replacementNodeName) == false)
					continue;

				if (nameSpace == null) {
					children.removeElement(thisNode);
					addChild(replacementNode);
					return;
				}

				if (thisNode.attributes == null)
					continue;

				String thisNodeNamespace = (String) thisNode.attributes
						.get("xmlns");
				if (nameSpace.equals(thisNodeNamespace)) {
					children.removeElement(thisNode);
					addChild(replacementNode);
					return;
				}
			}
		}

		addChild(replacementNode);
	}

	/**
	 * Method to get all the children of the current node
	 * 
	 * @return A List of all the children of this node
	 */

	public Vector getChildren() {
		if (children == null)
			return null;

		return children;
	}

	public int getChildCount() {
		if (children == null)
			return 0;

		return children.size();
	}

	public Node getChild(int location) {
		if (children == null || location < 0 
				|| location >= children.size())
			return null;

		return (Node)children.elementAt(location);
	}


}
