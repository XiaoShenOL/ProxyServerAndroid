package net.youmi.android.libs.common.network;

/**
 * 客户端错误码
 * 
 * @author jen created on 2013-5-19
 * @author zhitaocai edit 2014-5-13
 */
public interface Net_Client_Exception {

	public static final int NoException = 0;

	/**
	 * 网络与服务器建立连接超时
	 */
	public static final int ConnectTimeoutException = -100;

	/**
	 * 从ConnectionManager管理的连接池中取出连接超时
	 */
	public static final int ConnectionPoolTimeoutException = -101;

}
