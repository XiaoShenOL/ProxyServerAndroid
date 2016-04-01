package com.oplay.nohelper.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * @author zyq 16-3-31
 */
public class Util_Sp {
	public static final String SP_TABLE_NAME = "operator";
	public static final String SP_INFO_KEY = "info";

	public static boolean isOperatorInfoExist(Context context, String operatorInfo) {
		SharedPreferences sp = context.getSharedPreferences(SP_TABLE_NAME, Context.MODE_PRIVATE);
		String info = sp.getString(SP_INFO_KEY, "");
		if (operatorInfo.equals(info)) {
			return true;
		}
		return false;
	}

	public static void saveOperatorsInfo(Context context, String message) {
		SharedPreferences sp = context.getSharedPreferences(SP_TABLE_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor et = sp.edit();
		et.putString(SP_INFO_KEY, message);
		et.commit();
	}
}
