package CA;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A class representing Certificate Authority
 */
public class CA {

    private static final File root = new File(System.getenv("root"));

    public static byte[] extractPublicKeyFromCertificate(String certificate) {
        return null;
    }

    public static boolean validateCertificate(String certificate) {
        return false;
    }

    public static String createCertificate(String name, String privateKey) {
        File tmpKey = null;
        try {
            tmpKey = File.createTempFile("tmp", "key", root);
            BufferedWriter writer = new BufferedWriter(new FileWriter(tmpKey));
            writer.write(privateKey);
            writer.close();

            ProcessBuilder pb = new ProcessBuilder("openssl", "req", "-new",
                    "-key", tmpKey.getName(),
                    "-subj", "/CN=" + name);
            pb.directory(root);
            Process ps = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(ps.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                sb.append('\n');
            }
            String csr = sb.toString();

            pb = new ProcessBuilder("openssl", "x509", "-req",
                    "-days", "365",
                    "-CA", "CA.crt",
                    "-CAkey", "CA.key",
                    "-CAserial", "CA.srl");
            pb.directory(root);
            ps = pb.start();

            writer = new BufferedWriter(new OutputStreamWriter(ps.getOutputStream()));
            writer.write(csr);
            writer.close();

            reader = new BufferedReader(new InputStreamReader(ps.getInputStream()));
            sb = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                sb.append('\n');
            }
            return sb.toString();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if (tmpKey != null) tmpKey.delete();
        }
        return null;
    }

    public static void main(String[] args) throws IOException {
        System.out.println(createCertificate("tan", new String("-----BEGIN PRIVATE KEY-----\n" +
                "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQC+5qPFRLGyo4/p\n" +
                "UhlBswOWjsFKfmDy8VOcVxwRBZ/zeZNWcx92s62HKct1ObNHX7PqD+uG7lOliyTC\n" +
                "RuQSf3fdz3QALOfbz97073TjipCgJo/kZZK644E2Qb4a1URbDKOpzoWReBjy20tD\n" +
                "thpVe1LQygFVZdlJGTPWmPr56aACsJQpy8XGVyIj3y5VvKQylN6EtTpsGp50jjeZ\n" +
                "t1WpXYKvYDsTeRuRY3nxrVXxX8Ju1sXljDMMHn5DBmWnfC3elZVSsmHBsdoPHvid\n" +
                "X4SMwJlj6fqqDcGOst+MokCVvdrK0ZmM/fCYVZX82Sws23nXEVVEwPOmEWyL0Dk7\n" +
                "NhCwH77jAgMBAAECggEATwABTBSWePfJCWRJQXAEjxy6jspn8oG9+RfvbHfoboik\n" +
                "WYVAww2uRR4drYBYWbQYL0exT2B3Sy2e46sUnTcU8rz0I1JyDPwkJzalJ7jjCvOK\n" +
                "XplONHsLsNlaA564L/m2DQKDMeUCZhYsOnQ0x6VGa1L9ZErGtcJxOmCKlDuDVTjo\n" +
                "LxCIvROu+vIqAa0llTb9esYkPvK6QJJcKeKPDPPMcfu7c1JQcBDuCismmkSbQSb9\n" +
                "URX250L+oyRFcFzLrSfwB/EURmBwDDbzKGICub8yII8SP/RWFd8S02mE+B5Qsy7S\n" +
                "Dayj850Cjy/yntn+IKdvr4UwQX9WJaI0fzrDjWxzIQKBgQDwIZxWfaOp6eQ3Jk0A\n" +
                "Arvfj/Upa633WGyovVADzCtMoQrb0C9GSqOvCUWn/nnaJB9ik9zEjMKQxkoDp68O\n" +
                "qPNdnO1A4Boe/P+UgMmO111FsIYOR5lgYb6UYXzjP4U2V9s3echf9C1cNX7Fo0rA\n" +
                "2jFVf3KZ+7NT0tHWAmFPZ7FUswKBgQDLhC5XdCexl4VwVEkDh1WAaD4A9rFgpyyC\n" +
                "jzQCTknTANfdXD9xXlWQJePYOROPTTJRxVwaR2xo0XtQJMz2CUhYbWiCWIxG6l/P\n" +
                "WNgK35w1aai1X3AfRPAO7FDFk7vUbuOk4+v9ePuROxnbc/QITJL44WrIfm6ZgGvf\n" +
                "FC0gbZflEQKBgALjJJIVqKYeXdQb7ckWP1QM0xHQbaMPuR6+R/wDtHGTbiH+etMJ\n" +
                "irZMEj5W3Pg2fvocdQcX+i7rc3Bfz8cJWQPDI0coaqf8usX6VnIVPNXdrX72dW2n\n" +
                "PzdhhLLmzJ8+pzAfkr00nmcBbajKsddnyDgS9DnNwPY9DrDYDnt577PpAoGBAMFW\n" +
                "Qfv8sdTJYA/VUkOS5owE+5pIwTtTtToWts7V9tYIAAofn3mgp2I4TJpVpppFON9r\n" +
                "wVKJtZhiIWaCf1/gc6Tl0xm46xJXh0tgWGUEBs7LIGWlU8uw3ukeYKFB9ncaHRLB\n" +
                "H6h9rWdLoQXUwui7bggXhS0Qxxr2YAPdk/0xf9IxAoGBAKGvEUduiet0gwEGCVta\n" +
                "fgoRBQ66/+44wZNlszCbw5q+xxbPIEXE9I0cUae3FnWue+SY30yRo9jCx7vvzw7r\n" +
                "RmRySnaNkTR6AER4/OmvPMzDYDB+9Q6hHEg44NcClpwKrABVyh4L5mz9eCHigJvL\n" +
                "IZ1Kbv6/97oN+i2Jk4Mguxr4\n" +
                "-----END PRIVATE KEY-----\n")));
    }
}
