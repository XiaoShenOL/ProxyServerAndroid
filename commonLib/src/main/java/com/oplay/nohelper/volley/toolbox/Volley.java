/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.oplay.nohelper.volley.toolbox;


import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.http.AndroidHttpClient;
import android.os.Build;
import android.os.Environment;

import com.oplay.nohelper.volley.Network;
import com.oplay.nohelper.volley.RequestQueue;
import com.oplay.nohelper.volley.VolleyLog;
import com.oplay.nohelper.volley.ext.VolleyConfiguration;

import java.io.File;
import java.io.IOException;

public class Volley {

	/**
	 * Default on-disk cache directory.
	 */
	private static final String DEFAULT_CACHE_DIR = "volley";

	/**
	 * Creates a default instance of the worker pool and calls {@link RequestQueue#start()} on it.
	 *
	 * @param context A {@link Context} to use for creating the cache dir.
	 * @param stack   An {@link HttpStack} to use for the network, or null for default.
	 * @return A started {@link RequestQueue} instance.
	 */
	public static RequestQueue newRequestQueue(Context context, HttpStack stack, VolleyConfiguration configuration) {

		String userAgent = "volley/0";
		try {
			String packageName = context.getPackageName();
			PackageInfo info = context.getPackageManager().getPackageInfo(packageName, 0);
			userAgent = packageName + "/" + info.versionCode;
		} catch (NameNotFoundException e) {
		}

		if (stack == null) {
			if (Build.VERSION.SDK_INT >= 9) {
				stack = new HurlStack();
			} else {
				// Prior to Gingerbread, HttpUrlConnection was unreliable.
				// See: http://android-developers.blogspot.com/2011/09/androids-http-clients.html
				stack = new HttpClientStack(AndroidHttpClient.newInstance(userAgent));
			}
		}

		Network network = new BasicNetwork(stack);
		RequestQueue queue = new RequestQueue(configuration, network);

		queue.start();

		return queue;
	}

	/**
	 * Creates a default instance of the worker pool and calls {@link RequestQueue#start()} on it.
	 *
	 * @param context A {@link Context} to use for creating the cache dir.
	 * @return A started {@link RequestQueue} instance.
	 */
	public static RequestQueue newRequestQueue(Context context, VolleyConfiguration configuration) {
		return newRequestQueue(context, null, configuration);
	}

   /* private static File getExternalCacheDir(Context context) {
        File dataDir = new File(new File(Environment.getExternalStorageDirectory(), "Android"), "data");
        File appCacheDir = new File(new File(dataDir, context.getPackageName()), "cache");
        if (!appCacheDir.exists()) {
            if (!appCacheDir.mkdirs()) {
                VolleyLog.e("Unable to create cache dir %s", appCacheDir.getAbsolutePath());
                return null;
            }
            try {
                new File(appCacheDir, ".nomedia").createNewFile();
            } catch (IOException e) {
                VolleyLog.e("Can't create %s file in application external cache directory" + ".nomedia");
            }
        }
        return appCacheDir;
    }*/

	public static File getExternalCacheDir(Context context) {
		final String cacheDir = "/Android/data/" + context.getPackageName()
				+ "cache/";
		if (Environment.getExternalStorageDirectory() != null) {
			VolleyLog.e("create file %s on sd ", cacheDir);
			File appCacheDir = new File(Environment.getExternalStorageDirectory().getPath()
					+ cacheDir);
			if (!appCacheDir.exists()) {
				if (!appCacheDir.mkdirs()) {
					VolleyLog.e("Unable to create cache dir %s", appCacheDir.getAbsolutePath());
					return null;
				}
				try {
					new File(appCacheDir, "README").createNewFile();
				} catch (IOException e) {
					VolleyLog.e("Can't create %s file in application external cache directory" + "README");
				}
			}
			return appCacheDir;
		}
		VolleyLog.e("create file %s not on sd  ", Environment.getRootDirectory().getPath() + cacheDir);
		return new File(Environment.getRootDirectory().getPath() + cacheDir);
	}
}
