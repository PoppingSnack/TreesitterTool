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

public class GoSlicerTest {

    @Test
    public void testSliceDemoGo() throws IOException {
        Path path = Paths.get("d:\\Project\\WORKER\\TreesitterTool\\TestProject\\demo.go");
        String code = Files.readString(path);
        
        CodeSlicer slicer = SlicerFactory.getSlicerByExtension("go");
        List<CodeSlice> slices = slicer.slice(code);
        
        Assertions.assertFalse(slices.isEmpty(), "Slices should not be empty");
        
        boolean foundMain = false;
        boolean foundGreet = false;
        
        for (CodeSlice slice : slices) {
            System.out.println("Go Function: " + slice.getFunctionName());
            if ("main".equals(slice.getFunctionName())) foundMain = true;
            if ("Greet".equals(slice.getFunctionName())) foundGreet = true;
        }
        
        Assertions.assertTrue(foundMain, "Should find main");
        Assertions.assertTrue(foundGreet, "Should find Greet");
    }
}
