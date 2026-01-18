package org.guo.treesitter.service;

import org.guo.treesitter.model.CodeSlice;
import java.util.List;

public interface VectorStore {
    /**
     * Saves a code slice along with its embedding vector.
     */
    void save(CodeSlice slice, float[] vector);

    /**
     * Searches for the most similar code slices to the given query vector.
     *
     * @param queryVector The query embedding vector.
     * @param limit The maximum number of results to return.
     * @return A list of SearchResult objects.
     */
    List<SearchResult> search(float[] queryVector, int limit);

    class SearchResult {
        private final CodeSlice slice;
        private final double similarity;

        public SearchResult(CodeSlice slice, double similarity) {
            this.slice = slice;
            this.similarity = similarity;
        }

        public CodeSlice getSlice() {
            return slice;
        }

        public double getSimilarity() {
            return similarity;
        }
    }
}
