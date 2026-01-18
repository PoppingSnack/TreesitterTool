package org.guo.treesitter.service;

import org.guo.treesitter.core.SlicerFactory;
import org.guo.treesitter.model.CodeSlice;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

public class ProjectIngestor {

    private final EmbeddingService embeddingService;
    private final VectorStore vectorStore;

    public ProjectIngestor(EmbeddingService embeddingService, VectorStore vectorStore) {
        this.embeddingService = embeddingService;
        this.vectorStore = vectorStore;
    }

    public void ingest(Path projectRoot) throws IOException {
        Files.walkFileTree(projectRoot, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                processFile(file);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private void processFile(Path file) {
        String fileName = file.getFileName().toString();
        String extension = getFileExtension(fileName);
        
        try {
            CodeSlicer slicer = SlicerFactory.getSlicerByExtension(extension);
            String content = Files.readString(file);
            List<CodeSlice> slices = slicer.slice(content);
            
            for (CodeSlice slice : slices) {
                float[] vector = embeddingService.embed(slice.getContent());
                vectorStore.save(slice, vector);
            }
            System.out.println("Ingested: " + fileName + " (" + slices.size() + " functions)");
            
        } catch (IllegalArgumentException e) {
            // Unsupported file type, skip
        } catch (IOException e) {
            System.err.println("Error reading file: " + file);
            e.printStackTrace();
        }
    }

    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot == -1) return "";
        return fileName.substring(lastDot + 1);
    }
}
