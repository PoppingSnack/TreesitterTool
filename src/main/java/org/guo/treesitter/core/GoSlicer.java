package org.guo.treesitter.core;

import org.treesitter.TSNode;
import org.treesitter.TSLanguage;
import org.treesitter.TreeSitterGo;
import org.guo.treesitter.model.LanguageType;
import org.guo.treesitter.model.SliceType;

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
    protected boolean shouldSlice(TSNode node) {
        String type = node.getType();
        return "function_declaration".equals(type) || 
               "method_declaration".equals(type) ||
               "type_declaration".equals(type);
    }

    @Override
    protected String getName(TSNode node, byte[] sourceBytes) {
        String type = node.getType();
        if ("type_declaration".equals(type)) {
            // type_declaration usually has a type_spec child
            // (type_declaration (type_spec name: (type_identifier) ...))
            TSNode typeSpec = node.getChild(0); // usually the first child is type_spec, but let's be safe
            // Go grammar: type_declaration -> "type" type_spec | "type" "(" type_spec* ")"
            // Simple case: "type Foo struct {}"
            
            // Iterate children to find type_spec
            for (int i = 0; i < node.getChildCount(); i++) {
                TSNode child = node.getChild(i);
                if ("type_spec".equals(child.getType())) {
                    TSNode nameNode = child.getChildByFieldName("name");
                    if (nameNode != null) {
                        return getNodeText(nameNode, sourceBytes);
                    }
                }
            }
            return "anonymous_type";
        }
        
        TSNode nameNode = node.getChildByFieldName("name");
        if (nameNode != null && !nameNode.isNull()) {
            return getNodeText(nameNode, sourceBytes);
        }
        return "anonymous";
    }

    @Override
    protected SliceType getSliceType(TSNode node) {
        String type = node.getType();
        if ("type_declaration".equals(type)) {
            // Check inner type to distinguish struct vs interface
             for (int i = 0; i < node.getChildCount(); i++) {
                TSNode child = node.getChild(i);
                if ("type_spec".equals(child.getType())) {
                    TSNode typeNode = child.getChildByFieldName("type");
                    if (typeNode != null) {
                        String innerType = typeNode.getType();
                        if ("struct_type".equals(innerType)) return SliceType.STRUCT;
                        if ("interface_type".equals(innerType)) return SliceType.INTERFACE;
                    }
                }
            }
            return SliceType.STRUCT; // Default to struct or generic type
        }
        return SliceType.FUNCTION;
    }

    @Override
    public String getFunctionQuery() {
        return "[(function_declaration name: (identifier) @name) (method_declaration name: (field_identifier) @name)] @function";
    }
}
