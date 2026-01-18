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
            default:
                throw new IllegalArgumentException("Unsupported file extension: " + extension);
        }
    }
}
