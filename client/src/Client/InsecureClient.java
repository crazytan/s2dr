package Client;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/*
 * A wrapper for HTTPClient
 */
public class InsecureClient {

    // server URL
    private final String serverUrl;

    public InsecureClient(String host) {
        this.serverUrl = host;
    }

    public Message send(String route, String message) {
        HttpURLConnection connection = null;
        try {
            // open an url connection
            URL url = new URL(serverUrl + "/" + route);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setChunkedStreamingMode(0);

            // write the message
            OutputStream out = new BufferedOutputStream(connection.getOutputStream());
            out.write(message.getBytes("US-ASCII"));

            // get the response
            // TODO: make sure the stream is exhausted
            InputStream in = new BufferedInputStream(connection.getInputStream());
            byte[] arr = new byte[in.available()];
            in.read(arr, 0, in.available());
            String response = new String(arr, "US-ASCII");
            return Message.newMessage(response);
        }
        catch (MalformedURLException e) {
            e.printStackTrace();
            return Message.errorMessage("invalid URL!");
        }
        catch (IOException e) {
            e.printStackTrace();
            return Message.errorMessage("can't establish connection!");
        }
        finally {
            if (connection != null) connection.disconnect();
        }
    }
}
