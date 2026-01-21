# Create Unified Entry Point for Tree-sitter Tool

To provide a convenient and unified interface for LLMs and developers, we will introduce a central service class `TreeSitterTool` (or `CodeAnalysisEngine`) and enhance the language type management.

## 1. Enhance `LanguageType` Enum
*   Refactor `LanguageType` to include a list of supported file extensions for each language.
*   Add a static method `fromExtension(String ext)` to resolve language from file suffix.

## 2. Refactor `SlicerFactory`
*   Simplify `getSlicerByExtension` to use the new `LanguageType` lookup logic, reducing code duplication.

## 3. Implement Unified Entry Point: `CodeAnalysisEngine`
*   **Package**: `org.guo.treesitter`
*   **Methods**:
    *   `analyzeProject(Path root)`: Scan and analyze all supported files in a project.
    *   `analyzeProject(Path root, LanguageType language)`: Analyze only files of a specific language.
    *   `analyzeFile(Path file)`: Analyze a single file (auto-detect language).
    *   `analyzeSource(String source, LanguageType language)`: Analyze raw source code string.
    *   `analyzeSource(InputStream input, LanguageType language)`: Analyze input stream.

## 4. Testing
*   Create `CodeAnalysisEngineTest` to verify all entry points using the existing `TestProject`.

## 5. Documentation
*   Update `README.md` or create new usage docs to highlight this main entry point.

## Execution Steps
1.  Update `LanguageType.java`.
2.  Refactor `SlicerFactory.java`.
3.  Create `CodeAnalysisEngine.java`.
4.  Create `CodeAnalysisEngineTest.java`.
