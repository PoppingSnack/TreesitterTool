package org.guo.treesitter.core;

import org.treesitter.TSNode;
import org.treesitter.TSLanguage;
import org.treesitter.TreeSitterGo;
import org.guo.treesitter.model.LanguageType;

public class GoSlicer extends AbstractSlicer {

    @Override
    protected LanguageType getLanguageType() {
        return LanguageType.GO;
    }

    @Override
    protected TSLanguage getLanguage() {
        return new TreeSitterGo();
    }

    @Override
    protected boolean isFunctionNode(TSNode node) {
        String type = node.getType();
        return "function_declaration".equals(type) || "method_declaration".equals(type);
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
        return "[(function_declaration name: (identifier) @name) (method_declaration name: (field_identifier) @name)] @function";
    }
}
