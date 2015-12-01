package Client;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * A helper class for cryptographic operations
 */
public class ClientCrypto {

    public static SecretKey GenerateAESKey(int length) {
        try{
            KeyGenerator kenGen = KeyGenerator.getInstance("AES");
            kenGen.init(length);
            return kenGen.generateKey();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] AESEncrypt(byte[] text, SecretKey key) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return cipher.doFinal(text);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] AESDecrypt(byte[] text, SecretKey key) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, key);
            return cipher.doFinal(text);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] RSAEncrypt(byte[] text, PublicKey key) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return  cipher.doFinal(text);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] RSADecrypt(byte[] cipherText, PrivateKey key) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, key);
            return cipher.doFinal(cipherText);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] RSADecrypt(byte[] cipherText, PublicKey key) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, key);
            return cipher.doFinal(cipherText);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] Sign(byte[] text, PrivateKey key) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return cipher.doFinal(text);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] doSHA256(byte[] text) {
        try {
            MessageDigest hashTool = MessageDigest.getInstance("SHA-256");
            hashTool.update(text);
            return hashTool.digest();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //TODO
    public static String keyToString(PublicKey key) {
        String keyStr = new String("-----BEGIN PUBLIC KEY-----\n");
        keyStr = keyStr + new String(key.getEncoded()) + "-----END PUBLIC KEY-----\n";
        return keyStr;
    }

    //TODO
    public static String keyToString(PrivateKey key) {
        String keyStr = new String("-----BEGIN PRIVATE KEY-----\n");
        keyStr = keyStr + new String(key.getEncoded()) + "-----END PRIVATE KEY-----\n";
        return keyStr;
    }

    //TODO
    public static PublicKey stringToPublicKey(String string) {
        try {
            return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(string.getBytes()));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //TODO
    public static PrivateKey stringToPrivateKey(String string) {
        try {
            return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(string.getBytes()));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //TODO
    public static SecretKey stringToAESKey(String string) {
        try {
            return new SecretKeySpec(string.getBytes(), 0, string.getBytes().length, "AES");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] toByte(String hexString){
        return null;
    }

    public static String toHexString(byte[] text) {
        int len = text.length;
        String rst = new String();
        for (int i = 0; i < len; ++i) {
            rst = rst + ClientCrypto.intToString((text[i] >> 4) & (byte)0x0f) + ClientCrypto.intToString(text[i] & (byte)0x0f);
        }
        return rst;
    }

    private static String intToString(int integer){
        if (integer < 10)
            return Integer.toString(integer);
        switch (integer){
            case 10:
                return new String("A");
            case 11:
                return new String("B");
            case 12:
                return new String("C");
            case 13:
                return new String("D");
            case 14:
                return new String("E");
            case 15:
                return new String("F");
        }
        return null;
    }
}
