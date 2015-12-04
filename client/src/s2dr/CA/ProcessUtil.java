package s2dr.ca;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A utility class for process management
 */
public final class ProcessUtil {

    private static final File root = new File("../");

    private ProcessUtil() {}

    public static Process createOpenSSLProcess(String... args) throws IOException {
        List<String> command = new ArrayList<>();
        command.add("openssl");
        command.addAll(Arrays.asList(args));
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(root);
        return pb.start();
    }

    public static void writeStandardInput(String in, Process ps) throws IOException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(ps.getOutputStream()));
        writer.write(in);
        writer.close();
    }

    public static String getStandardOutput(Process ps) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(ps.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
            sb.append('\n');
        }
        return sb.toString();
    }

    public static String getStandardError(Process ps) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(ps.getErrorStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
            sb.append('\n');
        }
        return sb.toString();
    }
}
