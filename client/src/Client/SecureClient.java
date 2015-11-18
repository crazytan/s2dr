package Client;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;

/*
 * A class representing the client side of s2dr service.
 */
public class SecureClient {

    private UID name;

    // 128-bit master key for generating key identifier
    private SecretKey masterKey;

    private SecureClient() {}

    public UID getName() {
        return name;
    }

    public SecureClient(String name) {

        this.name = new UID(name);

        // generate master key
        try {
            KeyGenerator gen = KeyGenerator.getInstance("AES");
            gen.init(128);
            masterKey = gen.generateKey();
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public InsecureMessage init_session(String hostname) {
        return Channel.createChannel(name, hostname, masterKey);
    }

    public InsecureMessage check_out(UID document_id) {
        return Channel.send(name, "checkout", "{\"uid\":\"" + document_id + "\"}");
    }

    public InsecureMessage check_in(UID document_id, String document, SecurityFlag flag) {
        return Channel.send(name, "checkin", "{\"uid\":\"" + document_id + "\"}\"," +
                                             "\"document\":\"" + document + "\"," +
                                             "\"flag\":" + flag + "}");
    }

    public InsecureMessage delegate(UID document_id, SecureClient c, int time,
                                    Permission p, boolean propagationFlag) {
        return Channel.send(name, "delegate", "{\"uid\":\"" + document_id + "\"}\"," +
                                              "\"client\":\"" + c.getName() + "\"," +
                                              "\"time\":" + time + "," +
                                              "\"permission\":" + p + "," +
                                              "\"flag\":" + (propagationFlag ? 1 : 0) + "}");
    }

    public InsecureMessage safe_delete(UID document_id) {
        return Channel.send(name, "delete", "{\"uid\":\"" + document_id + "\"}");
    }

    public InsecureMessage terminate() {
        return Channel.send(name, "terminate", "");
    }

    public static SecureClient All = new SecureClient() {
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
        SecureClient c = new SecureClient("tan");
        System.out.println(SecureClient.All.getName());
    }
}
