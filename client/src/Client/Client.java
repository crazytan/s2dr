package Client;

import com.oracle.javafx.jmx.json.JSONDocument;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;

/*
 * A class representing the client side of s2dr service.
 */
public class Client {

    private UID name;

    // 128-bit master key for generating key identifier
    private SecretKey masterKey;

    private Client() {}

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
    }

    public JSONDocument init_session(String hostname) {
        return Channel.createChannel(name, hostname, masterKey);
    }

    public JSONDocument check_out(UID document_id) {
        return Channel.send(name, "checkout", "{\"uid\":\"" + document_id + "\"}");
    }

    public JSONDocument check_in(UID document_id, String document, SecurityFlag flag) {
        return Channel.send(name, "checkin", "{\"uid\":\"" + document_id + "\"}\"," +
                                             "\"document\":\"" + document + "\"," +
                                             "\"flag\":" + flag + "}");
    }

    public JSONDocument delegate(UID document_id, Client c, int time,
                         Permission p, boolean propagationFlag) {
        return Channel.send(name, "delegate", "{\"uid\":\"" + document_id + "\"}\"," +
                                              "\"client\":\"" + c.getName() + "\"," +
                                              "\"time\":" + time + "," +
                                              "\"permission\":" + p + "," +
                                              "\"flag\":" + (propagationFlag ? 1 : 0) + "}");
    }

    public JSONDocument safe_delete(UID document_id) {
        return Channel.send(name, "delete", "{\"uid\":\"" + document_id + "\"}");
    }

    public JSONDocument terminate() {
        return Channel.send(name, "terminate", "");
    }

    public static Client All = new Client() {
        @Override
        public UID getName() {
            return new UID("all");
        }
    };

    public enum Permission {
        checkin, checkout, both, owner
    }

    public enum SecurityFlag {
        none, confidentiality, integrity, both
    }

    public final class UID {
        public String id;

        public UID(String id) {
            this.id = id;
        }

        public String toString() {
            return id;
        }
    }

    public static void main(String[] args) {
        Client c = new Client("tan");
        System.out.println(Client.All.getName());
    }
}
