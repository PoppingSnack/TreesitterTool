package org.guo.treesitter;

import org.guo.treesitter.core.SlicerFactory;
import org.guo.treesitter.model.CodeSlice;
import org.guo.treesitter.model.LanguageType;
import org.guo.treesitter.service.CodeSlicer;
import org.guo.treesitter.utils.FileUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

/**
 * Unified entry point for Tree-sitter based code analysis.
 */
public class TreeSitterTool {

    /**
     * Analyze a single file by auto-detecting language from extension.
     * @param file Path to the source file
     * @return List of code slices
     */
    public List<CodeSlice> analyzeFile(Path file) throws IOException {
        String fileName = file.getFileName().toString();
        String extension = getFileExtension(fileName);
        
        try {
            CodeSlicer slicer = SlicerFactory.getSlicerByExtension(extension);
            // Use FileUtils to detect encoding and read content
            String content = FileUtils.readFile(file);
            return slicer.slice(content);
        } catch (IllegalArgumentException e) {
            // Unsupported extension
            System.err.println("Skipping unsupported file: " + fileName);
            return new ArrayList<>();
        }
    }

    /**
     * Analyze raw source code string with specified language.
     * @param sourceCode The source code content
     * @param language The language type
     * @return List of code slices
     */
    public List<CodeSlice> analyzeSource(String sourceCode, LanguageType language) {
        CodeSlicer slicer = SlicerFactory.getSlicer(language);
        return slicer.slice(sourceCode);
    }

    /**
     * Analyze input stream with specified language.
     * @param inputStream The source input stream
     * @param language The language type
     * @return List of code slices
     */
    public List<CodeSlice> analyzeSource(InputStream inputStream, LanguageType language) throws IOException {
        String sourceCode = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        return analyzeSource(sourceCode, language);
    }

    /**
     * Analyze all supported files in a project directory.
     * @param projectRoot Root directory of the project
     * @return List of code slices from all supported files
     */
    public List<CodeSlice> analyzeProject(Path projectRoot) throws IOException {
        List<CodeSlice> allSlices = new ArrayList<>();
        Files.walkFileTree(projectRoot, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                allSlices.addAll(analyzeFile(file));
                return FileVisitResult.CONTINUE;
            }
        });
        return allSlices;
    }

    /**
     * Analyze files of a specific language in a project directory.
     * @param projectRoot Root directory of the project
     * @param language Target language to analyze
     * @return List of code slices
     */
    public List<CodeSlice> analyzeProject(Path projectRoot, LanguageType language) throws IOException {
        List<CodeSlice> allSlices = new ArrayList<>();
        CodeSlicer slicer = SlicerFactory.getSlicer(language);
        
        Files.walkFileTree(projectRoot, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (isLanguageFile(file, language)) {
                    // Use FileUtils to detect encoding
                    String content = FileUtils.readFile(file);
                    allSlices.addAll(slicer.slice(content));
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return allSlices;
    }

    private boolean isLanguageFile(Path file, LanguageType language) {
        String extension = getFileExtension(file.getFileName().toString());
        return language.getExtensions().contains(extension.toLowerCase());
    }

    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot == -1) return "";
        return fileName.substring(lastDot + 1);
    }
}
