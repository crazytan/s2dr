package s2dr.client;

import com.google.gson.Gson;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.util.HashMap;
import java.util.Map;

/**
 * A class representing insecure message exchanged between client and channel
 */
public final class InsecureMessage {

    private final int result;

    private final String message;

    private static Gson gson = new Gson();

    private InsecureMessage(int result, String message) {
        this.result = result;
        this.message = message;
    }

    public boolean isSuccess() {
        return result == 0;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return gson.toJson(this);
    }

    public static InsecureMessage newMessage(String m) {
        return gson.fromJson(m, InsecureMessage.class);
    }

    public static InsecureMessage successMessage() {
        return new InsecureMessage(0, "");
    }

    public static InsecureMessage errorMessage(String message) {
        return new InsecureMessage(1, message);
    }

    public static void main(String... args) {
/*        Message m = Message.newMessage("{\"result\":0,\"message\":\"hello world\"}");
        System.out.println(m);
        System.out.println(m.isSuccess());
        System.out.println(m.getMessage());*/
        try {
            KeyGenerator keygen = KeyGenerator.getInstance("AES");
            keygen.init(128);
            SecretKey key = keygen.generateKey();
            Class<?> t;
            t = key.getClass();
            Cipher cipher = Cipher.getInstance("AES");
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        Map<String, String> map = new HashMap<String, String>();
        map.put("signature", "123");
        map.put("certificate", "{\"signature\":\"456\"}");
        System.out.println(gson.toJson(map));
        //Map<String, String> _map = gson.fromJson(gson.toJson(map), new HashMap<String, String>().getClass());
        Map<String, String> _map = gson.fromJson(gson.toJson(map), HashMap.class);
        System.out.println(_map);
    }
}
