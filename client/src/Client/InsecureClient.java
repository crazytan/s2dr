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

    public String send(String route, String message) {
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
            byte[] response = new byte[in.available()];
            in.read(response, 0, in.available());
            return new String(response, "US-ASCII");
        }
        catch (MalformedURLException e) {
            e.printStackTrace();
            return SecureMessage.errorMessage("invalid URL!").toString();
        }
        catch (IOException e) {
            e.printStackTrace();
            return SecureMessage.errorMessage("can't establish connection!").toString();
        }
        finally {
            if (connection != null) connection.disconnect();
        }
    }
}
