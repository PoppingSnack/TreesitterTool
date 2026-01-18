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

public class CppSlicerTest {

    private String getTestProjectRoot() {
        return System.getProperty("user.dir") + "/TestProject/cpp_demo";
    }

    @Test
    public void testSliceShapeCpp() throws IOException {
        // Test Header file for Class Definition
        Path path = Paths.get(getTestProjectRoot(), "Shape.h");
        String code = Files.readString(path);
        
        CodeSlicer slicer = SlicerFactory.getSlicerByExtension("h");
        List<CodeSlice> slices = slicer.slice(code);
        
        boolean foundClass = false;
        for (CodeSlice slice : slices) {
            System.out.println("Cpp Header Slice: " + slice.getName() + " [" + slice.getType() + "]");
            if ("Rectangle".equals(slice.getName()) && slice.getType() == SliceType.CLASS) foundClass = true;
        }
        
        Assertions.assertTrue(foundClass, "Should find Rectangle class in header");
    }
    
    @Test
    public void testSliceImplCpp() throws IOException {
        Path path = Paths.get(getTestProjectRoot(), "Shape.cpp");
        String code = Files.readString(path);
        
        CodeSlicer slicer = SlicerFactory.getSlicerByExtension("cpp");
        List<CodeSlice> slices = slicer.slice(code);
        
        boolean foundMethod = false;
        for (CodeSlice slice : slices) {
            System.out.println("Cpp Impl Slice: " + slice.getName() + " [" + slice.getType() + "]");
            if (slice.getName().contains("getArea") && slice.getType() == SliceType.FUNCTION) foundMethod = true;
        }
        Assertions.assertTrue(foundMethod, "Should find getArea method in cpp");
    }
}
