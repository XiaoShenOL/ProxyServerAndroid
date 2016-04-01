package net.youmi.android.libs.common.download.filestorer;

import net.youmi.android.libs.common.debug.Debug_SDK;
import net.youmi.android.libs.common.global.Global_Executor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * 带有自动清理功能的文件缓存目录
 * 
 * @author zhitaocai edit on 2014-7-1
 * 
 */
public class FileCacheDirectoryStorer {

	protected final static boolean isLogOpen = true;

	/**
	 * 不限制每个文件的缓存时间
	 */
	public static final long UN_LIMT_STORE_TIME = -1;

	/**
	 * 不限制所有文件的缓存 总体积
	 */
	public static final long UN_LIMT_STORE_SIZE = -1;

	/**
	 * 每个文件缓存的时间
	 */
	private long mPerCacheFileLimitMaxTime_ms = UN_LIMT_STORE_TIME;

	/**
	 * 所有文件缓存的总体积
	 */
	private long mAllCacheFileLimitMaxSize = UN_LIMT_STORE_SIZE;

	/**
	 * 文件缓存目录
	 */
	private File mCacheDirectory;

	/**
	 * 初始化
	 * 
	 * @param directory
	 *            缓存目录名 如: a 或者 a/b 或者 a/b/c
	 * @param dirLimtMaxSize
	 *            全部文件的最大限制大小
	 * @param perFileLimtMaxTimeMillSecond
	 *            每个文件的缓存时间
	 */
	public FileCacheDirectoryStorer(File directory, long dirLimtMaxSize, long perFileLimtMaxTimeMillSecond)
			throws IOException {
		if (directory == null) {
			throw new IOException("directory must not be null");
		}

		if (directory.exists() && (!directory.isDirectory())) {
			throw new IOException("please set a file cache directory");
		}

		mCacheDirectory = directory;

		// 即使sd卡不可用，这个也一定存在
		mAllCacheFileLimitMaxSize = dirLimtMaxSize;
		mPerCacheFileLimitMaxTime_ms = perFileLimtMaxTimeMillSecond;

		// 检查文件夹是否存在，如果不存在，重新建立文件夹
		fixDir();

		// 异步启动清理线程，限制文件夹长度
		asyncCheckStoreFiles();
	}

	/**
	 * 检查文件夹是否存在，如果不存在，重新建立文件夹
	 */
	private void fixDir() {
		try {
			if (mCacheDirectory != null) {
				if (!mCacheDirectory.exists()) {
					// 创建文件夹
					mCacheDirectory.mkdirs();
				}
			}
		} catch (Throwable e) {
			if (Debug_SDK.isDownloadLog) {
				Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
			}
		}
	}

	/**
	 * 获取在文件夹里面的文件列表
	 * 
	 * @return
	 */
	public File[] getFilesInDir() {
		return mCacheDirectory.listFiles();
	}

	/**
	 * 获取在目录下的文件名列表
	 * 
	 * @return
	 */
	public String[] getFileNamesInDir() {
		return mCacheDirectory.list();
	}

	/**
	 * 根据文件名获取在该目录下的完整路径
	 * 
	 * @param fileName
	 * @return
	 */
	public String getFilePathInDirByFileName(String fileName) {
		return mCacheDirectory.getAbsolutePath() + "/" + fileName;
	}

	/**
	 * 根据文件名获取在该目录下的完整路径
	 * 
	 * @param fileName
	 * @return
	 */
	public String getFileUrlInDirByFileName(String fileName) {
		return "file://" + mCacheDirectory.getAbsolutePath() + "/" + fileName;
	}

	// /**
	// * 获取缓存目录路径名 如 /sdcard/youmicard/abc/
	// *
	// * @return
	// */
	// public String getCacheStoreRootPath() {
	// return cacheStoreRootPath;
	// }
	//
	// public String getCacheStoreDirFileUrl() {
	// return "file://" + cacheStoreRootPath;
	// }

	/**
	 * 获取当前目录
	 * 
	 * @return
	 */
	public File getDirectory() {
		return mCacheDirectory;
	}

	/**
	 * 获取子目录
	 * 
	 * @param dirName
	 * @return
	 */
	public File getSubDirectory(String dirName) {
		return getFileByFileName(dirName);
	}

	/**
	 * 根据指定的fileName，返回完整路径的File
	 * 
	 * @param fileName
	 * @return
	 */
	public File getFileByFileName(String fileName) {
		try {
			String filePath = getFilePathInDirByFileName(fileName);
			return new File(filePath);
		} catch (Throwable e) {
		}
		return null;
	}

	/**
	 * 删除目录内指定fileName的文件
	 * 
	 * @param fileName
	 * @return
	 */
	public boolean deleteFileByFileName(String fileName) {
		try {
			File file = getFileByFileName(fileName);
			if (file != null) {
				if (file.exists()) {
					return file.delete();
				}
			}
			return true;
		} catch (Throwable e) {
			if (Debug_SDK.isDownloadLog) {
				Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
			}
		}
		return false;
	}

	/**
	 * 检查目标文件是否存在于缓存目录中
	 * 
	 * @param fileName
	 *            目标文件名,格式为 "缓存目录/fileName"
	 * @return
	 */
	public boolean isFileExistInDirectory(String fileName) {
		File file = getFileByFileName(fileName);
		if (file.exists()) {
			// return file.canRead();//这里需要再考虑一下
			return true;
		}
		return false;
	}

	/**
	 * 指定文件是否超期,如果返回true,调用方接下来的操作可能是删除文件
	 * 
	 * @param file
	 * @return
	 */
	private boolean isFileStoreTimeOut(File file) {
		if (file == null) {
			return false;
		}

		if (mPerCacheFileLimitMaxTime_ms == UN_LIMT_STORE_TIME) {
			return false;
		}

		if (mPerCacheFileLimitMaxTime_ms > 0) {
			if ((System.currentTimeMillis() - file.lastModified()) > mPerCacheFileLimitMaxTime_ms) {
				return true;
			}
		}

		return false;
	}

	/**
	 * 类创建后调用此方法,将文件目录里面的旧文件【过期的】删除，<br/>
	 * 当超过最大限制容量时也删除较旧的，直到目录的文件总量小于或等于规定的最大容量为止<br/>
	 * 该方法异步操作
	 */
	private void asyncCheckStoreFiles() {
		try {
			if (mAllCacheFileLimitMaxSize <= 0 && mPerCacheFileLimitMaxTime_ms <= 0) {
				return;
			}
			Global_Executor.getCachedThreadPool().execute(new CacheFileCleanerRunnable());

		} catch (Throwable e) {
			if (Debug_SDK.isDownloadLog) {
				Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
			}
		}

	}

	class FileLastModifyCom implements Comparator<File> {

		@Override
		public int compare(File lhs, File rhs) {
			try {
				if (lhs.lastModified() < rhs.lastModified()) {
					return -1;
				}
				return 1;
			} catch (Throwable e) {
				if (Debug_SDK.isDownloadLog) {
					Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
				}

			}
			return 0;
		}

	}

	/**
	 * 1、清除过期文件 2、清除旧的文件释放空间
	 * 
	 * @author zhitaocai
	 */
	class CacheFileCleanerRunnable implements Runnable {
		@Override
		public void run() {
			try {
				File rootDir = mCacheDirectory;
				if (!rootDir.exists()) {
					if (!rootDir.mkdirs()) {
						return;
					}
				}
				// 原列表
				File[] files = getFilesInDir();
				if (files == null) {
					return;
				}
				// 所有文件的总长度
				long countLen = 0;
				// 待排序列表
				List<File> fileList = new ArrayList<File>();
				// 添加到待排序列表中
				for (int i = 0; i < files.length; i++) {
					File file = files[i];
					// 先检查有没有超期了
					if (isFileStoreTimeOut(file)) {
						if (Debug_SDK.isDownloadLog) {
							Debug_SDK.td(Debug_SDK.mDownloadTag, this, "文件已经过期%s,准备删除", file.getAbsolutePath());
						}
						// 超期，删了
						file.delete();
						continue;
					}
					// 文件不存在
					if (!file.exists()) {
						continue;
					}
					if (mAllCacheFileLimitMaxSize != UN_LIMT_STORE_SIZE) {
						if (mAllCacheFileLimitMaxSize > 0) {
							// 只有需要检查目录大小的情况下才需要计算文件大小，以及把文件放入待排序列表中
							countLen += file.length();// 文件未超期或未被删除，就用来检查总容量
							fileList.add(file);// 加入到待排序列表中
						}
					}
				}
				// 按lastModify进行，从旧到新
				Collections.sort(fileList, new FileLastModifyCom());// 这里需要添加排序算法
				// 使用链接将文件进行排序，文件比较旧的排在前面，如果超过目录缓存的总大小，删除排在前面的文件。
				Iterator<File> iterator = fileList.iterator();
				if (Debug_SDK.isDownloadLog) {
					Debug_SDK.td(Debug_SDK.mDownloadTag, this, "准备删除旧的但未过时的文件");
				}
				int a = 10000;
				while (countLen > mAllCacheFileLimitMaxSize && iterator.hasNext()) {
					try {
						File gfFile = iterator.next();
						countLen -= gfFile.length();
						iterator.remove();
						if (Debug_SDK.isDownloadLog) {
							Debug_SDK.td(Debug_SDK.mDownloadTag, this, "删除旧的但未过时的文件%s", gfFile.getPath());
						}
						// 删除旧的文件
						gfFile.delete();
					} catch (Throwable e) {
						if (Debug_SDK.isDownloadLog) {
							Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
						}
					}
					--a;
					if (a < 0) {
						break;// 防止死循环
					}
				}
			} catch (Throwable e) {
				if (Debug_SDK.isDownloadLog) {
					Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
				}
			}
		}

	}
}
