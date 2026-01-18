package org.guo.treesitter.core;

import org.treesitter.TSNode;
import org.treesitter.TSLanguage;
import org.treesitter.TreeSitterTypescript;
import org.guo.treesitter.model.LanguageType;

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
    protected boolean isFunctionNode(TSNode node) {
        String type = node.getType();
        return "function_declaration".equals(type) || "method_definition".equals(type);
    }

    @Override
    protected String getFunctionName(TSNode node, byte[] sourceBytes) {
        TSNode nameNode = node.getChildByFieldName("name");
        if (nameNode != null && !nameNode.isNull()) {
            return getNodeText(nameNode, sourceBytes);
        }
        return "anonymous";
    }
}
