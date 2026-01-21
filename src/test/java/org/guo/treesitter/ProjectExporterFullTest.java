package org.guo.treesitter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.guo.treesitter.model.ExportNode;
import org.guo.treesitter.model.LanguageType;
import org.guo.treesitter.service.ProjectExporter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ProjectExporterFullTest {

    private final ProjectExporter exporter = new ProjectExporter();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private Path outputDir;
    private Path projectRoot;

    @BeforeEach
    public void setup() throws IOException {
        projectRoot = Paths.get(System.getProperty("user.dir"), "TestProject");
        outputDir = Paths.get(System.getProperty("user.dir"), "target", "export_test_full");
        Files.createDirectories(outputDir);
    }

    private List<ExportNode> exportAndRead(LanguageType language) throws IOException {
        Path outputFile = outputDir.resolve(language.name().toLowerCase() + "_nodes.ndjson");
        if (Files.exists(outputFile)) Files.delete(outputFile);

        exporter.exportWithWalk(projectRoot, language, outputFile);

        Assertions.assertTrue(Files.exists(outputFile), language + " output file should exist");
        return readNdjson(outputFile);
    }

    private List<ExportNode> readNdjson(Path file) throws IOException {
        List<ExportNode> nodes = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file.toFile()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                nodes.add(objectMapper.readValue(line, ExportNode.class));
            }
        }
        return nodes;
    }

    @Test
    public void testExportJava() throws IOException {
        List<ExportNode> nodes = exportAndRead(LanguageType.JAVA);
        // Expect: Main.main, Calculator.add, Calculator.subtract
        // Note: ProjectExporter currently filters slice.getType() == SliceType.FUNCTION
        // So Classes themselves won't be exported as nodes, only methods within them.
        // User requested "项目中的需要有写各中类型的函数和方法" -> "Project should have various types of functions and methods"
        // And output requirement said: "只输出函数切片" (Only output function slices).
        
        assertFunctionExists(nodes, "main", "Main.java");
        assertFunctionExists(nodes, "add", "Calculator.java");
    }

    @Test
    public void testExportPython() throws IOException {
        List<ExportNode> nodes = exportAndRead(LanguageType.PYTHON);
        // python_demo/main.py -> main
        // python_demo/calculator.py -> __init__, add, subtract
        
        assertFunctionExists(nodes, "main", "main.py");
        assertFunctionExists(nodes, "add", "calculator.py");
        assertFunctionExists(nodes, "__init__", "calculator.py");
    }

    @Test
    public void testExportTypeScript() throws IOException {
        List<ExportNode> nodes = exportAndRead(LanguageType.TYPESCRIPT);
        // typescript_demo/index.ts -> main
        // typescript_demo/user_service.ts -> addUser, getUsers
        
        assertFunctionExists(nodes, "main", "index.ts");
        assertFunctionExists(nodes, "addUser", "user_service.ts");
    }

    @Test
    public void testExportJavaScript() throws IOException {
        List<ExportNode> nodes = exportAndRead(LanguageType.JAVASCRIPT);
        // javascript_demo/main.js -> main
        // javascript_demo/calculator.js -> add, subtract, globalAdd
        
        assertFunctionExists(nodes, "main", "main.js");
        assertFunctionExists(nodes, "add", "calculator.js");
        assertFunctionExists(nodes, "globalAdd", "calculator.js");
    }

    @Test
    public void testExportGo() throws IOException {
        List<ExportNode> nodes = exportAndRead(LanguageType.GO);
        // go_demo/main.go -> main
        // go_demo/utils.go -> Add, Increment
        
        assertFunctionExists(nodes, "main", "main.go");
        assertFunctionExists(nodes, "Add", "utils.go");
        assertFunctionExists(nodes, "Increment", "utils.go");
    }

    @Test
    public void testExportC() throws IOException {
        List<ExportNode> nodes = exportAndRead(LanguageType.C);
        // c_demo/main.c -> main
        // c_demo/math_utils.c -> add, subtract
        
        assertFunctionExists(nodes, "main", "main.c");
        assertFunctionExists(nodes, "add", "math_utils.c");
    }

    @Test
    public void testExportCpp() throws IOException {
        List<ExportNode> nodes = exportAndRead(LanguageType.CPP);
        // cpp_demo/main.cpp -> main
        // cpp_demo/Shape.cpp -> getArea, setWidth, Rectangle (constructor)
        
        assertFunctionExists(nodes, "main", "main.cpp");
        // Check partial name match or exact match depending on extractor
        // CppSlicer might return qualified name or just name
        boolean foundGetArea = nodes.stream().anyMatch(n -> n.getName().contains("getArea"));
        Assertions.assertTrue(foundGetArea, "Should find getArea");
    }
    
    @Test
    public void testExportArkTS() throws IOException {
        // ArkTS Slicer relies on Python environment.
        // Assuming environment is set up as per previous steps.
        List<ExportNode> nodes = exportAndRead(LanguageType.ARKTS);
        
        // arkts_demo/demo.ets -> globalHelper
        // Note: MyComponent and EntryComponent are STRUCTs.
        // If ProjectExporter ONLY exports FUNCTION, then structs won't appear.
        // User said: "只输出函数切片" (Only output function slices).
        // So we only check for functions.
        
        assertFunctionExists(nodes, "globalHelper", "demo.ets");
    }

    private void assertFunctionExists(List<ExportNode> nodes, String funcName, String fileNamePart) {
        boolean found = nodes.stream().anyMatch(n -> 
            n.getName().equals(funcName) && 
            n.getPath().replace("\\", "/").contains(fileNamePart)
        );
        if (!found) {
            String existing = nodes.stream()
                .map(n -> n.getName() + "(" + n.getPath() + ")")
                .collect(Collectors.joining(", "));
            Assertions.fail("Function " + funcName + " in " + fileNamePart + " not found. Existing: " + existing);
        }
    }
}
