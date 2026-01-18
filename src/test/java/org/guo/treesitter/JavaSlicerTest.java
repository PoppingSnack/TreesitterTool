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

public class JavaSlicerTest {

    @Test
    public void testSliceDemoJava() throws IOException {
        Path path = Paths.get("d:\\Project\\WORKER\\TreesitterTool\\TestProject\\Demo.java");
        String code = Files.readString(path);
        
        CodeSlicer slicer = SlicerFactory.getSlicerByExtension("java");
        List<CodeSlice> slices = slicer.slice(code);
        
        Assertions.assertFalse(slices.isEmpty(), "Slices should not be empty");
        
        boolean foundConstructor = false;
        boolean foundSayHello = false;
        
        for (CodeSlice slice : slices) {
            System.out.println("Java Function: " + slice.getFunctionName());
            // Note: Constructor name matches class name usually, but our extractor might get it.
            // In JavaSlicer, we extract name node. For constructor_declaration, name is identifier (Demo).
            if ("Demo".equals(slice.getFunctionName())) foundConstructor = true;
            if ("sayHello".equals(slice.getFunctionName())) foundSayHello = true;
        }
        
        Assertions.assertTrue(foundConstructor, "Should find Demo constructor");
        Assertions.assertTrue(foundSayHello, "Should find sayHello");
    }
}
