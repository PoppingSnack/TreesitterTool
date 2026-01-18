# Tree-sitter 新手指南 & io.github.bonede 库使用手册

## 1. Tree-sitter 简介

**Tree-sitter** 是一个高性能的增量解析生成器工具和库。它能为源文件构建具体的语法树（Concrete Syntax Tree），并在源文件修改时高效地更新语法树。

### 核心特性
*   **高性能**：用 C 编写，速度极快，适合在文本编辑器中实时运行。
*   **容错性**：即使代码有语法错误，也能生成有效的语法树。
*   **统一 API**：不同语言（Python, Java, Go 等）使用相同的 API 进行解析和遍历。

### 核心概念
*   **Parser（解析器）**：有状态的对象，用于配置语言并解析文本。
*   **Language（语言）**：定义了特定编程语言的语法规则（如 Python 语法、Java 语法）。
*   **Tree（树）**：解析结果，包含整个 AST。
*   **Node（节点）**：AST 中的一个节点，代表语法结构（如函数定义、标识符、if 语句）。
*   **Cursor（游标）**：用于高效遍历树的有状态对象。

---

## 2. io.github.bonede 库详解

`io.github.bonede` 是 Tree-sitter 的 Java 绑定（Bindings），它通过 JNI (Java Native Interface) 调用底层的 C 库。

### 2.1 依赖引入 (Maven)

根据你的项目配置，核心依赖如下：

```xml
<!-- 核心库：提供 Parser, Node, Tree 等通用类 -->
<dependency>
    <groupId>io.github.bonede</groupId>
    <artifactId>tree-sitter</artifactId>
    <version>0.25.3</version>
</dependency>

<!-- 语言包：提供特定语言的语法定义 -->
<dependency>
    <groupId>io.github.bonede</groupId>
    <artifactId>tree-sitter-python</artifactId>
    <version>0.23.4</version>
</dependency>
```

### 2.2 核心类与方法说明

以下类均位于 `org.treesitter` 包下。

#### 1. `TSParser` (解析器)
*   `new TSParser()`: 创建解析器实例。
*   `setLanguage(TSLanguage language)`: 设置要解析的语言。
*   `parseString(TSTree oldTree, String source)`: 解析源码字符串。如果做增量解析，传入 `oldTree`；否则传 `null`。
    *   **注意**：返回的 `TSTree` 包含 native 资源。

#### 2. `TSLanguage` (语言接口)
*   这是一个接口，具体的语言实现类（如 `TreeSitterPython`）实现了它。
*   主要用于传给 `parser.setLanguage()`。

#### 3. `TSTree` (语法树)
*   `getRootNode()`: 获取树的根节点 (`TSNode`)。
*   资源管理：该库使用了 `java.lang.ref.Cleaner` 机制，通常不需要手动调用 delete，但为了性能，在极其频繁调用的场景下需注意对象生命周期。

#### 4. `TSNode` (节点)
*   `getType()`: 获取节点类型字符串（如 `function_definition`, `identifier`）。
*   `getStartByte()`, `getEndByte()`: 获取节点在源码中的字节偏移量。
*   `getStartPoint()`, `getEndPoint()`: 获取行号和列号。
*   `getChildCount()`: 获取子节点数量。
*   `getChild(int index)`: 获取指定索引的子节点。
*   `getChildByFieldName(String name)`: 获取指定字段名的子节点（非常有用！例如获取函数的 `name` 或 `body`）。
*   `isNull()`: 检查节点是否为空（无效节点）。

#### 5. `TSTreeCursor` (游标)
**性能优化的关键**。相比于递归调用 `node.getChild()` 创建大量 Node 对象，Cursor 允许你在树上移动而不产生过多垃圾对象。
*   `new TSTreeCursor(TSNode node)`: 在指定节点上初始化游标。
*   `gotoFirstChild()`: 移动到第一个子节点。
*   `gotoNextSibling()`: 移动到下一个兄弟节点.
*   `gotoParent()`: 移动到父节点。
*   `currentNode()`: 获取当前位置的 Node 对象。

---

## 3. 实战代码示例

以下代码展示了如何使用 `io.github.bonede` 库解析一段 Python 代码并提取函数名。

### 3.1 基础解析流程

```java
import org.treesitter.*;
import java.nio.charset.StandardCharsets;

public class TreeSitterDemo {
    public static void main(String[] args) {
        // 1. 初始化 Parser
        TSParser parser = new TSParser();
        
        // 2. 设置语言 (使用 Python)
        // 注意：具体类名取决于 io.github.bonede 对应语言包的实现，通常是 TreeSitterXxx
        TSLanguage pythonLang = new TreeSitterPython(); 
        parser.setLanguage(pythonLang);

        // 3. 准备代码
        String code = "def my_func(x):\n    return x + 1";
        // 建议转换为 byte 数组，因为 Tree-sitter 内部使用字节偏移
        byte[] sourceBytes = code.getBytes(StandardCharsets.UTF_8);

        // 4. 解析
        TSTree tree = parser.parseString(null, code);
        
        // 5. 获取根节点并遍历
        TSNode root = tree.getRootNode();
        printTree(root, "", sourceBytes);
    }

    // 递归打印树结构 (简单方式，性能一般)
    private static void printTree(TSNode node, String indent, byte[] sourceBytes) {
        String type = node.getType();
        int start = node.getStartByte();
        int end = node.getEndByte();
        
        // 提取节点对应的源码文本
        String text = new String(sourceBytes, start, end - start, StandardCharsets.UTF_8);
        
        System.out.println(indent + type + " [" + start + "-" + end + "]: " + text.replace("\n", "\\n"));

        for (int i = 0; i < node.getChildCount(); i++) {
            printTree(node.getChild(i), indent + "  ", sourceBytes);
        }
    }
}
```

### 3.2 高性能遍历 (使用 Cursor)

在本项目 `AbstractSlicer` 中，我们使用了 `TSTreeCursor`，这是推荐的做法。

```java
private void traverseWithCursor(TSNode rootNode) {
    TSTreeCursor cursor = new TSTreeCursor(rootNode);
    
    boolean visitedRoot = false;
    while (!visitedRoot) {
        TSNode currentNode = cursor.currentNode();
        System.out.println("Visiting: " + currentNode.getType());

        // 尝试进入子节点
        if (cursor.gotoFirstChild()) {
            continue;
        }

        // 尝试进入兄弟节点
        if (cursor.gotoNextSibling()) {
            continue;
        }

        // 回溯到父节点，并寻找父节点的兄弟节点
        boolean retracing = true;
        while (retracing) {
            if (!cursor.gotoParent()) {
                retracing = false;
                visitedRoot = true; // 回到了起点，结束
            } else {
                if (cursor.gotoNextSibling()) {
                    retracing = false; // 找到父节点的兄弟，继续向下
                }
            }
        }
    }
    // Cursor 也会由 Cleaner 管理，无需显式 close
}
```

## 4. 常见问题 (FAQ)

### Q1: `TSNode` 获取的文本为什么有时候不准？
**A**: Tree-sitter 的 Node 存储的是字节偏移量 (`byte range`)。Java 的 `String.substring` 使用的是字符索引 (`char index`)。对于包含中文等 **多字节字符** 的代码，直接用 `substring` 会导致偏移错误。
**解决方案**：始终使用 `code.getBytes(StandardCharsets.UTF_8)`，并根据 Node 的 `startByte` 和 `endByte` 从字节数组中构建字符串。

### Q2: 如何查找特定的语法节点？
**A**: 
1.  参考官方 grammar 仓库（如 `tree-sitter-python` 的 GitHub）里的 `grammar.js` 文件。
2.  或者编写一个小程序打印出 AST 结构，观察你需要提取的代码对应的节点类型 (`type`)。
3.  使用 `node.getChildByFieldName("name")` 可以快速获取具名子节点，比遍历所有子节点更准确。

### Q3: 为什么找不到 `org.treesitter.TSParser` 类？
**A**: 确保 `pom.xml` 中正确引入了 `io.github.bonede:tree-sitter` 依赖，并且版本号正确。

### Q4: 版本兼容性
**A**: `tree-sitter` 核心库与各个语言的 grammar 库（如 `tree-sitter-python`）之间存在 ABI 兼容性要求。如果遇到 `UnsatisfiedLinkError` 或版本不匹配警告，请尝试升级或降级语言包版本以匹配核心库版本。目前本项目配置的 `0.25.x` 核心库与 `0.23.x` 语言库是兼容的。
