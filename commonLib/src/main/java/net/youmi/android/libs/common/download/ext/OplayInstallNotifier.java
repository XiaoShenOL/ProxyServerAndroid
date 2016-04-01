package net.youmi.android.libs.common.download.ext;

import android.content.Context;

import net.youmi.android.libs.common.debug.AppDebugConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * 安装、卸载应用的通知类
 * Created by yxf on 14-12-3.
 */
public class OplayInstallNotifier {

	private static OplayInstallNotifier mInstance;
	private List<OnInstallListener> mListeners;

	private OplayInstallNotifier() {
		mListeners = new ArrayList<OnInstallListener>();
	}

	public synchronized static OplayInstallNotifier getInstance() {
		try {
			if (mInstance == null) {
				mInstance = new OplayInstallNotifier();
			}
		} catch (Exception e) {
			if (AppDebugConfig.IS_DEBUG) {
				e.printStackTrace();
			}
		}
		return mInstance;
	}

	public void notifyInstallListeners(Context context, String packageName) {
		for (OnInstallListener listener : mListeners) {
			if (listener != null) {
				listener.onInstall(context, packageName);
			}
		}
	}

	public synchronized void addListener(OnInstallListener listener) {
		if (listener != null && !mListeners.contains(listener)) {
			mListeners.add(listener);
		}
	}

	public synchronized void removeListener(OnInstallListener listener) {
		if (listener != null) {
			mListeners.remove(listener);
		}
	}

	public interface OnInstallListener {
		public void onInstall(Context context, final String packageName);
	}
}
