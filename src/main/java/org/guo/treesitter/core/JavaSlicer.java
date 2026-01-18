package org.guo.treesitter.core;

import org.treesitter.TSNode;
import org.treesitter.TSLanguage;
import org.treesitter.TreeSitterJava;
import org.guo.treesitter.model.LanguageType;

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
    protected boolean isFunctionNode(TSNode node) {
        String type = node.getType();
        return "method_declaration".equals(type) || "constructor_declaration".equals(type);
    }

    @Override
    protected String getFunctionName(TSNode node, byte[] sourceBytes) {
        TSNode nameNode = node.getChildByFieldName("name");
        if (nameNode != null && !nameNode.isNull()) {
            return getNodeText(nameNode, sourceBytes);
        }
        return "anonymous";
    }

    @Override
    public String getFunctionQuery() {
        return "[(method_declaration name: (identifier) @name) (constructor_declaration name: (identifier) @name)] @function";
    }
}
