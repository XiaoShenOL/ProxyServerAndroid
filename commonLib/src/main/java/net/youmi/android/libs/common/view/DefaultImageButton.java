package net.youmi.android.libs.common.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.widget.ImageButton;

import net.youmi.android.libs.common.debug.Debug_SDK;
import net.youmi.android.libs.common.util.Util_Res_Base64_Parser;

/**
 * 默认ImageButotn(可以通过base64码构造bitmap)
 * 
 * @author zhitaocai edit on 2014-7-2
 * 
 */
public class DefaultImageButton extends ImageButton {

	/**
	 * 传入bitmap的base64码
	 * 
	 * @param context
	 * @param bitmapBase64
	 *            可用|不可用状态下的图标
	 */
	public DefaultImageButton(Context context, String bitmapBase64) {
		super(context);
		try {
			this.setScaleType(ScaleType.CENTER_CROP);
			Bitmap bm = Util_Res_Base64_Parser.decodeBitmapFromBase64(bitmapBase64);
			if (bm != null) {
				Drawable drawable = new BitmapDrawable(getResources(), bm);
				initStates(drawable, drawable);
			}
		} catch (Throwable e) {
			if (Debug_SDK.isUiLog) {
				Debug_SDK.te(Debug_SDK.mUiTag, this, e);
			}
		}
	}

	/**
	 * 传入id生成按钮
	 * 
	 * @param context
	 * @param dfImgResID
	 */
	public DefaultImageButton(Context context, int dfImgResID) {
		super(context);
		try {
			this.setScaleType(ScaleType.CENTER_CROP);
			Drawable drawable = context.getResources().getDrawable(dfImgResID);
			initStates(drawable, drawable);
		} catch (Throwable e) {
			if (Debug_SDK.isUiLog) {
				Debug_SDK.te(Debug_SDK.mUiTag, this, e);
			}
		}
	}

	/**
	 * 初始化按钮
	 * 
	 * @param context
	 * @param dfImgResID_en
	 *            可用状态下的默认图标，用于替换
	 * @param bitmapBase64_able
	 *            可用状态下的图标
	 * @param dfImgResID_disen
	 *            不可用状态下的默认图标,用于替换
	 * @param bitmapBase64_disable
	 *            不可用状态下的图标
	 */
	public DefaultImageButton(Context context, String bitmapBase64_able, String bitmapBase64_disable) {
		super(context);
		try {
			this.setScaleType(ScaleType.CENTER_CROP);

			Drawable drawable_en = null;
			Bitmap bm_en = Util_Res_Base64_Parser.decodeBitmapFromBase64(bitmapBase64_able);
			if (bm_en != null) {
				drawable_en = new BitmapDrawable(getResources(), bm_en);
			}

			Drawable drawable_disen = null;
			Bitmap bm_disen = Util_Res_Base64_Parser.decodeBitmapFromBase64(bitmapBase64_disable);
			if (bm_disen != null) {
				drawable_disen = new BitmapDrawable(getResources(), bm_disen);
			}

			initStates(drawable_en, drawable_disen);

		} catch (Throwable e) {
			if (Debug_SDK.isUiLog) {
				Debug_SDK.te(Debug_SDK.mUiTag, this, e);
			}
		}
	}

	/**
	 * 初始化按钮
	 * 
	 * @param context
	 * @param dfImgResID_able
	 *            可用状态下的默认图标，用于替换
	 * @param bitmapBase64_en
	 *            可用状态下的图标
	 * @param dfImgResID_disable
	 *            不可用状态下的默认图标,用于替换
	 * @param bitmapBase64_disen
	 *            不可用状态下的图标
	 */
	public DefaultImageButton(Context context, int dfImgResID_able, int dfImgResID_disable) {
		super(context);
		try {
			this.setScaleType(ScaleType.CENTER_CROP);

			Drawable drawable_en = context.getResources().getDrawable(dfImgResID_able);
			Drawable drawable_disen = context.getResources().getDrawable(dfImgResID_disable);

			initStates(drawable_en, drawable_disen);

		} catch (Throwable e) {
			if (Debug_SDK.isUiLog) {
				Debug_SDK.te(Debug_SDK.mUiTag, this, e);
			}
		}

	}

	/**
	 * 设置按钮按下和未按下时的状态
	 * 
	 * @param enableDrawable
	 * @param disenableDrawable
	 */
	private void initStates(Drawable enableDrawable, Drawable disenableDrawable) {

		try {
			setImageDrawable(new ImageDrawable(enableDrawable, disenableDrawable));
		} catch (Throwable e) {
			if (Debug_SDK.isUiLog) {
				Debug_SDK.te(Debug_SDK.mUiTag, this, e);
			}
		}

		try {
			setBackgroundDrawable(new BackgroundDrawable());
		} catch (Throwable e) {
			if (Debug_SDK.isUiLog) {
				Debug_SDK.te(Debug_SDK.mUiTag, this, e);
			}
		}
	}

	/**
	 * 设置按钮按下时和未按下时的图标选择
	 * 
	 * @author zhitaocai
	 */
	private class ImageDrawable extends StateListDrawable {

		ImageDrawable(Drawable enableDrawable, Drawable disenableDrawable) {

			addState(PRESSED_ENABLED_STATE_SET, enableDrawable);
			addState(ENABLED_STATE_SET, enableDrawable);
			addState(EMPTY_STATE_SET, disenableDrawable);
		}

	}

	/**
	 * 设置按钮按下时和未按下时的背景颜色
	 * 
	 * @author zhitaocai
	 */
	private class BackgroundDrawable extends StateListDrawable {

		BackgroundDrawable() {

			Drawable empty = new ColorDrawable(Color.TRANSPARENT);
			Drawable press = new ColorDrawable(Color.parseColor("#661E90FF"));

			addState(PRESSED_ENABLED_STATE_SET, press);
			addState(ENABLED_STATE_SET, empty);
			addState(EMPTY_STATE_SET, empty);
		}

	}
}
