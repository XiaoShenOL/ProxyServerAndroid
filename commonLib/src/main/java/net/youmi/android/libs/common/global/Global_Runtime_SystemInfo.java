package net.youmi.android.libs.common.global;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;

import net.youmi.android.libs.common.basic.Basic_StringUtil;
import net.youmi.android.libs.common.coder.Coder_RC4;
import net.youmi.android.libs.common.debug.Debug_SDK;
import net.youmi.android.libs.common.util.Util_System_Permission;
import net.youmi.android.libs.common.util.Util_System_SDCard_Util;
import net.youmi.android.libs.common.util.Util_System_Service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Locale;

/**
 * 设备参数信息
 * 
 * @author jen
 * @author zhitaocai edit on 2014-5-21
 */
public class Global_Runtime_SystemInfo {

	/**
	 * mac地址的缓存文件名
	 */
	private final static String mMacCacheFileName = "m929bb76e8110d1a70260af57b446ebc";

	/**
	 * imei缓存文件名
	 */
	private final static String mImeiCacheFileName = "i42d45df023jnkdd93la483f9xGFKXI";

	/**
	 * imsi缓存文件名(即便是双卡双待机，也只保存一个imsi，优先保持第一个卡槽的imsi，没有就第二个)
	 */
	private final static String mImsiCacheFileName = "s92TjjdfoP2n3o9dfji2l9s1olkjf0p";

	/**
	 * mac、imei、imsi缓存文件名的加解密密钥（RC4）,不能轻易修改这个值，不然就拿不到正确的数据了
	 */
	private final static String mPsw = "pY32nlkjdf0xik2ljso0d9fi21LOI23";

	/**
	 * imei
	 */
	private static String mImei;
	private static String mImeiFromFile;

	/**
	 * imsi(最好不要静态变量，因为双卡机的存在，imsi可能有两个值)
	 */
	private static String mImsi;
	private static String mImsiFromFile;

	/**
	 * mac地址(去除了冒号)
	 */
	private static String mMac;

	/**
	 * Bssid
	 */
	private static String mBssid;

	/**
	 * Android ID
	 */
	private static String mAndroidId;

	/**
	 * 本地语言类型
	 */
	private static String mLocaleLanguage;

	/**
	 * 厂商
	 */
	private static String mManufacturer;

	/**
	 * 运营商名称 carrierName
	 */
	private static String mOperatorName;

	/**
	 * google-play-service的广告id
	 */
	private static String googleAdId;

	/**
	 * 获取本地语言 11-5-23
	 * 
	 * @return
	 */
	public static String getLocaleLanguage_Country() {
		try {
			if (mLocaleLanguage == null) {
				Locale l = Locale.getDefault();
				mLocaleLanguage = String.format("%s-%s", l.getLanguage(), l.getCountry());
			}
		} catch (Throwable e) {
			if (Debug_SDK.isGlobalLog) {
				Debug_SDK.te(Debug_SDK.mGlobalTag, Global_Runtime_SystemInfo.class, e);
			}
		}

		return mLocaleLanguage;
	}

	/**
	 * 初始化AndroidID
	 * 
	 * @param context
	 * @return
	 */
	private static String initAndroidId(Context context) {
		try {
			String andId = android.provider.Settings.Secure.getString(context.getContentResolver(),
					android.provider.Settings.Secure.ANDROID_ID);
			if (andId != null) {
				andId = andId.trim();
				andId = andId.toLowerCase();
				return andId;
			}
		} catch (Throwable e) {
			if (Debug_SDK.isGlobalLog) {
				Debug_SDK.te(Debug_SDK.mGlobalTag, Global_Runtime_SystemInfo.class, e);
			}
		}
		return "";
	}

	/**
	 * 获取Android Id
	 * 
	 * @param context
	 * @return
	 */
	public static String getAndroidId(Context context) {
		try {
			if (mAndroidId == null) {
				mAndroidId = initAndroidId(context);
			}
			if (mAndroidId != null) {
				return mAndroidId;
			}
		} catch (Throwable e) {
			if (Debug_SDK.isGlobalLog) {
				Debug_SDK.te(Debug_SDK.mGlobalTag, Global_Runtime_SystemInfo.class, e);
			}
		}
		return "";
	}

	/**
	 * 获取imei地址 2012-11-15
	 * 
	 * @author zhitaocai edit on 2014-6-5 从系统接口中获取
	 * @param context
	 * @return
	 */
	public static String getImei(Context context) {
		if (mImei == null) {
			mImei = getImeiFromSystemApi(context);
		}
		if (mImei != null) {
			// 保存第一次的imei到文件中，后续到这里的时候如果判断到已经存在文件则不写入
			saveDataToFile(context, mImeiCacheFileName, mImei, mPsw);
			return mImei;
		}
		return "";
	}

	/**
	 * 调用系统接口获取imei
	 * 
	 * @param context
	 * @return
	 */
	private static String getImeiFromSystemApi(Context context) {
		String imei = null;
		try {
			TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
			if (telephonyManager != null) {
				String imeiStr = telephonyManager.getDeviceId();
				if (imeiStr != null) {
					imei = imeiStr;
					if (imei != null) {
						imei = imei.trim();
						if (imei.indexOf(" ") > -1) {
							imei.replace(" ", "");
						}
						if (imei.indexOf("-") > -1) {
							imei = imei.replace("-", "");
						}
						if (imei.indexOf("\n") > -1) {
							imei = imei.replace("\n", "");
						}
						String meidStr = "MEID:";
						int stratIndex = imei.indexOf(meidStr);
						if (stratIndex > -1) {
							imei = imei.substring(stratIndex + meidStr.length());
						}
						imei = imei.trim();
						imei = imei.toLowerCase();
						if (imei.length() < 10) {
							imei = null;
						}
					}
				}
			}
		} catch (Throwable e) {
			if (Debug_SDK.isGlobalLog) {
				Debug_SDK.te(Debug_SDK.mGlobalTag, Global_Runtime_SystemInfo.class, e);
			}
		}
		return imei;
	}

	/**
	 * 注意这里可能获取到的为“”，因为还没有获取到imei并且保存到文件中
	 * <p>
	 * 建议需要先调用{@link #getImei(Context) }这个方法一次，才会将这次获取到的imei保存到文件
	 * <p>
	 * 但是如果媒介A已经调用过{@link #getImei(Context)} }这个方法，那么在另一个媒介里面，就可以直接调用这个方法来获取文件中的imei
	 * 
	 * @param context
	 * @return
	 */
	public static String getImeiFromFile(Context context) {
		if (mImeiFromFile == null || mImeiFromFile.length() == 0) {
			mImeiFromFile = getDataFromFile(context, mImeiCacheFileName, mPsw);
		}
		if (mImeiFromFile != null) {
			return mImeiFromFile;
		}
		return "";
	}

	/**
	 * 获取imsi地址
	 * <p>
	 * 如果是双卡双待机，则先返回卡1的IMSI，如果卡1的获取失败，则返回卡2的IMSI，如果都拿不到就返回空串""
	 * 
	 * @param context
	 * @return
	 */
	public static String getImsi(Context context) {
		if (mImsi == null) {
			mImsi = Global_Runtime_SystemInfo_IMSI.getImsi(context);
		}
		if (mImsi != null) {
			// 保存第一次的imei到文件中，后续到这里的时候如果判断到已经存在文件则不写入
			saveDataToFile(context, mImsiCacheFileName, mImsi, mPsw);
			return mImsi;
		}
		return "";
	}

	/**
	 * 注意这里可能获取到的为“”，因为还没有获取到imsi并且保存到文件中
	 * <p>
	 * 建议需要先调用{@link #getImsi(Context)}这个方法一次，才会将这次获取到的imsi保存到文件
	 * <p>
	 * 但是如果媒介A已经调用过{@link #getImsi(Context)}这个方法，那么在另一个媒介里面，就可以直接调用这个方法来获取文件中的imsi
	 * 
	 * @param context
	 * @return
	 */
	public static String getImsiFromFile(Context context) {

		if (mImsiFromFile == null || mImsiFromFile.length() == 0) {
			mImsiFromFile = getDataFromFile(context, mImsiCacheFileName, mPsw);
		}
		if (mImsiFromFile != null) {
			return mImsiFromFile;
		}
		return "";
	}

	/**
	 * 获取卡1的IMSI 如果获取失败则返回空串
	 * 
	 * @param context
	 * @return
	 */
	public static String getFirstImsi(Context context) {
		return Global_Runtime_SystemInfo_IMSI.getFirstImsi(context);
	}

	/**
	 * 获取卡2的IMSI 如果获取失败则返回空串
	 * 
	 * @param context
	 * @return
	 */
	public static String getSecondImsi(Context context) {
		return Global_Runtime_SystemInfo_IMSI.getSecondImsi(context);
	}

	/**
	 * 获取mac地址 2012-11-15
	 * 
	 * @param context
	 * @return
	 */
	public static String getMac(Context context) {
		// 首先从文件中获取mac地址
		if (mMac == null || mMac.length() == 0) {
			mMac = getDataFromFile(context, mMacCacheFileName, mPsw);
		}
		// 如果文件中没有的话，就从系统提供的api接口中获取，然后保存到文件中，以后就从文件中获取mac，可以在一定情度上保证防刷
		if (mMac == null || mMac.length() == 0) {
			mMac = getMacFromSystemApi(context);
			saveDataToFile(context, mMacCacheFileName, mMac, mPsw);
		}
		if (mMac != null) {
			return mMac;
		}
		return "";
	}

	/**
	 * 获取Bssid
	 * 
	 * @param context
	 * @return
	 */
	public static String getBssid(Context context) {
		try {
			WifiManager wifi = Util_System_Service.getWifiManager(context);
			WifiInfo info = wifi.getConnectionInfo();
			mBssid = info.getBSSID();
			if (mBssid != null) {
				return mBssid;
			}
		} catch (Throwable e) {
		}
		return "";

	}

	/**
	 * 调用系统接口获取mac地址，可能获取不了，如果没有开wifi的话
	 * 
	 * @param context
	 * @return
	 */
	private static String getMacFromSystemApi(Context context) {
		try {
			if (Util_System_Permission.isWith_ACCESS_WIFI_STATE_Permission(context)) {
				WifiManager wifi = Util_System_Service.getWifiManager(context);
				WifiInfo info = wifi.getConnectionInfo();
				String str = info.getMacAddress();
				if (str != null) {
					str = str.trim();
					str = str.replace(":", "");// 去掉:号
					str = str.toLowerCase(Locale.ENGLISH);
					return str;
				}
			}
		} catch (Throwable e) {
			if (Debug_SDK.isGlobalLog) {
				Debug_SDK.te(Debug_SDK.mGlobalTag, Global_Runtime_SystemInfo.class, e);
			}
		}
		return "";
	}

	// /**
	// * 从缓存文件中获取mac，这取决于上一次写入缓存的mac数据
	// *
	// * @param context
	// * @return
	// */
	// private static String getMacFromCacheFile(Context context) {
	// FileReader fr = null;
	// BufferedReader br = null;
	// try {
	// initMacCacheFileName(context);
	// initMacCachePsw(context);
	// File file = context.getFileStreamPath(mMacCacheFileName);
	// if (file == null) {
	// return "";
	// }
	// if (!file.exists()) {
	// return "";
	// }
	// fr = new FileReader(file);
	// br = new BufferedReader(fr);
	// String macToDecode = br.readLine();
	// macToDecode =
	// Basic_StringUtil.getNotEmptyStringElseReturnNull(macToDecode);
	// if (macToDecode == null) {
	// return "";
	// }
	// String mac = Coder_RC4.RC4(macToDecode, mMacPsw);
	// return mac;
	//
	// } catch (Throwable e) {
	// } finally {
	// try {
	// if (br != null) {
	// br.close();
	// }
	// } catch (Throwable e2) {
	// }
	// try {
	// if (fr != null) {
	// fr.close();
	// }
	// } catch (Throwable e2) {
	// }
	// }
	// return "";
	// }
	//
	// /**
	// * 将收集到的mac地址加密保存到cache文件中
	// *
	// * @param context
	// */
	// private static void saveMacToCacheFile(Context context, String mac) {
	// FileWriter fw = null;
	// BufferedWriter bw = null;
	// try {
	//
	// mac = Basic_StringUtil.getNotEmptyStringElseReturnNull(mac);
	// if (mac == null) {
	// return;
	// }
	// initMacCachePsw(context);
	// String encodedMac = Coder_RC4.RC4(mac, mMacPsw);
	//
	// initMacCacheFileName(context);
	// File file = context.getFileStreamPath(mMacCacheFileName);
	// fw = new FileWriter(file);
	// bw = new BufferedWriter(fw);
	// bw.write(encodedMac);
	// bw.flush();
	//
	// } catch (Throwable e) {
	// } finally {
	// try {
	// if (fw != null) {
	// fw.close();
	// }
	// } catch (Throwable e2) {
	// }
	// try {
	// if (bw != null) {
	// bw.close();
	// }
	// } catch (Throwable e2) {
	// }
	// }
	// }

	/**
	 * 获取品牌 DeviceVendor 如HTC
	 */
	public static String getManufacturerInfo() {
		try {
			if (mManufacturer == null) {
				Field f = Build.class.getField("MANUFACTURER");
				if (f != null) {
					mManufacturer = f.get(Build.class).toString().trim();
				}
			}
		} catch (Throwable e) {
			if (Debug_SDK.isGlobalLog) {
				Debug_SDK.te(Debug_SDK.mGlobalTag, Global_Runtime_SystemInfo.class, e);
			}
		}
		if (mManufacturer == null) {
			return Build.BRAND;
		}
		return mManufacturer;
	}

	/**
	 * 获取设备操作系统 PhoneOS 如 android 2.3 11-5-23
	 * 
	 * @return
	 */
	public static String getDeviceOsRelease() {
		return "android " + Build.VERSION.RELEASE;
	}

	/**
	 * 获取手机型号 DeviceDetail 11-5-23
	 * 
	 * @return
	 */
	public static String getDeviceModel() {
		return Build.MODEL;
	}

	/**
	 * 初始化 运营商信息 11-5-23
	 * 
	 * @param context
	 */
	private static void initOperatorName(Context context) {
		try {
			final TelephonyManager telephonyManager = Util_System_Service.getTelephonyManager(context);
			if (telephonyManager != null) {
				/*
				 * 运营商的名称 cn
				 */
				String str = telephonyManager.getNetworkOperatorName();
				if (str == null) {
					mOperatorName = "";
				} else {
					mOperatorName = str;
				}
			}
		} catch (Throwable e) {
			if (Debug_SDK.isGlobalLog) {
				Debug_SDK.te(Debug_SDK.mGlobalTag, Global_Runtime_SystemInfo.class, e);
			}
		}
	}

	/**
	 * 获取运营商名字
	 * 
	 * @param context
	 * @return
	 */
	public static String getOperatorName(Context context) {
		if (mOperatorName == null) {
			initOperatorName(context);
		}
		if (mOperatorName == null) {
			return "";
		}
		return mOperatorName;
	}

	/**
	 * 获取手机类型 2012-11-15
	 * 
	 * @param context
	 * @return
	 */
	public static int getPhoneType(Context context) {
		try {
			final TelephonyManager telephonyManager = Util_System_Service.getTelephonyManager(context);
			if (telephonyManager != null) {
				return telephonyManager.getPhoneType();
			}
		} catch (Throwable e) {
			if (Debug_SDK.isGlobalLog) {
				Debug_SDK.te(Debug_SDK.mGlobalTag, Global_Runtime_SystemInfo.class, e);
			}
		}
		return TelephonyManager.PHONE_TYPE_NONE;
	}

	/**
	 * 获取手机网络类型 2012-11-15
	 * 
	 * @param context
	 * @return
	 */
	public static int getNetworkType(Context context) {
		try {
			final TelephonyManager telephonyManager = Util_System_Service.getTelephonyManager(context);
			if (telephonyManager != null) {
				return telephonyManager.getNetworkType();
			}
		} catch (Throwable e) {
			if (Debug_SDK.isGlobalLog) {
				Debug_SDK.te(Debug_SDK.mGlobalTag, Global_Runtime_SystemInfo.class, e);
			}
		}

		return TelephonyManager.NETWORK_TYPE_UNKNOWN;
	}

	/**
	 * 获取当前设备使用的android-sdk版本号 如：17、19
	 * 
	 * @return
	 */
	public static int getAndroidSdkVersion() {
		return Build.VERSION.SDK_INT;
	}

	/**
	 * 获取谷歌广告的id——注意： 需要sdk这边在初始化的时候先调用 {@link #async_loadGoogleAdvertisingId(Context)} }这个方法异步获取一下这个获取是耗时操作，需要放在线程中获取<br>
	 * 前提：因为是反射获取的，所以需要开发者的app引入googleplay的库<br>
	 * 
	 * @param context
	 * @return
	 */
	public static String getGoogleAdvertisingId(Context context) {
		if (Basic_StringUtil.isNullOrEmpty(googleAdId)) {
			async_loadGoogleAdvertisingId(context);
		}
		if (!Basic_StringUtil.isNullOrEmpty(googleAdId)) {
			return googleAdId;
		}
		return "";
	}

	/**
	 * 异步获取谷歌广告的id，不返回结果，结果需要通过{@link getGoogleAdvertisingId}来获取——注意：这个获取是耗时操作，所以在线程中使用，这里先随便new一个使用，晚点在弄到线程池中<br>
	 * 前提：因为是反射获取的，所以需要开发者的app引入googleplay的库<br>
	 * 
	 * @param context
	 * @return
	 */
	public static void async_loadGoogleAdvertisingId(final Context context) {
		if (Basic_StringUtil.isNullOrEmpty(googleAdId)) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						Class<?> c = Class.forName("com.google.android.gms.ads.identifier.AdvertisingIdClient");
						Method m = c.getDeclaredMethod("getAdvertisingIdInfo", Context.class);
						Object result = m.invoke(c, context);
						Class<?> info = Class.forName("com.google.android.gms.ads.identifier" +
								".AdvertisingIdClient$Info");
						Method mid = info.getDeclaredMethod("getId", (Class<?>) null);
						Object aid = mid.invoke(result, (Object) null);
						if (aid != null) {
							googleAdId = aid.toString();
						}
					} catch (Throwable e) {
						if (Debug_SDK.isGlobalLog) {
							Debug_SDK.te(Debug_SDK.mGlobalTag, Global_Runtime_SystemInfo.class, e);
						}
					}

				}
			}).start();
		}
	}

	/**
	 * 1、将指定字符串进行RC4加密
	 * <p>
	 * 2、保存加密后的数据到sd卡中（如果sd卡可用，则优先保存到sd卡）或者这个应用的file目录下的指定文件
	 * <p>
	 * 注意：只保存一行
	 * 
	 * @param context
	 * @param fileName
	 *            文件名
	 * @param toSaveStr
	 *            保存字符串
	 * @param psw
	 *            密码
	 * @return
	 */
	private synchronized static boolean saveDataToFile(Context context, String fileName, String toSaveStr, String psw) {
		if (context == null || Basic_StringUtil.isNullOrEmpty(toSaveStr) || Basic_StringUtil.isNullOrEmpty(fileName)
				|| Basic_StringUtil.isNullOrEmpty(fileName)) {
			return false;
		}
		try {
			String encodeStr = Coder_RC4.RC4(toSaveStr, psw);
			File toSaveFile;
			// 小文件就不判断空间是否足够了
			// if
			// (Util_System_SDCard_Util.IsSdCardCanWrite_And_EnoughSpace(context,
			// 200)) {
			if (Util_System_SDCard_Util.IsSdCardCanWrite(context)) {

				// 先创建文件夹
				File dir = new File(Util_System_SDCard_Util.getSdcardRootPath() + "/Android/data/.dataycache");
				dir.mkdirs();

				// 然后创建文件
				toSaveFile = new File(dir.getAbsoluteFile() + "/" + fileName);
				if (!toSaveFile.exists()) {
					if (Debug_SDK.isGlobalLog) {
						Debug_SDK.td(Debug_SDK.mGlobalTag, Global_Runtime_SystemInfo.class, "%s文件不存在",
								toSaveFile.getAbsolutePath());
					}
					if (toSaveFile.createNewFile()) {
						if (Debug_SDK.isGlobalLog) {
							Debug_SDK.td(Debug_SDK.mGlobalTag, Global_Runtime_SystemInfo.class, "%s文件创建成功",
									toSaveFile.getAbsolutePath());
						}
					} else {
						if (Debug_SDK.isGlobalLog) {
							Debug_SDK.td(Debug_SDK.mGlobalTag, Global_Runtime_SystemInfo.class, "%s文件创建失败",
									toSaveFile.getAbsolutePath());
						}
					}
				} else {
					// 如果文件已经存在就暂时先当已经写入过内容，结束
					// 以后看看是不是再加验证文件的逻辑
					return false;
				}
			} else {
				toSaveFile = context.getFileStreamPath(fileName);
				if (Debug_SDK.isGlobalLog) {
					Debug_SDK.td(Debug_SDK.mGlobalTag, Global_Runtime_SystemInfo.class, "sd卡不可写，创建文件%s",
							toSaveFile.getAbsolutePath());
				}
				if (toSaveFile.exists()) {
					// 如果文件已经存在就暂时先当已经写入过内容，结束
					// 以后看看是不是再加验证文件的逻辑
					return false;
				}
			}

			FileWriter fw = null;
			BufferedWriter bw = null;
			try {
				if (toSaveFile != null) {
					fw = new FileWriter(toSaveFile);
					bw = new BufferedWriter(fw);
					bw.write(encodeStr);
					bw.flush();
					if (Debug_SDK.isGlobalLog) {
						Debug_SDK.td(Debug_SDK.mGlobalTag, Global_Runtime_SystemInfo.class, "字符串%s（加密后：%s）已经成功写入文件%s中",
								toSaveStr, encodeStr, toSaveFile.getAbsolutePath());
					}
					return true;
				}
			} catch (Throwable e) {
				if (Debug_SDK.isGlobalLog) {
					Debug_SDK.te(Debug_SDK.mGlobalTag, Global_Runtime_SystemInfo.class, e);
				}
			} finally {
				try {
					if (fw != null) {
						fw.close();
					}
				} catch (Throwable e) {
					if (Debug_SDK.isGlobalLog) {
						Debug_SDK.te(Debug_SDK.mGlobalTag, Global_Runtime_SystemInfo.class, e);
					}
				}
				try {
					if (bw != null) {
						bw.close();
					}
				} catch (Throwable e) {
					if (Debug_SDK.isGlobalLog) {
						Debug_SDK.te(Debug_SDK.mGlobalTag, Global_Runtime_SystemInfo.class, e);
					}
				}
			}
		} catch (Throwable e) {
			if (Debug_SDK.isGlobalLog) {
				Debug_SDK.te(Debug_SDK.mGlobalTag, Global_Runtime_SystemInfo.class, e);
			}
		}
		return false;
	}

	/**
	 * 1、从sd卡中（如果sd卡可用，则优先从sd卡中获取）或者这个应用的file目录下的指定文件中获取加密字符串(RC4加密的)
	 * <p>
	 * 2、解密这个加密字符串
	 * 
	 * @param context
	 * @param fileName
	 * @return 获取失败返回null，否则返回正确值
	 */
	private synchronized static String getDataFromFile(Context context, String fileName, String psw) {

		boolean isNeedToReWriteToSdCard = false;
		if (context == null || Basic_StringUtil.isNullOrEmpty(fileName) || Basic_StringUtil.isNullOrEmpty(psw)) {
			return null;
		}
		File toGetFile = null;
		// 如果sd卡可读，先看看sd卡中有没有这个文件
		if (Util_System_SDCard_Util.IsSdCardCanRead()) {
			toGetFile = new File(Util_System_SDCard_Util.getSdcardRootPath() + "/Android/data/.dataycache/" + fileName);
			// 如果sd卡中获取不到这个文件
			if (toGetFile != null) {
				if (!toGetFile.exists()) {
					if (Debug_SDK.isGlobalLog) {
						Debug_SDK.td(Debug_SDK.mGlobalTag, Global_Runtime_SystemInfo.class, "sd 卡中没有文件%s",
								toGetFile.getAbsolutePath());
					}
					toGetFile = context.getFileStreamPath(fileName);
					isNeedToReWriteToSdCard = true;
				}
			}
		}
		if (toGetFile == null) {
			toGetFile = context.getFileStreamPath(fileName);
			isNeedToReWriteToSdCard = true;
		}

		FileReader fr = null;
		BufferedReader br = null;
		try {
			if (toGetFile != null) {
				if (toGetFile.exists()) {
					fr = new FileReader(toGetFile);
					br = new BufferedReader(fr);
					String toDecodeStr = br.readLine();
					if (!Basic_StringUtil.isNullOrEmpty(toDecodeStr)) {
						String result = Coder_RC4.RC4(toDecodeStr, psw);
						if (Debug_SDK.isGlobalLog) {
							Debug_SDK.td(Debug_SDK.mGlobalTag, Global_Runtime_SystemInfo.class, "从文件%s中获取到字符串%s",
									toGetFile.getPath(), result);
						}
						result = Basic_StringUtil.getNotEmptyStringElseReturnNull(result);

						// 这里需要说明一下：
						// 假设要保存imei，但是可能一开始用户没有插入sd卡，因此imei就保存在应用的file目录了，如果有用户要刷我们的话
						// ，把应用删了重新安装，那么这个文件就不存在了，下次就会获取他们刷出来的那个值，因此我们尽量保证这个文件的值不要被删去，那么就将这个文件的内容重新写入sd卡中，那么如果用户有sd卡的话，这个值就可以尽量唯一了
						// 下次用户删去应用还是有之前那个值的
						if (isNeedToReWriteToSdCard) {
							saveDataToFile(context, fileName, result, psw);
						}
						return result;
					}
				}
			}
		} catch (Throwable e) {
			if (Debug_SDK.isGlobalLog) {
				Debug_SDK.te(Debug_SDK.mGlobalTag, Global_Runtime_SystemInfo.class, e);
			}
		} finally {
			try {
				if (fr != null) {
					fr.close();
				}
			} catch (Throwable e) {
				if (Debug_SDK.isGlobalLog) {
					Debug_SDK.te(Debug_SDK.mGlobalTag, Global_Runtime_SystemInfo.class, e);
				}
			}
			try {
				if (br != null) {
					br.close();
				}
			} catch (Throwable e) {
				if (Debug_SDK.isGlobalLog) {
					Debug_SDK.te(Debug_SDK.mGlobalTag, Global_Runtime_SystemInfo.class, e);
				}
			}
		}
		return null;
	}

}
