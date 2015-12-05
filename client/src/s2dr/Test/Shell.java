package s2dr.test;

import s2dr.client.InsecureMessage;
import s2dr.client.SecureClient;
import s2dr.client.SecureClient.Permission;
import s2dr.client.SecureClient.SecurityFlag;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

public final class Shell {

    private Shell() {}

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

    private static String readFromFile(String username,String fileName) {
        String data = "";
        try {
            Path path = FileSystems.getDefault().getPath("workspace/" + username + "/" + fileName);
            data = new String(Files.readAllBytes(path));
            return data;
        }
        catch (Exception e){
            System.out.println("file " + fileName + " not found! Check-in empty content.");
        }
        return "";
    }

    private static void saveToFile(String username,String fileName, String data) {
        try {
            Path path = FileSystems.getDefault().getPath("workspace/" + username + "/" + fileName);
            Files.write(path, data.getBytes());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void operate(String ...arg) throws IOException {
        System.out.println("*** a client for s2dr ***");
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        prompt();
        System.out.print("enter the client name: ");
        SecureClient client = new SecureClient(in.readLine().trim());
        while (true) {
            prompt();
            String[] commands = in.readLine().trim().split(" ");
            InsecureMessage message = null;
            if (commands[0].isEmpty()) {
                continue;
            }
            if (commands[0].equals("help")) {
                printHelp();
                continue;
            }
            if (commands[0].equals("exit")) {
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
                    saveToFile(client.getName().id, commands[2], message.getMessage());
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
                String data = readFromFile(client.getName().id, commands[2]);
                message = client.check_in(client.generateUID(commands[1]), data, generateFlag(flag));
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
                        generatePermission(permissionFlag),
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

    public static void main(String... args) throws IOException{
        Shell.operate(args);
    }
}
