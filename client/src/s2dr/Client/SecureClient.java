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
        System.out.println("Operations:        init, checkout, checkin, delegate, delete, terminate\n" +
                "-init:             init\n" +
                "-checkout:         checkout srcfile dstfile\n" +
                "-checkin:          checkin dstfile srcfile flags\n" +
                "-delegate:         delegate file client duration permissionFlags propagationFlags\n" +
                "-delete:           delete file\n" +
                "-terminate:        terminate\n" +
                "-exit:             exit\n\n" +
                "-checkinflags:     0/1/2/3 ==> none/confidentiality/integrity/both\n" +
                "-permissionflags:  0/1/2/3 ==> checkin/checkout/both/owner\n" +
                "-propagationflags: true/false");

    }

    public UID generateUID(String uid) {
        return new UID(uid);
    }

    private static SecurityFlag generateFlag(int flag) {
        int _flag = flag;//Integer.parseInt(flag);
        if (_flag == 0) {
            return SecurityFlag.none;
        }
        if (_flag == 1) {
            return SecurityFlag.confidentiality;
        }
        if (_flag == 2) {
            return SecurityFlag.integrity;
        }
        return SecurityFlag.both;
    }

    private static Permission generatePermission(int p) {
        int _p = p;//Integer.parseInt(p);
        if (_p == 0) {
            return Permission.checkin;
        }
        if (_p == 1) {
            return Permission.checkout;
        }
        if (_p == 2) {
            return Permission.both;
        }
        return Permission.owner;
    }

    private String readFromFile(String fileName) {
        String data = "";
        try {
            Path path = FileSystems.getDefault().getPath("workspace/" + name.id + "/" + fileName);
            data = new String(Files.readAllBytes(path));
            return data;
        }
        catch (Exception e){
            System.out.println("file " + fileName + " not found! Check-in empty content.");
        }
        return "";
    }

    private void saveToFile(String fileName, String data) {
        try {
            Path path = FileSystems.getDefault().getPath("workspace/" + name.id + "/" + fileName);
            Files.write(path, data.getBytes());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String... args) throws IOException {
        System.out.println("*** a client for s2dr ***");
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        SecureClient.prompt();
        System.out.print("enter the client name: ");
        SecureClient client = new SecureClient(in.readLine().trim());
        while (true) {
            SecureClient.prompt();
            String[] commands = in.readLine().trim().split(" ");
            InsecureMessage message = null;
            if (commands[0].isEmpty()) {
                continue;
            }
            if (commands[0].equals("help")) {
                SecureClient.printHelp();
                continue;
            }
            if (commands[0].equals("exit")) {
                client.terminate();
                break;
            }
            if (commands[0].equals("init")) {
                message = client.init_session("server");
            }
            if (commands[0].equals("checkout")) {
                if (commands.length < 2) {
                    System.out.println("command not valid! See 'help'");
                    continue;
                }
                message = client.check_out(client.generateUID(commands[1]));
                if (message.isSuccess()) {
                    client.saveToFile(commands[2], message.getMessage());
                }
            }
            if (commands[0].equals("checkin")) {
                if (commands.length < 3) {
                    System.out.println("command not valid! See 'help'");
                    continue;
                }
                int flag = 3;
                try {
                    flag = Integer.parseInt(commands[3]);
                }catch (NumberFormatException e) {
                    System.out.println("command not valid! See 'help'");
                }
                String data = client.readFromFile(commands[2]);
                message = client.check_in(client.generateUID(commands[1]), data, SecureClient.generateFlag(flag));
            }
            if (commands[0].equals("delegate")) {
                if (commands.length < 4) {
                    System.out.println("command not valid! See 'help'");
                    continue;
                }
                int time = 0;
                int permissionFlag = -1;
                try {
                    time = Integer.parseInt(commands[3]);
                    permissionFlag = Integer.parseInt(commands[4]);
                }
                catch (NumberFormatException e) {
                    System.out.println("command not valid! See 'help'");
                    continue;
                }
                message = client.delegate(client.generateUID(commands[1]),
                        commands[2].equals("all") ? SecureClient.All : new SecureClient(commands[2]),
                        time,
                        SecureClient.generatePermission(permissionFlag),
                        commands[5].equals("true"));
            }
            if (commands[0].equals("delete")) {
                if (commands.length < 2) {
                    System.out.println("command not valid! See 'help'");
                }
                message = client.safe_delete(client.generateUID(commands[1]));
            }
            if (commands[0].equals("terminate")) {
                message = client.terminate();
            }
            if (message == null) {
                System.out.println("command not valid! See 'help'");
                continue;
            }
            if (message.isSuccess()) {
                System.out.println("operation successful!");
            }
            else {
                System.out.println(message.getMessage());
            }
        }
    }
}
