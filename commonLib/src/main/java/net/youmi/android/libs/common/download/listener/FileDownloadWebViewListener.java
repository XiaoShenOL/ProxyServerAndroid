package net.youmi.android.libs.common.download.listener;

import net.youmi.android.libs.common.download.model.FileDownloadTask;
import net.youmi.android.libs.common.download.webview.FileDownloadWebView;

public interface FileDownloadWebViewListener {
	/**
	 * 通过webview获取最终的url成功
	 * 
	 * @param webView
	 * @param task
	 * @param userAgent
	 * @param contentDisposition
	 * @param mimetype
	 */
	void onFinishGetDownloadFileUrl(FileDownloadWebView webView, FileDownloadTask task, String userAgent,
	                                String contentDisposition, String mimetype);

	/**
	 * 请求超时，发送下载失败通知
	 * 
	 * @param webView
	 * @param task
	 */
	void onGetDownloadFileUrlTimesout_ToNofifyDownloadFailed(FileDownloadWebView webView, FileDownloadTask task);

	/**
	 * 会话已过时，移除
	 * 
	 * @param webView
	 */
	void onGetDownloadFileSessionTimeout_JustRemove(FileDownloadWebView webView);

}
