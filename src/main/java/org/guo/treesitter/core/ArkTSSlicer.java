package org.guo.treesitter.core;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.guo.treesitter.model.CodeSlice;
import org.guo.treesitter.model.LanguageType;
import org.guo.treesitter.model.SliceType;
import org.guo.treesitter.service.CodeSlicer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ArkTSSlicer implements CodeSlicer {

    private static final String PYTHON_SCRIPT_PATH = "scripts/arkts_slicer.py";
    private final ObjectMapper objectMapper = new ObjectMapper();

    // ThreadLocal to manage a separate Python daemon process for each thread
    private static final ThreadLocal<PythonDaemon> pythonDaemon = ThreadLocal.withInitial(() -> null);

    private static class PythonDaemon {
        Process process;
        BufferedWriter writer;
        BufferedReader reader;

        PythonDaemon(Process process) {
            this.process = process;
            // Use UTF-8 explicitly to avoid encoding issues with non-ASCII characters (e.g. Chinese)
            this.writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8));
            this.reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
        }

        void close() {
            try {
                if (writer != null) writer.close();
                if (reader != null) reader.close();
            } catch (IOException ignored) {}
            if (process != null) process.destroy();
        }
    }

    /**
     * Resolves the Python executable path with the following priority:
     * 1. Embedded runtime in project_root/runtime/[os]/python/
     * 2. Configured path (dev environment)
     * 3. System PATH ("python" or "python3")
     */
    private String getPythonCommand() {
        String os = System.getProperty("os.name").toLowerCase();
        Path projectRoot = Path.of(System.getProperty("user.dir"));
        Path embeddedPython;

        if (os.contains("win")) {
            // Windows: runtime/win/python/python.exe
            embeddedPython = projectRoot.resolve("runtime/win/python/python.exe");
        } else {
            // Linux/Mac: runtime/linux/python/bin/python3
            embeddedPython = projectRoot.resolve("runtime/linux/python/bin/python3");
        }

        if (Files.exists(embeddedPython)) {
            return embeddedPython.toAbsolutePath().toString();
        }

        // Fallback to previous hardcoded path or system path
        File configuredPython = new File("D:\\Software\\python3.13\\python.exe");
        if (configuredPython.exists() && configuredPython.isFile()) {
            return configuredPython.getAbsolutePath();
        }
        
        return "python";
    }

    @Override
    public List<CodeSlice> slice(String code) {
        ObjectNode request = objectMapper.createObjectNode();
        request.put("content", code);
        return sliceInternal(request);
    }


    public List<CodeSlice> sliceFile(File file) {
        ObjectNode request = objectMapper.createObjectNode();
        request.put("file", file.getAbsolutePath());
        return sliceInternal(request);
    }

    private List<CodeSlice> sliceInternal(ObjectNode requestNode) {
        List<CodeSlice> slices = new ArrayList<>();
        PythonDaemon daemon = getOrStartDaemon();

        if (daemon == null) {
            System.err.println("Failed to start ArkTS slicer daemon.");
            return slices;
        }

        try {
            // Send request
            String requestJson = objectMapper.writeValueAsString(requestNode);
            // Ensure single line for the protocol (ObjectMapper usually produces single line by default unless pretty printer is enabled)
            // But to be safe, replace newlines if any (though content might have newlines, json escapes them)
            // writeValueAsString guarantees a valid JSON string, which escapes internal newlines.
            // So it is safe to write it as a line.
            
            daemon.writer.write(requestJson);
            daemon.writer.newLine();
            daemon.writer.flush();

            // Read response
            String jsonOutput = daemon.reader.readLine();
            
            if (jsonOutput == null) {
                // Process likely died
                System.err.println("ArkTS slicer daemon closed unexpectedly. Restarting...");
                daemon.close();
                pythonDaemon.remove(); // Clear invalid daemon
                
                // Retry once
                daemon = getOrStartDaemon();
                if (daemon != null) {
                    daemon.writer.write(requestJson);
                    daemon.writer.newLine();
                    daemon.writer.flush();
                    jsonOutput = daemon.reader.readLine();
                }
            }

            if (jsonOutput != null) {
                 if (jsonOutput.startsWith("{") && jsonOutput.contains("\"error\"")) {
                    // It might be a valid error response from python
                    // Let's parse it to be sure
                    try {
                        Map<String, Object> errorMap = objectMapper.readValue(jsonOutput, new TypeReference<>() {});
                        if (errorMap.containsKey("error")) {
                            System.err.println("Python script error: " + errorMap.get("error"));
                            return slices;
                        }
                    } catch (Exception ignored) {
                        // Not a simple error object, maybe list? Proceed to try parsing as list.
                    }
                }
                
                // If it's a list (expected success case)
                if (jsonOutput.startsWith("[")) {
                    List<Map<String, Object>> rawSlices = objectMapper.readValue(jsonOutput, new TypeReference<>() {});
                    for (Map<String, Object> raw : rawSlices) {
                        String name = (String) raw.get("name");
                        String typeStr = (String) raw.get("type");
                        String content = (String) raw.get("content");
                        int startLine = (int) raw.get("startLine");
                        int endLine = (int) raw.get("endLine");
                        
                        SliceType type = SliceType.valueOf(typeStr);
                        slices.add(new CodeSlice(content, name, startLine, endLine, LanguageType.ARKTS, type));
                    }
                } else if (jsonOutput.contains("\"error\"")) {
                     System.err.println("Python script returned error: " + jsonOutput);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            // Invalidate daemon on error
            if (daemon != null) daemon.close();
            pythonDaemon.remove();
        }
        return slices;
    }

    private PythonDaemon getOrStartDaemon() {
        PythonDaemon daemon = pythonDaemon.get();
        if (daemon != null && daemon.process.isAlive()) {
            return daemon;
        }

        String pythonCommand = getPythonCommand();

        try {
            String scriptAbsPath = new File(PYTHON_SCRIPT_PATH).getAbsolutePath();
            // Start process in daemon mode
            ProcessBuilder pb = new ProcessBuilder(pythonCommand, scriptAbsPath, "--daemon");
            // DO NOT redirect error stream to stdout, keep them separate to avoid polluting JSON output
            // pb.redirectErrorStream(true); 
            
            // Set PYTHONPATH if using embedded runtime to ensure dependencies are found?
            // Usually embedded python finds its own site-packages if configured correctly (python310._pth or site-packages folder).
            // But if we want to be safe, we could add project_root/runtime/libs or similar.
            // For now, assume standard portable python layout.
            
            Process process = pb.start();
            daemon = new PythonDaemon(process);
            pythonDaemon.set(daemon);
            return daemon;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
