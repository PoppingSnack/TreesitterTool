package main

import "fmt"

func main() {
    fmt.Println("Hello")
}

type Person struct {
    Name string
}

func (p *Person) Greet() {
    fmt.Printf("Hello %s\n", p.Name)
}
