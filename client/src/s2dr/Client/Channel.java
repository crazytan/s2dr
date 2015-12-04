package s2dr.client;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.util.*;

import s2dr.ca.CA;
import s2dr.client.SecureClient.UID;
import com.google.gson.Gson;

/**
 * A secure channel used by client
 */
public final class Channel {

    private final SecretKey key;

    private final String clientIdentifier;

    private final String serverIdentifier;

    private final InsecureClient _client;

    private static Map<UID, Channel> channels = new HashMap<>();

    private Channel(SecretKey key, String clientIdentifier, String serverIdentifier, InsecureClient _client) {
        this.key = key;
        this.clientIdentifier = clientIdentifier;
        this.serverIdentifier = serverIdentifier;
        this._client = _client;
    }

    private SecureMessage send(String route, String message) {
        try {
            String sMessage = ClientCrypto.toHexString(ClientCrypto.AESEncrypt(message.getBytes(), key));
            String response = _client.send(route,
                    "{\"identifier\":\"" + clientIdentifier + "\"," +
                    "\"message\":\"" + sMessage + "\"}");
            return SecureMessage.newMessage(response);
        }
        catch (Exception e) {
            return SecureMessage.errorMessage(e.getMessage());
        }
    }

    private static PublicKey verifyCertAndExtractPublicKey(String certificate) {
        if (!CA.validateCertificate(certificate)) {
            return null;
        }
        try {
            String publicKeyStr = CA.extractPublicKeyFromCertificate(certificate);
            return ClientCrypto.stringToPublicKey(publicKeyStr);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String dealNewLine(String string) {
        return string.replace("\n", "\\n");
    }

    public static InsecureMessage createChannel(UID clientName, String hostname, SecretKey masterKey, PublicKey publicKey, PrivateKey privateKey) {
        InsecureClient _client = new InsecureClient(discover(hostname));

        //Load Certificate
        String certificate = "";
        try{
            Path path = FileSystems.getDefault().getPath(System.getenv("workspace") + "/" + clientName.id + "/certificate");
            byte[] crtByte = Files.readAllBytes(path);
            certificate = new String(crtByte);
        }
        catch (Exception e) {
            return InsecureMessage.errorMessage("Certificate not found!");
        }

        //Phase 1
        String sMessage1 = ClientCrypto.publicKeyToString(publicKey);
        byte[] sMessageByte1 = ClientCrypto.doSHA256(sMessage1.getBytes());
        String signature1 = ClientCrypto.toHexString(ClientCrypto.Sign(sMessageByte1, privateKey));
        String response1 = _client.send("init", "{\"phase\":1,\"message\":\"" + dealNewLine(sMessage1) + "\"," +
                "\"signature\":\"" + signature1 + "\"," + "\"certificate\":\"" + dealNewLine(certificate) +"\"}");

        Gson gson = new Gson();

        Map<String, String> map = new HashMap<String, String>();
        Map<String, String> map1 = gson.fromJson(response1, map.getClass());
        String rst1 = map1.get("result");
        if (!"0".equals(rst1)) {
            return InsecureMessage.errorMessage(map1.get("message"));
        }
        String rMessage1 = map1.get("message");
        byte[] rMessageByte1 = ClientCrypto.toByte(rMessage1);

        String rCrt1 = map1.get("certificate");
        PublicKey serverPublicKey = verifyCertAndExtractPublicKey(rCrt1);
        if (serverPublicKey == null) {
            return InsecureMessage.errorMessage("Invalid Certificate in phase 1!");
        }

        //Phase 2
        String sMessage2 = null;
        String signature2 = null;
        SecretKey clientKey = ClientCrypto.GenerateAESKey(128);
        try {
            byte[] clientKeyByte = clientKey.getEncoded();
            byte[] crypto = ClientCrypto.RSAEncrypt(clientKeyByte, serverPublicKey);
            sMessage2 = ClientCrypto.toHexString(crypto);
            byte[] sMessageByte2 = ClientCrypto.doSHA256(sMessage2.getBytes());
            signature2 = ClientCrypto.toHexString(ClientCrypto.Sign(sMessageByte2, privateKey));
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        String response2 = _client.send("init", "{\"phase\":2,\"message\":\"" + dealNewLine(sMessage2) + "\"," +
                "\"signature\":\"" + signature2 + "\"," + "\"certificate\":\"" + dealNewLine(certificate) +"\"}");

        Map<String, String> map2 = gson.fromJson(response2, map.getClass());
        String rst2 = map2.get("result");
        if (!"0".equals(rst2)) {
            return InsecureMessage.errorMessage(map2.get("message"));
        }
        String rMessage2 = map2.get("message");
        String rSign2 = map2.get("signature");
        String rCrt2 = map2.get("certificate");
        byte[] rMessageByte2 = ClientCrypto.RSADecrypt(ClientCrypto.toByte(rMessage2), privateKey);
        byte[] rSignByte2 = ClientCrypto.toByte(rSign2);
        serverPublicKey = verifyCertAndExtractPublicKey(rCrt2);
        if (serverPublicKey == null) {
            return InsecureMessage.errorMessage("Certificate invalid in phase 2!");
        }

        if (!Arrays.equals(ClientCrypto.doSHA256(rMessage2.getBytes()),ClientCrypto.RSADecrypt(rSignByte2, serverPublicKey))) {
            return InsecureMessage.errorMessage("phase2 signature not match!");
        }

        byte[] xorKeys = new byte[16];
        byte[] clientKeyByte = clientKey.getEncoded();
        for (int i = 0; i < 16; ++i) {
            xorKeys[i] = (byte)(clientKeyByte[i] ^ rMessageByte2[i]);
        }

        //Phase 3
        byte[] sharedKey = null;
        byte[] identifier = null;
        String sMessage3 = "";
        String signature3 = "";
        try {
            sharedKey = ClientCrypto.doSHA256(xorKeys);
            identifier = ClientCrypto.AESEncrypt(sharedKey, masterKey);
            sMessage3 = ClientCrypto.toHexString(identifier);
            byte[] sMessageByte3 = ClientCrypto.doSHA256(sMessage3.getBytes());
            signature3 = ClientCrypto.toHexString(ClientCrypto.Sign(sMessageByte3, privateKey));
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        String response3 = _client.send("init", "{\"phase\":3,\"message\":\"" + dealNewLine(sMessage3) + "\"," +
                "\"signature\":\"" + signature3 + "\"," + "\"certificate\":\"" + dealNewLine(certificate) +"\"}");

        Map<String, String> map3 = gson.fromJson(response3, map.getClass());
        String rst3 = map3.get("result");
        if (!"0".equals(rst3)) {
            return InsecureMessage.errorMessage(map3.get("message"));
        }
        String rMessage3 = map3.get("message");
        String rSign3 = map3.get("signature");
        String rCrt3 = map3.get("certificate");
        byte[] rSignByte3 = ClientCrypto.toByte(rSign3);

        serverPublicKey = verifyCertAndExtractPublicKey(rCrt3);
        if (serverPublicKey == null) {
            return InsecureMessage.errorMessage("Certificate invalid in phase 3!");
        }

        if (!Arrays.equals(ClientCrypto.doSHA256(rMessage3.getBytes()),ClientCrypto.RSADecrypt(rSignByte3, serverPublicKey))) {
            return InsecureMessage.errorMessage("phase3 signature not match!");
        }

        SecretKey key = new SecretKeySpec(sharedKey, 0, 16, "AES"); // Force key size to 128

        channels.put(clientName, new Channel(key, ClientCrypto.toHexString(identifier), rMessage3, _client));

        return InsecureMessage.successMessage();
    }

    private static String discover(String hostname) {
        return "http://localhost:8888";
    }

    public static InsecureMessage send(UID clientName, String route, String message) {
        if (channels.containsKey(clientName)) {
            Channel channel = channels.get(clientName);
            SecureMessage m = channel.send(route, message);
            if (!m.isSuccess()) {
                return InsecureMessage.errorMessage(m.getMessage());
            }
            if (!m.getIdentifier().equals(channel.serverIdentifier)) {
                return InsecureMessage.errorMessage("unrecognized identifier!");
            }
            String rMessage = new String(ClientCrypto.AESDecrypt(ClientCrypto.toByte(m.getMessage()), channel.key));
            return InsecureMessage.newMessage(rMessage);
        }
        return InsecureMessage.errorMessage("client name not found!");
    }
}
