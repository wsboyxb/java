package com.xb.security;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;

import com.xb.varlist.VarList;

public class RSA {

	public static final int KEYSIZE = 512;
	
	private KeyPair keyPair;
	private Key publicKey;
	private Key privateKey;
	
	/**
	 * 生成秘钥�?
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	public KeyPair generateKeyPair() throws NoSuchAlgorithmException {
		KeyPairGenerator pairgen = KeyPairGenerator.getInstance("RSA");
		SecureRandom random = new SecureRandom();
		pairgen.initialize(RSA.KEYSIZE, random);
		this.keyPair = pairgen.generateKeyPair();
		return this.keyPair;
	}

	/**
	 * 加密秘钥
	 * @param key
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 */
	public byte[] wrapKey(Key key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException {
		Cipher cipher = Cipher.getInstance("RSA");
//		cipher.init(Cipher.WRAP_MODE, this.privateKey);
		cipher.init(Cipher.WRAP_MODE, this.publicKey);
		byte[] wrappedKey = cipher.wrap(key);
		return wrappedKey;
	}
	
	/**
	 * 解密秘钥
	 * @param wrapedKeyBytes
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 */
	public Key unwrapKey(byte[] wrapedKeyBytes) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
		Cipher cipher = Cipher.getInstance("RSA");
//		cipher.init(Cipher.UNWRAP_MODE, this.publicKey);
		cipher.init(Cipher.UNWRAP_MODE, this.privateKey);
		Key key = cipher.unwrap(wrapedKeyBytes, "AES", Cipher.SECRET_KEY);
		return key;
	}

	public Key getPublicKey() {
		return publicKey;
	}

	public void setPublicKey(Key publicKey) {
		this.publicKey = publicKey;
	}

	public Key getPrivateKey() {
		return privateKey;
	}

	public void setPrivateKey(Key privateKey) {
		this.privateKey = privateKey;
	}
	
	public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, ShortBufferException, BadPaddingException, IOException, ClassNotFoundException {
		// TODO 自动生成的方法存�?

		AES aes = new AES();
		aes.generateKey();
		
		VarList varList = new VarList();
		varList.put("hello moto");
		varList.put(1);
		varList.put("123");
		varList.put(false);
		
		//aes加密消息�?
		InputStream inputStream = new ByteArrayInputStream(varList.toByteArray());
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		aes.encrypt(inputStream, out);
		
		//rsa加密aes的密�?
		RSA rsa = new RSA();
		ObjectInputStream inFileInputStream = new ObjectInputStream(new FileInputStream("F:\\publicKey.ini"));
		Key publicKey = (Key) inFileInputStream.readObject();
		inFileInputStream.close();
		rsa.setPublicKey(publicKey);
		Key aesKey = aes.getKey();
		byte[] encryptAesKey = rsa.wrapKey(aesKey);
		
		//解密aes的密�?
		RSA decryptRSA = new RSA();
		inFileInputStream = new ObjectInputStream(new FileInputStream("F:\\privateKey.ini"));
		Key privateKey = (Key) inFileInputStream.readObject();
		inFileInputStream.close();
		decryptRSA.setPrivateKey(privateKey);
		
		Key decryptAesKey = decryptRSA.unwrapKey(encryptAesKey);
		
		AES newAes = new AES();
		newAes.setKey(decryptAesKey);
		
		byte[] outbyte = out.toByteArray();
		inputStream = new ByteArrayInputStream(outbyte);
		out = new ByteArrayOutputStream();
		newAes.decrypt(inputStream, out);
		
		VarList newVarList = new VarList(out.toByteArray());
		System.out.println(newVarList);
		
	}
}

