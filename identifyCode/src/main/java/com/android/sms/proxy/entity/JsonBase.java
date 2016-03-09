package com.android.sms.proxy.entity;

/**
 * @author zyq 16-3-9
 */
public interface JsonBase<T> {

	//    @SerializedName("c")
//    private int mCode = -1;

	//    @SerializedName("d")
//    private T mData;

	//    @SerializedName("msg")
//    private String mMsg;

//    public T getData() {
//        return mData;
//    }

	public T getData();

	public void setData(T data);

	public int getCode();

	public void setCode(int code);

	public String getMsg();

	public void setMsg(String msg);

}
