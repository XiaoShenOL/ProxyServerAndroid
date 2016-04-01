/**
 * 
 * project: OPlay
 *
 * 
 * ========================================================================
 * amend date			amend user			amend reason
 * 2013-2-16			    CsHeng		
 * 		
 */
package net.youmi.android.libs.common.animator;

import android.content.Context;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

/**
 * 放点动画资源
 * 
 * @author CsHeng
 * @date 2013-2-16
 * 
 */
public class Anim_Generator {

	/**
	 * 坠落回拉动画-进场
	 * 
	 * @param context
	 * @param inDuration
	 * @return
	 */
	public static Animation getDownReverse_In(Context context, int inDuration) {
		Animation comeInAnimation = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0,
				Animation.RELATIVE_TO_PARENT, 0, Animation.RELATIVE_TO_PARENT, Animation.INFINITE,
				Animation.RELATIVE_TO_PARENT, 0);
		comeInAnimation.setDuration(inDuration);
		return comeInAnimation;
	}

	/**
	 * 坠落回拉动画-出场
	 * 
	 * @param context
	 * @param outDuration
	 * @return
	 */
	public static Animation getDownReverse_Out(Context context, int outDuration) {
		Animation comeOutAnimation = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0,
				Animation.RELATIVE_TO_PARENT, 0, Animation.RELATIVE_TO_PARENT, 0, Animation.RELATIVE_TO_PARENT,
				Animation.INFINITE);
		comeOutAnimation.setDuration(outDuration);
		return comeOutAnimation;
	}

	/**
	 * 从上而下坠落回拉动画
	 * 
	 * @param context
	 * @param inDuration
	 *            动画渐入时长
	 * @param outDuration
	 *            动画渐出时长
	 * @param duration
	 *            动画停留静止时间
	 * @return
	 */
	public static AnimationSet getDownReverse(Context context, int inDuration, int duration, int outDuration) {
		AnimationSet animationSet = new AnimationSet(true); // shareInterpolator
		animationSet.setInterpolator(AnimationUtils.loadInterpolator(context,
				android.R.anim.accelerate_decelerate_interpolator));
		Animation comeInAnimation = getDownReverse_In(context, inDuration);
		comeInAnimation.setFillAfter(true);

		Animation comeOutAnimation = getDownReverse_Out(context, outDuration);
		comeOutAnimation.setFillAfter(true);
		comeOutAnimation.setStartOffset(duration + inDuration);
		animationSet.addAnimation(comeInAnimation);
		animationSet.addAnimation(comeOutAnimation);
		return animationSet;
	}

	/**
	 * 渐入渐出动画-进场
	 * 
	 * @return
	 */
	public static Animation getCenterScale_In(Context context, int inDuration) {

		AnimationSet animationSet = new AnimationSet(false); // shareInterpolator
		animationSet.setInterpolator(AnimationUtils.loadInterpolator(context,
				android.R.anim.accelerate_decelerate_interpolator));
		Animation comeInAnimation = new AlphaAnimation(0.5f, 1f);
		comeInAnimation.setDuration(inDuration);
		comeInAnimation.setFillAfter(true);

		Animation middleAnimation = new ScaleAnimation(0.8f, // 动画起始时 X坐标上的伸缩尺寸
				1.0f,// 动画结束时 X坐标上的伸缩尺寸
				0.8f, // 动画起始时Y坐标上的伸缩尺寸
				1.0f,// 动画结束时Y坐标上的伸缩尺寸
				Animation.RELATIVE_TO_SELF,//
				0.5f,// 动画相对于物件的X坐标的开始位置
				Animation.RELATIVE_TO_SELF, //
				0.5f);// 动画相对于物件的Y坐标的开始位置
		middleAnimation.setDuration(inDuration);
		middleAnimation.setFillAfter(true);

		animationSet.addAnimation(comeInAnimation);
		animationSet.addAnimation(middleAnimation);
		return animationSet;
	}

	/**
	 * 渐入渐出动画-出场
	 * 
	 * @return
	 */
	public static Animation getCenterScale_Out(Context context, int outDuration) {
		Animation comeOutAnimation = new ScaleAnimation(1.0f, // 动画起始时 X坐标上的伸缩尺寸
				0.0f,// 动画结束时 X坐标上的伸缩尺寸
				1.0f, // 动画起始时Y坐标上的伸缩尺寸
				0.0f,// 动画结束时Y坐标上的伸缩尺寸
				Animation.RELATIVE_TO_SELF,//
				0.5f,// 动画相对于物件的X坐标的开始位置
				Animation.RELATIVE_TO_SELF, //
				0.5f);// 动画相对于物件的Y坐标的开始位置

		comeOutAnimation.setDuration(outDuration);
		return comeOutAnimation;
	}

	/**
	 * 渐入，停止，渐出动画
	 * 
	 * @param context
	 * @param inDuration
	 *            动画渐入时长
	 * @param outDuration
	 *            动画渐出时长
	 * @param duration
	 *            动画停留静止时间
	 * @return
	 */
	public static AnimationSet getCenterScale(Context context, int inDuration, int duration, int outDuration) {
		AnimationSet animationSet = new AnimationSet(false); // shareInterpolator
		animationSet.setInterpolator(AnimationUtils.loadInterpolator(context,
				android.R.anim.accelerate_decelerate_interpolator));
		Animation comeInAnimation = new AlphaAnimation(0.5f, 1f);
		comeInAnimation.setDuration(inDuration);
		comeInAnimation.setFillAfter(true);

		Animation middleAnimation = new ScaleAnimation(0.8f, // 动画起始时 X坐标上的伸缩尺寸
				1.0f,// 动画结束时 X坐标上的伸缩尺寸
				0.8f, // 动画起始时Y坐标上的伸缩尺寸
				1.0f,// 动画结束时Y坐标上的伸缩尺寸
				Animation.RELATIVE_TO_SELF,//
				0.5f,// 动画相对于物件的X坐标的开始位置
				Animation.RELATIVE_TO_SELF, //
				0.5f);// 动画相对于物件的Y坐标的开始位置
		middleAnimation.setDuration(inDuration);
		middleAnimation.setFillAfter(true);

		Animation comeOutAnimation = getCenterScale_Out(context, outDuration);
		comeOutAnimation.setFillAfter(true);
		comeOutAnimation.setStartOffset(duration + inDuration);
		animationSet.addAnimation(comeInAnimation);
		animationSet.addAnimation(middleAnimation);
		animationSet.addAnimation(comeOutAnimation);
		return animationSet;
	}

}
