package net.youmi.android.libs.common.template;

import net.youmi.android.libs.common.debug.Debug_SDK;

import java.util.ArrayList;
import java.util.List;

public abstract class Template_ListenersManager<T> {

    private List<T> mListeners;

    protected synchronized List<T> getListeners() {
        if (mListeners == null) {
            mListeners = new ArrayList<T>();
        }
        return mListeners;
    }

    /**
     * 注册监听器<br/>
     * 注册之后，所有下载过程都会进行通知，需要通过回调参数中的FileDownloadTask来辨别是否是需要的监听。<br/>
     * 建议在onCreate等时机调用注册。<br/>
     * 必须调用removeListener进行注销。<br/>
     *
     * @param listener
     */
    public void registerListener(T listener) {
        try {
            if (listener != null) {
                final List<T> list = getListeners();
                if (!list.contains(listener)) {
                    list.add(listener);
                }
            }
        } catch (Throwable e) {
            if (Debug_SDK.isDebug) {
                Debug_SDK.de(this.getClass().getSimpleName(), e);
            }
        }
    }

    /**
     * 注销监听器。<br/>
     * 建议在onDestory等时机调用注销。<br/>
     *
     * @param listener
     */
    public void removeListener(T listener) {
        try {
            if (listener != null) {
                final List<T> list = getListeners();
                list.remove(listener);
            }
        } catch (Throwable e) {
            if (Debug_SDK.isDebug) {
                Debug_SDK.de(this.getClass().getSimpleName(), e);
            }
        }
    }

}
