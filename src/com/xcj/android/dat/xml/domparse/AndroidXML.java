package com.xcj.android.dat.xml.domparse;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import android.util.Log;

public class AndroidXML {

	@SuppressWarnings("unused")
	private static final String TAG = "AndroidXML";
	Document m_doc = null;

	private AndroidXML(Document document) {
		m_doc = document;
	}

	public static AndroidXML parse(String node) {
		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
			Document doc = builder.parse(new InputSource(new StringReader(node)));
			return new AndroidXML(doc);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			Log.d("AndroidXML Parse:", e.toString());
			return null;
		}
	}

	public static AndroidXML parse(byte[] b) {
		try {
			if (b == null || b.length == 0) {
				Log.d("[ERROR]KZXML", "parse data is null");
				return null;
			}
			// 从DOM解析对象工厂中实例一个DOM解析对象
			DocumentBuilder builder = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
			// 将字节数组转化为字节输入流
			ByteArrayInputStream inputStream = new ByteArrayInputStream(b);
			// 创建新的输入源。
			InputSource is = new InputSource(inputStream);
			// setEncoding() 方法来通知DOM解析器使用何种编码
			is.setEncoding("UTF-8");
			// 将输入源交给Dom解析对象进行解析，并将Dom树返回。
			Document doc = builder.parse(is);
			return new AndroidXML(doc);

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			Log.d("[ERROR]AndroidXML Parse(Byte[]):", e.toString());
			return null;
		}
	}

	/**
	 * 字节数组输出流
	 * 
	 * @param baos
	 * @return
	 */
	public static AndroidXML parse(ByteArrayOutputStream baos) {
		if (baos != null) {
			try {
				return parse(baos.toByteArray());
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
				Log.d("[ERROR]AndroidXML parse(DataWriteStream)", e.toString());
				return null;
			}
		} else {
			return null;
		}
	}

	public XMLElement getRootElement() {
		if (m_doc != null) {
			return new XMLElement(m_doc.getDocumentElement());
		} else {
			return null;
		}
	}

	public void dispose() {
		m_doc = null;
	}

}
