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

public class PythonSlicerTest {

    @Test
    public void testSliceDemoPy() throws IOException {
        Path path = Paths.get("d:\\Project\\WORKER\\TreesitterTool\\TestProject\\demo.py");
        String code = Files.readString(path);
        
        CodeSlicer slicer = SlicerFactory.getSlicerByExtension("py");
        List<CodeSlice> slices = slicer.slice(code);
        
        Assertions.assertFalse(slices.isEmpty(), "Slices should not be empty");
        
        // Expected functions: hello_world, add, subtract
        boolean foundHello = false;
        boolean foundAdd = false;
        boolean foundSubtract = false;
        
        for (CodeSlice slice : slices) {
            System.out.println("Found function: " + slice.getFunctionName());
            if ("hello_world".equals(slice.getFunctionName())) foundHello = true;
            if ("add".equals(slice.getFunctionName())) foundAdd = true;
            if ("subtract".equals(slice.getFunctionName())) foundSubtract = true;
        }
        
        Assertions.assertTrue(foundHello, "Should find hello_world");
        Assertions.assertTrue(foundAdd, "Should find add");
        Assertions.assertTrue(foundSubtract, "Should find subtract");
    }
}
