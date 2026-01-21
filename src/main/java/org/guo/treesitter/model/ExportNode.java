package org.guo.treesitter.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"nodeId", "name", "label", "path", "text", "startLine", "endLine"})
public class ExportNode {
    private String nodeId;
    private String name;
    private String label;
    private String path;
    private String text;
    private int startLine;
    private int endLine;

    public ExportNode() {
    }

    public ExportNode(String nodeId, String name, String label, String path, String text, int startLine, int endLine) {
        this.nodeId = nodeId;
        this.name = name;
        this.label = label;
        this.path = path;
        this.text = text;
        this.startLine = startLine;
        this.endLine = endLine;
    }

    public String getNodeId() {
        return nodeId;
    }

    public String getName() {
        return name;
    }

    public String getLabel() {
        return label;
    }

    public String getPath() {
        return path;
    }

    public String getText() {
        return text;
    }

    public int getStartLine() {
        return startLine;
    }

    public int getEndLine() {
        return endLine;
    }
}
