package net.youmi.android.libs.common.download.ext;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat.Builder;

import net.youmi.android.libs.common.debug.AppDebugConfig;
import net.youmi.android.libs.common.debug.Debug_SDK;

/**
 * @author: CsHeng (csheng1204[at]gmail[dot]com)
 * Date: 14-3-10
 * Time: 上午11:44
 */
public class OPlayNotificationManager {

	public static final int REQUEST_CODE_DOWNLOAD = 12040;
	public static final int REQUEST_ID_DOWNLOAD = 10240;

	public static final int REQUEST_CODE_USERMESSAGE = 1204;
	public static final int REQUEST_ID_USERMESSAGE = 1024;

	public static void showDownload(Context context) {
		try {
			if (AppDebugConfig.IS_DEBUG) {
				AppDebugConfig.logMethodWithParams("OplayNotificationManager", "showdownload");
			}
			int count = OplayDownloadManager.getInstance(context).getEndOfPaused();
			NotificationManager notificationManager = (NotificationManager)
					context.getSystemService(Service.NOTIFICATION_SERVICE);
//			if (count > 0) {
//
//				Intent download = MainActivity.getFragmentIntent(context, ListFragment_App_Downloading.class, null);
//				PendingIntent pi = PendingIntent.getActivity(context, REQUEST_CODE_DOWNLOAD,
//						download, PendingIntent.FLAG_UPDATE_CURRENT);
//				String tickerText = String.format("您有%d个游戏正在下载中", count);
//				String title = "点击查看详情";
//
//				Builder builder = buildNotification(context, pi, tickerText, title);
//				builder.setOngoing(true);
//				builder.setAutoCancel(false);
//				notificationManager.notify(REQUEST_ID_DOWNLOAD, builder.build());
//			} else {
//				notificationManager.cancel(REQUEST_ID_DOWNLOAD);
//			}
		} catch (Exception e) {
			if (AppDebugConfig.IS_DEBUG) {
				Debug_SDK.e(e);
			}
		}
	}

	public static void cancelDownload(Context context) {
		try {
			if (AppDebugConfig.IS_DEBUG) {
				AppDebugConfig.logMethodName(OPlayNotificationManager.class);
			}
			final NotificationManager notificationManager = (NotificationManager)
					context.getSystemService(Service.NOTIFICATION_SERVICE);
			notificationManager.cancel(REQUEST_ID_DOWNLOAD);
		} catch (Exception e) {
			if (AppDebugConfig.IS_DEBUG) {
				Debug_SDK.e(e);
			}
		}
	}

	public static void showDownloadComplete(Context context, SimpleAppInfo simpleAppInfo) {
		try {
			if (AppDebugConfig.IS_DEBUG) {
				AppDebugConfig.logMethodName(OPlayNotificationManager.class);
			}
			String filePath = simpleAppInfo.getDestFilePath();
			Intent intent;
//			if (filePath.endsWith(AppConstants.SUFFIX_OPK)) {
//				intent = MainActivity.getInstallIntent(context, simpleAppInfo.getOwkUrl());
//			} else {
//				intent = Util_System_Package.getInstallApkIntentByApkFilePath(context, filePath);
//			}
//			int notificationId = simpleAppInfo.getOwkUrl().hashCode();
//			PendingIntent pi = PendingIntent.getActivity(context, notificationId, intent,
//					PendingIntent.FLAG_UPDATE_CURRENT);
//			final NotificationManager notificationManager = (NotificationManager)
//					context.getSystemService(Service.NOTIFICATION_SERVICE);
//			String tickerText = String.format("\"%s\"已下载完成", simpleAppInfo.getAppName());
//			final String title = "点击进行安装";
//
//			final Builder builder = new Builder(context);
//			builder.setContentIntent(pi);
//			builder.setContentTitle(title);
//			builder.setTicker(tickerText);
//			builder.setContentText(tickerText);
//			builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_stat_notify));
//			updateBySDKVersion(builder);
////        builder.setDefaults(Notification.DEFAULT_ALL);
//			builder.setAutoCancel(true);
//			notificationManager.cancel(notificationId);
//			notificationManager.notify(notificationId, builder.build());
		} catch (Exception e) {
			if (AppDebugConfig.IS_DEBUG) {
				Debug_SDK.e(e);
			}
		}
	}

	private static void updateBySDKVersion(Builder builder) {
//		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
//			builder.setSmallIcon(R.drawable.ic_notification_144);
//			builder.setColor(Color.YELLOW);
//		} else {
//			builder.setSmallIcon(R.drawable.ic_stat_notify);
//		}
	}

	public static void showDownloadFailed(Context context, String rawUrl, String appName, String reason) {
//		try {
//			if (AppDebugConfig.IS_DEBUG) {
//				AppDebugConfig.logMethodName(OPlayNotificationManager.class);
//			}
//			Intent download = MainActivity.getFragmentIntent(context, ListFragment_App_Downloading.class, null);
//
//			int notificationId = rawUrl.hashCode();
//			PendingIntent pi = PendingIntent.getActivity(context, notificationId, download,
//					PendingIntent.FLAG_UPDATE_CURRENT);
//			final NotificationManager notificationManager = (NotificationManager)
//					context.getSystemService(Service.NOTIFICATION_SERVICE);
//			String tickerText = null;
//			if (TextUtils.isEmpty(reason)) {
//				tickerText = String.format("%s下载失败", appName);
//			} else {
//				tickerText = String.format("%s, %s下载失败", reason, appName);
//			}
//			final String title = "点击查看详情";
//
//			Builder builder = buildNotification(context, pi, tickerText, title);
//			builder.setAutoCancel(true);
//			notificationManager.cancel(notificationId);
//			notificationManager.notify(notificationId, builder.build());
//		} catch (Exception e) {
//			if (AppDebugConfig.IS_DEBUG) {
//				Debug_SDK.e(e);
//			}
//		}
	}

	public static void clearDownloadComplete(Context context, String rawUrl) {
		try {
			if (AppDebugConfig.IS_DEBUG) {
				AppDebugConfig.logMethodName(OPlayNotificationManager.class);
			}
			int notificationId = rawUrl.hashCode();
			final NotificationManager notificationManager = (NotificationManager) context.getSystemService(Service
					.NOTIFICATION_SERVICE);
			notificationManager.cancel(notificationId);
		} catch (Exception e) {
			if (AppDebugConfig.IS_DEBUG) {
				Debug_SDK.e(e);
			}
		}
	}

	public static void showUnzipFailed(Context context, SimpleAppInfo simpleAppInfo) {
//		try {
//			if (AppDebugConfig.IS_DEBUG) {
//				AppDebugConfig.logMethodName(OPlayNotificationManager.class);
//			}
//			String filePath = simpleAppInfo.getDestFilePath();
//			Intent intent = null;
//			if (filePath.endsWith(AppConstants.SUFFIX_OPK)) {
//				intent = MainActivity.getInstallIntent(context, simpleAppInfo.getOwkUrl());
//			}
//			if (intent == null) return;
//			String fileUrl = simpleAppInfo.getDestFilePath();
//			int notificationId = fileUrl.hashCode();
//			final NotificationManager notificationManager = (NotificationManager)
//					context.getSystemService(Service.NOTIFICATION_SERVICE);
//			notificationManager.cancel(notificationId);
//			PendingIntent pi = PendingIntent.getActivity(context, notificationId, intent,
//					PendingIntent.FLAG_UPDATE_CURRENT);
//			String tickerText = String.format("游戏\"%s\"数据包解压失败了", simpleAppInfo.getAppName());
//			String title = "请点击重试";
//			Builder builder = buildNotification(context, pi, tickerText, title);
//			builder.setOngoing(true);
//			builder.setAutoCancel(false);
//			notificationManager.notify(notificationId, builder.build());
//		} catch (Exception e) {
//			if (AppDebugConfig.IS_DEBUG) {
//				Debug_SDK.e(e);
//			}
//		}
	}

	public static void showUnzipProgress(Context context, SimpleAppInfo simpleAppInfo, int progress,
	                                     boolean isCancel) {
//		try {
//			if (AppDebugConfig.IS_DEBUG) {
//				AppDebugConfig.logMethodName(OPlayNotificationManager.class);
//			}
//			String fileUrl = simpleAppInfo.getDestFilePath();
//			int notificationId = fileUrl.hashCode();
//			final NotificationManager notificationManager = (NotificationManager)
//					context.getSystemService(Service.NOTIFICATION_SERVICE);
//			if (progress == 100 || isCancel) {
//				notificationManager.cancel(notificationId);
//			} else {
//				Intent toHome = MainActivity.getReorderToFrontIntent(context);
//				PendingIntent pi = PendingIntent.getActivity(context, notificationId, toHome,
//						PendingIntent.FLAG_UPDATE_CURRENT);
//				String tickerText = String.format("游戏\"%s\"数据包正在解压中", simpleAppInfo.getAppName());
//				String title = context.getString(R.string.app_name);
//
//				Builder builder = buildNotification(context, pi, tickerText, title);
//				builder.setProgress(100, progress, false);
//				builder.setOngoing(true);
//				builder.setAutoCancel(false);
//				notificationManager.notify(notificationId, builder.build());
//			}
//		} catch (Exception e) {
//			if (AppDebugConfig.IS_DEBUG) {
//				Debug_SDK.e(e);
//			}
//		}
	}

//	public static Builder buildNotification(Context context, PendingIntent pi, String tickerText, String title) {
//		Builder builder = new Builder(context);
//		builder.setContentIntent(pi);
//		builder.setContentTitle(title);
//		builder.setTicker(tickerText);
//		builder.setContentText(tickerText);
//		builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_stat_notify));
//		updateBySDKVersion(builder);
////        builder.setDefaults(Notification.DEFAULT_ALL);
////        builder.setOngoing(true);
////        builder.setAutoCancel(false);
//		return builder;
//	}

	public static void showNotification(Context context, Builder builder) {
		final NotificationManager notificationManager =
				(NotificationManager) context.getSystemService(Service.NOTIFICATION_SERVICE);
		notificationManager.cancel(REQUEST_ID_USERMESSAGE);
		notificationManager.notify(REQUEST_ID_USERMESSAGE, builder.build());
	}

	private static void cancelNotification(Context context, int ID) {
		final NotificationManager notificationManager = (NotificationManager)
				context.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(ID);
	}

	public static void showUserMessage(Context context, int uncheckCount) {
//		Intent toMsgIntent = null;
//		if (uncheckCount > 0) {
//			toMsgIntent = MainActivity.getFragmentIntent(context, ListFragment_Message_Center.class, null);
//		} else {
//			cancelNotification(context, REQUEST_ID_USERMESSAGE);
//		}
//		if (toMsgIntent != null) {
//			final PendingIntent pi = PendingIntent.getActivity(
//					context, REQUEST_CODE_USERMESSAGE, toMsgIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//			String tickerText = context.getString(R.string.pattern_message_notice, uncheckCount);
//			final String title = context.getString(R.string.user_message_title);
//			Builder builder = buildNotification(context, pi, tickerText, title);
//			builder.setAutoCancel(true);
//			showNotification(context, builder);
//		}
	}

}
