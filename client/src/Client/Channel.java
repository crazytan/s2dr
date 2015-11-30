package Client;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import Client.ClientCrypto;
import Client.SecureClient.UID;
import com.sun.corba.se.spi.activation.IIOP_CLEAR_TEXT;
import com.google.gson.Gson;
import sun.security.rsa.RSAPublicKeyImpl;

/**
 * A secure channel used by client
 */
public class Channel {

    private SecretKey key;

    private String clientIdentifier;

    private String serverIdentifier;

    private InsecureClient _client;

    private Channel(SecretKey key, String clientIdentifier, String serverIdentifier, InsecureClient _client) {
        this.key = key;
        this.clientIdentifier = clientIdentifier;
        this.serverIdentifier = serverIdentifier;
        this._client = _client;
    }

    private String encrypt(String plainText) {
        try {
            Cipher cipher = getAESCipher();
            cipher.init(Cipher.ENCRYPT_MODE, key);
            // TODO
            byte[] encrypted = cipher.doFinal(plainText.getBytes("US-ASCII"));
            return new String(encrypted, "US-ASCII");
        }
        catch (InvalidKeyException e) {
            e.printStackTrace();
            return "";
        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "";
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String decrypt(String cipherText) {
        try {
            Cipher cipher = getAESCipher();
            cipher.init(Cipher.DECRYPT_MODE, key);
            // TODO
            byte[] decrypted = cipher.doFinal(cipherText.getBytes("US-ASCII"));
            return new String(decrypted, "US-ASCII");
        }
        catch (InvalidKeyException e) {
            e.printStackTrace();
            return "";
        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "";
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private SecureMessage send(String route, String message) {
        try {
            String response = _client.send(route,
                    "{\"identifier\":\"" + clientIdentifier + "\"," +
                    "\"message\":\"" + encrypt(message) + "\"}");
            return SecureMessage.newMessage(response);
        }
        catch (Exception e) {
            return SecureMessage.errorMessage(e.getMessage());
        }
    }

    private static Map<UID, Channel> channels;

    private static Cipher getAESCipher() {
        try {
            return Cipher.getInstance("AES");
        }
        catch (Exception e) { return null; }
    }

    public static InsecureMessage createChannel(UID clientName, String hostname, SecretKey masterKey, PublicKey publicKey, PrivateKey privateKey) {
        InsecureClient _client = new InsecureClient(discover(hostname));

        //Phase 1
        RSAPublicKeyImpl publicKeyImpl = (RSAPublicKeyImpl) publicKey;
        String sMessage1 = ""; // TODO: publicKey format
        byte[] sMessageByte1 = ClientCrypto.doSHA256(sMessage1.getBytes());
        String signature1 = ClientCrypto.toHexString(ClientCrypto.Sign(sMessageByte1, privateKey));
        String response1 = _client.send("init", "{\"phase\":1,\"message\":" + ClientCrypto.toHexString(sMessage1.getBytes()) + "\"," +
                "\"signature\":\"" + signature1 + "\"," + "\"certificate\":\"" + "" +"\"}");

        Gson gson = new Gson();
        Map<String, String> map = new HashMap<String, String>();
        Map<String, String> map1 = gson.fromJson(response1, map.getClass());
        String rMessage1 = map1.get("message");
        byte[] rMessageByte1 = ClientCrypto.toByte(rMessage1);

        //TODO: check CA
        PublicKey serverPublicKey = null; // TODO: transfer rMessageByte1 to serverPublicKey

        String rSign1 = map1.get("signature");
        byte[] rSignByte1 = ClientCrypto.toByte(rSign1);
        if (!Arrays.equals(ClientCrypto.doSHA256(rMessageByte1),ClientCrypto.RSADecrypt(rSignByte1, serverPublicKey))) {
            System.out.println("phase1 signature not match!");
            return null;
        }

        //Phase 2
        String sMessage2 = null;
        String signature2 = null;
        SecretKey clientKey = ClientCrypto.GenerateAESKey(256);
        try {
            byte[] clientKeyByte = clientKey.toString().getBytes("US-ASCII");
            sMessage2 = ClientCrypto.toHexString(ClientCrypto.RSAEncrypt(clientKeyByte, serverPublicKey)); //TODO: check encode format
            byte[] sMessageByte2 = ClientCrypto.doSHA256(sMessage2.getBytes());
            signature2 = ClientCrypto.toHexString(ClientCrypto.Sign(sMessageByte2, privateKey));
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        String response2 = _client.send("init", "{\"phase\":2,\"message\":" + sMessage2 + "\"," +
                "\"signature\":\"" + signature2 + "\"," + "\"certificate\":\"" + "" +"\"}");

        Map<String, String> map2 = gson.fromJson(response2, map.getClass());
        String rMessage2 = map2.get("message");
        String rSign2 = map2.get("signature");
        byte[] rMessageByte2 = ClientCrypto.toByte(rMessage2);
        byte[] rSignByte2 = ClientCrypto.toByte(rSign2);

        if (!Arrays.equals(ClientCrypto.doSHA256(rMessageByte2),ClientCrypto.RSADecrypt(rSignByte2, serverPublicKey))) {
            System.out.println("phase2 signature not match!");
            return null;
        }

        byte[] xorKeys = new byte[256];
        byte[] clientKeyByte = clientKey.getEncoded();
        for (int i = 0; i < 256; ++i) {
            xorKeys[i] = (byte)(clientKeyByte[i] | rMessageByte2[i]);
        }

        //Phase 3
        byte[] sharedKey = null;
        byte[] identifier = null;
        String sMessage3 = null;
        String signature3 = null;
        try {
            sharedKey = ClientCrypto.doSHA256(xorKeys);
            identifier = ClientCrypto.AESEncrypt(sharedKey, masterKey);
            sMessage3 = ClientCrypto.toHexString(identifier);
            byte[] sMessageByte3 = ClientCrypto.doSHA256(sMessage3.getBytes());
            signature3 = ClientCrypto.toHexString(ClientCrypto.Sign(sMessageByte3, privateKey));
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        String response3 = _client.send("init", "{\"phase\":3,\"message\":" + sMessage3 + "\"," +
                "\"signature\":\"" + signature3 + "\"," + "\"certificate\":\"" + "" +"\"}");

        Map<String, String> map3 = gson.fromJson(response3, map.getClass());
        String rMessage3 = map3.get("message");
        String rSign3 = map3.get("signature");
        byte[] rMessageByte3 = ClientCrypto.toByte(rMessage3);
        byte[] rSignByte3 = ClientCrypto.toByte(rSign3);

        if (!Arrays.equals(ClientCrypto.doSHA256(rMessageByte3),ClientCrypto.RSADecrypt(rSignByte3, serverPublicKey))) {
            System.out.println("phase3 signature not match!");
            return null;
        }

        // TODO: symmetric key to channel encryption
        SecretKey key = new SecretKeySpec(sharedKey, 0, sharedKey.length, "AES");

        // TODO: generate identifier
        String identifierStr = identifier.toString();
        String serverIdentifierStr = rMessageByte3.toString();

        // TODO: put a new Channel in the map
         channels.put(clientName, new Channel(key, identifierStr, serverIdentifierStr, _client));

//        InsecureMessage result = InsecureMessage.newMessage();
        // TODO
        return null;
    }

    private static String discover(String hostname) {
        return "http://localhost:8888";
    }

    public static InsecureMessage send(UID clientName, String route, String message) {
        if (channels.containsKey(clientName)) {
            Channel channel = channels.get(clientName);
            SecureMessage m = channel.send(route, message);
            if (!m.isSuccess()) return InsecureMessage.errorMessage(m.getMessage());
            if (!m.getIdentifier().equals(channel.serverIdentifier))
                return InsecureMessage.errorMessage("unrecognized identifier!");
            return InsecureMessage.newMessage(channel.decrypt(m.getMessage()));
        }
        return InsecureMessage.errorMessage("client name not found!");
    }
}
