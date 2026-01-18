package org.guo.treesitter.query;

import org.guo.treesitter.core.PythonSlicer;
import org.guo.treesitter.core.TypeScriptSlicer;
import org.guo.treesitter.model.CodeSlice;
import org.guo.treesitter.model.LanguageType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QueryExecutorTest {

    @Test
    public void testPythonQueryExecution() {
        String code = "def hello():\n    pass";
        PythonSlicer slicer = new PythonSlicer(); // Used to get language config
        
        // We need to expose the TSLanguage from Slicer or just new it up.
        // But AbstractSlicer has getLanguage() as protected. 
        // For testing, let's create a subclass or make it public? 
        // Actually TreeSitterQueryExecutor constructor takes TSLanguage.
        // We can just create a new TreeSitterPython() for the test since we know it.
        
        TreeSitterQueryExecutor executor = new TreeSitterQueryExecutor(
            new org.treesitter.TreeSitterPython(), 
            LanguageType.PYTHON
        );

        String query = "(function_definition name: (identifier) @name) @function";
        
        List<Map<String, CodeSlice>> results = executor.execute(code, query);
        
        Assertions.assertEquals(1, results.size());
        Map<String, CodeSlice> match = results.get(0);
        
        Assertions.assertTrue(match.containsKey("name"));
        Assertions.assertTrue(match.containsKey("function"));
        
        Assertions.assertEquals("hello", match.get("name").getContent());
        // Check full function content
        Assertions.assertTrue(match.get("function").getContent().contains("def hello():"));
    }

    @Test
    public void testTypeScriptQueryExecution() {
        String code = "function greet() { return 'hi'; }";
        
        TreeSitterQueryExecutor executor = new TreeSitterQueryExecutor(
            new org.treesitter.TreeSitterTypescript(), 
            LanguageType.TYPESCRIPT
        );

        String query = "(function_declaration name: (identifier) @name) @function";
        
        List<Map<String, CodeSlice>> results = executor.execute(code, query);
        
        Assertions.assertEquals(1, results.size());
        Map<String, CodeSlice> match = results.get(0);
        
        Assertions.assertEquals("greet", match.get("name").getContent());
    }

    @Test
    public void testDynamicQueryBuilder() {
        String template = "(function_definition name: (identifier) @name (#eq? @name \"{{targetName}}\")) @function";
        Map<String, String> replacements = new HashMap<>();
        replacements.put("targetName", "target_func");
        
        String result = QueryBuilder.build(template, replacements);
        Assertions.assertEquals("(function_definition name: (identifier) @name (#eq? @name \"target_func\")) @function", result);
    }
}
