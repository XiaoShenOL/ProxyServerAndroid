package com.android.sms.proxy.entity;

import com.avos.avoscloud.AVClassName;
import com.avos.avoscloud.AVObject;

/**
 * @author zyq 16-3-31
 */
@AVClassName("CheckInfo")
public class CheckInfo extends AVObject {

	public static final Creator CREATOR = AVObjectCreator.instance;
	public static final String OPERATORS = "operatorsNumber";//运营商电话
	public static final String OPERATORCODE = "operatorCode";//运营商查询指令
	public static final String DELETEOLDDATA = "deleteOldData";//是否删除旧数据

    public CheckInfo(){};

	public String getOperators() {
		return getString(OPERATORS);
	}

	public String getOperatorCode() {
		return getString(OPERATORCODE);
	}

	public String getDeleteOldData() {
		return getString(DELETEOLDDATA);
	}

	public void setOperators(String operators) {
		put(OPERATORS, operators);
	}

	public void setOperatorcode(String operatorcode) {
		put(OPERATORCODE, operatorcode);
	}

	public void setDeleteolddata(String isDeleteOldData) {
		put(DELETEOLDDATA, isDeleteOldData);
	}


}
