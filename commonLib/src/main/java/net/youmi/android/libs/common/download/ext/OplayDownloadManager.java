package net.youmi.android.libs.common.download.ext;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import net.youmi.android.libs.common.debug.AppDebugConfig;
import net.youmi.android.libs.common.debug.Debug_SDK;
import net.youmi.android.libs.common.download.BaseApkDownloadManager;
import net.youmi.android.libs.common.download.filenamecreator.DownloadFileNameListener;
import net.youmi.android.libs.common.download.filestorer.FileCacheDirectoryStorer;
import net.youmi.android.libs.common.download.listener.ApkDownloadListener;
import net.youmi.android.libs.common.download.model.FileDownloadTask;
import net.youmi.android.libs.common.util.Util_System_Runtime;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 本类是下载管理的核心类，本类可供调用的 方法有
 * (只能通过本类对数据库db_download进行的操作）
 * ****************************************
 * <p/>
 * 1.App状态控制方法
 * getDownloadTask(SimpleAppInfo s)
 * restartDownloadTask(SimpleAppInfo s)
 * stopDownloadTask(SimpleAppInfo s)
 * removeDownloadTask(SimpleAppInfo s)
 * stopAll()
 * deleteDownloadInfo(SimpleAppInfo s)
 * ****************************************
 * <p/>
 * 2.get类信息的方法
 * getAppDownloadStatus(String url)
 * getDownloadList();
 * getEndOfDownloading();
 * getEndOfPaused();
 * getEndOfFinished();
 * ****************************************
 * <p/>
 * 3.监听器的添加/删除方法
 * addDownloadStatusChangListener()
 * removeDownloadStatusChangListener()
 * addProgressUpdateListener()
 * removeProgressUpdateListener()
 * ****************************************
 * oplayDownload(String url) 方法是之后重写了基类的方法，原因是webview只能在UI线程运行
 * <p/>
 * Created by yxf on 14-9-22.
 */

public final class OplayDownloadManager extends BaseApkDownloadManager implements
		OplayInstallNotifier.OnInstallListener {

	private static final int MAX_DOWNLOADING_COUNT = 3;
	private final static boolean DEBUG = false;
	public static OplayDownloadManager mInstance = null;

	private static DBHelper_Download dbHelper_download = null;

	//进度条满进度
	private final int DOWNLOADFINISHED_PERCENT = 100;
	//进度条空进度
	private final int DOWNLOADSTART_PERCENT = 0;

	//url,packageName到下载任务的映射
	private Map<String, SimpleAppInfo> mUrl_SimpleAppInfo;
	private Map<String, SimpleAppInfo> mPackageName_SimpleAppInfo;

	//下载队列，等候队列，暂停队列，完成队列
	private List<SimpleAppInfo> mManagerList = null;
	private int mDownloadingCnt;
	private int mPendingCnt;
	private int mPausedCnt;
	private int mFinishedCnt;

	//监听器队列
	private List<OnDownloadStatusChangeListener> mDownloadStatusListeners;
	private List<OnProgressUpdateListener> mOnProgressUpdateListeners;
	private ReentrantLock mNotifyStatusLock;
	private ReentrantLock mNotifyProcessLock;

	private OplayDownloadManager(Context context) {
		super(context.getApplicationContext());
		mManagerList = new ArrayList<>();
		mDownloadingCnt = 0;
		mPendingCnt = 0;
		mPausedCnt = 0;
		mFinishedCnt = 0;
		mUrl_SimpleAppInfo = new HashMap<>();
		mPackageName_SimpleAppInfo = new HashMap<>();
		mDownloadStatusListeners = new LinkedList<>();
		mOnProgressUpdateListeners = new LinkedList<>();
		dbHelper_download = DBHelper_Download.getInstance(mApplicationContext);
		addDownloadStatusListener(dbHelper_download);
		addProgressUpdateListener(dbHelper_download);
		OplayInstallNotifier.getInstance().addListener(this);
		mNotifyStatusLock = new ReentrantLock();
		mNotifyProcessLock = new ReentrantLock();
	}

	public synchronized static OplayDownloadManager getInstance(Context context) {
		try {
			if (mInstance == null) {
				mInstance = new OplayDownloadManager(context);
			}
		} catch (Exception e) {
			if (AppDebugConfig.IS_DEBUG) {
				e.printStackTrace();
			}
		}
		return mInstance;
	}

	public void onDestroy() {
	}

	/**
	 * ********************************************************************
	 * <p/> app启动初始化
	 * ********************************************************************
	 */
	public void initDownloadList() {
		dbHelper_download.getDownloadList();
		OPlayNotificationManager.showDownload(mApplicationContext);
	}

	//仅限于初始化的时候用
	public void addPausedTask(SimpleAppInfo info) {
		if (!checkDownloadTask(info)) {
			if (AppDebugConfig.IS_DEBUG) {
				Log.d("OplayDownloadManager", "Wrong input info in addPausedTask");
			}
			return;
		}
		mManagerList.add(getEndOfPaused(), info);
		mPausedCnt++;
		mUrl_SimpleAppInfo.put(info.getOwkUrl(), info);
		mPackageName_SimpleAppInfo.put(info.getPackageName(), info);
	}

	public void addFinishedTask(SimpleAppInfo info) {
		if (!checkDownloadTask(info)) {
			if (AppDebugConfig.IS_DEBUG) {
				Log.d("OplayDownloadManager", "Wrong input info in addFinishedTask");
			}
			return;
		}
		mManagerList.add(getEndOfFinished(), info);
		mFinishedCnt++;
		mUrl_SimpleAppInfo.put(info.getOwkUrl(), info);
		mPackageName_SimpleAppInfo.put(info.getPackageName(), info);
	}

	/**
	 * ********************************************************************
	 * 下载过程中事件的回调
	 * ********************************************************************
	 */
	@Override
	protected FileCacheDirectoryStorer getCacheDirectoryStorer(FileDownloadTask task) {
		return OplayApkCacheDirectoryStorer.getInstance(mApplicationContext);// 指定目录
	}

	@Override
	protected DownloadFileNameListener getFileNameFactory() {
		return OplayApkFileNameCreator.getInstance(mApplicationContext);//指定名字构造器
	}

	/**
	 * ***************************************************************************
	 * 用户主动发起事件
	 * ***************************************************************************
	 */

	/**
	 * 添加一个下载任务到下载队列中
	 *
	 * @param info 被添加到下载队列中的任务
	 */
	public void addDownloadTask(SimpleAppInfo info) {
		//校验下载信息的完整性
		if (!checkDownloadTask(info)) {
			return;
		}
		AppDebugConfig.logMethodWithParams(this, "A task had been added to the downloadList!");
		final String apkUrl = info.getOwkUrl();
		//下载包已经在队列中的情况
		if (mUrl_SimpleAppInfo.containsKey(apkUrl)) {
			AppDebugConfig.logMethodWithParams(this, "The Task already in the downloading list!");

			final SimpleAppInfo appInfo = mUrl_SimpleAppInfo.get(apkUrl);
			if (appInfo == null || appInfo.getDownloadStatus() == null) {
				AppDebugConfig.logMethodWithParams(this, "下载内容错误");
				return;
			}
			final DownloadStatus status = appInfo.getDownloadStatus();

			AppDebugConfig.logMethodWithParams(this, "原本状态: " + status);
			switch (status) {
				case DISABLE: {
					AppDebugConfig.logMethodWithParams(this, "The Task status invalid: DISABLE CAN NOT BE DOWNLOAD!");
					return;
				}
				case PENDING:
				case DOWNLOADING: {
					AppDebugConfig.logMethodWithParams(this, "The Task is downloading!");
					return;
				}
				case PAUSED:
				case FAILED: {
					AppDebugConfig.logMethodWithParams(this, "The Task is not downloading, restart task!");
					restartDownloadTask(info);
					return;
				}
				case FINISHED: {
					File apkFile = appInfo.getDestFile();
					if (apkFile.exists()) {
						AppDebugConfig.logMethodWithParams(this, "文件已存在，直接安装");
						//如果文件存在直接安装
						handleStartInstallApk(appInfo);
						return;
					} else {
						AppDebugConfig.logMethodWithParams(this, "文件不存在，重置任务，重新下载安装");
						//如果文件不存在，重置状态并删除，重新下载安装
						deleteDownloadInfo(appInfo);
						//zhu
						break;
					}
				}
				default: {
					AppDebugConfig.logMethodWithParams(this, "info status invalid!");
					deleteDownloadInfo(appInfo);
					return;
				}
			}
		}
		mUrl_SimpleAppInfo.put(info.getOwkUrl(), info);
		mPackageName_SimpleAppInfo.put(info.getPackageName(), info);
		addPendingTask(info);
		OPlayNotificationManager.showDownload(mApplicationContext);
		showloginformation("addDownloadTask");
	}

	/**
	 * 从 暂停，下载失败中恢复下载的事件
	 * 注：这个函数可以被外部调用，故info不必保证在下载队列中,可以放心使用
	 *
	 * @param info 需要被恢复的事件
	 */
	public void restartDownloadTask(SimpleAppInfo info) {
		if (!checkDownloadTask(info)) {
			return;
		}
		SimpleAppInfo s = mUrl_SimpleAppInfo.get(info.getOwkUrl());
		if (s != null) {
			if (mManagerList.remove(s)) {
				mPausedCnt = decrease(mPausedCnt);
				addPendingTask(s);
			}
		}
		showloginformation("restartDownloadTask");
	}

	/**
	 * 暂停一个下载任务,info不必一定在下载队列中，根据初始URL保证唯一性
	 *
	 * @param info 即将被暂停的任务
	 */
	public void stopDownloadTask(SimpleAppInfo info) {
		info = mUrl_SimpleAppInfo.get(info.getOwkUrl());
		if (!checkDownloadTask(info)) {
			return;
		}
		info = stopDownloadingTask(info);
		if (info != null) {
			mManagerList.add(mDownloadingCnt + mPendingCnt, info);
			info.setDownloadStatus(DownloadStatus.PAUSED);
			mPausedCnt++;
			notifyDownloadStatusListeners(info);
		}

		showloginformation("stopDownloadTask");
	}

	/**
	 * 根据url移除一个下载任务
	 *
	 * @param url 即将被移除任务的初始URL
	 */
	public synchronized void removeDownloadTask(String url) {
		SimpleAppInfo info = mUrl_SimpleAppInfo.get(url);
		if (!checkDownloadTask(info)) {
			return;
		}
		DownloadStatus ds = info.getDownloadStatus();
		switch (ds) {
			case DOWNLOADING:
				stopDownloadingTask(info);
				break;
			case PENDING:
				mManagerList.remove(info);
				mPendingCnt = decrease(mPendingCnt);
				break;
			case PAUSED:
			case FAILED:
				mManagerList.remove(info);
				mPausedCnt = decrease(mPausedCnt);
				break;
			case FINISHED:
				mManagerList.remove(info);
				mFinishedCnt = decrease(mFinishedCnt);
				break;
		}
		mUrl_SimpleAppInfo.remove(info.getOwkUrl());
		mPackageName_SimpleAppInfo.remove(info.getPackageName());
		notifyDownloadStatusListeners(info);
		OPlayNotificationManager.showDownload(mApplicationContext);
		showloginformation("removeDownloadTask");
	}

	/**
	 * 添加一个任务到等待队列
	 *
	 * @param info 即将要添加到队列中的任务
	 */
	private synchronized void addPendingTask(SimpleAppInfo info) {
		mManagerList.add(getEndOfDownloading(), info);
		info.setDownloadStatus(DownloadStatus.PENDING);
		mPendingCnt++;
		notifyDownloadStatusListeners(info);
		if (mDownloadingCnt < MAX_DOWNLOADING_COUNT) {
			String url = info.getOwkUrl();
			if (oplayDownload(url)) {
				logDownloadStart(info);
				SimpleAppInfo s = mManagerList.remove(mDownloadingCnt);
				mPendingCnt = decrease(mPendingCnt);
				s.setDownloadStatus(DownloadStatus.DOWNLOADING);
				mManagerList.add(mDownloadingCnt, s);
				mDownloadingCnt++;
				notifyDownloadStatusListeners(s);
			}
		}
	}

	/**
	 * 停止一个等待下载或者正在下载的任务，这个函数只能在本类内部调用，info要保证在下载队列中
	 *
	 * @param info 将要被停止的下载任务
	 */

	private synchronized SimpleAppInfo stopDownloadingTask(SimpleAppInfo info) {
		if (AppDebugConfig.IS_DEBUG) {
			AppDebugConfig.logMethodWithParams(this, info.getOwkUrl());
		}
		if (info == null) {
			return null;
		}
		mManagerList.remove(info);
		if (DownloadStatus.DOWNLOADING.equals(info.getDownloadStatus())) {
			mDownloadingCnt = decrease(mDownloadingCnt);
			stopDownload(info.getOwkUrl());
			if (mPendingCnt > 0) {
				SimpleAppInfo s = mManagerList.get(mDownloadingCnt);
				if (s != null && oplayDownload(s.getOwkUrl())) {
					mManagerList.remove(s);
					mPendingCnt = decrease(mPendingCnt);
					s.setDownloadStatus(DownloadStatus.DOWNLOADING);
					mManagerList.add(mDownloadingCnt, s);
					mDownloadingCnt++;
				}
			}
		} else {
			mPendingCnt = decrease(mPendingCnt);
		}
		return info;
	}

	private boolean checkDownloadTask(SimpleAppInfo info) {
		return !(info == null || TextUtils.isEmpty(info.getPackageName()) || TextUtils.isEmpty(info.getOwkUrl()));
	}

	/**
	 * 关闭所有正在下载的下载项,每次都找第一个，如果状态为正在下载或者在排队，就暂停它
	 */
	public synchronized void stopAll() {
		try {

			SimpleAppInfo info = mManagerList.get(0);
			int i = 0;
			if (info != null) {
				while (info.getDownloadStatus().equals(DownloadStatus.DOWNLOADING) ||
						info.getDownloadStatus().equals(DownloadStatus.PENDING)) {
					stopDownloadTask(info);
					info = mManagerList.get(0);

					if (++i > 1000) {
						//这里防止死循环--！
						break;
					}
				}
			}
		} catch (Exception e) {
			Debug_SDK.e(e);
		}
	}

	private void deleteDownloadInfo(SimpleAppInfo s) {
		if (s != null) {
			final SimpleAppInfo info = mUrl_SimpleAppInfo.get(s.getOwkUrl());
			if (info != null) {
				info.setDownloadStatus(null);
				mUrl_SimpleAppInfo.remove(s.getOwkUrl());
				mPackageName_SimpleAppInfo.remove(s.getPackageName());
				mManagerList.remove(info);
				s.initAppInfoStatus(mApplicationContext);
			}
		}
	}

	/**
	 * **************************************************************************
	 * <p/> get 方法
	 * **************************************************************************
	 */

	public DownloadStatus getAppDownloadStatus(String url) {
		//如果存在，就返回当前的status；不存在就返回null
		SimpleAppInfo s = mUrl_SimpleAppInfo.get(url);
		return s != null ? s.getDownloadStatus() : null;
	}

	public SimpleAppInfo getSimpleAppInfoByPackageName(String packageName) {
		return mPackageName_SimpleAppInfo.get(packageName);
	}

	public SimpleAppInfo getSimpleAppInfoByUrl(String url) {
		return mUrl_SimpleAppInfo.get(url);
	}

	public List<SimpleAppInfo> getDownloadList() {
		initStatus();
		return mManagerList;
	}

	private void initStatus() {
		for (SimpleAppInfo s : mManagerList) {
			s.initAppInfoStatus(mApplicationContext);
		}
	}

	public int getEndOfDownloading() {
		return mDownloadingCnt + mPendingCnt;
	}

	public int getEndOfPaused() {
		return getEndOfDownloading() + mPausedCnt;
	}

	public int getEndOfFinished() {
		return getEndOfPaused() + mFinishedCnt;
	}

	public int getNotificationNum() {
		return getEndOfDownloading() + mPausedCnt;
	}

	//通过URL获取下载进度
	public int getProgressByUrl(String url) {
		final SimpleAppInfo s = mUrl_SimpleAppInfo.get(url);
		if (s != null) {
			return s.getCompleteProgress();
		}
		return 0;
	}

	/**
	 * debug information
	 */
	private void showloginformation(String tag) {
		if (AppDebugConfig.IS_DEBUG) {
			Log.i("OplayDownloadManager_" + tag, "pending: " + mPendingCnt);
			Log.i("OplayDownloadManager_" + tag, "downloading: " + mDownloadingCnt);
			Log.i("OplayDownloadManager_" + tag, "paused: " + mPausedCnt);
			Log.i("OplayDownloadManager_" + tag, "finished: " + mFinishedCnt);
			for (SimpleAppInfo s : mManagerList) {
				Log.i("OplayDownloadManager_" + tag, s.getAppName() + "-----" + s.getDownloadStatus());
			}
		}
	}

	@Override
	public void onInstall(Context context, final String packageName) {
		final SimpleAppInfo s = getSimpleAppInfoByPackageName(packageName);
		if (s != null) {
			s.initAppInfoStatus(mApplicationContext);
			if (AppDebugConfig.IS_DEBUG) {
				AppDebugConfig.logMethodWithParams(this, s.getAppName(), s.getDownloadStatus(), s.getInstallStatus(),
						s.getAppStatus());
			}
			notifyDownloadStatusListeners(s);
		}
	}

	/**
	 * ***************************************************************************
	 * 下载状态改变后，负责给监听器发消息
	 * ***************************************************************************
	 */
	public synchronized void addDownloadStatusListener(OnDownloadStatusChangeListener listener) {
		try {
			if (mNotifyStatusLock != null) {
				mNotifyStatusLock.lock();
			}
			if (listener != null && !mDownloadStatusListeners.contains(listener)) {
				mDownloadStatusListeners.add(listener);
			}
		} catch (Exception e) {
			Debug_SDK.e(e);
		} finally {
			if (mNotifyStatusLock != null) {
				mNotifyStatusLock.unlock();
			}
		}
	}

	public synchronized void removeDownloadStatusListener(OnDownloadStatusChangeListener listener) {
		try {
			if (mNotifyStatusLock != null) {
				mNotifyStatusLock.lock();
			}
			if (listener != null) {
				mDownloadStatusListeners.remove(listener);
			}
		} catch (Exception e) {
			Debug_SDK.e(e);
		} finally {
			if (mNotifyStatusLock != null) {
				mNotifyStatusLock.unlock();
			}
		}
	}

	public void notifyDownloadStatusListeners(SimpleAppInfo info) {
		try {
			if (mNotifyStatusLock != null) {
				mNotifyStatusLock.lock();
			}
			int size;
			OnDownloadStatusChangeListener[] arrays;
			size = mDownloadStatusListeners.size();
			arrays = new OnDownloadStatusChangeListener[size];
			mDownloadStatusListeners.toArray(arrays);
			if (arrays != null) {
				for (int i = 0; i < size; i++) {
					if (arrays[i] != null) {
						arrays[i].onDownloadStatusChanged(info);
					}
				}
			}
		} catch (Exception e) {
			Debug_SDK.e(e);
		} finally {
			if (mNotifyStatusLock != null) {
				mNotifyStatusLock.unlock();
			}
		}
	}

	public synchronized void removeProgressUpdateListener(OnProgressUpdateListener listener) {
		if (listener != null) {
			mOnProgressUpdateListeners.remove(listener);
		}
	}

	public synchronized void addProgressUpdateListener(OnProgressUpdateListener listener) {
		if (listener != null && !mOnProgressUpdateListeners.contains(listener)) {
			mOnProgressUpdateListeners.add(listener);
		}
	}

	public void notifyProgressUpdateListeners(String url, int percent, long speedBytePerS) {
		int size;
		OnProgressUpdateListener[] arrays;
		synchronized (this) {
			size = mOnProgressUpdateListeners.size();
			arrays = new OnProgressUpdateListener[size];
			mOnProgressUpdateListeners.toArray(arrays);
		}
		if (arrays != null) {
			for (int i = 0; i < arrays.length; i++) {
				if (arrays[i] != null) {
					arrays[i].onProgressUpdate(url, percent, speedBytePerS);
				}
			}
		}
	}

	@Override
	protected void notifyListener_onFileDownloadProgressUpdate(ApkDownloadListener listener, FileDownloadTask task,
	                                                           long contentLength, long completeLength, int percent,
	                                                           long speedBytesPerS) {
		final String downloadUrl = task.getRawUrl();
		if (downloadUrl != null) {
			SimpleAppInfo s = mUrl_SimpleAppInfo.get(downloadUrl);
			if (s != null && percent != 0) {
				s.setCompleteProgress(percent);
			}
		}
		notifyProgressUpdateListeners(task.getRawUrl(), percent, speedBytesPerS);
	}

	@Override
	protected void onHandleApkDownloadBeforeStart_FileLock(FileDownloadTask task) {

	}

	@Override
	protected void onHandleApkDownloadStart(FileDownloadTask task) {
	}

	@Override
	protected void onHandleApkDownloadSuccess(FileDownloadTask task) {
		SimpleAppInfo s = mUrl_SimpleAppInfo.get(task.getRawUrl());
		logDownloadFinish(s);
		if (s != null) {
			stopDownloadingTask(s);
			s.setDownloadStatus(DownloadStatus.FINISHED);
			s.setCompleteProgress(DOWNLOADFINISHED_PERCENT);
			mManagerList.add(mDownloadingCnt + mPendingCnt + mPausedCnt, s);
			mFinishedCnt++;
			notifyDownloadStatusListeners(s);
			OPlayNotificationManager.showDownloadComplete(mApplicationContext, s);
			OPlayNotificationManager.showDownload(mApplicationContext);
			final long fileLength = s.getApkFileSizeLong();
			handleStartInstallApk(s);
		}
		showloginformation("handleDownloadSuccess");
	}

	@Override
	protected void onHandleApkDownloadFailed(FileDownloadTask task) {
		try {
			showloginformation("handleDownloadFailed");
			SimpleAppInfo s = mUrl_SimpleAppInfo.get(task.getRawUrl());
			if (s != null) {
				stopDownloadingTask(s);
				s.setDownloadStatus(DownloadStatus.FAILED);
				mManagerList.add(mDownloadingCnt + mPendingCnt, s);
				mPausedCnt++;
				logDownloadFailed(s);

				File storeFile = task.getStoreFile();
				if (storeFile != null) {
					File parent = new File(storeFile.getParent());
					if (parent.getUsableSpace() < task.getContentLength()) {
						OPlayNotificationManager.showDownloadFailed(mApplicationContext,
								s.getOwkUrl(), s.getAppName(), "手机内存空间不足");
						notifyDownloadStatusListeners(s);
						return;
					}
				}
				OPlayNotificationManager.showDownloadFailed(mApplicationContext, s.getOwkUrl(), s.getAppName(), null);
				notifyDownloadStatusListeners(s);
			}
		} catch (Exception e) {
			Debug_SDK.e(e);
		}

	}

	@Override
	protected void onHandleApkDownloadStop(FileDownloadTask task) {

	}

	@Override
	protected synchronized void onHandleApkAlreadyExist(FileDownloadTask task) {
		String rawUrl = task.getRawUrl();
		SimpleAppInfo s = mUrl_SimpleAppInfo.get(rawUrl);
		if (s == null) {
			return;
		} else {
			stopDownloadingTask(s);
			mManagerList.add(getEndOfPaused(), s);
			s.setDownloadStatus(DownloadStatus.FINISHED);
			s.setCompleteProgress(DOWNLOADFINISHED_PERCENT);
			notifyDownloadStatusListeners(s);
			mFinishedCnt++;
			OPlayNotificationManager.showDownload(mApplicationContext);
			handleStartInstallApk(s);
		}
		showloginformation("handleAlreadyExists");
	}

	private int decrease(int number) {
		return number - 1 < 0 ? 0 : number - 1;
	}

	/**
	 * apk存在的情况下，启动安装<br/>
	 * 如果属于offer任务，还会发送
	 */
	private void handleStartInstallApk(SimpleAppInfo vo) {
		try {
			// 如果应该自动安装，则弹出安装，否则不处理
			InstallManager.install(mApplicationContext, vo);
		} catch (Throwable e) {
			if (AppDebugConfig.IS_DEBUG) {
				Debug_SDK.e(e);
			}
		}
	}

	public boolean oplayDownload(final String url) {
		Util_System_Runtime.getInstance().runInUiThread(new Runnable() {
			@Override
			public void run() {
				download(url);
			}
		});
		return true;
	}

	private void logDownloadStart(SimpleAppInfo info) {
		try {
			final int appId = info.getAppId();
		} catch (Exception e) {
			if (AppDebugConfig.IS_DEBUG) {
				Debug_SDK.e(e);
			}
		}
	}

	private void logDownloadFinish(SimpleAppInfo info) {
		try {
			final int appId = info.getAppId();

		} catch (Exception e) {
			if (AppDebugConfig.IS_DEBUG) {
				Debug_SDK.e(e);
			}
		}
	}

	private void logDownloadFailed(SimpleAppInfo info) {
		try {
			final String url = info.getOwkUrl();
		} catch (Exception e) {
			if (AppDebugConfig.IS_DEBUG) {
				Debug_SDK.e(e);
			}
		}
	}

	private boolean isDebug() {
		return DEBUG && AppDebugConfig.IS_DEBUG;
	}

	public interface OnDownloadStatusChangeListener {
		public void onDownloadStatusChanged(final SimpleAppInfo info);
	}

	public interface OnProgressUpdateListener {
		public void onProgressUpdate(final String url, final int percent, final long speedBytesPerS);
	}
}