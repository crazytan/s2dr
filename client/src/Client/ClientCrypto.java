package Client;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * Created by Kard on 11/18/15.
 */
public class ClientCrypto {
    public byte[] AESEncrypt(byte[] text, SecretKey key) {
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
    public byte[] RSAEncrypt(byte[] text, PublicKey key) {
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

    public byte[] RSADecrypt(byte[] cipherText, PrivateKey key) {
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

    public byte[] Sign(byte[] text, PrivateKey key) {
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
}
