package net.youmi.android.libs.common.coder;

import net.youmi.android.libs.common.debug.Debug_SDK;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;

public class Coder_DES {

	private Key mKey;

	public static Coder_DES createDES(String psw) {

		try {
			return new Coder_DES(psw);
		} catch (Throwable e) {
			if (Debug_SDK.isCoderLog) {
				Debug_SDK.te(Debug_SDK.mCoderTag, Coder_DES.class, e);
			}
		}
		return null;
	}

	private Coder_DES(String psw) throws NoSuchAlgorithmException {
		KeyGenerator _generator = KeyGenerator.getInstance("DES");
		_generator.init(new SecureRandom(psw.getBytes()));
		this.mKey = _generator.generateKey();
		_generator = null;
	}

	/**
	 * 对输入byte[]进行加密，输出byte[]
	 * 
	 * @param toEncrypt
	 * @return
	 */
	public byte[] encrypt(byte[] toEncrypt) {
		ByteArrayInputStream is = null;
		ByteArrayOutputStream out = null;
		try {
			is = new ByteArrayInputStream(toEncrypt);
			out = new ByteArrayOutputStream();

			if (encrypt(is, out)) {
				return out.toByteArray();
			}

		} catch (Throwable e) {
			if (Debug_SDK.isCoderLog) {
				Debug_SDK.te(Debug_SDK.mCoderTag, Coder_DES.class, e);
			}
		} finally {
			try {
				if (out != null) {
					out.close();
				}
			} catch (Throwable e) {
				if (Debug_SDK.isCoderLog) {
					Debug_SDK.te(Debug_SDK.mCoderTag, Coder_DES.class, e);
				}
			}
			try {
				if (is != null) {
					is.close();
				}
			} catch (Throwable e) {
				if (Debug_SDK.isCoderLog) {
					Debug_SDK.te(Debug_SDK.mCoderTag, Coder_DES.class, e);
				}
			}
		}
		return toEncrypt;
	}

	/**
	 * 对输入byte[]进行加密，输出byte[]
	 * 
	 * @param toDecrypt
	 * @return
	 */
	public byte[] decrypt(byte[] toDecrypt) {
		ByteArrayInputStream is = null;
		ByteArrayOutputStream out = null;
		try {
			is = new ByteArrayInputStream(toDecrypt);
			out = new ByteArrayOutputStream();

			if (decrypt(is, out)) {
				return out.toByteArray();
			}

		} catch (Throwable e) {
			if (Debug_SDK.isCoderLog) {
				Debug_SDK.te(Debug_SDK.mCoderTag, Coder_DES.class, e);
			}
		} finally {
			try {
				if (out != null) {
					out.close();
				}
			} catch (Throwable e) {
				if (Debug_SDK.isCoderLog) {
					Debug_SDK.te(Debug_SDK.mCoderTag, Coder_DES.class, e);
				}
			}
			try {
				if (is != null) {
					is.close();
				}
			} catch (Throwable e) {
				if (Debug_SDK.isCoderLog) {
					Debug_SDK.te(Debug_SDK.mCoderTag, Coder_DES.class, e);
				}
			}
		}
		return toDecrypt;
	}

	/**
	 * 对输入流进行加密，并保存在输出流中
	 * 
	 * @param is_toEncrypt
	 *            要加密的文件
	 * @param out_dest
	 *            加密后存放的文件名
	 */
	public boolean encrypt(InputStream is_toEncrypt, OutputStream out_dest) {

		CipherInputStream cis = null;

		try {

			Cipher cipher = Cipher.getInstance("DES");
			cipher.init(Cipher.ENCRYPT_MODE, this.mKey);

			cis = new CipherInputStream(is_toEncrypt, cipher);
			byte[] buffer = new byte[1024];
			int r;
			while ((r = cis.read(buffer)) > 0) {
				out_dest.write(buffer, 0, r);
			}

			return true;
		} catch (Throwable e) {
			if (Debug_SDK.isCoderLog) {
				Debug_SDK.te(Debug_SDK.mCoderTag, Coder_DES.class, e);
			}
		} finally {
			try {
				if (cis != null) {
					cis.close();
				}
			} catch (Throwable e) {
				if (Debug_SDK.isCoderLog) {
					Debug_SDK.te(Debug_SDK.mCoderTag, Coder_DES.class, e);
				}
			}

		}

		return false;
	}

	/**
	 * 对输入流采用DES算法进行解密
	 * 
	 * @param is_toDecrypt
	 *            输入流
	 * @param out_dest
	 *            输出流
	 */
	public boolean decrypt(InputStream is_toDecrypt, OutputStream out_dest) {

		CipherOutputStream cos = null;
		try {

			Cipher cipher = Cipher.getInstance("DES");
			cipher.init(Cipher.DECRYPT_MODE, this.mKey);

			cos = new CipherOutputStream(out_dest, cipher);
			byte[] buffer = new byte[1024];
			int r;
			while ((r = is_toDecrypt.read(buffer)) >= 0) {
				cos.write(buffer, 0, r);
			}

			return true;
		} catch (Throwable e) {
			if (Debug_SDK.isCoderLog) {
				Debug_SDK.te(Debug_SDK.mCoderTag, Coder_DES.class, e);
			}
		} finally {
			try {
				if (cos != null) {
					cos.close();
				}
			} catch (Throwable e) {
				if (Debug_SDK.isCoderLog) {
					Debug_SDK.te(Debug_SDK.mCoderTag, Coder_DES.class, e);
				}
			}

		}
		return false;
	}

	/**
	 * 文件file进行加密并保存目标文件destFile中
	 * 
	 * @param file
	 *            要加密的文件
	 * @param destFile
	 *            加密后存放的文件名
	 */
	public boolean encrypt(File file, File destFile) {

		InputStream is = null;
		OutputStream out = null;
		CipherInputStream cis = null;

		try {

			Cipher cipher = Cipher.getInstance("DES");
			cipher.init(Cipher.ENCRYPT_MODE, this.mKey);
			is = new FileInputStream(file);
			out = new FileOutputStream(destFile);
			cis = new CipherInputStream(is, cipher);
			byte[] buffer = new byte[1024];
			int r;
			while ((r = cis.read(buffer)) > 0) {
				out.write(buffer, 0, r);
			}

			return true;
		} catch (Throwable e) {
			if (Debug_SDK.isCoderLog) {
				Debug_SDK.te(Debug_SDK.mCoderTag, Coder_DES.class, e);
			}
		} finally {

			try {
				if (out != null) {
					out.close();
				}
			} catch (Throwable e) {
				if (Debug_SDK.isCoderLog) {
					Debug_SDK.te(Debug_SDK.mCoderTag, Coder_DES.class, e);
				}
			}

			try {
				if (cis != null) {
					cis.close();
				}
			} catch (Throwable e) {
				if (Debug_SDK.isCoderLog) {
					Debug_SDK.te(Debug_SDK.mCoderTag, Coder_DES.class, e);
				}
			}

			try {
				if (is != null) {
					is.close();
				}
			} catch (Throwable e) {
				if (Debug_SDK.isCoderLog) {
					Debug_SDK.te(Debug_SDK.mCoderTag, Coder_DES.class, e);
				}
			}

		}

		return false;
	}

	/**
	 * 文件采用DES算法解密文件
	 * 
	 * @param file
	 *            已加密的文件
	 * @param dest
	 *            解密后存放的文件
	 */
	public boolean decrypt(File file, File dest) {
		InputStream is = null;
		OutputStream out = null;
		CipherOutputStream cos = null;
		try {

			Cipher cipher = Cipher.getInstance("DES");
			cipher.init(Cipher.DECRYPT_MODE, this.mKey);
			is = new FileInputStream(file);
			out = new FileOutputStream(dest);
			cos = new CipherOutputStream(out, cipher);
			byte[] buffer = new byte[1024];
			int r;
			while ((r = is.read(buffer)) >= 0) {
				cos.write(buffer, 0, r);
			}

			return true;
		} catch (Throwable e) {
			if (Debug_SDK.isCoderLog) {
				Debug_SDK.te(Debug_SDK.mCoderTag, Coder_DES.class, e);
			}
		} finally {

			try {
				if (out != null) {
					out.close();
				}
			} catch (Throwable e) {
				if (Debug_SDK.isCoderLog) {
					Debug_SDK.te(Debug_SDK.mCoderTag, Coder_DES.class, e);
				}
			}

			try {
				if (cos != null) {
					cos.close();
				}
			} catch (Throwable e) {
				if (Debug_SDK.isCoderLog) {
					Debug_SDK.te(Debug_SDK.mCoderTag, Coder_DES.class, e);
				}
			}

			try {
				if (is != null) {
					is.close();
				}
			} catch (Throwable e) {
				if (Debug_SDK.isCoderLog) {
					Debug_SDK.te(Debug_SDK.mCoderTag, Coder_DES.class, e);
				}
			}
		}
		return false;
	}
}
