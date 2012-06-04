package org.openuat.android.service;

import java.nio.ByteBuffer;
import java.security.SecureRandom;

import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.AESFastEngine;
import org.bouncycastle.crypto.modes.GCMBlockCipher;
import org.bouncycastle.crypto.params.AEADParameters;
import org.bouncycastle.crypto.params.KeyParameter;

// http://stackoverflow.com/a/10366194
public class AESGCM {
	/**
	 * Used to create IV
	 */
	static private final SecureRandom Random = new SecureRandom();

	static public int NonceBitSize = 128;
	static public int MacBitSize = 128;
	static public int KeyBitSize = 256;

	/**
	 * Simple Encryption And Authentication (AES-GCM) of a byte-array.
	 * 
	 * @param secretMessage
	 *            The byte-array to be encrypted.
	 * @param key
	 *            The key to be used.
	 * @return An encrypted byte-array if successful, null otherwise.
	 * @throws IllegalStateException
	 * @throws InvalidCipherTextException
	 * @throws IllegalArgumentException
	 */
	public static byte[] SimpleEncrypt(final byte[] secretMessage,
			final byte[] key) throws IllegalStateException,
			InvalidCipherTextException, IllegalArgumentException {
		// User Error Checks
		if (key == null || key.length != KeyBitSize / 8)
			throw new IllegalArgumentException(String.format(
					"Key needs to be %d bit!", KeyBitSize));

		if (secretMessage == null || secretMessage.length == 0)
			throw new IllegalArgumentException("Secret Message Required!");

		// Using random nonce large enough not to repeat
		byte[] nonce = new byte[NonceBitSize / 8];
		Random.nextBytes(nonce);

		GCMBlockCipher cipher = new GCMBlockCipher(new AESFastEngine());
		AEADParameters parameters = new AEADParameters(new KeyParameter(key),
				MacBitSize, nonce, new byte[0]);
		cipher.init(true, parameters);

		// Generate Cipher Text With Auth Tag
		byte[] cipherText = new byte[cipher.getOutputSize(secretMessage.length)];
		int len = cipher.processBytes(secretMessage, 0, secretMessage.length,
				cipherText, 0);
		cipher.doFinal(cipherText, len);

		ByteBuffer buffer = ByteBuffer.allocate(nonce.length
				+ cipherText.length);
		buffer.put(nonce, 0, nonce.length);
		buffer.put(cipherText, 0, cipherText.length);
		return buffer.array();
	}

	/**
	 * Simple Decryption & Authentication (AES-GCM) of a byte-array.
	 * 
	 * @param encryptedMessage
	 *            The byte array to be decrypted
	 * @param key
	 *            The key to be used
	 * @return The decrypted byte array if successful, null otherwise.
	 * @throws IllegalStateException
	 * @throws InvalidCipherTextException
	 * @throws IllegalArgumentException
	 */
	public static byte[] SimpleDecrypt(final byte[] encryptedMessage,
			final byte[] key) throws IllegalStateException,
			InvalidCipherTextException, IllegalArgumentException {
		// User Error Checks
		if (key == null || key.length != KeyBitSize / 8)
			throw new IllegalArgumentException(String.format(
					"Key needs to be {0} bit!", KeyBitSize));

		if (encryptedMessage == null || encryptedMessage.length == 0)
			throw new IllegalArgumentException("Encrypted Message Required!");

		ByteBuffer cipherReader = ByteBuffer.wrap(encryptedMessage);

		// Grab Nonce
		byte[] nonce = new byte[NonceBitSize / 8];
		cipherReader.get(nonce);

		GCMBlockCipher cipher = new GCMBlockCipher(new AESFastEngine());
		AEADParameters parameters = new AEADParameters(new KeyParameter(key),
				MacBitSize, nonce, new byte[0]);
		cipher.init(false, parameters);

		// Decrypt Cipher Text
		byte[] cipherText = new byte[encryptedMessage.length - nonce.length];
		cipherReader.get(cipherText);

		byte[] plainText = new byte[cipher.getOutputSize(cipherText.length)];
		int len = cipher.processBytes(cipherText, 0, cipherText.length,
				plainText, 0);
		cipher.doFinal(plainText, len);

		// Grab Authentication
		byte[] cipherTag = new byte[MacBitSize / 8];
		System.arraycopy(cipherText, cipherText.length - cipherTag.length,
				cipherTag, 0, cipherTag.length);

		// Check Autentication
		byte[] calcTag = cipher.getMac();
		boolean auth = true;
		for (int i = 0; i < cipherTag.length; i++)
			auth = auth && cipherTag[i] == calcTag[i];

		if (!auth)
			return null;
		else
			return plainText;

	}
}
