package Client;

import CA.CA;
import sun.lwawt.macosx.CImage;
import sun.security.util.SecurityConstants;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.*;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
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

    private String certificate;

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
            Path path = FileSystems.getDefault().getPath(System.getenv("workspace") + name);
            if (Files.exists(path)) {
                try {
                    path = FileSystems.getDefault().getPath(System.getenv("workspace") + name + "/key.master");
                    byte[] masterkeyByte = Files.readAllBytes(path);
                    masterKey = ClientCrypto.stringToAESKey(new String(masterkeyByte));

                    path = FileSystems.getDefault().getPath(System.getenv("workspace") + name + "/key.public");
                    byte[] publickeyByte = Files.readAllBytes(path);
                    publicKey = ClientCrypto.stringToPublicKey(new String(publickeyByte));

                    path = FileSystems.getDefault().getPath(System.getenv("workspace") + name + "/key.private");
                    byte[] privatekeyByte = Files.readAllBytes(path);
                    privateKey = ClientCrypto.stringToPrivateKey(new String(privatekeyByte));

                    path = FileSystems.getDefault().getPath(System.getenv("workspace") + name + "/certificate");
                    byte[] certificateByte = Files.readAllBytes(path);
                    certificate = new String(certificateByte);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }else {
                File dir = new File(System.getenv("workspace") + name);
                dir.mkdir();KeyGenerator gen = KeyGenerator.getInstance("AES");
                gen.init(256);
                masterKey = gen.generateKey();

                KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
                KeyPair keyPair = keyPairGen.generateKeyPair();
                publicKey = keyPair.getPublic();
                privateKey = keyPair.getPrivate();

                certificate = CA.createCertificate(name, ClientCrypto.privateKeyToString(privateKey));

                //save to disk
                try {
                    path = FileSystems.getDefault().getPath(System.getenv("workspace") + name + "/key.master");
                    Files.write(path, ClientCrypto.aesKeyToString(masterKey).getBytes());
                    path = FileSystems.getDefault().getPath(System.getenv("workspace") + name + "/key.public");
                    Files.write(path, ClientCrypto.publicKeyToString(publicKey).getBytes());
                    path = FileSystems.getDefault().getPath(System.getenv("workspace") + name + "/key.private");
                    Files.write(path, ClientCrypto.privateKeyToString(privateKey).getBytes());
                    path = FileSystems.getDefault().getPath(System.getenv("workspace") + name + "/certificate");
                    Files.write(path, certificate.getBytes());
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
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
