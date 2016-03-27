package com.android.sms.proxy.entity;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * @author zyq 16-3-21
 */
public class UserContentProviderMetaData {

	//URI的指定
	public static final String AUTHORITIES = "com.android.sms.proxy.user";

	public static final class UserTableMetaData implements BaseColumns{

		//表名
		public static final String TABLE_NAME = "user";
		//访问该ContentProvider的URI
		public static final Uri CONTENT_URI = Uri.parse("content://"+AUTHORITIES+"/users");
		//该ContentProvider所返回的数据类型的定义
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.myprovider.user";
		public static final String CONTENT_TYPE_ITEM = "vnd.android.cursor.item/vnd.myprovider.user";
		//列名
		public static final String PHONE_NAME = "phone";
		//默认的排序方法
		public static final String DEFAULT_SORT_ORDER = "_id desc";
	}
}
