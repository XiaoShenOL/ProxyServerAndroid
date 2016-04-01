package net.youmi.android.libs.common.download;

import android.content.Context;

import net.youmi.android.libs.common.debug.Debug_SDK;
import net.youmi.android.libs.common.download.basic.BasicFileDownloaderExecutor;
import net.youmi.android.libs.common.download.basic.FinalFileDownloader;
import net.youmi.android.libs.common.download.listener.FileAvailableChecker;
import net.youmi.android.libs.common.download.listener.FileDownloadListener;
import net.youmi.android.libs.common.download.listener.FileDownloadStatePublisher;
import net.youmi.android.libs.common.download.model.FileDownloadTask;
import net.youmi.android.libs.common.template.Template_ListenersManager;

import java.io.File;
import java.util.HashMap;
import java.util.List;

/**
 * 文件下载调用入口
 * 
 * @author zhitaocai edit on 2014-5-28
 * @author zhitaocai edit on 2014-7-16
 * 
 */
class FinalFileDownloadManager extends Template_ListenersManager<FileDownloadListener> implements
		FileDownloadStatePublisher {

	private final static boolean isLogOpen = false;
	private static FinalFileDownloadManager mInstance;

	private static int sObjectID = 0;
	private int mObjectID = 0;

	/**
	 * key : 下载任务的原始url <br>
	 * value ： 下载线程
	 */
	private HashMap<String, FinalFileDownloader> mTasksMap = new HashMap<String, FinalFileDownloader>();

	private FinalFileDownloadManager() {
		++sObjectID;
		mObjectID = sObjectID;
	}

	public static synchronized FinalFileDownloadManager getInstance() {
		try {
			if (mInstance == null) {
				mInstance = new FinalFileDownloadManager();
			}
		} catch (Throwable e) {
			if (Debug_SDK.isDownloadLog && isLogOpen) {
				Debug_SDK.te(Debug_SDK.mDownloadTag, FinalFileDownloadManager.class, e);
			}
		}
		return mInstance;
	}

	/**
	 * 下载指定task的文件<br/>
	 * 
	 * @param context
	 * @param task
	 *            下载任务描述(不支持meta跳转)
	 * @param checker
	 *            文件判断器，用于判断文件是否有效。
	 * @return 如下载正常开始，返回true。否则返回false。
	 */
	public boolean downloadFile(Context context, FileDownloadTask task, FileAvailableChecker checker) {
		try {
			if (task == null) {
				return false;
			}
			if (!task.isAvailable()) {
				return false;
			}
			if (Debug_SDK.isDownloadLog && isLogOpen) {
				Debug_SDK.td(Debug_SDK.mDownloadTag, this, "##将要进行下载的任务:\n%s ", task.toString());
				Debug_SDK.td(Debug_SDK.mDownloadTag, this, "##当前正在下载任务总个数:%d ", mTasksMap.size());
				Debug_SDK.td(Debug_SDK.mDownloadTag, this, "##FinalFileDownloadManager.ID:%d", mObjectID);
			}
			if (mTasksMap.containsKey(task.getRawUrl())) {
				if (Debug_SDK.isDownloadLog && isLogOpen) {
					Debug_SDK.td(Debug_SDK.mDownloadTag, this, "##本次的task有对应的线程在进行中,返回true");
				}
				return true;
			}
			if (Debug_SDK.isDownloadLog && isLogOpen) {
				Debug_SDK.td(Debug_SDK.mDownloadTag, this, "##本次的task没有对应的线程在进行中,准备创建新的下载任务线程");
			}
			final FinalFileDownloader downloader = new FinalFileDownloader(context, task, checker, this);
			mTasksMap.put(task.getRawUrl(), downloader);
			if (Debug_SDK.isDownloadLog && isLogOpen) {
				Debug_SDK.td(Debug_SDK.mDownloadTag, this, "创建新的下载线程成功");
			}
			// 提交任务到线程池，开启异步下载和监听进度等操作
			BasicFileDownloaderExecutor.execute(downloader);
			return true;
		} catch (Throwable e) {
			if (Debug_SDK.isDownloadLog && isLogOpen) {
				Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
			}
		}
		return false;

	}

	/**
	 * 停止下载任务
	 * 
	 * 
	 * @param context
	 * @param rawUrl
	 * @return
	 */
	public boolean stopDownloadByUrl(Context context, String rawUrl) {
		try {
			if (mTasksMap.containsKey(rawUrl)) {
				if (Debug_SDK.isDownloadLog && isLogOpen) {
					Debug_SDK.td(Debug_SDK.mDownloadTag, this, "当前下载管理器BaseFileDownloadManager的ID:%d 准备停止下载:\n%s",
							mObjectID, rawUrl);
				}
				FinalFileDownloader downloader = mTasksMap.get(rawUrl);
				downloader.stopDownload();
				mTasksMap.remove(rawUrl);
				return true;
			}
		} catch (Throwable e) {
			if (Debug_SDK.isDownloadLog && isLogOpen) {
				Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
			}
		}
		return false;

	}

	@Override
	public void publishFileDownloadStart(FileDownloadTask task) {
		try {
			if (Debug_SDK.isDownloadLog && isLogOpen) {
				Debug_SDK.td(Debug_SDK.mDownloadTag, this, "通知：下载开始");
			}
			final List<FileDownloadListener> list = getListeners();
			if (list != null) {
				if (!list.isEmpty()) {
					if (Debug_SDK.isDownloadLog && isLogOpen) {
						Debug_SDK.td(Debug_SDK.mDownloadTag, this, "当前共有%d个监听者要处理", list.size());
					}
					for (int i = 0, size = list.size(); i < size; i++) {
						try {
							if (Debug_SDK.isDownloadLog && isLogOpen) {
								Debug_SDK.ti(Debug_SDK.mDownloadTag, this, "处理第%d个监听者%s", i + 1, list.get(i).getClass()
										.getName());
							}
							list.get(i).onFileDownloadStart(task);
							if (Debug_SDK.isDownloadLog && isLogOpen) {
								Debug_SDK.ti(Debug_SDK.mDownloadTag, this, "第%d个监听者%s处理完毕", i + 1, list.get(i)
										.getClass().getName());
							}
						} catch (Throwable e) {
							Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
						}
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
	public void publishFileDownloadSuccess(FileDownloadTask task) {
		try {
			if (Debug_SDK.isDownloadLog && isLogOpen) {
				Debug_SDK.td(Debug_SDK.mDownloadTag, this, "通知：下载成功");
			}
			File file = task.getStoreFile();
			if (Debug_SDK.isDownloadLog && isLogOpen) {
				Debug_SDK.td(Debug_SDK.mDownloadTag, this, "文件路径：%s", file.getAbsolutePath());
			}
			final List<FileDownloadListener> list = getListeners();
			if (list != null) {
				if (!list.isEmpty()) {
					if (Debug_SDK.isDownloadLog && isLogOpen) {
						Debug_SDK.td(Debug_SDK.mDownloadTag, this, "当前共有%d个监听者要处理", list.size());
					}
					for (int i = 0, size = list.size(); i < size; i++) {
						try {
							if (Debug_SDK.isDownloadLog && isLogOpen) {
								Debug_SDK.ti(Debug_SDK.mDownloadTag, this, "处理第%d个监听者%s", i + 1, list.get(i).getClass()
										.getName());
							}
							list.get(i).onFileDownloadSuccess(task);
							if (Debug_SDK.isDownloadLog && isLogOpen) {
								Debug_SDK.ti(Debug_SDK.mDownloadTag, this, "第%d个监听者%s处理完毕", i + 1, list.get(i)
										.getClass().getName());
							}
						} catch (Throwable e) {
							Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
						}
					}
				}
			}
			// 任务结束 清除任务
			mTasksMap.remove(task.getRawUrl());
		} catch (Throwable e) {
			if (Debug_SDK.isDownloadLog && isLogOpen) {
				Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
			}
		}

	}

	@Override
	public void publishFileDownloadFailed(FileDownloadTask task) {
		try {
			if (Debug_SDK.isDownloadLog && isLogOpen) {
				Debug_SDK.td(Debug_SDK.mDownloadTag, this, "通知：下载失败");
			}
			final List<FileDownloadListener> list = getListeners();
			if (list != null) {
				if (!list.isEmpty()) {
					if (Debug_SDK.isDownloadLog && isLogOpen) {
						Debug_SDK.td(Debug_SDK.mDownloadTag, this, "当前共有%d个监听者要处理", list.size());
					}
					for (int i = 0, size = list.size(); i < size; i++) {
						try {
							if (Debug_SDK.isDownloadLog && isLogOpen) {
								Debug_SDK.ti(Debug_SDK.mDownloadTag, this, "处理第%d个监听者%s", i + 1, list.get(i).getClass()
										.getName());
							}
							list.get(i).onFileDownloadFailed(task);
							if (Debug_SDK.isDownloadLog && isLogOpen) {
								Debug_SDK.ti(Debug_SDK.mDownloadTag, this, "第%d个监听者%s处理完毕", i + 1, list.get(i)
										.getClass().getName());
							}
						} catch (Throwable e) {
							Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
						}
					}
				}
			}
			// 任务结束 清除任务
			mTasksMap.remove(task.getRawUrl());
		} catch (Throwable e) {
			if (Debug_SDK.isDownloadLog && isLogOpen) {
				Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
			}
		}
	}

	@Override
	public void publishFileExistAvailable(FileDownloadTask task) {
		try {
			if (Debug_SDK.isDownloadLog && isLogOpen) {
				Debug_SDK.td(Debug_SDK.mDownloadTag, this, "通知：文件已经存在");
			}
			File file = task.getStoreFile();
			if (Debug_SDK.isDownloadLog && isLogOpen) {
				Debug_SDK.td(Debug_SDK.mDownloadTag, this, "文件路径：%s", file.getAbsolutePath());
			}
			final List<FileDownloadListener> list = getListeners();
			if (list != null) {
				if (!list.isEmpty()) {
					if (Debug_SDK.isDownloadLog && isLogOpen) {
						Debug_SDK.td(Debug_SDK.mDownloadTag, this, "当前共有%d个监听者要处理", list.size());
					}
					for (int i = 0, size = list.size(); i < size; i++) {
						try {
							if (Debug_SDK.isDownloadLog && isLogOpen) {
								Debug_SDK.ti(Debug_SDK.mDownloadTag, this, "处理第%d个监听者%s", i + 1, list.get(i).getClass()
										.getName());
							}
							list.get(i).onFileAlreadyExist(task);
							if (Debug_SDK.isDownloadLog && isLogOpen) {
								Debug_SDK.ti(Debug_SDK.mDownloadTag, this, "第%d个监听者%s处理完毕", i + 1, list.get(i)
										.getClass().getName());

							}
						} catch (Throwable e) {
							Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
						}
					}
				}
			}
			// 任务结束 清除任务
			mTasksMap.remove(task.getRawUrl());
		} catch (Throwable e) {
			if (Debug_SDK.isDownloadLog && isLogOpen) {
				Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
			}
		}
	}

	@Override
	public void publishFileDownloadProgress(FileDownloadTask task, int percent, long speedBytesPerSecond,
			long totalLength, long completeLength) {
		try {
			if (Debug_SDK.isDownloadLog && isLogOpen) {
				Debug_SDK.td(Debug_SDK.mDownloadTag, this, "通知：下载进度百分比%d, 任务url:%s", percent, task.getRawUrl());
			}
			final List<FileDownloadListener> list = getListeners();

			if (list != null) {
				if (!list.isEmpty()) {
					for (int i = 0, size = list.size(); i < size; i++) {
						try {
							list.get(i).onFileDownloadProgressUpdate(task, totalLength, completeLength, percent,
									speedBytesPerSecond);
						} catch (Throwable e) {
							Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
						}
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
	public void publishFileDownloadStop(FileDownloadTask task) {
		try {
			if (Debug_SDK.isDownloadLog && isLogOpen) {
				Debug_SDK.td(Debug_SDK.mDownloadTag, this, "通知：下载被停止");
			}
			final List<FileDownloadListener> list = getListeners();
			if (list != null) {
				if (!list.isEmpty()) {
					if (Debug_SDK.isDownloadLog && isLogOpen) {
						Debug_SDK.td(Debug_SDK.mDownloadTag, this, "当前共有%d个监听者要处理", list.size());
					}
					for (int i = 0, size = list.size(); i < size; i++) {
						try {
							if (Debug_SDK.isDownloadLog && isLogOpen) {
								Debug_SDK.ti(Debug_SDK.mDownloadTag, this, "处理第%d个监听者%s", i + 1, list.get(i).getClass()
										.getName());
							}
							list.get(i).onFileDownloadStop(task);
							if (Debug_SDK.isDownloadLog && isLogOpen) {
								Debug_SDK.ti(Debug_SDK.mDownloadTag, this, "第%d个监听者%s处理完毕", i + 1, list.get(i)
										.getClass().getName());
							}
						} catch (Throwable e) {
							Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
						}
					}
				}
			}
			// 任务结束 清除任务
			mTasksMap.remove(task.getRawUrl());
		} catch (Throwable e) {
			if (Debug_SDK.isDownloadLog && isLogOpen) {
				Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
			}
		}
	}

	@Override
	public void publishFileDownloadBeforeStart_FileLock(FileDownloadTask task) {
		try {
			if (Debug_SDK.isDownloadLog && isLogOpen) {
				Debug_SDK.td(Debug_SDK.mDownloadTag, this, "通知：下载文件还处于文件锁中");
			}
			final List<FileDownloadListener> list = getListeners();
			if (list != null) {
				if (!list.isEmpty()) {
					if (Debug_SDK.isDownloadLog && isLogOpen) {
						Debug_SDK.td(Debug_SDK.mDownloadTag, this, "当前共有%d个监听者要处理", list.size());
					}
					for (int i = 0, size = list.size(); i < size; i++) {
						try {
							if (Debug_SDK.isDownloadLog && isLogOpen) {
								Debug_SDK.ti(Debug_SDK.mDownloadTag, this, "处理第%d个监听者%s", i + 1, list.get(i).getClass()
										.getName());
							}
							list.get(i).onFileDownloadBeforeStart_FileLock(task);
							if (Debug_SDK.isDownloadLog && isLogOpen) {
								Debug_SDK.ti(Debug_SDK.mDownloadTag, this, "第%d个监听者%s处理完毕", i + 1, list.get(i)
										.getClass().getName());
							}
						} catch (Throwable e) {
							Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
						}
					}
				}
			}
		} catch (Throwable e) {
			if (Debug_SDK.isDownloadLog && isLogOpen) {
				Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
			}
		}
	}
}
