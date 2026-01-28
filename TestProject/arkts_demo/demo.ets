@Component
struct MyComponent {
  build() {
    Column() {
      Text('Hello ArkTS')
        .fontSize(50)
        .fontWeight(FontWeight.Bold)
      Text('你好，ArkTS') // 中文测试
        .fontSize(30)
    }
  }
}

@Entry
@Component
struct EntryComponent {
  build() {
    MyComponent()
  }
}

function globalHelper() {
  console.log("Global Helper");
  console.log("全局助手函数"); // 中文日志
}

function 中文函数名() {
    console.log("测试中文函数名");
}

