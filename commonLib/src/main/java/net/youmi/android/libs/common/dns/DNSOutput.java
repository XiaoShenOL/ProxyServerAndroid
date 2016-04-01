package net.youmi.android.libs.common.dns;

public class DNSOutput {

	private byte[] array;
	private int pos;
	private int saved_pos;

	public DNSOutput(int size) {
		array = new byte[size];
		pos = 0;
		saved_pos = -1;
	}

	public DNSOutput() {
		this(32);
	}

	public int current() {
		return pos;
	}

	private void check(long val, int bits) {
		long max = 1;
		max <<= bits;
		if (val < 0 || val > max) {
			throw new IllegalArgumentException(val + " out of range for "
					+ bits + " bit value");
		}
	}

	private void need(int n) {
		if (array.length - pos >= n) {
			return;
		}
		int newsize = array.length * 2;
		if (newsize < pos + n) {
			newsize = pos + n;
		}
		byte[] newarray = new byte[newsize];
		System.arraycopy(array, 0, newarray, 0, pos);
		array = newarray;
	}

	public void jump(int index) {
		if (index > pos) {
			throw new IllegalArgumentException("cannot jump past "
					+ "end of data");
		}
		pos = index;
	}

	public void save() {
		saved_pos = pos;
	}

	public void restore() {
		if (saved_pos < 0) {
			throw new IllegalStateException("no previous state");
		}
		pos = saved_pos;
		saved_pos = -1;
	}

	public void writeU8(int val) {
		check(val, 8);
		need(1);
		array[pos++] = (byte) (val & 0xFF);
	}

	public void writeU16(int val) {
		check(val, 16);
		need(2);
		array[pos++] = (byte) ((val >>> 8) & 0xFF);
		array[pos++] = (byte) (val & 0xFF);
	}

	public void writeU16At(int val, int where) {
		check(val, 16);
		if (where > pos - 2)
			throw new IllegalArgumentException("cannot write past "
					+ "end of data");
		array[where++] = (byte) ((val >>> 8) & 0xFF);
		array[where++] = (byte) (val & 0xFF);
	}

	public void writeU32(long val) {
		check(val, 32);
		need(4);
		array[pos++] = (byte) ((val >>> 24) & 0xFF);
		array[pos++] = (byte) ((val >>> 16) & 0xFF);
		array[pos++] = (byte) ((val >>> 8) & 0xFF);
		array[pos++] = (byte) (val & 0xFF);
	}

	public void writeByteArray(byte[] b, int off, int len) {
		need(len);
		System.arraycopy(b, off, array, pos, len);
		pos += len;
	}

	public void writeByteArray(byte[] b) {
		writeByteArray(b, 0, b.length);
	}

	public void writeCountedString(byte[] s) {
		if (s.length > 0xFF) {
			throw new IllegalArgumentException("Invalid counted string");
		}
		need(1 + s.length);
		array[pos++] = (byte) (s.length & 0xFF);
		writeByteArray(s, 0, s.length);
	}

	public byte[] toByteArray() {
		byte[] out = new byte[pos];
		System.arraycopy(array, 0, out, 0, pos);
		return out;
	}

}
