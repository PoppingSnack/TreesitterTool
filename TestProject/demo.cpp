#include <iostream>

class Calculator {
public:
    int add(int a, int b) {
        return a + b;
    }
};

void globalFunction() {
    std::cout << "Global Function" << std::endl;
}
