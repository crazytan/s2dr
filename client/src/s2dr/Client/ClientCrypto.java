package s2dr.client;

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
import java.util.Base64;

/**
 * A helper class for cryptographic operations
 */
public final class ClientCrypto {

    private ClientCrypto() {}

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
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return cipher.doFinal(text);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

    public static byte[] AESDecrypt(byte[] text, SecretKey key) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, key);
            return cipher.doFinal(text);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

    public static byte[] RSAEncrypt(byte[] text, PublicKey key) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return  cipher.doFinal(text);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

    public static byte[] RSADecrypt(byte[] cipherText, PrivateKey key) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding");
            cipher.init(Cipher.DECRYPT_MODE, key);
            return cipher.doFinal(cipherText);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return new byte[0];
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
        return new byte[0];
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
        return new byte[0];
    }

    public static byte[] doSHA256(byte[] text) {
        try {
            MessageDigest hashTool = MessageDigest.getInstance("SHA-256");
            return hashTool.digest(text);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

    private static String divideToLines(String string) {
        String multiLinesStr = "";
        String tmp = string;
        while (tmp.length() > 64) {
            multiLinesStr = multiLinesStr + tmp.substring(0, 64) + "\n";
            tmp = tmp.substring(64, tmp.length());
        }
        multiLinesStr = multiLinesStr + tmp + "\n";
        return multiLinesStr;
    }

    public static String publicKeyToString(PublicKey key) {
        byte[] keyByte = Base64.getEncoder().encode(key.getEncoded());
        String keyStr = new String(keyByte);
        return "-----BEGIN PUBLIC KEY-----\n" + divideToLines(keyStr) + "-----END PUBLIC KEY-----\n";
    }

    public static String privateKeyToString(PrivateKey key) {
        byte[] keyByte = Base64.getEncoder().encode(key.getEncoded());
        String keyStr = new String(keyByte);
        return "-----BEGIN PRIVATE KEY-----\n" + divideToLines(keyStr) + "-----END PRIVATE KEY-----\n";
    }

    public static String aesKeyToString(SecretKey key) {
        byte[] keyByte = Base64.getEncoder().encode(key.getEncoded());
        String keyStr = new String(keyByte);
        return "-----BEGIN AES KEY-----\n" + divideToLines(keyStr) + "-----END AES KEY-----";
    }

    public static PublicKey stringToPublicKey(String string) {
        try {
            String tmp = string.replace("-----BEGIN PUBLIC KEY-----\n", "").replace("-----END PUBLIC KEY-----", "").replace("\n", "");
            byte[] keyByte = Base64.getDecoder().decode(tmp.getBytes());
            return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(keyByte));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static PrivateKey stringToPrivateKey(String string) {
        try {
            String tmp = string.replace("-----BEGIN PRIVATE KEY-----\n", "").replace("-----END PRIVATE KEY-----", "").replace("\n", "");
            byte[] keyByte = Base64.getDecoder().decode(tmp.getBytes());
            return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(keyByte));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static SecretKey stringToAESKey(String string) {
        try {
            String tmp = string.replace("-----BEGIN AES KEY-----\n", "").replace("-----END AES KEY-----", "").replace("\n", "");
            byte[] keyByte = Base64.getDecoder().decode(tmp.getBytes());
            return new SecretKeySpec(keyByte, 0, keyByte.length, "AES");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] toByte(String hexString){
        int len = hexString.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                    + Character.digit(hexString.charAt(i+1), 16));
        }
        return data;
    }

    public static String toHexString(byte[] text) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length; ++i) {
            sb.append(ClientCrypto.digitToChar((text[i] >> 4) & (byte)0x0f));
            sb.append(ClientCrypto.digitToChar(text[i] & (byte)0x0f));
        }
        return sb.toString();
    }

    private static Character digitToChar(int integer){
        if (integer < 10) {
            return (char) (integer + '0');
        }
            //return Integer.toString(integer);
        switch (integer){
            case 10:
                return 'a';
            case 11:
                return 'b';
            case 12:
                return 'c';
            case 13:
                return 'd';
            case 14:
                return 'e';
            default:
                return 'f';
        }
    }
}
