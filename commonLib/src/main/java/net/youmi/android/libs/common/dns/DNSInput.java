package net.youmi.android.libs.common.dns;

import java.io.IOException;

public class DNSInput {

	private byte[] array;
	private int pos;
	private int end;
	private int saved_pos;
	private int saved_end;

	public DNSInput(byte[] input) {
		array = input;
		pos = 0;
		end = array.length;
		saved_pos = -1;
		saved_end = -1;
	}

	public int current() {
		return pos;
	}

	public int remaining() {
		return end - pos;
	}

	private void require(int n) throws IOException {
		if (n > remaining()) {
			throw new IOException("end of input");
		}
	}

	public void setActive(int len) {
		if (len > array.length - pos) {
			throw new IllegalArgumentException("cannot set active "
					+ "region past end of input");
		}
		end = pos + len;
	}

	public void clearActive() {
		end = array.length;
	}

	public int saveActive() {
		return end;
	}

	public void restoreActive(int pos) {
		if (pos > array.length) {
			throw new IllegalArgumentException("cannot set active "
					+ "region past end of input");
		}
		end = pos;
	}

	public void jump(int index) {
		if (index >= array.length) {
			throw new IllegalArgumentException("cannot jump past "
					+ "end of input");
		}
		pos = index;
		end = array.length;
	}

	public void save() {
		saved_pos = pos;
		saved_end = end;
	}

	public void restore() {
		if (saved_pos < 0) {
			throw new IllegalStateException("no previous state");
		}
		pos = saved_pos;
		end = saved_end;
		saved_pos = -1;
		saved_end = -1;
	}

	public int readU8() throws IOException {
		require(1);
		return (array[pos++] & 0xFF);
	}

	public int readU16() throws IOException {
		require(2);
		int b1 = array[pos++] & 0xFF;
		int b2 = array[pos++] & 0xFF;
		return ((b1 << 8) + b2);
	}

	public long readU32() throws IOException {
		require(4);
		int b1 = array[pos++] & 0xFF;
		int b2 = array[pos++] & 0xFF;
		int b3 = array[pos++] & 0xFF;
		int b4 = array[pos++] & 0xFF;
		return (((long) b1 << 24) + (b2 << 16) + (b3 << 8) + b4);
	}

	public void readByteArray(byte[] b, int off, int len) throws IOException {
		require(len);
		System.arraycopy(array, pos, b, off, len);
		pos += len;
	}

	public byte[] readByteArray(int len) throws IOException {
		require(len);
		byte[] out = new byte[len];
		System.arraycopy(array, pos, out, 0, len);
		pos += len;
		return out;
	}

	public byte[] readByteArray() {
		int len = remaining();
		byte[] out = new byte[len];
		System.arraycopy(array, pos, out, 0, len);
		pos += len;
		return out;
	}

	public byte[] readCountedString() throws IOException {
		require(1);
		int len = array[pos++] & 0xFF;
		return readByteArray(len);
	}

	public byte[] readByteArrayCon(int ipos, int len) throws IOException {
		require(len);
		byte[] out = new byte[len];
		System.arraycopy(array, ipos, out, 0, len);
		return out;
	}

}
