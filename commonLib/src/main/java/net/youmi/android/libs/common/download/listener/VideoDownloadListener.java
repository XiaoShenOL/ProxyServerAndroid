package net.youmi.android.libs.common.download.listener;

import net.youmi.android.libs.common.download.model.FileDownloadTask;

/**
 * 
 * @author zhitaocai edit on 2014-7-17 <br>
 *         添加{@link onVideoDownloadStop}的监听
 * @author zhitaocai edit on 2014-7-18 <br>
 *         添加下载开始之前，文件处于文件锁的回调（如果刚刚调用停止下载的代码之后，立即点击再次重新下载的话，可能就会到这里） {@link onVideoDownloadProgressUpdate}
 * 
 */
public interface VideoDownloadListener {

	public void onVideoDownloadSuccess(FileDownloadTask task);

	public void onVideoDownloadFailed(FileDownloadTask task);

	public void onVideoDownloadStop(FileDownloadTask task);

	public void onVideoDownloadStart(FileDownloadTask task);

	public void onVideoDownloadProgressUpdate(FileDownloadTask task, long contentLength, long completeLength,
	                                          int percent, long speedBytesPerS);

	public void onVideoDownloadBeforeStart_FileLock(FileDownloadTask task);

}
