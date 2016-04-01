package net.youmi.android.libs.common.download.listener;

import net.youmi.android.libs.common.download.model.FileDownloadTask;

/**
 * 
 * @author zhitaocai edit on 2014-7-17<br>
 *         添加{@link onFileDownloadStop 下载停止}的监听
 * 
 */
public interface FileDownloadResultListener {

	public void onFileDownloadSuccess(FileDownloadTask task);

	public void onFileDownloadFailed(FileDownloadTask task);

	public void onFileAlreadyExist(FileDownloadTask task);

	public void onFileDownloadStop(FileDownloadTask task);
}
