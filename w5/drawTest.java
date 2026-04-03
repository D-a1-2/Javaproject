package w5;

abstract class Shape {
    public abstract void draw();
    
}
class Circle extends Shape {
    @Override
    public void draw() {
        System.out.println("画一个圆");
    }
}
class Rectangle extends Shape {
    @Override
    public void draw() {
        System.out.println("画一个矩形");
    }
}
public class drawTest {

    public static void drawShape(Shape s) {
        s.draw();
    }
    public static void main(String[] args) {
        Circle c = new Circle();
        Rectangle r = new Rectangle();
        drawShape(c);
        drawShape(r);
    }
}