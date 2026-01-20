# 代码切片策略 (Code Slicing Strategy)

为了让大模型（LLM）更好地理解项目结构，本工具不仅提取函数，还提取了类、接口、结构体和枚举等高层结构。本文档详细说明了各语言的切片支持情况。

## 1. 通用模型 (Common Model)

所有提取的代码片段都包含一个 `SliceType` 属性，用于区分代码块的类型：

*   **FUNCTION**: 函数、方法、构造函数。
*   **CLASS**: 类定义。
*   **INTERFACE**: 接口定义。
*   **STRUCT**: 结构体定义。
*   **ENUM**: 枚举定义。
*   **TYPE_ALIAS**: 类型别名。

## 2. 语言支持详情 (Language Support)

### 2.1 Java

| 结构 | Tree-sitter Node Type | 映射 SliceType | 备注 |
| :--- | :--- | :--- | :--- |
| 类 | `class_declaration` | `CLASS` | 包含内部类 |
| 接口 | `interface_declaration` | `INTERFACE` | |
| 枚举 | `enum_declaration` | `ENUM` | |
| 方法 | `method_declaration` | `FUNCTION` | |
| 构造函数 | `constructor_declaration` | `FUNCTION` | |

### 2.2 Python

| 结构 | Tree-sitter Node Type | 映射 SliceType | 备注 |
| :--- | :--- | :--- | :--- |
| 类 | `class_definition` | `CLASS` | |
| 函数/方法 | `function_definition` | `FUNCTION` | 包含模块级函数和类方法 |

### 2.3 TypeScript / JavaScript

| 结构 | Tree-sitter Node Type | 映射 SliceType | 备注 |
| :--- | :--- | :--- | :--- |
| 类 | `class_declaration` | `CLASS` | |
| 接口 | `interface_declaration` | `INTERFACE` | 仅 TS |
| 类型别名 | `type_alias_declaration` | `TYPE_ALIAS` | 仅 TS |
| 枚举 | `enum_declaration` | `ENUM` | 仅 TS |
| 函数 | `function_declaration` | `FUNCTION` | |
| 生成器函数 | `generator_function_declaration` | `FUNCTION` | 仅 JS (TS 也可能支持) |
| 方法 | `method_definition` | `FUNCTION` | 类中的方法 |

### 2.4 Go

| 结构 | Tree-sitter Node Type | 映射 SliceType | 备注 |
| :--- | :--- | :--- | :--- |
| 结构体 | `type_declaration` (inner `struct_type`) | `STRUCT` | e.g. `type User struct { ... }` |
| 接口 | `type_declaration` (inner `interface_type`) | `INTERFACE` | e.g. `type Service interface { ... }` |
| 函数 | `function_declaration` | `FUNCTION` | |
| 方法 | `method_declaration` | `FUNCTION` | 绑定到接收者的方法 |

### 2.5 C / C++

| 结构 | Tree-sitter Node Type | 映射 SliceType | 备注 |
| :--- | :--- | :--- | :--- |
| 类 | `class_specifier` | `CLASS` | 仅 C++ |
| 结构体 | `struct_specifier` | `STRUCT` | |
| 枚举 | `enum_specifier` | `ENUM` | |
| 函数 | `function_definition` | `FUNCTION` | |

## 3. 扩展指南 (Extension Guide)

如果需要支持新的语言或结构：

1.  在 `org.guo.treesitter.model.SliceType` 中添加新的枚举值（如果需要）。
2.  继承 `AbstractSlicer` 创建新的 Slicer 类。
3.  实现 `shouldSlice(TSNode node)`：定义哪些 AST 节点需要被提取。
4.  实现 `getSliceType(TSNode node)`：将 AST 节点映射到 `SliceType`。
5.  实现 `getName(TSNode node, ...)`：定义如何从节点中提取名称。
