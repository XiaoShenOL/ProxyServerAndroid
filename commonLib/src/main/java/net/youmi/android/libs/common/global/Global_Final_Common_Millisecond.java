package net.youmi.android.libs.common.global;

import net.youmi.android.libs.common.debug.Debug_SDK;

public class Global_Final_Common_Millisecond {

	/**
	 * 1秒的毫秒数:1000毫秒
	 */
	public static final long oneSecond_ms = 1000;

	/**
	 * 1分钟的毫秒数:60秒*1000毫秒
	 */
	public static final long oneMinute_ms = 60 * oneSecond_ms;

	/**
	 * 1小时的毫秒数:60分钟*60秒*1000毫秒
	 */
	public static final long oneHour_ms = 60 * oneMinute_ms;

	/**
	 * 1天的毫秒数:24小时*60分钟*60秒*1000毫秒
	 */
	public static final long oneDay_ms = 24 * oneHour_ms;

	/**
	 * 1周的毫秒数:7天*24小时*60分钟*60秒*1000毫秒
	 */
	public static final long oneWeek_ms = 7 * oneDay_ms;
	/**
	 * 1个月的毫秒数:31天*24小时*60分钟*60秒*1000毫秒
	 */
	public static final long oneMonth_ms = 31 * oneDay_ms;

	/**
	 * @param @param time_ms
	 * @param @return 传入参数名字
	 * @return String 返回类型
	 * @Title: getTimeString
	 * @Description:将毫秒转换为时间字符串：XX天XX小时XX分钟
	 * @date 2012-8-22 下午9:36:25
	 * @throw
	 */
	public static String getTimeString(long time_ms) {
		String timeText = "";
		try {

			if (time_ms >= oneDay_ms) {
				int day = (int) (time_ms / oneDay_ms);
				timeText += (day + "天");
				time_ms -= day * oneDay_ms;
			}
			if (time_ms >= oneHour_ms) {
				int day = (int) (time_ms / oneHour_ms);
				timeText += (day + "小时");
				time_ms -= day * oneHour_ms;
			}
			if (time_ms >= oneMinute_ms) {
				int day = (int) (time_ms / oneMinute_ms);
				timeText += (day + "分钟");
				time_ms -= day * oneMinute_ms;
			}

			if (timeText.length() == 0) {
				int second = (int) (time_ms / oneSecond_ms);
				timeText += (second + "秒");
			}

		} catch (Throwable e) {
			if (Debug_SDK.isGlobalLog) {
				Debug_SDK.te(Debug_SDK.mGlobalTag, Global_Final_Common_Millisecond.class, e);
			}
		}
		return timeText;
	}

	/**
	 * @param @param time_ms，time_ms小于0的时候，xxx时间前，大于o的时候是xxx时间后
	 * @param @return 传入参数名字
	 * @return String 返回类型
	 * @Title: getRoughTime
	 * @Description:返回粗略时间字符串
	 * @date 2012-9-10 下午4:54:44
	 * @throw
	 */
	public static String getRoughTime(long time_ms) {
		String timeText = "";
		long abs_time_ms = Math.abs(time_ms);
		if (time_ms > 0) {
			if (abs_time_ms >= 42 * oneDay_ms) {
				int day = (int) (abs_time_ms / oneMonth_ms);
				timeText += (day + "月后");
			} else if (abs_time_ms >= 10 * oneDay_ms) {
				int day = (int) (abs_time_ms / oneWeek_ms);
				timeText += (day + "星期后");
			} else if (abs_time_ms >= 36 * oneHour_ms) {
				int day = (int) (abs_time_ms / oneDay_ms);
				timeText += (day + "天后");
			} else if (abs_time_ms > 90 * oneMinute_ms) {
				int day = (int) (abs_time_ms / oneHour_ms);
				timeText = day + "小时后";
			} else if (abs_time_ms > 60 * oneSecond_ms) {
				int day = (int) (abs_time_ms / oneMinute_ms);
				timeText = day + "分钟后";
			} else {
				timeText = "稍后";
			}
		} else {
			if (abs_time_ms >= 42 * oneDay_ms) {
				int day = (int) (abs_time_ms / oneMonth_ms);
				timeText += (day + "月前");
			} else if (abs_time_ms >= 10 * oneDay_ms) {
				int day = (int) (abs_time_ms / oneWeek_ms);
				timeText += (day + "星期前");
			} else if (abs_time_ms >= 36 * oneHour_ms) {
				int day = (int) (abs_time_ms / oneDay_ms);
				timeText += (day + "天前");
			} else if (abs_time_ms > 90 * oneMinute_ms) {
				int day = (int) (abs_time_ms / oneHour_ms);
				timeText = day + "小时前";
			} else if (abs_time_ms > 60 * oneSecond_ms) {
				int day = (int) (abs_time_ms / oneMinute_ms);
				timeText = day + "分钟前";
			} else {
				timeText = "刚刚";
			}
		}
		return timeText;
	}

}
