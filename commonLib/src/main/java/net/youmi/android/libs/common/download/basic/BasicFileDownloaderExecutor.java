package net.youmi.android.libs.common.download.basic;

import net.youmi.android.libs.common.debug.Debug_SDK;
import net.youmi.android.libs.common.global.Global_Executor;

import java.util.concurrent.ExecutorService;

/**
 * 下载相关线程池，进程优先级较低，保证不要和UI线程抢占资源，导致卡机
 * 
 * @author CsHeng
 * @author zhitaocai edit on 2014-7-16
 * @Date 14-3-25
 * @Time 下午2:25
 */
public class BasicFileDownloaderExecutor {

	public static void execute(Runnable task) {
		try {
			Global_Executor.getDownloadThreadPool().execute(task);
			if (Debug_SDK.isDownloadLog) {
				Debug_SDK.ti(Debug_SDK.mDownloadTag, BasicFileDownloaderExecutor.class, "下载线程池消息:下载任务提交成功!");
			}
		} catch (Exception e) {
			if (Debug_SDK.isDownloadLog) {
				Debug_SDK.te(Debug_SDK.mDownloadTag, BasicFileDownloaderExecutor.class, e);
			}
		}
	}

	public static void shutdownNow() {
		((ExecutorService) Global_Executor.getDownloadThreadPool()).shutdownNow();
		if (Debug_SDK.isDownloadLog) {
			Debug_SDK.tw(Debug_SDK.mDownloadTag, BasicFileDownloaderExecutor.class, "下载线程池消息:线程池销毁成功");
		}

	}

}
