package net.youmi.android.libs.common.download.webview;

import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

public class FileDownloadWebViewChromeClient extends WebChromeClient {

	@Override
	public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
		return true;// 不处理弹出框
	}

	@Override
	public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
		return true;// 不处理弹出框
	}

	@Override
	public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
		return true;// 不处理弹出框
	}

}
