package main

import (
    "fmt"
    "example/demo/utils"
)

func main() {
    fmt.Println("Go Demo")
    result := utils.Add(1, 2)
    fmt.Printf("Result: %d\n", result)
}
