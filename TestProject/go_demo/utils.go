package utils

func Add(a, b int) int {
    return a + b
}

type Counter struct {
    Value int
}

func (c *Counter) Increment() {
    c.Value++
}
