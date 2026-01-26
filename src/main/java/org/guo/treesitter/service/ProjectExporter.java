package org.guo.treesitter.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.guo.treesitter.TreeSitterTool;
import org.guo.treesitter.model.CodeSlice;
import org.guo.treesitter.model.ExportNode;
import org.guo.treesitter.model.LanguageType;
import org.guo.treesitter.model.SliceType;
import org.guo.treesitter.utils.NodeIdGenerator;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.ArrayList;

public class ProjectExporter {

    private final TreeSitterTool treeSitterTool;
    private final ObjectMapper objectMapper;

    public ProjectExporter() {
        this.treeSitterTool = new TreeSitterTool();
        this.objectMapper = new ObjectMapper();
    }

    public void export(Path projectRoot, LanguageType language, Path outputFile) throws IOException {
        // Ensure output directory exists
        if (outputFile.getParent() != null) {
            Files.createDirectories(outputFile.getParent());
        }

        try (BufferedWriter writer = Files.newBufferedWriter(outputFile)) {
            List<CodeSlice> slices = treeSitterTool.analyzeProject(projectRoot, language);
            
            for (CodeSlice slice : slices) {
                // Filter only functions as requested
                if (slice.getType() == SliceType.FUNCTION) {
                    
                    // Note: CodeSlice currently doesn't store the file path it came from.
                    // We need to update TreeSitterTool/CodeSlice or handle path association here.
                    // Since TreeSitterTool.analyzeProject returns a flat list of CodeSlices, 
                    // we lost the file context if CodeSlice doesn't have it.
                    
                    // ISSUE: CodeSlice needs 'filePath' field.
                    // Let's assume we will update CodeSlice to include filePath, 
                    // OR we need to reimplement the walking logic here to keep track of files.
                    // Reimplementing walking is safer to avoid breaking existing API right now, 
                    // but updating CodeSlice is better long term.
                    // Given the prompt "打通输入到输出的所有流程", I should probably update CodeSlice or wrapping it.
                    // But I cannot easily change CodeSlice constructor everywhere in one go without breaking tests.
                    
                    // Alternative: Modify TreeSitterTool to return Map<Path, List<CodeSlice>> or similar?
                    // Or let's just do the file walking here and call analyzeFile.
                }
            }
        }
    }
    
    // Re-implementing walk with export logic to have access to file paths
    // Optimized with parallel processing for better performance, especially for ArkTS
    public void exportWithWalk(Path projectRoot, LanguageType language, Path outputFile) throws IOException {
         if (outputFile.getParent() != null) {
            Files.createDirectories(outputFile.getParent());
        }

        try (BufferedWriter writer = Files.newBufferedWriter(outputFile);
             Stream<Path> stream = Files.walk(projectRoot)) {
            
            // Collect files first to facilitate parallel processing
            List<Path> files = stream.filter(Files::isRegularFile).collect(Collectors.toList());
            
            files.parallelStream().forEach(file -> {
                try {
                    // Check extension
                    String fileName = file.getFileName().toString();
                    int lastDotIndex = fileName.lastIndexOf(".");
                    if (lastDotIndex == -1) return;
                    
                    String ext = fileName.substring(lastDotIndex + 1).toLowerCase();
                    
                    if (language.getExtensions().contains(ext)) {
                         List<CodeSlice> slices = treeSitterTool.analyzeFile(file);
                         if (slices == null || slices.isEmpty()) return;

                         Path relativePath = projectRoot.relativize(file);
                         String relativePathStr = relativePath.toString().replace("\\", "/");
                         StringBuilder fileOutputBuffer = new StringBuilder();
                         
                         for (CodeSlice slice : slices) {
                             if (slice.getType() == SliceType.FUNCTION) {
                                 String nodeId = NodeIdGenerator.generate(
                                     relativePathStr, // Normalize path separators
                                     slice.getName(), 
                                     slice.getStartLine()
                                 );
                                 
                                 ExportNode node = new ExportNode(
                                     nodeId,
                                     slice.getName(),
                                     "function_definition",
                                     relativePathStr,
                                     slice.getContent(),
                                     slice.getStartLine(),
                                     slice.getEndLine()
                                 );
                                 
                                 fileOutputBuffer.append(objectMapper.writeValueAsString(node));
                                 fileOutputBuffer.append(System.lineSeparator());
                             }
                         }
                         
                         // Synchronized write per file to minimize contention
                         if (fileOutputBuffer.length() > 0) {
                             synchronized (writer) {
                                 writer.write(fileOutputBuffer.toString());
                             }
                         }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }
}
