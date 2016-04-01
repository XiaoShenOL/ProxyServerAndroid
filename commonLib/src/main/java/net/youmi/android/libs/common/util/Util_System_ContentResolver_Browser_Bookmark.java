//package net.youmi.android.libs.common.util;
//
//import android.content.ContentResolver;
//import android.content.ContentValues;
//import android.content.Context;
//import android.net.Uri;
//import android.provider.Browser;
//
//import net.youmi.android.libs.common.debug.Debug_SDK;
//
///**
// * 浏览器书签（未经测试）
// *
// * @author zhitaocai edit on 2014-7-15
// *
// */
//public class Util_System_ContentResolver_Browser_Bookmark {
//
//	public static boolean addBrowserBookmark(Context context, String title, String url) {
//		try {
//			if (!Util_System_Permission.isWith_WRITE_HISTORY_BOOKMARKS(context)) {
//				if (Debug_SDK.isUtilLog) {
//					Debug_SDK
//							.te(Debug_SDK.mUtilTag, Util_System_ContentResolver_Browser_Bookmark.class, "不具有添加浏览器书签权限");
//				}
//				return false;
//			}
//
//			if (title == null) {
//				if (Debug_SDK.isUtilLog) {
//					Debug_SDK.te(Debug_SDK.mUtilTag, Util_System_ContentResolver_Browser_Bookmark.class,
//							"添加书签失败:title为空");
//				}
//				return false;
//			}
//
//			if (url == null) {
//				if (Debug_SDK.isUtilLog) {
//					Debug_SDK
//							.te(Debug_SDK.mUtilTag, Util_System_ContentResolver_Browser_Bookmark.class, "添加书签失败:url为空");
//				}
//				return false;
//			}
//
//			ContentValues cv = null;
//			ContentResolver contentResolver = context.getContentResolver();
//			String where = "title=?";
//			String[] arrayOfString = { title };
//
//			contentResolver.delete(Browser.BOOKMARKS_URI, where, arrayOfString);
//			cv = new ContentValues();
//			cv.put("bookmark", Integer.valueOf(1));
//			cv.put("title", title);
//			cv.put("url", url);
//
//			Uri uri = contentResolver.insert(Browser.BOOKMARKS_URI, cv);
//
//			if (uri == null) {
//				if (Debug_SDK.isUtilLog) {
//					Debug_SDK.te(Debug_SDK.mUtilTag, Util_System_ContentResolver_Browser_Bookmark.class,
//							"添加书签失败:结果uri为空");
//				}
//				return false;
//			} else {
//				if (Debug_SDK.isUtilLog) {
//					Debug_SDK.td(Debug_SDK.mUtilTag, Util_System_ContentResolver_Browser_Bookmark.class,
//							"添加书签成功:结果uri不为空:%s", uri.toString());
//				}
//				return true;
//			}
//
//		} catch (Throwable e) {
//			if (Debug_SDK.isUtilLog) {
//				Debug_SDK.te(Debug_SDK.mUtilTag, Util_System_ContentResolver_Browser_Bookmark.class, e);
//			}
//		}
//
//		return false;
//	}
//
//}
