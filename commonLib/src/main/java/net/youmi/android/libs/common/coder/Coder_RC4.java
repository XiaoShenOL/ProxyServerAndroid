package net.youmi.android.libs.common.coder;

import net.youmi.android.libs.common.basic.Basic_StringUtil;
import net.youmi.android.libs.common.debug.Debug_SDK;
import net.youmi.android.libs.common.global.Global_Charsets;

import java.io.UnsupportedEncodingException;

/**
 * RC4加解密
 * 
 * @author zhitaocai
 * @since 2014-5-20
 */
public class Coder_RC4 {

	public static String RC4(String input, String key) {
		if (Basic_StringUtil.isNullOrEmpty(input) || Basic_StringUtil.isNullOrEmpty(key)) {
			return null;
		}
		try {

			String tempInput = input;
			try {
				input = Global_Charsets.Change.toUTF_8(tempInput);
			} catch (UnsupportedEncodingException e) {
				input = tempInput;
			}
			String tempKey = key;
			try {
				key = Global_Charsets.Change.toUTF_8(tempKey);
			} catch (UnsupportedEncodingException e) {
				key = tempKey;
			}

			int[] iS = new int[256];
			byte[] iK = new byte[256];

			for (int i = 0; i < 256; i++)
				iS[i] = i;

			int j = 1;

			for (short i = 0; i < 256; i++) {
				iK[i] = (byte) key.charAt((i % key.length()));
			}

			j = 0;

			for (int i = 0; i < 255; i++) {
				j = (j + iS[i] + iK[i]) % 256;
				int temp = iS[i];
				iS[i] = iS[j];
				iS[j] = temp;
			}

			int i = 0;
			j = 0;
			char[] iInputChar = input.toCharArray();
			char[] iOutputChar = new char[iInputChar.length];
			for (short x = 0; x < iInputChar.length; x++) {
				i = (i + 1) % 256;
				j = (j + iS[i]) % 256;
				int temp = iS[i];
				iS[i] = iS[j];
				iS[j] = temp;
				int t = (iS[i] + (iS[j] % 256)) % 256;
				int iY = iS[t];
				char iCY = (char) iY;
				iOutputChar[x] = (char) (iInputChar[x] ^ iCY);
			}

			String result = new String(iOutputChar);
			try {
				return Global_Charsets.Change.toUTF_8(result);
			} catch (UnsupportedEncodingException e) {
				if (Debug_SDK.isCoderLog) {
					Debug_SDK.te(Debug_SDK.mCoderTag, Coder_RC4.class, e);
				}
			}
			return result;

		} catch (Throwable e) {
			if (Debug_SDK.isCoderLog) {
				Debug_SDK.te(Debug_SDK.mCoderTag, Coder_RC4.class, e);
			}
		}

		return null;

	}

}
