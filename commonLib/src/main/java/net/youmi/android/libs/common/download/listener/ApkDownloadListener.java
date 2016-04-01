package net.youmi.android.libs.common.download.listener;

import net.youmi.android.libs.common.download.model.FileDownloadTask;

/**
 * 
 * @author zhitaocai edit on 2014-7-17 <br>
 *         添加{@link onApkDownloadStop}的监听
 * @author zhitaocai edit on 2014-7-18 <br>
 *         添加下载开始之前，文件处于文件锁的回调（如果刚刚调用停止下载的代码之后，立即点击再次重新下载的话，可能就会到这里） {@link onApkDownloadBeforeStart_FileLock}
 * 
 */
public interface ApkDownloadListener {

	public void onApkDownloadBeforeStart_FileLock(FileDownloadTask task);

	public void onApkDownloadStart(FileDownloadTask task);

	public void onApkDownloadSuccess(FileDownloadTask task);

	public void onApkDownloadFailed(FileDownloadTask task);

	public void onApkDownloadStop(FileDownloadTask task);

	/**
	 * 
	 * @param task
	 * @param contentLength
	 * @param completeLength
	 *            已经完成的长度
	 * @param percent
	 *            当前完成百分比
	 * @param speedBytesPerS
	 *            当前下载速度(单位： 字节/秒)
	 */
	public void onApkDownloadProgressUpdate(FileDownloadTask task, long contentLength, long completeLength,
	                                        int percent, long speedBytesPerS);

	public void onApkInstallSuccess(int rawUrlHashCode);

}
