package org.guo.treesitter.core;

import org.treesitter.TSNode;
import org.treesitter.TSParser;
import org.treesitter.TSTree;
import org.treesitter.TSTreeCursor;
import org.treesitter.TSLanguage;
import org.guo.treesitter.model.CodeSlice;
import org.guo.treesitter.model.LanguageType;
import org.guo.treesitter.service.CodeSlicer;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractSlicer implements CodeSlicer {

    // ThreadLocal to reuse Parser instances for performance
    private static final ThreadLocal<TSParser> parserThreadLocal = ThreadLocal.withInitial(TSParser::new);

    protected abstract LanguageType getLanguageType();
    protected abstract TSLanguage getLanguage(); 
    protected abstract boolean isFunctionNode(TSNode node);
    protected abstract String getFunctionName(TSNode node, byte[] sourceBytes);

    @Override
    public List<CodeSlice> slice(String code) {
        List<CodeSlice> slices = new ArrayList<>();
        if (code == null || code.isEmpty()) {
            return slices;
        }

        TSParser parser = parserThreadLocal.get();
        // Reset parser might be needed if reusing? setLanguage usually resets or prepares.
        parser.setLanguage(getLanguage());

        // Parse bytes to handle multi-byte characters correctly with byte offsets
        byte[] sourceBytes = code.getBytes(StandardCharsets.UTF_8);

        // parseString takes (oldTree, string). Pass null for oldTree.
        TSTree tree = parser.parseString(null, code);
        
        try {
            TSNode root = tree.getRootNode();
            TSTreeCursor cursor = new TSTreeCursor(root);
            traverse(cursor, sourceBytes, slices);
        } catch (Exception e) {
            e.printStackTrace();
        } 
        // No explicit close() method on Tree or Cursor in this binding, relies on Cleaner.
        
        return slices;
    }

    private void traverse(TSTreeCursor cursor, byte[] sourceBytes, List<CodeSlice> slices) {
        boolean reachedRoot = false;
        while (!reachedRoot) {
            TSNode currentNode = cursor.currentNode();
            
            if (isFunctionNode(currentNode)) {
                String funcName = getFunctionName(currentNode, sourceBytes);
                
                // Convert 0-based row to 1-based line number
                int startLine = currentNode.getStartPoint().getRow() + 1;
                int endLine = currentNode.getEndPoint().getRow() + 1;
                
                int startByte = currentNode.getStartByte();
                int endByte = currentNode.getEndByte();
                
                // Extract content using bytes to ensure correct offsets
                String content = new String(sourceBytes, startByte, endByte - startByte, StandardCharsets.UTF_8);

                slices.add(new CodeSlice(content, funcName, startLine, endLine, getLanguageType()));
            }

            if (cursor.gotoFirstChild()) {
                continue;
            }

            if (cursor.gotoNextSibling()) {
                continue;
            }

            boolean retracing = true;
            while (retracing) {
                if (!cursor.gotoParent()) {
                    retracing = false;
                    reachedRoot = true;
                } else {
                    if (cursor.gotoNextSibling()) {
                        retracing = false;
                    }
                }
            }
        }
    }
    
    protected String getNodeText(TSNode node, byte[] sourceBytes) {
        if (node == null || node.isNull()) return null;
        int start = node.getStartByte();
        int end = node.getEndByte();
        return new String(sourceBytes, start, end - start, StandardCharsets.UTF_8);
    }
}
