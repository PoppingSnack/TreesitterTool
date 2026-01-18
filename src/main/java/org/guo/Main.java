package org.guo;

import org.guo.treesitter.core.SlicerFactory;
import org.guo.treesitter.model.CodeSlice;
import org.guo.treesitter.service.CodeSlicer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java -jar treesitter-tool.jar <file-path>");
            return;
        }

        String filePath = args[0];
        try {
            Path path = Paths.get(filePath);
            String extension = getFileExtension(filePath);
            if (extension.isEmpty()) {
                System.err.println("Error: File must have an extension.");
                return;
            }
            
            String content = Files.readString(path);

            CodeSlicer slicer = SlicerFactory.getSlicerByExtension(extension);
            List<CodeSlice> slices = slicer.slice(content);

            System.out.println("Found " + slices.size() + " functions:");
            for (CodeSlice slice : slices) {
                System.out.println("--------------------------------------------------");
                System.out.println("Function: " + slice.getFunctionName());
                System.out.println("Language: " + slice.getLanguage());
                System.out.println("Line: " + slice.getStartLine() + " - " + slice.getEndLine());
                System.out.println("Content:\n" + slice.getContent());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getFileExtension(String filePath) {
        int lastIndexOf = filePath.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return ""; // empty extension
        }
        return filePath.substring(lastIndexOf + 1);
    }
}
