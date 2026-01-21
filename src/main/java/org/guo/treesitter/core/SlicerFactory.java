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
        slicers.put(LanguageType.ARKTS, new ArkTSSlicer());
    }

    public static CodeSlicer getSlicer(LanguageType languageType) {
        CodeSlicer slicer = slicers.get(languageType);
        if (slicer == null) {
            throw new IllegalArgumentException("Unsupported language: " + languageType);
        }
        return slicer;
    }

    public static CodeSlicer getSlicerByExtension(String extension) {
        // Special logic for TS/ETS conflict if needed, otherwise delegate to enum
        if ("ets".equalsIgnoreCase(extension)) {
            return getSlicer(LanguageType.ARKTS);
        }
        
        try {
            LanguageType type = LanguageType.fromExtension(extension);
            return getSlicer(type);
        } catch (IllegalArgumentException e) {
            // Re-throw or handle as before
            throw new IllegalArgumentException("Unsupported file extension: " + extension);
        }
    }
}
