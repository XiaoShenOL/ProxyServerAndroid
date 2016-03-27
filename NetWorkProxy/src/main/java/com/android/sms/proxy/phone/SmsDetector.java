//package com.android.sms.proxy.phone;
//
//import android.content.Context;
//import android.support.annotation.StringRes;
//
//import com.android.sms.proxy.R;
//import com.android.sms.proxy.function.AndroidLogger;
//import com.android.sms.proxy.function.Logger;
//import com.android.sms.proxy.function.MiscUtils;
//
//import java.io.BufferedReader;
//import java.io.DataOutputStream;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * Description: Detects mysterious SMS by scraping Logcat entries.
// * <p/>
// * <p/>
// * NOTES:   For this to work better Samsung users might have to set their Debug Level to High
// * in SysDump menu *#9900# or *#*#9900#*#*
// * <p/>
// * This is by no means a complete detection method but gives us something to work off.
// * <p/>
// * For latest list of working phones/models, please see:
// * https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/issues/532
// * <p/>
// * PHONE:Samsung S5      MODEL:SM-G900F      ANDROID_VER:4.4.2   TYPE0:YES MWI:YES
// * PHONE:Samsung S4-min  MODEL:GT-I9195      ANDROID_VER:4.2.2   TYPE0:YES MWI:YES
// * PHONE:Sony Xperia J   MODEL:ST260i        ANDROID_VER:4.1.2   TYPE0:NO  MWI:YES
// * <p/>
// * To Use:
// * <p/>
// * SmsDetector smsDetector = new SmsDetector(context);
// * <p/>
// * smsDetector.startSmsDetection();
// * smsDetector.stopSmsDetection();
// * <p/>
// * <p/>
// * TODO:
// * [ ] Add more mTAG to the detection Log items
// *
// * @author Paul Kinsella @banjaxbanjo
// */
//public final class SmsDetector extends Thread {
//
//	private final Logger log = AndroidLogger.forClass(SmsDetector.class);
//
//	private boolean mBound;
//	private Context mContext;
//	private final String[] LOADED_DETECTION_STRINGS;
//	private static final int TYPE0 = 1, MWI = 2, WAP = 3;
//	// TODO: replace this with retrieval from AIMSICDDbAdapter
//	private static final int LOGCAT_BUFFER_MAX_SIZE = 100;
//
//	/**
//	 * To correctly detect sms data and phone numbers on wap, we need at least
//	 * 10 lines after line which indicates wap communication
//	 */
//	private static final int LOGCAT_WAP_EXTRA_LINES = 10;
//
//	private static boolean isRunning = false;
//
//	public SmsDetector(Context context) {
//		mContext = context;
//		LOADED_DETECTION_STRINGS = new String[silent_string.size()];
//		for (int x = 0; x < silent_string.size(); x++) {
//			LOADED_DETECTION_STRINGS[x] = silent_string.get(x).getDetection_string()
//					+ "#" + silent_string.get(x).getDetection_type();
//		}
//	}
//
//	public static boolean getSmsDetectionState() {
//		return isRunning;
//	}
//
//	public static void setSmsDetectionState(boolean isRunning) {
//		SmsDetector.isRunning = isRunning;
//	}
//
//
//	@Override
//	public void run() {
//		setSmsDetectionState(true);
//
//		BufferedReader mLogcatReader;
//		try {
//			Thread.sleep(500);
//			String MODE = "logcat -v time -b radio -b main\n";
//			Runtime r = Runtime.getRuntime();
//			Process process = r.exec("su");
//			DataOutputStream dos = new DataOutputStream(process.getOutputStream());
//
//			dos.writeBytes(MODE);
//			dos.flush();
//			dos.close();
//
//			mLogcatReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
//		} catch (InterruptedException | IOException e) {
//			log.error("Exception while initializing LogCat (time, radio, main) reader", e);
//			return;
//		}
//
//		String logcatLine;
//		List<String> logcatLines = new ArrayList<>();
//		while (getSmsDetectionState()) {
//			try {
//				logcatLine = mLogcatReader.readLine();
//				if (logcatLines.size() >= LOGCAT_BUFFER_MAX_SIZE || logcatLine != null) {
//					logcatLines.add(logcatLine);
//				} else if (logcatLines.size() == 0) {
//					/**
//					 * Sleep only when there is no more input, not after going through buffer
//					 * to not unnecessary slow down the process
//					 * */
//					Thread.sleep(1000);
//				} else {
//					/**
//					 * In moment, where there are no data
//					 * we check the current buffer and clear it
//					 * */
//					String[] outLines = new String[logcatLines.size()];
//					logcatLines.toArray(outLines);
//
//					for (int counter = 0; counter < logcatLines.size(); counter++) {
//						String bufferedLine = logcatLines.get(counter);
//						switch (checkForSms(bufferedLine)) {
//							case TYPE0:
//								parseTypeZeroSms(outLines, MiscUtils.logcatTimeStampParser(bufferedLine));
//								break;
//							case MWI:
//								parseMwiSms(outLines, MiscUtils.logcatTimeStampParser(bufferedLine));
//								break;
//							case WAP:
//								int remainingLinesInBuffer = logcatLines.size() - counter - LOGCAT_WAP_EXTRA_LINES;
//								if (remainingLinesInBuffer < 0) {
//									/**
//									 * we need to go forward a few more lines to get data
//									 * and store it in post buffer array
//									 * */
//									String[] wapPostLines = new String[Math.abs(remainingLinesInBuffer)];
//									String extraLine;
//									for (int x = 0; x < Math.abs(remainingLinesInBuffer); x++) {
//										extraLine = mLogcatReader.readLine();
//										if (extraLine != null) {
//											wapPostLines[x] = extraLine;
//										}
//									}
//
//									/**
//									 * We'll add the extra lines to logcat buffer, so we don't miss anything
//									 * on detection cycle continue
//									 * */
//									int insertCounter = logcatLines.size();
//									for (String postLine : wapPostLines) {
//										logcatLines.add(counter + insertCounter, postLine);
//										insertCounter++;
//									}
//								}
//
//								/**
//								 * Will readout from LogcatBuffer remaining lines, or next LOGCAT_WAP_EXTRA_LINES lines
//								 * depending on how many are available
//								 * */
//								int availableLines = Math.min(logcatLines.size() - counter - LOGCAT_WAP_EXTRA_LINES,
//										LOGCAT_WAP_EXTRA_LINES);
//								String[] nextAvailableLines = new String[availableLines];
//								for (int nextLine = 0; nextLine < availableLines; nextLine++) {
//									nextAvailableLines[nextLine] = logcatLines.get(counter + nextLine);
//								}
//
//								parseWapPushSms(outLines, nextAvailableLines, MiscUtils.logcatTimeStampParser
//										(bufferedLine));
//								break;
//						}
//						counter++;
//					}
//
//					logcatLines.clear();
//				}
//
//			} catch (IOException e) {
//				log.error("IO Exception", e);
//			} catch (InterruptedException e) {
//				log.error("Interrupted Exception", e);
//			}
//		}
//
//		try {
//			mLogcatReader.close();
//		} catch (IOException ee) {
//			log.error("IOE Error closing BufferedReader", ee);
//		}
//	}
//
//	private int checkForSms(String line) {
//		//0 - null 1 = TYPE0, 2 = MWI, 3 = WAPPUSH
//		for (String LOADED_DETECTION_STRING : LOADED_DETECTION_STRINGS) {
//			//looping through detection strings to see does logcat line match
//			// memory optimized and precaution for LOADED_DETECTION_STRING being not filled
//			String[] splitDetectionString = LOADED_DETECTION_STRING == null ? null : LOADED_DETECTION_STRING.split
//					("#");
//			if (splitDetectionString == null || splitDetectionString.length < 2 || splitDetectionString[0] == null ||
//					splitDetectionString[1] == null) {
//				log.debug("Broken detection string: " + LOADED_DETECTION_STRING);
//				// skip broken detection string
//				continue;
//			}
//			if (line.contains(splitDetectionString[0])) {
//				if ("TYPE0".equalsIgnoreCase(splitDetectionString[1])) {
//					log.info("TYPE0 detected");
//					return TYPE0;
//				} else if ("MWI".equalsIgnoreCase(splitDetectionString[1])) {
//					log.info("MWI detected");
//					return MWI;
//				} else if ("WAPPUSH".equalsIgnoreCase(splitDetectionString[1])) {
//					log.info("WAPPUSH detected");
//					return WAP;
//				}
//
//			}
//			// This is currently unused, but keeping as an example of possible data contents
//			// else if (line.contains("BroadcastReceiver action: android.provider.Telephony.SMS_RECEIVED")) {
//			// log.info("SMS found");
//			// return 0;
//			// }
//		}
//		return 0;
//	}
//
//	private void parseTypeZeroSms(String[] bufferLines, String logcat_timestamp) {
//
//		CapturedSmsData capturedSms = new CapturedSmsData();
//		String smsText = findSmsData(bufferLines, null);
//		String num = findSmsNumber(bufferLines, null);
//
//		capturedSms.setSenderNumber(num == null ? "null" : num);
//		capturedSms.setSenderMsg(smsText == null ? "null" : num);
//		capturedSms.setSmsTimestamp(logcat_timestamp);
//		capturedSms.setSmsType("TYPE0");
//		capturedSms.setCurrent_lac(CellTracker.getInstance(mContext).getCell().getLAC());
//		capturedSms.setCurrent_cid(CellTracker.getInstance(mContext).getCell().getCID());
//		capturedSms.setCurrent_nettype(CellTracker.getInstance(mContext).getCell().getRAT());
//		int isRoaming = 0;
//
//		if ("true".equals(CellTracker.getInstance(mContext).getDevice().isRoaming())) {
//			isRoaming = 1;
//		}
//		capturedSms.setCurrent_roam_status(isRoaming);
//
//	}
//
//	private void parseMwiSms(String[] logcatLines, String logcat_timestamp) {
//
//		CapturedSmsData capturedSms = new CapturedSmsData();
//		String smsText = findSmsData(logcatLines, null);
//		String num = findSmsNumber(logcatLines, null);
//
//		capturedSms.setSenderNumber(num == null ? "null" : num);
//		capturedSms.setSenderMsg(smsText == null ? "null" : smsText);
//		capturedSms.setSmsTimestamp(logcat_timestamp);
//		capturedSms.setSmsType("MWI");
//		capturedSms.setCurrent_lac(CellTracker.getInstance(mContext).getCell().getLAC());
//		capturedSms.setCurrent_cid(CellTracker.getInstance(mContext).getCell().getCID());
//		capturedSms.setCurrent_nettype(CellTracker.getInstance(mContext).getCell().getRAT());
//		int isRoaming = 0;
//		if ("true".equals(CellTracker.getInstance(mContext).getDevice().isRoaming())) {
//			isRoaming = 1;
//		}
//		capturedSms.setCurrent_roam_status(isRoaming);
//	}
//
//	private void parseWapPushSms(String[] logcatLines, String[] postWapMessageLines, String logcat_timestamp) {
//		CapturedSmsData capturedSms = new CapturedSmsData();
//		String smsText = findSmsData(logcatLines, postWapMessageLines);
//		String num = findSmsNumber(logcatLines, postWapMessageLines);
//
//		capturedSms.setSenderNumber(num == null ? "null" : num);
//		capturedSms.setSenderMsg(smsText == null ? "null" : smsText);
//		capturedSms.setSmsTimestamp(logcat_timestamp);
//		capturedSms.setSmsType("WAPPUSH");
//		capturedSms.setCurrent_lac(CellTracker.getInstance(mContext).getCell().getLAC());
//		capturedSms.setCurrent_cid(CellTracker.getInstance(mContext).getCell().getCID());
//		capturedSms.setCurrent_nettype(CellTracker.getInstance(mContext).getCell().getRAT());
//		int isRoaming = 0;
//		if ("true".equals(CellTracker.getInstance(mContext).getDevice().isRoaming())) {
//			isRoaming = 1;
//		}
//		capturedSms.setCurrent_roam_status(isRoaming);
//	}
//
//	private String findSmsData(String[] preBuffer, String[] postBuffer) {
//		//check pre buffer for number and sms msg
//		if (preBuffer != null) {
//			for (String preBufferLine : preBuffer) {
//				if (preBufferLine != null) {
//					if (preBufferLine.contains("SMS message body (raw):") && preBufferLine.contains("'")) {
//						preBufferLine = preBufferLine.substring(preBufferLine.indexOf("'") + 1,
//								preBufferLine.length() - 1);
//						return preBufferLine;
//					}
//				}
//			}
//			//check post buffer for number and sms msg
//			if (postBuffer != null) {
//				for (int x = 0; x < postBuffer.length; x++) {
//					if (postBuffer[x] != null) {
//						String testLine = preBuffer[x];
//						if (testLine.contains("SMS message body (raw):") && testLine.contains("'")) {
//							testLine = testLine.substring(testLine.indexOf("'") + 1,
//									testLine.length() - 1);
//							return testLine;
//						}
//					}
//				}
//			}
//		}
//		return null;
//	}
//
//	private String findSmsNumber(String[] preBuffer, String[] postBuffer) {
//		//check pre buffer for number and sms msg
//		if (preBuffer != null) {
//			for (String preBufferLine : preBuffer) {
//				if (preBufferLine != null) {
//					if (preBufferLine.contains("SMS originating address:") && preBufferLine.contains("+")) {
//						return preBufferLine.substring(preBufferLine.indexOf("+"));
//					} else if (preBufferLine.contains("OrigAddr")) {
//						preBufferLine = preBufferLine.substring(preBufferLine.indexOf("OrigAddr")).replace("OrigAddr",
//								"").trim();
//						return preBufferLine;
//					}
//				}
//			}
//		}
//		//check post buffer for number and sms msg
//		if (postBuffer != null) {
//			for (String postBufferLine : postBuffer) {
//				if (postBufferLine != null) {
//					if (postBufferLine.contains("SMS originating address:") && postBufferLine.contains("+")) {
//						return postBufferLine.substring(postBufferLine.indexOf("+"));
//					} else if (postBufferLine.contains("OrigAddr")) {
//						postBufferLine = postBufferLine.substring(postBufferLine.indexOf("OrigAddr")).replace
//								("OrigAddr", "").trim();
//						return postBufferLine;
//					}
//				}
//			}
//
//		}
//		return null;
//	}
//
//
//	public enum SmsType {
//		SILENT(
//				R.string.alert_silent_sms_detected,
//				R.string.typezero_header,
//				R.string.typezero_data
//		),
//		MWI(
//				R.string.alert_mwi_detected,
//				R.string.typemwi_header,
//				R.string.typemwi_data
//		),
//		WAP_PUSH(
//				R.string.alert_silent_wap_sms_detected,
//				R.string.typewap_header,
//				R.string.typewap_data
//		);
//
//		@StringRes
//		private int alert;
//
//		@StringRes
//		private int title;
//
//		@StringRes
//		private int message;
//
//		SmsType(@StringRes int alert,
//		        @StringRes int title,
//		        @StringRes int message) {
//			this.alert = alert;
//			this.title = title;
//			this.message = message;
//		}
//
//		@StringRes
//		public int getAlert() {
//			return alert;
//		}
//
//		@StringRes
//		public int getTitle() {
//			return title;
//		}
//
//		@StringRes
//		public int getMessage() {
//			return message;
//		}
//	}
//}
