package Client;

import javax.crypto.SecretKey;
import java.util.Map;

import Client.InsecureClient;
import Client.Client.UID;

/**
 * A secure channel used by client
 */
public class Channel {

    private SecretKey key;

    private SecretKey masterKey;

    private byte[] identifier;

    private InsecureClient _client;

    private Channel(SecretKey key, SecretKey masterKey, byte[] identifier, InsecureClient _client) {
        this.key = key;
        this.masterKey = masterKey;
        this.identifier = identifier;
        this._client = _client;
    }

    private Message send(String route, String message) {
        Message result = Message.newMessage();
        // TODO
        return result;
    }

    private static Map<UID, Channel> channels;

    public static Message createChannel(UID clientName, String hostname, SecretKey masterKey) {
        InsecureClient _client = new InsecureClient(discover(hostname));

        // TODO: symmetric key to channel encryption
        SecretKey key = null;

        // TODO: generate identifier
        byte[] identifier = null;

        // TODO: put a new Channel in the map
        channels.put(clientName, new Channel(key, masterKey, identifier, _client));

        Message result = Message.newMessage();
        // TODO
        return result;
    }

    private static String discover(String hostname) {
        return "http://localhost:8888";
    }

    public static Message send(UID clientName, String route, String message) {
        if (channels.containsKey(clientName)) {
            return channels.get(clientName).send(route, message);
        }
        return Message.errorMessage("client name not found!");
    }
}
