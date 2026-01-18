package org.guo.treesitter.core;

import org.treesitter.TSNode;
import org.treesitter.TSLanguage;
import org.treesitter.TreeSitterC;
import org.guo.treesitter.model.LanguageType;
import org.guo.treesitter.model.SliceType;

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
    protected boolean shouldSlice(TSNode node) {
        String type = node.getType();
        return "function_definition".equals(type) ||
               "struct_specifier".equals(type) ||
               "enum_specifier".equals(type);
    }

    @Override
    protected String getName(TSNode node, byte[] sourceBytes) {
        String type = node.getType();
        
        if ("struct_specifier".equals(type) || "enum_specifier".equals(type)) {
            TSNode nameNode = node.getChildByFieldName("name");
            if (nameNode != null && !nameNode.isNull()) {
                return getNodeText(nameNode, sourceBytes);
            }
            return "anonymous";
        }

        // function_definition -> declarator -> function_declarator -> declarator -> identifier
        TSNode declarator = node.getChildByFieldName("declarator");
        if (declarator != null) {
            return extractNameFromDeclarator(declarator, sourceBytes);
        }
        return "anonymous";
    }

    private String extractNameFromDeclarator(TSNode declarator, byte[] sourceBytes) {
        if (declarator == null || declarator.isNull()) return "anonymous";
        String type = declarator.getType();
        
        if ("identifier".equals(type)) {
            return getNodeText(declarator, sourceBytes);
        }
        
        if ("function_declarator".equals(type) || "pointer_declarator".equals(type) || "parenthesized_declarator".equals(type)) {
             TSNode childDeclarator = declarator.getChildByFieldName("declarator");
             return extractNameFromDeclarator(childDeclarator, sourceBytes);
        }
        
        return "anonymous";
    }

    @Override
    protected SliceType getSliceType(TSNode node) {
        String type = node.getType();
        if ("struct_specifier".equals(type)) return SliceType.STRUCT;
        if ("enum_specifier".equals(type)) return SliceType.ENUM;
        return SliceType.FUNCTION;
    }

    @Override
    public String getFunctionQuery() {
        return "(function_definition declarator: (function_declarator declarator: (identifier) @name)) @function";
    }
}
