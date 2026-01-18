package org.guo.treesitter.model;

public class CodeSlice {
    private String content;
    private String name; // Renamed from functionName
    private int startLine;
    private int endLine;
    private LanguageType language;
    private SliceType type; // New field

    public CodeSlice(String content, String name, int startLine, int endLine, LanguageType language, SliceType type) {
        this.content = content;
        this.name = name;
        this.startLine = startLine;
        this.endLine = endLine;
        this.language = language;
        this.type = type;
    }

    // Backward compatibility constructor (defaults to FUNCTION)
    public CodeSlice(String content, String name, int startLine, int endLine, LanguageType language) {
        this(content, name, startLine, endLine, language, SliceType.FUNCTION);
    }

    public String getContent() {
        return content;
    }

    public String getName() {
        return name;
    }

    // Alias for backward compatibility if needed, though we should update callers
    public String getFunctionName() {
        return name;
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

    public SliceType getType() {
        return type;
    }

    @Override
    public String toString() {
        return "CodeSlice{" +
                "name='" + name + '\'' +
                ", type=" + type +
                ", startLine=" + startLine +
                ", endLine=" + endLine +
                ", language=" + language +
                '}';
    }
}
