package rip.protocol.bridge.shared.utils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

public class EncryptionHandler {

    private Cipher ecipher, dcipher;
    private static String key = "????????????????";

    public EncryptionHandler(SecretKey key) throws Exception {
        ecipher = Cipher.getInstance("AES");
        dcipher = Cipher.getInstance("AES");
        ecipher.init(Cipher.ENCRYPT_MODE, key);
        dcipher.init(Cipher.DECRYPT_MODE, key);
    }

    public String encrypt(String str) throws Exception {
        byte[] utf8 = str.getBytes(StandardCharsets.UTF_8);
        byte[] enc = ecipher.doFinal(utf8);

        return new sun.misc.BASE64Encoder().encode(enc);
    }

    public String decrypt(String str) throws Exception {
        byte[] dec = new sun.misc.BASE64Decoder().decodeBuffer(str);
        byte[] utf8 = dcipher.doFinal(dec);

        return new String(utf8, StandardCharsets.UTF_8);
    }

    public static String encryptUsingKey(String str) {
        SecretKey secretKey = new SecretKeySpec(key.getBytes(), "AES");
        EncryptionHandler encrypter;

        try {
            encrypter = new EncryptionHandler(secretKey);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        String encrypted;
        try {
            encrypted = encrypter.encrypt(str);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return encrypted;
    }

    public static String decryptUsingKey(String str) {
        SecretKey secretKey = new SecretKeySpec(key.getBytes(), "AES");
        EncryptionHandler encrypter;
        try {
            encrypter = new EncryptionHandler(secretKey);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        try {
            return encrypter.decrypt(str);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}