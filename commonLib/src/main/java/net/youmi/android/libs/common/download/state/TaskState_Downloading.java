package net.youmi.android.libs.common.download.state;

import android.content.Context;

import net.youmi.android.libs.common.debug.Debug_SDK;
import net.youmi.android.libs.common.download.FileDownloadConfig;
import net.youmi.android.libs.common.download.basic.BasicFileDownloaderHandler;
import net.youmi.android.libs.common.download.basic.FinalFileDownloader;
import net.youmi.android.libs.common.download.listener.FileDownloadStatePublisher;
import net.youmi.android.libs.common.download.model.FileDownloadState;
import net.youmi.android.libs.common.download.model.FileDownloadTask;

/**
 * 正在下载中的状态处理逻辑：负责将文件的下载进度进行通知
 * <p>
 * 根据检查结果将会切换到以下几个状态：
 * <ul>
 * <li>{@link TaskState_Download_Success 下载成功}</li>
 * <li>{@link TaskState_Download_Failed 下载失败}</li>
 * </ul>
 * <hr>
 * 
 * @author jen
 */
public class TaskState_Downloading extends TaskState {

	public TaskState_Downloading() {
		super(FileDownloadState.STATE_DOWNLOADING);
	}

	private long mLastRecordCompleteLength = 0;
	private boolean mIsRunning = true;
	long mLastNotifyTimeMs;

	@Override
	public void handleState(Context context, FinalFileDownloader fileDownloader, FileDownloadTask fileDownloadTask,
			FileDownloadStatePublisher downloadStatePublisher) {
		super.handleState(context, fileDownloader, fileDownloadTask, downloadStatePublisher);
		try {

			while (mIsRunning) {
				try {
					BasicFileDownloaderHandler downloader = fileDownloader.getDownloadHandler();

					if (downloader == null) {
						if (Debug_SDK.isDownloadLog && isLogOpen) {
							Debug_SDK.dd(mTag, "BasicFileDownloaderHandler为空,准备切换到下载失败状态");
						}
						// 停止状态
						mIsRunning = false;
						// 跳转到下载失败
						fileDownloader.changeState(FileDownloadState.STATE_DOWNLOAD_FAILED);
						break;
					}

					// 如果已经不在运行了就判断结果
					if (!downloader.isRunning()) {
						// 先标记不在运行了，然后根据结果进行状态跳转
						mIsRunning = false;
						// 下载成功
						if (downloader.isSuccess()) {
							if (Debug_SDK.isDownloadLog && isLogOpen) {
								Debug_SDK.dd(mTag, "下载完成,准备切换到下载完成状态");
							}
							// 切换到下载完成的状态
							fileDownloader.changeState(FileDownloadState.STATE_DOWNLOAD_SUCCESS);
							break;
						}

						// 下载停止
						if (downloader.isStop()) {
							if (Debug_SDK.isDownloadLog && isLogOpen) {
								Debug_SDK.dd(mTag, "下载停止,准备切换到下载停止状态");
							}
							// 切换到下载完成的状态
							fileDownloader.changeState(FileDownloadState.STATE_DOWNLOAD_STOP);
							break;
						}

						// 下载失败
						if (downloader.isFailed()) {
							if (Debug_SDK.isDownloadLog && isLogOpen) {
								Debug_SDK.dd(mTag, "下载失败,准备切换到下载失败状态");
							}
							// 切换到下载完成的状态
							fileDownloader.changeState(FileDownloadState.STATE_DOWNLOAD_FAILED);
							break;
						}
					}

					// 到这里就标识还在下载还在进行中
					if (System.currentTimeMillis() - mLastNotifyTimeMs < FileDownloadConfig.INTERVAL_PROGRESS_NOTIFY) {
						continue;
					}

					// 总长度
					long contentLength = downloader.getContentLength();

					// 已经完成长度
					long completeLength = downloader.getCompleteLength();

					// 已完成百分比
					int percent = downloader.getPercent();

					// 增长量
					long increate = completeLength - mLastRecordCompleteLength;// 算出增长量
					// 速度： B/S
					long speedBytesPerSecond = (increate * 1000) / FileDownloadConfig.INTERVAL_PROGRESS_NOTIFY;// 算出下载速度

					// 通知下载进度
					downloadStatePublisher.publishFileDownloadProgress(fileDownloadTask, percent, speedBytesPerSecond,
							contentLength, completeLength);
					mLastRecordCompleteLength = completeLength;
					mLastNotifyTimeMs = System.currentTimeMillis();

				} catch (Throwable e) {
					if (Debug_SDK.isDownloadLog && isLogOpen) {
						Debug_SDK.de(mTag, e);
					}
				}
			}
		} catch (Throwable e) {
			if (Debug_SDK.isDownloadLog && isLogOpen) {
				Debug_SDK.de(mTag, e);
			}
		}
	}
}
