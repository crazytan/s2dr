package Test;

import Client.ClientCrypto;
import Client.InsecureMessage;
import Client.SecureClient;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

/**
 * Test script
 */
public class Test {

    private static final File workspace = new File(System.getenv("workspace"));

    private static SecureClient client0;

    private static SecureClient client1;

    private static InsecureMessage getSuccessMessage(InsecureMessage message) {
        if (!message.isSuccess()) {
            System.out.println("failed! Message: " + message.getMessage());
            System.exit(1);
        }
        System.out.println("done!");
        return message;
    }

    private static InsecureMessage getErrorMessage(InsecureMessage message) {
        if (message.isSuccess()) {
            System.out.println("failed!");
            System.exit(1);
        }
        System.out.println(message.getMessage());
        return message;
    }

    private static void initialize() {
        System.out.print("Creating client_0...");
        client0 = new SecureClient("client_0");
        System.out.println("done!");

        System.out.print("Creating client_1...");
        client1 = new SecureClient("client_1");
        System.out.println("done!");
    }

    public static void main(String[] args) throws Exception {
        initialize();

        System.out.print("Client0: initializing session with server...");
        getSuccessMessage(client0.init_session("server"));

        System.out.print("Client0: checking in 0.txt...");
        String text = "hello world";
        getSuccessMessage(client0.check_in(client0.generateUID("0.txt"), text, SecureClient.SecurityFlag.integrity));

        System.out.println("Checking signature...");
        String signature = ClientCrypto.toHexString(ClientCrypto.doSHA256(text.getBytes()));
        System.out.println("Client signature: " + signature);
        String dbSignature = Mongo.getSignatureByUID("0.txt");
        System.out.println("Server signature: " + dbSignature);
        if (!signature.equals(dbSignature)) {
            System.out.println("failed!");
            System.exit(1);
        }
        System.out.println("done!");

        System.out.print("Client0: checking out 0.txt...");
        text = getSuccessMessage(client0.check_out(client0.generateUID("0.txt"))).getMessage();
        File copy = new File(workspace.getAbsolutePath() + "/client_0/0_copy.txt");
        BufferedWriter writer = new BufferedWriter(new FileWriter(copy));
        writer.write(text);
        writer.close();

        System.out.print("Client1: initializing session with server...");
        getSuccessMessage(client1.init_session("server"));

        System.out.print("Client1: checking out 0.txt...");
        getErrorMessage(client1.check_out(client1.generateUID("0.txt")));

        System.out.print("Client0: safe deleting 0.txt...");
        getSuccessMessage(client0.safe_delete(client0.generateUID("0.txt")));

        System.out.print("Client0: checking out 0.txt...");
        getErrorMessage(client0.check_out(client0.generateUID("0.txt")));
    }
}
