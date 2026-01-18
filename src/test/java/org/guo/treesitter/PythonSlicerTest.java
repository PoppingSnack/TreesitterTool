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
        boolean foundClass = false;
        
        for (CodeSlice slice : slices) {
            System.out.println("Python Slice: " + slice.getName() + " [" + slice.getType() + "]");
            if ("add".equals(slice.getName()) && slice.getType() == SliceType.FUNCTION) foundAdd = true;
            if ("Calculator".equals(slice.getName()) && slice.getType() == SliceType.CLASS) foundClass = true;
        }
        
        Assertions.assertTrue(foundAdd, "Should find add method");
        Assertions.assertTrue(foundClass, "Should find Calculator class");
    }
}
