# TreesitterTool 开发与扩展指南

本项目的目标是提供一个高性能、可扩展的代码切片工具，基于 Tree-sitter 技术，目前支持 Python 和 TypeScript 的函数级切片。

## 1. 项目结构

```
src/main/java/org/guo/treesitter/
├── core/
│   ├── AbstractSlicer.java      # 切片逻辑的基类，处理通用的 Tree-sitter 操作
│   ├── PythonSlicer.java        # Python 语言的具体实现
│   ├── TypeScriptSlicer.java    # TypeScript 语言的具体实现
│   └── SlicerFactory.java       # 工厂类，用于根据语言或文件后缀获取对应的 Slicer
├── model/
│   ├── CodeSlice.java           # 数据模型，表示一个代码切片（函数）
│   └── LanguageType.java        # 支持的语言枚举
└── service/
    ├── CodeSlicer.java          # 切片服务接口
    └── EmbeddingService.java    # (预留) Embedding 服务接口
```

## 2. 核心设计

### 2.1 AbstractSlicer
`AbstractSlicer` 是核心抽象类，它封装了 Tree-sitter 的解析流程。为了提高性能，它使用了：
*   **ThreadLocal<TSParser>**: 复用 Parser 实例，避免频繁创建销毁带来的开销。
*   **TSTreeCursor**: 使用游标（Cursor）遍历 AST，比对象式遍历（`node.getChild()`）性能更高，内存占用更小。

### 2.2 SlicerFactory
使用工厂模式管理不同语言的 Slicer 实例。对外提供 `getSlicerByExtension(String extension)` 方法，方便调用方集成。

## 3. 如何扩展新语言

假设你要添加 **Java** 语言的支持，请遵循以下步骤：

### 第一步：添加 Maven 依赖
在 `pom.xml` 中添加对应语言的 Tree-sitter 绑定库。
> **注意**：请确保使用的 grammar 版本与 core (`tree-sitter`) 版本兼容。当前项目 Core 版本为 `0.25.3`。

```xml
<dependency>
    <groupId>io.github.bonede</groupId>
    <artifactId>tree-sitter-java</artifactId>
    <version>0.23.2</version> <!-- 请查找匹配的最新版本 -->
</dependency>
```

### 第二步：更新 LanguageType
在 `src/main/java/org/guo/treesitter/model/LanguageType.java` 中添加枚举值：

```java
public enum LanguageType {
    PYTHON,
    TYPESCRIPT,
    JAVA // 新增
}
```

### 第三步：实现具体的 Slicer
创建 `JavaSlicer` 类，继承 `AbstractSlicer`，并实现以下抽象方法：

```java
package org.guo.treesitter.core;

import org.treesitter.TSNode;
import org.treesitter.TSLanguage;
import org.treesitter.TreeSitterJava; // 具体的类名可能不同，请参考 jar 包
import org.guo.treesitter.model.LanguageType;

public class JavaSlicer extends AbstractSlicer {

    @Override
    protected LanguageType getLanguageType() {
        return LanguageType.JAVA;
    }

    @Override
    protected TSLanguage getLanguage() {
        // 返回该语言的单例或新实例
        return new TreeSitterJava(); 
    }

    @Override
    protected boolean isFunctionNode(TSNode node) {
        // 定义哪些节点被视为"函数"
        // 对于 Java，通常是 method_declaration, constructor_declaration
        String type = node.getType();
        return "method_declaration".equals(type) || "constructor_declaration".equals(type);
    }

    @Override
    protected String getFunctionName(TSNode node, byte[] sourceBytes) {
        // 从节点中提取函数名
        TSNode nameNode = node.getChildByFieldName("name");
        if (nameNode != null && !nameNode.isNull()) {
            return getNodeText(nameNode, sourceBytes);
        }
        return "anonymous";
    }
}
```

### 第四步：注册到工厂
在 `src/main/java/org/guo/treesitter/core/SlicerFactory.java` 中注册新类：

```java
static {
    slicers.put(LanguageType.PYTHON, new PythonSlicer());
    slicers.put(LanguageType.TYPESCRIPT, new TypeScriptSlicer());
    slicers.put(LanguageType.JAVA, new JavaSlicer()); // 新增
}

// 在 getSlicerByExtension 中添加后缀映射
public static CodeSlicer getSlicerByExtension(String extension) {
    switch (extension.toLowerCase()) {
        // ... 其他语言
        case "java":
            return getSlicer(LanguageType.JAVA);
    }
}
```

### 第五步：添加测试用例
在 `src/test/java/org/guo/treesitter/` 下创建 `JavaSlicerTest.java`，验证解析结果是否符合预期。

## 4. 常用开发命令

*   **运行测试**: `mvn clean test`
*   **构建项目**: `mvn clean package`

## 5. 调试技巧
*   使用 `node.getType()` 查看当前节点类型。
*   使用 `TSNode` 的 `toString()` 方法（或 Tree-sitter 的 `printDotGraphs`）可以打印 AST 结构，帮助确定 `isFunctionNode` 需要匹配的类型名称。
