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

public class GoSlicerTest {

    private String getTestProjectRoot() {
        return System.getProperty("user.dir") + "/TestProject/go_demo";
    }

    @Test
    public void testSliceUtilsGo() throws IOException {
        Path path = Paths.get(getTestProjectRoot(), "utils.go");
        String code = Files.readString(path);
        
        CodeSlicer slicer = SlicerFactory.getSlicerByExtension("go");
        List<CodeSlice> slices = slicer.slice(code);
        
        Assertions.assertFalse(slices.isEmpty(), "Slices should not be empty");
        
        boolean foundAdd = false;
        boolean foundCounter = false;
        
        for (CodeSlice slice : slices) {
            System.out.println("Go Slice: " + slice.getName() + " [" + slice.getType() + "]");
            if ("Add".equals(slice.getName()) && slice.getType() == SliceType.FUNCTION) foundAdd = true;
            if ("Counter".equals(slice.getName()) && slice.getType() == SliceType.STRUCT) foundCounter = true;
        }
        
        Assertions.assertTrue(foundAdd, "Should find Add function");
        Assertions.assertTrue(foundCounter, "Should find Counter struct");
    }
}
