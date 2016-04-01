package net.youmi.android.libs.common.download;

import android.content.Context;

import net.youmi.android.libs.common.debug.Debug_SDK;
import net.youmi.android.libs.common.download.filestorer.FileCacheDirectoryStorer;
import net.youmi.android.libs.common.download.listener.FileAvailableChecker;
import net.youmi.android.libs.common.download.listener.FileDownloadListener;
import net.youmi.android.libs.common.download.listener.FileDownloadResultListener;
import net.youmi.android.libs.common.download.model.FileDownloadTask;
import net.youmi.android.libs.common.template.Template_ListenersManager;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;

/**
 * 这个类不知道在哪里用到，广告是没有用到的，而且这个类逻辑好少
 * 
 * @author zhitaocai
 * 
 */
public class BaseRawFileDownloadManager extends Template_ListenersManager<FileDownloadResultListener> implements
		FileDownloadListener, FileAvailableChecker {

	private Context mApplicationContext;

	private FileCacheDirectoryStorer mStorer;

	/**
	 * 用来标识本类所启动的下载任务，如果不是本类所启动的FileDownloadTask，在接受到回调时将不进行任何处理。
	 */
	private HashSet<FileDownloadTask> mFileDownloadTaskSet;

	public BaseRawFileDownloadManager(Context context, FileCacheDirectoryStorer storer) throws IOException,
			IllegalArgumentException {
		if (context == null) {
			throw new IllegalArgumentException("context is null");
		}
		mApplicationContext = context.getApplicationContext();
		if (storer == null) {
			throw new IOException("Cache Directory is null");
		}
		mStorer = storer;
		mFileDownloadTaskSet = new HashSet<FileDownloadTask>();
		FinalFileDownloadManager.getInstance().registerListener(this);
	}

	/**
	 * 下载文件
	 * 
	 * @param url
	 * @return
	 */
	public boolean downloadFile(String url) {
		try {
			FileDownloadTask task = new FileDownloadTask(url);
			if (!task.isAvailable()) {
				return false;
			}
			File storeFile = mStorer.getFileByFileName(task.getIdentity());
			task.setStoreFile(storeFile);

			if (FinalFileDownloadManager.getInstance().downloadFile(mApplicationContext, task, this)) {
				mFileDownloadTaskSet.add(task);
				return true;
			}
		} catch (Throwable e) {
			if (Debug_SDK.isDownloadLog) {
				Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
			}
		}
		return false;
	}

	/**
	 * 从本地中加载文件
	 * 
	 * @param url
	 * @return
	 */
	public File loadFileJustFromCache(String url) {

		try {
			FileDownloadTask task = new FileDownloadTask(url);
			if (!task.isAvailable()) {
				return null;
			}
			File storeFile = mStorer.getFileByFileName(task.getIdentity());
			if (storeFile.exists()) {
				return storeFile;
			}
		} catch (Throwable e) {
			if (Debug_SDK.isDownloadLog) {
				Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
			}
		}
		return null;
	}

	@Override
	public void onFileDownloadBeforeStart_FileLock(FileDownloadTask task) {
	}

	@Override
	public void onFileDownloadStart(FileDownloadTask task) {
	}

	@Override
	public void onFileDownloadSuccess(FileDownloadTask task) {
		try {
			try {
				if (task == null) {
					return;
				}
				if (!mFileDownloadTaskSet.contains(task)) {
					if (Debug_SDK.isDownloadLog) {
						Debug_SDK.tw(Debug_SDK.mDownloadTag, this, "(%s)非本管理器[%s]启动的下载任务，取消处理", task.getRawUrl(), this
								.getClass().getSimpleName());
					}
					return;
				}
			} catch (Throwable e) {
				if (Debug_SDK.isDownloadLog) {
					Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
				}
			}
			List<FileDownloadResultListener> lists = getListeners();
			for (int i = 0; i < lists.size(); i++) {
				try {
					lists.get(i).onFileDownloadSuccess(task);
				} catch (Throwable e) {
					if (Debug_SDK.isDownloadLog) {
						Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
					}
				}
			}
		} catch (Throwable e) {
			if (Debug_SDK.isDownloadLog) {
				Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
			}
		}
	}

	@Override
	public void onFileDownloadFailed(FileDownloadTask task) {
		try {
			try {
				if (task == null) {
					return;
				}
				if (!mFileDownloadTaskSet.contains(task)) {
					if (Debug_SDK.isDownloadLog) {
						Debug_SDK.tw(Debug_SDK.mDownloadTag, this, "(%s)非本管理器[%s]启动的下载任务，取消处理", task.getRawUrl(), this
								.getClass().getSimpleName());
					}
					return;
				}
			} catch (Throwable e) {
				if (Debug_SDK.isDownloadLog) {
					Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
				}
			}
			List<FileDownloadResultListener> lists = getListeners();
			for (int i = 0; i < lists.size(); i++) {
				try {
					lists.get(i).onFileDownloadFailed(task);
				} catch (Throwable e) {
					if (Debug_SDK.isDownloadLog) {
						Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
					}
				}
			}
		} catch (Throwable e) {
			if (Debug_SDK.isDownloadLog) {
				Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
			}
		}
	}

	@Override
	public void onFileDownloadStop(FileDownloadTask task) {
		try {
			try {
				if (task == null) {
					return;
				}
				if (!mFileDownloadTaskSet.contains(task)) {
					if (Debug_SDK.isDownloadLog) {
						Debug_SDK.tw(Debug_SDK.mDownloadTag, this, "(%s)非本管理器[%s]启动的下载任务，取消处理", task.getRawUrl(), this
								.getClass().getSimpleName());
					}
					return;
				}
			} catch (Throwable e) {
				if (Debug_SDK.isDownloadLog) {
					Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
				}
			}
			List<FileDownloadResultListener> lists = getListeners();
			for (int i = 0; i < lists.size(); i++) {
				try {
					lists.get(i).onFileDownloadStop(task);
				} catch (Throwable e) {
					if (Debug_SDK.isDownloadLog) {
						Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
					}
				}
			}
		} catch (Throwable e) {
			if (Debug_SDK.isDownloadLog) {
				Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
			}
		}

	}

	@Override
	public void onFileAlreadyExist(FileDownloadTask task) {
		onFileDownloadSuccess(task);
	}

	@Override
	public void onFileDownloadProgressUpdate(FileDownloadTask task, long contentLength, long completeLength,
			int percent, long speedBytesPerS) {
	}

	@Override
	public boolean checkFileAvailable(FileDownloadTask task) {
		return true;
	}

	@Override
	public boolean isNeedToCheckContentLengthByNetwork(FileDownloadTask task) {
		return false;
	}

}
