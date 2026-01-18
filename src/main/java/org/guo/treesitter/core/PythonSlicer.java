package org.guo.treesitter.core;

import org.treesitter.TSNode;
import org.treesitter.TSLanguage;
import org.treesitter.TreeSitterPython;
import org.guo.treesitter.model.LanguageType;

public class PythonSlicer extends AbstractSlicer {

    @Override
    protected LanguageType getLanguageType() {
        return LanguageType.PYTHON;
    }

    @Override
    protected TSLanguage getLanguage() {
        return new TreeSitterPython();
    }

    @Override
    protected boolean isFunctionNode(TSNode node) {
        return "function_definition".equals(node.getType());
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
