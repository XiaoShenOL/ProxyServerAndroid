package net.youmi.android.libs.common.download.state;

import android.content.Context;

import net.youmi.android.libs.common.debug.Debug_SDK;
import net.youmi.android.libs.common.download.FileDownloadConfig;
import net.youmi.android.libs.common.download.basic.FinalFileDownloader;
import net.youmi.android.libs.common.download.listener.FileDownloadStatePublisher;
import net.youmi.android.libs.common.download.model.FileDownloadState;
import net.youmi.android.libs.common.download.model.FileDownloadTask;

import java.io.File;

/**
 * 初始化状态处理逻辑：
 * <ol>
 * <li>检查{@link FileDownloadTask}参数是否正确可用</li>
 * <li>检查最终保存文件及缓存文件是否存在</li>
 * </ol>
 * <hr>
 * 根据检查结果将会切换到以下几个状态：
 * <ul>
 * <li>{@link TaskState_Download_Start 启动下载}</li>
 * <li>{@link TaskState_Observe_Others_Downloading 观察其他进程下载}</li>
 * <li>{@link TaskState_StoreFileAlreadyExist 存储文件已存在}</li>
 * </ul>
 * <hr>
 * 
 * @author jen
 * @author zhitaocai edit on 2014-5-29
 *         <p>
 *         暂存文件由原来的在创建{@link FinalFileDownloader}对象的时候创建改为在开始下载时才创建暂存文件，因此这里取消暂存文件的检查
 * 
 */
public class TaskState_Init extends TaskState {

	public TaskState_Init() {
		super(FileDownloadState.STATE_INIT);
	}

	@Override
	public void handleState(Context context, FinalFileDownloader fileDownloader, FileDownloadTask fileDownloadTask,
			FileDownloadStatePublisher downloadStatePublisher) {
		super.handleState(context, fileDownloader, fileDownloadTask, downloadStatePublisher);
		try {
			// 参数检查
			// 获取下载任务
			// 检查下载任务是否可用
			if (fileDownloadTask == null || (!fileDownloadTask.isAvailable())) {
				// 任务不可用，切换到任务失败状态。
				// ...通知
				// ...切换状态
				if (Debug_SDK.isDownloadLog && isLogOpen) {
					Debug_SDK.dd(mTag, "filedownloadTask任务不可用,切换到失败状态");
				}
				fileDownloader.changeState(FileDownloadState.STATE_DOWNLOAD_FAILED);
				return;
			}

			// 目标存储文件
			File storeFile = fileDownloader.getStoreFile();
			// 目标暂存文件
			File tempFile = fileDownloader.getTempFile();

			if (Debug_SDK.isDownloadLog && isLogOpen) {
				Debug_SDK.de(mTag, "存储文件为空？ %b 暂存文件为空? %b", storeFile == null, tempFile == null);
			}

			// 1、如果两个文件都为null，则判定为失败
			if (storeFile == null && tempFile == null) {
				fileDownloader.changeState(FileDownloadState.STATE_DOWNLOAD_FAILED);
				return;
			}

			// 2、到这一步就表明，暂存文件和实际文件最少有一个不为空
			// 2.1、检查目标存储文件是否存在
			if (storeFile != null) {
				if (storeFile.exists()) {
					if (Debug_SDK.isDownloadLog && isLogOpen) {
						Debug_SDK.dd(mTag, "存储文件存在%s\n", storeFile.getPath());
					}
					// 目标存储文件存在，切换到文件已存在状态。
					fileDownloader.changeState(FileDownloadState.STATE_ALREADY_EXIST);
					return;
				}
			}

			// 2.2、检查暂存文件是否存在
			if (tempFile != null) {
				if (tempFile.exists()) {
					// 如果是sdk使用，就需要开启下载前的多进程检查
					if (FileDownloadConfig.PRODUCT_TYPE == 2) {
						if (Debug_SDK.isDownloadLog && isLogOpen) {
							Debug_SDK.dd(mTag, "暂存文件存在%s\n当前产品类型为sdk，开始检查暂存文件是否处于文件锁中", tempFile.getPath());
						}
						// 检查暂存文件是否被处于文件锁中
						if (fileDownloader.isTempFileUsingByOtherProgress()) {
							if (Debug_SDK.isDownloadLog && isLogOpen) {
								Debug_SDK.de(mTag, "暂存文件处于文件锁中,先回调下载前文件处于文件锁的监听");
							}
							downloadStatePublisher.publishFileDownloadBeforeStart_FileLock(fileDownloadTask);
							fileDownloader.changeState(FileDownloadState.STATE_OBSERVER_OTHERS_DOWNLOADING);
							return;
						}
						if (Debug_SDK.isDownloadLog && isLogOpen) {
							Debug_SDK.de(mTag, "暂存文件没有处于文件锁中");
						}
					} else {
						if (Debug_SDK.isDownloadLog && isLogOpen) {
							Debug_SDK.dd(mTag, "暂存文件存在%s\n当前产品类型为非sdk，不检查文件锁", tempFile.getPath());
						}
					}
				}
			}

			fileDownloader.changeState(FileDownloadState.STATE_DOWNLOAD_START);

		} catch (Throwable e) {
			if (Debug_SDK.isDownloadLog && isLogOpen) {
				Debug_SDK.dd(mTag, e);
			}
		}

	}

}
