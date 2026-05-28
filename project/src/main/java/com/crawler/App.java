package com.crawler;

import com.crawler.controller.CrawlerController;
import com.crawler.view.ConsoleView;

public class App {
    public static void main(String[] args) {
        ConsoleView view = new ConsoleView(System.in, System.out);
        CrawlerController controller = new CrawlerController(view);
        controller.start();
    }
}
