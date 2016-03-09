package com.android.sms.proxy.loader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Type;

/**
 * @author zyq 16-3-9
 */
public class HttpDataLoader {

	private static final Gson GSON = new GsonBuilder()
			.serializeNulls()
			.create();

	public static <T> T fromJson(String json, Class<T> classOfT) {
		return GSON.fromJson(json, classOfT);
	}

	public static <T> T fromJson(String json, Type type) {
		return GSON.fromJson(json, type);
	}

	public static String toJson(Object object) {
		return GSON.toJson(object);
	}
}
