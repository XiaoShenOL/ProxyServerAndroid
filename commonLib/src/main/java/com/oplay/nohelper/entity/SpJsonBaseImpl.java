package com.oplay.nohelper.entity;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * @author zyq 16-3-9
 */
public class SpJsonBaseImpl<T> implements JsonBase<T>,Serializable {

	@SerializedName("c")
	private int mCode = -1;

	@SerializedName("d")
	private T mData;

	@SerializedName("msg")
	private String mMsg;

	@Override
	public T getData() {
		return mData;
	}

	@Override
	public void setData(T data) {
		mData = data;
	}

	@Override
	public int getCode() {
		return mCode;
	}

	@Override
	public void setCode(int code) {
		mCode = code;
	}

	@Override
	public String getMsg() {
		return mMsg;
	}

	@Override
	public void setMsg(String msg) {
		mMsg = msg;
	}

}
