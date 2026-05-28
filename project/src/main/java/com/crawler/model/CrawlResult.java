package com.crawler.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CrawlResult {
    private final String strategyName;
    private final String source;
    private final String title;
    private final String content;
    private final LocalDateTime crawledAt;

    public CrawlResult(String strategyName, String source, String title, String content) {
        this.strategyName = strategyName;
        this.source = source;
        this.title = title;
        this.content = content;
        this.crawledAt = LocalDateTime.now();
    }

    public String getStrategyName() {
        return strategyName;
    }

    public String getSource() {
        return source;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getCrawledAt() {
        return crawledAt;
    }

    public String toFileText() {
        return "爬取方式：" + strategyName + System.lineSeparator()
                + "数据来源：" + source + System.lineSeparator()
                + "爬取时间：" + crawledAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + System.lineSeparator()
                + "标题：" + title + System.lineSeparator()
                + "内容：" + System.lineSeparator()
                + content + System.lineSeparator();
    }
}
