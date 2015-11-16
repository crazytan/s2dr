package Channel;

import javax.crypto.SecretKey;
import java.util.HashMap;

/**
 * A secure channel used by client
 */
public class Channel {

    private SecretKey key;

    private byte[] identifier;

    public Channel(String hostname, SecretKey masterKey) {}

    public HashMap<String, Object> send(String route, byte[] message) {
        return null;
    }
}
