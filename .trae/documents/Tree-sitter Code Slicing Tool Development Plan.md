# Tree-sitter Code Slicing Tool Plan

This plan outlines the development of a Java-based tool using `io.github.bonede` Tree-sitter bindings to slice Python and TypeScript code at the function level.

## 1. Project Configuration (`pom.xml`)
- Add `io.github.bonede:tree-sitter` (Core).
- Add `io.github.bonede:tree-sitter-python` (Python Grammar).
- Add `io.github.bonede:tree-sitter-typescript` (TypeScript Grammar).
- Add `junit:junit` (Testing).
- Set compiler level to Java 17.

## 2. Core Architecture
- **Package Structure**: `org.guo.treesitter`
- **Domain Models**:
    - `CodeSlice`: Stores content, function name, start/end lines/bytes, language.
    - `LanguageType`: Enum for `PYTHON`, `TYPESCRIPT`.
- **Interfaces**:
    - `CodeSlicer`: Main interface with `List<CodeSlice> slice(String code)`.
    - `EmbeddingService`: Interface for future embedding implementation.
- **Implementation Strategy**:
    - **Design Pattern**: Strategy Pattern for language-specific slicing logic. Factory Pattern for creating slicers.
    - **Performance**: Use `TreeCursor` for efficient AST traversal (avoiding full object overhead). Use `ThreadLocal` for `Parser` instances to ensure thread safety while maintaining performance.

## 3. Implementation Details
- **`AbstractSlicer`**: Base class handling common Tree-sitter initialization and cursor management.
- **`PythonSlicer`**:
    - Target Node: `function_definition`.
    - Logic: Extract function name from child identifiers.
- **`TypeScriptSlicer`**:
    - Target Nodes: `function_declaration`, `method_definition`, `lexical_declaration` (for arrow functions assigned to vars). *Initial scope: `function_declaration` and `method_definition`.*
- **`SlicerFactory`**: Returns the correct slicer based on file extension or language type.

## 4. Demo & Testing
- **Test Project Setup**:
    - Create directory: `d:\Project\WORKER\TreesitterTool\TestProject`
    - Create `demo.py`: Python script with functions and classes.
    - Create `demo.ts`: TypeScript file with functions, interfaces, and classes.
- **Unit Tests**:
    - `PythonSlicerTest`: Validate slicing of `demo.py`.
    - `TypeScriptSlicerTest`: Validate slicing of `demo.ts`.
    - Verify correct line numbers and content extraction.

## 5. Execution Steps
1.  **Update Dependencies**: Modify `pom.xml`.
2.  **Scaffold Code**: Create interfaces and models.
3.  **Implement Slicers**: Write the core parsing logic using `TreeCursor`.
4.  **Create Demo Files**: Populate `TestProject`.
5.  **Write Tests**: Implement JUnit tests.
6.  **Verify**: Run tests and demonstrate output.
