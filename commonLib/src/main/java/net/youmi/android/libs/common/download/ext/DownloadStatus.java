package net.youmi.android.libs.common.download.ext;

/**
 * app的下载状态
 * Created by yxf on 14-9-22.
 */
public enum DownloadStatus {

	PENDING,
	DOWNLOADING,
	PAUSED,
	FAILED,
	FINISHED,
	DISABLE;

	public static DownloadStatus value2Name(int value) {
		for (DownloadStatus status : DownloadStatus.values()) {
			if (status.ordinal() == value) {
				return status;
			}
		}
		return null;
	}
}
