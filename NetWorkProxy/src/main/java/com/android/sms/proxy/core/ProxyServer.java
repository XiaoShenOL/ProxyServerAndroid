package com.android.sms.proxy.core;

import android.util.Log;

import com.android.sms.proxy.entity.NativeParams;
import com.android.sms.proxy.service.ProxyServiceUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;
import java.util.Set;

public class ProxyServer {

	private static final boolean DEBUG = NativeParams.HEARTBEAT_PROXY_SERVER_DEBUG;
	public static final String TAG = "ProxyServer";
	private static final int DEFAULT_PORT = ProxyServiceUtil.getDestPort();
	private static final int MAX_PORT = 65535; // real max can be 65535

	private static volatile ProxyServer instance;


	public static ProxyServer getInstance() {
		synchronized (ProxyServer.class) {
			if (instance == null) {
				instance = new ProxyServer();
			}
		}
		return instance;
	}

	private int port;
	private boolean running;
	private Selector selector;
	private ServerSocketChannel server;

	private ProxyServer() {
		port = DEFAULT_PORT;
		running = false;
	}

	public int getPort() {
		return port;
	}

	public Selector getSeletor() {
		return selector;
	}

	public boolean isRunning() {
		return running;
	}

	public synchronized boolean start() {
		if (running) {
			return false;
		}

		if (DEBUG) {
			Log.d(TAG, "start proxy server");
		}
		try {
			selector = Selector.open();
		} catch (Throwable e) {
			if (DEBUG) {
				Log.e(TAG, "create selector exception", e);
			}
			return false;
		}

		try {
			// 获得一个ServerSocket通道
			server = ServerSocketChannel.open();
			//　设置该通道是非阻塞的
			server.configureBlocking(false);
		} catch (Throwable e) {
			if (DEBUG) {
				Log.e(TAG, "create server channel exception", e);
			}
			return false;
		}

		while (true && port < MAX_PORT) {
			try {
				server.socket().bind(new InetSocketAddress(port));
			} catch (IOException e) {
				if (DEBUG) {
					Log.d(TAG, "proxyService.start()函数异常:" + e.toString());
				}
				++port;
				if (DEBUG) {
					Log.d(TAG, "当前端口不满足，重新添加端口号：" + port);
				}
				continue;
			}
			if (DEBUG) {
				Log.d(TAG, "proxy server listen port " + port);
			}
			break;
		}

		if (port >= MAX_PORT) {
			return false;
		}

		try {
			//将通道管理器和该通道绑定，并为该通道注册SelectionKey.OP_ACCEPT事件，注册该事件后，
			//当该事件到达时候，selector.select()会返回，如果该事件没有到达selector.select()会一直阻塞
			server.register(selector, SelectionKey.OP_ACCEPT);
		} catch (ClosedChannelException e) {
			if (DEBUG) {
				Log.e(TAG, "register selector exception", e);
			}
			return false;
		}

		running = true;
		Thread t = new Thread(new Runnable() {
			public void run() {
				doProxy();
				running = false;
			}
		});
		t.setDaemon(false);
		t.setName("ProxyServer");
		t.start();
		return true;
	}

	public synchronized boolean stop() {
		if (!running) {
			return false;
		}

		if (DEBUG) {
			Log.d(TAG, "stop proxy server");
		}
		running = false;

		try {
			selector.wakeup();
			selector.close();
			selector = null;
		} catch (Exception e) {
			if (DEBUG) {
				Log.e(TAG, "close selector exception.", e);
			}
		}

		try {
			server.close();
			server = null;
		} catch (IOException e) {
			if (DEBUG) {
				Log.e(TAG, "close server exception.", e);
			}
		}
		return true;
	}

	private void doProxy() {
		if (DEBUG) {
			Log.d(TAG, "do proxy server start");
		}
		while (true) {
			if (DEBUG) {
				Log.d(TAG, "server " + server + " selector " + selector);
			}
			if (server == null || selector == null) {
				break;
			}

			Set<SelectionKey> keys = null;
			try {
				int number = selector.select();
				if (DEBUG) {
					Log.d(TAG, "selector.select()!!!!" + number);
				}
				if (!selector.isOpen()) {
					break;
				}
				keys = selector.selectedKeys();
			} catch (Exception e) {
				if (DEBUG) {
					Log.e(TAG, "selector select exception", e);
				}
				continue;
			}

			Iterator<SelectionKey> iterator = keys.iterator();
			while (iterator.hasNext()) {
				SelectionKey key = iterator.next();
				iterator.remove();

				//获取关联的管道并且清除缓存区,难道是request channel
				Object attr = key.attachment();
				ChannelPair cp = null;
				if (attr instanceof ChannelPair) {
					if (DEBUG) {
						Log.d(TAG, "attr --> channelPair");
					}
					cp = (ChannelPair) attr;
				} else {
					cp = new ChannelPair();
				}
				try {
					cp.handleKey(key);
				} catch (Exception e) {
					// catch handle key exception
					if (DEBUG) {
						Log.e(TAG, e.toString());
					}
				}
			}

		}
		if (DEBUG) {
			Log.d(TAG, "do proxy server finish");
		}
	}
}
