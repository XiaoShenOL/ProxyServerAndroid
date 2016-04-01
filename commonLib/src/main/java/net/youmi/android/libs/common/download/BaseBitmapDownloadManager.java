package net.youmi.android.libs.common.download;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import net.youmi.android.libs.common.debug.Debug_SDK;
import net.youmi.android.libs.common.download.filestorer.FileCacheDirectoryStorer;
import net.youmi.android.libs.common.download.listener.FileAvailableChecker;
import net.youmi.android.libs.common.download.listener.FileDownloadListener;
import net.youmi.android.libs.common.download.listener.ImageDownloadListener;
import net.youmi.android.libs.common.download.model.FileDownloadTask;
import net.youmi.android.libs.common.template.Template_ListenersManager;

import java.io.File;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * 图片下载管理基础类<br/>
 * <i>该类将持有一个图片内存软索引，当要下载的Bitmap存在于内存软索引中时，直接返回给调用者。</i><br/>
 * 否则将启动下载任务进行下载。<br/>
 * <strong>注意:请对该类保持最少的实例化引用，可以对大图片及icon分别进行一个单例实例化。</strong>
 * 
 * @author jen
 * @author zhitaocai edit on 2014-7-16
 * 
 */
public abstract class BaseBitmapDownloadManager extends Template_ListenersManager<ImageDownloadListener> implements
		FileDownloadListener, FileAvailableChecker {

	/**
	 * 本类中的log是否开启，因为图片下载比较多，所以在此在设置一个开关
	 */
	private final static boolean isLogOpen = true;

	private Context mApplicationContext;

	private FileCacheDirectoryStorer mStorer;

	private HashMap<FileDownloadTask, SoftReference<Bitmap>> mCacheTableBitmaps;

	/**
	 * 用来标识本类所启动的下载任务，如果不是本类所启动的FileDownloadTask，在接受到回调时将不进行任何处理。
	 */
	private HashSet<FileDownloadTask> mFileDownloadTaskSet;

	public BaseBitmapDownloadManager(Context context, FileCacheDirectoryStorer storer) throws IOException {

		if (context == null) {
			throw new IllegalArgumentException("context is null");
		}
		mApplicationContext = context.getApplicationContext();
		if (storer == null) {
			throw new IOException("Cache Directory is null");
		}
		mStorer = storer;
		mCacheTableBitmaps = new HashMap<FileDownloadTask, SoftReference<Bitmap>>();
		mFileDownloadTaskSet = new HashSet<FileDownloadTask>();
		// 在初始化的时候，必须注册文件下载管理器的监听
		FinalFileDownloadManager.getInstance().registerListener(this);
	}

	/**
	 * 加载图片，如果内存缓存中存在，则直接返回Bitmap，否则从本地文件或网络url获取，请注册监听
	 * 
	 * @param context
	 * @param rawUrl
	 * @return
	 */
	public Bitmap loadBitmap(String rawUrl) {
		try {
			FileDownloadTask task = new FileDownloadTask(rawUrl);
			if (!task.isAvailable()) {
				return null;
			}

			// 从内存中获取图片
			Bitmap bm = getBitmapFromSoftCacheMap(task);
			if (bm != null) {
				return bm;
			}
			// 从网络中获取图片，如果本地文件存在的话就回直接返回，否则启动网络
			loadBitmapFromNet(task);
		} catch (Throwable e) {
			if (Debug_SDK.isDownloadLog && isLogOpen) {
				Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
			}
		}
		return null;
	}

	// /**
	// * 从缓存加载图片，如果内存cache及本地文件存在图片，则直接返回bitmap<br/>
	// * 此操作遇到大文件Bitmap，在一些极品手机极有可能卡住UI线程，请务必注意
	// *
	// * @param context
	// * @param url
	// * @return
	// */
	// public Bitmap loadBitmapJustFromCache(String url) {
	// Bitmap bm = null;
	// try {
	// FileDownloadTask task = new FileDownloadTask(url);
	// if (!task.isAvailable()) {
	// return null;
	// }
	// // 从缓存中获取图片
	// bm = loadBitmapFromCache(task);
	// if (bm != null) {
	// return bm;
	// }
	// // 从文件中获取图片
	// bm = loadBitmapFromFile(task);
	// if (bm != null) {
	// return bm;
	// }
	// } catch (Throwable e) {
	// if (Debug_SDK.isDownloadLog && isLogOpen) {
	// Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
	// }
	// }
	// return bm;
	// }

	/**
	 * 从内存缓存中获取Bitmap
	 * 
	 * @param task
	 * @return
	 */
	protected Bitmap getBitmapFromSoftCacheMap(FileDownloadTask task) {
		try {
			if (mCacheTableBitmaps.containsKey(task)) {
				SoftReference<Bitmap> srb = mCacheTableBitmaps.get(task);
				if (srb != null) {
					Bitmap bm = srb.get();
					if (bm != null && (!bm.isRecycled())) {
						if (Debug_SDK.isDownloadLog && isLogOpen) {
							Debug_SDK.td(Debug_SDK.mDownloadTag, this, "%s\n图片存在于内存缓存中，可用!", task.getRawUrl());
						}
						// 图片已经存在
						return bm;
					}
				}
				if (Debug_SDK.isDownloadLog && isLogOpen) {
					Debug_SDK.td(Debug_SDK.mDownloadTag, this, "%s\n图片存在于内存缓存中，但已被系统recycled，删除引用!", task.getRawUrl());
				}
				// 到了这里说明缓存的图片有问题，直接删除之
				mCacheTableBitmaps.remove(task);
			}
		} catch (Throwable e) {
			if (Debug_SDK.isDownloadLog && isLogOpen) {
				Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
			}
		}
		return null;
	}

	/**
	 * 从本地文件中解析出图片
	 * 
	 * @param task
	 * @return
	 */
	protected Bitmap getBitmapFromFile(FileDownloadTask task) {
		Bitmap bm = null;
		try {
			File storeFile = mStorer.getFileByFileName(task.getIdentity());
			if (storeFile.exists()) {
				// 解码Bitmap
				bm = decodeBitmapFromFile(storeFile);
				if (bm != null) {
					if (Debug_SDK.isDownloadLog && isLogOpen) {
						Debug_SDK.td(Debug_SDK.mDownloadTag, this, "%s\n图片存在于文件中，可用!", task.getRawUrl());
					}
					// 存储到Bitmap中
					putBitmapToSoftCacheMap(task, bm); // 这里的结果并不十分关注
				}
			}
		} catch (Throwable e) {
			if (Debug_SDK.isDownloadLog && isLogOpen) {
				Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
			}
		}
		return bm;
	}

	/**
	 * 从网络中解析出图片,需要注册监听
	 * 
	 * @param task
	 * @return
	 */
	protected void loadBitmapFromNet(FileDownloadTask task) {
		try {
			File storeFile = mStorer.getFileByFileName(task.getIdentity());
			task.setStoreFile(storeFile);
			mFileDownloadTaskSet.add(task);
			if (!FinalFileDownloadManager.getInstance().downloadFile(mApplicationContext, task, this)) {
				mFileDownloadTaskSet.remove(task);
			}
		} catch (Throwable e) {
			if (Debug_SDK.isDownloadLog && isLogOpen) {
				Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
			}
		}
	}

	/**
	 * 从文件中解码Bitmap
	 * 
	 * @param file
	 * @return
	 */
	protected Bitmap decodeBitmapFromFile(File file) {
		try {
			if (file == null) {
				return null;
			}
			if (!file.exists()) {
				return null;
			}
			Bitmap bm = BitmapFactory.decodeFile(file.getAbsolutePath());
			if (bm != null) {
				if (!bm.isRecycled()) {
					return bm;
				}
			}
		} catch (Throwable e) {
			if (Debug_SDK.isDownloadLog && isLogOpen) {
				Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
			}
		}
		return null;
	}

	/**
	 * 将Bitmap放入缓存表中
	 * 
	 * @param task
	 * @param bm
	 * @return
	 */
	protected boolean putBitmapToSoftCacheMap(FileDownloadTask task, Bitmap bm) {
		try {

			Bitmap bmExist = getBitmapFromSoftCacheMap(task);// 从缓存表中获取可用的图片
			boolean isBmExistAvaliable = (bmExist != null);

			// 将要设置的图片不可用的情况下，结果会以已经缓存的图片为准
			if (bm == null) {
				return isBmExistAvaliable;
			}

			if (bm.isRecycled()) {
				return isBmExistAvaliable;
			}

			// 图片可用，设置图片
			return (mCacheTableBitmaps.put(task, new SoftReference<Bitmap>(bm)) != null);

		} catch (Throwable e) {
			if (Debug_SDK.isDownloadLog && isLogOpen) {
				Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
			}
		}
		return false;
	}

	@Override
	public final void onFileDownloadBeforeStart_FileLock(FileDownloadTask task) {
		// 这里是不需要处理的
	}
	
	
	@Override
	public final void onFileDownloadStart(FileDownloadTask task) {
		// 这里是不需要处理的
	}

	@Override
	public final void onFileDownloadSuccess(FileDownloadTask task) {
		try {
			try {
				if (task == null) {
					return;
				}
				if (!mFileDownloadTaskSet.contains(task)) {
					return;
				}
			} catch (Throwable e) {
				if (Debug_SDK.isDownloadLog && isLogOpen) {
					Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
				}
			}
			// 解压图片，并且进行保存
			Bitmap bm = getBitmapFromSoftCacheMap(task);
			if (bm == null) {
				try {
					bm = decodeBitmapFromFile(task.getStoreFile());// 从文件中加载
					putBitmapToSoftCacheMap(task, bm);// 将图片放入缓存
				} catch (Throwable e) {
					if (Debug_SDK.isDownloadLog && isLogOpen) {
						Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
					}
				}
			}
			if (bm == null) {
				return;
			}
			List<ImageDownloadListener> lists = getListeners();
			for (int i = 0; i < lists.size(); i++) {
				try {
					lists.get(i).onImageDownloadSuccess(task.getDestUrl(), bm);
				} catch (Throwable e) {
					if (Debug_SDK.isDownloadLog && isLogOpen) {
						Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
					}
				}
			}
		} catch (Throwable e) {
			if (Debug_SDK.isDownloadLog && isLogOpen) {
				Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
			}
		}
	}

	@Override
	public final void onFileDownloadFailed(FileDownloadTask task) {
		try {
			try {
				if (task == null) {
					return;
				}
				if (!mFileDownloadTaskSet.contains(task)) {
					return;
				}
			} catch (Throwable e) {
				if (Debug_SDK.isDownloadLog && isLogOpen) {
					Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
				}
			}
			List<ImageDownloadListener> lists = getListeners();
			for (int i = 0; i < lists.size(); i++) {
				try {
					lists.get(i).onImageDownloadFailed(task.getDestUrl());
				} catch (Throwable e) {
					if (Debug_SDK.isDownloadLog && isLogOpen) {
						Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
					}
				}
			}
		} catch (Throwable e) {
			if (Debug_SDK.isDownloadLog && isLogOpen) {
				Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
			}
		}
	}

	@Override
	public final void onFileDownloadStop(FileDownloadTask task) {
		try {
			try {
				if (task == null) {
					return;
				}
				if (!mFileDownloadTaskSet.contains(task)) {
					return;
				}
			} catch (Throwable e) {
				if (Debug_SDK.isDownloadLog && isLogOpen) {
					Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
				}
			}
			List<ImageDownloadListener> lists = getListeners();
			for (int i = 0; i < lists.size(); i++) {
				try {
					lists.get(i).onImageDownloadStop(task.getDestUrl());
				} catch (Throwable e) {
					if (Debug_SDK.isDownloadLog && isLogOpen) {
						Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
					}
				}
			}
		} catch (Throwable e) {
			if (Debug_SDK.isDownloadLog && isLogOpen) {
				Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
			}
		}
	}

	@Override
	public final void onFileDownloadProgressUpdate(FileDownloadTask task, long contentLength, long completeLength,
			int percent, long speedBytesPerS) {
		// 这里是不需要处理进度的
	}

	@Override
	public void onFileAlreadyExist(FileDownloadTask task) {
		if (Debug_SDK.isDownloadLog && isLogOpen) {
			Debug_SDK.td(Debug_SDK.mDownloadTag, this, "图片已存在于文件缓存中:%s", task.getRawUrl());
		}
		onFileDownloadSuccess(task);
	}

	/**
	 * 对已经存在的文件进行检查，直接将文件解码为Bitmap并且作软索引。
	 */
	@Override
	public boolean checkFileAvailable(FileDownloadTask task) {
		boolean result = false;
		try {
			// 解码Bitmap
			Bitmap bm = decodeBitmapFromFile(task.getStoreFile());
			if (bm != null) {
				result = true;// 说明图片已经可用了
			}
			// 存储到软索引中
			putBitmapToSoftCacheMap(task, bm); // 这里的结果并不十分关注
		} catch (Throwable e) {
			if (Debug_SDK.isDownloadLog && isLogOpen) {
				Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
			}
		}
		return result;
	}

	/**
	 * 是否需要检查文件长度，这里是不需要的
	 */
	@Override
	public boolean isNeedToCheckContentLengthByNetwork(FileDownloadTask task) {
		return false;
	}

}
