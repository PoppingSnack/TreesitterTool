package org.guo.treesitter.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.guo.treesitter.model.CodeSlice;
import org.guo.treesitter.model.LanguageType;
import org.guo.treesitter.service.VectorStore;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A simple VectorStore implementation using H2 Database.
 * Since H2 doesn't support vector search natively, this implementation
 * performs a full scan and calculates cosine similarity in memory (Brute Force).
 * This is suitable for small to medium-sized projects (thousands of functions).
 */
public class H2VectorStore implements VectorStore {

    private static final String JDBC_URL = "jdbc:h2:./data/vectors;DB_CLOSE_DELAY=-1";
    private static final String USER = "sa";
    private static final String PASSWORD = "";
    private final ObjectMapper objectMapper = new ObjectMapper();

    public H2VectorStore() {
        initializeDb();
    }

    private void initializeDb() {
        try (Connection conn = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {
            
            stmt.execute("CREATE TABLE IF NOT EXISTS code_slices (" +
                    "id IDENTITY PRIMARY KEY, " +
                    "content CLOB, " +
                    "function_name VARCHAR(255), " +
                    "start_line INT, " +
                    "end_line INT, " +
                    "language VARCHAR(50), " +
                    "vector JSON" + // Store vector as JSON array
                    ")");
            
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize H2 database", e);
        }
    }

    @Override
    public void save(CodeSlice slice, float[] vector) {
        String sql = "INSERT INTO code_slices (content, function_name, start_line, end_line, language, vector) VALUES (?, ?, ?, ?, ?, ? FORMAT JSON)";
        
        try (Connection conn = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, slice.getContent());
            pstmt.setString(2, slice.getFunctionName());
            pstmt.setInt(3, slice.getStartLine());
            pstmt.setInt(4, slice.getEndLine());
            pstmt.setString(5, slice.getLanguage().name());
            pstmt.setString(6, objectMapper.writeValueAsString(vector));
            
            pstmt.executeUpdate();
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to save vector", e);
        }
    }

    @Override
    public List<SearchResult> search(float[] queryVector, int limit) {
        List<SearchResult> allResults = new ArrayList<>();
        String sql = "SELECT content, function_name, start_line, end_line, language, vector FROM code_slices";
        
        try (Connection conn = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                String content = rs.getString("content");
                String funcName = rs.getString("function_name");
                int startLine = rs.getInt("start_line");
                int endLine = rs.getInt("end_line");
                String langStr = rs.getString("language");
                String vectorJson = rs.getString("vector");
                
                float[] vector = objectMapper.readValue(vectorJson, float[].class);
                double similarity = cosineSimilarity(queryVector, vector);
                
                CodeSlice slice = new CodeSlice(content, funcName, startLine, endLine, LanguageType.valueOf(langStr));
                allResults.add(new SearchResult(slice, similarity));
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Search failed", e);
        }
        
        // Sort by similarity descending
        allResults.sort((a, b) -> Double.compare(b.getSimilarity(), a.getSimilarity()));
        
        // Return top 'limit'
        if (allResults.size() > limit) {
            return allResults.subList(0, limit);
        }
        return allResults;
    }
    
    public void clear() {
        try (Connection conn = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM code_slices");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to clear DB", e);
        }
    }

    private double cosineSimilarity(float[] v1, float[] v2) {
        if (v1.length != v2.length) return 0.0;
        
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        
        for (int i = 0; i < v1.length; i++) {
            dotProduct += v1[i] * v2[i];
            normA += v1[i] * v1[i];
            normB += v2[i] * v2[i];
        }
        
        if (normA == 0 || normB == 0) return 0.0;
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
