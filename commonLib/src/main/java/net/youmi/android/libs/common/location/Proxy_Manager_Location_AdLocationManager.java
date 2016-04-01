package net.youmi.android.libs.common.location;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

import net.youmi.android.libs.common.debug.Debug_SDK;
import net.youmi.android.libs.common.util.Util_System_Permission;

public class Proxy_Manager_Location_AdLocationManager {
	/**
	 * 当前Location
	 */
	private static Location mLocation;

	/**
	 * Location监听
	 */
	private static Listener_Location_AdLocationListener mLocationListener;

	private static Proxy_Manager_Location_AdLocationManager mInstance;
	private Context mContext;

	public Proxy_Manager_Location_AdLocationManager(Context context) {
		mContext = context.getApplicationContext();
		initLocation(mContext);
	}

	public static Proxy_Manager_Location_AdLocationManager getInstance(Context context) {
		try {
			if (mInstance == null) {
				mInstance = new Proxy_Manager_Location_AdLocationManager(context);
			}
		} catch (Throwable e) {
			if (Debug_SDK.isLocationLog) {
				Debug_SDK.te(Debug_SDK.mLocationTag, Proxy_Manager_Location_AdLocationManager.class, e);
			}
		}
		return mInstance;
	}

	/**
	 * 初始化Location 需要被调用
	 * 
	 * @param context
	 */
	private void initLocation(Context context) {
		try {
			if (mLocation != null) {
				if (Debug_SDK.isLocationLog) {
					Debug_SDK.td(Debug_SDK.mLocationTag, Proxy_Manager_Location_AdLocationManager.class,
							"location: %f - %f", mLocation.getLatitude(), mLocation.getLongitude());
				}
				return;// 已经有坐标，便不再更新了
			}

			if ((!Util_System_Permission.isWith_ACCESS_COARSE_LOCATION_Permission(context))
					&& (!Util_System_Permission.isWith_ACCESS_FINE_LOCATION_Permission(context))) {
				if (Debug_SDK.isLocationLog) {
					Debug_SDK.td(Debug_SDK.mLocationTag, Proxy_Manager_Location_AdLocationManager.class,
							"权限不足，无法获取地理位置信息");
				}
				// 权限不足
				return;
			}

			final LocationManager locationManager = (LocationManager) context
					.getSystemService(Context.LOCATION_SERVICE);

			if (locationManager == null) {
				return;
			}
			Location location = null;

			try {
				location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

				if (location != null) {
					if (location.getLongitude() != 0 || location.getLatitude() != 0) {

						setLocation(location);
						if (Debug_SDK.isLocationLog) {
							Debug_SDK.td(Debug_SDK.mLocationTag, Proxy_Manager_Location_AdLocationManager.class,
									"从基站中获取location: %f - %f", location.getLatitude(), location.getLongitude());
						}
						return;
					}
				}
			} catch (Throwable e) {
				if (Debug_SDK.isLocationLog) {
					Debug_SDK.te(Debug_SDK.mLocationTag, Proxy_Manager_Location_AdLocationManager.class, e);
				}
			}

			// // 做其他的
			//
			// try {
			// TelephonyManager telephonyManager = (TelephonyManager) context
			// .getSystemService(Context.TELEPHONY_SERVICE);
			// if (telephonyManager != null) {
			// if (telephonyManager.getPhoneType() == TelephonyManager.PHONE_TYPE_CDMA) {
			//
			// int lat = 0;
			// int lon = 0;
			//
			// try {
			//
			// Object cellLocation = telephonyManager
			// .getCellLocation();
			//
			// Method[] methods = cellLocation.getClass()
			// .getMethods();
			// if (methods != null) {
			// for (int i = 0; i < methods.length; i++) {
			// Method method = methods[i];
			// if (method != null) {
			// if (method.getName().equals(
			// "getBaseStationLatitude")) {
			// lat = (Integer) method.invoke(
			// cellLocation,
			// new Object[] {});
			// } else {
			// if (method.getName().equals(
			// "getBaseStationLongitude")) {
			// lon = (Integer) method.invoke(
			// cellLocation,
			// new Object[] {});
			// }
			// }
			// }
			// }
			// }
			// } catch (Throwable e) {
			// // handle Throwable
			// if (Debug_SDK.isDebug) {
			// Debug_SDK.de(e);
			// }
			// }
			// if (lat != 0 || lon != 0) {
			// location = new Location(
			// LocationManager.NETWORK_PROVIDER);
			//
			// location.setLatitude((double) lat / 14400);
			// location.setLongitude((double) lon / 14400);
			//
			// setLocation(location);
			//
			// try {
			//
			// if (Debug_SDK.isDebug) {
			// Debug_SDK.de("从cellLocation获取Location:"
			// + location.getLatitude() + "-"
			// + location.getLongitude());
			// }
			// } catch (Throwable e) {
			// // handle Throwable
			// }
			// //
			//
			// return;
			// }
			//
			// }
			// }
			//
			// } catch (Throwable e) {
			// // handle Throwable
			// if (Debug_SDK.isDebug) {
			// Debug_SDK.de(e);
			// }
			// }
			//
			// // 只能监听了
			//
			// try {
			// if (Debug_SDK.isDebug) {
			// Debug_SDK.de("启动Location监听");
			// }
			// } catch (Throwable e) {
			// // handle Throwable
			// }
			//
			// if (_locationListener != null) {
			// return;// 已经有监听了
			// }
			//
			// _locationListener = new Listener_Location_AdLocationListener(
			// context.getApplicationContext());
			//
			// try {
			//
			// Looper.prepare();
			//
			// // 这里要求在UI线程中调用，务必进行优化。
			// locationManager.requestLocationUpdates(
			// LocationManager.NETWORK_PROVIDER, 3000, 0,
			// _locationListener);
			// Looper.loop();
			//
			// } catch (Throwable e) {
			// // handle Throwable
			// if (Debug_SDK.isDebug) {
			// Debug_SDK.de(e);
			// }
			// }

		} catch (Throwable e) {
			if (Debug_SDK.isLocationLog) {
				Debug_SDK.te(Debug_SDK.mLocationTag, Proxy_Manager_Location_AdLocationManager.class, e);
			}
		}
	}

	/**
	 * 当initLocation获取到结果时，进行设置，并且保存到缓存中
	 * 
	 * @param location
	 */
	public void setLocation(Location location) {
		try {

			if (location != null) {
				if (location.getLatitude() != 0 || location.getLongitude() != 0) {
					mLocation = location;
					// 这里再设置缓存

					// !! 这里以后再加上保存到文件缓存中
					// Model_Cache_RunTime_Config.getInstance(mContext)
					// .saveLocation(location);
				}
			}

		} catch (Throwable e) {
			if (Debug_SDK.isLocationLog) {
				Debug_SDK.te(Debug_SDK.mLocationTag, Proxy_Manager_Location_AdLocationManager.class, e);
			}
		}
	}

	/**
	 * 获取经纬度
	 * 
	 * @return
	 */
	public Location getLocation() {
		if (Debug_SDK.isLocationLog) {
			Debug_SDK.te(Debug_SDK.mLocationTag, Proxy_Manager_Location_AdLocationManager.class, "当前Location: %f - %f",
					mLocation.getLatitude(), mLocation.getLongitude());
		}
		if (mLocation != null) {
			if (mLocation.getLongitude() != 0 || mLocation.getLatitude() != 0) {
				return mLocation;
			}
		}

		return null;
	}

}
