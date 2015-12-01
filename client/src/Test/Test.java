package Test;

import sun.security.rsa.RSAPublicKeyImpl;
import org.bouncycastle.openssl.PEMParser;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;

/**
 * Test script
 */
public class Test {

    private static String workspace;

    private static String root;

    private static void initialize() throws Exception {
        workspace = System.getenv("workspace");
        root = System.getenv("root");

        BufferedReader reader = new BufferedReader(new FileReader(root + "/CA"));
        PEMParser parser = new PEMParser(reader);
        Object key = parser.readObject();
        System.out.println(key);
    }

    public static void main(String[] args) throws Exception {
        initialize();
    }
}
