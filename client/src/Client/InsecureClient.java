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
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setChunkedStreamingMode(0);

            // write the message
            OutputStream out = new BufferedOutputStream(connection.getOutputStream());
            out.write(message.getBytes("US-ASCII"));
            out.flush();
            out.close();

            // get the response
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

    public static void main(String[] args) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL("http://localhost:8888/init");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setChunkedStreamingMode(0);

            OutputStream out = new BufferedOutputStream(connection.getOutputStream());
            out.write("{\"status\":1}".getBytes("US-ASCII"));
            out.flush();
            out.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (connection != null) connection.disconnect();
        }
    }
}
