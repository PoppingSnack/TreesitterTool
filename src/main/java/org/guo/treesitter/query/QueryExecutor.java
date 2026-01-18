package org.guo.treesitter.query;

import org.guo.treesitter.model.CodeSlice;
import java.util.List;
import java.util.Map;

public interface QueryExecutor {
    /**
     * Execute a query on the given code.
     * @param code The source code.
     * @param queryStr The S-expression query string.
     * @return List of matches, where each match is a map of Capture Name -> CodeSlice.
     */
    List<Map<String, CodeSlice>> execute(String code, String queryStr);

    /**
     * Execute a query with dynamic replacements.
     * @param code The source code.
     * @param queryTemplate The query template with placeholders (e.g., {{name}}).
     * @param replacements Map of placeholder names to values.
     * @return List of matches.
     */
    List<Map<String, CodeSlice>> execute(String code, String queryTemplate, Map<String, String> replacements);
}
