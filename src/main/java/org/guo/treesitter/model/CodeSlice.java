package org.guo.treesitter.model;

public class CodeSlice {
    private String content;
    private String functionName;
    private int startLine;
    private int endLine;
    private LanguageType language;

    public CodeSlice(String content, String functionName, int startLine, int endLine, LanguageType language) {
        this.content = content;
        this.functionName = functionName;
        this.startLine = startLine;
        this.endLine = endLine;
        this.language = language;
    }

    public String getContent() {
        return content;
    }

    public String getFunctionName() {
        return functionName;
    }

    public int getStartLine() {
        return startLine;
    }

    public int getEndLine() {
        return endLine;
    }

    public LanguageType getLanguage() {
        return language;
    }

    @Override
    public String toString() {
        return "CodeSlice{" +
                "functionName='" + functionName + '\'' +
                ", startLine=" + startLine +
                ", endLine=" + endLine +
                ", language=" + language +
                '}';
    }
}
