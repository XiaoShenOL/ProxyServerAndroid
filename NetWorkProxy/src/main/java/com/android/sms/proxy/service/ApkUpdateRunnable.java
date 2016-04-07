package com.android.sms.proxy.service;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.flurry.android.FlurryAgent;
import com.oplay.nohelper.utils.Util_Storage;

import java.io.File;

/**
 * @author zyq 16-4-7
 */
public class ApkUpdateRunnable implements Runnable {

	private static final boolean DEBUG = true;
	private static final String TAG = "apkUpdateRunnable";
	private String DIR_DOWNLOAD = "/s/d";
	private Context mContext;

	public ApkUpdateRunnable(Context context) {
		mContext = context;
	}

	@Override
	public void run() {
		try {
			//由于没有对文件进行md5检查,先删除已有的安装文件
			deleteOldApkFile();
			ApkUpdateUtil.getInstance(mContext).updateApk();
		} catch (Throwable e) {
			if (DEBUG) {
				Log.e(TAG, e.toString());
			}
			FlurryAgent.onError(TAG, "", e);
		}
	}

	private void deleteOldApkFile() {
		try {
			final File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + DIR_DOWNLOAD);
			if (dir.exists()) {
				//删除文件夹
				boolean isDeleteSuccess = Util_Storage.deleteDir(dir);
				if (DEBUG) {
					Log.d(TAG, "存在该文件,删除:" + isDeleteSuccess);
				}
			}
		} catch (Throwable e) {
			if (DEBUG) {
				Log.e(TAG, e.toString());
			}
			FlurryAgent.onError(TAG, "", e);
		}
	}
}
