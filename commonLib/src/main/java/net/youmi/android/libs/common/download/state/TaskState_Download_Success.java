package net.youmi.android.libs.common.download.state;

import android.content.Context;

import net.youmi.android.libs.common.debug.Debug_SDK;
import net.youmi.android.libs.common.download.basic.FinalFileDownloader;
import net.youmi.android.libs.common.download.listener.FileAvailableChecker;
import net.youmi.android.libs.common.download.listener.FileDownloadStatePublisher;
import net.youmi.android.libs.common.download.model.FileDownloadState;
import net.youmi.android.libs.common.download.model.FileDownloadTask;

import java.io.File;

public class TaskState_Download_Success extends TaskState {

	public TaskState_Download_Success() {
		super(FileDownloadState.STATE_DOWNLOAD_SUCCESS);
	}

	@Override
	public void handleState(Context context, FinalFileDownloader fileDownloader, FileDownloadTask fileDownloadTask,
			FileDownloadStatePublisher downloadStatePublisher) {
		super.handleState(context, fileDownloader, fileDownloadTask, downloadStatePublisher);
		try {

			File storeFile = fileDownloader.getStoreFile();
			// 文件为空
			if (storeFile == null) {
				fileDownloader.changeState(FileDownloadState.STATE_DOWNLOAD_FAILED);
				return;
			}
			// 文件不存在
			if (!storeFile.exists()) {
				fileDownloader.changeState(FileDownloadState.STATE_DOWNLOAD_FAILED);
				return;
			}

			try {
				if (fileDownloadTask.getContentLength() <= 0) {
					fileDownloadTask.setContentLength(fileDownloader.getDownloadHandler().getContentLength());// 设置长度
				}
			} catch (Throwable e) {
				if (Debug_SDK.isDownloadLog && isLogOpen) {
					Debug_SDK.de(mTag, e);
				}
			}

			FileAvailableChecker mFileChecker = fileDownloader.getFileAvailableChecker();

			if (mFileChecker == null) {
				// 文件不需要检查，直接通知
				// 下载成功(因为检查程序不存在，所以不检查)
				// 通知内容是：文件已经成功下载
				downloadStatePublisher.publishFileDownloadSuccess(fileDownloadTask);
				return;
			}

			if (mFileChecker.checkFileAvailable(fileDownloadTask)) {
				// 文件可用，进行通知
				downloadStatePublisher.publishFileDownloadSuccess(fileDownloadTask);
				return;
			} else {
				// 文件不可用，删除文件，结束流程
				storeFile.delete();

				// 切换到下载失败状态
				fileDownloader.changeState(FileDownloadState.STATE_DOWNLOAD_FAILED);
				return;
			}

		} catch (Throwable e) {
			if (Debug_SDK.isDownloadLog && isLogOpen) {
				Debug_SDK.de(mTag, e);
			}

		}

	}

}
