public class Employee {
    private String id;
    private String name;
    private String department;
    private double salary;

    public  Employee(String id,String name,String department,double salary){
        this.name=name;
        this.department=department;
        this.id=id;
        setSalary(salary);
    }
    public String getId() {return id;}
    public String getName() {return name;}
    public String getDepartment() {return department;}
    public double getSalary() {return salary;}

    public void setName(String name) {this.name = name;}
    public void setDepartment(String department) {this.department = department;}
    public void setSalary(double salary) {
        if (salary >=2000) {
            this.salary = salary;
        } else {
            System.out.println("工资必须大于或等于2000。");
        }
    }
    public void raiseSalary(double percent){
        double newSalary=salary+salary*percent/100;
        if (newSalary<2000){
            this.salary=2000;
        }else {this.salary=newSalary;
        }
        System.out.println("员工"+name+"的工资已调整为"+salary);
        
        }
    }
    
