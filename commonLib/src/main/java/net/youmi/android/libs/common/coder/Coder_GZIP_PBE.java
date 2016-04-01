package net.youmi.android.libs.common.coder;

import net.youmi.android.libs.common.basic.Basic_StringUtil;
import net.youmi.android.libs.common.debug.Debug_SDK;
import net.youmi.android.libs.common.global.Global_Charsets;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * 加密：先使用gzip压缩加密内容，然后使用PBE加密,最后的将盐放前面，密文放后面
 * <p>
 * 解密：加密反之
 * 
 * @author zhitaocai
 * 
 */
public class Coder_GZIP_PBE {

	/**
	 * 先使用gzip压缩加密内容，然后使用PBE加密
	 * <p>
	 * 密文格式： 8位盐+GZIP压缩后经过PBE加密的内容
	 * 
	 * @param valueToEncrypt
	 * @param psw
	 * @return
	 */
	public static byte[] encryptFromString(String valueToEncrypt, String psw) {
		try {
			valueToEncrypt = Basic_StringUtil.getNotEmptyStringElseReturnNull(valueToEncrypt);
			if (valueToEncrypt == null) {
				return null;
			}
			return encrypt(valueToEncrypt.getBytes(Global_Charsets.UTF_8), psw);
		} catch (Throwable e) {
			if (Debug_SDK.isCoderLog) {
				Debug_SDK.te(Debug_SDK.mCoderTag, Coder_GZIP_PBE.class, e);
			}
		}
		return null;

	}

	/**
	 * 先使用gzip压缩加密内容，然后使用PBE加密
	 * <p>
	 * 密文格式： 8位盐+GZIP压缩后经过PBE加密的内容
	 * 
	 * @param buffToEncrypt
	 * @param psw
	 * @return
	 */
	public static byte[] encrypt(byte[] buffToEncrypt, String psw) {

		if (buffToEncrypt == null) {
			return null;
		}

		if (buffToEncrypt.length == 0) {
			return null;
		}

		long begin = System.currentTimeMillis();
		long nt_begin_gzip = 0;
		long nt_begin_PBE = 0;
		long nt_end_gzip = 0;
		long nt_end_PBE = 0;
		int len_test_after_gzip = 0;
		int len_test_after_PBE = 0;
		int len_src = buffToEncrypt.length;// Test

		GZIPOutputStream gzipOs = null;
		ByteArrayOutputStream srcAfterGzip = null;
		ByteArrayOutputStream result = null;

		try {
			// 先压缩，再加密

			nt_begin_gzip = System.currentTimeMillis();

			srcAfterGzip = new ByteArrayOutputStream();

			gzipOs = new GZIPOutputStream(srcAfterGzip);

			gzipOs.write(buffToEncrypt);
			gzipOs.flush();
			closeGZIPOutputStream(gzipOs); // 这句话不能在后面finally写，不然是在解密的时候会出错的EOFException，暂时不解

			len_test_after_gzip = srcAfterGzip.size();

			nt_end_gzip = System.currentTimeMillis();// Test

			nt_begin_PBE = nt_end_gzip;// Test

			// 获取盐
			byte[] salt = Coder_PBE.initSalt();// 长度为8的byte数组

			byte[] buffer = srcAfterGzip.toByteArray();// 原始待加密数据(经过gzip压缩)

			byte[] encodedBuffer = Coder_PBE.encrypt(buffer, psw, salt);// 使用PBE进行加密

			result = new ByteArrayOutputStream();
			result.write(salt);// 写入8位byte的盐
			result.flush();// 强制圧入
			result.write(encodedBuffer);// 写入加密后的流
			result.flush();
			nt_end_PBE = System.currentTimeMillis();// Test

			len_test_after_PBE = result.size();// Test

			return result.toByteArray();

		} catch (Throwable e) {
			if (Debug_SDK.isCoderLog) {
				Debug_SDK.te(Debug_SDK.mCoderTag, Coder_GZIP_PBE.class, e);
			}
		} finally {
			if (Debug_SDK.isCoderLog) {
				long end = System.currentTimeMillis();
				long t_all = end - begin;
				long t_pbe = nt_end_PBE - nt_begin_PBE;
				long t_gzip = nt_end_gzip - nt_begin_gzip;

				Debug_SDK.td(Debug_SDK.mCoderTag, Coder_GZIP_PBE.class,
						"GZIP加密,源长度[%d字节],总花费时间:[%d毫秒],GZIP压缩:[%d毫秒],GZIP结果:[%d字节],PBE加密:[%d毫秒],PBE结果:[%d字节]", len_src,
						t_all, t_gzip, len_test_after_gzip, t_pbe, len_test_after_PBE);
			}
			closeByteArrayOutputStream(result);
			closeByteArrayOutputStream(srcAfterGzip);
			// closeGZIPOutputStream(gzipOs);
		}
		return null;
	}

	/**
	 * 先读取8位盐，然后使用PBE解密后面的内容最后使用GZIP解压缩
	 * <p>
	 * 密文格式： 8位盐+GZIP压缩后经过PBE加密的内容
	 * 
	 * @param buffToDecrypt
	 * @param psw
	 * @return
	 */
	public static String decryptToString(byte[] buffToDecrypt, String psw) {
		try {
			byte[] buff = decrypt(buffToDecrypt, psw);
			if (buff != null) {
				return new String(buff, Global_Charsets.UTF_8);
			}
		} catch (Throwable e) {
			if (Debug_SDK.isCoderLog) {
				Debug_SDK.te(Debug_SDK.mCoderTag, Coder_GZIP_PBE.class, e);
			}
		}
		return null;

	}

	/**
	 * 先读取8位盐，然后使用PBE解密后面的内容最后使用GZIP解压缩
	 * <p>
	 * 密文格式： 8位盐+GZIP压缩后经过PBE加密的内容
	 * 
	 * @param buffToDecrypt
	 * @param psw
	 * @return
	 */
	public static byte[] decrypt(byte[] buffToDecrypt, String psw) {
		if (buffToDecrypt == null) {
			return null;
		}

		if (buffToDecrypt.length == 0) {
			return null;
		}

		long begin = System.currentTimeMillis();
		long nt_begin_gzip = 0;
		long nt_begin_PBE = 0;
		long nt_end_gzip = 0;
		long nt_end_PBE = 0;
		int len_test_after_gzip = 0;
		int len_test_after_PBE = 0;
		int len_src = buffToDecrypt.length;

		ByteArrayInputStream src = null;
		ByteArrayInputStream srcAfterPBEDecode = null;
		GZIPInputStream resultAfterGzip = null;
		ByteArrayOutputStream result = null;

		try {

			src = new ByteArrayInputStream(buffToDecrypt);

			byte[] salt = new byte[8];
			int buffLen = buffToDecrypt.length - 8;
			byte[] buffer = new byte[buffLen];

			src.read(salt, 0, 8);
			src.read(buffer);
			nt_begin_PBE = System.currentTimeMillis();// Test

			byte[] togzipBuffer = Coder_PBE.decrypt(buffer, psw, salt);

			len_test_after_PBE = togzipBuffer.length;

			nt_end_PBE = System.currentTimeMillis();// Test

			nt_begin_gzip = nt_end_PBE;// Test
			srcAfterPBEDecode = new ByteArrayInputStream(togzipBuffer);
			resultAfterGzip = new GZIPInputStream(srcAfterPBEDecode);
			result = new ByteArrayOutputStream();

			int len = 0;
			byte[] buff = new byte[1024];
			while ((len = resultAfterGzip.read(buff)) > 0) {
				result.write(buff, 0, len);
			}
			result.flush();
			len_test_after_gzip = result.size(); // Test
			nt_end_gzip = System.currentTimeMillis();// Test

			return result.toByteArray();

		} catch (Throwable e) {
			if (Debug_SDK.isCoderLog) {
				Debug_SDK.te(Debug_SDK.mCoderTag, Coder_GZIP_PBE.class, e);
			}
		} finally {

			if (Debug_SDK.isCoderLog) {

				long end = System.currentTimeMillis();
				long t_all = end - begin;
				long t_pbe = nt_end_PBE - nt_begin_PBE;
				long t_gzip = nt_end_gzip - nt_begin_gzip;
				Debug_SDK.td(Debug_SDK.mCoderTag, Coder_GZIP_PBE.class,
						"GZIP解密,源长度:[%d字节],总花费时间:[%d毫秒],PBE解密:[%d毫秒],PBE结果:[%d字节],Gzip解压:[%d毫秒],gzip结果:[%d字节]",
						len_src, t_all, t_pbe, len_test_after_PBE, t_gzip, len_test_after_gzip);
			}
			closeByteArrayOutputStream(result);
			closeGZIPInputStream(resultAfterGzip);
			closeByteArrayInputStream(srcAfterPBEDecode);
			closeByteArrayInputStream(src);
		}
		return null;
	}

	private static void closeByteArrayInputStream(ByteArrayInputStream bais) {
		try {
			if (bais != null) {
				bais.close();
			}
		} catch (Throwable e) {
			if (Debug_SDK.isCoderLog) {
				Debug_SDK.te(Debug_SDK.mCoderTag, Coder_GZIP_PBE.class, e);
			}
		} finally {
			bais = null;
		}
	}

	private static void closeByteArrayOutputStream(ByteArrayOutputStream baos) {
		try {
			if (baos != null) {
				baos.close();
			}
		} catch (Throwable e) {
			if (Debug_SDK.isCoderLog) {
				Debug_SDK.te(Debug_SDK.mCoderTag, Coder_GZIP_PBE.class, e);
			}
		} finally {
			baos = null;
		}
	}

	private static void closeGZIPInputStream(GZIPInputStream gzipIs) {
		try {
			if (gzipIs != null) {
				gzipIs.close();
			}
		} catch (Throwable e) {
			if (Debug_SDK.isCoderLog) {
				Debug_SDK.te(Debug_SDK.mCoderTag, Coder_GZIP_PBE.class, e);
			}
		} finally {
			gzipIs = null;
		}
	}

	private static void closeGZIPOutputStream(GZIPOutputStream gzipOs) {
		try {
			if (gzipOs != null) {
				gzipOs.close();
			}
		} catch (Throwable e) {
		} finally {
			gzipOs = null;
		}
	}
}
