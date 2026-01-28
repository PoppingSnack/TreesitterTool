package org.guo.treesitter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.guo.treesitter.model.ExportNode;
import org.guo.treesitter.model.LanguageType;
import org.guo.treesitter.service.ProjectExporter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ProjectExporterTest {

    private final ProjectExporter exporter = new ProjectExporter();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testExportPython() throws IOException {
        Path projectRoot = Paths.get(System.getProperty("user.dir"), "TestProject");
        Path outputDir = Paths.get(System.getProperty("user.dir"), "target", "export_test");
        Path outputFile = outputDir.resolve("nodes.ndjson");
        
        // Clean up previous run
        if (Files.exists(outputFile)) {
            Files.delete(outputFile);
        }
        
        exporter.exportWithWalk(projectRoot, LanguageType.PYTHON, outputFile);
        
        Assertions.assertTrue(Files.exists(outputFile), "Output file should be created");
        
        List<ExportNode> nodes = readNdjson(outputFile);
        Assertions.assertFalse(nodes.isEmpty(), "Should have exported nodes");
        
        // Check for specific function from python_demo/calculator.py
        boolean foundAdd = nodes.stream().anyMatch(n -> 
            n.getName().equals("add") && 
            n.getPath().contains("calculator.py") &&
            n.getLabel().equals("function_definition")
        );
        
        Assertions.assertTrue(foundAdd, "Should find 'add' function from calculator.py");

        // Check for Chinese content
        boolean foundChinese = nodes.stream().anyMatch(n -> 
            n.getText().contains("这是一个加法函数") || 
            n.getName().equals("中文方法")
        );
        Assertions.assertTrue(foundChinese, "Should find Chinese content or method name");
        
        // Verify format of one node
        ExportNode node = nodes.get(0);
        Assertions.assertNotNull(node.getNodeId(), "nodeId should not be null");
        Assertions.assertNotNull(node.getText(), "text should not be null");
        Assertions.assertTrue(node.getStartLine() > 0, "startLine should be > 0");
    }

    private List<ExportNode> readNdjson(Path file) throws IOException {
        List<ExportNode> nodes = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(file)) {
            String line;
            while ((line = reader.readLine()) != null) {
                nodes.add(objectMapper.readValue(line, ExportNode.class));
            }
        }
        return nodes;
    }
}
