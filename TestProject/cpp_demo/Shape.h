#ifndef SHAPE_H
#define SHAPE_H

class Rectangle {
private:
    int width, height;
public:
    Rectangle(int w, int h);
    int getArea();
    void setWidth(int w);
};

#endif
