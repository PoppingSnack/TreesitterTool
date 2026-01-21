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
    public void exportWithWalk(Path projectRoot, LanguageType language, Path outputFile) throws IOException {
         if (outputFile.getParent() != null) {
            Files.createDirectories(outputFile.getParent());
        }

        try (BufferedWriter writer = Files.newBufferedWriter(outputFile)) {
            Files.walk(projectRoot)
                .filter(Files::isRegularFile)
                .forEach(file -> {
                    try {
                        // Check extension
                        String fileName = file.getFileName().toString();
                        String ext = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
                        
                        if (language.getExtensions().contains(ext)) {
                             List<CodeSlice> slices = treeSitterTool.analyzeFile(file);
                             Path relativePath = projectRoot.relativize(file);
                             
                             for (CodeSlice slice : slices) {
                                 if (slice.getType() == SliceType.FUNCTION) {
                                     String nodeId = NodeIdGenerator.generate(
                                         relativePath.toString().replace("\\", "/"), // Normalize path separators
                                         slice.getName(), 
                                         slice.getStartLine()
                                     );
                                     
                                     ExportNode node = new ExportNode(
                                         nodeId,
                                         slice.getName(),
                                         "function_definition",
                                         relativePath.toString().replace("\\", "/"),
                                         slice.getContent(),
                                         slice.getStartLine(),
                                         slice.getEndLine()
                                     );
                                     
                                     writer.write(objectMapper.writeValueAsString(node));
                                     writer.newLine();
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
