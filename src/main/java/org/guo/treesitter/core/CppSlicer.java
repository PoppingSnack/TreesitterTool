package org.guo.treesitter.core;

import org.treesitter.TSNode;
import org.treesitter.TSLanguage;
import org.treesitter.TreeSitterCpp;
import org.guo.treesitter.model.LanguageType;

public class CppSlicer extends AbstractSlicer {

    @Override
    protected LanguageType getLanguageType() {
        return LanguageType.CPP;
    }

    @Override
    protected TSLanguage getLanguage() {
        return new TreeSitterCpp();
    }

    @Override
    protected boolean isFunctionNode(TSNode node) {
        return "function_definition".equals(node.getType());
    }

    @Override
    protected String getFunctionName(TSNode node, byte[] sourceBytes) {
        TSNode declarator = node.getChildByFieldName("declarator");
        if (declarator == null || declarator.isNull()) {
            return "anonymous";
        }

        // Similar to C but handles C++ specifics like scoped identifiers (MyClass::method)
        TSNode current = declarator;
        while (current != null && !current.isNull()) {
            if ("identifier".equals(current.getType()) || "field_identifier".equals(current.getType())) {
                return getNodeText(current, sourceBytes);
            }
            if ("qualified_identifier".equals(current.getType())) {
                // Return the full qualified name e.g. Class::Method
                return getNodeText(current, sourceBytes);
            }
            if ("function_declarator".equals(current.getType()) || 
                "pointer_declarator".equals(current.getType()) || 
                "reference_declarator".equals(current.getType())) {
                current = current.getChildByFieldName("declarator");
                continue;
            }
            break;
        }
        return "unknown_cpp_function";
    }

    @Override
    public String getFunctionQuery() {
        // Matches function definitions. 
        // Note: C++ grammar is complex. This query matches common cases.
        return "(function_definition declarator: (_ declarator: [(identifier) (qualified_identifier)] @name)) @function";
    }
}
