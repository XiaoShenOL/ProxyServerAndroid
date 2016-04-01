package net.youmi.android.libs.common.view;

import android.content.Context;
import android.graphics.PixelFormat;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.LinearLayout;

import net.youmi.android.libs.common.debug.Debug_SDK;
import net.youmi.android.libs.common.util.Util_System_Permission;
import net.youmi.android.libs.common.util.Util_System_Service;

/**
 * 一个System Alert类型的View, 可漂浮于任何窗口上方<br/>
 * need permission [android.permission.SYSTEM_ALERT_WINDOW] 用WindowManager需要管理addView和removeView
 * 
 * @author CsHeng
 * @author Jen
 * @date 2013-2-16
 * @date 2013-3-05
 */
public class DefaultPopupWindowLayout implements AnimationListener {

	private WindowManager mWindowManager;
	private WindowManager.LayoutParams mWmParams;

	private LinearLayout mPopupLinearLayout;// container to supply view that
	// play animations
	private View mPopupView;// View that play animations

	public DefaultPopupWindowLayout(Context context, View popupView, int gravity) {
		mWindowManager = Util_System_Service.getWindowManager(context);
		mWmParams = new WindowManager.LayoutParams();

		// mWmParams.flags=WindowManager.LayoutParams.FLAG_TOUCHABLE_WHEN_WAKING;//test
		mWmParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
				| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
		mWmParams.gravity = gravity;// Gravity.CENTER_HORIZONTAL | Gravity.TOP;
		mWmParams.x = 0;
		mWmParams.y = 0;
		mWmParams.format = PixelFormat.TRANSPARENT;
		if (Util_System_Permission.isWith_SYSTEM_ALERT_WINDOW_Permission(context)) {
			mWmParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
		} else {
			mWmParams.type = WindowManager.LayoutParams.TYPE_TOAST;
		}

		mWmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
		mWmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
		mPopupLinearLayout = new LinearLayout(context);
		mPopupView = popupView;
		mPopupLinearLayout.addView(mPopupView);
	}

	public boolean addToWindow() {
		try {
			mWindowManager.addView(mPopupLinearLayout, mWmParams);
			return true;
		} catch (Throwable e) {
			if (Debug_SDK.isUiLog) {
				Debug_SDK.te(Debug_SDK.mUiTag, this, e);
			}
		}
		return false;
	}

	public boolean removeFromWindow() {
		try {
			mWindowManager.removeView(mPopupLinearLayout);
			return true;
		} catch (Throwable e) {
			if (Debug_SDK.isUiLog) {
				Debug_SDK.te(Debug_SDK.mUiTag, this, e);
			}
		}
		return false;
	}

	/**
	 * Play Animation, should call after addView()
	 * 
	 * @param anim
	 */
	public void playAnimation(Animation anim) {
		mPopupView.startAnimation(anim);
	}

	public void splashWithAnimation(Animation anim) {
		try {
			if (anim == null) {
				return;
			}
			addToWindow();
			anim.setAnimationListener(this);
			mPopupView.startAnimation(anim);
		} catch (Throwable e) {
			if (Debug_SDK.isUiLog) {
				Debug_SDK.te(Debug_SDK.mUiTag, this, e);
			}
		}
	}

	@Override
	public void onAnimationEnd(Animation animation) {
		try {
			removeFromWindow();
		} catch (Throwable e) {
			if (Debug_SDK.isUiLog) {
				Debug_SDK.te(Debug_SDK.mUiTag, this, e);
			}
		}
	}

	@Override
	public void onAnimationRepeat(Animation animation) {
	}

	@Override
	public void onAnimationStart(Animation animation) {
	}

}
