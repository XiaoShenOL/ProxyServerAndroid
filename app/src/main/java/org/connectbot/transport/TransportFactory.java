package org.connectbot.transport;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

/**
 * @author zyq 16-3-5
 */
public class TransportFactory {

	private static final String TAG = "CB.TransportFactory";

	private static String[] transportNames = {
			SSH.getProtocolName(),

	};

	/**
	 * @param protocol
	 * @return
	 */
	public static AbsTransport getTransport(String protocol) {
		if (SSH.getProtocolName().equals(protocol)) {
			return new SSH();
		}  else {
			return null;
		}
	}

	public static Uri getUri(String scheme, String input) {
		Log.d("TransportFactory", String.format(
				"Attempting to discover URI for scheme=%s on input=%s", scheme,
				input));
		if (SSH.getProtocolName().equals(scheme))
			return SSH.getUri(input);
		else
			return null;
	}

	public static String[] getTransportNames() {
		return transportNames;
	}

	public static boolean isSameTransportType(AbsTransport a, AbsTransport b) {
		if (a == null || b == null)
			return false;

		return a.getClass().equals(b.getClass());
	}

	public static boolean canForwardPorts(String protocol) {
		// TODO uh, make this have less knowledge about its children
		if (SSH.getProtocolName().equals(protocol)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * @param protocol text name of protocol
	 * @param context
	 * @return expanded format hint
	 */
	public static String getFormatHint(String protocol, Context context) {
		if (SSH.getProtocolName().equals(protocol)) {
			return SSH.getFormatHint(context);
		} else {
			return AbsTransport.getFormatHint(context);
		}
	}

//	/**
//	 * @param hostdb Handle to HostDatabase
//	 * @param uri URI to target server
//	 * @return true when host was found
//	 */
//	public static HostBean findHost(HostStorage hostdb, Uri uri) {
//		AbsTransport transport = getTransport(uri.getScheme());
//
//		Map<String, String> selection = new HashMap<String, String>();
//
//		transport.getSelectionArgs(uri, selection);
//		if (selection.size() == 0) {
//			Log.e(TAG, String.format("Transport %s failed to do something useful with URI=%s",
//					uri.getScheme(), uri.toString()));
//			throw new IllegalStateException("Failed to get needed selection arguments");
//		}
//
//		return hostdb.findHost(selection);
//	}
}
