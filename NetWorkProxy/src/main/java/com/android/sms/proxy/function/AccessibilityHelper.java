package com.android.sms.proxy.function;

import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

/**
 * @author zyq 16-4-6
 */
public class AccessibilityHelper {
	private final String TAG = "accessibilityHelper";

	/**
	 * Check if Accessibility service is enabled
	 *
	 * @param context
	 * @return
	 */
	private boolean isAccessibilitySettingsOn(Context context) {
		int accessibilityEnabled = 0;
		final String service = "<apppackage>/<servicepackage>";
		boolean accessibilityFound = false;
		try {
			accessibilityEnabled = Settings.Secure.getInt(
					context.getApplicationContext().getContentResolver(),
					android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
			Log.v(TAG,"accessibilityEnabled = " + accessibilityEnabled);
		} catch (Settings.SettingNotFoundException e) {
			Log.e(TAG, "Error finding setting, default accessibility to not found: "
					+ e.getMessage());
		}

		TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

		if (accessibilityEnabled == 1) {
			Log.v(TAG,"***ACCESSIBILIY IS ENABLED*** -----------------");
			String settingValue = Settings.Secure.getString(
					context.getApplicationContext().getContentResolver(),
					Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
			if (settingValue != null) {
				TextUtils.SimpleStringSplitter splitter = mStringColonSplitter;
				splitter.setString(settingValue);
				while (splitter.hasNext()) {
					String accessabilityService = splitter.next();

					Log.v(TAG,"-------------- > accessabilityService :: " + accessabilityService);
					if (accessabilityService.equalsIgnoreCase(service)) {
						Log.v(TAG,"We've found the correct setting - accessibility is switched on!");
						return true;
					}
				}
			}
		} else {
			Log.v(TAG,"***ACCESSIBILIY IS DISABLED***");
		}

		return accessibilityFound;
	}

}
