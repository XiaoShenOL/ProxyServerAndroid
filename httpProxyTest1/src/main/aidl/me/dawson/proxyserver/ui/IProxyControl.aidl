// IProxyControl.aidl
package me.dawson.proxyserver.ui;

// Declare any non-default types here with import statements

interface IProxyControl {
    boolean start();

    	boolean stop();

    	boolean isRunning();

    	int getPort();
}
