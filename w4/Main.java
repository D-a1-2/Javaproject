package w4;

// 所有类写在同一个文件里

abstract class Shape {
    abstract double getArea();
}

class Circle extends Shape {
    double radius;
    Circle(double radius) { this.radius = radius; }
    public double getArea() { return 3.14159 * radius * radius; }
}

class Rectangle extends Shape {
    double width, height;
    Rectangle(double width, double height) {
        this.width = width;
        this.height = height;
    }
    public double getArea() { return width * height; }
}

class Triangle extends Shape {
    double base, height;
    Triangle(double base, double height) {
        this.base = base;
        this.height = height;
    }
    public double getArea() { return 0.5 * base * height; }
}

class ShapeUtil {
    static void printArea(Shape shape) {
        System.out.println("面积是: " + shape.getArea());
    }
}

public class Main {
    public static void main(String[] args) {
        Shape c = new Circle(5);
        Shape r = new Rectangle(4, 6);
        Shape t = new Triangle(3, 8);
        
        ShapeUtil.printArea(c);
        ShapeUtil.printArea(r);
        ShapeUtil.printArea(t);
    }
}

