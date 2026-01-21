package org.guo.treesitter;

import org.guo.treesitter.model.CodeSlice;
import org.guo.treesitter.model.LanguageType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class TreeSitterToolTest {

    private final TreeSitterTool tool = new TreeSitterTool();
    private final Path testProjectRoot = Paths.get(System.getProperty("user.dir"), "TestProject");

    @Test
    public void testAnalyzeFile() throws IOException {
        Path pythonFile = testProjectRoot.resolve("python_demo/main.py");
        List<CodeSlice> slices = tool.analyzeFile(pythonFile);
        
        Assertions.assertFalse(slices.isEmpty());
        Assertions.assertEquals(LanguageType.PYTHON, slices.get(0).getLanguage());
    }

    @Test
    public void testAnalyzeSourceString() {
        String code = "def foo(): pass";
        List<CodeSlice> slices = tool.analyzeSource(code, LanguageType.PYTHON);
        
        Assertions.assertEquals(1, slices.size());
        Assertions.assertEquals("foo", slices.get(0).getName());
    }

    @Test
    public void testAnalyzeSourceStream() throws IOException {
        String code = "class MyClass { void method() {} }";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(code.getBytes(StandardCharsets.UTF_8));
        
        List<CodeSlice> slices = tool.analyzeSource(inputStream, LanguageType.JAVA);
        
        Assertions.assertFalse(slices.isEmpty());
        // Should find class and method
        Assertions.assertTrue(slices.stream().anyMatch(s -> s.getName().equals("MyClass")));
    }

    @Test
    public void testAnalyzeProjectAutoDetect() throws IOException {
        List<CodeSlice> slices = tool.analyzeProject(testProjectRoot);
        
        // Should contain slices from multiple languages
        boolean hasPython = slices.stream().anyMatch(s -> s.getLanguage() == LanguageType.PYTHON);
        boolean hasJava = slices.stream().anyMatch(s -> s.getLanguage() == LanguageType.JAVA);
        
        Assertions.assertTrue(hasPython, "Should find Python slices");
        Assertions.assertTrue(hasJava, "Should find Java slices");
    }

    @Test
    public void testAnalyzeProjectSpecificLanguage() throws IOException {
        List<CodeSlice> slices = tool.analyzeProject(testProjectRoot, LanguageType.GO);
        
        // Should only contain Go slices
        Assertions.assertFalse(slices.isEmpty());
        boolean allGo = slices.stream().allMatch(s -> s.getLanguage() == LanguageType.GO);
        Assertions.assertTrue(allGo, "All slices should be Go");
    }
}
