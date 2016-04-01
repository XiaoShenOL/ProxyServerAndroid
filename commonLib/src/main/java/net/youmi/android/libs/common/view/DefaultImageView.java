package net.youmi.android.libs.common.view;

import android.content.Context;
import android.widget.ImageView;

import net.youmi.android.libs.common.util.Util_Res_Base64_Parser;

/**
 * 默认ImageView
 * 
 * @author zhitaocai edit on 2014-7-2
 * 
 */
public class DefaultImageView extends ImageView {

	/**
	 * 如果可以从base64中获取图片就显示，否则显示默认的id
	 * 
	 * @param context
	 * @param base64Str
	 * @param defaultResId
	 */
	public DefaultImageView(Context context, String base64Str, int defaultResId) {
		super(context);
		try {
			this.setImageBitmap(Util_Res_Base64_Parser.decodeBitmapFromBase64(base64Str));
		} catch (Throwable e) {
			this.setImageResource(defaultResId);
		}
		setScaleType(ScaleType.CENTER_INSIDE);
	}
}