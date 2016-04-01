package net.youmi.android.libs.common.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.NinePatch;
import android.graphics.drawable.NinePatchDrawable;

import net.youmi.android.libs.common.basic.Basic_StringUtil;
import net.youmi.android.libs.common.coder.Coder_Base64;
import net.youmi.android.libs.common.debug.Debug_SDK;
import net.youmi.android.libs.common.global.Global_Charsets;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

/**
 * base64 与 图片之间的转换 有些方法还待测试
 * 
 * @author zhitaocai edit on 2014-7-4
 */
public class Util_Res_Base64_Parser {

	/**
	 * 从base64码获取其所代表的bitmap
	 */
	public synchronized static Bitmap decodeBitmapFromBase64(String base64) {
		try {
			if (base64 == null) {
				if (Debug_SDK.isUtilLog) {
					Debug_SDK.te(Debug_SDK.mUtilTag, Util_Res_Base64_Parser.class, "获取bitmap时出错，base64 == null");
				}
				return null;
			}
			byte[] buff = Coder_Base64.decodeToBytes(base64);
			if (buff == null) {
				if (Debug_SDK.isUtilLog) {
					Debug_SDK.te(Debug_SDK.mUtilTag, Util_Res_Base64_Parser.class,
							"获取bitmap时出错，解析base64字符串为数组buff == null");
				}
				return null;
			}
			return BitmapFactory.decodeByteArray(buff, 0, buff.length);

		} catch (Throwable e) {
			if (Debug_SDK.isUtilLog) {
				Debug_SDK.te(Debug_SDK.mUtilTag, Util_Res_Base64_Parser.class, e);
			}
		}
		return null;
	}

	private synchronized static byte[] Bitmap2Bytes(Bitmap bm) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
		return baos.toByteArray();
	}

	/**
	 * 从bitmap中获取base64字符串 （待测试）
	 * 
	 * @param bm
	 * @return
	 */
	public synchronized static String getBitmapBase64StringFromBitmap(Bitmap bm) {
		return changeByteArrayToBase64String(Bitmap2Bytes(bm));
	}

	/**
	 * 保存位图到files目录 (测试用)
	 * 
	 * @param context
	 * @param bm
	 * @param fileName
	 */
	public synchronized static void saveBitmapToFile(Context context, Bitmap bm, String fileName) {
		FileOutputStream fOut = null;
		try {
			File file = context.getFileStreamPath(fileName + ".png");
			if (file != null && file.exists() && file.isFile()) {
				file.delete();
				file.createNewFile();
			}
			try {
				fOut = new FileOutputStream(file);
				bm.compress(Bitmap.CompressFormat.PNG, 100, fOut);
				fOut.flush();
			} catch (Throwable e) {
				if (Debug_SDK.isUtilLog) {
					Debug_SDK.te(Debug_SDK.mUtilTag, Util_Res_Base64_Parser.class, e);
				}
			}
		} catch (Throwable e) {
			if (Debug_SDK.isUtilLog) {
				Debug_SDK.te(Debug_SDK.mUtilTag, Util_Res_Base64_Parser.class, e);
			}
		} finally {
			try {
				if (fOut != null) {
					fOut.close();
				}
			} catch (Throwable e) {
				if (Debug_SDK.isUtilLog) {
					Debug_SDK.te(Debug_SDK.mUtilTag, Util_Res_Base64_Parser.class, e);
				}
			}
		}
	}

	/**
	 * 从Resouces Id中获取base64字符串 （待测试）
	 * 
	 * @param bm
	 * @return
	 */
	public synchronized static String getBitmapBase64StringFromResId(Context context, int resId) {
		return changeByteArrayToBase64String(Bitmap2Bytes(BitmapFactory.decodeResource(context.getResources(), resId)));
	}

	/**
	 * 从base64 中获取.9png drawable
	 * 
	 * @param bmBase64
	 * @param chunkBase64
	 * @return NinePatchDrawable
	 */
	public synchronized static NinePatchDrawable decodeNinePathchFromBase64(Context context, String bmBase64,
			String chunkBase64) {
		try {
			if (context == null || Basic_StringUtil.isNullOrEmpty(bmBase64)
					|| Basic_StringUtil.isNullOrEmpty(chunkBase64)) {
				if (Debug_SDK.isUtilLog) {
					Debug_SDK.te(Debug_SDK.mUtilTag, Util_Res_Base64_Parser.class, "获取.9png时出错，参数有误");
				}
				return null;
			}

			// 获取位图
			Bitmap bitmap = decodeBitmapFromBase64(bmBase64);
			if (bitmap == null) {
				if (Debug_SDK.isUtilLog) {
					Debug_SDK.te(Debug_SDK.mUtilTag, Util_Res_Base64_Parser.class, "获取.9png时出错，bitmap == null");
				}
				return null;
			}

			// 加载.9png数据块信息
			byte[] chunkBytes = Coder_Base64.decodeToBytes(chunkBase64);
			if (chunkBytes == null) {
				if (Debug_SDK.isUtilLog) {
					Debug_SDK.te(Debug_SDK.mUtilTag, Util_Res_Base64_Parser.class, "获取.9png时出错，chunkBytes == null");
				}
				return null;
			}

			NinePatch ninePatch = new NinePatch(bitmap, chunkBytes, null);
			return new NinePatchDrawable(context.getResources(), ninePatch);

		} catch (Throwable e) {
			if (Debug_SDK.isUtilLog) {
				Debug_SDK.te(Debug_SDK.mUtilTag, Util_Res_Base64_Parser.class, e);
			}
		}
		return null;
	}

	/**
	 * 获取.9png的数据块信息
	 * 
	 * @param bmBase64
	 *            bitmap base64String
	 * @return byte []
	 */
	private synchronized static byte[] getNinePathChunkByteArrayFromBase64(String bmBase64) {
		return getNinePathChunkByteArrayFromBitmap(decodeBitmapFromBase64(bmBase64));
	}

	/**
	 * 获取.9png的数据块信息(注意这里是有点坑的，要试试没有加.9png格式之前的base64 以及加了那4条线之后的 base64 貌似只有一个是可以的)
	 * 
	 * @param base64
	 *            bitmap base64String
	 * @return base64 String
	 */
	public synchronized static String getNinePathChunkBase64StringFromBase64(String bmBase64) {
		return changeByteArrayToBase64String(getNinePathChunkByteArrayFromBase64(bmBase64));
	}

	/**
	 * 传入res的id来获取该.9png的chunk 亲测可以
	 * 
	 * @param resId
	 * @return byte[]
	 */
	private synchronized static byte[] getNinePathChunkByteArrayFromResId(Context context, int resId) {
		return getNinePathChunkByteArrayFromBitmap(BitmapFactory.decodeResource(context.getResources(), resId));
	}

	/**
	 * 传入res的id来获取该.9png的chunk 亲测可以
	 * 
	 * @param resId
	 * @return byte[]
	 */
	public synchronized static String getNinePathChunkBase64StringFromResId(Context context, int resId) {
		return changeByteArrayToBase64String(getNinePathChunkByteArrayFromResId(context, resId));
	}

	/**
	 * 从bitmap中获取.9png的chunk
	 * 
	 * @param bitmap
	 * @return byte[]
	 */
	private synchronized static byte[] getNinePathChunkByteArrayFromBitmap(Bitmap bitmap) {
		try {
			if (bitmap == null) {
				if (Debug_SDK.isUtilLog) {
					Debug_SDK.te(Debug_SDK.mUtilTag, Util_Res_Base64_Parser.class, "解析获取.9png的chunk时出错，bitmap == null");
				}
				return null;
			}
			// 从bitmap 中获取chunk
			byte[] chunk = bitmap.getNinePatchChunk();
			if (chunk == null) {
				if (Debug_SDK.isUtilLog) {
					Debug_SDK.te(Debug_SDK.mUtilTag, Util_Res_Base64_Parser.class, "解析获取.9png的chunk时出错，chunk == null");
				}
				return null;
			}

			// 判断是否为.9png的数据块
			if (!NinePatch.isNinePatchChunk(chunk)) {
				if (Debug_SDK.isUtilLog) {
					Debug_SDK.te(Debug_SDK.mUtilTag, Util_Res_Base64_Parser.class,
							"解析获取.9png的chunk时出错，chunk不是.9png的数据块");
				}
				return null;
			}
			return chunk;
		} catch (Throwable e) {
			if (Debug_SDK.isUtilLog) {
				Debug_SDK.te(Debug_SDK.mUtilTag, Util_Res_Base64_Parser.class, e);
			}
		}
		// finally {
		// if (bitmap != null && !bitmap.isRecycled()) {
		// bitmap.recycle();
		// }
		// }
		return null;
	}

	/**
	 * 从bitmap中获取.9png的chunk
	 * 
	 * @param bitmap
	 * @return String
	 */
	public synchronized static String getNinePathChunkBase64StringFromBitmap(Bitmap bitmap) {
		return changeByteArrayToBase64String(getNinePathChunkByteArrayFromBitmap(bitmap));
	}

	/**
	 * 将byte数组转换为base64String
	 * 
	 * @param bytes
	 * @return
	 */
	private synchronized static String changeByteArrayToBase64String(byte[] bytes) {
		try {
			String src = Global_Charsets.Change.toUTF_8(bytes);
			if (!Basic_StringUtil.isNullOrEmpty(src)) {
				if (Debug_SDK.isUtilLog) {
					Debug_SDK.tv(Debug_SDK.mUtilTag, Util_Res_Base64_Parser.class, "原始字符串为：\n%s", src);
				}
				String encodeStr = Coder_Base64.encode(src);
				if (Debug_SDK.isUtilLog) {
					Debug_SDK.tv(Debug_SDK.mUtilTag, Util_Res_Base64_Parser.class, "base64加密后，字符串为：\n%s", encodeStr);
				}
				return encodeStr;
			}
		} catch (Throwable e) {
			if (Debug_SDK.isUtilLog) {
				Debug_SDK.te(Debug_SDK.mUtilTag, Util_Res_Base64_Parser.class, e);
			}
		}
		return null;

	}
}
