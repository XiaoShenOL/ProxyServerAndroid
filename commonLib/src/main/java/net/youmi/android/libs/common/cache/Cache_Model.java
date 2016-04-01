package net.youmi.android.libs.common.cache;

/**
 * 缓存Model类
 * 
 * @author jen
 */
public class Cache_Model {

	private String mKey;
	private byte[] mData;
	private long mValidTime_ms;

	public Cache_Model(String key, byte[] data, long validTime_ms) {
		mKey = key;
		mData = data;
		mValidTime_ms = validTime_ms;
	}

	public byte[] getData() {
		return mData;
	}

	public String getKey() {
		return mKey;
	}

	public long getValidTime_ms() {
		return mValidTime_ms;
	}

}
