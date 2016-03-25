// IProxyControl.aidl
package com.android.sms.proxy.service;

// Declare any non-default types here with import statements


interface IProxyControl {
    boolean start();

    	boolean stop();

    	boolean isRunning();

    	int getPort();
}
