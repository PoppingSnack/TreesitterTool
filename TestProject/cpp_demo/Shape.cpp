#include "Shape.h"

Rectangle::Rectangle(int w, int h) : width(w), height(h) {}

int Rectangle::getArea() {
    return width * height;
}

void Rectangle::setWidth(int w) {
    width = w;
}
