package net.youmi.android.libs.common.download.listener;

import net.youmi.android.libs.common.download.model.FileDownloadTask;

/**
 * 
 * @author zhitaocai edit on 2014-7-18 <br>
 *         添加刚刚开始下载的时候，文件还处于文件锁的回调 {@link onFileDownloadBeforeStart_FileLock}
 * 
 */
public interface FileDownloadListener extends FileDownloadResultListener {

	public void onFileDownloadProgressUpdate(FileDownloadTask task, long contentLength, long completeLength,
	                                         int percent, long speedBytesPerS);

	public void onFileDownloadStart(FileDownloadTask task);

	public void onFileDownloadBeforeStart_FileLock(FileDownloadTask task);
}
