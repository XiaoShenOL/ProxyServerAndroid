package net.youmi.android.libs.common.coder;

import net.youmi.android.libs.common.basic.Basic_Random;
import net.youmi.android.libs.common.debug.Debug_SDK;

import java.io.UnsupportedEncodingException;

public class Coder_CECoder {

	/**
	 * 字符总数0-9a-zA-Z\_\- a-z: 10-35 A-Z: 36-61 _: 62 -: 63
	 */
	final static int MAX = 64;

	private static final String dicStr = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_-";
	/**
	 * 加密之后的字符集
	 */
	private static final char dic[] = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_-".toCharArray();

	/**
	 * CE加密使用
	 */
	private static int primes[] = { 2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31 };

	/**
	 * 大写字符串
	 */
	final static char BIG_HEX_CAHRS[] = "0123456789ABCDEF".toCharArray();

	final static String offersCePlatformKey = "SYxjBwnEu9HMUfEv";// 这个是积分墙v2专用的//
	// 以后考虑对该key进行加密
	// "DRWjzp4vScwqwyrb";//这个是banner及积分墙v1专用的。

	final static String statCePlatformKey = "SPXswpSABmZtdX3V";

	// 积分墙第二版专用：
	//
	// /**
	// * 广告URL加密的平台密钥
	// * @var string
	// */
	// const SDK_SECRET_KEY = 'SYxjBwnEu9HMUfEv';
	//
	// /**
	// * sdk加密使用的参数
	// * @var int
	// */
	// const SDK_PRIME_KEY = 144481;

	/**
	 * 积分墙接口质素参数
	 */
	final static int offersPrimeNumber = 144481;// 这个是积分墙v2专用的。
	// 13859;//这个是积分墙v1及banner专用的。

	/**
	 * 统计接口质素参数
	 */
	final static int statPrimeNumber = 108301;// 这个是积分墙v2专用的。

	// 13859;//这个是积分墙v1及banner专用的。

	/**
	 * 加密字符串
	 * 
	 * @param strBytes
	 *            待加密字符串
	 * @param enkey
	 *            当前参与加密的密钥
	 * @param primeNumber
	 * @return 加密后的字符串
	 */
	public final static String ceEncode(final byte[] strBytes, final String enkey, int primeNumber)
			throws UnsupportedEncodingException {
		// 初始化明文密钥
		StringBuilder buf = new StringBuilder();

		byte kbs[] = enkey.getBytes();
		byte mBytes[] = new byte[kbs.length + strBytes.length];
		System.arraycopy(kbs, 0, mBytes, 0, kbs.length);
		System.arraycopy(strBytes, 0, mBytes, kbs.length, strBytes.length);

		String m = Coder_Md5.md5(mBytes);

		// Test.log("md5: "+m);

		m = m.substring(9, 18);
		int mLens = m.length();
		int end = 0;
		int dec = 0, chr = 0;
		String tmp = null;
		for (int i = 0; i < 9; i += 3) {
			end = i + 3;
			if (end > mLens) {
				end = mLens;
			}
			tmp = m.substring(i, end);
			dec = Integer.parseInt(tmp, 16);
			chr = (dec < 64) ? 0 : dec >>> 6;
			buf.append(dic[chr]);
			buf.append(dic[dec & 63]);
		}

		final String plainKey = buf.toString();

		// Test.log("yas: plainkey: "+plainKey);

		// //////////////////////////////
		// this.init( plainKey, key );
		String key = Coder_Md5.md5(plainKey + enkey);

		// Test.log("yas: key: "+key);

		int x0 = 0;

		for (int i = 0; i < 26; ++i) {
			x0 <<= 1;
			if (key.charAt(i) > '7') {
				x0++;
			}
		}
		if (x0 < 67) {
			x0 = 67;
		}

		int x = ((x0 & 1) == 0) ? (x0 - 1) : (x0 - 2);
		boolean flag = true;
		int tp = 0;
		while (x > 0) {
			flag = true;
			for (int i = 0; i < primes.length; ++i) {
				tp = primes[i];
				if (x % tp == 0) {
					flag = false;
					break;
				}
			}

			if (flag) {
				break;
			}

			x -= 2;
		}
		int a = x;

		// ///////////////////////////////////

		buf.delete(0, buf.length());
		byte keyBytes[] = key.getBytes();
		final int key_lens = keyBytes.length;
		final int str_lens = strBytes.length;
		int k = 0;
		byte data = 0;
		int index1 = 0;
		for (int s = 0; s < str_lens; ++s) {
			data = (byte) (strBytes[s] ^ keyBytes[k++]);
			if (k >= key_lens) {
				k = 0;
			}
			index1 = (data & 0xf0) >>> 4;
			buf.append(BIG_HEX_CAHRS[index1]);
			index1 = data & 0x0f;
			buf.append(BIG_HEX_CAHRS[index1]);
		}

		// /hexToCipher//////////////////////////////

		String str = buf.toString();

		// Test.log("yas" +
		// " before hex2CipherString:"+str);

		buf.delete(0, buf.length());
		final int lens = str.length();
		int index = x0;

		// Test.log("yas x0:"+x0);

		int dec2 = 0;
		byte chr2 = 0;
		int end2 = 0;
		buf.append(dic[(lens % 3)]);

		// Test.log("yas buf.append( this.dic[(lens % 3)] ):"+buf.toString());

		for (int i = 0; i < lens; i += 3) {
			end2 = (i + 3) < lens ? (i + 3) : lens;
			String tmp2 = str.substring(i, end2);
			dec2 = Integer.parseInt(tmp2, 16);
			chr2 = (byte) ((dec2 < MAX) ? 0 : (dec2 >>> 6));

			index = (a * index + primeNumber) & 63;
			buf.append(dic[(chr2 + index) & 63]);

			index = (a * index + primeNumber) & 63;
			buf.append(dic[(dec2 + index) & 63]);
		}

		// Test.log("ce hex2CipherString:"+buf.toString());

		return plainKey + buf.toString();
	}

	/**
	 * 获取随机密钥
	 */
	public final static String getRandomKey(int lens) {
		try {
			StringBuilder buffer = new StringBuilder(lens);
			for (int i = 0; i < lens; ++i) {
				int index = Math.abs(Basic_Random.nextInt(dic.length)) % dic.length;
				buffer.append(dic[index]);
			}
			return buffer.toString();
		} catch (Throwable e) {
			if (Debug_SDK.isCoderLog) {
				Debug_SDK.te(Debug_SDK.mCoderTag, Coder_CECoder.class, e);
			}
		}

		return "abcd";
	}

	/**
	 * 把指定字符串转换为密文<br/>
	 * 注意，该算法与正规的十六进制转64进制有差异，不能外部使用
	 */
	public final static String converHexTo64_GwRule(String str) {
		final int lens = str.length();
		StringBuilder buffer = new StringBuilder((lens << 1) / 3);
		int dec = 0;
		byte chr = 0;
		int end = 0;

		for (int i = 0; i < lens; i += 3) {
			end = (i + 3) < lens ? (i + 3) : lens;
			String tmp = str.substring(i, end);
			dec = Integer.parseInt(tmp, 16);
			chr = (byte) ((dec < MAX) ? 0 : (dec >>> 6));
			buffer.append(dic[chr]);

			buffer.append(dic[dec & 63]);
		}

		return buffer.toString();
	}

	/**
	 * 将六十四进制的字符转换为十进制的数字
	 * 
	 * @param c
	 * @return
	 */
	public final static int conver64ToDecimal(char c) {
		return dicStr.indexOf(c);
	}

	public final static char converDecimalTo64(int i) {
		try {
			if (i < dic.length && i > -1) {
				return dic[i];
			}
		} catch (Throwable e) {
			if (Debug_SDK.isCoderLog) {
				Debug_SDK.te(Debug_SDK.mCoderTag, Coder_CECoder.class, e);
			}
		}

		return '0';
	}
}
