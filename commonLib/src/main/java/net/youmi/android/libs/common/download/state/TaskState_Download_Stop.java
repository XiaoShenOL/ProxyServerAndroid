package net.youmi.android.libs.common.download.state;

import android.content.Context;

import net.youmi.android.libs.common.debug.Debug_SDK;
import net.youmi.android.libs.common.download.basic.FinalFileDownloader;
import net.youmi.android.libs.common.download.listener.FileDownloadStatePublisher;
import net.youmi.android.libs.common.download.model.FileDownloadState;
import net.youmi.android.libs.common.download.model.FileDownloadTask;

/**
 * 
 * @author zhitaocai create on 2014-7-17
 * 
 */
public class TaskState_Download_Stop extends TaskState {

	public TaskState_Download_Stop() {
		super(FileDownloadState.STATE_DOWNLOAD_STOP);
	}

	@Override
	public void handleState(Context context, FinalFileDownloader fileDownloader, FileDownloadTask fileDownloadTask,
			FileDownloadStatePublisher downloadStatePublisher) {
		super.handleState(context, fileDownloader, fileDownloadTask, downloadStatePublisher);
		try {
			downloadStatePublisher.publishFileDownloadStop(fileDownloadTask);
		} catch (Throwable e) {
			if (Debug_SDK.isDownloadLog && isLogOpen) {
				Debug_SDK.de(mTag, e);
			}
		}
	}
}
