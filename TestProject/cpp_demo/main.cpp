#include <iostream>
#include "Shape.h"

int main() {
    Rectangle rect(10, 20);
    std::cout << "Area: " << rect.getArea() << std::endl;
    return 0;
}
