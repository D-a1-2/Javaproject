public class BankAccount {
    private String accountNumber;
    private String ownerName;
    private double balance;

    public BankAccount(String accountNumber, String ownerName) {
        this.accountNumber = accountNumber;
        this.ownerName = ownerName;
        this.balance = 0.0; // 初始余额为0
    }

    public void deposit(double amount) {
        if (amount > 0) {
            balance += amount;
            System.out.println("成功存入: " + amount);
        } else {
            System.out.println("存款金额必须为正数。");
        }
    }

    public void withdraw(double amount) {
        if (amount > 0) {
            if (balance >= amount) {
                balance -= amount;
                System.out.println("成功取出: " + amount);
            } else {
                System.out.println("余额不足，无法取出。");
            }
        } else {
            System.out.println("取款金额必须为正数。");
        }
    }
    public void setownerName(String ownerName) {
        this.ownerName = ownerName;
    }   
    public double getBalance() {
        return balance;
    }

    public String getAccountNumber() {
        return accountNumber;
    }
    
}
