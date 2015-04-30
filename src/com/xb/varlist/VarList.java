package com.xb.varlist;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Vector;

public class VarList {
	public static final byte INVAILD = 0;
	public static final byte BYTE = 1;
	public static final byte BOOL = 2;
	public static final byte SHORT = 3;
	public static final byte CHAR = 4;
	public static final byte INT = 5;
	public static final byte LONG = 6;
	public static final byte FLOAT = 7;
	public static final byte DOUBLE = 8;
	public static final byte STRING = 9;
	public static final byte VARLIST = 10;
	public static final byte BYTES = 11;

	private class Pos {
		private int begin;
		private int length;
		private byte dataType;

		private Pos() {
		}

		public Pos(int begin, int length) {
			this.begin = begin;
			this.length = length;
		}
		
		public int getBegin() {
			return begin;
		}

		public void setBegin(int begin) {
			this.begin = begin;
		}

		public int getLength() {
			return length;
		}

		public void setLength(int length) {
			this.length = length;
		}

		public byte getDataType() {
			return dataType;
		}

		public void setDataType(byte dataType) {
			this.dataType = dataType;
		}
	}

	private Vector<Pos> posList = new Vector<Pos>();
	private byte[] buffer;

	public VarList() {
		buffer = new byte[0x100];
	}

	public VarList(byte[] bytes) {
		int len = bytes.length;
		int selfSize = genCapacity(len / 2);
		buffer = new byte[selfSize];

		int index = 0;
		int end = 0;
		while (index < len) {
			byte type = bytes[index++];
			end = index + 4;
			if (end > bytes.length) {
				break;
			}
			int length = getInt(Arrays.copyOfRange(bytes, index, end));
			index += 4;
			end = index + length;
			if (end > bytes.length) {
				break;
			}
			byte[] data = Arrays.copyOfRange(bytes, index, end);
			index += length;
			switch (type) {
			case SHORT:
				put(getShort(data));
				break;
			case CHAR:
				put(getChar(data));
				break;
			case INT:
				put(getInt(data));
				break;
			case LONG:
				put(getLong(data));
				break;
			case FLOAT:
				put(getFloat(data));
				break;
			case DOUBLE:
				put(getDouble(data));
				break;
			case STRING:
				put(getString(data));
				break;
			case VARLIST:
				VarList b = new VarList(data);
				put(b);
				break;
			case BOOL:
				put(data[0] == 1);
				break;
			case BYTE:
				put(data[0]);
				break;
			case BYTES:
				put(data);
				break;
			default:
				break;
			}
		}

	}

	public byte[] toByteArray() {
		int size = getPosBegin() + 5 * posList.size();
		byte[] rst = new byte[size];
		int index = 0;

		for (Pos pos : posList) {
			rst[index++] = pos.getDataType();
			byte[] length = getBytes(pos.getLength());
			System.arraycopy(length, 0, rst, index, 4);
			index += 4;
			System.arraycopy(buffer, pos.getBegin(), rst, index,
					pos.getLength());
			index += pos.getLength();
		}
		return rst;
	}

	public byte getType(int index) {
		if (index >= posList.size()) {
			return INVAILD;
		}

		return posList.elementAt(index).getDataType();
	}

	public int size() {
		return posList.size();
	}

	public boolean isEmpty() {
		return posList.isEmpty();
	}
	
	public VarList concat(VarList other) {
		for (int i = 0; i < other.size(); ++i) {
			switch (other.getType(i)) {
			case SHORT:
				put(other.shortValue(i));
				break;
			case CHAR:
				put(other.charValue(i));
				break;
			case INT:
				put(other.intValue(i));
				break;
			case LONG:
				put(other.longValue(i));
				break;
			case FLOAT:
				put(other.floatValue(i));
				break;
			case DOUBLE:
				put(other.doubleValue(i));
				break;
			case STRING:
				put(other.stringValue(i));
				break;
			case VARLIST:
				put(other.varlistValue(i));
				break;
			case BOOL:
				put(other.boolValue(i));
				break;
			case BYTE:
				put(other.byteValue(i));
				break;
			case BYTES:
				put(other.byteArrayValue(i));
				break;
			default:
				break;
			}
		}

		return this;
	}
	
	public void clear() {
		posList.clear();
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		boolean first = true;
		for (int i = 0; i < size(); ++i) {
			if (!first) {
				sb.append(", ");
			}
			first = false;
			switch (getType(i)) {
			case SHORT:
				sb.append("short: ");
				sb.append(shortValue(i));
				break;
			case CHAR:
				sb.append("char: ");
				sb.append(charValue(i));
				break;
			case INT:
				sb.append("int: ");
				sb.append(intValue(i));
				break;
			case LONG:
				sb.append("long: ");
				sb.append(longValue(i));
				break;
			case FLOAT:
				sb.append("float: ");
				sb.append(floatValue(i));
				break;
			case DOUBLE:
				sb.append("double: ");
				sb.append(doubleValue(i));
				break;
			case STRING:
				sb.append("string: ");
				sb.append(stringValue(i));
				break;
			case VARLIST:
				sb.append("varlist: ");
				sb.append(varlistValue(i));
				break;
			case BOOL:
				sb.append("bool: ");
				sb.append(boolValue(i));
				break;
			case BYTE:
				sb.append("byte: ");
				sb.append(byteValue(i));
				break;
			case BYTES:
				sb.append("bytes: ");
				sb.append(Arrays.toString(byteArrayValue(i)));
			default:
				break;
			}
		}
		sb.append("]");
		return sb.toString();
	}

	public VarList put(byte v) {
		int begin = getPosBegin();
		int length = 1;
		Pos p = new Pos(begin, length);
		p.setDataType(BYTE);
		posList.add(p);
		ensureCapacity(begin + length);
		buffer[begin] = v;
		return this;
	}

	public VarList put(boolean v) {
		byte[] b = new byte[1];
		b[0] = (byte) (v ? 1 : 0);
	
		int begin = getPosBegin();
		int length = b.length;
		Pos p = new Pos(begin, length);
		p.setDataType(BOOL);
		posList.add(p);
		ensureCapacity(begin + length);
		System.arraycopy(b, 0, buffer, begin, length);
		return this;
	}

	public VarList put(short v) {
		byte[] b = getBytes(v);

		int begin = getPosBegin();
		int length = b.length;
		Pos p = new Pos(begin, length);
		p.setDataType(SHORT);
		posList.add(p);
		ensureCapacity(begin + length);
		System.arraycopy(b, 0, buffer, begin, length);
		return this;
	}

	public VarList put(char v) {
		byte[] b = getBytes(v);

		int begin = getPosBegin();
		int length = b.length;
		Pos p = new Pos(begin, length);
		p.setDataType(CHAR);
		posList.add(p);
		ensureCapacity(begin + length);
		System.arraycopy(b, 0, buffer, begin, length);
		return this;
	}

	public VarList put(int v) {
		byte[] b = getBytes(v);

		int begin = getPosBegin();
		int length = b.length;
		Pos p = new Pos(begin, length);
		p.setDataType(INT);
		posList.add(p);
		ensureCapacity(begin + length);
		System.arraycopy(b, 0, buffer, begin, length);
		return this;
	}

	public VarList put(long v) {
		byte[] b = getBytes(v);

		int begin = getPosBegin();
		int length = b.length;
		Pos p = new Pos(begin, length);
		p.setDataType(LONG);
		posList.add(p);
		ensureCapacity(begin + length);
		System.arraycopy(b, 0, buffer, begin, length);
		return this;
	}

	public VarList put(float v) {
		byte[] b = getBytes(v);

		int begin = getPosBegin();
		int length = b.length;
		Pos p = new Pos(begin, length);
		p.setDataType(FLOAT);
		posList.add(p);
		ensureCapacity(begin + length);
		System.arraycopy(b, 0, buffer, begin, length);
		return this;
	}

	public VarList put(double v) {
		byte[] b = getBytes(v);

		int begin = getPosBegin();
		int length = b.length;
		Pos p = new Pos(begin, length);
		p.setDataType(DOUBLE);
		posList.add(p);
		ensureCapacity(begin + length);
		System.arraycopy(b, 0, buffer, begin, length);
		return this;
	}

	public VarList put(String v) {
		byte[] b = getBytes(v);

		int begin = getPosBegin();
		int length = b.length;
		Pos p = new Pos(begin, length);
		p.setDataType(STRING);
		posList.add(p);
		ensureCapacity(begin + length);
		System.arraycopy(b, 0, buffer, begin, length);
		return this;
	}

	public VarList put(VarList v) {
		byte[] b = v.toByteArray();

		int begin = getPosBegin();
		int length = b.length;
		Pos p = new Pos(begin, length);
		p.setDataType(VARLIST);
		posList.add(p);
		ensureCapacity(begin + length);
		System.arraycopy(b, 0, buffer, begin, length);
		return this;
	}

	public VarList put(byte[] v) {
		int begin = getPosBegin();
		int length = v.length;
		Pos p = new Pos(begin, length);
		p.setDataType(BYTES);
		posList.add(p);
		ensureCapacity(begin + length);
		for (byte b : v) {
			buffer[begin++] = b;
		}
		return this;
	}

	public byte byteValue(int index) {
		if (BYTE != getType(index)) {
			return 0;
		}
	
		Pos p = posList.elementAt(index);
		return buffer[p.getBegin()];
	}

	public boolean boolValue(int index) {
		if (BOOL != getType(index)) {
			return false;
		}
	
		Pos p = posList.elementAt(index);
		byte[] data = Arrays.copyOfRange(buffer, p.getBegin(),
				p.getBegin() + p.getLength());
		return data[0] == 1;
	}

	public short shortValue(int index) {
		if (SHORT != getType(index)) {
			return 0;
		}

		Pos p = posList.elementAt(index);
		byte[] data = Arrays.copyOfRange(buffer, p.getBegin(),
				p.getBegin() + p.getLength());
		return getShort(data);
	}

	public char charValue(int index) {
		if (CHAR != getType(index)) {
			return 0;
		}

		Pos p = posList.elementAt(index);
		byte[] data = Arrays.copyOfRange(buffer, p.getBegin(),
				p.getBegin() + p.getLength());
		return getChar(data);
	}

	public int intValue(int index) {
		if (INT != getType(index)) {
			return 0;
		}

		Pos p = posList.elementAt(index);
		byte[] data = Arrays.copyOfRange(buffer, p.getBegin(),
				p.getBegin() + p.getLength());
		return getInt(data);
	}

	public long longValue(int index) {
		if (LONG != getType(index)) {
			return 0;
		}

		Pos p = posList.elementAt(index);
		byte[] data = Arrays.copyOfRange(buffer, p.getBegin(),
				p.getBegin() + p.getLength());
		return getLong(data);
	}

	public float floatValue(int index) {
		if (FLOAT != getType(index)) {
			return 0f;
		}

		Pos p = posList.elementAt(index);
		byte[] data = Arrays.copyOfRange(buffer, p.getBegin(),
				p.getBegin() + p.getLength());
		return getFloat(data);
	}

	public double doubleValue(int index) {
		if (DOUBLE != getType(index)) {
			return 0;
		}

		Pos p = posList.elementAt(index);
		byte[] data = Arrays.copyOfRange(buffer, p.getBegin(),
				p.getBegin() + p.getLength());
		return getDouble(data);
	}

	public String stringValue(int index) {
		if (STRING != getType(index)) {
			return "";
		}

		Pos p = posList.elementAt(index);
		byte[] data = Arrays.copyOfRange(buffer, p.getBegin(),
				p.getBegin() + p.getLength());
		return getString(data);
	}

	public VarList varlistValue(int index) {
		if (VARLIST != getType(index)) {
			return new VarList();
		}

		Pos p = posList.elementAt(index);
		byte[] data = Arrays.copyOfRange(buffer, p.getBegin(),
				p.getBegin() + p.getLength());
		VarList b = new VarList(data);
		return b;
	}

	public byte[] byteArrayValue(int index) {
		if (BYTES != getType(index)) {
			return new byte[0];
		}

		Pos p = posList.elementAt(index);
		byte[] data = Arrays.copyOfRange(buffer, p.getBegin(),
				p.getBegin() + p.getLength());
		return data;
	}

	public static byte[] getBytes(short data) {
		byte[] bytes = new byte[2];
		bytes[0] = (byte) (data & 0xff);
		bytes[1] = (byte) ((data & 0xff00) >> 8);
		return bytes;
	}

	public static byte[] getBytes(char data) {
		byte[] bytes = new byte[2];
		bytes[0] = (byte) (data);
		bytes[1] = (byte) (data >> 8);
		return bytes;
	}

	public static byte[] getBytes(VarList data) {
		return data.toByteArray();
	}

	public static byte[] getBytes(int data) {
		byte[] bytes = new byte[4];
		bytes[0] = (byte) (data & 0xff);
		bytes[1] = (byte) ((data & 0xff00) >> 8);
		bytes[2] = (byte) ((data & 0xff0000) >> 16);
		bytes[3] = (byte) ((data & 0xff000000) >> 24);
		return bytes;
	}

	public static byte[] getBytes(long data) {
		byte[] bytes = new byte[8];
		bytes[0] = (byte) (data & 0xff);
		bytes[1] = (byte) ((data >> 8) & 0xff);
		bytes[2] = (byte) ((data >> 16) & 0xff);
		bytes[3] = (byte) ((data >> 24) & 0xff);
		bytes[4] = (byte) ((data >> 32) & 0xff);
		bytes[5] = (byte) ((data >> 40) & 0xff);
		bytes[6] = (byte) ((data >> 48) & 0xff);
		bytes[7] = (byte) ((data >> 56) & 0xff);
		return bytes;
	}

	public static byte[] getBytes(float data) {
		int intBits = Float.floatToIntBits(data);
		return getBytes(intBits);
	}

	public static byte[] getBytes(double data) {
		long intBits = Double.doubleToLongBits(data);
		return getBytes(intBits);
	}

	public static byte[] getBytes(String data, String charsetName) {
		Charset charset = Charset.forName(charsetName);
		return data.getBytes(charset);
	}

	public static byte[] getBytes(String data) {
		return getBytes(data, "GBK");
	}

	public static short getShort(byte[] bytes) {
		return (short) ((0xff & bytes[0]) | (0xff00 & (bytes[1] << 8)));
	}

	public static char getChar(byte[] bytes) {
		return (char) ((0xff & bytes[0]) | (0xff00 & (bytes[1] << 8)));
	}

	public static int getInt(byte[] bytes) {
		return (0xff & bytes[0]) | (0xff00 & (bytes[1] << 8))
				| (0xff0000 & (bytes[2] << 16))
				| (0xff000000 & (bytes[3] << 24));
	}

	public static long getLong(byte[] bytes) {
		return (0xffL & (long) bytes[0]) | (0xff00L & ((long) bytes[1] << 8))
				| (0xff0000L & ((long) bytes[2] << 16))
				| (0xff000000L & ((long) bytes[3] << 24))
				| (0xff00000000L & ((long) bytes[4] << 32))
				| (0xff0000000000L & ((long) bytes[5] << 40))
				| (0xff000000000000L & ((long) bytes[6] << 48))
				| (0xff00000000000000L & ((long) bytes[7] << 56));
	}

	public static float getFloat(byte[] bytes) {
		return Float.intBitsToFloat(getInt(bytes));
	}

	public static double getDouble(byte[] bytes) {
		long l = getLong(bytes);
		return Double.longBitsToDouble(l);
	}

	public static String getString(byte[] bytes, String charsetName) {
		return new String(bytes, Charset.forName(charsetName));
	}

	public static String getString(byte[] bytes) {
		return getString(bytes, "GBK");
	}

	private void ensureCapacity(int newSize) {
		if (newSize > buffer.length) {
			grow(newSize);
		}
	}

	private int getPosBegin() {
		if (posList.isEmpty()) {
			return 0;
		}

		Pos p = posList.lastElement();
		return p.getBegin() + p.getLength();
	}

	private void grow(int newSize) {
		int capacity = buffer.length << 1;
		capacity = Math.max(capacity, newSize);
		capacity = genCapacity(capacity);
		byte[] newBuffer = new byte[newSize];
		System.arraycopy(buffer, 0, newBuffer, 0, buffer.length);
		buffer = newBuffer;
	}

	private int genCapacity(int num) {
		int mask = 0x40000000;
		while (0 != mask) {
			if (0 != (mask & num)) {
				if (mask != num) {
					mask <<= 1;
				}
				return mask;
			}
			mask >>= 1;
		}

		return 0;
	}
}
