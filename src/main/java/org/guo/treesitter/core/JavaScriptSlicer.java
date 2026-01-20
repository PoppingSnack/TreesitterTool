package org.guo.treesitter.core;

import org.treesitter.TSNode;
import org.treesitter.TSLanguage;
import org.treesitter.TreeSitterJavascript;
import org.guo.treesitter.model.LanguageType;
import org.guo.treesitter.model.SliceType;

public class JavaScriptSlicer extends AbstractSlicer {

    @Override
    protected LanguageType getLanguageType() {
        return LanguageType.JAVASCRIPT;
    }

    @Override
    protected TSLanguage getLanguage() {
        return new TreeSitterJavascript();
    }

    @Override
    protected boolean shouldSlice(TSNode node) {
        String type = node.getType();
        return "function_declaration".equals(type) || 
               "class_declaration".equals(type) ||
               "method_definition".equals(type) ||
               "generator_function_declaration".equals(type);
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
        if ("class_declaration".equals(type)) {
            return SliceType.CLASS;
        }
        // functions, methods, generators -> FUNCTION
        return SliceType.FUNCTION;
    }

    @Override
    public String getFunctionQuery() {
        return "[(function_declaration name: (identifier) @name) " +
               "(class_declaration name: (identifier) @name) " +
               "(method_definition name: (property_identifier) @name)] @function";
    }
}
