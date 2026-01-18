package org.guo.treesitter;

import org.guo.treesitter.core.JavaSlicer;
import org.guo.treesitter.core.SlicerFactory;
import org.guo.treesitter.model.CodeSlice;
import org.guo.treesitter.model.LanguageType;
import org.guo.treesitter.query.TreeSitterQueryExecutor;
import org.guo.treesitter.service.CodeSlicer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class JavaSlicerTest {

    private String getTestProjectRoot() {
        return System.getProperty("user.dir") + "/TestProject/java_demo";
    }

    @Test
    public void testSliceCalculatorJava() throws IOException {
        Path path = Paths.get(getTestProjectRoot(), "Calculator.java");
        String code = Files.readString(path);
        
        CodeSlicer slicer = SlicerFactory.getSlicerByExtension("java");
        List<CodeSlice> slices = slicer.slice(code);
        
        Assertions.assertFalse(slices.isEmpty(), "Slices should not be empty");
        
        boolean foundAdd = false;
        boolean foundSubtract = false;
        
        for (CodeSlice slice : slices) {
            if ("add".equals(slice.getFunctionName())) foundAdd = true;
            if ("subtract".equals(slice.getFunctionName())) foundSubtract = true;
        }
        
        Assertions.assertTrue(foundAdd, "Should find add");
        Assertions.assertTrue(foundSubtract, "Should find subtract");
    }

    @Test
    public void testQueryApi() throws IOException {
        Path path = Paths.get(getTestProjectRoot(), "Main.java");
        String code = Files.readString(path);

        JavaSlicer slicer = new JavaSlicer();
        TreeSitterQueryExecutor executor = new TreeSitterQueryExecutor(
            new org.treesitter.TreeSitterJava(),
            LanguageType.JAVA
        );

        String query = slicer.getFunctionQuery();
        List<Map<String, CodeSlice>> results = executor.execute(code, query);
        
        Assertions.assertEquals(1, results.size());
        Map<String, CodeSlice> match = results.get(0);
        // Note: main method name is "main"
        Assertions.assertEquals("main", match.get("name").getContent());
    }
}
