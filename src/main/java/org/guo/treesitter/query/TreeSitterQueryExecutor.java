package org.guo.treesitter.query;

import org.guo.treesitter.model.CodeSlice;
import org.guo.treesitter.model.LanguageType;
import org.treesitter.*;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TreeSitterQueryExecutor implements QueryExecutor {

    private final TSLanguage language;
    private final LanguageType languageType;

    public TreeSitterQueryExecutor(TSLanguage language, LanguageType languageType) {
        this.language = language;
        this.languageType = languageType;
    }

    @Override
    public List<Map<String, CodeSlice>> execute(String code, String queryStr) {
        List<Map<String, CodeSlice>> results = new ArrayList<>();
        if (code == null || queryStr == null) {
            return results;
        }

        try {
            // 1. Initialize Parser
            TSParser parser = new TSParser();
            parser.setLanguage(language);

            // 2. Parse Code
            TSTree tree = parser.parseString(null, code);
            TSNode root = tree.getRootNode();

            // 3. Create Query
            TSQuery query = new TSQuery(language, queryStr);

            // 4. Execute Query
            TSQueryCursor cursor = new TSQueryCursor();
            cursor.exec(query, root);

            // 5. Iterate Matches
            TSQueryMatch match = new TSQueryMatch();
            byte[] sourceBytes = code.getBytes(StandardCharsets.UTF_8);

            while (cursor.nextMatch(match)) {
                Map<String, CodeSlice> captureMap = new HashMap<>();
                TSQueryCapture[] captures = match.getCaptures();
                
                for (TSQueryCapture capture : captures) {
                    TSNode node = capture.getNode();
                    int captureIndex = capture.getIndex();
                    String captureName = query.getCaptureNameForId(captureIndex);
                    
                    CodeSlice slice = createSliceFromNode(node, sourceBytes, captureName);
                    captureMap.put(captureName, slice);
                }
                results.add(captureMap);
            }
            
            // Resources are managed by Cleaner, but for long running apps explicit management might be needed
            // Here we rely on GC/Cleaner as per binding design

        } catch (Exception e) {
            e.printStackTrace();
            // In a real app, log error or throw generic exception
        }
        
        return results;
    }

    @Override
    public List<Map<String, CodeSlice>> execute(String code, String queryTemplate, Map<String, String> replacements) {
        String finalQuery = QueryBuilder.build(queryTemplate, replacements);
        return execute(code, finalQuery);
    }

    private CodeSlice createSliceFromNode(TSNode node, byte[] sourceBytes, String name) {
        int startByte = node.getStartByte();
        int endByte = node.getEndByte();
        
        int startLine = node.getStartPoint().getRow() + 1;
        int endLine = node.getEndPoint().getRow() + 1;
        
        String content = new String(sourceBytes, startByte, endByte - startByte, StandardCharsets.UTF_8);
        
        // Function name is not strictly applicable for all captures, so we use the capture name as a placeholder
        // or leave it null/empty. Here we use captureName for tracking.
        return new CodeSlice(content, name, startLine, endLine, languageType);
    }
}
