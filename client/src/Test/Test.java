package Test;

import Client.ClientCrypto;
import Client.InsecureMessage;
import Client.SecureClient;
import Client.SecureClient.SecurityFlag;
import Client.SecureClient.Permission;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Test script
 */
public class Test {

    private static final File workspace = new File(System.getenv("workspace"));

    private static SecureClient client0;

    private static SecureClient client1;

    private static SecureClient client2;

    private static void saveFile(String client, String fileName, String text) throws IOException {
        File file = new File(workspace.getAbsolutePath() + "/" + client + "/" + fileName);
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write(text);
        writer.close();
    }

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
            System.out.println("test failed!");
            System.exit(1);
        }
        System.out.println(message.getMessage());
        return message;
    }

    private static void Sleep(int numSeconds) throws Exception {
        System.out.print("sleep " + numSeconds + " seconds");
        for (int i = 0;i < numSeconds;i++) {
            Thread.sleep(1000);
            System.out.print('.');
        }
        System.out.println();
    }

    private static void initialize() {
        System.out.print("Creating client_0...");
        client0 = new SecureClient("client_0");
        System.out.println("done!");

        System.out.print("Creating client_1...");
        client1 = new SecureClient("client_1");
        System.out.println("done!");

        System.out.print("Creating client_2...");
        client2 = new SecureClient("client_2");
        System.out.println("done!");
    }

    public static void main(String[] args) throws Exception {
        initialize();

        System.out.print("Client0: initializing session with server...");
        getSuccessMessage(client0.init_session("server"));

        System.out.print("Client0: checking in 0.txt...");
        String text = "hello world";
        getSuccessMessage(client0.check_in(client0.generateUID("0.txt"), text, SecurityFlag.integrity));

        /*System.out.println("Checking signature...");
        String signature = ClientCrypto.toHexString(ClientCrypto.doSHA256(text.getBytes()));
        System.out.println("Client signature: " + signature);
        String dbSignature = Mongo.getSignatureByUID("0.txt");
        System.out.println("Server signature: " + dbSignature);
        if (!signature.equals(dbSignature)) {
            System.out.println("failed!");
            System.exit(1);
        }
        System.out.println("done!");*/

        System.out.print("Client0: checking out 0.txt...");
        text = getSuccessMessage(client0.check_out(client0.generateUID("0.txt"))).getMessage();
        System.out.println("message: " + text);
        saveFile("client_0", "0_copy.txt", text);

        System.out.print("Client1: initializing session with server...");
        getSuccessMessage(client1.init_session("server"));

        System.out.print("Client1: checking out 0.txt...");
        getErrorMessage(client1.check_out(client1.generateUID("0.txt")));

        System.out.print("Client0: safe deleting 0.txt...");
        getSuccessMessage(client0.safe_delete(client0.generateUID("0.txt")));

        System.out.print("Client0: checking out 0.txt...");
        getErrorMessage(client0.check_out(client0.generateUID("0.txt")));

        System.out.print("Client0: checking in 1.txt...");
        getSuccessMessage(client0.check_in(client0.generateUID("1.txt"), text, SecurityFlag.confidentiality));

        System.out.print("Client0: checking out 1.txt...");
        text = getSuccessMessage(client0.check_out(client0.generateUID("1.txt"))).getMessage();
        System.out.println("message: " + text);
        saveFile("client_0", "1_copy.txt", text);

        System.out.print("Client0: terminating session...");
        getSuccessMessage(client0.terminate());

        System.out.print("Client0: initializing session with server...");
        getSuccessMessage(client0.init_session("server"));

        System.out.print("Client0: checking in 1.txt...");
        getSuccessMessage(client0.check_in(client0.generateUID("1.txt"), text, SecurityFlag.both));

        System.out.print("Client0: checking out 1.txt...");
        text = getSuccessMessage(client0.check_out(client0.generateUID("1.txt"))).getMessage();
        System.out.println("message: " + text);
        saveFile("client_0", "1_copy2.txt", text);

        System.out.print("Client0: delegating to client1...");
        getSuccessMessage(client0.delegate(client0.generateUID("1.txt"), client1, 30, Permission.both, false));

        System.out.print("Client1: checking out 1.txt...");
        text = getSuccessMessage(client1.check_out(client1.generateUID("1.txt"))).getMessage();
        System.out.println("message: " + text);
        saveFile("client_1", "1_copy.txt", text);

        System.out.print("Client1: checking in 1.txt...");
        getSuccessMessage(client1.check_in(client1.generateUID("1.txt"), "all work and no play makes Jack a dull boy", SecurityFlag.both));

        System.out.print("Client1: delegating to client2...");
        getErrorMessage(client1.delegate(client1.generateUID("1.txt"), client2, 30, Permission.both, false));

        System.out.print("Client2: initializing session with server...");
        getSuccessMessage(client2.init_session("server"));

        System.out.print("Client2: checking out 1.txt...");
        getErrorMessage(client2.check_out(client2.generateUID("1.txt")));

        Sleep(30);
        System.out.print("Client1: checking out 1.txt...");
        getErrorMessage(client1.check_out(client1.generateUID("1.txt")));

        System.out.print("Client0: checking out 1.txt...");
        getSuccessMessage(client0.check_out(client0.generateUID("1.txt")));

        System.out.print("Client0: delegating to client1...");
        getSuccessMessage(client0.delegate(client0.generateUID("1.txt"), client1, 30, Permission.checkout, true));

        System.out.print("Client1: delegating to client2...");
        getSuccessMessage(client1.delegate(client1.generateUID("1.txt"), client2, 60, Permission.checkout, false));

        System.out.print("Client2: checking out 1.txt...");
        text = getSuccessMessage(client2.check_out(client2.generateUID("1.txt"))).getMessage();
        System.out.println("message: " + text);
        saveFile("client_2", "1_copy2.txt", text);

        Sleep(30);
        System.out.print("Client2: checking out 1.txt...");
        getErrorMessage(client2.check_out(client2.generateUID("1.txt")));

        Sleep(30);
        System.out.print("Client2: checking out 1.txt...");
        getErrorMessage(client2.check_out(client2.generateUID("1.txt")));

        client0.terminate();
        client1.terminate();
        client2.terminate();
        System.out.println("test complete!");
    }
}
