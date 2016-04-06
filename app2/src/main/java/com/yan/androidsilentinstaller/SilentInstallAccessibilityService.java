package com.yan.androidsilentinstaller;

import android.accessibilityservice.AccessibilityService;
import android.annotation.TargetApi;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Yan Gao on 3/31/16.
 */
public class SilentInstallAccessibilityService extends AccessibilityService {

    Map<Integer, Boolean> handledMap = new HashMap<>();

    public SilentInstallAccessibilityService() {
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        AccessibilityNodeInfo nodeInfo = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            nodeInfo = event.getSource();
            if (nodeInfo != null) {
                int eventType = event.getEventType();
                if (eventType== AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED ||
                        eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                    if (handledMap.get(event.getWindowId()) == null) {
                        boolean handled = iterateNodesAndHandle(nodeInfo);
                        Log.d("TAG","是否找到要找的地方:"+handled);
                        if (handled) {
                            handledMap.put(event.getWindowId(), true);
                        }
                    }
                }
            }
        }

    }
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private boolean iterateNodesAndHandle(AccessibilityNodeInfo nodeInfo) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            if (nodeInfo != null) {
                int childCount = nodeInfo.getChildCount();
                if ("android.widget.Button".equals(nodeInfo.getClassName())) {
                    String nodeContent = nodeInfo.getText().toString();
                    Log.d("TAG", "content is " + nodeContent);
                    if ("安装".equals(nodeContent)
                            || "完成".equals(nodeContent)
                            || "确定".equals(nodeContent)) {
                        nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        return true;
                    }
                } else if ("android.widget.ScrollView".equals(nodeInfo.getClassName())) {
                    nodeInfo.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
                }
                Log.d("TAG","node.childCount:"+childCount);
                for (int i = 0; i < childCount; i++) {
                    AccessibilityNodeInfo childNodeInfo = nodeInfo.getChild(i);
                    if (iterateNodesAndHandle(childNodeInfo)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void onInterrupt() {

    }
}
