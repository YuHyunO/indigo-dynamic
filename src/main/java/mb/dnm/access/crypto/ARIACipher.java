package mb.dnm.access.crypto;

import com.indigo.temp.core.crypto.ARIAEngine;
import com.indigo.temp.core.crypto.AnsiX923Padding;
import com.indigo.temp.core.crypto.CryptoPadding;

import java.security.InvalidKeyException;

public class ARIACipher {
	String masterKey = null;

	public void setPassword(String masterKey) {
		masterKey = (masterKey.length() > 32 ? masterKey.substring(0, 32) : masterKey);

		this.masterKey = masterKey;
	}

	public byte[] encrypt(byte[] data) {
		try {
			CryptoPadding padding = new com.indigo.temp.core.crypto.AnsiX923Padding();

			byte[] mk = padding.addPadding(masterKey.getBytes(), 32);

			ARIAEngine instance = new ARIAEngine(256);

			return instance.encrypt(data, mk);
		} catch (InvalidKeyException ike) {
			throw new RuntimeException(ike);
		}
	}

	public byte[] decrypt(byte[] encryptedData) {
		try {
			CryptoPadding padding = new AnsiX923Padding();

			byte[] mk = padding.addPadding(masterKey.getBytes(), 32);

			ARIAEngine instance = new ARIAEngine(256);

			return instance.decrypt(encryptedData, mk);
		} catch (InvalidKeyException ike) {
			throw new RuntimeException(ike);
		}
	}
}