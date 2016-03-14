package com.oplay.nohelper.assist.debug;

import android.util.Log;

/**
 * sdk 测试用输出log类<br>
 * <p/>
 * 支持两套方案：
 * <p/>
 * 1、di,de,dd,dv,dw 等的使用： <br>
 * 和常规的Log使用差不多，输入标签，输入信息，输出Throwable就可以了
 * <p/>
 * 2、ti,te,td,tv,tw 等的使用：<br>
 * 和常规的Log区别在于标签的创建：<br>
 * 需要传入preTag(模块名称)， 以及Object（模块中的某个类）来创建一个标签
 * <p/>
 * <hr>
 * 其他注意注意事项：
 * <ol>
 * <li>比较多，后面在说吧</li>
 * </ol>
 * <hr>
 *
 * @author zhitaocai create on 2014-7-10
 */
class Debug_SDK_Log {

	/**
	 * 是否显示log的全局快关 正式版发布务必为 true 【正式版发布务必为false】
	 */
	public final static boolean isDebug = false;
	/**
	 * 显示log详细信息的文字的颜色
	 * <ul>
	 * <li>{@code Log.DEBUG} : 蓝色</li>
	 * <li>{@code Log.ERROR} : 红色</li>
	 * <li>{@code Log.INFO} : 绿色</li>
	 * <li>{@code Log.VERBOSE} : 黑色</li>
	 * <li>{@code Log.WARN} : 黄色</li>
	 * </ul>
	 */
	protected final static int mInvokeInfoColor = Log.VERBOSE;
	/**
	 * 是否显示调用log的方法的详细信息 如果觉得这个详细信息不需要可以设置为false
	 */
	protected static boolean isEnableToPrintInvokeInfo = false;

	// ////////////////////////////////////////////////////////////////////////////////////
	// 第一套方案:
	// 和常规的Log使用差不多，输入标签，输入信息，输出Throwable就可以了

	// INFO (Custom tag)
	public static void di(String tag, String msg) {
		if (isDebug) {
			printDebugInfo(Log.INFO, tag, null, msg);
		}
	}

	public static void di(String tag, String fmt, Object... args) {
		if (isDebug) {
			printDebugInfo(Log.INFO, tag, null, fmt, args);
		}
	}

	public static void di(String tag, Throwable tr) {
		if (isDebug) {
			printDebugInfo(Log.INFO, tag, tr, null);
		}
	}

	public static void di(String tag, String msg, Throwable tr) {
		if (isDebug) {
			printDebugInfo(Log.INFO, tag, tr, msg);
		}
	}

	public static void di(String tag, Throwable tr, String fmt, Object... args) {
		if (isDebug) {
			printDebugInfo(Log.INFO, tag, tr, fmt, args);
		}
	}

	// ERROR (Custom tag)
	public static void de(String tag, String msg) {
		if (isDebug) {
			printDebugInfo(Log.ERROR, tag, null, msg);
		}
	}

	public static void de(String tag, String fmt, Object... args) {
		if (isDebug) {
			printDebugInfo(Log.ERROR, tag, null, fmt, args);
		}
	}

	public static void de(String tag, Throwable tr) {
		if (isDebug) {
			printDebugInfo(Log.ERROR, tag, tr, null);
		}
	}

	public static void de(String tag, String msg, Throwable tr) {
		if (isDebug) {
			printDebugInfo(Log.ERROR, tag, tr, msg);
		}
	}

	public static void de(String tag, Throwable tr, String fmt, Object... args) {
		if (isDebug) {
			printDebugInfo(Log.ERROR, tag, tr, fmt, args);
		}
	}

	// DEBUG (Custom tag)
	public static void dd(String tag, String msg) {
		if (isDebug) {
			printDebugInfo(Log.DEBUG, tag, null, msg);
		}
	}

	public static void dd(String tag, String fmt, Object... args) {
		if (isDebug) {
			printDebugInfo(Log.DEBUG, tag, null, fmt, args);
		}
	}

	public static void dd(String tag, Throwable tr) {
		if (isDebug) {
			printDebugInfo(Log.DEBUG, tag, tr, null);
		}
	}

	public static void dd(String tag, String msg, Throwable tr) {
		if (isDebug) {
			printDebugInfo(Log.DEBUG, tag, tr, msg);
		}
	}

	public static void dd(String tag, Throwable tr, String fmt, Object... args) {
		if (isDebug) {
			printDebugInfo(Log.DEBUG, tag, tr, fmt, args);
		}
	}

	// WARN (Custom tag)
	public static void dw(String tag, String msg) {
		if (isDebug) {
			printDebugInfo(Log.WARN, tag, null, msg);
		}
	}

	public static void dw(String tag, String fmt, Object... args) {
		if (isDebug) {
			printDebugInfo(Log.WARN, tag, null, fmt, args);
		}
	}

	public static void dw(String tag, Throwable tr) {
		if (isDebug) {
			printDebugInfo(Log.WARN, tag, tr, null);
		}
	}

	public static void dw(String tag, String msg, Throwable tr) {
		if (isDebug) {
			printDebugInfo(Log.WARN, tag, tr, msg);
		}
	}

	public static void dw(String tag, Throwable tr, String fmt, Object... args) {
		if (isDebug) {
			printDebugInfo(Log.WARN, tag, tr, fmt, args);
		}
	}

	// VERBOSE (Custom tag)
	public static void dv(String tag, String msg) {
		if (isDebug) {
			printDebugInfo(Log.VERBOSE, tag, null, msg);
		}
	}

	public static void dv(String tag, String fmt, Object... args) {
		if (isDebug) {
			printDebugInfo(Log.VERBOSE, tag, null, fmt, args);
		}
	}

	public static void dv(String tag, Throwable tr) {
		if (isDebug) {
			printDebugInfo(Log.VERBOSE, tag, tr, null);
		}
	}

	public static void dv(String tag, String msg, Throwable tr) {
		if (isDebug) {
			printDebugInfo(Log.VERBOSE, tag, tr, msg);
		}
	}

	public static void dv(String tag, Throwable tr, String fmt, Object... args) {
		if (isDebug) {
			printDebugInfo(Log.VERBOSE, tag, tr, fmt, args);
		}
	}

	// ////////////////////////////////////////////////////////////////////////////////////
	// 第二套方案:
	// 和常规的Log使用差不多，但是在标签哪里：需要传入preTag(模块名称)， 以及Object（模块中的某个类）来创建一个标签
	// 这套log将在广告sdk v4.10上启用，需要输入模块的tag名称 以及当前类 就可以定义一个很好的tag
	// 如下载模块的类a 可以定义为download_a 那么preTag就是download，object就是a.class或者a.this
	// 缺陷：暂时支持单模块前缀，如果是多模块前缀的话 在传入之前就应该要做好tag处理

	// INFO (Custom tag With prefix tag)
	public static void ti(String preTag, Object object, String msg) {
		if (isDebug) {
			printDebugInfo_WithPreTag(Log.INFO, preTag, object, null, msg);
		}
	}

	public static void ti(String preTag, Object object, String fmt, Object... args) {
		if (isDebug) {
			printDebugInfo_WithPreTag(Log.INFO, preTag, object, null, fmt, args);
		}
	}

	public static void ti(String preTag, Object object, Throwable tr) {
		if (isDebug) {
			printDebugInfo_WithPreTag(Log.INFO, preTag, object, tr, null);
		}
	}

	public static void ti(String preTag, Object object, String msg, Throwable tr) {
		if (isDebug) {
			printDebugInfo_WithPreTag(Log.INFO, preTag, object, tr, msg);
		}
	}

	public static void ti(String preTag, Object object, String fmt, Throwable tr, Object... args) {
		if (isDebug) {
			printDebugInfo_WithPreTag(Log.INFO, preTag, object, tr, fmt, args);
		}
	}

	// ERROR (Custom tag With prefix tag)
	public static void te(String preTag, Object object, String msg) {
		if (isDebug) {
			printDebugInfo_WithPreTag(Log.ERROR, preTag, object, null, msg);
		}
	}

	public static void te(String preTag, Object object, String fmt, Object... args) {
		if (isDebug) {
			printDebugInfo_WithPreTag(Log.ERROR, preTag, object, null, fmt, args);
		}
	}

	public static void te(String preTag, Object object, Throwable tr) {
		if (isDebug) {
			printDebugInfo_WithPreTag(Log.ERROR, preTag, object, tr, null);
		}
	}

	public static void te(String preTag, Object object, String msg, Throwable tr) {
		if (isDebug) {
			printDebugInfo_WithPreTag(Log.ERROR, preTag, object, tr, msg);
		}
	}

	public static void te(String preTag, Object object, String fmt, Throwable tr, Object... args) {
		if (isDebug) {
			printDebugInfo_WithPreTag(Log.ERROR, preTag, object, tr, fmt, args);
		}
	}

	// DEBUG (Custom tag With prefix tag)
	public static void td(String preTag, Object object, String msg) {
		if (isDebug) {
			printDebugInfo_WithPreTag(Log.DEBUG, preTag, object, null, msg);
		}
	}

	public static void td(String preTag, Object object, String fmt, Object... args) {
		if (isDebug) {
			printDebugInfo_WithPreTag(Log.DEBUG, preTag, object, null, fmt, args);
		}
	}

	public static void td(String preTag, Object object, Throwable tr) {
		if (isDebug) {
			printDebugInfo_WithPreTag(Log.DEBUG, preTag, object, tr, null);
		}
	}

	public static void td(String preTag, Object object, String msg, Throwable tr) {
		if (isDebug) {
			printDebugInfo_WithPreTag(Log.DEBUG, preTag, object, tr, msg);
		}
	}

	public static void td(String preTag, Object object, String fmt, Throwable tr, Object... args) {
		if (isDebug) {
			printDebugInfo_WithPreTag(Log.DEBUG, preTag, object, tr, fmt, args);
		}
	}

	// WARN (Custom tag With prefix tag)
	public static void tw(String preTag, Object object, String msg) {
		if (isDebug) {
			printDebugInfo_WithPreTag(Log.WARN, preTag, object, null, msg);
		}
	}

	public static void tw(String preTag, Object object, String fmt, Object... args) {
		if (isDebug) {
			printDebugInfo_WithPreTag(Log.WARN, preTag, object, null, fmt, args);
		}
	}

	public static void tw(String preTag, Object object, Throwable tr) {
		if (isDebug) {
			printDebugInfo_WithPreTag(Log.WARN, preTag, object, tr, null);
		}
	}

	public static void tw(String preTag, Object object, String msg, Throwable tr) {
		if (isDebug) {
			printDebugInfo_WithPreTag(Log.WARN, preTag, object, tr, msg);
		}
	}

	public static void tw(String preTag, Object object, String fmt, Throwable tr, Object... args) {
		if (isDebug) {
			printDebugInfo_WithPreTag(Log.WARN, preTag, object, tr, fmt, args);
		}
	}

	// VERBOSE (Custom tag With prefix tag)
	public static void tv(String preTag, Object object, String msg) {
		if (isDebug) {
			printDebugInfo_WithPreTag(Log.VERBOSE, preTag, object, null, msg);
		}
	}

	public static void tv(String preTag, Object object, String fmt, Object... args) {
		if (isDebug) {
			printDebugInfo_WithPreTag(Log.VERBOSE, preTag, object, null, fmt, args);
		}
	}

	public static void tv(String preTag, Object object, Throwable tr) {
		if (isDebug) {
			printDebugInfo_WithPreTag(Log.VERBOSE, preTag, object, tr, null);
		}
	}

	public static void tv(String preTag, Object object, String msg, Throwable tr) {
		if (isDebug) {
			printDebugInfo_WithPreTag(Log.VERBOSE, preTag, object, tr, msg);
		}
	}

	public static void tv(String preTag, Object object, String fmt, Throwable tr, Object... args) {
		if (isDebug) {
			printDebugInfo_WithPreTag(Log.VERBOSE, preTag, object, tr, fmt, args);
		}
	}

	/**
	 * 输出log 标签不带模块前缀
	 *
	 * @param level
	 * @param tag
	 * @param tr
	 * @param msg
	 * @param args
	 */
	protected static void printDebugInfo(int level, String tag, Throwable tr, String msg, Object... args) {
		if (isDebug) {
			if (isEnableToPrintInvokeInfo) {
				BasicLog.printInvokeInfo(level, tag);
				// BasicLog.printInvokeInfo(mInvokeInfoColor, tag);
			}
			BasicLog.printLog(level, tag, tr, msg, args);
		}
	}

	/**
	 * 输出log 标签带模块前缀， 如下载模块类A的输出为download_A
	 *
	 * @param level
	 * @param preTag
	 * @param object
	 * @param tr
	 * @param msg
	 * @param args
	 */
	protected static void printDebugInfo_WithPreTag(int level, String preTag, Object object, Throwable tr, String msg,
	                                                Object... args) {
		if (isDebug) {
			if (isEnableToPrintInvokeInfo) {
				BasicLog.printInvokeInfo(level, BasicLog.getFinalTag(preTag, object));
				// BasicLog.printInvokeInfo(mInvokeInfoColor,
				// BasicLog.getFinalTag(preTag, object));
			}
			BasicLog.printLog(level, BasicLog.getFinalTag(preTag, object), tr, msg, args);
		}
	}
}
