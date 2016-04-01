package net.youmi.android.libs.common.download.state;

import android.content.Context;

import net.youmi.android.libs.common.debug.Debug_SDK;
import net.youmi.android.libs.common.download.basic.FinalFileDownloader;
import net.youmi.android.libs.common.download.listener.FileDownloadStatePublisher;
import net.youmi.android.libs.common.download.model.FileDownloadState;
import net.youmi.android.libs.common.download.model.FileDownloadTask;

public class TaskState_Download_Failed extends TaskState {

	public TaskState_Download_Failed() {
		super(FileDownloadState.STATE_DOWNLOAD_FAILED);
	}

	@Override
	public void handleState(Context context, FinalFileDownloader fileDownloader, FileDownloadTask fileDownloadTask,
			FileDownloadStatePublisher downloadStatePublisher) {
		super.handleState(context, fileDownloader, fileDownloadTask, downloadStatePublisher);
		try {
			downloadStatePublisher.publishFileDownloadFailed(fileDownloadTask);
		} catch (Throwable e) {
			if (Debug_SDK.isDownloadLog && isLogOpen) {
				Debug_SDK.de(mTag, e);
			}
		}
	}
}
