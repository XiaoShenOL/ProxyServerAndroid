package net.youmi.android.libs.common.download;

import android.content.Context;

import net.youmi.android.libs.common.debug.Debug_SDK;
import net.youmi.android.libs.common.download.listener.ApkDownloadListener;
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
 * 指定的url除非不带查询串并且以.apk结尾，否则都会判定为可跳转型链接，这时候会通过一个特殊的webview进行加载并提取出下载的最终url。</li>
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
public abstract class BaseApkDownloadManager extends BaseRedirectAbleDownloadManager<ApkDownloadListener> {

	public BaseApkDownloadManager(Context context) {
		super(context);
	}

	/**
	 * 通过检查url的后缀名来判定是否为最终下载链接。<br/>
	 * 返回值:true,表示是最终下载链接，则不再需要使用webview进行链接预加载。<br/>
	 * 否则返回false。
	 */
	@Override
	protected boolean checkSuffix(String url) {
		try {
			return url.trim().toLowerCase().endsWith(".apk");
		} catch (Throwable e) {
			if (Debug_SDK.isDownloadLog) {
				Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
			}
		}
		return false;
	}
	@Override
	protected final void onHandleFileDownloadBeforeStart_FileLock(FileDownloadTask task) {
		onHandleApkDownloadBeforeStart_FileLock(task);
	}

	@Override
	protected final void onHandleFileDownloadStart(FileDownloadTask task) {
		onHandleApkDownloadStart(task);
	}

	@Override
	protected final void onHandleFileDownloadSuccess(FileDownloadTask task) {
		onHandleApkDownloadSuccess(task);
	}

	@Override
	protected final void onHandleFileDownloadFailed(FileDownloadTask task) {
		onHandleApkDownloadFailed(task);
	}

	@Override
	protected final void onHandleFileDownloadStop(FileDownloadTask task) {
		onHandleApkDownloadStop(task);
	}

	@Override
	protected final void onHandleFileAlreadyExist(FileDownloadTask task) {
		onHandleApkAlreadyExist(task);
	}
	
	@Override
	protected void notifyListener_onFileDownloadBeforeStart_FileLock(ApkDownloadListener listener, FileDownloadTask task) {
		listener.onApkDownloadBeforeStart_FileLock(task);
	}

	@Override
	protected void notifyListener_onFileDownloadStart(ApkDownloadListener listener, FileDownloadTask task) {
		listener.onApkDownloadStart(task);
	}

	@Override
	protected void notifyListener_onFileDownloadSuccess(ApkDownloadListener listener, FileDownloadTask task) {
		listener.onApkDownloadSuccess(task);
	}

	@Override
	protected void notifyListener_onFileDownloadFailed(ApkDownloadListener listener, FileDownloadTask task) {
		listener.onApkDownloadFailed(task);
	}
	
	@Override
	protected void notifyListener_onFileDownloadStop(ApkDownloadListener listener, FileDownloadTask task) {
		listener.onApkDownloadStop(task);
	}

	@Override
	protected void notifyListener_onFileDownloadProgressUpdate(ApkDownloadListener listener, FileDownloadTask task,
			long contentLength, long completeLength, int percent, long speedBytesPerS) {
		listener.onApkDownloadProgressUpdate(task, contentLength, completeLength, percent, speedBytesPerS);
	}

	@Override
	protected void notifyListener_onFileAlreadyExist(ApkDownloadListener listener, FileDownloadTask task) {
		listener.onApkDownloadSuccess(task);
	}

	/**
	 * 子类必须实现：处理开始下载之前，文件处于文件锁的通知，可以做发送效果记录等等的操作。
	 * 
	 * @param task
	 */
	protected abstract void onHandleApkDownloadBeforeStart_FileLock(FileDownloadTask task);
	
	/**
	 * 子类必须实现：处理下载开始通知，可以做发送效果记录等等的操作。
	 * 
	 * @param task
	 */
	protected abstract void onHandleApkDownloadStart(FileDownloadTask task);

	/**
	 * 发送效果记录，启动app安装，还有其他的通知等等
	 * 
	 * @param task
	 */
	protected abstract void onHandleApkDownloadSuccess(FileDownloadTask task);

	/**
	 * 发送效果，还有其他的通知等等
	 * 
	 * @param task
	 */
	protected abstract void onHandleApkDownloadFailed(FileDownloadTask task);

	/**
	 * 发送效果，还有其他的通知等等
	 * 
	 * @param task
	 */
	protected abstract void onHandleApkDownloadStop(FileDownloadTask task);

	/**
	 * 发送效果记录，启动app安装，还有其他的通知等等。
	 * 
	 * @param task
	 */
	protected abstract void onHandleApkAlreadyExist(FileDownloadTask task);

}
