package Client;

import com.google.gson.Gson;

import javax.crypto.SecretKey;

/**
 * A wrapper for JSONDocument class
 */
public class Message {

    private int result;

    private String message;

    private Message(int result, String message) {
        this.result = result;
        this.message = message;
    }

    public boolean isSuccess() {
        return result == 0;
    }

    public String getMessage() {
        return message;
    }

    public String toString() {
        return gson.toJson(this);
    }

    private static Gson gson = new Gson();

    public static Message newMessage(String m) {
        return gson.fromJson(m, Message.class);
    }

    public static Message successMessage() {
        return new Message(0, "");
    }

    public static Message errorMessage(String message) {
        return new Message(1, message);
    }

    public static void main(String[] args) {
        Message m = Message.newMessage("{\"result\":0,\"message\":\"hello world\"}");
        System.out.println(m);
        System.out.println(m.isSuccess());
        System.out.println(m.getMessage());
    }
}
