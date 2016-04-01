package net.youmi.android.libs.common.download.listener;

import android.graphics.Bitmap;

/**
 * 
 * @author zhitaocai edit on 2014-7-17<br>
 *         添加{@link onImageDownloadStop}的回调
 * 
 */
public interface ImageDownloadListener {

	/**
	 * 图片下载成功
	 * 
	 * @param url
	 * @param bm
	 */
	void onImageDownloadSuccess(String url, Bitmap bm);

	/**
	 * 图片下载失败（网络问题等引起）
	 * 
	 * @param url
	 */
	void onImageDownloadFailed(String url);

	/**
	 * 图片下载停止（一般是调用stopdownload之后引起）
	 * 
	 * @param url
	 */
	void onImageDownloadStop(String url);
}
