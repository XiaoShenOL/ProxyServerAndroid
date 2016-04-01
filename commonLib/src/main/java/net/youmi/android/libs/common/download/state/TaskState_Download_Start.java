package net.youmi.android.libs.common.download.state;

import android.content.Context;

import net.youmi.android.libs.common.debug.Debug_SDK;
import net.youmi.android.libs.common.download.basic.FinalFileDownloader;
import net.youmi.android.libs.common.download.listener.FileDownloadStatePublisher;
import net.youmi.android.libs.common.download.model.FileDownloadState;
import net.youmi.android.libs.common.download.model.FileDownloadTask;

/**
 * 启动下载任务处理逻辑：启动下载
 * 
 * <hr>
 * <strong>注意之前的状态处理结果以及有可能触发到此状态的可能：</strong>
 * <ol>
 * <li>Init里面Temp文件检查状态，检查结果是：缓存文件不存在或已经超时</li>
 * <li>OtherObserve监控状态，检查结果是：缓存文件不存在或已经超时</li>
 * </ol>
 * <hr>
 * 根据检查结果将会切换到以下几个状态：
 * <ul>
 * <li>{@link TaskState_Downloading 下载中}</li>
 * <li>{@link TaskState_Download_Failed 下载失败}</li>
 * </ul>
 * <hr>
 * 
 * @author jen
 * 
 */
public class TaskState_Download_Start extends TaskState {

	public TaskState_Download_Start() {
		super(FileDownloadState.STATE_DOWNLOAD_START);
	}

	@Override
	public void handleState(Context context, FinalFileDownloader fileDownloader, FileDownloadTask fileDownloadTask,
			FileDownloadStatePublisher downloadStatePublisher) {
		super.handleState(context, fileDownloader, fileDownloadTask, downloadStatePublisher);

		try {
			// 注意之前的状态处理结果以及有可能触发到此状态的可能：
			// 1.Temp文件检查状态，检查结果是：缓存文件不存在或已经超时
			// 2.OtherObserve监控状态，检查结果是：缓存文件不存在或已经超时
			// 开始下载任务

			if (fileDownloader.startDownload()) {
				// 通知下载开始。
				downloadStatePublisher.publishFileDownloadStart(fileDownloadTask);

				// 跳转到下载过程监控状态
				fileDownloader.changeState(FileDownloadState.STATE_DOWNLOADING);

			} else {
				// 跳转到下载失败状态
				fileDownloader.changeState(FileDownloadState.STATE_DOWNLOAD_FAILED);
			}

		} catch (Throwable e) {
			if (Debug_SDK.isDownloadLog && isLogOpen) {
				Debug_SDK.de(mTag, e);
			}
		}
	}

}
