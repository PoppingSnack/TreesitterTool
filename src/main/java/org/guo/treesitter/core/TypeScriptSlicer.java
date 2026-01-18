package org.guo.treesitter.core;

import org.treesitter.TSNode;
import org.treesitter.TSLanguage;
import org.treesitter.TreeSitterTypescript;
import org.guo.treesitter.model.LanguageType;
import org.guo.treesitter.model.SliceType;

public class TypeScriptSlicer extends AbstractSlicer {

    @Override
    protected LanguageType getLanguageType() {
        return LanguageType.TYPESCRIPT;
    }

    @Override
    protected TSLanguage getLanguage() {
        return new TreeSitterTypescript();
    }

    @Override
    protected boolean shouldSlice(TSNode node) {
        String type = node.getType();
        return "function_declaration".equals(type) || 
               "method_definition".equals(type) ||
               "class_declaration".equals(type) ||
               "interface_declaration".equals(type) ||
               "type_alias_declaration".equals(type) ||
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
            case "type_alias_declaration":
                return SliceType.TYPE_ALIAS;
            case "enum_declaration":
                return SliceType.ENUM;
            case "function_declaration":
            case "method_definition":
                return SliceType.FUNCTION;
            default:
                return SliceType.OTHER;
        }
    }

    @Override
    public String getFunctionQuery() {
        return "[(function_declaration name: (identifier) @name) " +
               "(class_declaration name: (type_identifier) @name) " +
               "(interface_declaration name: (type_identifier) @name)] @function";
    }
}
