package Client;

import com.oracle.javafx.jmx.json.JSONDocument;

import javax.crypto.SecretKey;
import java.util.HashMap;

import Client.Client.UID;

/**
 * A secure channel used by client
 */
public class Channel {

    private SecretKey key;

    private SecretKey masterKey;

    private byte[] identifier;

    private final String host;

    private static HashMap<UID, Channel> channels;

    public static JSONDocument createChannel(UID clientName, String hostname, SecretKey masterKey) {
        JSONDocument result = JSONDocument.createObject();
        /*result.setNumber("result", 0);
        result.setString("message", "");*/
        return result;
    }

    private Channel(SecretKey key, SecretKey masterKey, byte[] identifier, String host) {
        this.key = key;
        this.masterKey = masterKey;
        this.identifier = identifier;
        this.host = host;
    }

    private static String discover(String hostname) {
        return "http://localhost:8888";
    }

    private JSONDocument send(String route, String message) {
        JSONDocument result = JSONDocument.createObject();
        return result;
    }

    public static JSONDocument send(UID clientName, String route, String message) {
        return channels.get(clientName).send(route, message);
    }
}
