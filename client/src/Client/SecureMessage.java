package Client;

import com.google.gson.Gson;

/**
 * A class representing secure message exchanged between channel and insecure client
 */
public class SecureMessage extends InsecureMessage {

    private String identifier;

    private SecureMessage(int result, String message) {
        super(result, message);
        identifier = "";
    }

    public static SecureMessage errorMessage(String message) {
        return new SecureMessage(1, message);
    }
}
