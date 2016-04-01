package net.youmi.android.libs.common.dns;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class Message {

	public static final int MESSAGE_MAXLENGTH = 65535;
	public static final int HEADER_LENGTH = 12;

	/** The question (first) section */
	public static final int Section_QUESTION = 0;

	/** The answer (second) section */
	public static final int Section_ANSWER = 1;

	/** The authority (third) section */
	public static final int Section_AUTHORITY = 2;

	/** The additional (fourth) section */
	public static final int Section_ADDITIONAL = 3;

	private int size;

	private Header header;
	private List[] sections;
	private Record question;
	private byte[] addr;

	public Message(String nameString) {
		sections = new List[4];
		header = new Header();
		Name name = Name.fromString(nameString);
		name = resolve(name, name.root);
//		Log.e("name", "name:" + name.toString() + "  len:" + name.length());
		Record question;
		question = new Record();
		question.name = name;
		this.question = question;
		addRecord(question, Section_QUESTION);

	}

	public byte[] getAddr() {
		return addr;
	}

	public Message(byte[] b) throws IOException {
		this(new DNSInput(b));
	}

	private Message(Header header) {
		sections = new List[4];
		this.header = header;
	}

	Message(DNSInput in) throws IOException {
		sections = new List[4];
		header = new Header(in);
		try {
			for (int i = 0; i < 4; i++) {
				int count = header.getCount(i);
				if (count > 0)
					sections[i] = new ArrayList(count);
				for (int j = 0; j < count; j++) {
					int pos = in.current();
					byte[] addr = fromWire(in, i);
					if (addr != null) {
						this.addr = addr;
						break;
					}
				}
				if (addr != null) {
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		size = in.current();
	}

	byte[] fromWire(DNSInput in, int section) throws IOException {
		Name name;
		int type;
		name = new Name(in);
		type = in.readU16();// type
		in.readU16();// dclass
		if (section == Section_QUESTION) {
			return null;
		}

		in.readU32();// ttl
		in.readU16();// length
		byte[] addre = null;
		// 目前无法全部遍历所有情况，只选发现会用到的几种
		switch (type) {
		case 1:
			addre = in.readByteArray(4);
			break;
		case 2:
		case 5:
			new Name(in);
			break;
		default:
			break;
		}

		return addre;
	}

	private Name resolve(Name current, Name suffix) {
		Name tname = null;
		if (suffix == null)
			tname = current;
		else {
			try {
				tname = Name.concatenate(current, suffix);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return tname;
	}

	public void addRecord(Record r, int section) {
		if (sections[section] == null)
			sections[section] = new LinkedList();
		header.incCount(section);
		sections[section].add(r);
	}

	public Header getHeader() {
		return header;
	}

	public Record getQuestion() {
		return question;
	}

	public byte[] toWire() {
		DNSOutput out = new DNSOutput();

		header.toWire(out);
//		Log.e("towrire", "afterheader:" + out.current() + "");
		Compression c = new Compression();
		for (int i = 0; i < 4; i++) {
			if (sections[i] == null)
				continue;
			for (int j = 0; j < sections[i].size(); j++) {
				Record rec = (Record) sections[i].get(j);
				rec.toWire(out, i, c);
			}
		}
		size = out.current();
		return out.toByteArray();
	}

	public class Header implements Cloneable {

		private int id;
		private int flags;
		private int[] counts;
		private Random random = new Random();

		public Header() {
			counts = new int[4];
			flags = 0;
			id = -1;
		}

		public Header(int id) {
			this();
			this.id = id;
		}

		Header(DNSInput in) throws IOException {
			this(in.readU16());
			flags = in.readU16();
			for (int i = 0; i < counts.length; i++)
				counts[i] = in.readU16();
		}

		void toWire(DNSOutput out) {
			out.writeU16(getID());
			out.writeU16(256);
			for (int i = 0; i < counts.length; i++)
				out.writeU16(counts[i]);
		}

		public int getID() {
			if (id >= 0)
				return id;
			synchronized (this) {
				if (id < 0)
					id = random.nextInt(0xffff);
				return id;
			}
		}

		void setCount(int field, int value) {
			if (value < 0 || value > 0xFFFF)
				throw new IllegalArgumentException("DNS section count " + value
						+ " is out of range");
			counts[field] = value;
		}

		public int getCount(int field) {
			return counts[field];
		}

		public Object clone() {
			Header h = new Header();
			h.id = id;
			h.flags = flags;
			System.arraycopy(counts, 0, h.counts, 0, counts.length);
			return h;
		}

		public void setFlag(int bit) {
			// bits are indexed from left to right
			flags |= (1 << (15 - bit));
		}

		void incCount(int field) {
			if (counts[field] == 0xFFFF)
				throw new IllegalStateException("DNS section count cannot "
						+ "be incremented");
			counts[field]++;
		}
	}

	public class Record implements Cloneable, Serializable {
		private static final long serialVersionUID = -7526297526990495469L;
		Name name;
		byte[] addr;

		public Record() {

		}

		public void toWire(DNSOutput out, int section, Compression c) {
			name.toWire(out, c);
			out.writeU16(1);
			out.writeU16(1);
			if (section == Section_QUESTION)
				return;
		}

	}

}
