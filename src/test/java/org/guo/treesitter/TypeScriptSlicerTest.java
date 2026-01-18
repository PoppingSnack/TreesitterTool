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

public class TypeScriptSlicerTest {

    @Test
    public void testSliceDemoTs() throws IOException {
        Path path = Paths.get("d:\\Project\\WORKER\\TreesitterTool\\TestProject\\demo.ts");
        String code = Files.readString(path);
        
        CodeSlicer slicer = SlicerFactory.getSlicerByExtension("ts");
        List<CodeSlice> slices = slicer.slice(code);
        
        Assertions.assertFalse(slices.isEmpty(), "Slices should not be empty");
        
        // Expected functions: greet, getName (constructor is usually a method_definition too but depends on parser)
        boolean foundGreet = false;
        boolean foundGetName = false;
        
        for (CodeSlice slice : slices) {
            System.out.println("Found function: " + slice.getFunctionName());
            if ("greet".equals(slice.getFunctionName())) foundGreet = true;
            if ("getName".equals(slice.getFunctionName())) foundGetName = true;
        }
        
        Assertions.assertTrue(foundGreet, "Should find greet");
        Assertions.assertTrue(foundGetName, "Should find getName");
    }
}
