package org.guo.treesitter;

import org.guo.treesitter.service.EmbeddingService;
import org.guo.treesitter.service.ProjectIngestor;
import org.guo.treesitter.service.VectorStore;
import org.guo.treesitter.service.impl.H2VectorStore;
import org.guo.treesitter.service.impl.MockEmbeddingService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class ProjectEmbeddingTest {

    @Test
    public void testProjectIngestionAndSearch() throws IOException {
        // 1. Setup Services
        EmbeddingService embeddingService = new MockEmbeddingService(64);
        H2VectorStore vectorStore = new H2VectorStore();
        
        // Clear previous test data
        vectorStore.clear();
        
        ProjectIngestor ingestor = new ProjectIngestor(embeddingService, vectorStore);
        
        // 2. Ingest TestProject
        Path projectRoot = Paths.get(System.getProperty("user.dir"), "TestProject");
        ingestor.ingest(projectRoot);
        
        // 3. Perform Search
        // We read the actual file content to ensure line endings and whitespace match exactly
        // what was ingested.
        Path queryFilePath = projectRoot.resolve("python_demo/main.py");
        String fileContent = Files.readString(queryFilePath);
        
        // We need to extract the function content just like the slicer did.
        // Instead of re-parsing, let's manually construct the string if we are sure, 
        // or better: let's pick a known slice from the store to test retrieval.
        // But since we want to test "search", let's assume we have the query text.
        // A safer way for this test is to find the "main" function slice from the file content 
        // using the same slicer logic, then use that content to query.
        
        org.guo.treesitter.service.CodeSlicer pythonSlicer = org.guo.treesitter.core.SlicerFactory.getSlicerByExtension("py");
        List<org.guo.treesitter.model.CodeSlice> slices = pythonSlicer.slice(fileContent);
        
        // Find the 'main' function slice
        String queryText = slices.stream()
                .filter(s -> "main".equals(s.getFunctionName()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Could not find main function in python_demo/main.py"))
                .getContent();

        float[] queryVector = embeddingService.embed(queryText);
        
        List<VectorStore.SearchResult> results = vectorStore.search(queryVector, 5);
        
        Assertions.assertFalse(results.isEmpty(), "Search should return results");
        
        // Check if the top result is highly similar (should be 1.0 for exact match)
        VectorStore.SearchResult topResult = results.get(0);
        // Note: Due to floating point arithmetic and JSON serialization/deserialization, 
        // we use a small epsilon. MockEmbeddingService is deterministic.
        // However, if there are multiple functions with the same content (e.g. main in different files),
        // any of them could be top. 
        // Also, the query text is manually constructed here.
        // Let's print details for debugging if it fails again.
        
        System.out.println("Top Search Result:");
        System.out.println("Function: " + topResult.getSlice().getFunctionName());
        System.out.println("Similarity: " + topResult.getSimilarity());
        System.out.println("Content: " + topResult.getSlice().getContent());

        // Verify we found the main function (could be from any language demo that has main)
        Assertions.assertEquals("main", topResult.getSlice().getFunctionName());
        Assertions.assertTrue(topResult.getSimilarity() > 0.99, "Top result should be nearly identical (found: " + topResult.getSimilarity() + ")");
    }
}
