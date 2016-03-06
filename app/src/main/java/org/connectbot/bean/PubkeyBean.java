package org.connectbot.bean;

import android.content.ContentValues;

import org.connectbot.util.PubkeyDatabase;
import org.connectbot.util.PubkeyUtils;

import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;

/**
 * @author zyq 16-3-6
 */
public class PubkeyBean extends AbstractBean {

	public static final String BEAN_NAME = "pubkey";

	private static final String KEY_TYPE_RSA = "RSA";

	private static final String KEY_TYPE_DSA = "DSA";

	private static final String KEY_TYPE_EC = "EC";

	/* Database fields */
	private long id ;
	private String nickname = "test9";
	private String type = KEY_TYPE_RSA;
	private byte[] privateKey = new byte[]
			{48, -126, 1, -29, 2, 1, 0, 48, 13, 6, 9, 42, -122, 72, -122, -9, 13, 1, 1, 1, 5, 0, 4, -126, 1, -51, 48,
					-126, 1, -55, 2, 1, 0, 2, 97, 0, -89, -39,
					-62, 24, -99, -115, -92, 65, 42, 122, -20, -111, 31, -35, -33, -27, -21, -18, -65, 19, 108, 74,
					87, 36, -49, 121, -47, -18, 69, -3,
					-33, 105, -56, 116, 56, 89, 21, -22, -16, -74, -28, 85, -12, 4, 102, -91, 73, 80, 64, -44, -65,
					46, -73, -51, 123, -67, 76, 9, -62, 69,
					-39, -97, 48, -58, 112, 27, 12, -22, -107, -24, -123, -39, -8, -55, 55, -100, 52, 2, -86, 62,
					-128, -96, -45, 121, -95, -105, 73, 46,
					47, 6, 26, -88, -22, -97, -57, -67, 2, 3, 1, 0, 1, 2, 96, 56, 9, -61, 123, 100, 94, 125, -112, 36,
					22, -106, 20, 125, -121, 29, -47, 45,
					75, -80, 15, 84, 0, 29, -57, -12, 92, -65, 113, -59, 124, 84, -78, -124, 114, 8, -67, -11, 75, 82,
					-62, 60, -55, 85, -122, -126, -98,
					124, 50, -4, 62, -68, -52, 57, 9, -87, -21, -8, 22, -110, -61, -85, -88, 12, -40, -86, -77, -62,
					-97, -44, 85, 102, 64, -104, 23,
					-126, -5, -80, 93, 18, -30, -128, -110, 48, 101, 97, -59, 126, 23, -104, -125, 81, 4, -15, -49,
					-65, 33, 2, 49, 0, -35, 18, 106, -101,
					-44, 110, -75, -60, 123, 98, -19, -26, 78, 125, -42, 69, 83, 122, -19, 99, -39, 13, 120, -121, 88,
					93, -51, -96, -65, 41, -64, -117,
					-11, 27, 18, -68, 1, -39, 21, -53, -112, -13, 61, -86, -55, -47, -115, 11, 2, 49, 0, -62, 94, -71,
					-24, -42, -14, 41, 55, -103, 29,
					-61, 127, -86, -47, 81, -126, -99, 94, -118, 48, -94, -11, -30, -87, -7, -26, -66, -89, 116, -18,
					-105, 37, 90, -125, -106, -73, -5,
					-120, 73, -78, 73, -28, 83, 77, 72, 40, 43, 87, 2, 48, 38, 127, -81, -117, 37, -86, -122, 3, 10,
					-115, -58, -22, 69, -97, 10, 114,
					-106, 64, 33, 25, 51, 82, 114, 59, -9, -70, -106, 53, -71, 52, -73, 48, 104, 101, -25, -108, 80,
					42, 18, -18, -103, 118, -110, -75,
					-84, 97, -50, -45, 2, 48, 7, -18, 57, -125, -88, -111, -124, -31, 1, -36, 87, 7, -76, 126, -119,
					-26, -36, 104, -23, 22, -122, -17,
					-91, 105, -18, 10, 94, 41, 18, -6, 81, 44, 114, -2, -1, 125, 78, 68, 7, 77, -64, -6, 17, -12, 11,
					71, -88, 77, 2, 48, 88, 121, -25, 67,
					-114, -45, 125, -107, -104, -3, -22, 66, 94, 90, 98, 74, -91, 62, -19, -66, -74, -99, 39, 52, 42,
					14, 41, 18, -47, 35, 35, -36, 72,
					122, 67, -104, -126, 118, 7, -45, -54, 116, -76, -18, -119, 35, -9, 22};
	private byte[] publicKey = new byte[]{
			48, 124, 48, 13, 6, 9, 42, -122, 72, -122, -9, 13, 1, 1, 1, 5, 0, 3, 107, 0, 48, 104, 2, 97, 0, -89, -39,
			-62, 24, -99, -115, -92, 65,
			42, 122, -20, -111, 31, -35, -33, -27, -21, -18, -65, 19, 108, 74, 87, 36, -49, 121, -47, -18, 69, -3,
			-33, 105, -56, 116, 56, 89, 21,
			-22, -16, -74, -28, 85, -12, 4, 102, -91, 73, 80, 64, -44, -65, 46, -73, -51, 123, -67, 76, 9, -62, 69,
			-39, -97, 48, -58, 112, 27, 12,
			-22, -107, -24, -123, -39, -8, -55, 55, -100, 52, 2, -86, 62, -128, -96, -45, 121, -95, -105, 73, 46, 47,
			6, 26, -88, -22, -97, -57,
			-67, 2, 3, 1, 0, 1};
	private boolean encrypted = false;
	private boolean startup = true;
	private boolean confirmUse = false;
	private int lifetime = 0;

	/* Transient values */
	private transient boolean unlocked = false;
	private transient Object unlockedPrivate = null;
	private transient String description;

	@Override
	public String getBeanName() {
		return BEAN_NAME;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getId() {
		return id;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getNickname() {
		return nickname;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

	public void setPrivateKey(byte[] privateKey) {
		if (privateKey == null)
			this.privateKey = null;
		else
			this.privateKey = privateKey.clone();
	}

	public byte[] getPrivateKey() {
		if (privateKey == null)
			return null;
		else
			return privateKey.clone();
	}

	public void setPublicKey(byte[] encoded) {
		if (encoded == null)
			publicKey = null;
		else
			publicKey = encoded.clone();
	}

	public byte[] getPublicKey() {
		if (publicKey == null)
			return null;
		else
			return publicKey.clone();
	}

	public void setEncrypted(boolean encrypted) {
		this.encrypted = encrypted;
	}

	public boolean isEncrypted() {
		return encrypted;
	}

	public void setStartup(boolean startup) {
		this.startup = startup;
	}

	public boolean isStartup() {
		return startup;
	}

	public void setConfirmUse(boolean confirmUse) {
		this.confirmUse = confirmUse;
	}

	public boolean isConfirmUse() {
		return confirmUse;
	}

	public void setLifetime(int lifetime) {
		this.lifetime = lifetime;
	}

	public int getLifetime() {
		return lifetime;
	}

	public void setUnlocked(boolean unlocked) {
		this.unlocked = unlocked;
	}

	public boolean isUnlocked() {
		return unlocked;
	}

	public void setUnlockedPrivate(Object unlockedPrivate) {
		this.unlockedPrivate = unlockedPrivate;
	}

	public Object getUnlockedPrivate() {
		return unlockedPrivate;
	}

	public String getDescription() {
		if (description == null) {
			final StringBuilder sb = new StringBuilder();
			try {
				final PublicKey pubKey = PubkeyUtils.decodePublic(publicKey, type);
				if (KEY_TYPE_RSA.equals(type)) {
					int bits = ((RSAPublicKey) pubKey).getModulus().bitLength();
					sb.append("RSA ");
					sb.append(bits);
					sb.append("-bit");
				} else if (KEY_TYPE_DSA.equals(type)) {
					sb.append("DSA 1024-bit");
				} else if (KEY_TYPE_EC.equals(type)) {
					int bits = ((ECPublicKey) pubKey).getParams().getCurve().getField()
							.getFieldSize();
					sb.append("EC ");
					sb.append(bits);
					sb.append("-bit");
				} else {
					sb.append("Unknown Key Type");
				}
			} catch (NoSuchAlgorithmException e) {
				sb.append("Unknown Key Type");
			} catch (InvalidKeySpecException e) {
				sb.append("Unknown Key Type");
			}

			if (encrypted)
				sb.append(" (encrypted)");

			description = sb.toString();
		}
		return description;
	}

	/* (non-Javadoc)
	 * @see org.connectbot.bean.AbstractBean#getValues()
	 */
	@Override
	public ContentValues getValues() {
		ContentValues values = new ContentValues();

		values.put(PubkeyDatabase.FIELD_PUBKEY_NICKNAME, nickname);
		values.put(PubkeyDatabase.FIELD_PUBKEY_TYPE, type);
		values.put(PubkeyDatabase.FIELD_PUBKEY_PRIVATE, privateKey);
		values.put(PubkeyDatabase.FIELD_PUBKEY_PUBLIC, publicKey);
		values.put(PubkeyDatabase.FIELD_PUBKEY_ENCRYPTED, encrypted ? 1 : 0);
		values.put(PubkeyDatabase.FIELD_PUBKEY_STARTUP, startup ? 1 : 0);
		values.put(PubkeyDatabase.FIELD_PUBKEY_CONFIRMUSE, confirmUse ? 1 : 0);
		values.put(PubkeyDatabase.FIELD_PUBKEY_LIFETIME, lifetime);
		return null;
	}

}
