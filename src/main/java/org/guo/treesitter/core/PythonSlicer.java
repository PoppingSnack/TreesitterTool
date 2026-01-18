package org.guo.treesitter.core;

import org.treesitter.TSNode;
import org.treesitter.TSLanguage;
import org.treesitter.TreeSitterPython;
import org.guo.treesitter.model.LanguageType;
import org.guo.treesitter.model.SliceType;

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
    protected boolean shouldSlice(TSNode node) {
        String type = node.getType();
        return "function_definition".equals(type) || 
               "class_definition".equals(type);
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
        if ("class_definition".equals(type)) {
            return SliceType.CLASS;
        } else if ("function_definition".equals(type)) {
            return SliceType.FUNCTION;
        }
        return SliceType.OTHER;
    }

    @Override
    public String getFunctionQuery() {
        return "[(function_definition name: (identifier) @name) (class_definition name: (identifier) @name)] @function";
    }
}
