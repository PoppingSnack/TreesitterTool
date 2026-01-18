# Expand Code Slicing Scope for LLM Context

To better support LLM-based project analysis, we need to capture high-level structural information beyond just functions. This includes Classes, Interfaces, Structs, and Enums.

## 1. Data Model Enhancements
*   **Create `SliceType` Enum**: Define types `FUNCTION`, `CLASS`, `INTERFACE`, `STRUCT`, `ENUM`.
*   **Update `CodeSlice`**:
    *   Rename `functionName` -> `name`.
    *   Add `type` field of type `SliceType`.

## 2. Core Slicer Refactoring
*   **`AbstractSlicer`**:
    *   Rename `isFunctionNode` -> `shouldSlice`.
    *   Rename `getFunctionName` -> `getName`.
    *   Add abstract/hook `getSliceType(TSNode node)` to determine the type.

## 3. Language-Specific Implementations
*   **Java (`JavaSlicer`)**:
    *   Add support for: `class_declaration`, `interface_declaration`, `enum_declaration`.
*   **Python (`PythonSlicer`)**:
    *   Add support for: `class_definition`.
*   **TypeScript (`TypeScriptSlicer`)**:
    *   Add support for: `class_declaration`, `interface_declaration`, `type_alias_declaration`.
*   **Go (`GoSlicer`)**:
    *   Add support for: `type_declaration` (handling `struct_type` and `interface_type`).
*   **C (`CSlicer`)**:
    *   Add support for: `struct_specifier`, `enum_specifier`.
*   **C++ (`CppSlicer`)**:
    *   Add support for: `class_specifier`, `struct_specifier`.

## 4. Testing
*   Update `*SlicerTest.java` for all languages.
*   Verify that parsing `TestProject` demo files now returns slices for classes/structs.
*   Example: `JavaSlicerTest` should assert that "Calculator" class is found, not just "add" method.

## 5. Execution Steps
1.  **Refactor Model**: Create `SliceType` and update `CodeSlice`.
2.  **Refactor Abstract Base**: Update `AbstractSlicer` logic.
3.  **Update Slicers**: Implement new logic for each language.
4.  **Update Tests**: Verify expansion.
