package net.youmi.android.libs.common.download.listener;

import net.youmi.android.libs.common.download.model.FileDownloadTask;

/**
 * 通知各种下载状态间转化
 * 
 * @author CsHeng
 * @author zhitaocai edit on 2014-7-17<br>
 *         添加{@link publishFileDownloadStop}的通知
 * @author zhitaocai edit on 2014-7-18<br>
 *         添加{@link publishFileDownloadBeforeStart_FileLock}的通知
 *         
 * @Date 14-3-28
 * @Time 下午3:53
 */
public interface FileDownloadStatePublisher {

	/**
	 * 进行通知：下载开始
	 */
	public void publishFileDownloadStart(FileDownloadTask task);

	/**
	 * 进行通知：下载成功
	 * 
	 * @param task
	 */
	public void publishFileDownloadSuccess(FileDownloadTask task);

	/**
	 * 进行通知：下载失败
	 * 
	 * @param task
	 */
	public void publishFileDownloadFailed(FileDownloadTask task);

	/**
	 * 进行通知：文件已存在并且可用
	 * 
	 * @param task
	 */
	public void publishFileExistAvailable(FileDownloadTask task);

	/**
	 * 进行通知：进度有变化，由管理器分发进度给其他监听者
	 * 
	 * @param task
	 * @param percent
	 * @param speedBytesPerSecond
	 * @param totalLength
	 * @param completeLength
	 */
	public void publishFileDownloadProgress(FileDownloadTask task, int percent, long speedBytesPerSecond,
	                                        long totalLength, long completeLength);

	/**
	 * 进行通知：下载停止（代码里面调用stopdownload就会到这里）
	 * 
	 * @param task
	 */
	public void publishFileDownloadStop(FileDownloadTask task);

	/**
	 * 进行通知：下载开始之前，文件处于文件锁（如果刚刚调用停止下载的代码之后，立即点击再次重新下载的话，可能就会到这里）
	 * 
	 * @param task
	 */
	public void publishFileDownloadBeforeStart_FileLock(FileDownloadTask task);
}
