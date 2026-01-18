package org.guo.treesitter.service;

public interface EmbeddingService {
    /**
     * Converts a text string into a vector (array of floats).
     *
     * @param text The input text (e.g., function content).
     * @return The embedding vector.
     */
    float[] embed(String text);
    
    /**
     * Returns the dimension of the vectors produced by this service.
     */
    int getDimension();
}
