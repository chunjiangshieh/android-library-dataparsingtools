package com.xcj.android.dat.xml.parse;

/*
 Note on funding

 If you feel my work on this project is worth something, please make a donation
 to my paypal account (al@alsutton.com) at http://www.paypal.com/
 */

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Hashtable;

/**
 * Class for building an the object tree.
 */

public class TreeBuilder
{
	/**
	 * The current node being parsed.
	 */

	// private Node currentNode;
	/**
	 * The root node of the tree being parsed.
	 */

	private Node rootNode;

	/**
	 * Default constructor.
	 */

	public TreeBuilder()
	{
		// currentNode = null;
	}

	public Node getRootNode()
	{
		return rootNode;
	}

	/**
	 * Method to construct a tree from a reader.
	 * 
	 * @return The root node of the tree.
	 */

	/*
	 * public Node createTree(Reader is) throws Exception { currentNode = null;
	 * XMLParser parser = new XMLParser(this); parser.parse(is);
	 * 
	 * return rootNode; }
	 */

	public Node createTree(byte[] data) throws Exception
	{
		// currentNode = null;
		// XMLParser parser = new XMLParser(this);
		// parser.setInputUTF8Encoded(true);
		// try {
		// parser.parse(data);
		// } catch (Exception e) {
		// }

		rootNode = parseTree(new ByteArrayInputStream(data));

		return rootNode;

	}

	public Node createTree(String str) throws Exception
	{
		// currentNode = null;
		// XMLParser parser = new XMLParser(this);
		// parser.setInputUTF8Encoded(true);
		// try {
		// parser.parse(str);
		// } catch (Exception e) {
		// }

		rootNode = parseTree(new ByteArrayInputStream(str.getBytes("utf-8")));
		return rootNode;

	}

	public Node createTree(InputStream is)
	{
		// currentNode = null;
		// XMLParser parser = new XMLParser(this);
		// parser.setInputUTF8Encoded(true);
		// try {
		// parser.parse(is);
		// } catch (Exception e) {
		// //e.printStackTrace();
		// }
		rootNode = parseTree(is);
		return rootNode;

	}

	/**
	 * Method called when an tag start is encountered.
	 * 
	 * @param name
	 *            Tag name.
	 * @param attributes
	 *            The tags attributes.
	 */

	// public void tagStarted(String name, Hashtable attributes) {
	// Node newNode = new Node(currentNode, name, attributes);
	// if (currentNode == null)
	// rootNode = newNode;
	//
	// currentNode = newNode;
	// }
	//
	// /**
	// * Method called when some plain text between two tags is encountered.
	// *
	// * @param text
	// * The plain text in question.
	// */
	//
	// public void plaintextEncountered(String text) {
	// if (currentNode != null) {
	// currentNode.addText(text);
	// }
	// }
	//
	// /**
	// * The method called when a tag end is encountered.
	// *
	// * @param name
	// * The name of the tag that has just ended.
	// */
	//
	// public void tagEnded(String name) {
	// String currentNodeName = currentNode.getName();
	// if (currentNodeName.equals(name))
	// currentNode = currentNode.getParent();
	// }

	public static Node parseTree(String data)
	{

		try
		{
			byte[] bytes = data.getBytes("utf-8");
			return parseTree(new ByteArrayInputStream(bytes));
		} catch (UnsupportedEncodingException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

	}

	public static Node parseTree(byte[] data)
	{
		return parseTree(new ByteArrayInputStream(data));
	}

	public static Node parseTree(InputStream in)
	{
		XMLParser parser;

		try
		{
			InputStreamReader inputStreamReader = new InputStreamReader(in,
					"utf-8");
			parser = new XMLParser(inputStreamReader);
		} catch (IOException exception)
		{
			throw new RuntimeException("Could not create xml parser."
					+ exception);
		}

		Node root = new Node();
		Node currentNode = root;
		String newName;
		int newType;

		try
		{
			while ((parser.next()) != XMLParser.END_DOCUMENT)
			{
				newName = parser.getName();
				newType = parser.getType();

				if (newType == XMLParser.START_TAG)
				{
					Hashtable attributes = null;
					int attributeCount = parser.getAttributeCount();

					if (attributeCount > 0)
					{
						attributes = new Hashtable();

						for (int i = 0; i < attributeCount; i++)
						{
							attributes.put(parser.getAttributeName(i), parser
									.getAttributeValue(i));
						}
					}

					Node newNode = new Node(currentNode, newName, attributes,
							newType);
					currentNode = newNode;
				}

				else if (newType == XMLParser.END_TAG)
				{
					currentNode = currentNode.getParent();
				}

				else if (newType == XMLParser.TEXT)
				{
					String text = parser.getText();
					currentNode.setText(text);
				}
			}
		} catch (Exception exception)
		{
			throw new RuntimeException("parse error:" + exception);
		}
		if (root.getChildCount() == 1)
		{
			return root.getChild(0);
		} else
		{
			return root;
		}
	}

	// public static void main(String[] args) {
	// try {
	// String is = Util.getResourceStr("/test.dat");
	// System.out.println(is);
	// Node iq = parseTree(new ByteArrayInputStream(is.getBytes()));
	// System.out.println(iq.toString());
	// } catch (Exception e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }

}
