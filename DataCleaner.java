public class DataCleaner {
    public static void main(String[] args) {
        int[] sensorData = {85, -5, 92, 0, 105, 999, 88, 76};
        
        int validSum = 0;   // 有效数据总和
        int validCount = 0; // 有效数据个数
        
        // 请在此处编写你的流程控制代码
        for (int data : sensorData) {
            if (data > 0 && data <= 100) {
                validSum += data;
                validCount++;
            }else if (data <= 0 || (data > 100 && data != 999)) {
                System.out.println("警告:发现越界数据[" + data + "]，已跳过。");
                continue; // 无效数据，跳过处理
            }else if  (data == 999) {
                System.out.println("致命错误:传感器掉线,终止处理。");
                break; // 遇到异常数据，停止处理后续数据
            }
            
            
        }
        if (validCount > 0) {
            double average = (double) validSum / validCount;
            System.out.printf("有效数据平均值: %.2f\n", average);
        } else {
            System.out.println("无有效数据");
        }
    }
}
