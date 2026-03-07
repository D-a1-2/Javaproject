
import java.util.Scanner;

public class TemperatureConverter {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("--- 温度转换程序 ---");
        System.out.println("请输入带符号的温度值（例如 32C 或 100F）:");
        
        if (!scanner.hasNext()) return;
        String input = scanner.next();

        // 逻辑：判断末尾字母是 C 还是 F
        if (input.endsWith("C") || input.endsWith("c")) {
            double c = Double.parseDouble(input.substring(0, input.length() - 1));
            double f = c * 1.8 + 32;
            System.out.printf("转换后的华氏度为: %.2fF\n", f);
        } else if (input.endsWith("F") || input.endsWith("f")) {
            double f = Double.parseDouble(input.substring(0, input.length() - 1));
            double c = (f - 32) / 1.8;
            System.out.printf("转换后的摄氏度为: %.2fC\n", c);
        } else {
            System.out.println("输入格式错误，请记得带上 C 或 F！");
        }
        
        scanner.close();
    }
}