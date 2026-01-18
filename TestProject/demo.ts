function greet(name: string): string {
    return `Hello, ${name}`;
}

class Person {
    constructor(private name: string) {}

    public getName(): string {
        return this.name;
    }
}
