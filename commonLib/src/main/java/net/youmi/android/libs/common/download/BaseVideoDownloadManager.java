package net.youmi.android.libs.common.download;

import android.content.Context;

import net.youmi.android.libs.common.debug.Debug_SDK;
import net.youmi.android.libs.common.download.listener.VideoDownloadListener;
import net.youmi.android.libs.common.download.model.FileDownloadTask;

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
 * 指定的url除非不带查询串并且以[.mp4|.avi|.rmvb]结尾，否则都会判定为可跳转型链接， 这时候会通过一个特殊的webview进行加载并提取出下载的最终url。</li>
 * <li>
 * 文件名: <br/>
 * 从上面的步骤得到的最终下载url会作一个md5处理，得到32位hex字符串，这个字符串将作为下载文件的名字。</li>
 * </ol>
 * 
 * 注意，sdk必须继承该类，并使用单例进行apk的下载管理。子类应该考虑对下载过程中的步骤进行效果记录的发送。<br/>
 * 该管理器只负责完成下载流程，之后的启动安装流程并不进行处理。子类需要对后续操作进行补充处理。
 * 
 * @author jen
 * @since 2012-11-22
 * @author zhitaocai edit on 2014-7-16
 */
public abstract class BaseVideoDownloadManager extends BaseRedirectAbleDownloadManager<VideoDownloadListener> {

	public BaseVideoDownloadManager(Context context) {
		super(context);
	}

	/**
	 * 通过url的后缀判定是否为视频文件，如果是，则返回true
	 */
	@Override
	protected boolean checkSuffix(String url) {
		try {
			String u = url.trim().toLowerCase();
			return ((u.endsWith(".mp4")) || (u.endsWith(".rmvb")) || (u.endsWith(".rm")));

		} catch (Throwable e) {
			if (Debug_SDK.isDownloadLog) {
				Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
			}
		}
		return false;
	}
	
	@Override
	protected final void onHandleFileDownloadBeforeStart_FileLock(FileDownloadTask task) {
		onHandleVideoDownloadBeforeStart_FileLock(task);
	}

	@Override
	protected final void onHandleFileDownloadStart(FileDownloadTask task) {
		onHandleVideoDownloadStart(task);
	}

	@Override
	protected final void onHandleFileDownloadSuccess(FileDownloadTask task) {
		onHandleVideoDownloadSuccess(task);
	}

	@Override
	protected final void onHandleFileDownloadFailed(FileDownloadTask task) {
		onHandleVideoDownloadFailed(task);
	}

	@Override
	protected final void onHandleFileDownloadStop(FileDownloadTask task) {
		onHandleVideoDownloadStop(task);
	}

	@Override
	protected final void onHandleFileAlreadyExist(FileDownloadTask task) {
		onHandleVideoAlreadyExist(task);
	}

	@Override
	protected void notifyListener_onFileDownloadBeforeStart_FileLock(VideoDownloadListener listener, FileDownloadTask task) {
		listener.onVideoDownloadBeforeStart_FileLock(task);
	}
	
	@Override
	protected void notifyListener_onFileDownloadStart(VideoDownloadListener listener, FileDownloadTask task) {
		listener.onVideoDownloadStart(task);
	}

	@Override
	protected void notifyListener_onFileDownloadProgressUpdate(VideoDownloadListener listener, FileDownloadTask task,
			long contentLength, long completeLength, int percent, long speedBytesPerS) {
		listener.onVideoDownloadProgressUpdate(task, contentLength, completeLength, percent, speedBytesPerS);
	}

	@Override
	protected void notifyListener_onFileDownloadSuccess(VideoDownloadListener listener, FileDownloadTask task) {
		listener.onVideoDownloadSuccess(task);
	}

	@Override
	protected void notifyListener_onFileDownloadFailed(VideoDownloadListener listener, FileDownloadTask task) {
		listener.onVideoDownloadFailed(task);
	}

	@Override
	protected void notifyListener_onFileDownloadStop(VideoDownloadListener listener, FileDownloadTask task) {
		listener.onVideoDownloadStop(task);
	}

	@Override
	protected void notifyListener_onFileAlreadyExist(VideoDownloadListener listener, FileDownloadTask task) {
		listener.onVideoDownloadSuccess(task);
	}
	
	/**
	 * 子类必须实现：处理下载开始时，文件处于文件锁的通知，可以做发送效果记录等等的操作。
	 * 
	 * @param task
	 */
	protected abstract void onHandleVideoDownloadBeforeStart_FileLock(FileDownloadTask task);

	/**
	 * 子类必须实现：处理下载开始通知，可以做发送效果记录等等的操作。
	 * 
	 * @param task
	 */
	protected abstract void onHandleVideoDownloadStart(FileDownloadTask task);

	/**
	 * 子类必须实现：发送效果记录，启动播放，还有其他的通知等等
	 * 
	 * @param task
	 */
	protected abstract void onHandleVideoDownloadSuccess(FileDownloadTask task);

	/**
	 * 子类必须实现：发送效果，还有其他的通知等等
	 */
	protected abstract void onHandleVideoDownloadFailed(FileDownloadTask task);

	/**
	 * 子类必须实现：发送效果，还有其他的通知等等
	 */
	protected abstract void onHandleVideoDownloadStop(FileDownloadTask task);

	/**
	 * 子类必须实现：发送效果记录，启动视频播放，还有其他的通知等等。
	 * 
	 * @param task
	 */
	protected abstract void onHandleVideoAlreadyExist(FileDownloadTask task);
}
