package net.youmi.android.libs.common.download.state;

import android.content.Context;

import net.youmi.android.libs.common.debug.Debug_SDK;
import net.youmi.android.libs.common.download.FileDownloadConfig;
import net.youmi.android.libs.common.download.basic.BasicFileDownloaderHandler;
import net.youmi.android.libs.common.download.basic.FinalFileDownloader;
import net.youmi.android.libs.common.download.listener.FileDownloadStatePublisher;
import net.youmi.android.libs.common.download.model.FileDownloadState;
import net.youmi.android.libs.common.download.model.FileDownloadTask;
import net.youmi.android.libs.common.network.Util_Network_HttpUtil;

import java.io.File;

/**
 * 观察下载状态，通过文件的变动来观察，这是在多进程的情况下处理的
 * 
 * @author jen
 * @author zhitaocai edit on 2014-6-3
 * 
 */
public class TaskState_Observe_Others_Downloading extends TaskState {

	private long mLastRecordCompleteLength = 0;

	/**
	 * 获取长度的次数，如果失败3次，则取消
	 */
	private int mGetContentLengthTims = 0;

	private boolean mIsRunning = true;

	public TaskState_Observe_Others_Downloading() {
		super(FileDownloadState.STATE_OBSERVER_OTHERS_DOWNLOADING);
	}

	@Override
	public void handleState(Context context, FinalFileDownloader fileDownloader, FileDownloadTask fileDownloadTask,
			FileDownloadStatePublisher downloadStatePublisher) {
		super.handleState(context, fileDownloader, fileDownloadTask, downloadStatePublisher);

		try {
			File tempFile = fileDownloader.getTempFile();
			File storeFile = fileDownloader.getStoreFile();

			while (mIsRunning) {

				try {
					// 重要，
					// 1、先看看下载的线程还在不在
					// 2、然后看看下载的线程是不是还在运行
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

					if (storeFile != null) {
						if (storeFile.exists()) {
							if (Debug_SDK.isDownloadLog && isLogOpen) {
								Debug_SDK.dd(mTag, "存储文件已存在 %s", storeFile.getPath());
							}
							// 切换到文件已经存在的状态。
							fileDownloader.changeState(FileDownloadState.STATE_ALREADY_EXIST);
							break;
						}
					}

					// // 这里需要判断暂存文件是否为空，当另一个进程完成对tempFile的写入之后（即下载完成），会重命名tempFile,所以需要判断一下tempfile是否为空
					// if (tempFile == null) {
					// if (Debug_SDK.isDownloadLog && isLogOpen) {
					// Debug_SDK.dd(mTag, "暂存文件没有被使用 %s");
					// }
					// // 切换到初始化状态然后进行配置检查之后进行下载
					// fileDownloader.changeState(FileDownloadState.STATE_INIT);
					// break;
					// }

					// 再次检查缓存文件是否被其他进程使用，或者暂存文件是否为空
					if (!fileDownloader.isTempFileUsingByOtherProgress()) {
						if (Debug_SDK.isDownloadLog && isLogOpen) {
							Debug_SDK.dd(mTag, "暂存文件没有被其他进程使用");
						}
						// 切换到初始化状态然后进行配置检查之后进行下载
						fileDownloader.changeState(FileDownloadState.STATE_INIT);
						break;
					}
					if (Debug_SDK.isDownloadLog && isLogOpen) {
						Debug_SDK.de(mTag, "暂存文件处于文件锁中，将持续监听下载进度 ");
					}

					// 下面的情况是，其他进程的下载仍在继续，因此只能通过观察文件的状态来监控(共享)其下载进度。
					// 总长度
					long contentLength = fileDownloader.getDownloadTask().getContentLength();
					// 如果总长度小于0
					if (contentLength <= 0) {
						// 看看还可不可以从网络获取总长度
						if (mGetContentLengthTims < 3) {
							// 从网络获取文件总长度, 并缓存起来
							contentLength = Util_Network_HttpUtil.getContentLength(context, fileDownloader
									.getDownloadTask().getDestUrl());
							fileDownloader.getDownloadTask().setContentLength(contentLength);
							if (Debug_SDK.isDownloadLog && isLogOpen) {
								Debug_SDK.dd(mTag, "第%d次,从网络中获取文件长度", mGetContentLengthTims + 1);
							}
							mGetContentLengthTims++;
						}
					}

					// 这里需要判断暂存文件是否为空，当另一个进程完成对tempFile的写入之后（即下载完成），会重命名tempFile而导致tempFile不见了
					// 所以需要判断一下tempfile是否为空,不然在获取tempFile的length的时候会出现错误
					if (tempFile == null) {
						// 切换到初始化状态然后进行配置检查之后进行下载
						fileDownloader.changeState(FileDownloadState.STATE_INIT);
						break;
					} else {

						if (tempFile.exists()) {

							// 已经完成长度
							long completeLength = tempFile.length();

							// 已完成百分比
							int percent = 0;
							if (contentLength > 0) {
								percent = (int) ((completeLength * 100) / contentLength);
							}

							// 增长量
							long increate = completeLength - mLastRecordCompleteLength;// 算出增长量
							// 速度： B/S
							long speedBytesPerSecond = (increate * 1000) / FileDownloadConfig.INTERVAL_PROGRESS_NOTIFY;// 算出下载速度

							mLastRecordCompleteLength = completeLength;

							if (Debug_SDK.isDownloadLog && isLogOpen) {
								Debug_SDK.de(mTag, "观察其他进程的下载进度 百分之 %d ", percent);
							}
							// 通知下载进度
							downloadStatePublisher.publishFileDownloadProgress(fileDownloadTask, percent,
									speedBytesPerSecond, contentLength, completeLength);
						}
					}
				} catch (Throwable e) {
					if (Debug_SDK.isDownloadLog && isLogOpen) {
						Debug_SDK.dd(mTag, e);
					}
				}
				try {
					Thread.sleep(FileDownloadConfig.INTERVAL_PROGRESS_NOTIFY);
				} catch (Throwable e) {
					if (Debug_SDK.isDownloadLog && isLogOpen) {
						Debug_SDK.dd(mTag, e);
					}
				}
			}
		} catch (Throwable e) {
			if (Debug_SDK.isDownloadLog && isLogOpen) {
				Debug_SDK.dd(mTag, e);
			}
		}
	}
}
