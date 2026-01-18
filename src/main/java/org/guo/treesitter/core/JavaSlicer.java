package org.guo.treesitter.core;

import org.treesitter.TSNode;
import org.treesitter.TSLanguage;
import org.treesitter.TreeSitterJava;
import org.guo.treesitter.model.LanguageType;
import org.guo.treesitter.model.SliceType;

public class JavaSlicer extends AbstractSlicer {

    @Override
    protected LanguageType getLanguageType() {
        return LanguageType.JAVA;
    }

    @Override
    protected TSLanguage getLanguage() {
        return new TreeSitterJava();
    }

    @Override
    protected boolean shouldSlice(TSNode node) {
        String type = node.getType();
        return "method_declaration".equals(type) || 
               "constructor_declaration".equals(type) ||
               "class_declaration".equals(type) ||
               "interface_declaration".equals(type) ||
               "enum_declaration".equals(type);
    }

    @Override
    protected String getName(TSNode node, byte[] sourceBytes) {
        TSNode nameNode = node.getChildByFieldName("name");
        if (nameNode != null && !nameNode.isNull()) {
            return getNodeText(nameNode, sourceBytes);
        }
        return "anonymous";
    }

    @Override
    protected SliceType getSliceType(TSNode node) {
        String type = node.getType();
        switch (type) {
            case "class_declaration":
                return SliceType.CLASS;
            case "interface_declaration":
                return SliceType.INTERFACE;
            case "enum_declaration":
                return SliceType.ENUM;
            case "method_declaration":
            case "constructor_declaration":
                return SliceType.FUNCTION;
            default:
                return SliceType.OTHER;
        }
    }

    @Override
    public String getFunctionQuery() {
        // Updated query to include classes and interfaces for query-based extraction if needed
        return "[(method_declaration name: (identifier) @name) " +
               "(constructor_declaration name: (identifier) @name) " +
               "(class_declaration name: (identifier) @name) " +
               "(interface_declaration name: (identifier) @name)] @function";
    }
}
