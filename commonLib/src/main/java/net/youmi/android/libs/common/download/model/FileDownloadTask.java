package net.youmi.android.libs.common.download.model;

import net.youmi.android.libs.common.coder.Coder_Md5;
import net.youmi.android.libs.common.debug.Debug_SDK;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 下载任务描述
 *
 * @author zhitaocai edit on 2014-5-29
 */
public class FileDownloadTask {

	/**
	 * 文件最终下载地址
	 */
	private String mDestUrl;

	/**
	 * 由文件最终下载地址生成的identity
	 */
	private String mIdentity;

	/**
	 * 由文件最终下载地址生成的identity的hashcode
	 */
	private int mHashCode;

	/**
	 * 下载任务是否可用，一般是检查url是否正常
	 */
	private boolean mIsAvailable = false;

	/**
	 * 最终存储文件的长度
	 */
	private long mContentLength = 0;

	/**
	 * 原始文件下载链接
	 */
	private String mRawUrl;

	/**
	 * 原始文件下载链接生成的identity
	 */
	private String mRawIdentity;

	/**
	 * md5校验码
	 */
	private String mMd5sum;

	/**
	 * 文件的最终保存位置，创建对象的时候传入
	 */
	private File mStoreFile;

	/**
	 * 缓存文件位置，进入下载之前创建，不在创建对象的时候传入
	 */
	private File mTempFile;

	/**
	 * 重定向经过的 URL，在经过webview重定向的时候拿到
	 */
	private List<String> mRedirectUrls;

	// /**
	// * 服务器原始文件名
	// */
	// private String sourceFileName;
	//
	// /**
	// * 目标保存路径
	// */
	// private String destSaveFilePath;

	// /**
	// * 下载中的文件路径
	// */
	// private String downloadTempFilePath;

	public FileDownloadTask(String rawUrl, String md5sum, long contentLength) {
		this(rawUrl, md5sum);
		// 设置contentLength
		setContentLength(contentLength);
	}

	public FileDownloadTask(String rawUrl, String md5sum) {
		this(rawUrl);

		// 设置md5
		this.setMd5sum(md5sum);
	}

	public FileDownloadTask(String rawUrl) {
		if (rawUrl == null) {
			mIsAvailable = false;
			return;
		}

		rawUrl = rawUrl.trim();
		if (rawUrl.length() <= 0) {
			mIsAvailable = false;
			return;
		}

		mDestUrl = rawUrl;
		mIdentity = Coder_Md5.md5(rawUrl);

		mRawUrl = mDestUrl;
		mRawIdentity = mIdentity;

		mHashCode = mIdentity.hashCode();
		mIsAvailable = true;
		mRedirectUrls = new ArrayList<>();
	}

	/**
	 * 获取文件下载地址(最终下载地址)
	 *
	 * @return
	 */
	public String getDestUrl() {
		return mDestUrl;
	}

	/**
	 * 获取由文件最终下载地址生成的identity
	 *
	 * @return
	 */
	public String getIdentity() {
		return mIdentity;
	}

	/**
	 * 获取原始的url
	 *
	 * @return
	 */
	public String getRawUrl() {
		return mRawUrl;
	}

	/**
	 * 获取由原始的url产生的identity
	 *
	 * @return
	 */
	public String getRawIdentity() {
		return mRawIdentity;
	}

	/**
	 * 下载任务是否可用，一般是检查url是否正常
	 *
	 * @return
	 */
	public boolean isAvailable() {
		return mIsAvailable;
	}

	/**
	 * 设置长度
	 *
	 * @param contentLength
	 */
	public void setContentLength(long contentLength) {
		this.mContentLength = contentLength;
	}

	/**
	 * 获取长度
	 *
	 * @return
	 */
	public long getContentLength() {
		return mContentLength;
	}

	protected void setMd5sum(String md5sum) {
		try {
			if (md5sum == null) {
				return;
			}
			md5sum = md5sum.trim().toLowerCase(Locale.getDefault());
			if (md5sum.length() <= 0) {
				return;
			}

			// 最好能检查到32位hex字符
			this.mMd5sum = md5sum;

		} catch (Throwable e) {
			if (Debug_SDK.isDownloadLog) {
				Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
			}
		}
	}

	/**
	 * 获取md5校验码
	 *
	 * @return
	 */
	public String getMd5sum() {
		return mMd5sum;
	}

	/**
	 * 设置原始下载链接
	 *
	 * @param rawUrl
	 */
	public void setRawUrl(String rawUrl) {
		try {
			if (rawUrl == null) {
				return;
			}
			rawUrl = rawUrl.trim();
			if (rawUrl.length() <= 0) {
				return;
			}
			String md5 = Coder_Md5.md5(rawUrl);
			this.mRawUrl = rawUrl;
			this.mRawIdentity = md5;

		} catch (Throwable e) {
			if (Debug_SDK.isDownloadLog) {
				Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
			}
		}
	}

	/**
	 * 获取最终存储的文件地址
	 *
	 * @return
	 */
	public File getStoreFile() {
		return mStoreFile;
	}

	/**
	 * 设置最终存储的文件地址
	 *
	 * @param storeFile
	 */
	public void setStoreFile(File storeFile) {
		mStoreFile = storeFile;
	}

	/**
	 * 获取缓存文件的地址，可能为空，只有在开始下载和下载过程中，才会可能获取到真实的缓存地址
	 *
	 * @return
	 */
	public File getTempFile() {
		return mTempFile;
	}

	/**
	 * 设置缓存文件的地址，在下载开始的时候才设置
	 *
	 * @param mTempFile
	 */
	public void setTempFile(File mTempFile) {
		this.mTempFile = mTempFile;
	}

	@Override
	public int hashCode() {
		return mHashCode;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}

		if (o.hashCode() == this.hashCode()) {
			return true;
		}

		return false;
	}

	@Override
	public String toString() {
		try {
			if (Debug_SDK.isDownloadLog) {
				return "FileDownloadTask [" + "\n mDestUrl=" + mDestUrl + ", " + "\n mIdentity=" + mIdentity + ", "
						+ "\n mHashCode=" + mHashCode + ", \n mIsAvailable=" + mIsAvailable + ", \n mContentLength="
						+ mContentLength + ", \n mRawUrl=" + mRawUrl + ", \n mRawIdentity=" + mRawIdentity
						+ ", \n mMd5sum=" + mMd5sum + ", \n mStoreFile=" + mStoreFile + ", \n mTempFile=" + mTempFile
						+ "\n]";
			}
		} catch (Throwable e) {
			if (Debug_SDK.isDownloadLog) {
				Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
			}
		}
		return super.toString();
	}
	// /**
	// * 获取文件原始名字
	// *
	// * @return
	// */
	// public String getSourceFileName() {
	// return sourceFileName;
	// }
	//
	// /**
	// * 设置文件原始名字
	// *
	// * @param sourceFileName
	// */
	// void setSourceFileName(String sourceFileName) {
	// this.sourceFileName = sourceFileName;
	// }
	//
	// public String getDestSaveFilePath() {
	// return destSaveFilePath;
	// }
	//
	// /**
	// * 设置文件最终保存路径(只限组件内调用)
	// *
	// * @param destSaveFilePath
	// */
	// void setDestSaveFilePath(String destSaveFilePath) {
	// this.destSaveFilePath = destSaveFilePath;
	// }

	// /**
	// *获取下载中的temp文件路径
	// * @return
	// */
	// public String getDownloadTempFilePath() {
	// return downloadTempFilePath;
	// }

	// /**
	// * 设置下载中的temp文件路径
	// * @param downloadTempFilePath
	// */
	// public void setDownloadTempFilePath(String downloadTempFilePath) {
	// this.downloadTempFilePath = downloadTempFilePath;
	// }

	public List<String> getRedirectUrls() {
		return mRedirectUrls;
	}

	public void setRedirectUrls(List<String> redirectUrls) {
		if (redirectUrls != null) {
			mRedirectUrls.clear();
			mRedirectUrls.addAll(redirectUrls);
			redirectUrls.clear();
		}
	}
}
