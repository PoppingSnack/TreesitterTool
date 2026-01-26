package org.guo.treesitter;

import org.guo.treesitter.model.LanguageType;
import org.guo.treesitter.service.ProjectExporter;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;

/**
 * Performance test for ProjectExporter.
 * Can be used to benchmark ArkTS parsing speed.
 */
public class ArkTSPerformanceTest {

    @Test
    public void benchmarkArkTSExport() throws IOException {
        // --- Configuration ---
        // Change this path to your real ArkTS project root
        // Defaulting to current dir for demonstration, but you should point to a real project
        String projectPathStr = "D:\\Project\\WORKER\\TreesitterTool"; 
        LanguageType language = LanguageType.ARKTS;
        String outputFileName = "benchmark_arkts_output.ndjson";
        // ---------------------

        Path projectRoot = Paths.get(projectPathStr);
        Path outputDir = Paths.get("target", "performance_test");
        Path outputFile = outputDir.resolve(outputFileName);

        if (!Files.exists(projectRoot)) {
            System.out.println("Project root does not exist: " + projectRoot);
            System.out.println("Please configure 'projectPathStr' in the test code.");
            return;
        }

        // Ensure output directory exists
        if (Files.exists(outputFile)) {
            Files.delete(outputFile);
        }
        Files.createDirectories(outputDir);

        ProjectExporter exporter = new ProjectExporter();

        System.out.println("Starting export benchmark...");
        System.out.println("Project Root: " + projectRoot);
        System.out.println("Language: " + language);
        System.out.println("Output File: " + outputFile);

        // Warm-up (optional, maybe skip for long running tasks)
        // System.out.println("Warming up...");
        // exporter.exportWithWalk(projectRoot, language, outputFile);

        System.out.println("Executing...");
        long startMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        Instant start = Instant.now();

        exporter.exportWithWalk(projectRoot, language, outputFile);

        Instant end = Instant.now();
        long endMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        long lines = 0;
        if (Files.exists(outputFile)) {
            lines = Files.lines(outputFile).count();
        }

        Duration timeElapsed = Duration.between(start, end);
        
        System.out.println("--------------------------------------------------");
        System.out.println("Benchmark Finished");
        System.out.println("--------------------------------------------------");
        System.out.println("Time Taken: " + timeElapsed.toMillis() + " ms (" + timeElapsed.toSeconds() + " s)");
        System.out.println("Total Slices Exported: " + lines);
        if (lines > 0) {
            System.out.println("Average Time per Slice: " + (timeElapsed.toMillis() / (double) lines) + " ms");
        }
        System.out.println("Memory Used (Approx): " + ((endMem - startMem) / 1024 / 1024) + " MB");
        System.out.println("Output written to: " + outputFile.toAbsolutePath());
        System.out.println("--------------------------------------------------");
    }
}
