package net.youmi.android.libs.common.download.listener;

import android.content.Context;

import net.youmi.android.libs.common.download.basic.FinalFileDownloader;
import net.youmi.android.libs.common.download.model.FileDownloadTask;

public interface FileDownloadStateHandler {

	/**
	 * 根据不同的状态处理不同的下载
	 * 
	 * @param context
	 * @param fileDownloader
	 *            下载线程
	 * @param fileDownloadTask
	 *            下载任务描述
	 * @param downloadStatePublisher
	 *            下载状态通知器
	 */
	void handleState(Context context, FinalFileDownloader fileDownloader, FileDownloadTask fileDownloadTask,
	                 FileDownloadStatePublisher downloadStatePublisher);

}
