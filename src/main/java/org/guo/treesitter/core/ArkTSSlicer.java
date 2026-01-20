package org.guo.treesitter.core;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.guo.treesitter.model.CodeSlice;
import org.guo.treesitter.model.LanguageType;
import org.guo.treesitter.model.SliceType;
import org.guo.treesitter.service.CodeSlicer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ArkTSSlicer implements CodeSlicer {

    private static final String PYTHON_SCRIPT_PATH = "scripts/arkts_slicer.py";
    // Use the python path provided by the user, default to simple "python" if not found
    private static final String CONFIGURED_PYTHON_EXECUTABLE = "D:\\Software\\python3.13\\python.exe";
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<CodeSlice> slice(String code) {
        // ArkTSSlicer requires a file on disk to work with the python script easily.
        // For slice(String code), we create a temp file.
        try {
            Path tempFile = Files.createTempFile("arkts_temp", ".ets");
            Files.writeString(tempFile, code);
            List<CodeSlice> slices = sliceFile(tempFile.toFile());
            Files.delete(tempFile);
            return slices;
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<CodeSlice> sliceFile(File file) {
        List<CodeSlice> slices = new ArrayList<>();
        
        String pythonCommand = "python";
        File configuredPython = new File(CONFIGURED_PYTHON_EXECUTABLE);
        if (configuredPython.exists() && configuredPython.isFile()) {
            pythonCommand = CONFIGURED_PYTHON_EXECUTABLE;
        } else {
            // Fallback check or logging if needed, for now just use "python" from PATH
            // But user specifically asked to handle non-path scenario if it's just a command name
            // If CONFIGURED_PYTHON_EXECUTABLE is not a path but a command (unlikely given the name, but possible logic)
            // Here we prioritize the absolute path if it exists.
        }

        try {
            String scriptAbsPath = new File(PYTHON_SCRIPT_PATH).getAbsolutePath();
            // Use the determined python command
            ProcessBuilder pb = new ProcessBuilder(pythonCommand, scriptAbsPath, file.getAbsolutePath());
            pb.redirectErrorStream(true);
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line);
            }
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                String jsonOutput = output.toString();
                if (jsonOutput.startsWith("{") && jsonOutput.contains("\"error\"")) {
                    System.err.println("Python script error: " + jsonOutput);
                    return slices;
                }
                
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
            } else {
                System.err.println("ArkTS slicer process failed with code " + exitCode);
                System.err.println("Output: " + output);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return slices;
    }
}
