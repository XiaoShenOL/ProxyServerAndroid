package net.youmi.android.libs.common.download.state;

import android.content.Context;

import net.youmi.android.libs.common.debug.Debug_SDK;
import net.youmi.android.libs.common.download.basic.FinalFileDownloader;
import net.youmi.android.libs.common.download.listener.FileDownloadStateHandler;
import net.youmi.android.libs.common.download.listener.FileDownloadStatePublisher;
import net.youmi.android.libs.common.download.model.FileDownloadTask;

/**
 * 不同的任务阶段对应不同的策略，如果失败了，应有对应的错误码暴露给其他层 状态切换由handleState处理，调用方不应该处理state间的变化
 * 
 * @author zhitaocai edit on 2014-7-16
 */
public abstract class TaskState implements FileDownloadStateHandler {

	protected final String mTag = Debug_SDK.mDownloadTag + this.getClass().getSimpleName();
	protected final boolean isLogOpen = true;
	protected int mCurrentDownloadState;

	TaskState(int state) {
		mCurrentDownloadState = state;
	}

	/**
	 * 根据具体的状态状态处理
	 */
	public void handleState(Context context, FinalFileDownloader fileDownloader, FileDownloadTask fileDownloadTask,
			FileDownloadStatePublisher downloadStatePublisher) {
	}
}
