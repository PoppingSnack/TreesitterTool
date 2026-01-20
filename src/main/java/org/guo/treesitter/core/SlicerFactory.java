package org.guo.treesitter.core;

import org.guo.treesitter.model.LanguageType;
import org.guo.treesitter.service.CodeSlicer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SlicerFactory {

    private static final Map<LanguageType, CodeSlicer> slicers = new ConcurrentHashMap<>();

    static {
        slicers.put(LanguageType.PYTHON, new PythonSlicer());
        slicers.put(LanguageType.TYPESCRIPT, new TypeScriptSlicer());
        slicers.put(LanguageType.JAVA, new JavaSlicer());
        slicers.put(LanguageType.C, new CSlicer());
        slicers.put(LanguageType.CPP, new CppSlicer());
        slicers.put(LanguageType.GO, new GoSlicer());
        slicers.put(LanguageType.JAVASCRIPT, new JavaScriptSlicer());
    }

    public static CodeSlicer getSlicer(LanguageType languageType) {
        CodeSlicer slicer = slicers.get(languageType);
        if (slicer == null) {
            throw new IllegalArgumentException("Unsupported language: " + languageType);
        }
        return slicer;
    }

    public static CodeSlicer getSlicerByExtension(String extension) {
        switch (extension.toLowerCase()) {
            case "py":
            case "python":
                return getSlicer(LanguageType.PYTHON);
            case "ts":
            case "typescript":
                return getSlicer(LanguageType.TYPESCRIPT);
            case "java":
                return getSlicer(LanguageType.JAVA);
            case "c":
                return getSlicer(LanguageType.C);
            case "cpp":
            case "cc":
            case "cxx":
            case "h":
            case "hpp":
                return getSlicer(LanguageType.CPP);
            case "go":
                return getSlicer(LanguageType.GO);
            case "js":
            case "javascript":
                return getSlicer(LanguageType.JAVASCRIPT);
            default:
                throw new IllegalArgumentException("Unsupported file extension: " + extension);
        }
    }
}
