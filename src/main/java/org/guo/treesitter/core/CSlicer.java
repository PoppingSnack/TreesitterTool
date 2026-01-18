package org.guo.treesitter.core;

import org.treesitter.TSNode;
import org.treesitter.TSLanguage;
import org.treesitter.TreeSitterC;
import org.guo.treesitter.model.LanguageType;

public class CSlicer extends AbstractSlicer {

    @Override
    protected LanguageType getLanguageType() {
        return LanguageType.C;
    }

    @Override
    protected TSLanguage getLanguage() {
        return new TreeSitterC();
    }

    @Override
    protected boolean isFunctionNode(TSNode node) {
        return "function_definition".equals(node.getType());
    }

    @Override
    protected String getFunctionName(TSNode node, byte[] sourceBytes) {
        // In C, the name is inside declarator -> function_declarator -> declarator -> identifier
        // Or direct declarator -> identifier. It can be complex due to pointers.
        // We will try to find the 'declarator' child.
        TSNode declarator = node.getChildByFieldName("declarator");
        if (declarator == null || declarator.isNull()) {
            return "anonymous";
        }
        
        // Simple heuristic: drill down until identifier
        TSNode current = declarator;
        while (current != null && !current.isNull()) {
            if ("identifier".equals(current.getType())) {
                return getNodeText(current, sourceBytes);
            }
            if ("function_declarator".equals(current.getType())) {
                current = current.getChildByFieldName("declarator");
                continue;
            }
            if ("pointer_declarator".equals(current.getType())) {
                current = current.getChildByFieldName("declarator");
                continue;
            }
            if ("parenthesized_declarator".equals(current.getType())) {
                current = current.getChildByFieldName("declarator");
                continue;
            }
            break;
        }
        
        return "unknown_c_function";
    }

    @Override
    public String getFunctionQuery() {
        // Matches standard function definitions
        // Capture the declarator structure to extract name later if needed, but for simplicity
        // we try to match the identifier deep inside.
        // This query attempts to find the identifier nested within declarators.
        return "(function_definition declarator: (_ declarator: (identifier) @name)) @function";
    }
}
