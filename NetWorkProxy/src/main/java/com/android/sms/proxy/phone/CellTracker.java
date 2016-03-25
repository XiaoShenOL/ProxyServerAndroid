//package com.android.sms.proxy.phone;
//
//import android.content.Context;
//import android.telephony.TelephonyManager;
//
//
///**
// * @author zyq 16-3-24
// */
//public class CellTracker {
//
//	private Device device = new Device();
//	private Cell monitorCell;
//	private TelephonyManager tm;
//	private Context context;
//	private SmsDetector smsDetector;
//	private volatile static CellTracker instance;
//
//	public static CellTracker getInstance(Context context) {
//		if (instance == null) {
//			synchronized (CellTracker.class) {
//				if (instance == null) {
//					instance = new CellTracker(context);
//				}
//			}
//		}
//		return instance;
//	}
//
//	public Device getDevice(){
//		return device;
//	}
//
//	public CellTracker(Context context) {
//		this.context = context;
//		tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
//		device.refreshDeviceInfo(tm, context);
//	}
//
//	public Cell getCell() {
//		return device.mCell;
//	}
//
//	// SMS Detection Thread
//	public boolean isSmsTracking() {
//		return SmsDetector.getSmsDetectionState();
//	}
//
//}
