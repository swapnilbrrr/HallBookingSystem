package utils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Low-level text-file access used by every repository.
 *
 * All data is stored in plain .txt files as required by the assignment brief;
 * no database is involved.
 */
public class FileHandler {

    // Generic method to read all lines from a specific text file
    public static List<String> readLines(String filePath) {
        List<String> lines = new ArrayList<>();
        File file = new File(filePath);
        if (!file.exists()) {
            return lines; // A missing file simply has no records yet
        }
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + filePath + " (" + e.getMessage() + ")");
        }
        return lines;
    }

    /**
     * Appends one record to a text file.
     *
     * If the file already exists but does not end with a line separator, a
     * separator is written first. Without this guard the new record would be
     * concatenated onto the last existing one, silently corrupting both.
     */
    public static void appendLine(String filePath, String data) {
        try {
            ensureParentDirectory(filePath);
            boolean needsSeparator = !endsWithNewline(filePath);
            try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(filePath, true), StandardCharsets.UTF_8))) {
                if (needsSeparator) {
                    bw.newLine();
                }
                bw.write(data);
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error writing to file: " + filePath + " (" + e.getMessage() + ")");
        }
    }

    public static void writeAllLines(String filePath, List<String> lines) {
        try {
            ensureParentDirectory(filePath);
            try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(filePath, false), StandardCharsets.UTF_8))) {
                for (String line : lines) {
                    bw.write(line);
                    bw.newLine();
                }
            }
        } catch (IOException e) {
            System.err.println("Error overwriting file: " + filePath + " (" + e.getMessage() + ")");
        }
    }

    /** True when the file is absent, empty, or its final byte is CR or LF. */
    private static boolean endsWithNewline(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists() || file.length() == 0) {
            return true; // Nothing to separate from
        }
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            raf.seek(file.length() - 1);
            int last = raf.read();
            return last == '\n' || last == '\r';
        }
    }

    private static void ensureParentDirectory(String filePath) {
        File parent = new File(filePath).getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
    }
}
