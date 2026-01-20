package org.guo.treesitter;

import org.guo.treesitter.core.JavaScriptSlicer;
import org.guo.treesitter.core.SlicerFactory;
import org.guo.treesitter.model.CodeSlice;
import org.guo.treesitter.model.LanguageType;
import org.guo.treesitter.model.SliceType;
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

public class JavaScriptSlicerTest {

    private String getTestProjectRoot() {
        return System.getProperty("user.dir") + "/TestProject/javascript_demo";
    }

    @Test
    public void testSliceCalculatorJs() throws IOException {
        Path path = Paths.get(getTestProjectRoot(), "calculator.js");
        String code = Files.readString(path);
        
        CodeSlicer slicer = SlicerFactory.getSlicerByExtension("js");
        List<CodeSlice> slices = slicer.slice(code);
        
        Assertions.assertFalse(slices.isEmpty(), "Slices should not be empty");
        
        boolean foundAdd = false;
        boolean foundGlobalAdd = false;
        boolean foundClass = false;
        
        for (CodeSlice slice : slices) {
            System.out.println("JS Slice: " + slice.getName() + " [" + slice.getType() + "]");
            if ("add".equals(slice.getName()) && slice.getType() == SliceType.FUNCTION) foundAdd = true;
            if ("globalAdd".equals(slice.getName()) && slice.getType() == SliceType.FUNCTION) foundGlobalAdd = true;
            if ("Calculator".equals(slice.getName()) && slice.getType() == SliceType.CLASS) foundClass = true;
        }
        
        Assertions.assertTrue(foundAdd, "Should find add method");
        Assertions.assertTrue(foundGlobalAdd, "Should find globalAdd function");
        Assertions.assertTrue(foundClass, "Should find Calculator class");
    }

    @Test
    public void testQueryApi() throws IOException {
        Path path = Paths.get(getTestProjectRoot(), "main.js");
        String code = Files.readString(path);

        JavaScriptSlicer slicer = new JavaScriptSlicer();
        TreeSitterQueryExecutor executor = new TreeSitterQueryExecutor(
            new org.treesitter.TreeSitterJavascript(),
            LanguageType.JAVASCRIPT
        );

        String query = slicer.getFunctionQuery();
        List<Map<String, CodeSlice>> results = executor.execute(code, query);
        
        Assertions.assertEquals(1, results.size());
        Map<String, CodeSlice> match = results.get(0);
        Assertions.assertEquals("main", match.get("name").getContent());
    }
}
