# Implement NDJSON Export for Function Slices

This plan outlines the implementation of a new export feature that scans a project, extracts function slices for a specified language, and outputs them to a single NDJSON file with a standardized format.

## 1. Core Logic: `ProjectExporter`
*   **Purpose**: Orchestrate the scanning, slicing, and NDJSON serialization.
*   **Input**: `Path projectRoot`, `LanguageType language`, `Path outputFile`.
*   **Process**:
    1.  Initialize `TreeSitterTool`.
    2.  Scan files matching the language.
    3.  Extract slices (filter for `SliceType.FUNCTION` only).
    4.  Generate `nodeId` (MD5 of relative path + function signature).
    5.  Serialize to NDJSON.

## 2. Model Enhancement: `NodeIdGenerator`
*   Create utility class `NodeIdGenerator`.
*   Method: `generate(String filePath, String functionName, int startLine)`.
*   Logic: MD5 hash of `filePath + ":" + functionName + ":" + startLine`.

## 3. Data Transfer Object: `ExportNode`
*   Create a POJO to match the target JSON structure:
    *   `nodeId`: String
    *   `name`: String
    *   `label`: String (Fixed as "function_definition" or mapped from slice type)
    *   `path`: String (Relative path)
    *   `text`: String
    *   `startLine`: int
    *   `endLine`: int

## 4. CLI / Main Entry Point Update
*   Update `Main.java` or create `ExporterMain.java` to accept CLI arguments:
    *   `--project <path>`
    *   `--lang <language>`
    *   `--out <file>`

## 5. Testing
*   Create `ProjectExporterTest`.
*   Run against `TestProject` for multiple languages.
*   Verify output file content matches NDJSON format.

## Execution Steps
1.  Create `NodeIdGenerator`.
2.  Create `ExportNode` model.
3.  Implement `ProjectExporter` service.
4.  Create Unit Test `ProjectExporterTest`.
5.  (Optional) Expose via CLI.
