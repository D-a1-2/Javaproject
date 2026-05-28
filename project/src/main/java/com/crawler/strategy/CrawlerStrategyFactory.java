package com.crawler.strategy;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class CrawlerStrategyFactory {
    private final Map<String, CrawlStrategy> strategies = new LinkedHashMap<>();

    public CrawlerStrategyFactory() {
        register(new HtmlTextStrategy());
        register(new TitleStrategy());
        register(new SinaStockStrategy());
    }

    public CrawlStrategy get(String key) {
        return strategies.get(key.toLowerCase());
    }

    public Collection<CrawlStrategy> list() {
        return strategies.values();
    }

    private void register(CrawlStrategy strategy) {
        strategies.put(strategy.getKey(), strategy);
    }
}
