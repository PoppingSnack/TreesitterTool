package org.guo.treesitter;

import org.guo.treesitter.core.SlicerFactory;
import org.guo.treesitter.model.CodeSlice;
import org.guo.treesitter.model.SliceType;
import org.guo.treesitter.service.CodeSlicer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class JavaSlicerTest {

    private String getTestProjectRoot() {
        return System.getProperty("user.dir") + "/TestProject/java_demo";
    }

    @Test
    public void testSliceCalculatorJava() throws IOException {
        Path path = Paths.get(getTestProjectRoot(), "Calculator.java");
        String code = Files.readString(path);
        
        CodeSlicer slicer = SlicerFactory.getSlicerByExtension("java");
        List<CodeSlice> slices = slicer.slice(code);
        
        Assertions.assertFalse(slices.isEmpty(), "Slices should not be empty");
        
        boolean foundAdd = false;
        boolean foundSubtract = false;
        boolean foundClass = false;
        
        for (CodeSlice slice : slices) {
            System.out.println("Java Slice: " + slice.getName() + " [" + slice.getType() + "]");
            if ("add".equals(slice.getName()) && slice.getType() == SliceType.FUNCTION) foundAdd = true;
            if ("subtract".equals(slice.getName()) && slice.getType() == SliceType.FUNCTION) foundSubtract = true;
            if ("Calculator".equals(slice.getName()) && slice.getType() == SliceType.CLASS) foundClass = true;
        }
        
        Assertions.assertTrue(foundAdd, "Should find add method");
        Assertions.assertTrue(foundSubtract, "Should find subtract method");
        Assertions.assertTrue(foundClass, "Should find Calculator class");
    }
}
