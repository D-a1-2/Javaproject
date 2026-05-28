package com.crawler.model;

public class CrawlTask {
    private final String strategyKey;
    private final String source;
    private final String displayName;

    public CrawlTask(String strategyKey, String source, String displayName) {
        this.strategyKey = strategyKey;
        this.source = source;
        this.displayName = displayName;
    }

    public String getStrategyKey() {
        return strategyKey;
    }

    public String getSource() {
        return source;
    }

    public String getDisplayName() {
        return displayName;
    }
}
