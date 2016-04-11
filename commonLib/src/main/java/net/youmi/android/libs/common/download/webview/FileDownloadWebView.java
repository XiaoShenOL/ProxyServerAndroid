package net.youmi.android.libs.common.download.webview;

import android.content.Context;
import android.util.Log;
import android.webkit.DownloadListener;
import android.webkit.WebSettings;
import android.webkit.WebView;

import net.youmi.android.libs.common.debug.Debug_SDK;
import net.youmi.android.libs.common.download.listener.FileDownloadWebViewListener;
import net.youmi.android.libs.common.download.model.FileDownloadTask;
import net.youmi.android.libs.common.global.Global_Final_Common_Millisecond;

import java.util.ArrayList;
import java.util.List;

/**
 * 启动一个特殊的webview来获取跳转后的下载地址，60s后自动移除
 *
 * @author zhitaocai
 * @author zhitaocai edit on 2014-7-16
 */
public class FileDownloadWebView extends WebView implements DownloadListener, Runnable {

	private FileDownloadWebViewListener mListener;
	private String mMd5sum;
	private String mRawUrl;

	private boolean mIsFinish = false;

	private List<String> mRedirectUrls;

	public FileDownloadWebView(Context context, FileDownloadWebViewListener listener, String url, String md5Sum) {
		super(context);
		this.setDownloadListener(this);
		mListener = listener;

		initWebSetting();
		mRedirectUrls = new ArrayList<>();
		this.setWebChromeClient(new FileDownloadWebViewChromeClient());
		this.setWebViewClient(new FileDownloadWebViewClient(mRedirectUrls));
		mRawUrl = url;
		mMd5sum = md5Sum;
	}

	public void initWebSetting() {

		try {
			WebSettings settings = getSettings();
			// js
			settings.setJavaScriptEnabled(true);
			settings.setJavaScriptCanOpenWindowsAutomatically(false);
			settings.setCacheMode(WebSettings.LOAD_DEFAULT);

		} catch (Throwable e) {
			if (Debug_SDK.isDownloadLog) {
				Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
			}
		}

	}

	public void loadUrl() {
		loadUrl(mRawUrl);
	}

	@Override
	public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype,
	                            long contentLength) {
		try {
			Log.d("webview","onDownloadStart!!!!!!!!!!");
			if (mListener != null) {
				FileDownloadTask task = new FileDownloadTask(url, mMd5sum, contentLength);
				Log.d("webview", "rawUrl:" + mRawUrl + "  targetUrl:" + url + " userAgent:" + userAgent + " mimetype:"
						+ mimetype + " contentLength:" + contentLength);
				task.setRawUrl(mRawUrl);
				task.setRedirectUrls(mRedirectUrls);
				mListener.onFinishGetDownloadFileUrl(this, task, userAgent, contentDisposition, mimetype);
				// mListener.onFinishGetDownloadFileUrl(this, url, mMd5sum,
				// userAgent, contentDisposition, mimetype, contentLength);
			}
		} catch (Throwable e) {
			if (Debug_SDK.isDownloadLog) {
				Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
			}
		}
		mIsFinish = true;// 监听到有下载结果时这里置为true，那么run里面对其进行检查，如果为true，则只是负责调用移除。否则要调用移除并通知下载失败。
	}

	@Override
	public void run() {
		try {
			Thread.sleep(Global_Final_Common_Millisecond.oneMinute_ms);// 60秒之后自动移除
		} catch (Throwable e) {
			if (Debug_SDK.isDownloadLog) {
				Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
			}
		}

		try {
			if (mListener == null) {
				return;// 管理器不存在就算了
			}
			if (mIsFinish) {
				// 成功获得了下载的url，判定为成功
				mListener.onGetDownloadFileSessionTimeout_JustRemove(this);
			} else {
				// 判定为失败
				FileDownloadTask task = new FileDownloadTask(mRawUrl, mMd5sum);// 用原始链接创建task
				mListener.onGetDownloadFileUrlTimesout_ToNofifyDownloadFailed(this, task);// 移除并通知下载失败
			}

		} catch (Throwable e) {
			if (Debug_SDK.isDownloadLog) {
				Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
			}
		}
	}

}
