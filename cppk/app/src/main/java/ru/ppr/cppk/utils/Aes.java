package ru.ppr.cppk.utils;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Класс для работы с шифрованием/дешифрованием данных закрытых пин кодом по AES
 * алгоритму. Корректно работает с цифрами и с латинницей, с кириллицей на
 * android не работает...
 *
 * @author G.Kashka
 */

public class Aes {

    private static final byte[] salt = {(byte) 0x1A, (byte) 0x28, (byte) 0x62, (byte) 0xFC, (byte) 0x24, (byte) 0xFA, (byte) 0x61, (byte) 0xD9, (byte) 0x9B, (byte) 0xB2, (byte) 0x69, (byte) 0x73,
            (byte) 0x8E, (byte) 0xC8, (byte) 0x8D, (byte) 0x53};

    public static byte[] encrypt(byte[] data, String pin) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, InvalidKeySpecException,
            InvalidParameterSpecException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = getCiper(pin, Cipher.ENCRYPT_MODE);
        return cipher.doFinal(data);
    }

    public static byte[] decrypt(byte[] data, String pin) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, InvalidKeySpecException,
            InvalidParameterSpecException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = getCiper(pin, Cipher.DECRYPT_MODE);
        return cipher.doFinal(data);
    }

    public static byte[] decrypt(byte[] data, char[] pin) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, InvalidKeySpecException,
            InvalidParameterSpecException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = getCiper(pin, Cipher.DECRYPT_MODE);
        return cipher.doFinal(data);
    }

    private static byte[] getIv(String pin) throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        PBEKeySpec spec = new PBEKeySpec(pin.toCharArray(), salt, 1000, 256);
        SecretKey tmp = factory.generateSecret(spec);
        SecretKeySpec secret = new SecretKeySpec(tmp.getEncoded(), "AES");
        byte[] out = new byte[16];
        for (int i = 0; (i + 16) < secret.getEncoded().length; i++) {
            out[i] = secret.getEncoded()[i + 16];
        }
        return out;
    }

    private static byte[] getIv(char[] pin) throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        PBEKeySpec spec = new PBEKeySpec(pin, salt, 1000, 256);
        SecretKey tmp = factory.generateSecret(spec);
        SecretKeySpec secret = new SecretKeySpec(tmp.getEncoded(), "AES");
        byte[] out = new byte[16];
        for (int i = 0; (i + 16) < secret.getEncoded().length; i++) {
            out[i] = secret.getEncoded()[i + 16];
        }
        return out;
    }

    private static SecretKeySpec getSecretKeySpec(String pin, byte[] salt) throws InvalidKeySpecException, NoSuchAlgorithmException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        PBEKeySpec spec = new PBEKeySpec(pin.toCharArray(), salt, 1000, 128);
        SecretKey tmp = factory.generateSecret(spec);
        SecretKeySpec secret = new SecretKeySpec(tmp.getEncoded(), "AES");
        byte[] out = new byte[16];
        for (int i = 0; (i + 16) < secret.getEncoded().length; i++) {
            out[i] = secret.getEncoded()[i + 16];
        }
        return secret;
    }

    private static SecretKeySpec getSecretKeySpec(char[] pin, byte[] salt) throws InvalidKeySpecException, NoSuchAlgorithmException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        PBEKeySpec spec = new PBEKeySpec(pin, salt, 1000, 128);
        SecretKey tmp = factory.generateSecret(spec);
        SecretKeySpec secret = new SecretKeySpec(tmp.getEncoded(), "AES");
        byte[] out = new byte[16];
        for (int i = 0; (i + 16) < secret.getEncoded().length; i++) {
            out[i] = secret.getEncoded()[i + 16];
        }
        return secret;
    }

    private static Cipher getCiper(String pin, int type) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, InvalidKeySpecException,
            InvalidParameterSpecException {
        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
        IvParameterSpec ivParameterSpec = new IvParameterSpec(getIv(pin));
        cipher.init(type, getSecretKeySpec(pin, salt), ivParameterSpec);
        return cipher;
    }

    private static Cipher getCiper(char[] pin, int type) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, InvalidKeySpecException,
            InvalidParameterSpecException {
        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
        IvParameterSpec ivParameterSpec = new IvParameterSpec(getIv(pin));
        cipher.init(type, getSecretKeySpec(pin, salt), ivParameterSpec);
        return cipher;
    }

}
