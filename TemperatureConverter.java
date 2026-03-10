import java.util.Scanner;

// 温度转换器示例程序（Java版本）
// 支持摄氏度(C)与华氏度(F)之间互转

public class TemperatureConverter {

    /**
     * 将摄氏度转换为华氏度。
     *
     * @param c 摄氏温度
     * @return 对应的华氏温度
     */
    public static double celsiusToFahrenheit(double c) {
        return c * 9.0 / 5.0 + 32.0;
    }

    /**
     * 将华氏度转换为摄氏度。
     *
     * @param f 华氏温度
     * @return 对应的摄氏温度
     */
    public static double fahrenheitToCelsius(double f) {
        return (f - 32.0) * 5.0 / 9.0;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        // 提示用户输入，格式示例："36.6 C" 或 "97 F"
        System.out.print("请输入要转换的温度与单位（例如 36.6 C 或 97 F）：");
        String s = scanner.nextLine().trim();
        
        if (s.isEmpty()) {
            System.out.println("输入为空，程序退出。");
            scanner.close();
            return;
        }

        String[] parts = s.split("\\s+");
        try {
            // 允许用户输入两个部分：数值与单位
            double value = Double.parseDouble(parts[0]);
            String unit = parts.length > 1 ? parts[1].toUpperCase() : "C";

            if (unit.startsWith("C")) {
                // 从摄氏度转换为华氏度
                double f = celsiusToFahrenheit(value);
                // 使用 %s 完美还原 Python 的浮点数默认显示效果
                System.out.printf("%s °C = %.2f °F\n", value, f);
            } else if (unit.startsWith("F")) {
                // 从华氏度转换为摄氏度
                double c = fahrenheitToCelsius(value);
                System.out.printf("%s °F = %.2f °C\n", value, c);
            } else {
                System.out.println("未知单位，请使用 C 或 F。");
            }
        } catch (Exception e) {
            System.out.println("输入解析失败，请按示例输入数值与单位，例如：36.6 C");
        } finally {
            scanner.close();
        }
    }
}

