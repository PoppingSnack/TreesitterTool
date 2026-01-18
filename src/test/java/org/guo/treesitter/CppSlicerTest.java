package org.guo.treesitter;

import org.guo.treesitter.core.SlicerFactory;
import org.guo.treesitter.model.CodeSlice;
import org.guo.treesitter.service.CodeSlicer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class CppSlicerTest {

    @Test
    public void testSliceDemoCpp() throws IOException {
        Path path = Paths.get("d:\\Project\\WORKER\\TreesitterTool\\TestProject\\demo.cpp");
        String code = Files.readString(path);
        
        CodeSlicer slicer = SlicerFactory.getSlicerByExtension("cpp");
        List<CodeSlice> slices = slicer.slice(code);
        
        Assertions.assertFalse(slices.isEmpty(), "Slices should not be empty");
        
        boolean foundAdd = false;
        boolean foundGlobal = false;
        
        for (CodeSlice slice : slices) {
            System.out.println("Cpp Function: " + slice.getFunctionName());
            if ("add".equals(slice.getFunctionName())) foundAdd = true;
            if ("globalFunction".equals(slice.getFunctionName())) foundGlobal = true;
        }
        
        Assertions.assertTrue(foundAdd, "Should find add");
        Assertions.assertTrue(foundGlobal, "Should find globalFunction");
    }
}
