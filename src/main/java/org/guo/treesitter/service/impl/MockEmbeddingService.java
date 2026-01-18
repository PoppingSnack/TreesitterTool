package org.guo.treesitter.service.impl;

import org.guo.treesitter.service.EmbeddingService;
import java.util.Random;

/**
 * A mock embedding service for testing and demonstration purposes.
 * It generates deterministic "pseudo-random" vectors based on the input text hash.
 */
public class MockEmbeddingService implements EmbeddingService {

    private final int dimension;

    public MockEmbeddingService(int dimension) {
        this.dimension = dimension;
    }

    public MockEmbeddingService() {
        this(128); // Default dimension
    }

    @Override
    public float[] embed(String text) {
        float[] vector = new float[dimension];
        // Use text hashcode to seed random for determinism
        long seed = text.hashCode();
        Random random = new Random(seed);
        
        double norm = 0;
        for (int i = 0; i < dimension; i++) {
            vector[i] = random.nextFloat() - 0.5f;
            norm += vector[i] * vector[i];
        }
        
        // Normalize vector
        norm = Math.sqrt(norm);
        if (norm > 0) {
            for (int i = 0; i < dimension; i++) {
                vector[i] /= norm;
            }
        }
        
        return vector;
    }

    @Override
    public int getDimension() {
        return dimension;
    }
}
