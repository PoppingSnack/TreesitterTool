@Component
struct MyComponent {
  build() {
    Column() {
      Text('Hello ArkTS')
        .fontSize(50)
        .fontWeight(FontWeight.Bold)
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
}
