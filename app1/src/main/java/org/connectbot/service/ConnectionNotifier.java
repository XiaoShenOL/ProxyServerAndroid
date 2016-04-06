package org.connectbot.service;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;

import org.connectbot.bean.HostBean;
import org.connectbot.util.PreferenceConstants;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author zyq 16-3-6
 */
public abstract  class ConnectionNotifier {

	private static final int ONLINE_NOTIFICATION = 1;
	private static final int ACTIVITY_NOTIFICATION = 2;
	private static final int ONLINE_DISCONNECT_NOTIFICATION = 3;

	public static ConnectionNotifier getInstance() {
		if (PreferenceConstants.PRE_ECLAIR)
			return PreEclair.Holder.sInstance;
		else
			return EclairAndBeyond.Holder.sInstance;
	}

	protected NotificationManager getNotificationManager(Context context) {
		return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
	}

//	protected NotificationCompat.Builder newNotificationBuilder(Context context) {
//		NotificationCompat.Builder builder =
//				new NotificationCompat.Builder(context)
//						.setSmallIcon(R.drawable.notification_icon)
//						.setWhen(System.currentTimeMillis());
//
//		return builder;
//	}

	protected Notification newActivityNotification(Context context, HostBean host) {
//		NotificationCompat.Builder builder = newNotificationBuilder(context);
//
//		Resources res = context.getResources();
//
//		String contentText = res.getString(
//				R.string.notification_text, host.getNickname());
//
//		Intent notificationIntent = new Intent(context, ConsoleActivity.class);
//		notificationIntent.setAction("android.intent.action.VIEW");
//		notificationIntent.setData(host.getUri());
//
//		PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
//				notificationIntent, 0);
//
//		builder.setContentTitle(res.getString(R.string.app_name))
//				.setContentText(contentText)
//				.setContentIntent(contentIntent);
//
//		builder.setAutoCancel(true);
//
//		int ledOnMS = 300;
//		int ledOffMS = 1000;
//		builder.setDefaults(Notification.DEFAULT_LIGHTS);
//			builder.setLights(Color.WHITE, ledOnMS, ledOffMS);
//
//		return builder.build();
		return  null;
	}

	protected Notification newRunningNotification(Context context) {
//		NotificationCompat.Builder builder = newNotificationBuilder(context);

//		builder.setOngoing(true);
//		builder.setWhen(0);
//      todo 处理是否要显示当前是否在运行.
//		builder.setContentIntent(PendingIntent.getActivity(context,
//				ONLINE_NOTIFICATION,
//				new Intent(context, ConsoleActivity.class), 0));
//
//		Resources res = context.getResources();
//		builder.setContentTitle(res.getString(R.string.app_name));
//		builder.setContentText(res.getString(R.string.app_is_running));
//
//		Intent disconnectIntent = new Intent(context, HostListActivity.class);
//		disconnectIntent.setAction(HostListActivity.DISCONNECT_ACTION);
//		builder.addAction(
//				android.R.drawable.ic_menu_close_clear_cancel,
//				res.getString(R.string.list_host_disconnect),
//				PendingIntent.getActivity(
//						context,
//						ONLINE_DISCONNECT_NOTIFICATION,
//						disconnectIntent,
//						0));

//		return builder.build();
		return  null;
	}

	public void showActivityNotification(Service context, HostBean host) {
		getNotificationManager(context).notify(ACTIVITY_NOTIFICATION, newActivityNotification(context, host));
	}

	public void hideActivityNotification(Service context) {
		getNotificationManager(context).cancel(ACTIVITY_NOTIFICATION);
	}

	public abstract void showRunningNotification(Service context);
	public abstract void hideRunningNotification(Service context);

	private static class PreEclair extends ConnectionNotifier {
		private static final Class<?>[] setForegroundSignature = new Class[] {boolean.class};
		private Method setForeground = null;

		private static class Holder {
			private static final PreEclair sInstance = new PreEclair();
		}

		public PreEclair() {
			try {
				setForeground = Service.class.getMethod("setForeground", setForegroundSignature);
			} catch (Exception e) {
			}
		}

		@Override
		public void showRunningNotification(Service context) {
			if (setForeground != null) {
				Object[] setForegroundArgs = new Object[1];
				setForegroundArgs[0] = Boolean.TRUE;
				try {
					setForeground.invoke(context, setForegroundArgs);
				} catch (InvocationTargetException e) {
				} catch (IllegalAccessException e) {
				}
				getNotificationManager(context).notify(ONLINE_NOTIFICATION, newRunningNotification(context));
			}
		}

		@Override
		public void hideRunningNotification(Service context) {
			if (setForeground != null) {
				Object[] setForegroundArgs = new Object[1];
				setForegroundArgs[0] = Boolean.FALSE;
				try {
					setForeground.invoke(context, setForegroundArgs);
				} catch (InvocationTargetException e) {
				} catch (IllegalAccessException e) {
				}
				getNotificationManager(context).cancel(ONLINE_NOTIFICATION);
			}
		}
	}

	@TargetApi(5)
	private static class EclairAndBeyond extends ConnectionNotifier {
		private static class Holder {
			private static final EclairAndBeyond sInstance = new EclairAndBeyond();
		}

		@Override
		public void showRunningNotification(Service context) {
			context.startForeground(ONLINE_NOTIFICATION, newRunningNotification(context));
		}

		@Override
		public void hideRunningNotification(Service context) {
			context.stopForeground(true);
		}
	}
}
