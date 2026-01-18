package org.guo.treesitter;

import org.guo.treesitter.core.GoSlicer;
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

public class GoSlicerTest {

    private String getTestProjectRoot() {
        return System.getProperty("user.dir") + "/TestProject/go_demo";
    }

    @Test
    public void testSliceUtilsGo() throws IOException {
        Path path = Paths.get(getTestProjectRoot(), "utils.go");
        String code = Files.readString(path);
        
        CodeSlicer slicer = SlicerFactory.getSlicerByExtension("go");
        List<CodeSlice> slices = slicer.slice(code);
        
        Assertions.assertFalse(slices.isEmpty(), "Slices should not be empty");
        
        boolean foundAdd = false;
        boolean foundIncrement = false;
        
        for (CodeSlice slice : slices) {
            if ("Add".equals(slice.getFunctionName())) foundAdd = true;
            if ("Increment".equals(slice.getFunctionName())) foundIncrement = true;
        }
        
        Assertions.assertTrue(foundAdd, "Should find Add");
        Assertions.assertTrue(foundIncrement, "Should find Increment");
    }

    @Test
    public void testQueryApi() throws IOException {
        Path path = Paths.get(getTestProjectRoot(), "main.go");
        String code = Files.readString(path);

        GoSlicer slicer = new GoSlicer();
        TreeSitterQueryExecutor executor = new TreeSitterQueryExecutor(
            new org.treesitter.TreeSitterGo(),
            LanguageType.GO
        );

        String query = slicer.getFunctionQuery();
        List<Map<String, CodeSlice>> results = executor.execute(code, query);
        
        Assertions.assertEquals(1, results.size());
        Map<String, CodeSlice> match = results.get(0);
        Assertions.assertEquals("main", match.get("name").getContent());
    }
}
