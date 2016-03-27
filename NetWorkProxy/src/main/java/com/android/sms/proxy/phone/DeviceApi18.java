package com.android.sms.proxy.phone;

import android.annotation.TargetApi;
import android.os.Build;
import android.telephony.CellIdentityCdma;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.android.sms.proxy.function.AndroidLogger;
import com.android.sms.proxy.function.Logger;

import java.util.List;

/**
 * This class is taking in consideration newly available network info items
 * that are only available in the AOS API 18 and above. In this case we're
 * concerned with Wcdma Cell info (CellInfoWcdma)
 *
 * See: http://developer.android.com/reference/android/os/Build.VERSION_CODES.html
 *
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class DeviceApi18 {

	public static final Logger log = AndroidLogger.forClass(DeviceApi18.class);

	public static void loadCellInfo(TelephonyManager tm, Device pDevice) {
		int lCurrentApiVersion = android.os.Build.VERSION.SDK_INT;
		try {
			if (pDevice.mCell == null) {
				pDevice.mCell = new Cell();
			}
			List<CellInfo> cellInfoList = tm.getAllCellInfo();
			if (cellInfoList != null) {
				for (final CellInfo info : cellInfoList) {

					//Network Type
					pDevice.mCell.setNetType(tm.getNetworkType());

					if (info instanceof CellInfoGsm) {
						final CellSignalStrengthGsm gsm = ((CellInfoGsm) info).getCellSignalStrength();
						final CellIdentityGsm identityGsm = ((CellInfoGsm) info).getCellIdentity();
						// Signal Strength
						pDevice.mCell.setDBM(gsm.getDbm()); // [dBm]
						// Cell Identity
						pDevice.mCell.setCID(identityGsm.getCid());
						pDevice.mCell.setMCC(identityGsm.getMcc());
						pDevice.mCell.setMNC(identityGsm.getMnc());
						pDevice.mCell.setLAC(identityGsm.getLac());

					} else if (info instanceof CellInfoCdma) {
						final CellSignalStrengthCdma cdma = ((CellInfoCdma) info).getCellSignalStrength();
						final CellIdentityCdma identityCdma = ((CellInfoCdma) info).getCellIdentity();
						// Signal Strength
						pDevice.mCell.setDBM(cdma.getDbm());
						// Cell Identity
						pDevice.mCell.setCID(identityCdma.getBasestationId());
						pDevice.mCell.setMNC(identityCdma.getSystemId());
						pDevice.mCell.setLAC(identityCdma.getNetworkId());
						pDevice.mCell.setSID(identityCdma.getSystemId());

					} else if (info instanceof CellInfoLte) {
						final CellSignalStrengthLte lte = ((CellInfoLte) info).getCellSignalStrength();
						final CellIdentityLte identityLte = ((CellInfoLte) info).getCellIdentity();
						// Signal Strength
						pDevice.mCell.setDBM(lte.getDbm());
						pDevice.mCell.setTimingAdvance(lte.getTimingAdvance());
						// Cell Identity
						pDevice.mCell.setMCC(identityLte.getMcc());
						pDevice.mCell.setMNC(identityLte.getMnc());
						pDevice.mCell.setCID(identityLte.getCi());

					} else if  (lCurrentApiVersion >= Build.VERSION_CODES.JELLY_BEAN_MR2 && info instanceof CellInfoWcdma) {
						final CellSignalStrengthWcdma wcdma = ((CellInfoWcdma) info).getCellSignalStrength();
						final CellIdentityWcdma identityWcdma = ((CellInfoWcdma) info).getCellIdentity();
						// Signal Strength
						pDevice.mCell.setDBM(wcdma.getDbm());
						// Cell Identity
						pDevice.mCell.setLAC(identityWcdma.getLac());
						pDevice.mCell.setMCC(identityWcdma.getMcc());
						pDevice.mCell.setMNC(identityWcdma.getMnc());
						pDevice.mCell.setCID(identityWcdma.getCid());
						pDevice.mCell.setPSC(identityWcdma.getPsc());

					} else {
						log.info("Unknown type of cell signal!"
								+ "\n ClassName: " + info.getClass().getSimpleName()
								+ "\n ToString: " + info.toString());
					}
					if (pDevice.mCell.isValid()) {
						break;
					}
				}
			}
		} catch (NullPointerException npe) {
			log.error("loadCellInfo: Unable to obtain cell signal information: ", npe);
		}
	}

	public static void startListening(TelephonyManager tm, PhoneStateListener listener) {
		tm.listen(listener,
				PhoneStateListener.LISTEN_CELL_INFO |
						PhoneStateListener.LISTEN_CELL_LOCATION |
						PhoneStateListener.LISTEN_DATA_CONNECTION_STATE |
						PhoneStateListener.LISTEN_SERVICE_STATE |
						PhoneStateListener.LISTEN_SIGNAL_STRENGTHS
		);
	}
}
