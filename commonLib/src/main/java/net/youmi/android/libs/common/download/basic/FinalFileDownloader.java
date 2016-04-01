package net.youmi.android.libs.common.download.basic;

import android.content.Context;

import net.youmi.android.libs.common.debug.Debug_SDK;
import net.youmi.android.libs.common.download.FileDownloadConfig;
import net.youmi.android.libs.common.download.listener.FileAvailableChecker;
import net.youmi.android.libs.common.download.listener.FileDownloadStatePublisher;
import net.youmi.android.libs.common.download.model.FileDownloadState;
import net.youmi.android.libs.common.download.model.FileDownloadTask;
import net.youmi.android.libs.common.download.state.TaskState;
import net.youmi.android.libs.common.download.state.TaskState_Download_Failed;
import net.youmi.android.libs.common.download.state.TaskState_Download_Start;
import net.youmi.android.libs.common.download.state.TaskState_Download_Stop;
import net.youmi.android.libs.common.download.state.TaskState_Download_Success;
import net.youmi.android.libs.common.download.state.TaskState_Downloading;
import net.youmi.android.libs.common.download.state.TaskState_Init;
import net.youmi.android.libs.common.download.state.TaskState_Observe_Others_Downloading;
import net.youmi.android.libs.common.download.state.TaskState_StoreFileAlreadyExist;

import java.io.File;

/**
 * 最终文件下载类，主要进行下载状态的通知
 * <p>
 * 调用到文件下载类{@link BasicFileDownloaderHandler}进行文件下载
 * 
 * @author zhitaocai edit on 2014-5-28
 * @author zhitaocai edit on 2014-7-16
 */
public class FinalFileDownloader implements Runnable {

	/**
	 * 下载任务描述
	 */
	private FileDownloadTask mFileDownloadTask;

	/**
	 * 下载状态通知器
	 */
	private FileDownloadStatePublisher mDownloadStatePublisher;

	/**
	 * 实际文件下载逻辑类
	 */
	private BasicFileDownloaderHandler mBasicFileDownloaderHandler;

	/**
	 * 文件检查接口
	 */
	private FileAvailableChecker mFileAvailableChecker;

	/**
	 * 当前状态
	 */
	private TaskState mCurrentTaskState;

	/**
	 * 初始化状态
	 */
	private TaskState_Init mTaskState_Init;

	/**
	 * 下载之前对temp文件进行检查 对temp文件进行检查，如果可用则启用开始下载状态，否则启用下载观察状态
	 */
	// private TaskState_Init_TempFile_Check mTaskState_CheckTempFile_BeforeDownload;

	/**
	 * 下载之前文件已经存在的状态
	 */
	private TaskState_StoreFileAlreadyExist mTaskState_StoreFileAlreadyExist;

	/**
	 * 创建下载任务的状态
	 */
	private TaskState_Download_Start mTaskState_DownloadTaskCreate;

	/**
	 * 下载中
	 */
	private TaskState_Downloading mTaskState_Downloading;

	/**
	 * 下载完成的状态
	 */
	private TaskState_Download_Success mTaskState_DownloadComplete;

	/**
	 * 下载失败
	 */
	private TaskState_Download_Failed mTaskState_DownloadFailed;

	/**
	 * 下载停止
	 */
	private TaskState_Download_Stop mTaskState_DownloadStop;

	/**
	 * 观察其他进行下载进度的状态
	 */
	private TaskState_Observe_Others_Downloading mTaskState_Observe_Others_Downloading;

	private Context mApplicationContext;

	public FinalFileDownloader(Context context, FileDownloadTask fileDownloadTask, FileAvailableChecker checker,
			FileDownloadStatePublisher publisher) {

		mApplicationContext = context.getApplicationContext();
		mFileDownloadTask = fileDownloadTask;
		checkDirectory(mFileDownloadTask.getStoreFile());
		// 设置缓存文件
		mFileDownloadTask.setTempFile(new File(mFileDownloadTask.getStoreFile().getPath()
				+ FileDownloadConfig.TEMP_FILE_SUFFIX));

		mFileAvailableChecker = checker;
		mDownloadStatePublisher = publisher;

		long start = 0;
		if (fileDownloadTask.getTempFile().length() > 0) {
			start = fileDownloadTask.getTempFile().length();
		}
		mBasicFileDownloaderHandler = new BasicFileDownloaderHandler(mApplicationContext,
				fileDownloadTask.getDestUrl(), start, fileDownloadTask.getTempFile(), fileDownloadTask.getStoreFile());

		if (Debug_SDK.isDownloadLog) {
			Debug_SDK.td(Debug_SDK.mDownloadTag, this, "创建最终下载文件类对象%s成功，下载任务描述:\n%s", this.getClass().getSimpleName(),
					mFileDownloadTask.toString());
		}
	}

	@Override
	public void run() {
		try {
			// 注意：这里不是一开始就下载的，而是进行诸多状态确认之后才进行下载的，具体下载是在startDownload方法中
			changeState(FileDownloadState.STATE_INIT);
		} catch (Throwable e) {
			if (Debug_SDK.isDownloadLog) {
				Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
			}
		}
	}

	/**
	 * 检查暂存下载文件的目录，如果没有就创建目录
	 * 
	 * @param storeFile
	 */
	private void checkDirectory(File storeFile) {
		try {

			if (storeFile == null) {
				return;
			}

			if (storeFile.exists()) {
				// 文件存在的情况，说明目录也存在
				return;
			}

			// 文件不存在，有可能目录也不存在
			File dir = storeFile.getParentFile();
			if (dir != null) {
				if (dir.exists()) {
					return;
				}

				// 创建目录
				dir.mkdirs();
			}

		} catch (Throwable e) {
			if (Debug_SDK.isDownloadLog) {
				Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
			}
		}
	}

	/**
	 * 切换状态，然后执行不同状态该干的事情
	 * 
	 * @param toState
	 */
	public void changeState(int toState) {
		if (mCurrentTaskState != null) {
			if (mCurrentTaskState.equals(toState)) {
				if (Debug_SDK.isDownloadLog) {
					Debug_SDK.td(Debug_SDK.mDownloadTag, this, "要切换的状态和当前想要切换的状态一样，无需切换");
				}
			}
		}

		TaskState newState = null;
		switch (toState) {
		case FileDownloadState.STATE_INIT:
			newState = getState_Init();
			break;
		// case FileDownloadState.STATE_INIT_TEMPFILE_CHECK:
		// newState = getState_CheckTempFileBeforeDownload();
		// break;
		case FileDownloadState.STATE_DOWNLOAD_START:
			newState = getState_CreateDownloadTask();
			break;
		case FileDownloadState.STATE_DOWNLOADING:
			newState = getState_Downloading();
			break;
		case FileDownloadState.STATE_DOWNLOAD_SUCCESS:
			newState = getState_DownloadComplete();
			break;
		case FileDownloadState.STATE_DOWNLOAD_FAILED:
			newState = getState_Failed();
			break;
		case FileDownloadState.STATE_DOWNLOAD_STOP:
			newState = getState_Stop();
			break;
		case FileDownloadState.STATE_ALREADY_EXIST:
			newState = getState_FileAlreadyExistState();
			break;
		case FileDownloadState.STATE_OBSERVER_OTHERS_DOWNLOADING:
			newState = getState_Observe_Others_Downloading();
			break;
		default:
			break;
		}

		if (newState != null && !newState.equals(mCurrentTaskState)) {
			if (Debug_SDK.isDownloadLog) {
				Debug_SDK.td(Debug_SDK.mDownloadTag, this, "切换处理状态:%s --> %s", mCurrentTaskState == null ? null
						: mCurrentTaskState.getClass().getSimpleName(), newState.getClass().getSimpleName());
			}
			mCurrentTaskState = newState;
			mCurrentTaskState.handleState(mApplicationContext, this, mFileDownloadTask, mDownloadStatePublisher);
		}
	}

	/**
	 * 开始下载
	 * 
	 * @return
	 */
	public boolean startDownload() {

		try {
			if (Debug_SDK.isDownloadLog) {
				Debug_SDK.td(Debug_SDK.mDownloadTag, this, "检查BasicFileDownloaderHandler：");
			}
			if (mBasicFileDownloaderHandler != null) {
				if (Debug_SDK.isDownloadLog) {
					Debug_SDK.td(Debug_SDK.mDownloadTag, this, "不为null");
				}
				// 这里就不需要判断你是不是在运行了，因为执行线程的时候会判断的
				// if (mBasicFileDownloaderHandler.isRunning()) {
				// if (Debug_SDK.isDownloadLog) {
				// Debug_SDK.td(Debug_SDK.mDownloadTag, this, "正在运行");
				// }
				// } else {
				if (Debug_SDK.isDownloadLog) {
					Debug_SDK.td(Debug_SDK.mDownloadTag, this, "不在外面判断是不是在运行中，直接启动");
				}
				BasicFileDownloaderExecutor.execute(mBasicFileDownloaderHandler);
				// }
			} else {
				if (Debug_SDK.isDownloadLog) {
					Debug_SDK.td(Debug_SDK.mDownloadTag, this, "为null\n准备创建新的mBasicFileDownloaderHandler");
				}
				long start = 0;
				if (mFileDownloadTask.getTempFile().length() > 0) {
					start = mFileDownloadTask.getTempFile().length();
				}
				mBasicFileDownloaderHandler = new BasicFileDownloaderHandler(mApplicationContext,
						mFileDownloadTask.getDestUrl(), start, mFileDownloadTask.getTempFile(),
						mFileDownloadTask.getStoreFile());
				if (Debug_SDK.isDownloadLog) {
					Debug_SDK.td(Debug_SDK.mDownloadTag, this, "成功创建新的mBasicFileDownloaderHandler");
				}
				BasicFileDownloaderExecutor.execute(mBasicFileDownloaderHandler);
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
	 * 停止下载 1、停止实际的文件下载线程 2、断开网络链接 3、通知下载任务变化
	 */
	public void stopDownload() {
		try {
			// 停止下载线程
			if (mBasicFileDownloaderHandler != null) {
				if (Debug_SDK.isDownloadLog) {
					Debug_SDK.tw(Debug_SDK.mDownloadTag, this, "BasicFileDownloaderHandler != null 准备调用停止");
				}
				mBasicFileDownloaderHandler.stopDownload();
			} else {
				if (Debug_SDK.isDownloadLog) {
					Debug_SDK.tw(Debug_SDK.mDownloadTag, this, "BasicFileDownloaderHandler == null");
				}
			}
		} catch (Throwable e) {
			if (Debug_SDK.isDownloadLog) {
				Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
			}
		}
	}

	private synchronized TaskState getState_Init() {
		if (mTaskState_Init == null) {
			mTaskState_Init = new TaskState_Init();
		}
		return mTaskState_Init;
	}

	// public synchronized TaskState getState_CheckTempFileBeforeDownload() {
	// if (mTaskState_CheckTempFile_BeforeDownload == null) {
	// mTaskState_CheckTempFile_BeforeDownload = new TaskState_Init_TempFile_Check();
	// }
	// return mTaskState_CheckTempFile_BeforeDownload;
	// }

	private synchronized TaskState getState_CreateDownloadTask() {
		if (mTaskState_DownloadTaskCreate == null) {
			mTaskState_DownloadTaskCreate = new TaskState_Download_Start();
		}
		return mTaskState_DownloadTaskCreate;
	}

	private synchronized TaskState getState_Downloading() {
		if (mTaskState_Downloading == null) {
			mTaskState_Downloading = new TaskState_Downloading();
		}
		return mTaskState_Downloading;
	}

	private synchronized TaskState getState_DownloadComplete() {
		if (mTaskState_DownloadComplete == null) {
			mTaskState_DownloadComplete = new TaskState_Download_Success();
		}
		return mTaskState_DownloadComplete;
	}

	private synchronized TaskState getState_Failed() {
		if (mTaskState_DownloadFailed == null) {
			mTaskState_DownloadFailed = new TaskState_Download_Failed();
		}
		return mTaskState_DownloadFailed;
	}

	private synchronized TaskState getState_Stop() {
		if (mTaskState_DownloadStop == null) {
			mTaskState_DownloadStop = new TaskState_Download_Stop();
		}
		return mTaskState_DownloadStop;
	}

	private synchronized TaskState getState_FileAlreadyExistState() {
		if (mTaskState_StoreFileAlreadyExist == null) {
			mTaskState_StoreFileAlreadyExist = new TaskState_StoreFileAlreadyExist();
		}
		return mTaskState_StoreFileAlreadyExist;
	}

	private synchronized TaskState getState_Observe_Others_Downloading() {
		if (mTaskState_Observe_Others_Downloading == null) {
			mTaskState_Observe_Others_Downloading = new TaskState_Observe_Others_Downloading();
		}
		return mTaskState_Observe_Others_Downloading;
	}

	@Override
	public int hashCode() {
		if (mFileDownloadTask != null) {
			return mFileDownloadTask.hashCode();
		}
		return super.hashCode();
	}

	/**
	 * 判断其他进程是否存在使用暂存文件(进行下载)
	 * 
	 * @return
	 */
	public boolean isTempFileUsingByOtherProgress() {
		try {
			if (getTempFile() == null) {
				return false;
			}
			// 如果存在文件
			if (getTempFile().exists()) {
				// 则检查当前时间和最后一次编辑时间的差值
				long currentTime = System.currentTimeMillis();
				long lastEditTime = getTempFile().lastModified();
				long intervalTime = currentTime - lastEditTime;
				if (intervalTime < FileDownloadConfig.TEMP_FILE_LOCK_TIME_OUT_MS) {
					// 判定为其他进程正在占用
					return true;
				} else {
					// 已超过限定值，判定为未被占用。
					return false;
				}
			}
		} catch (Throwable e) {
			if (Debug_SDK.isDownloadLog) {
				Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
			}
		}
		return false;
	}

	/**
	 * 获取最终存储下载文件
	 * 
	 * @return
	 */
	public File getStoreFile() {
		return mFileDownloadTask.getStoreFile();
	}

	/**
	 * 获取下载暂存文件
	 * 
	 * @return
	 */
	public File getTempFile() {
		return mFileDownloadTask.getTempFile();
	}

	/**
	 * 获取下载任务描述
	 * 
	 * @return
	 */
	public FileDownloadTask getDownloadTask() {
		return mFileDownloadTask;
	}

	/**
	 * 获取文件检查接口对象
	 * 
	 * @return
	 */
	public FileAvailableChecker getFileAvailableChecker() {
		return mFileAvailableChecker;
	}

	/**
	 * 获取文件下载逻辑类对象
	 * 
	 * @return
	 */
	public BasicFileDownloaderHandler getDownloadHandler() {
		return mBasicFileDownloaderHandler;
	}

}