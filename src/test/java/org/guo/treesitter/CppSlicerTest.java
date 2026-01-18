package org.guo.treesitter;

import org.guo.treesitter.core.CppSlicer;
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

public class CppSlicerTest {

    private String getTestProjectRoot() {
        return System.getProperty("user.dir") + "/TestProject/cpp_demo";
    }

    @Test
    public void testSliceShapeCpp() throws IOException {
        Path path = Paths.get(getTestProjectRoot(), "Shape.cpp");
        String code = Files.readString(path);
        
        CodeSlicer slicer = SlicerFactory.getSlicerByExtension("cpp");
        List<CodeSlice> slices = slicer.slice(code);
        
        Assertions.assertFalse(slices.isEmpty(), "Slices should not be empty");
        
        boolean foundGetArea = false;
        boolean foundSetWidth = false;
        boolean foundConstructor = false;
        
        for (CodeSlice slice : slices) {
            // Note: CppSlicer extracts fully qualified names if possible or just identifiers
            // In Shape.cpp: Rectangle::getArea -> qualified_identifier
            String funcName = slice.getFunctionName();
            System.out.println("Found function: " + funcName);
            if (funcName.contains("getArea")) foundGetArea = true;
            if (funcName.contains("setWidth")) foundSetWidth = true;
            if (funcName.contains("Rectangle")) foundConstructor = true;
        }
        
        Assertions.assertTrue(foundGetArea, "Should find getArea");
        Assertions.assertTrue(foundSetWidth, "Should find setWidth");
        Assertions.assertTrue(foundConstructor, "Should find Constructor");
    }

    @Test
    public void testQueryApi() throws IOException {
        Path path = Paths.get(getTestProjectRoot(), "main.cpp");
        String code = Files.readString(path);

        CppSlicer slicer = new CppSlicer();
        TreeSitterQueryExecutor executor = new TreeSitterQueryExecutor(
            new org.treesitter.TreeSitterCpp(),
            LanguageType.CPP
        );

        String query = slicer.getFunctionQuery();
        List<Map<String, CodeSlice>> results = executor.execute(code, query);
        
        Assertions.assertEquals(1, results.size());
        Map<String, CodeSlice> match = results.get(0);
        Assertions.assertEquals("main", match.get("name").getContent());
    }
}
