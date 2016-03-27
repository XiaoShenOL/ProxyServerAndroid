package com.oplay.nohelper.loader;


import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.oplay.nohelper.assist.AESCrypt;

import java.lang.reflect.Type;

/**
 * @author zyq 16-3-9
 */
public class HttpDataLoader {

	public static final String AES_KEY = "3Ce7671Ff686D51d";
	private static final Gson GSON = new GsonBuilder()
			.serializeNulls()
			.create();

	public static <T> T fromJson(String json, Class<T> classOfT) {
		try {
			Log.d("HttpDataLoader","加密后结果是："+json);
			String result = AESCrypt.decrypt(AES_KEY, json);
			Log.d("HttpDataLoader", "解密后结果是：" + result);
			return GSON.fromJson(result, classOfT);
		} catch (Exception e) {
			Log.e("error",e.fillInStackTrace().toString());
		}
		return null;


	}

	public static <T> T fromJson(String json, Type type) {
		return GSON.fromJson(json, type);
	}

	public static String toJson(Object object) {
		return GSON.toJson(object);
	}
}
