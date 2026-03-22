package w3;

public class EmployeeTest {
    public static void main(String[] args) {
        Employee e1 = new Employee("E001", "张三", "技术部", 2500);
        Employee e2 = new Employee("E002", "李四", "市场部", 1800);

        System.out.println(e1.getName()+"D的工资："+e1.getSalary());
        e1.raiseSalary(10);
        
        e2.setName("李四");
        System.out.println(e2.getName()+"的工资："+e2.getSalary());  
    }
    
}
