package org.guo.treesitter.utils;

import org.mozilla.universalchardet.UniversalDetector;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileUtils {

    /**
     * Reads file content with automatic encoding detection.
     * Defaults to UTF-8 if detection fails.
     */
    public static String readFile(Path file) throws IOException {
        byte[] bytes = Files.readAllBytes(file);
        Charset charset = detectCharset(bytes);
        return new String(bytes, charset);
    }

    private static Charset detectCharset(byte[] bytes) {
        UniversalDetector detector = new UniversalDetector(null);
        detector.handleData(bytes, 0, bytes.length);
        detector.dataEnd();
        String encoding = detector.getDetectedCharset();
        detector.reset();

        if (encoding != null) {
            try {
                return Charset.forName(encoding);
            } catch (Exception ignored) {
                // Fallback to UTF-8 if detected charset is not supported by JVM
            }
        }
        // Default fallback
        return StandardCharsets.UTF_8;
    }
}
