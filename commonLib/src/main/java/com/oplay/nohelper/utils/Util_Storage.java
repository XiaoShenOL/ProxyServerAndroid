package com.oplay.nohelper.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;

import com.oplay.nohelper.assist.debug.Debug_SDK;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import static android.os.Environment.MEDIA_MOUNTED;

/**
 * @author zyq 16-3-10
 */
public class Util_Storage {

	private static final String TAG = Debug_SDK.mStorageTag;
	public static final String SD_CARD = "sdCard";
	public static final String EXTERNAL_SD_CARD = "externalSdCard";
	public static final String EXTERNAL_STORAGE_PERMISSION = "android.permission.WRITE_EXTERNAL_STORAGE";
	public static final String DEFAULT_DIR_NAME = "volley";

	/**
	 * Returns application cache directory. Cache directory will be created on SD card
	 * <i>("/Android/data/[app_package_name]/cache")</i> (if card is mounted and app has appropriate permission) or
	 * on device's file system depending incoming parameters.
	 *
	 * @param context        Application context
	 * @param preferExternal Whether prefer external location for cache
	 * @return Cache {@link File directory}.<br />
	 * <b>NOTE:</b> Can be null in some unpredictable cases (if SD card is unmounted and
	 * {@link Context#getCacheDir() Context.getCacheDir()} returns null).
	 */
	public static File getCacheDirectory(Context context, boolean preferExternal) {
		if (context == null) return null;
		if (context != null) context = context.getApplicationContext();

		File appCacheDir = null;
		String externalStorageState;
		try {
			externalStorageState = Environment.getExternalStorageState();
		} catch (NullPointerException e) { // (sh)it happens (Issue #660)
			externalStorageState = "";
		}
		if (preferExternal && MEDIA_MOUNTED.equals(externalStorageState) && hasExternalStoragePermission(context)) {
			appCacheDir = getExternalCacheDir(context);
		}
		if (appCacheDir == null) {
			appCacheDir = context.getCacheDir();
		}
		if (appCacheDir == null) {
			String cacheDirPath = "/data/data/" + context.getPackageName() + "/kidscache/";
			if (Debug_SDK.isStorageLog) {
				Debug_SDK.dw(TAG, "Can't define system cache directory! '%s' will be used.", cacheDirPath);
			}
			appCacheDir = new File(cacheDirPath);
		}
		return appCacheDir;
	}

	public static String getExternalSdCard(Context context) {
		String externalStorageState;
		try {
			externalStorageState = Environment.getExternalStorageState();
		} catch (NullPointerException e) {
			externalStorageState = "";
		}
		return externalStorageState;
	}

	public static File createReserveDiskCacheDir(Context context, String dirname) {
		File cacheDir = Util_Storage.getCacheDirectory(context, true);
		File individualDir = new File(cacheDir, dirname);
		if (individualDir.exists() || individualDir.mkdir()) {
			cacheDir = individualDir;
		}
		return cacheDir;
	}

	/**
	 * 存放在sd0卡
	 *
	 * @param context
	 * @return
	 */
	private static File getExternalCacheDir(Context context) {
		if (context == null) return null;
		if (context != null) context = context.getApplicationContext();

		File dataDir = new File(new File(Environment.getExternalStorageDirectory(), "Android"), "data");
		File appCacheDir = new File(new File(dataDir, context.getPackageName()), "cache");
		if (!appCacheDir.exists()) {
			if (!appCacheDir.mkdirs()) {
				if (Debug_SDK.isStorageLog) {
					Debug_SDK.dw(TAG, "%s", "Unable to create external cache directory");
				}
				return null;
			}
			try {
				new File(appCacheDir, ".nomedia").createNewFile();
			} catch (IOException e) {
				if (Debug_SDK.isStorageLog) {
					Debug_SDK.dw(TAG, "%s", "Can't create \".nomedia\" file in application external cache directory");
				}
			}
		}
		return appCacheDir;
	}

	private static boolean hasExternalStoragePermission(Context context) {
		if (context == null) return false;
		if (context != null) context = context.getApplicationContext();

		int perm = context.checkCallingOrSelfPermission(EXTERNAL_STORAGE_PERMISSION);
		return perm == PackageManager.PERMISSION_GRANTED;
	}

	/**
	 * Returns individual application cache directory (for only image caching from ImageLoader). Cache directory
	 * will be
	 * created on SD card <i>("/Android/data/[app_package_name]/cache/uil-images")</i> if card is mounted and app has
	 * appropriate permission. Else - Android defines cache directory on device's file system.
	 *
	 * @param context Application context
	 * @return Cache {@link File directory}
	 */
	public static File getIndividualCacheDirectory(Context context) {
		return getIndividualCacheDirectory(context, DEFAULT_DIR_NAME);
	}

	/**
	 * Returns individual application cache directory (for only image caching from ImageLoader). Cache directory
	 * will be
	 * created on SD card <i>("/Android/data/[app_package_name]/cache/uil-images")</i> if card is mounted and app has
	 * appropriate permission. Else - Android defines cache directory on device's file system.
	 *
	 * @param context  Application context
	 * @param cacheDir Cache directory path (e.g.: "AppCacheDir", "AppDir/cache/images")
	 * @return Cache {@link File directory}
	 */
	public static File getIndividualCacheDirectory(Context context, String cacheDir) {
		if (context == null) return null;
		if (context != null) context = context.getApplicationContext();

		File appCacheDir = getCacheDirectory(context);
		File individualCacheDir = new File(appCacheDir, cacheDir);
		if (!individualCacheDir.exists()) {
			if (!individualCacheDir.mkdir()) {
				individualCacheDir = appCacheDir;
			}
		}
		return individualCacheDir;
	}

	/**
	 * Returns application cache directory. Cache directory will be created on SD card
	 * <i>("/Android/data/[app_package_name]/cache")</i> if card is mounted and app has appropriate permission. Else -
	 * Android defines cache directory on device's file system.
	 *
	 * @param context Application context
	 * @return Cache {@link File directory}.<br />
	 * <b>NOTE:</b> Can be null in some unpredictable cases (if SD card is unmounted and
	 * {@link Context#getCacheDir() Context.getCacheDir()} returns null).
	 */
	public static File getCacheDirectory(Context context) {
		return getCacheDirectory(context, true);
	}

	/**
	 * 获取自定义的缓存区,对于sd卡需要确定有足够的内存空间.
	 *
	 * @param context  Application context
	 * @param cacheDir Cache directory path (e.g.: "AppCacheDir", "AppDir/cache/images")
	 * @return Cache {@link File directory}
	 */
	public static File getOwnCacheDirectory(Context context, String cacheDir, boolean preferExternal, boolean
			preferInternal, long limitSize) {
		if (context == null) return null;
		if (context != null) context = context.getApplicationContext();

		File appCacheDir = null;
		if (preferExternal && MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) &&
				hasExternalStoragePermission(context)) {
			if (isSdCanWrite_And_EnoughSpace(context, limitSize)) {
				appCacheDir = new File(context.getExternalCacheDir(), cacheDir);
			}
		}
		if (appCacheDir == null || (!appCacheDir.exists() && !appCacheDir.mkdirs())) {
			if (preferInternal) {
				appCacheDir = context.getCacheDir();
			}
		}
		return appCacheDir;
	}


	/**
	 * @return True if the external storage is available. False otherwise.
	 */
	public static boolean isAvailable() {
		String state = Environment.getExternalStorageState();
		return Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
	}

	/**
	 * 获取sd卡的根路径
	 *
	 * @return
	 */
	public static String getSdCardPath() {
		return Environment.getExternalStorageDirectory().getPath() + "/";
	}

	/**
	 * @return True if the external storage is writable. False otherwise.
	 */
	public static boolean isWritable() {
		String state = Environment.getExternalStorageState();
		return Environment.MEDIA_MOUNTED.equals(state);

	}

	/**
	 * 这里只对外部存储sd的容量做判断,对内部存储Context.getCacheDir(),机身不足时候,文件会被删除,而外部存储不会
	 *
	 * @param context
	 * @param limitSize
	 * @return
	 */
	public static boolean isSdCanWrite_And_EnoughSpace(Context context, long limitSize) {
		if (context == null) return false;
		if (context != null) context = context.getApplicationContext();
		if (MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) && hasExternalStoragePermission(context)) {
			String path = Environment.getExternalStorageDirectory().getPath();

			StatFs statFs = new StatFs(path);
			//获取block 的SIZE
			long blockSize = statFs.getBlockSize();

			//已经使用block的数量
			long availableBlock = statFs.getAvailableBlocks();

			long availableSize = blockSize * availableBlock;

			if (availableSize < 0) {
				availableSize = Math.abs(availableSize);
			}

			if (Debug_SDK.isStorageLog) {
				Debug_SDK.ti(Debug_SDK.mStorageTag, Util_Storage.class, "sdcard:" + path + ",可用容量:"
						+ availableSize + ",需求容量:" + limitSize + ",可用块:" + availableBlock + ",每块容量:" + blockSize);
			}
			if (availableSize >= limitSize) {
				if (Debug_SDK.isStorageLog) {
					Debug_SDK.ti(Debug_SDK.mStorageTag, Util_Storage.class, "sdcard容量充足");
				}
				return true;
			} else {
				if (Debug_SDK.isStorageLog) {
					Debug_SDK.ti(Debug_SDK.mStorageTag, Util_Storage.class, "sdcard容量不足");
				}
			}
		} else {
			if (Debug_SDK.isStorageLog) {
				Debug_SDK.ti(Debug_SDK.mStorageTag, Util_Storage.class, "sdcard不可写");
			}
		}
		return false;
	}


	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	public static long getUsableSpace(File path) {
		if (path == null) {
			return -1;
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			return path.getUsableSpace();
		} else {
			if (!path.exists()) {
				return 0;
			} else {
				final StatFs stats = new StatFs(path.getPath());
				return (long) stats.getBlockSize() * (long) stats.getAvailableBlocks();
			}
		}
	}

	public static boolean deleteDir(File f) {
		if (!f.exists()) return false;
		if (f.isDirectory()) {
			for (File file : f.listFiles()) {
				deleteDir(file);
			}
		}
		return f.delete();
	}

	/**
	 * @return A map of all storage locations available
	 */
	public static Map<String, File> getAllStorageLocations() {
		Map<String, File> map = new HashMap<String, File>(10);

		List<String> mMounts = new ArrayList<String>(10);
		List<String> mVold = new ArrayList<String>(10);
		mMounts.add("/mnt/sdcard");
		mVold.add("/mnt/sdcard");

		try {
			File mountFile = new File("/proc/mounts");
			if (mountFile.exists()) {
				Scanner scanner = new Scanner(mountFile);
				while (scanner.hasNext()) {
					String line = scanner.nextLine();
					if (line.startsWith("/dev/block/vold/")) {
						String[] lineElements = line.split(" ");
						String element = lineElements[1];

						// don't add the default mount path
						// it's already in the list.
						if (!element.equals("/mnt/sdcard"))
							mMounts.add(element);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			File voldFile = new File("/system/etc/vold.fstab");
			if (voldFile.exists()) {
				Scanner scanner = new Scanner(voldFile);
				while (scanner.hasNext()) {
					String line = scanner.nextLine();
					if (line.startsWith("dev_mount")) {
						String[] lineElements = line.split(" ");
						String element = lineElements[2];

						if (element.contains(":"))
							element = element.substring(0, element.indexOf(":"));
						if (!element.equals("/mnt/sdcard"))
							mVold.add(element);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}


		for (int i = 0; i < mMounts.size(); i++) {
			String mount = mMounts.get(i);
			if (!mVold.contains(mount))
				mMounts.remove(i--);
		}
		mVold.clear();

		List<String> mountHash = new ArrayList<String>(10);

		for (String mount : mMounts) {
			File root = new File(mount);
			if (root.exists() && root.isDirectory() && root.canWrite()) {
				File[] list = root.listFiles();
				String hash = "[";
				if (list != null) {
					for (File f : list) {
						hash += f.getName().hashCode() + ":" + f.length() + ", ";
					}
				}
				hash += "]";
				if (!mountHash.contains(hash)) {
					String key = SD_CARD + "_" + map.size();
					if (map.size() == 0) {
						key = SD_CARD;
					} else if (map.size() == 1) {
						key = EXTERNAL_SD_CARD;
					}
					mountHash.add(hash);
					map.put(key, root);
				}
			}
		}

		mMounts.clear();

		if (map.isEmpty()) {
			map.put(SD_CARD, Environment.getExternalStorageDirectory());
		}

		return map;
	}

	/**
	 * copy file
	 *
	 * @param src  source file
	 * @param dest target file
	 * @throws IOException
	 */
	public static void copyFile(File src, File dest) throws IOException {
		FileChannel inChannel = null;
		FileChannel outChannel = null;
		try {
			if (!dest.exists()) {
				dest.createNewFile();
			}
			inChannel = new FileInputStream(src).getChannel();
			outChannel = new FileOutputStream(dest).getChannel();
			inChannel.transferTo(0, inChannel.size(), outChannel);
		} finally {
			if (inChannel != null) {
				inChannel.close();
			}
			if (outChannel != null) {
				outChannel.close();
			}
		}
	}

	/**
	 * delete file
	 *
	 * @param file file
	 * @return true if delete success
	 */
	public static boolean deleteFile(File file) {
		if (!file.exists()) {
			return true;
		}
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			for (File f : files) {
				deleteFile(f);
			}
		}
		return file.delete();
	}
}
