package net.youmi.android.libs.common.download;

import android.content.Context;
import android.util.Log;

import net.youmi.android.libs.common.basic.Basic_StringUtil;
import net.youmi.android.libs.common.coder.Coder_Md5;
import net.youmi.android.libs.common.debug.Debug_SDK;
import net.youmi.android.libs.common.download.filenamecreator.DownloadFileNameListener;
import net.youmi.android.libs.common.download.filestorer.FileCacheDirectoryStorer;
import net.youmi.android.libs.common.download.listener.FileAvailableChecker;
import net.youmi.android.libs.common.download.listener.FileDownloadListener;
import net.youmi.android.libs.common.download.listener.FileDownloadWebViewListener;
import net.youmi.android.libs.common.download.model.FileDownloadTask;
import net.youmi.android.libs.common.download.webview.FileDownloadWebView;
import net.youmi.android.libs.common.template.Template_ListenersManager;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * 支持重定向下载链接的下载管理类。<br/>
 * <p>
 * 支持以下重定向跳转:
 * <ul>
 * <li>
 * 301</li>
 * <li>
 * 302</li>
 * <li>
 * Meta</li>
 * <li>
 * JavaScript</li>
 * </ul>
 * </p>
 * 针对可重定向的特性按以下规则对文件的下载流程进行封装处理:<br/>
 * <ol>
 * <li>
 * url判定: <br/>
 * 指定的url除非不带查询串并且以子类指定的后缀名结尾，否则都会判定为可跳转型链接， 这时候会通过一个特殊的FileDownloadWebView进行加载并提取出下载的最终url。<br/>
 * 使用WebView进行下载url提取的花时如果超过60秒，则判定为下载失败</li>
 * <li>
 * 文件名: <br/>
 * 通过IDownloadFileNameFactory获取。</li>
 * </ol>
 * <p>
 *
 * </p>
 * 注意，sdk必须继承该类，并使用单例进行apk的下载管理。子类应该考虑对下载过程中的步骤进行效果记录的发送。<br/>
 * 该管理器只负责完成下载流程，之后的启动安装流程并不进行处理。子类需要对后续操作进行补充处理。
 *
 * @author jen
 * @author zhitaocai edit on 2014-7-16
 * @since 2012-11-20
 */
public abstract class BaseRedirectAbleDownloadManager<T> extends Template_ListenersManager<T> implements
		FileDownloadListener, FileAvailableChecker, FileDownloadWebViewListener {

	/**
	 * 用来标识所有已经启动的webview
	 */
	private HashSet<FileDownloadWebView> mFileDownloadWebViewSet;

	/**
	 * 用来标识本类所启动的下载任务，如果不是本类所启动的FileDownloadTask，在接受到回调时将不进行任何处理。
	 */
	private HashSet<FileDownloadTask> mFileDownloadTaskSet;

	/**
	 * 保存正在下载中的原始url与目标下载任务的映射
	 */
	private HashMap<String, FileDownloadTask> mMap_Downloading_RawUrl_FileDownloadTask;

	protected Context mApplicationContext;

	public Context getApplicationContext() {
		return mApplicationContext;
	}

	public BaseRedirectAbleDownloadManager(Context context) {
		mApplicationContext = context.getApplicationContext();
		mMap_Downloading_RawUrl_FileDownloadTask = new HashMap<String, FileDownloadTask>();
		mFileDownloadWebViewSet = new HashSet<FileDownloadWebView>();
		mFileDownloadTaskSet = new HashSet<FileDownloadTask>();
		// 在初始化的时候，必须注册文件下载管理器的监听
		FinalFileDownloadManager.getInstance().registerListener(this); // 这里是否需要找地方注销注册呢
	}

	public boolean download(String url) {
		return download(url, null);
	}

	/**
	 * 1、检查这个url是否已经在下载了，如果是就结束下面步骤 2、检查这个url是否为最终url，然后判断是否需要通过webview获取最终url 3、进行下载
	 *
	 * @param rawUrl
	 * @param md5sum
	 * @return
	 */
	public boolean download(String rawUrl, String md5sum) {
		try {
			if (rawUrl == null) {
				return false;
			}
			rawUrl = rawUrl.trim();
			if (rawUrl.length() <= 0) {
				return false;
			}

			if (Debug_SDK.isDownloadLog) {
				Debug_SDK.ti(Debug_SDK.mDownloadTag, this, "---下载url:%s\n---md5:%s", rawUrl, md5sum);
				Debug_SDK.ti(Debug_SDK.mDownloadTag, this, "---当前列表存储的webview数量:%d", mFileDownloadWebViewSet.size());
			}
			// 如果包括这个任务就标识正在下载中
			if (mMap_Downloading_RawUrl_FileDownloadTask.containsKey(rawUrl)) {
				if (Debug_SDK.isDownloadLog) {
					Debug_SDK.td(Debug_SDK.mDownloadTag + "123_", this, "---[%s]正在下载中,不启动下载任务\n当前总共有%d个下载任务在运行",
							rawUrl, mMap_Downloading_RawUrl_FileDownloadTask.size());
				}
				return true;
			}

			// 判断是否需要使用webview进行预先加载获取最终url
//			if (isNeedToLoadWithWebView(rawUrl)) {
				try {
					FileDownloadWebView wv = new FileDownloadWebView(mApplicationContext, this, rawUrl, md5sum);
					mFileDownloadWebViewSet.add(wv);
					try {
						new Thread(wv).start();// 启动自我监听，睡眠 60s（期间加载url），60s后根据加载结果来反馈监听
					} catch (Throwable e) {
						if (Debug_SDK.isDownloadLog) {
							Debug_SDK.td(Debug_SDK.mDownloadTag, this, e);
						}
					}
					wv.loadUrl();
					return true;
				} catch (Throwable e) {
					if (Debug_SDK.isDownloadLog) {
						Debug_SDK.td(Debug_SDK.mDownloadTag, this, e);
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
	 * 停止下载任务
	 *
	 * @param task
	 */
	public boolean stopDownload(FileDownloadTask task) {
		try {
			// 停止下载，从下载中映射列表中移除
			if (FinalFileDownloadManager.getInstance().stopDownloadByUrl(mApplicationContext, task.getRawUrl())) {
				removeFileDownloadTaskFromMapAndSet(task);
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
	 * 停止下载任务
	 *
	 */
	public boolean stopDownload(String url) {
		return stopDownload(url, null);
	}

	/**
	 * 停止下载任务
	 *
	 */
	public boolean stopDownload(String url, String md5sum) {
		if (Basic_StringUtil.isNullOrEmpty(url)) {
			return false;
		}
		return stopDownload(new FileDownloadTask(url, md5sum));
	}

	/**
	 * 获取最终保存的下载文件
	 *
	 * @param task
	 * @param contentDisposition
	 * @return
	 */
	protected File getStoreDownloadFile(FileDownloadTask task, String contentDisposition) {
		try {
			// 目录
			FileCacheDirectoryStorer storer = getCacheDirectoryStorer(task);
			// 文件名
			String fileName = getFileNameFactory().getStoreFileName(task, contentDisposition);
			// 文件
			File downloadFile = storer.getFileByFileName(fileName);
			// task.setDestSaveFilePath(downloadFile.getAbsolutePath());//重要设置

			return downloadFile;

		} catch (Throwable e) {
			if (Debug_SDK.isDownloadLog) {
				Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
			}
		}
		return null;
	}

	/**
	 * 检查文件是否可用<br/>
	 * <ol>
	 * <li>
	 * 如果存在md5sum，则检查文件的md5sum，如果通过则返回true，否则返回false</li>
	 * <li>
	 * 在md5sum不存在的情况下，如果，检查文件的长度是否符合，如果符合则返回true，否则返回false</li>
	 * </ol>
	 * ps:在以上两个标准都无法判断的情况下，可能需要根据文件是否可正常解压缩来判断。
	 */
	@Override
	public boolean checkFileAvailable(FileDownloadTask task) {
		try {
			if (task == null) {
				return false;
			}
			if (task.getStoreFile() == null) {
				return false;
			}

			if (!task.getStoreFile().exists()) {
				return false;
			}
			if (task.getMd5sum() != null) {
				return Coder_Md5.checkMd5Sum(task.getStoreFile(), task.getMd5sum());// 直接返回md5sum检查的结果
			}
			if (task.getContentLength() > 0) {
				return (task.getContentLength() == task.getStoreFile().length());// 直接返回文件长度判断的结果
			}
		} catch (Throwable e) {
			if (Debug_SDK.isDownloadLog) {
				Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
			}
		}
		return false;
	}

	/**
	 * 是否需要联网检查文件长度<br/>
	 * 这里通过FileDownloadTask里面是否已经有contentLength和md5sum值来判断是否需要联网检查文件长度，规则如下:<br/>
	 * 如果md5sum不为空，则不需要联网检查文件长度，返回false<br/>
	 * 如果md5sum为空，而contentLength>0(可能是通过webview加载得到的url和contentLength)， 则不需要联网检查文件长度，返回false<br/>
	 * 如果md5sum为空，同时contentLength<=0，则需要联网检查文件长度。<br/>
	 * <br/>
	 * 注:md5sum在FileDownloadTask设置时，需要进行验证，如果不是合法的字符串，需要置为null.
	 */
	@Override
	public boolean isNeedToCheckContentLengthByNetwork(FileDownloadTask task) {
		try {
			if (task == null) {
				return false;
			}

			// md5sum不为空，则不需要联网检查文件长度，返回false
			if (task.getMd5sum() != null) {
				return false;
			}

			// md5sum为空的情况，需要判断contentLength
			if (task.getContentLength() > 0) {

				// contentLength>0(可能是通过webview加载得到的url和contentLength)，
				// 则不需要联网检查文件长度，返回false
				return false;
			}

			// contentLength<=0
			// 需要联网检查文件长度，返回true
			return true;

		} catch (Throwable e) {
			if (Debug_SDK.isDownloadLog) {
				Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
			}
		}

		return true;
	}

	/**
	 * 将指定的下载任务从set和map中移除
	 * <p>
	 * 文件下载成功，失败，或者文件本来就存在的时候都要调用这个
	 *
	 * @param task
	 */
	public void removeFileDownloadTaskFromMapAndSet(FileDownloadTask task) {
		try {
			if (task == null) {
				return;
			}
			if (mMap_Downloading_RawUrl_FileDownloadTask.containsKey(task.getRawUrl())) {
				mMap_Downloading_RawUrl_FileDownloadTask.remove(task.getRawUrl());
				if (Debug_SDK.isDownloadLog) {
					Debug_SDK.tw(Debug_SDK.mDownloadTag + "123_", this, "本管理器 [%s]从map中移除task成功:\ntaskurl:%s", this
							.getClass().getSimpleName(), task.getDestUrl());
				}
			}
			if (mFileDownloadTaskSet.contains(task)) {

				if (mFileDownloadTaskSet.remove(task)) {
					if (Debug_SDK.isDownloadLog) {
						Debug_SDK.tw(Debug_SDK.mDownloadTag + "123_", this, "本管理器 [%s]从set中移除task成功:\ntaskurl:%s", this
								.getClass().getSimpleName(), task.getDestUrl());
					}
				} else {
					if (Debug_SDK.isDownloadLog) {
						Debug_SDK.tw(Debug_SDK.mDownloadTag + "123_", this, "本管理器 [%s]从set中移除task失败:\ntaskurl:%s", this
								.getClass().getSimpleName(), task.getDestUrl());
					}
				}
				return;
			}
		} catch (Throwable e) {
			if (Debug_SDK.isDownloadLog) {
				Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
			}
		}
	}

	@Override
	public final void onFileDownloadBeforeStart_FileLock(FileDownloadTask task) {
		try {
			if (task == null) {
				return;
			}
			if (!mFileDownloadTaskSet.contains(task)) {
				if (Debug_SDK.isDownloadLog) {
					Debug_SDK.te(Debug_SDK.mDownloadTag, this, "下载准备开始(文件处于文件锁)：下载url:[%s],不是管理器[%s]启动的下载任务,取消处理",
							task.getDestUrl(), this.getClass().getSimpleName());
				}
				return;
			} else {
				if (Debug_SDK.isDownloadLog) {
					Debug_SDK.ti(Debug_SDK.mDownloadTag, this, "下载准备开始(文件处于文件锁)：下载url:[%s],是管理器[%s]启动的下载任务,需要处理",
							task.getDestUrl(), this.getClass().getSimpleName());
				}
			}
		} catch (Throwable e) {
			if (Debug_SDK.isDownloadLog) {
				Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
			}
		}

		try {
			onHandleFileDownloadBeforeStart_FileLock(task);
		} catch (Throwable e) {
			if (Debug_SDK.isDownloadLog) {
				Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
			}
		}

		List<T> lists = getListeners();
		for (int i = 0; i < lists.size(); i++) {
			try {
				T listener = lists.get(i);
				if (listener != null) {
					notifyListener_onFileDownloadBeforeStart_FileLock(listener, task);
				}
			} catch (Throwable e) {
				if (Debug_SDK.isDownloadLog) {
					Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
				}
			}
		}
	}

	@Override
	public final void onFileDownloadStart(FileDownloadTask task) {
		try {
			if (task == null) {
				return;
			}
			if (!mFileDownloadTaskSet.contains(task)) {
				if (Debug_SDK.isDownloadLog) {
					Debug_SDK.te(Debug_SDK.mDownloadTag, this, "下载开始：下载url:[%s],不是管理器[%s]启动的下载任务,取消处理",
							task.getDestUrl(), this.getClass().getSimpleName());
				}
				return;
			} else {
				if (Debug_SDK.isDownloadLog) {
					Debug_SDK.ti(Debug_SDK.mDownloadTag, this, "下载开始：下载url:[%s],是管理器[%s]启动的下载任务,需要处理",
							task.getDestUrl(), this.getClass().getSimpleName());
				}
			}
		} catch (Throwable e) {
			if (Debug_SDK.isDownloadLog) {
				Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
			}
		}

		try {
			onHandleFileDownloadStart(task);
		} catch (Throwable e) {
			if (Debug_SDK.isDownloadLog) {
				Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
			}
		}

		List<T> lists = getListeners();
		for (int i = 0; i < lists.size(); i++) {
			try {
				T listener = lists.get(i);
				if (listener != null) {
					notifyListener_onFileDownloadStart(listener, task);
				}
			} catch (Throwable e) {
				if (Debug_SDK.isDownloadLog) {
					Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
				}
			}
		}
	}

	@Override
	public final void onFileDownloadSuccess(FileDownloadTask task) {

		removeFileDownloadTaskFromMapAndSet(task);

		try {
			onHandleFileDownloadSuccess(task);
		} catch (Throwable e) {
			if (Debug_SDK.isDownloadLog) {
				Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
			}
		}

		List<T> lists = getListeners();
		for (int i = 0; i < lists.size(); i++) {
			try {
				T listener = lists.get(i);
				if (listener != null) {
					notifyListener_onFileDownloadSuccess(listener, task);
				}
			} catch (Throwable e) {
				if (Debug_SDK.isDownloadLog) {
					Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
				}
			}
		}
	}

	@Override
	public final void onFileDownloadFailed(FileDownloadTask task) {

		removeFileDownloadTaskFromMapAndSet(task);

		try {
			onHandleFileDownloadFailed(task);
		} catch (Throwable e) {
			if (Debug_SDK.isDownloadLog) {
				Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
			}
		}

		List<T> lists = getListeners();
		for (int i = 0; i < lists.size(); i++) {
			try {
				T listener = lists.get(i);
				if (listener != null) {
					notifyListener_onFileDownloadFailed(listener, task);
				}
			} catch (Throwable e) {
				if (Debug_SDK.isDownloadLog) {
					Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
				}
			}
		}
	}

	@Override
	public final void onFileDownloadStop(FileDownloadTask task) {
		removeFileDownloadTaskFromMapAndSet(task);

		try {
			onHandleFileDownloadStop(task);
		} catch (Throwable e) {
			if (Debug_SDK.isDownloadLog) {
				Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
			}
		}

		List<T> lists = getListeners();
		for (int i = 0; i < lists.size(); i++) {
			try {
				T listener = lists.get(i);
				if (listener != null) {
					notifyListener_onFileDownloadStop(listener, task);
				}
			} catch (Throwable e) {
				if (Debug_SDK.isDownloadLog) {
					Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
				}
			}
		}

	}

	@Override
	public final void onFileAlreadyExist(FileDownloadTask task) {

		removeFileDownloadTaskFromMapAndSet(task);

		try {
			onHandleFileAlreadyExist(task);
		} catch (Throwable e) {
			if (Debug_SDK.isDownloadLog) {
				Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
			}
		}

		List<T> lists = getListeners();
		for (int i = 0; i < lists.size(); i++) {
			try {
				T listener = lists.get(i);
				if (listener != null) {
					notifyListener_onFileAlreadyExist(listener, task);
				}
			} catch (Throwable e) {
				if (Debug_SDK.isDownloadLog) {
					Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
				}
			}
		}
	}

	@Override
	public final void onFileDownloadProgressUpdate(FileDownloadTask task, long contentLength, long completeLength,
			int percent, long speedBytesPerS) {
		try {
			if (task == null) {
				return;
			}
			if (!mFileDownloadTaskSet.contains(task)) {
				if (Debug_SDK.isDownloadLog) {
					Debug_SDK.te(Debug_SDK.mDownloadTag, this, "下载进度通知：下载url:[%s],不是管理器[%s]启动的下载任务,取消处理",
							task.getDestUrl(), this.getClass().getSimpleName());
				}
				return;
			} else {
				if (Debug_SDK.isDownloadLog) {
					Debug_SDK.ti(Debug_SDK.mDownloadTag, this, "下载进度通知：下载url:[%s],是管理器[%s]启动的下载任务,需要处理",
							task.getDestUrl(), this.getClass().getSimpleName());
				}
			}
		} catch (Throwable e) {
			if (Debug_SDK.isDownloadLog) {
				Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
			}
		}

		List<T> lists = getListeners();
		for (int i = 0; i < lists.size(); i++) {
			try {
				T listener = lists.get(i);
				if (listener != null) {
					notifyListener_onFileDownloadProgressUpdate(listener, task, contentLength, completeLength, percent,
							speedBytesPerS);
				}
			} catch (Throwable e) {
				if (Debug_SDK.isDownloadLog) {
					Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
				}
			}
		}
		if(lists.size()==0){
			notifyListener_onFileDownloadProgressUpdate(null, task, contentLength, completeLength, percent,
					speedBytesPerS);
		}
	}

	/**
	 * 通过特殊的webview来获取最终的下载url——成功，那么就启动下载任务
	 */
	@Override
	final public void onFinishGetDownloadFileUrl(FileDownloadWebView webView, FileDownloadTask task, String userAgent,
			String contentDisposition, String mimetype) {
		try {
			if (task != null && task.isAvailable()) {
				task.setStoreFile(getStoreDownloadFile(task, contentDisposition)); // 重要，必须传入最终存储文件
				if (Debug_SDK.isDownloadLog) {
					Debug_SDK.td(Debug_SDK.mDownloadTag, this, "webview 解析重定向成功，准备开启下载");
				}
				if (FinalFileDownloadManager.getInstance().downloadFile(mApplicationContext, task, this)) {
					if (Debug_SDK.isDownloadLog) {
						Debug_SDK.td(Debug_SDK.mDownloadTag, this, "开启下载成功");
					}
					mFileDownloadTaskSet.add(task);
					mMap_Downloading_RawUrl_FileDownloadTask.put(task.getRawUrl(), task);
				}
			}
		} catch (Throwable e) {
			if (Debug_SDK.isDownloadLog) {
				Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
			}
		}
		// 从列表中移除
		mFileDownloadWebViewSet.remove(webView);
	}

	/**
	 * 通过特殊的webview获取最终的url地址失败——超时，那么久需要通知说下载失败了
	 */
	@Override
	public void onGetDownloadFileUrlTimesout_ToNofifyDownloadFailed(FileDownloadWebView webView, FileDownloadTask task) {
		if (mFileDownloadWebViewSet == null) {
			return;
		}
		if (webView == null) {
			return;
		}
		mFileDownloadWebViewSet.remove(webView);
		try {
			Log.d("test","下载地址："+task.getDestUrl());
			onFileDownloadFailed(task);
		} catch (Throwable e) {
			if (Debug_SDK.isDownloadLog) {
				Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
			}
		}

	}

	/**
	 * 时间够之后就自动从webview列表中移除本次webview释放资源
	 */
	@Override
	public void onGetDownloadFileSessionTimeout_JustRemove(FileDownloadWebView webView) {
		if (mFileDownloadWebViewSet == null) {
			return;
		}
		if (webView == null) {
			return;
		}
		mFileDownloadWebViewSet.remove(webView);
	}

	/**
	 * 获取要下载的缓存目录操作类
	 */
	protected abstract FileCacheDirectoryStorer getCacheDirectoryStorer(FileDownloadTask task);

	/**
	 * 获取文件名生成器
	 */
	protected abstract DownloadFileNameListener getFileNameFactory();

	/**
	 * 检查文件后缀名，如果返回true，表示确认为最终链接。否则判定为跳转链接。
	 */
	protected abstract boolean checkSuffix(String url);

	/**
	 * 处理文件开始下载之前文件处于文件锁的事件，如发效果记录等等。
	 */
	protected abstract void onHandleFileDownloadBeforeStart_FileLock(FileDownloadTask task);

	/**
	 * 处理文件开始下载事件，如发效果记录等等。
	 */
	protected abstract void onHandleFileDownloadStart(FileDownloadTask task);

	/**
	 * 在文件下载成功的情况下，可以发送记录或做其他的处理。
	 */
	protected abstract void onHandleFileDownloadSuccess(FileDownloadTask task);

	/**
	 * 在apk下载失败的时候，可以发送记录或做其他处理。
	 */
	protected abstract void onHandleFileDownloadFailed(FileDownloadTask task);

	/**
	 * 在apk下载停止的时候，可以发送记录或做其他处理。
	 */
	protected abstract void onHandleFileDownloadStop(FileDownloadTask task);

	/**
	 * 在apk已经存在的情况下，可以发送记录或做其他处理。
	 */
	protected abstract void onHandleFileAlreadyExist(FileDownloadTask task);

	/**
	 * 子类必须实现，通知监听器：文件开始下载之前，文件处于文件锁的事件。
	 */
	protected abstract void notifyListener_onFileDownloadBeforeStart_FileLock(T listener, FileDownloadTask task);

	/**
	 * 子类必须实现，通知监听器：文件开始下载。
	 */
	protected abstract void notifyListener_onFileDownloadStart(T listener, FileDownloadTask task);

	/**
	 * 子类必须实现，通知监听器文件下载成功
	 */
	protected abstract void notifyListener_onFileDownloadSuccess(T listener, FileDownloadTask task);

	/**
	 * 子类必须实现，通知监听器文件下载失败
	 */
	protected abstract void notifyListener_onFileDownloadFailed(T listener, FileDownloadTask task);

	/**
	 * 子类必须实现，通知监听器文件下载停止
	 */
	protected abstract void notifyListener_onFileDownloadStop(T listener, FileDownloadTask task);

	/**
	 * 子类必须实现，通知监听器文件已经存在。
	 */
	protected abstract void notifyListener_onFileAlreadyExist(T listener, FileDownloadTask task);

	/**
	 * 子类必须实现，通知监听器文件的下载进度
	 */
	protected abstract void notifyListener_onFileDownloadProgressUpdate(T listener, FileDownloadTask task,
			long contentLength, long completeLength, int percent, long speedBytesPerS);

}
