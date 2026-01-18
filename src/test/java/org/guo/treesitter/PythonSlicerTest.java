package org.guo.treesitter;

import org.guo.treesitter.core.PythonSlicer;
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

public class PythonSlicerTest {

    private String getTestProjectRoot() {
        return System.getProperty("user.dir") + "/TestProject/python_demo";
    }

    @Test
    public void testSliceCalculatorPy() throws IOException {
        Path path = Paths.get(getTestProjectRoot(), "calculator.py");
        String code = Files.readString(path);
        
        CodeSlicer slicer = SlicerFactory.getSlicerByExtension("py");
        List<CodeSlice> slices = slicer.slice(code);
        
        Assertions.assertFalse(slices.isEmpty(), "Slices should not be empty");
        
        boolean foundAdd = false;
        boolean foundSubtract = false;
        boolean foundInit = false;
        
        for (CodeSlice slice : slices) {
            if ("add".equals(slice.getFunctionName())) foundAdd = true;
            if ("subtract".equals(slice.getFunctionName())) foundSubtract = true;
            if ("__init__".equals(slice.getFunctionName())) foundInit = true;
        }
        
        Assertions.assertTrue(foundAdd, "Should find add");
        Assertions.assertTrue(foundSubtract, "Should find subtract");
        Assertions.assertTrue(foundInit, "Should find __init__");
    }

    @Test
    public void testQueryApi() throws IOException {
        Path path = Paths.get(getTestProjectRoot(), "main.py");
        String code = Files.readString(path);

        PythonSlicer slicer = new PythonSlicer();
        TreeSitterQueryExecutor executor = new TreeSitterQueryExecutor(
            new org.treesitter.TreeSitterPython(),
            LanguageType.PYTHON
        );

        String query = slicer.getFunctionQuery();
        List<Map<String, CodeSlice>> results = executor.execute(code, query);
        
        Assertions.assertEquals(1, results.size());
        Map<String, CodeSlice> match = results.get(0);
        Assertions.assertEquals("main", match.get("name").getContent());
    }
}
