package org.guo.treesitter.service;

import org.guo.treesitter.model.CodeSlice;
import java.util.List;

public interface CodeSlicer {
    List<CodeSlice> slice(String code);
}
