package s2dr.client;

import s2dr.ca.CA;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;

/*
 * A class representing the client side of s2dr service.
 */
public class SecureClient {

    private static final File workspace = new File("./workspace");

    private UID name;

    // 128-bit master key for generating key identifier
    private SecretKey masterKey;

    private PublicKey publicKey;

    private PrivateKey privateKey;

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

    private SecureClient() {}

    public SecureClient(String name) {

        this.name = new UID(name);

        // generate master key
        try {
            Path path = FileSystems.getDefault().getPath(workspace.getAbsolutePath() + "/" + name);
            if (Files.exists(path)) {
                try {
                    path = FileSystems.getDefault().getPath(workspace.getAbsolutePath() + "/" + name + "/" + name + ".master");
                    byte[] masterkeyByte = Files.readAllBytes(path);
                    masterKey = ClientCrypto.stringToAESKey(new String(masterkeyByte));

                    path = FileSystems.getDefault().getPath(workspace.getAbsolutePath() + "/" + name + "/" + name + ".pub");
                    byte[] publickeyByte = Files.readAllBytes(path);
                    publicKey = ClientCrypto.stringToPublicKey(new String(publickeyByte));

                    path = FileSystems.getDefault().getPath(workspace.getAbsolutePath() + "/" + name + "/" + name + ".key");
                    byte[] privatekeyByte = Files.readAllBytes(path);
                    privateKey = ClientCrypto.stringToPrivateKey(new String(privatekeyByte));

                    path = FileSystems.getDefault().getPath(workspace.getAbsolutePath() + "/" + name + "/" + name + ".crt");
                    byte[] certificateByte = Files.readAllBytes(path);
                    String certificate = new String(certificateByte);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                File dir = new File(workspace.getAbsolutePath() + "/" + name);
                dir.mkdir();

                KeyGenerator gen = KeyGenerator.getInstance("AES");
                gen.init(128);
                masterKey = gen.generateKey();

                KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
                KeyPair keyPair = keyPairGen.generateKeyPair();
                publicKey = keyPair.getPublic();
                privateKey = keyPair.getPrivate();

                String certificate = CA.createCertificate(name, ClientCrypto.privateKeyToString(privateKey));

                //save to disk
                try {
                    path = FileSystems.getDefault().getPath(workspace.getAbsolutePath() + "/" + name + "/" + name + ".master");
                    Files.write(path, ClientCrypto.aesKeyToString(masterKey).getBytes());

                    path = FileSystems.getDefault().getPath(workspace.getAbsolutePath() + "/" + name + "/" + name + ".pub");
                    Files.write(path, ClientCrypto.publicKeyToString(publicKey).getBytes());

                    path = FileSystems.getDefault().getPath(workspace.getAbsolutePath() + "/" + name + "/" + name + ".key");
                    Files.write(path, ClientCrypto.privateKeyToString(privateKey).getBytes());

                    path = FileSystems.getDefault().getPath(workspace.getAbsolutePath() + "/" + name + "/" + name + ".crt");
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
                                             "\"flag\":" + flag.ordinal() + "}");
    }

    public InsecureMessage delegate(UID document_id, SecureClient c, int time,
                                    Permission p, boolean propagationFlag) {
        return Channel.send(name, "delegate", "{\"uid\":\"" + document_id + "\"," +
                                              "\"client\":\"" + c.getName() + "\"," +
                                              "\"time\":" + time + "," +
                                              "\"permission\":" + p.ordinal() + "," +
                                              "\"flag\":" + (propagationFlag ? 1 : 0) + "}");
    }

    public InsecureMessage safe_delete(UID document_id) {
        return Channel.send(name, "delete", "{\"uid\":\"" + document_id + "\"}");
    }

    public InsecureMessage terminate() {
        return Channel.send(name, "terminate", "{}");
    }

    public UID generateUID(String uid) {
        return new UID(uid);
    }
}
