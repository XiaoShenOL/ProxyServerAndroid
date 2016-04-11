package net.youmi.android.libs.common.download.webview;

import android.webkit.WebView;
import android.webkit.WebViewClient;

import net.youmi.android.libs.common.debug.Debug_SDK;

import java.util.List;

public class FileDownloadWebViewClient extends WebViewClient {
	List<String> mList;

	public FileDownloadWebViewClient(List<String> list) {
		mList = list;
	}

	@Override
	public boolean shouldOverrideUrlLoading(WebView view, String url) {
		if (Debug_SDK.isDownloadLog) {
			Debug_SDK.te(Debug_SDK.mDownloadTag, this, "downloadUrl:"+url);
		}
		if (null != mList && !mList.contains(url)) {
			mList.add(url);
		}
		return false;
	}

	@Override
	public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
		super.onReceivedError(view, errorCode, description, failingUrl);

		if (Debug_SDK.isDownloadLog) {
			Debug_SDK.te(Debug_SDK.mDownloadTag, this, "onReceivedError-errCode:%d,desc:%s,failingUrl:%s", errorCode,
					description, failingUrl);
		}
	}

}
