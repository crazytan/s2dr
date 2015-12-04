package s2dr.ca;

import java.io.*;

/**
 * A class representing Certificate Authority
 */
public final class CA {

    private static final File root = new File(System.getenv("root"));

    private CA() {}

    public static String extractPublicKeyFromCertificate(String certificate) {
        try {
            Process ps = ProcessUtil.createOpenSSLProcess("x509", "-pubkey", "-noout");
            ProcessUtil.writeStandardInput(certificate, ps);
            return ProcessUtil.getStandardOutput(ps);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean validateCertificate(String certificate) {
        try {
            Process ps = ProcessUtil.createOpenSSLProcess("verify", "-trusted", "CA.crt");
            ProcessUtil.writeStandardInput(certificate, ps);
            String result = ProcessUtil.getStandardOutput(ps);
            if (result.contains("OK")) {
                return true;
            }
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

            Process ps = ProcessUtil.createOpenSSLProcess("req", "-new", "-key", tmpKey.getName(), "-subj", "/CN=" + name);
            String csr = ProcessUtil.getStandardOutput(ps);
            ps = ProcessUtil.createOpenSSLProcess("x509", "-req", "-days", "365", "-CA", "CA.crt", "-CAkey", "CA.key", "-CAserial", "CA.srl");
            ProcessUtil.writeStandardInput(csr, ps);
            return ProcessUtil.getStandardOutput(ps);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if (tmpKey != null) {
                tmpKey.delete();
            }
        }
        return null;
    }
}
