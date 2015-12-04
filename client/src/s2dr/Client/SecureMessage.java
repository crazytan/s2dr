package s2dr.client;

import com.google.gson.Gson;

/**
 * A class representing secure message exchanged between channel and insecure client
 */
public final class SecureMessage {

    private final int result;

    private final String message;

    private final String identifier;

    private static Gson gson = new Gson();

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

    public static SecureMessage newMessage(String m) {
        return gson.fromJson(m, SecureMessage.class);
    }

    public static SecureMessage errorMessage(String message) {
        return new SecureMessage(1, message);
    }

    public static void main(String... args) {
        SecureMessage m = SecureMessage.errorMessage("123");
        System.out.println(m);
    }
}
