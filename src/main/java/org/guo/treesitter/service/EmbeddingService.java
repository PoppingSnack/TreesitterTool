package org.guo.treesitter.service;

import org.guo.treesitter.model.CodeSlice;

public interface EmbeddingService {
    void embed(CodeSlice slice);
}
