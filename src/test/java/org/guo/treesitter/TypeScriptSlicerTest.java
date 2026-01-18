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

public class TypeScriptSlicerTest {

    private String getTestProjectRoot() {
        return System.getProperty("user.dir") + "/TestProject/typescript_demo";
    }

    @Test
    public void testSliceUserService() throws IOException {
        Path path = Paths.get(getTestProjectRoot(), "user_service.ts");
        String code = Files.readString(path);
        
        CodeSlicer slicer = SlicerFactory.getSlicerByExtension("ts");
        List<CodeSlice> slices = slicer.slice(code);
        
        Assertions.assertFalse(slices.isEmpty(), "Slices should not be empty");
        
        boolean foundAddUser = false;
        boolean foundClass = false;
        boolean foundInterface = false;
        
        for (CodeSlice slice : slices) {
            System.out.println("TS Slice: " + slice.getName() + " [" + slice.getType() + "]");
            if ("addUser".equals(slice.getName()) && slice.getType() == SliceType.FUNCTION) foundAddUser = true;
            if ("UserService".equals(slice.getName()) && slice.getType() == SliceType.CLASS) foundClass = true;
            if ("User".equals(slice.getName()) && slice.getType() == SliceType.INTERFACE) foundInterface = true;
        }
        
        Assertions.assertTrue(foundAddUser, "Should find addUser method");
        Assertions.assertTrue(foundClass, "Should find UserService class");
        Assertions.assertTrue(foundInterface, "Should find User interface");
    }
}
