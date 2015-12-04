package s2dr.Client;

import com.google.gson.Gson;

/**
 * A class representing secure message exchanged between channel and insecure client
 */
public class SecureMessage {

    private int result;

    private String message;

    private String identifier;

    private SecureMessage(int result, String message) {
        this.result = result;
        this.message = message;
        identifier = "";
    }

    public boolean isSuccess() {
        return result == 0;
    }

    public String getMessage() {
        return message;
    }

    public String getIdentifier() {
        return identifier;
    }

    private static Gson gson = new Gson();

    public static SecureMessage newMessage(String m) {
        return gson.fromJson(m, SecureMessage.class);
    }

    public static SecureMessage errorMessage(String message) {
        return new SecureMessage(1, message);
    }

    public static void main(String[] args) {
        SecureMessage m = SecureMessage.errorMessage("123");
        System.out.println(m);
    }
}
