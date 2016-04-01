package net.youmi.android.libs.common.download.state;

import android.content.Context;

import net.youmi.android.libs.common.debug.Debug_SDK;
import net.youmi.android.libs.common.download.basic.FinalFileDownloader;
import net.youmi.android.libs.common.download.listener.FileAvailableChecker;
import net.youmi.android.libs.common.download.listener.FileDownloadStatePublisher;
import net.youmi.android.libs.common.download.model.FileDownloadState;
import net.youmi.android.libs.common.download.model.FileDownloadTask;
import net.youmi.android.libs.common.network.Util_Network_HttpUtil;

import java.io.File;

public class TaskState_StoreFileAlreadyExist extends TaskState {

	public TaskState_StoreFileAlreadyExist() {
		super(FileDownloadState.STATE_ALREADY_EXIST);
	}

	@Override
	public void handleState(Context context, FinalFileDownloader fileDownloader, FileDownloadTask fileDownloadTask,
			FileDownloadStatePublisher downloadStatePublisher) {
		super.handleState(context, fileDownloader, fileDownloadTask, downloadStatePublisher);
		try {

			final File storeFile = fileDownloader.getStoreFile();
			final FileAvailableChecker fileAvailableChecker = fileDownloader.getFileAvailableChecker();

			// 注意，文件必须存在才能到达这个状态
			// 只有初始化状态的检查以及对其他下载者的观察状态结果才能到达此状态

			if (fileAvailableChecker == null) {
				// 文件不需要检查，直接通知
				// 下载成功(因为检查程序不存在，所以不检查)
				// 通知内容是：文件已经存在
				downloadStatePublisher.publishFileExistAvailable(fileDownloadTask);
				return;
			}

			boolean isFileOk = true;// 默认为true

			// 判断是否需要联网对比文件长度
			if (fileAvailableChecker.isNeedToCheckContentLengthByNetwork(fileDownloadTask)) {

				long l1 = storeFile.length();
				long l2 = Util_Network_HttpUtil.getContentLength(context, fileDownloadTask.getDestUrl());
				if (fileDownloadTask.getContentLength() <= 0) {
					fileDownloadTask.setContentLength(l2);// 这里设置task的contentLength，因为下一步的检查可能会用到
				}

				if (l1 != l2) {
					isFileOk = false;// 由于长度不一致，因此判断为文件有误
				}
			}
			if (isFileOk) {
				// 文件长度ok的情况下，调用方对其进行文件可用性检查，如apk文件需要检查md5，图片文件需要检查解码
				if (!fileAvailableChecker.checkFileAvailable(fileDownloadTask)) {
					isFileOk = false;
				}
			}
			if (isFileOk) {
				downloadStatePublisher.publishFileExistAvailable(fileDownloadTask);
			} else {
				// 文件不可用，删除文件，继续下面的流程
				storeFile.delete();
				// 切换初始化状态重新进行下载
				fileDownloader.changeState(FileDownloadState.STATE_INIT);
			}
		} catch (Throwable e) {
			if (Debug_SDK.isDownloadLog && isLogOpen) {
				Debug_SDK.de(mTag, e);
			}
		}
	}

}
