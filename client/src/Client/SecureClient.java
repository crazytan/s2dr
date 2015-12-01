package Client;

import sun.lwawt.macosx.CImage;
import sun.security.util.SecurityConstants;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.security.*;
import java.sql.SQLSyntaxErrorException;

/*
 * A class representing the client side of s2dr service.
 */
public class SecureClient {

    private UID name;

    // 128-bit master key for generating key identifier
    private SecretKey masterKey;

    private PublicKey publicKey;

    private PrivateKey privateKey;

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

    private SecureClient() {}

    public SecureClient(String name) {

        this.name = new UID(name);

        // generate master key
        try {
            KeyGenerator gen = KeyGenerator.getInstance("AES");
            gen.init(128);
            masterKey = gen.generateKey();

            // TODO
            KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
            KeyPair keyPair = keyPairGen.generateKeyPair();
            publicKey = keyPair.getPublic();
            privateKey = keyPair.getPrivate();
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public UID getName() {
        return name;
    }

    public InsecureMessage init_session(String hostname) {
        return Channel.createChannel(name, hostname, masterKey, publicKey, privateKey);
    }

    public InsecureMessage check_out(UID document_id) {
        return Channel.send(name, "checkout", "{\"uid\":\"" + document_id + "\"}");
    }

    public InsecureMessage check_in(UID document_id, String document, SecurityFlag flag) {
        return Channel.send(name, "checkin", "{\"uid\":\"" + document_id + "\"," +
                                             "\"document\":\"" + document + "\"," +
                                             "\"flag\":" + flag + "}");
    }

    public InsecureMessage delegate(UID document_id, SecureClient c, int time,
                                    Permission p, boolean propagationFlag) {
        return Channel.send(name, "delegate", "{\"uid\":\"" + document_id + "\"," +
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

    private static void prompt() {
        System.out.print("> ");
    }

    private static void printHelp() {
        System.out.println("Operations: init, checkout, checkin, delegate, delete, terminate");
    }

    private UID generateUID(String uid) {
        return new UID(uid);
    }

    private static SecurityFlag generateFlag(String flag) {
        int _flag = Integer.parseInt(flag);
        if (_flag == 0) return SecurityFlag.none;
        if (_flag == 1) return SecurityFlag.confidentiality;
        if (_flag == 2) return SecurityFlag.integrity;
        return SecurityFlag.both;
    }

    private static Permission generatePermission(String p) {
        int _p = Integer.parseInt(p);
        if (_p == 0) return Permission.checkin;
        if (_p == 1) return Permission.checkout;
        if (_p == 2) return Permission.both;
        return Permission.owner;
    }

    public static void main(String[] args) throws IOException {
        System.out.println("*** a client for s2dr ***");
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        SecureClient.prompt();
        System.out.print("enter the client name: ");
        SecureClient client = new SecureClient(in.readLine().trim());
        while (true) {
            SecureClient.prompt();
            String[] commands = in.readLine().trim().split(" ");
            InsecureMessage message = null;
            if (commands[0].isEmpty()) continue;
            if (commands[0].equals("help")) SecureClient.printHelp();
            if (commands[0].equals("exit")) break;
            if (commands[0].equals("init"))
                message = client.init_session("server");
            if (commands[0].equals("checkout"))
                message = client.check_out(client.generateUID(commands[1]));
            if (commands[0].equals("checkin"))
                message = client.check_in(client.generateUID(commands[1]), commands[2], SecureClient.generateFlag(commands[3]));
            if (commands[0].equals("delegate"))
                message = client.delegate(client.generateUID(commands[1]),
                        commands[2].equals("all")?SecureClient.All:new SecureClient(commands[2]),
                        Integer.parseInt(commands[3]),
                        SecureClient.generatePermission(commands[4]),
                        commands[5].equals("true"));
            if (commands[0].equals("delete"))
                message = client.safe_delete(client.generateUID(commands[1]));
            if (commands[0].equals("terminate"))
                message = client.terminate();
            if (message.isSuccess())
                System.out.println("operation successful!");
            else
                System.out.println(message.getMessage());
        }
    }
}
