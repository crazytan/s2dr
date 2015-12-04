package s2dr.Client;

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
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
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
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
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
            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding");
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
            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding");
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
            return hashTool.digest(text);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String divideToLines(String string) {
        String multiLinesStr = "";
        while (string.length() > 64) {
            multiLinesStr = multiLinesStr + string.substring(0, 64) + "\n";
            string = string.substring(64, string.length());
        }
        multiLinesStr = multiLinesStr + string + "\n";
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
            string =  string.replace("-----BEGIN PUBLIC KEY-----\n", "");
            string =  string.replace("-----END PUBLIC KEY-----", "");
            string =  string.replace("\n", "");
            byte[] keyByte = Base64.getDecoder().decode(string.getBytes());
            return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(keyByte));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static PrivateKey stringToPrivateKey(String string) {
        try {
            string =  string.replace("-----BEGIN PRIVATE KEY-----\n", "");
            string =  string.replace("-----END PRIVATE KEY-----", "");
            string =  string.replace("\n", "");
            byte[] keyByte = Base64.getDecoder().decode(string.getBytes());
            return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(keyByte));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static SecretKey stringToAESKey(String string) {
        try {
            string = string.replace("-----BEGIN AES KEY-----\n", "");
            string =  string.replace("-----END AES KEY-----", "");
            string =  string.replace("\n", "");
            byte[] keyByte = Base64.getDecoder().decode(string.getBytes());
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
        if (integer < 10) return (char) (integer + '0');
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
            case 15:
                return 'f';
        }
        return null;
    }
}