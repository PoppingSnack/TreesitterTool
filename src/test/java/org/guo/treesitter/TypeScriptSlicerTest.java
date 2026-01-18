package org.guo.treesitter;

import org.guo.treesitter.core.SlicerFactory;
import org.guo.treesitter.core.TypeScriptSlicer;
import org.guo.treesitter.model.CodeSlice;
import org.guo.treesitter.model.LanguageType;
import org.guo.treesitter.query.TreeSitterQueryExecutor;
import org.guo.treesitter.service.CodeSlicer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

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
        boolean foundGetUsers = false;
        
        for (CodeSlice slice : slices) {
            if ("addUser".equals(slice.getFunctionName())) foundAddUser = true;
            if ("getUsers".equals(slice.getFunctionName())) foundGetUsers = true;
        }
        
        Assertions.assertTrue(foundAddUser, "Should find addUser");
        Assertions.assertTrue(foundGetUsers, "Should find getUsers");
    }

    @Test
    public void testQueryApi() throws IOException {
        Path path = Paths.get(getTestProjectRoot(), "index.ts");
        String code = Files.readString(path);

        TypeScriptSlicer slicer = new TypeScriptSlicer();
        TreeSitterQueryExecutor executor = new TreeSitterQueryExecutor(
            new org.treesitter.TreeSitterTypescript(),
            LanguageType.TYPESCRIPT
        );

        String query = slicer.getFunctionQuery();
        List<Map<String, CodeSlice>> results = executor.execute(code, query);
        
        Assertions.assertEquals(1, results.size());
        Map<String, CodeSlice> match = results.get(0);
        Assertions.assertEquals("main", match.get("name").getContent());
    }
}
