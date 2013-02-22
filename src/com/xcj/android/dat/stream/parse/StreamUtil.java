package com.xcj.android.dat.stream.parse;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;


/**
 * 流的读取与写入
 * 
 * @author chunjiang.shieh
 * 
 */
public class StreamUtil {
	
	/************************write******************************************/

	/**
	 * byte 占一个字节
	 * 
	 * @param dos
	 * @param id
	 */
	public static void addField(DataOutputStream dos, byte id) {
		try {
			dos.writeByte(id);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * short 占两个字节
	 * 
	 * @param dos
	 * @param id
	 */
	public static void addField(DataOutputStream dos, short id) {
		try {
			dos.writeShort(id);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * int 占四个字节
	 * 
	 * @param dos
	 * @param id
	 */
	public static void addField(DataOutputStream dos, int id) {
		try {
			dos.writeInt(id);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 1+n
	 * 
	 * @param dos
	 * @param by
	 */
	public static void addFieldByte(DataOutputStream dos, byte[] by) {
		try {
			dos.writeByte(by.length);
			dos.write(by);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 2+n
	 * 
	 * @param dos
	 * @param by
	 */
	public static void addFieldShort(DataOutputStream dos, byte[] by) {
		try {
			dos.writeShort((short) by.length);
			dos.write(by);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 4+n
	 * 
	 * @param dos
	 * @param by
	 */
	public static void addFieldInt(DataOutputStream dos, byte[] by) {
		try {
			dos.writeInt(by.length);
			dos.write(by);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/****************************read******************************************/

	/**
	 * 1+n
	 * 
	 * @param dis
	 * @return
	 * @throws Exception
	 */
	public static String readComposString1(DataInputStream dis)
			throws Exception {
		int len = dis.read();
		return readString(dis, len);

	}

	/**
	 * 2+n
	 * 
	 * @param dis
	 * @return
	 * @throws Exception
	 */
	public static String readComposString2(DataInputStream dis)
			throws Exception {
		int len = dis.readShort();
		return readString(dis, len);
	}

	/**
	 * 4+n
	 * 
	 * @param dis
	 * @return
	 * @throws Exception
	 */
	public static String readComposString3(DataInputStream dis)
			throws Exception {
		int len = dis.readInt();
		return readString(dis, len);
	}
	
	
	
	private static String readString(InputStream inputStream, int len)
			throws Exception {
		if (len > 0) {
			byte[] b = new byte[len];
			inputStream.read(b, 0, len);
			try {
				return new String(b, "UTF-8");
			} catch (Exception e) {
				return new String(b);
			}
		}
		return null;
	}

}
