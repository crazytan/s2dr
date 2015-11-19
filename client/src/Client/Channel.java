package Client;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Map;

import Client.ClientCrypto;
import Client.SecureClient.UID;
import com.sun.corba.se.spi.activation.IIOP_CLEAR_TEXT;

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

        _client.send("init","\"phase\":1");
        // TODO: symmetric key to channel encryption
        SecretKey key = null;

        // TODO: generate identifier
        String identifier = null;

        // TODO: put a new Channel in the map
        channels.put(clientName, new Channel(key, identifier, _client));

        //Message result = Message.newMessage();
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
