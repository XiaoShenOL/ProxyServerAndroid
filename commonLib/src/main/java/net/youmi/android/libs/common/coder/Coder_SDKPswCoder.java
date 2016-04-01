package net.youmi.android.libs.common.coder;

import net.youmi.android.libs.common.basic.Basic_Converter;
import net.youmi.android.libs.common.global.Global_Charsets;

import java.io.ByteArrayOutputStream;

public class Coder_SDKPswCoder {

	/**
	 * 加密
	 *
	 * @param toEncode $str 原文
	 * @param key $key 密钥
	 * @return string
	 */
	public static String encode(String toEncode, String key) {

		if (toEncode == null) {
			return "";
		}

		try {

			toEncode = toEncode.trim();
			if (toEncode.length() == 0) {
				return "";
			}
		} catch (Throwable e) {
			// handle exception
		}

		if (key == null) {
			return "";
		}

		if (key.length() == 0) {
			return "";
		}

		StringBuilder sb = new StringBuilder();

		try {
			key = Coder_Md5.md5(key);
			key = Coder_Md5.md5(key.substring(12))
					+ Coder_Md5.md5(key.substring(0, 20));
			int index = 0;

			byte[] key_buffer = key.getBytes(Global_Charsets.UTF_8);
			byte[] buffer = toEncode.getBytes(Global_Charsets.UTF_8);
			int key_len = key_buffer.length;
			int len = buffer.length;

			for (int i = 0; i < len; i++) {
				int a = buffer[i];
				int b = key_buffer[index];

				int c = ((a ^ b) & 0xff);

				String temp = Integer.toHexString(c);

				if (temp == null) {
					sb.append("00");
				} else {

					temp = temp.trim();

					if (temp.length() == 1) {
						sb.append("0");
						sb.append(temp);
					} else {
						if (temp.length() == 0) {
							sb.append("00");
						} else {
							sb.append(temp);
						}
					}
				}

				index = (++index) % key_len;
			}

		} catch (Throwable e) {
			// handle Throwable
		}

		return sb.toString();
	}

	public static String decode(String toDecode, String key) {

		if (toDecode == null) {
			return null;
		}

		toDecode = toDecode.trim();
		if (toDecode.length() == 0) {
			return null;
		}

		if (key == null) {
			return null;
		}

		if (key.length() == 0) {
			return null;
		}

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		try {

			key = Coder_Md5.md5(key);
			key = Coder_Md5.md5(key.substring(12))
					+ Coder_Md5.md5(key.substring(0, 20));

			int index = 0;

			byte[] key_buffer = key.getBytes(Global_Charsets.UTF_8);

			int key_len = key_buffer.length;
			int len = toDecode.length();

			for (int i = 0; i < len; i += 2) {

				char c0 = toDecode.charAt(i);
				char c1 = toDecode.charAt(i + 1);

				byte b0 = Basic_Converter.hexCharToByte(c0);
				byte b1 = Basic_Converter.hexCharToByte(c1);

				byte b = (byte) ((b0 << 4) | b1);
				b = (byte) (b ^ key_buffer[index]);

				baos.write(b);

				index = (++index) % key_len;
			}

			return new String(baos.toByteArray(), Global_Charsets.UTF_8);

		} catch (Throwable e) {
		}
		return null;
	}

}
