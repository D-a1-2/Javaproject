package w5;

abstract class Vehicle {
    public abstract void run();
}
class Car extends Vehicle {
    @Override
    public void run() {
        System.out.println("汽车在跑");
    }
}
class Bike extends Vehicle {
    @Override
    public void run() {
        System.out.println("自行车在跑");
    }
}
class Truck extends Vehicle {
    @Override
    public void run() {
        System.out.println("卡车在跑");
    }
}
public class runVehicle {
    public static void runVehicle(Vehicle v) {
        v.run();
    }
    public static void main(String[] args) {
        Vehicle[] vehicles = {new Car(), new Bike(), new Truck()};
        for (Vehicle v : vehicles) {
            runVehicle(v);
        }       
    }
}