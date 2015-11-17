package Client;

import com.oracle.javafx.jmx.json.JSONDocument;

import javax.crypto.SecretKey;
import java.util.Map;

import Client.Client.UID;

/**
 * A secure channel used by client
 */
public class Channel {

    private SecretKey key;

    private SecretKey masterKey;

    private byte[] identifier;

    private final String host;

    private static Map<UID, Channel> channels;

    public static JSONDocument createChannel(UID clientName, String hostname, SecretKey masterKey) {
        JSONDocument result = JSONDocument.createObject();
        // TODO
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
        // TODO
        return result;
    }

    public static JSONDocument send(UID clientName, String route, String message) {
        if (channels.containsKey(clientName)) {
            return channels.get(clientName).send(route, message);
        }
        JSONDocument result = JSONDocument.createObject();
        result.setNumber("result", 1);
        result.setString("message", "client name not found!");
        return result;
    }
}
