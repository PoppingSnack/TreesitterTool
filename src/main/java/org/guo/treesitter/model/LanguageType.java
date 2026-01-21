package org.guo.treesitter.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public enum LanguageType {
    PYTHON("py", "python"),
    TYPESCRIPT("ts", "tsx", "typescript"),
    JAVASCRIPT("js", "jsx", "javascript", "mjs", "cjs"),
    JAVA("java"),
    C("c"),
    CPP("cpp", "cc", "cxx", "h", "hpp"),
    GO("go"),
    ARKTS("ets");

    private final Set<String> extensions;

    LanguageType(String... extensions) {
        this.extensions = new HashSet<>(Arrays.asList(extensions));
    }

    public Set<String> getExtensions() {
        return Collections.unmodifiableSet(extensions);
    }

    public static LanguageType fromExtension(String extension) {
        if (extension == null || extension.isEmpty()) {
            throw new IllegalArgumentException("Extension cannot be null or empty");
        }
        String normalizedExt = extension.toLowerCase();
        // Remove leading dot if present
        if (normalizedExt.startsWith(".")) {
            normalizedExt = normalizedExt.substring(1);
        }

        // Special handling for conflicts (e.g. .ts for TypeScript vs ArkTS)
        // By default .ts maps to TYPESCRIPT. 
        // If specific logic is needed (like ArkTS precedence), it can be handled here or by caller.
        // For now, we map based on strict ownership or convention.
        
        for (LanguageType type : values()) {
            if (type.extensions.contains(normalizedExt)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unsupported file extension: " + extension);
    }
}
