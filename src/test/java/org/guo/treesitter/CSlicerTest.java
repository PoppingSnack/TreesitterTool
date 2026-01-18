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

public class CSlicerTest {

    @Test
    public void testSliceDemoC() throws IOException {
        Path path = Paths.get("d:\\Project\\WORKER\\TreesitterTool\\TestProject\\demo.c");
        String code = Files.readString(path);
        
        CodeSlicer slicer = SlicerFactory.getSlicerByExtension("c");
        List<CodeSlice> slices = slicer.slice(code);
        
        Assertions.assertFalse(slices.isEmpty(), "Slices should not be empty");
        
        boolean foundAdd = false;
        boolean foundPrintHello = false;
        
        for (CodeSlice slice : slices) {
            System.out.println("C Function: " + slice.getFunctionName());
            if ("add".equals(slice.getFunctionName())) foundAdd = true;
            if ("print_hello".equals(slice.getFunctionName())) foundPrintHello = true;
        }
        
        Assertions.assertTrue(foundAdd, "Should find add");
        Assertions.assertTrue(foundPrintHello, "Should find print_hello");
    }
}
