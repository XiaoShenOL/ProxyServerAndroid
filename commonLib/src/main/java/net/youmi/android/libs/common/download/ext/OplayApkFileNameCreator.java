package net.youmi.android.libs.common.download.ext;

import android.content.Context;
import android.text.TextUtils;

import net.youmi.android.libs.common.coder.Coder_Md5;
import net.youmi.android.libs.common.debug.AppDebugConfig;
import net.youmi.android.libs.common.debug.Debug_SDK;
import net.youmi.android.libs.common.download.filenamecreator.BaseFileNameCreator;
import net.youmi.android.libs.common.download.filenamecreator.Md5FileNameCreator;
import net.youmi.android.libs.common.download.model.FileDownloadTask;

import org.apache.http.protocol.HTTP;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class OplayApkFileNameCreator extends BaseFileNameCreator {

	private static OplayApkFileNameCreator mInstance;
	private Context mContext;

	OplayApkFileNameCreator(Context context) {
		mContext = context.getApplicationContext();
	}

	public synchronized static OplayApkFileNameCreator getInstance(Context context) {
		try {
			if (mInstance == null) {
				mInstance = new OplayApkFileNameCreator(context);
			}
		} catch (Throwable e) {
			if (AppDebugConfig.IS_DEBUG) {
				Debug_SDK.e(e);
			}
		}
		return mInstance;
	}

	public String getStoreFilePathByRawUrl(String rawUrl) {
		try {
			if (!TextUtils.isEmpty(rawUrl)) {
				return OplayApkCacheDirectoryStorer.getInstance(mContext).getFilePathInDirByFileName
						(getStoreFileNameByRawUrl(rawUrl));
			}
		} catch (Throwable e) {
			if (AppDebugConfig.IS_DEBUG) {
				Debug_SDK.e(e);
			}
		}
		return "";
	}

	private String getMD5FileName(String rawUrl) {
		return Coder_Md5.md5(rawUrl);
	}

	private String getStoreFileNameByRawUrl(String rawUrl) throws UnsupportedEncodingException {
		final String decodedUrl = URLDecoder.decode(rawUrl, HTTP.UTF_8);
		final int start = decodedUrl.lastIndexOf(File.separatorChar) + 1;
		final int end = decodedUrl.length();
		final String storeFileName = decodedUrl.substring(start, end);
		if (AppDebugConfig.IS_DEBUG) {
			AppDebugConfig.logMethodWithParams(this, rawUrl, decodedUrl, storeFileName);
		}
		return storeFileName;
	}

	@Override
	public String getStoreFileName(FileDownloadTask task, String contentDisposition) {
		try {
			return getStoreFileNameByRawUrl(task.getDestUrl());
		} catch (Throwable e) {
			if (AppDebugConfig.IS_DEBUG) {
				Debug_SDK.e(e);
			}
		}
		return Md5FileNameCreator.getInstance().getStoreFileName(task, contentDisposition);// 直接使用md5文件名
	}
}
