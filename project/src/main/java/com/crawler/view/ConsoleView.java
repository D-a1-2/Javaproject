package com.crawler.view;

import com.crawler.model.CrawlResult;

import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.Scanner;

public class ConsoleView {
    private final Scanner scanner;
    private final PrintStream out;

    public ConsoleView(InputStream inputStream, PrintStream out) {
        this.scanner = new Scanner(inputStream);
        this.out = out;
    }

    public void showWelcome() {
        out.println("Java 期末爬虫项目");
        out.println("CLI + MVC + Command + Strategy + Exception");
        out.println("输入 help 查看命令。");
    }

    public String readCommand() {
        out.print("> ");
        if (!scanner.hasNextLine()) {
            return "exit";
        }
        return scanner.nextLine().trim();
    }

    public void showMessage(String message) {
        out.println(message);
    }

    public void showError(String message) {
        out.println("[错误] " + message);
    }

    public void showResult(CrawlResult result, Path savedFile) {
        out.println();
        out.println("爬取完成：" + result.getTitle());
        out.println("来源：" + result.getSource());
        out.println("方式：" + result.getStrategyName());
        out.println("保存文件：" + savedFile.toAbsolutePath());
        out.println("内容预览：");
        String content = result.getContent();
        out.println(content.length() > 300 ? content.substring(0, 300) + "..." : content);
        out.println();
    }
}
