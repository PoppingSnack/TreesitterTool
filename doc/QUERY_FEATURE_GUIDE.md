# Tree-sitter 查询功能指南

本文档旨在介绍 Tree-sitter 的查询（Query）机制、执行原理以及在本工具中的能力边界。

## 1. 什么是 Tree-sitter Query？

Tree-sitter 提供了一种基于 **S-expressions（S表达式）** 的模式匹配语言，用于在语法树（AST）中查找特定的节点模式。这类似于正则表达式之于文本，XPath 之于 XML。

通过 Query，我们可以声明式地描述"长什么样"的语法结构，而不是编写复杂的递归代码来手动遍历 AST。

### 1.1 基本语法

查询由一个或多个**模式（Pattern）**组成。每个模式描述了一棵子树。

*   **节点匹配**：`(function_definition)` 匹配类型为 `function_definition` 的节点。
*   **层级关系**：
    ```lisp
    (function_definition
      (block (return_statement))
    )
    ```
    匹配包含 `return_statement` 的函数定义。
*   **字段约束**：`name: (identifier)` 匹配字段名为 `name` 且类型为 `identifier` 的子节点。
*   **捕获（Capture）**：使用 `@capture_name` 将匹配到的节点保存下来，供后续处理使用。
    ```lisp
    (function_definition name: (identifier) @func_name)
    ```
*   **通配符**：`_` 匹配任意类型的节点。

### 1.2 谓词（Predicates）

查询支持谓词，用于对匹配结果进行更细粒度的过滤（通常基于文本内容）。

*   `#eq? @capture "value"`: 捕获节点的文本必须等于 "value"。
*   `#match? @capture "^regex$"`: 捕获节点的文本必须匹配正则表达式。
*   `#any-of? @capture "a" "b"`: 文本必须是列表中的一个。

## 2. 执行机制

Tree-sitter 的查询执行分为三个阶段：

1.  **编译（Compilation）**：
    *   将 S-expression 字符串编译为高效的字节码或状态机 (`TSQuery` 对象)。
    *   在此阶段会验证查询语法的合法性，以及节点类型是否存在于当前语言中。

2.  **执行（Execution）**：
    *   使用 `TSQueryCursor` 在 AST 上运行查询。
    *   游标会遍历 AST，寻找满足模式的节点序列。
    *   这是一个流式过程，通过 `cursor.nextMatch()` 逐个获取匹配结果。

3.  **提取（Extraction）**：
    *   每个匹配结果 (`TSQueryMatch`) 包含多个捕获 (`TSQueryCapture`)。
    *   例如，一个模式可能同时捕获了函数的"名称"(@name)和"函数体"(@body)。

## 3. 能力边界

### 3.1 它能做什么
*   **语法结构提取**：非常适合提取函数、类、注释、特定调用等。
*   **模糊匹配**：即使代码有语法错误，只要局部结构符合模式，依然可以匹配。
*   **多模式组合**：可以在一个查询文件中定义多个模式，一次性匹配多种结构。

### 3.2 它不能做什么
*   **跨文件语义分析**：Tree-sitter 是单文件解析器，无法知道 `import` 的模块里定义了什么，也无法进行跨文件的引用跳转。
*   **类型检查**：它只能看到语法结构（"这是一个标识符"），不知道运行时类型（"这是一个 String 对象"）。
*   **复杂的逻辑判断**：谓词能力有限，复杂的业务逻辑（如"如果父节点是A且祖父节点不是B"）通常需要在获取节点后在宿主代码（Java）中判断。

## 4. 本项目中的应用

在 `TreesitterTool` 中，我们封装了 `QueryExecutor` 接口，主要用于：
1.  **代码切片**：通过查询语句精确定位函数定义的范围（起始行、结束行）。
2.  **动态检索**：允许用户传入模板查询（如查找特定名称的函数），通过 `QueryBuilder` 动态替换参数后执行。

这种方式比硬编码的 AST 遍历（`AbstractSlicer` 中的 `traverse` 方法）更灵活，维护成本更低。
