# 查询功能开发指南

本文档面向开发者，介绍如何在本项目中开发和扩展查询功能，包括代码结构、三方库 API 使用以及动态查询的实现。

## 1. 代码结构与核心类

本项目的查询模块位于 `org.guo.treesitter.query` 包下。

| 类/接口 | 说明 |
| :--- | :--- |
| **`QueryExecutor`** | 核心接口，定义了 `execute` 方法，返回 `List<Map<String, CodeSlice>>`。 |
| **`TreeSitterQueryExecutor`** | 基于 `io.github.bonede` 库的具体实现。负责管理 Parser、Query 和 Cursor 的生命周期。 |
| **`QueryBuilder`** | 工具类，用于处理动态查询模板（替换 `{{key}}` 占位符）。 |

## 2. 如何开发一个新的查询

### 2.1 编写查询语句 (S-expression)

首先，你需要根据目标语言的 grammar 编写查询语句。
**技巧**：可以使用 Tree-sitter 的 CLI 工具或在线 Playground 调试查询语句。

**示例 (Java 方法提取)**:
```lisp
(method_declaration
  name: (identifier) @method_name
  parameters: (formal_parameters)
  body: (block)
) @method_body
```

### 2.2 调用 QueryExecutor

在代码中，你需要获取对应语言的 `TSLanguage` 实例，并创建 Executor。

```java
// 1. 准备环境
TSLanguage javaLang = new TreeSitterJava(); // 假设已引入 java 绑定库
TreeSitterQueryExecutor executor = new TreeSitterQueryExecutor(javaLang, LanguageType.JAVA);

// 2. 执行查询
String queryStr = "(method_declaration name: (identifier) @name) @function";
List<Map<String, CodeSlice>> results = executor.execute(sourceCode, queryStr);

// 3. 处理结果
for (Map<String, CodeSlice> match : results) {
    CodeSlice nameSlice = match.get("name");
    System.out.println("Found method: " + nameSlice.getContent());
}
```

### 2.3 使用动态查询

如果查询条件是动态的（例如：查找名为 "main" 的函数），可以使用模板：

```java
String template = "(function_definition name: (identifier) @name (#eq? @name \"{{targetFunc}}\")) @function";
Map<String, String> params = new HashMap<>();
params.put("targetFunc", "main");

List<Map<String, CodeSlice>> results = executor.execute(code, template, params);
```

## 3. 三方库 (io.github.bonede) API 详解

`TreeSitterQueryExecutor` 底层依赖 `io.github.bonede` 提供的以下核心类：

### 3.1 `org.treesitter.TSQuery`
*   **构造函数**: `new TSQuery(TSLanguage lang, String source)`
    *   *注意*：如果查询语法错误，会抛出异常。
*   **方法**:
    *   `getCaptureNameForId(int id)`: 根据捕获索引获取名称（如 "name"）。
    *   `getPatternCount()`: 获取模式数量。

### 3.2 `org.treesitter.TSQueryCursor`
*   **作用**: 有状态的游标，用于在节点上执行查询。
*   **方法**:
    *   `exec(TSQuery query, TSNode node)`: 在指定节点（通常是 root）上启动查询。
    *   `nextMatch(TSQueryMatch match)`: 移动到下一个匹配项。返回 `false` 表示遍历结束。

### 3.3 `org.treesitter.TSQueryMatch`
*   **作用**: 代表一次成功的模式匹配。
*   **方法**:
    *   `getCaptures()`: 返回 `TSQueryCapture[]` 数组。注意，一次匹配可能包含多个捕获（例如 `@name` 和 `@function`）。

### 3.4 `org.treesitter.TSQueryCapture`
*   **作用**: 具体的捕获结果。
*   **方法**:
    *   `getNode()`: 获取捕获到的 AST 节点 (`TSNode`)。
    *   `getIndex()`: 获取捕获的索引 ID（需配合 `TSQuery.getCaptureNameForId` 转换回字符串名称）。

## 4. 扩展建议

如果你需要在新的 Slicer 中支持基于 Query 的切片：

1.  修改 `AbstractSlicer` 的子类（如 `PythonSlicer`）。
2.  重写 `getFunctionQuery()` 方法，返回该语言提取函数的标准查询语句。
3.  （可选）在 `AbstractSlicer` 中将遍历逻辑从手动游标遍历 (`traverse`) 重构为使用 `QueryExecutor`，可以大幅简化代码。

```java
@Override
public String getFunctionQuery() {
    // Python 示例
    return "(function_definition name: (identifier) @name) @function";
}
```

## 5. 常见错误处理

*   **QueryError**: 如果查询字符串包含语法错误或未知的节点类型，`new TSQuery()` 会抛出异常。开发时请务必先验证查询语句。
*   **Capture Missing**: 并非每个模式都会触发所有捕获。在处理 `Map<String, CodeSlice>` 时，请注意判空。
