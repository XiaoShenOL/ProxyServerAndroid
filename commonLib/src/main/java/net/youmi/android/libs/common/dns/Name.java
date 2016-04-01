package net.youmi.android.libs.common.dns;

import java.io.IOException;
import java.io.Serializable;
import java.text.DecimalFormat;

public class Name implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5055338040548147647L;

	/* The name data */
	private byte[] name;

	private long offsets;
	/** The root name */
	public static final Name root;

	/** The root name */
	public static final Name empty;

	/** The maximum length of a Name */
	private static final int MAXNAME = 255;

	/** The maximum length of a label a Name */
	private static final int MAXLABEL = 63;

	/** The maximum number of labels in a Name */
	private static final int MAXLABELS = 128;

	/** The maximum number of cached offsets */
	private static final int MAXOFFSETS = 7;

	private static final byte[] emptyLabel = new byte[] { (byte) 0 };
	private static final byte lowercase[] = new byte[256];

	private static final int LABEL_NORMAL = 0;
	private static final int LABEL_COMPRESSION = 0xC0;
	private static final int LABEL_MASK = 0xC0;

	private static final DecimalFormat byteFormat = new DecimalFormat();

	static {
		root = new Name();
		root.appendSafe(emptyLabel, 0, 1);
		empty = new Name();
		empty.name = new byte[0];
	}

	private Name() {
	}

	private final void appendSafe(byte[] array, int start, int n) {
		try {
			append(array, start, n);
		} catch (Exception e) {
		}
	}

	public Name(DNSInput in) throws IOException {
		int len, pos;
		boolean done = false;
		byte[] label = new byte[MAXLABEL + 1];
		boolean savedState = false;
		while (!done) {
			len = in.readU8();
			switch (len & LABEL_MASK) {
			case LABEL_NORMAL:
				if (getlabels() >= MAXLABELS)
					throw new IOException("too many labels");
				if (len == 0) {
					append(emptyLabel, 0, 1);
					done = true;
				} else {
					label[0] = (byte) len;
					in.readByteArray(label, 1, len);
					append(label, 0, 1);
				}
				break;
			case LABEL_COMPRESSION:
				pos = in.readU8();
				pos += ((len & ~LABEL_MASK) << 8);

				if (pos >= in.current() - 2)
					throw new IOException("bad compression");
				if (!savedState) {
					in.save();
					savedState = true;
				}
				in.jump(pos);
				break;
			default:
				throw new IOException("bad label type");
			}
		}
		if (savedState) {
			in.restore();
		}
	}

	public static Name fromString(String s) throws IllegalArgumentException {
		return fromString(s, null);
	}

	public static Name fromString(String s, Name origin)
			throws IllegalArgumentException {
		if (s.equals("@") && origin != null)
			return origin;
		else if (s.equals("."))
			return (root);

		return new Name(s, origin);
	}

	public Name(Name src, int n) {
		int slabels = src.labels();
		if (n > slabels)
			throw new IllegalArgumentException("attempted to remove too "
					+ "many labels");
		name = src.name;
		setlabels(slabels - n);
		for (int i = 0; i < MAXOFFSETS && i < slabels - n; i++)
			setoffset(i, src.offset(i + n));
	}

	public Name(String s, Name origin) throws IllegalArgumentException {
		if (s.equals("")) {
		} else if (s.equals("@")) {
			if (origin == null)
				copy(empty, this);
			else
				copy(origin, this);
			return;
		} else if (s.equals(".")) {
			copy(root, this);
			return;
		}
		int labelstart = -1;
		int pos = 1;
		byte[] label = new byte[MAXLABEL + 1];
		boolean escaped = false;
		int digits = 0;
		int intval = 0;
		boolean absolute = false;
		for (int i = 0; i < s.length(); i++) {
			byte b = (byte) s.charAt(i);
			if (escaped) {
				if (b >= '0' && b <= '9' && digits < 3) {
					digits++;
					intval *= 10;
					intval += (b - '0');
					if (intval > 255)
						throw new IllegalArgumentException("bad escape");
					if (digits < 3)
						continue;
					b = (byte) intval;
				} else if (digits > 0 && digits < 3)
					throw new IllegalArgumentException("bad escape");
				if (pos > MAXLABEL)
					throw new IllegalArgumentException("label too long");
				labelstart = pos;
				label[pos++] = b;
				escaped = false;
			} else if (b == '\\') {
				escaped = true;
				digits = 0;
				intval = 0;
			} else if (b == '.') {
				if (labelstart == -1)
					throw new IllegalArgumentException("invalid empty label");
				label[0] = (byte) (pos - 1);
				appendFromString(s, label, 0, 1);
				labelstart = -1;
				pos = 1;
			} else {
				if (labelstart == -1)
					labelstart = i;
				if (pos > MAXLABEL)
					throw new IllegalArgumentException("label too long");
				label[pos++] = b;
			}
		}
		if (digits > 0 && digits < 3)
			throw new IllegalArgumentException("bad escape");
		if (escaped)
			throw new IllegalArgumentException("bad escape");
		if (labelstart == -1) {
			appendFromString(s, emptyLabel, 0, 1);
			absolute = true;
		} else {
			label[0] = (byte) (pos - 1);
			appendFromString(s, label, 0, 1);
		}
		if (origin != null && !absolute)
			appendFromString(s, origin.name, origin.offset(0),
					origin.getlabels());
	}

	private static final void copy(Name src, Name dst) {
		if (src.offset(0) == 0) {
			dst.name = src.name;
			dst.offsets = src.offsets;
		} else {
			int offset0 = src.offset(0);
			int namelen = src.name.length - offset0;
			int labels = src.labels();
			dst.name = new byte[namelen];
			System.arraycopy(src.name, offset0, dst.name, 0, namelen);
			for (int i = 0; i < labels && i < MAXOFFSETS; i++)
				dst.setoffset(i, src.offset(i) - offset0);
			dst.setlabels(labels);
		}
	}

	private final int offset(int n) {
		if (n == 0 && getlabels() == 0)
			return 0;
		if (n < 0 || n >= getlabels())
			throw new IllegalArgumentException("label out of range");
		if (n < MAXOFFSETS) {
			int shift = 8 * (7 - n);
			return ((int) (offsets >>> shift) & 0xFF);
		} else {
			int pos = offset(MAXOFFSETS - 1);
			for (int i = MAXOFFSETS - 1; i < n; i++)
				pos += (name[pos] + 1);
			return (pos);
		}
	}

	private final int getlabels() {
		return (int) (offsets & 0xFF);
	}

	public byte[] getLabel(int n) {
		int pos = offset(n);
		byte len = (byte) (name[pos] + 1);
		byte[] label = new byte[len];
		System.arraycopy(name, pos, label, 0, len);
		return label;
	}

	private final void setlabels(int labels) {
		offsets &= ~(0xFF);
		offsets |= labels;
	}

	private final void setoffset(int n, int offset) {
		if (n >= MAXOFFSETS)
			return;
		int shift = 8 * (7 - n);
		offsets &= (~(0xFFL << shift));
		offsets |= ((long) offset << shift);
	}

	public int labels() {
		return getlabels();
	}

	private final void appendFromString(String fullName, byte[] array,
			int start, int n) throws IllegalArgumentException {
		try {
			append(array, start, n);
		} catch (Exception e) {
		}
	}

	private final void append(byte[] array, int start, int n)
			throws IllegalArgumentException {
		int length = (name == null ? 0 : (name.length - offset(0)));
		int alength = 0;
		for (int i = 0, pos = start; i < n; i++) {
			int len = array[pos];
			if (len > MAXLABEL)
				throw new IllegalStateException("invalid label");
			len++;
			pos += len;
			alength += len;
		}
		int newlength = length + alength;
		if (newlength > MAXNAME) {
			throw new IllegalArgumentException("name too long");
		}
		int labels = getlabels();
		int newlabels = labels + n;
		if (newlabels > MAXLABELS)
			throw new IllegalStateException("too many labels");
		byte[] newname = new byte[newlength];
		if (length != 0)
			System.arraycopy(name, offset(0), newname, 0, length);
		System.arraycopy(array, start, newname, length, alength);
		name = newname;
		for (int i = 0, pos = length; i < n; i++) {
			setoffset(labels + i, pos);
			pos += (newname[pos] + 1);
		}
		setlabels(newlabels);
	}

	public boolean isAbsolute() {
		int nlabels = labels();
		if (nlabels == 0)
			return false;
		return name[offset(nlabels - 1)] == 0;
	}

	public static Name concatenate(Name prefix, Name suffix) throws Exception {
		if (prefix.isAbsolute())
			return (prefix);
		Name newname = new Name();
		copy(prefix, newname);
		newname.append(suffix.name, suffix.offset(0), suffix.getlabels());
		return newname;
	}

	/**
	 * Is the current Name a subdomain of the specified name?
	 */
	public boolean subdomain(Name domain) {
		int labels = labels();
		int dlabels = domain.labels();
		if (dlabels > labels)
			return false;
		if (dlabels == labels)
			return equals(domain);
		return domain.equals(name, offset(labels - dlabels));
	}

	private final boolean equals(byte[] b, int bpos) {
		int labels = labels();
		for (int i = 0, pos = offset(0); i < labels; i++) {
			if (name[pos] != b[bpos])
				return false;
			int len = name[pos++];
			bpos++;
			if (len > MAXLABEL)
				throw new IllegalStateException("invalid label");
			for (int j = 0; j < len; j++)
				if (lowercase[(name[pos++] & 0xFF)] != lowercase[(b[bpos++] & 0xFF)])
					return false;
		}
		return true;
	}

	public static Name fromConstantString(String s) {
		try {
			return fromString(s, null);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Invalid name '" + s + "'");
		}
	}

	public void toWire(DNSOutput out, Compression c) {

		int labels = labels();
		for (int i = 0; i < labels - 1; i++) {
			Name tname;
			if (i == 0)
				tname = this;
			else
				tname = new Name(this, i);
			int pos = -1;
			if (c != null)
				pos = c.get(tname);
			if (pos >= 0) {
				pos |= (LABEL_MASK << 8);
				out.writeU16(pos);
				return;
			} else {
				if (c != null)
					c.add(out.current(), tname);
				int off = offset(i);
				// Log.e("towrire", "name.length:" + name.length + "");
				out.writeByteArray(name, off, name[off] + 1);
			}
		}
		out.writeU8(0);
	}

	public void toWireCanonical(DNSOutput out) {
		byte[] b = toWireCanonical();
		out.writeByteArray(b);
	}

	public byte[] toWireCanonical() {
		int labels = labels();
		if (labels == 0)
			return (new byte[0]);
		byte[] b = new byte[name.length - offset(0)];
		for (int i = 0, spos = offset(0), dpos = 0; i < labels; i++) {
			int len = name[spos];
			if (len > MAXLABEL)
				throw new IllegalStateException("invalid label");
			b[dpos++] = name[spos++];
			for (int j = 0; j < len; j++)
				b[dpos++] = lowercase[(name[spos++] & 0xFF)];
		}
		return b;
	}

	public String toString() {
		return toString(false);
	}

	public String toString(boolean omitFinalDot) {
		int labels = labels();
		if (labels == 0)
			return "@";
		else if (labels == 1 && name[offset(0)] == 0)
			return ".";
		StringBuffer sb = new StringBuffer();
		for (int i = 0, pos = offset(0); i < labels; i++) {
			int len = name[pos];
			if (len > MAXLABEL)
				throw new IllegalStateException("invalid label");
			if (len == 0) {
				if (!omitFinalDot)
					sb.append('.');
				break;
			}
			if (i > 0)
				sb.append('.');
			sb.append(byteString(name, pos));
			pos += (1 + len);
		}
		return sb.toString();
	}

	private String byteString(byte[] array, int pos) {
		StringBuffer sb = new StringBuffer();
		int len = array[pos++];
		for (int i = pos; i < pos + len; i++) {
			int b = array[i] & 0xFF;
			if (b <= 0x20 || b >= 0x7f) {
				sb.append('\\');
				sb.append(byteFormat.format(b));
			} else if (b == '"' || b == '(' || b == ')' || b == '.' || b == ';'
					|| b == '\\' || b == '@' || b == '$') {
				sb.append('\\');
				sb.append((char) b);
			} else
				sb.append((char) b);
		}
		return sb.toString();
	}

	public short length() {
		if (getlabels() == 0)
			return 0;
		return (short) (name.length - offset(0));
	}

}
