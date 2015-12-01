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
        try {
            ProcessBuilder pb = new ProcessBuilder("openssl", "verify", "-trusted", "CA.crt");
            pb.directory(root);
            Process ps = pb.start();

            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(ps.getOutputStream()));
            writer.write(certificate);
            writer.close();

            BufferedReader reader = new BufferedReader(new InputStreamReader(ps.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                sb.append('\n');
            }
            if (sb.indexOf("OK") != -1) return true;
        }
        catch (IOException e) {
            e.printStackTrace();
        }
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
        System.out.println(validateCertificate(new String("-----BEGIN CERTIFICATE-----\n" +
                "MIICjzCCAXcCAQUwDQYJKoZIhvcNAQEFBQAwDTELMAkGA1UEAxMCQ0EwHhcNMTUx\n" +
                "MjAxMDMzNzAzWhcNMTYxMTMwMDMzNzAzWjAOMQwwCgYDVQQDEwN0YW4wggEiMA0G\n" +
                "CSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQC+5qPFRLGyo4/pUhlBswOWjsFKfmDy\n" +
                "8VOcVxwRBZ/zeZNWcx92s62HKct1ObNHX7PqD+uG7lOliyTCRuQSf3fdz3QALOfb\n" +
                "z97073TjipCgJo/kZZK644E2Qb4a1URbDKOpzoWReBjy20tDthpVe1LQygFVZdlJ\n" +
                "GTPWmPr56aACsJQpy8XGVyIj3y5VvKQylN6EtTpsGp50jjeZt1WpXYKvYDsTeRuR\n" +
                "Y3nxrVXxX8Ju1sXljDMMHn5DBmWnfC3elZVSsmHBsdoPHvidX4SMwJlj6fqqDcGO\n" +
                "st+MokCVvdrK0ZmM/fCYVZX82Sws23nXEVVEwPOmEWyL0Dk7NhCwH77jAgMBAAEw\n" +
                "DQYJKoZIhvcNAQEFBQADggEBAL7D/LeW3jg2Vqghjd4qHcfaWLTZ+gkJSNOedlQT\n" +
                "vYnJgicASqrU4ajcjpQG09gwi281LPEpOF3O4h6H6YSA6fQY4PvFAyRyfKNushnU\n" +
                "tL8+ercHTLInwkf1amN3KGl44C06uWkwclKnVd8jAi90Utl1+vCqq3O2UXnncRxy\n" +
                "dcKH/p0FU5EzmVXB7GZAU3TIQ1rDsssMjOAG0uV3WYu3XHmRULkkU8vWvW8x5adc\n" +
                "b0ZsRQtyqYG7XDestEM4gMeJZoddARoYCC4qHBEP7GE4i4bOREntvjz1OF3+aUQv\n" +
                "MmxlGIbpumVLsZ0TKfwJuXr2vvDmico/vIlSm7lfbclLU5w=\n" +
                "-----END CERTIFICATE-----\n")));
    }
}
