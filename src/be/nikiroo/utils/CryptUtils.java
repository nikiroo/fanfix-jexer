package be.nikiroo.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.SSLException;

import be.nikiroo.utils.streams.Base64InputStream;
import be.nikiroo.utils.streams.Base64OutputStream;

/**
 * Small utility class to do AES encryption/decryption.
 * <p>
 * It is multi-thread compatible, but beware:
 * <ul>
 * <li>The encrypt/decrypt calls are serialized</li>
 * <li>The streams are independent (and thus parallel)</li>
 * </ul>
 * <p>
 * Do not assume it is secure; it just here to offer a more-or-less protected
 * exchange of data because anonymous and self-signed certificates backed SSL is
 * against Google wishes, and I need Android support.
 * 
 * @author niki
 */
public class CryptUtils {
	static private final String AES_NAME = "AES/CFB128/NoPadding";

	private Cipher ecipher;
	private Cipher dcipher;
	private byte[] bytes32;

	/**
	 * Small and lazy-easy way to initialize a 128 bits key with
	 * {@link CryptUtils}.
	 * <p>
	 * <b>Some</b> part of the key will be used to generate a 128 bits key and
	 * initialize the {@link CryptUtils}; even NULL will generate something.
	 * <p>
	 * <b>This is most probably not secure. Do not use if you actually care
	 * about security.</b>
	 * 
	 * @param key
	 *            the {@link String} to use as a base for the key, can be NULL
	 */
	public CryptUtils(String key) {
		try {
			init(key2key(key));
		} catch (InvalidKeyException e) {
			// We made sure that the key is correct, so nothing here
			e.printStackTrace();
		}
	}

	/**
	 * Create a new instance of {@link CryptUtils} with the given 128 bits key.
	 * <p>
	 * The key <b>must</b> be exactly 128 bits long.
	 * 
	 * @param bytes32
	 *            the 128 bits (32 bytes) of the key
	 * 
	 * @throws InvalidKeyException
	 *             if the key is not an array of 128 bits
	 */
	public CryptUtils(byte[] bytes32) throws InvalidKeyException {
		init(bytes32);
	}

	/**
	 * Wrap the given {@link InputStream} so it is transparently encrypted by
	 * the current {@link CryptUtils}.
	 * 
	 * @param in
	 *            the {@link InputStream} to wrap
	 * @return the auto-encode {@link InputStream}
	 */
	public InputStream encrypt(InputStream in) {
		Cipher ecipher = newCipher(Cipher.ENCRYPT_MODE);
		return new CipherInputStream(in, ecipher);
	}

	/**
	 * Wrap the given {@link InputStream} so it is transparently encrypted by
	 * the current {@link CryptUtils} and encoded in base64.
	 * 
	 * @param in
	 *            the {@link InputStream} to wrap
	 * 
	 * @return the auto-encode {@link InputStream}
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 */
	public InputStream encrypt64(InputStream in) throws IOException {
		return new Base64InputStream(encrypt(in), true);
	}

	/**
	 * Wrap the given {@link OutputStream} so it is transparently encrypted by
	 * the current {@link CryptUtils}.
	 * 
	 * @param out
	 *            the {@link OutputStream} to wrap
	 * 
	 * @return the auto-encode {@link OutputStream}
	 */
	public OutputStream encrypt(OutputStream out) {
		Cipher ecipher = newCipher(Cipher.ENCRYPT_MODE);
		return new CipherOutputStream(out, ecipher);
	}

	/**
	 * Wrap the given {@link OutputStream} so it is transparently encrypted by
	 * the current {@link CryptUtils} and encoded in base64.
	 * 
	 * @param out
	 *            the {@link OutputStream} to wrap
	 * 
	 * @return the auto-encode {@link OutputStream}
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 */
	public OutputStream encrypt64(OutputStream out) throws IOException {
		return encrypt(new Base64OutputStream(out, true));
	}

	/**
	 * Wrap the given {@link OutputStream} so it is transparently decoded by the
	 * current {@link CryptUtils}.
	 * 
	 * @param in
	 *            the {@link InputStream} to wrap
	 * 
	 * @return the auto-decode {@link InputStream}
	 */
	public InputStream decrypt(InputStream in) {
		Cipher dcipher = newCipher(Cipher.DECRYPT_MODE);
		return new CipherInputStream(in, dcipher);
	}

	/**
	 * Wrap the given {@link OutputStream} so it is transparently decoded by the
	 * current {@link CryptUtils} and decoded from base64.
	 * 
	 * @param in
	 *            the {@link InputStream} to wrap
	 * 
	 * @return the auto-decode {@link InputStream}
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 */
	public InputStream decrypt64(InputStream in) throws IOException {
		return decrypt(new Base64InputStream(in, false));
	}

	/**
	 * Wrap the given {@link OutputStream} so it is transparently decoded by the
	 * current {@link CryptUtils}.
	 * 
	 * @param out
	 *            the {@link OutputStream} to wrap
	 * @return the auto-decode {@link OutputStream}
	 */
	public OutputStream decrypt(OutputStream out) {
		Cipher dcipher = newCipher(Cipher.DECRYPT_MODE);
		return new CipherOutputStream(out, dcipher);
	}

	/**
	 * Wrap the given {@link OutputStream} so it is transparently decoded by the
	 * current {@link CryptUtils} and decoded from base64.
	 * 
	 * @param out
	 *            the {@link OutputStream} to wrap
	 * 
	 * @return the auto-decode {@link OutputStream}
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 */
	public OutputStream decrypt64(OutputStream out) throws IOException {
		return new Base64OutputStream(decrypt(out), false);
	}

	/**
	 * This method required an array of 128 bits.
	 * 
	 * @param bytes32
	 *            the array, which <b>must</b> be of 128 bits (32 bytes)
	 * 
	 * @throws InvalidKeyException
	 *             if the key is not an array of 128 bits (32 bytes)
	 */
	private void init(byte[] bytes32) throws InvalidKeyException {
		if (bytes32 == null || bytes32.length != 32) {
			throw new InvalidKeyException(
					"The size of the key must be of 128 bits (32 bytes), it is: "
							+ (bytes32 == null ? "null" : "" + bytes32.length)
							+ " bytes");
		}

		this.bytes32 = bytes32;
		this.ecipher = newCipher(Cipher.ENCRYPT_MODE);
		this.dcipher = newCipher(Cipher.DECRYPT_MODE);
	}

	/**
	 * Create a new {@link Cipher}of the given mode (see
	 * {@link Cipher#ENCRYPT_MODE} and {@link Cipher#ENCRYPT_MODE}).
	 * 
	 * @param mode
	 *            the mode ({@link Cipher#ENCRYPT_MODE} or
	 *            {@link Cipher#ENCRYPT_MODE})
	 * 
	 * @return the new {@link Cipher}
	 */
	private Cipher newCipher(int mode) {
		try {
			// bytes32 = 32 bytes, 32 > 16
			byte[] iv = new byte[16];
			for (int i = 0; i < iv.length; i++) {
				iv[i] = bytes32[i];
			}
			IvParameterSpec ivspec = new IvParameterSpec(iv);
			Cipher cipher = Cipher.getInstance(AES_NAME);
			cipher.init(mode, new SecretKeySpec(bytes32, "AES"), ivspec);
			return cipher;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(
					"Cannot initialize encryption sub-system", e);
		}
	}

	/**
	 * Encrypt the data.
	 * 
	 * @param data
	 *            the data to encrypt
	 * 
	 * @return the encrypted data
	 * 
	 * @throws SSLException
	 *             in case of I/O error (i.e., the data is not what you assumed
	 *             it was)
	 */
	public byte[] encrypt(byte[] data) throws SSLException {
		synchronized (ecipher) {
			try {
				return ecipher.doFinal(data);
			} catch (IllegalBlockSizeException e) {
				throw new SSLException(e);
			} catch (BadPaddingException e) {
				throw new SSLException(e);
			}
		}
	}

	/**
	 * Encrypt the data.
	 * 
	 * @param data
	 *            the data to encrypt
	 * 
	 * @return the encrypted data
	 * 
	 * @throws SSLException
	 *             in case of I/O error (i.e., the data is not what you assumed
	 *             it was)
	 */
	public byte[] encrypt(String data) throws SSLException {
		return encrypt(StringUtils.getBytes(data));
	}

	/**
	 * Encrypt the data, then encode it into Base64.
	 * 
	 * @param data
	 *            the data to encrypt
	 * @param zip
	 *            TRUE to also compress the data in GZIP format; remember that
	 *            compressed and not-compressed content are different; you need
	 *            to know which is which when decoding
	 * 
	 * @return the encrypted data, encoded in Base64
	 * 
	 * @throws SSLException
	 *             in case of I/O error (i.e., the data is not what you assumed
	 *             it was)
	 */
	public String encrypt64(String data) throws SSLException {
		return encrypt64(StringUtils.getBytes(data));
	}

	/**
	 * Encrypt the data, then encode it into Base64.
	 * 
	 * @param data
	 *            the data to encrypt
	 * 
	 * @return the encrypted data, encoded in Base64
	 * 
	 * @throws SSLException
	 *             in case of I/O error (i.e., the data is not what you assumed
	 *             it was)
	 */
	public String encrypt64(byte[] data) throws SSLException {
		try {
			return StringUtils.base64(encrypt(data));
		} catch (IOException e) {
			// not exactly true, but we consider here that this error is a crypt
			// error, not a normal I/O error
			throw new SSLException(e);
		}
	}

	/**
	 * Decode the data which is assumed to be encrypted with the same utilities.
	 * 
	 * @param data
	 *            the encrypted data to decode
	 * 
	 * @return the original, decoded data
	 * 
	 * @throws SSLException
	 *             in case of I/O error
	 */
	public byte[] decrypt(byte[] data) throws SSLException {
		synchronized (dcipher) {
			try {
				return dcipher.doFinal(data);
			} catch (IllegalBlockSizeException e) {
				throw new SSLException(e);
			} catch (BadPaddingException e) {
				throw new SSLException(e);
			}
		}
	}

	/**
	 * Decode the data which is assumed to be encrypted with the same utilities
	 * and to be a {@link String}.
	 * 
	 * @param data
	 *            the encrypted data to decode
	 * 
	 * @return the original, decoded data,as a {@link String}
	 * 
	 * @throws SSLException
	 *             in case of I/O error
	 */
	public String decrypts(byte[] data) throws SSLException {
		try {
			return new String(decrypt(data), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// UTF-8 is required in all conform JVMs
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Decode the data which is assumed to be encrypted with the same utilities
	 * and is a Base64 encoded value.
	 * 
	 * @param data
	 *            the encrypted data to decode in Base64 format
	 * @param zip
	 *            TRUE to also uncompress the data from a GZIP format
	 *            automatically; if set to FALSE, zipped data can be returned
	 * 
	 * @return the original, decoded data
	 * 
	 * @throws SSLException
	 *             in case of I/O error
	 */
	public byte[] decrypt64(String data) throws SSLException {
		try {
			return decrypt(StringUtils.unbase64(data));
		} catch (IOException e) {
			// not exactly true, but we consider here that this error is a crypt
			// error, not a normal I/O error
			throw new SSLException(e);
		}
	}

	/**
	 * Decode the data which is assumed to be encrypted with the same utilities
	 * and is a Base64 encoded value, then convert it into a String (this method
	 * assumes the data <b>was</b> indeed a UTF-8 encoded {@link String}).
	 * 
	 * @param data
	 *            the encrypted data to decode in Base64 format
	 * @param zip
	 *            TRUE to also uncompress the data from a GZIP format
	 *            automatically; if set to FALSE, zipped data can be returned
	 * 
	 * @return the original, decoded data
	 * 
	 * @throws SSLException
	 *             in case of I/O error
	 */
	public String decrypt64s(String data) throws SSLException {
		try {
			return new String(decrypt(StringUtils.unbase64(data)), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// UTF-8 is required in all conform JVMs
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			// not exactly true, but we consider here that this error is a crypt
			// error, not a normal I/O error
			throw new SSLException(e);
		}
	}

	/**
	 * This is probably <b>NOT</b> secure!
	 * 
	 * @param input
	 *            some {@link String} input
	 * 
	 * @return a 128 bits key computed from the given input
	 */
	static private byte[] key2key(String input) {
		return StringUtils.getMd5Hash("" + input).getBytes();
	}
}
