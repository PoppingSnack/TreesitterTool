# Embedding & Vector Search System

本文档详细介绍了 TreesitterTool 中的代码 Embedding 存储与检索系统的设计与使用。

## 1. 系统架构 (Architecture)

该模块旨在实现对项目代码的语义化理解、存储和检索。它通过 AST (Tree-sitter) 提取代码片段（函数/方法），将其转换为向量（Embedding），并存储在本地数据库中以供相似度查询。

### 核心组件

*   **`ProjectIngestor`**: 系统的入口。负责遍历项目目录，识别支持的源代码文件，调用切片器提取函数，并协调 Embedding 生成与存储。
*   **`EmbeddingService`**: 抽象接口，负责将文本转换为浮点数向量。
*   **`VectorStore`**: 抽象接口，负责向量及元数据的持久化存储和检索。
*   **`CodeSlice`**: 基础数据模型，表示一个代码片段（名称、类型、内容、位置信息等）。

## 2. 接口定义 (Interfaces)

### 2.1 EmbeddingService

```java
public interface EmbeddingService {
    // 将文本转换为向量
    float[] embed(String text);
    
    // 获取向量维度
    int getDimension();
}
```

*   **当前实现**: `MockEmbeddingService`
    *   这是一个用于测试的实现，它基于输入文本的哈希值生成确定性的伪随机向量。
    *   **注意**: 在生产环境中，应替换为调用 OpenAI、HuggingFace 或本地 Python 模型服务的实现。

### 2.2 VectorStore

```java
public interface VectorStore {
    // 保存代码切片及其向量
    void save(CodeSlice slice, float[] vector);

    // 根据查询向量搜索最相似的代码片段
    List<SearchResult> search(float[] queryVector, int limit);
}
```

*   **当前实现**: `H2VectorStore`
    *   基于 H2 Database (嵌入式模式) 实现。
    *   **存储**: 使用 `JSON` 格式将向量数组存储在文本字段中。
    *   **检索**: 由于 H2 原生不支持向量索引，目前采用 **Brute-Force (暴力扫描)** 方式，即在内存中计算查询向量与所有存储向量的余弦相似度 (Cosine Similarity)。
    *   **适用场景**: 中小型项目（数万个函数级别）。对于大规模项目，建议迁移至 Milvus, Qdrant 或 pgvector。

## 3. 数据库设计 (Database Schema)

使用 H2 数据库，表名为 `code_slices`。

| 字段名 | 类型 | 描述 |
| :--- | :--- | :--- |
| `id` | IDENTITY | 主键 |
| `content` | CLOB | 代码片段的完整文本内容 |
| `function_name` | VARCHAR | 代码片段名称 (函数名/类名等) |
| `start_line` | INT | 开始行号 |
| `end_line` | INT | 结束行号 |
| `language` | VARCHAR | 编程语言类型 (JAVA, PYTHON, etc.) |
| `vector` | JSON | 序列化后的向量数组 (e.g., `[0.1, -0.5, ...]`) |

## 4. 使用指南 (Usage Guide)

### 4.1 引入依赖

确保 `pom.xml` 中包含以下依赖：

```xml
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <version>2.2.224</version>
</dependency>
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.15.2</version>
</dependency>
```

### 4.2 代码入库 (Ingestion)

```java
// 1. 初始化服务
EmbeddingService embeddingService = new MockEmbeddingService(128); // 128维
VectorStore vectorStore = new H2VectorStore(); // 默认存储在 ./data/vectors
ProjectIngestor ingestor = new ProjectIngestor(embeddingService, vectorStore);

// 2. 指定项目根目录
Path projectRoot = Paths.get("d:/path/to/your/project");

// 3. 执行入库（扫描 -> 切片 -> Embedding -> 存储）
ingestor.ingest(projectRoot);
```

### 4.3 语义搜索 (Semantic Search)

```java
// 1. 准备查询文本
String queryText = "def main(): print('hello')";

// 2. 生成查询向量
float[] queryVector = embeddingService.embed(queryText);

// 3. 执行搜索 (Top 5)
List<VectorStore.SearchResult> results = vectorStore.search(queryVector, 5);

// 4. 处理结果
for (VectorStore.SearchResult result : results) {
    System.out.println("Function: " + result.getSlice().getFunctionName());
    System.out.println("Similarity: " + result.getSimilarity());
    System.out.println("Content: \n" + result.getSlice().getContent());
}
```

## 5. 扩展与优化

1.  **替换 Embedding 模型**:
    *   实现 `EmbeddingService` 接口，在 `embed` 方法中调用外部 API (如 OpenAI `text-embedding-3-small`)。
2.  **升级向量数据库**:
    *   实现 `VectorStore` 接口，对接专业的向量数据库（如 Milvus）。
    *   或者使用支持向量插件的 PostgreSQL (pgvector)。
3.  **增量更新**:
    *   目前的 `ProjectIngestor` 是全量扫描。可以通过记录文件修改时间或 Hash 值来实现增量更新，避免重复 Embedding（节省成本）。
