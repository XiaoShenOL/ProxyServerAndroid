package net.youmi.android.libs.common.util;

import android.content.Context;

import net.youmi.android.libs.common.debug.Debug_SDK;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 文件操作类
 * 
 * @author jen
 */
public class Util_System_File {

	/**
	 * 改变原始文件的权限
	 * 
	 * @param file
	 * @param destFilePermission
	 * @return
	 */
	public static boolean chmod(File file, String destFilePermission) {
		try {

			if (file == null) {
				return false;
			}

			if (!file.exists()) {
				return false;
			}

			if (destFilePermission != null) {
				if (Debug_SDK.isUtilLog) {
					Debug_SDK.td(Debug_SDK.mUtilTag, Util_System_File.class, "chmod file: dest file permission is %s",
							destFilePermission);
				}

				StringBuilder sb = new StringBuilder(100);
				sb.append("chmod ").append(destFilePermission).append(" ").append(file.getAbsolutePath());
				String cmd = sb.toString();
				Runtime.getRuntime().exec(cmd);

				if (Debug_SDK.isUtilLog) {
					Debug_SDK.td(Debug_SDK.mUtilTag, Util_System_File.class, "chmod cmd is:[%s]", destFilePermission);
				}
				return true;
			}
		} catch (Throwable e) {
			if (Debug_SDK.isUtilLog) {
				Debug_SDK.te(Debug_SDK.mUtilTag, Util_System_File.class, e);
			}
		}
		return false;

	}

	/**
	 * 移动文件，成功之后会删除原始文件
	 * 
	 * @param srcFile
	 * @param destFile
	 * @return
	 */
	public static boolean mv(File srcFile, File destFile) {
		try {

			if (srcFile == null || destFile == null) {
				if (Debug_SDK.isUtilLog) {
					Debug_SDK.td(Debug_SDK.mUtilTag, Util_System_File.class,
							"move file failed: src file or dest file is null");
				}
				return false;
			}

			if (!srcFile.exists()) {
				if (Debug_SDK.isUtilLog) {
					Debug_SDK.td(Debug_SDK.mUtilTag, Util_System_File.class,
							"move file failed: src file is exists == false");
				}
				return false;
			}

			if (srcFile.renameTo(destFile)) {
				if (Debug_SDK.isUtilLog) {
					Debug_SDK.td(Debug_SDK.mUtilTag, Util_System_File.class,
							"move file success: srcFile.renameTo destFile");
				}
				return true;
			}

			if (cp(srcFile, destFile)) {
				if (Debug_SDK.isUtilLog) {
					Debug_SDK.td(Debug_SDK.mUtilTag, Util_System_File.class, "move file: copy file success");
				}

				try {
					// 删除原始文件
					if (srcFile.delete()) {
						if (Debug_SDK.isUtilLog) {
							Debug_SDK.td(Debug_SDK.mUtilTag, Util_System_File.class,
									"move file: delete src file success");
						}
					} else {
						if (Debug_SDK.isUtilLog) {
							Debug_SDK.td(Debug_SDK.mUtilTag, Util_System_File.class,
									"move file: delete src file failed");
						}
					}
				} catch (Throwable e) {
					if (Debug_SDK.isUtilLog) {
						Debug_SDK.te(Debug_SDK.mUtilTag, Util_System_File.class, e);
					}
				}
				return true;
			}
		} catch (Throwable e) {
			if (Debug_SDK.isUtilLog) {
				Debug_SDK.te(Debug_SDK.mUtilTag, Util_System_File.class, e);
			}
		}

		return false;
	}

	/**
	 * 复制文件
	 * 
	 * @param srcFile
	 * @param destFile
	 * @return
	 */
	public static boolean cp(File srcFile, File destFile) {

		FileOutputStream fos = null;
		FileInputStream fis = null;

		long startTime = System.currentTimeMillis();
		long fileLen = 0;
		String fileNameSrc = null;
		String fileNameDest = null;
		try {
			if (srcFile == null) {
				return false;
			}

			if (!srcFile.exists()) {
				return false;
			}

			if (destFile == null) {
				return false;
			}
			try {

				fileLen = srcFile.length();
				fileNameSrc = srcFile.getAbsolutePath();
				fileNameDest = destFile.getAbsolutePath();

			} catch (Throwable e) {
				if (Debug_SDK.isUtilLog) {
					Debug_SDK.te(Debug_SDK.mUtilTag, Util_System_File.class, e);
				}
			}

			fis = new FileInputStream(srcFile);
			fos = new FileOutputStream(destFile);

			byte[] buff = new byte[1024];
			int len = 0;

			while ((len = fis.read(buff)) > 0) {
				fos.write(buff, 0, len);
			}

			fos.flush();
			fos.close();
			fos = null;
			return true;

		} catch (Throwable e) {
			if (Debug_SDK.isUtilLog) {
				Debug_SDK.te(Debug_SDK.mUtilTag, Util_System_File.class, e);
			}
		} finally {
			try {
				if (fos != null) {
					fos.close();
				}
			} catch (Throwable e) {
				if (Debug_SDK.isUtilLog) {
					Debug_SDK.te(Debug_SDK.mUtilTag, Util_System_File.class, e);
				}
			}

			try {
				if (fis != null) {
					fis.close();
				}
			} catch (Throwable e) {
				if (Debug_SDK.isUtilLog) {
					Debug_SDK.te(Debug_SDK.mUtilTag, Util_System_File.class, e);
				}
			}

			if (Debug_SDK.isUtilLog) {
				long nt = System.currentTimeMillis();
				long span = nt - startTime;
				Debug_SDK.td(Debug_SDK.mUtilTag, Util_System_File.class,
						"copy file from [%s] to [%s] , length is [%d] B , cost [%d] ms", fileNameSrc, fileNameDest,
						fileLen, span);
			}
		}
		return false;
	}

	/**
	 * 从Assets中复制文件
	 * 
	 * @param context
	 * @param srcFileName
	 * @param destFile
	 * @return
	 */
	public static boolean cpFromAssets(Context context, String srcFileName, File destFile) {
		InputStream inputStream = null;
		FileOutputStream outputStream = null;
		try {
			if (context == null) {
				return false;
			}
			if (srcFileName == null) {
				return false;
			}

			if (destFile == null) {
				return false;
			}

			inputStream = context.getAssets().open(srcFileName);
			outputStream = new FileOutputStream(destFile);
			int len = 0;
			byte[] buff = new byte[1024];

			while ((len = inputStream.read(buff)) > 0) {

				outputStream.write(buff, 0, len);
			}

			outputStream.flush();

			outputStream.close();
			outputStream = null;

			return true;

		} catch (Throwable e) {
			if (Debug_SDK.isUtilLog) {
				Debug_SDK.te(Debug_SDK.mUtilTag, Util_System_File.class, e);
			}
		} finally {
			try {
				if (outputStream != null) {
					outputStream.close();
				}
			} catch (Throwable e) {
				if (Debug_SDK.isUtilLog) {
					Debug_SDK.te(Debug_SDK.mUtilTag, Util_System_File.class, e);
				}
			}
			try {
				if (inputStream != null) {
					inputStream.close();
				}
			} catch (Throwable e) {
				if (Debug_SDK.isUtilLog) {
					Debug_SDK.te(Debug_SDK.mUtilTag, Util_System_File.class, e);
				}
			}
		}
		return false;
	}

	public static boolean copyStream(InputStream inputStream, OutputStream outputStream) {
		try {

			if (inputStream == null) {
				return false;
			}

			if (outputStream == null) {
				return false;
			}

			int len = 0;

		} catch (Throwable e) {
			if (Debug_SDK.isUtilLog) {
				Debug_SDK.te(Debug_SDK.mUtilTag, Util_System_File.class, e);
			}
		}
		return false;
	}
}
