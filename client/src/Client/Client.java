package Client;

import Channel.Channel;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;

/*
 * A class representing the client side of s2dr service.
 */
public class Client {

    private UID name;

    private SecretKey masterKey;

    private Channel channel;

    public UID getName() {
        return name;
    }

    public Client(String name) {
        this.name = new UID(name);

        // generate master key
        try {
            KeyGenerator gen = KeyGenerator.getInstance("AES");
            masterKey = gen.generateKey();
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        channel = null;
    }

    public void init_session(String hostname) {
        channel = new Channel(hostname, masterKey);
    }

    public void check_out(UID document_id) {}

    public void check_in(UID document_id, SecurityFlag flag) {}

    public void delegate(UID document_id, Client c, int time,
                         Permission p, boolean propagationFlag) {}

    public void safe_delete(UID document_id) {}

    public void terminate() {}

    public static Client All;

    public static void main(String[] args) {
        Client c = new Client("tan");
    }
}
