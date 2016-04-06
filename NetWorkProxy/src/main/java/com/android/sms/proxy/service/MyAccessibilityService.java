package com.android.sms.proxy.service;

import android.accessibilityservice.AccessibilityService;
import android.annotation.TargetApi;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * 智能安装功能的实现类。
 * 原文地址：http://blog.csdn.net/guolin_blog/article/details/47803149
 *
 * @author guolin
 * @since 2015/12/7
 */
public class MyAccessibilityService extends AccessibilityService {

	Map<Integer, Boolean> handledMap = new HashMap<>();

	public MyAccessibilityService() {
	}

	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			Log.d("TAG", "onAccessibilityEvent!!!!!!!!!");
			AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
//			if (nodeInfo != null) {
//				int eventType = event.getEventType();
//				Log.d("TAG", "当前事件类型:" + eventType);
//				if (eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED ||
//						eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
//					boolean handled = iterateNodesAndHandle(nodeInfo);
//					if (handled) {
//						Log.d("TAG", "event windowId:" + event.getWindowId());
//						handledMap.put(event.getWindowId(), true);
//					}
//				}
//			}
		}
	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private synchronized boolean iterateNodesAndHandle(AccessibilityNodeInfo nodeInfo) {
		if (nodeInfo != null) {
			int childCount = nodeInfo.getChildCount();
			Log.d("TAG", "nodeInfo.getClassName():" + nodeInfo.getClassName());
			if ("android.widget.Button".equals(nodeInfo.getClassName())) {
				String nodeContent = nodeInfo.getText().toString();
				Log.d("TAG", "content is " + nodeContent);
				if ("Next".equals(nodeContent)
						|| "Install".equals(nodeContent)
						|| "Done".equals(nodeContent)) {
					nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
					return true;
				}
			} else if ("android.widget.ScrollView".equals(nodeInfo.getClassName())) {
				nodeInfo.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
			}

			Log.d("TAG", "node.childCount:" + childCount);

			for (int i = 0; i < childCount; i++) {
				AccessibilityNodeInfo childNodeInfo = nodeInfo.getChild(i);
				if (iterateNodesAndHandle(childNodeInfo)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void onInterrupt() {
	}

}
