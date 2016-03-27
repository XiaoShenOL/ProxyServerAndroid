package net.luna.common.util;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import java.lang.reflect.Field;

/**
 * ScreenUtils
 * <ul>
 * <strong>Convert between dp and sp</strong>
 * <li>{@link ScreenUtils#dpToPx(Context, float)}</li>
 * <li>{@link ScreenUtils#pxToDp(Context, float)}</li>
 * </ul>
 */
public class ScreenUtils {

    /**
     * 低密度 120
     */
    public static final int DENSITY_LEVEL_LOW = 120;

    /**
     * 标准密度 160
     */
    public static final int DENSITY_LEVEL_MEDIUM = 160;

    /**
     * 高密度 240
     */
    public static final int DENSITY_LEVEL_HIGH = 240;

    /**
     * 超高密度 320
     */
    public static final int DENSITY_LEVEL_XHIGH = 320;

    /**
     * 当前屏幕分辨率的密度 级别 LEVEL 可能不是真正的物理密度
     */
    private int mDisplayDensityDpi = DENSITY_LEVEL_MEDIUM;

    /**
     * 当前屏幕物理分辨率的密度 级别 LEVEL 真正的物理密度
     */
    private int mScreenDensityDpi = DENSITY_LEVEL_MEDIUM;

    /**
     * 是否支持自适应分辨率。 如果为true，则从系统得到的屏幕分辨率为DENSITY_DEFAULT，即320*480级别。(density不受影响)
     * sdk默认所有处理都只需要根据160级别的density设计，不需要经过缩放再处理。
     * 如果为false，则从系统得到的屏幕分辨率为实际分辨率。(density不受影响) sdk所有有关分辨率的处理都需要经过缩放后再处理。
     */
    private boolean mIsSupportAnyDensity = true;

    /**
     * 手机屏幕的实际宽度，比高度小
     */
    private int mScreenW;

    /**
     * 手机屏幕的实际高度，大于宽度
     */
    private int mScreenH;

    // private int _targetMinSdkVersion = 0;
    /**
     * 系统屏幕密度倍数
     */
    private float mDensity = 1.0f;

    /**
     * 当前状态屏幕分辨率 水平宽度，可能不是真正的分辨率，但必是最适合屏幕的分辨率
     */
    private int mDisplayWidth;

    /**
     * 当前状态屏幕分辨率 垂直高度，可能不是真正的分辨率，但必是最适合屏幕的分辨率
     */
    private int mDisplayHeight;

    private ScreenUtils() {
        throw new AssertionError();
    }

    public static float dpToPx(Context context, float dp) {
        if (context == null) {
            return -1;
        }
        return dp * context.getResources().getDisplayMetrics().density;
    }

    public static float pxToDp(Context context, float px) {
        if (context == null) {
            return -1;
        }
        return px / context.getResources().getDisplayMetrics().density;
    }

    public static float dpToPxInt(Context context, float dp) {
        return (int) (dpToPx(context, dp) + 0.5f);
    }

    public static float pxToDpCeilInt(Context context, float px) {
        return (int) (pxToDp(context, px) + 0.5f);
    }

    public static ScreenUtils getInstance(Context context) {
        return createDisplayInfo(context);
    }

    protected ScreenUtils(DisplayMetrics resourceDM, DisplayMetrics systemDM) {
        // 记录当前屏幕的最合适宽高，用于显示和加载资源。
        mDisplayWidth = resourceDM.widthPixels;// 当前的分辨率w//可能不是系统的分辨率
        mDisplayHeight = resourceDM.heightPixels;// 当前的分辨率h//可能不是系统的分辨率

        // 记录屏幕密度
        mDensity = resourceDM.density;// 当前density//可能不是系统的density

        // 默认160
        mDisplayDensityDpi = DENSITY_LEVEL_MEDIUM;// 先设置为默认的160，然后接下来用反射处理。如果是1.5的话，即使反射不到，也是160的

        try {
            // 如果手机版本为4或以上
            Field f = resourceDM.getClass().getField("densityDpi");
            if (f != null) {
                mDisplayDensityDpi = f.getInt(resourceDM);
            }

        } catch (Throwable e) {
        }

        mScreenDensityDpi = DENSITY_LEVEL_MEDIUM;// 先设置为默认的160,然后使用反射得到物理密度
        try {
            // 如果手机版本为4或以上
            Field f = systemDM.getClass().getField("densityDpi");
            if (f != null) {
                mScreenDensityDpi = f.getInt(systemDM);
            }
        } catch (Throwable e) {
        }

        if (mDisplayDensityDpi == DENSITY_LEVEL_MEDIUM) {
            // 有可能是自适应模式
            // 屏幕的真正分辨率，用于中间层记录
            mScreenW = Math.round(systemDM.widthPixels * systemDM.density);// 屏幕物理分辨率宽度
            mScreenH = Math.round(systemDM.heightPixels * systemDM.density); // 屏幕物理分辨率高度

            mIsSupportAnyDensity = true;// 自适应分辨率//也可能是320*480的机子，但也算上是自适应模式吧
        } else {
            mScreenW = mDisplayWidth;
            mScreenH = mDisplayHeight;
            mIsSupportAnyDensity = false;// 非自适应分辨率//只要densityDip!=160，就是非自适应模式的
        }

        if (mScreenW > mScreenH) {
            // 保证屏幕宽度比高度小
            int temp = mScreenW;
            mScreenW = mScreenH;
            mScreenH = temp;
        }
    }

    private static ScreenUtils createDisplayInfo(Context context) {

        long nt = System.currentTimeMillis();
        try {
            DisplayMetrics systemDM = new DisplayMetrics();
            // activity.getWindowManager().getDefaultDisplay()
            // .getMetrics(systemDM);

            // 以下方法待测试，未证实(从tapjoy那里抄的)
            WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            windowManager.getDefaultDisplay().getMetrics(systemDM);

            DisplayMetrics resourceDM = context.getResources().getDisplayMetrics();

            return new ScreenUtils(resourceDM, systemDM);

        } catch (Throwable e) {
        } finally {
        }
        return null;
    }

    boolean isSupportAnyDensity() {
        return mIsSupportAnyDensity;
    }

    /**
     * 当前屏幕分辨率 水平宽度
     *
     * @return
     */
    public int getDisplayWidth() {
        return mDisplayWidth;
    }

    /**
     * 当前屏幕分辨率 垂直高度
     *
     * @return
     */
    public int getDisplayHeight() {
        return mDisplayHeight;
    }

    /**
     * 获取屏幕分辨率密度级别【虚拟】 120、160、240、320
     *
     * @return
     */
    public int getDisplay_DensityLevel() {
        return mDisplayDensityDpi;
    }

    /**
     * 获取屏幕分辨率密度级别【物理】 120、160、240、320
     *
     * @return
     */
    public int getScreen_DensityLevel() {
        return mScreenDensityDpi;
    }

    /**
     * 获取屏幕分辨率密度比
     *
     * @return
     */
    public float getDensity() {
        return mDensity;
    }

    /**
     * 获取屏幕宽高中最短的一边
     *
     * @return
     */
    int getMinDisplayLength() {
        if (mDisplayWidth > mDisplayHeight) {
            return mDisplayHeight;
        }
        return mDisplayWidth;
    }

    /**
     * 获取屏幕宽高中最长的一边
     *
     * @return
     */
    int getMaxLength() {
        if (mDisplayWidth > mDisplayHeight) {
            return mDisplayWidth;
        }
        return mDisplayHeight;
    }

    /**
     * 获取屏幕的宽度，规则是屏幕宽高中最短的一边
     *
     * @return
     */
    public int getScreenWidth() {
        return mScreenW;
    }

    /**
     * 获取屏幕的高度，规则是屏幕宽高中最长的一边
     *
     * @return
     */
    public int getScreenHeight() {
        return mScreenH;
    }

}
