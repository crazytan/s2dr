package Client;

import com.oracle.javafx.jmx.json.JSONDocument;

/**
 * A wrapper for JSONDocument class
 */
public class Message extends JSONDocument {

    private Message(JSONDocument.Type type) {
        super(type);
    }

    public static Message newMessage() {
        return new Message(Type.OBJECT);
    }

    public static Message newMessage(String m) {
        // TODO: use Gson
        return null;
    }
    
    public static Message errorMessage(String message) {
        Message m = new Message(Type.OBJECT);
        m.setNumber("result", 1);
        m.setString("message", message);
        return m;
    }
}
