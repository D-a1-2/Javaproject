package com.crawler.strategy;

import com.crawler.exception.CrawlerException;
import com.crawler.model.CrawlResult;

public interface CrawlStrategy {
    String getKey();

    String getName();

    CrawlResult crawl(String source, String displayName) throws CrawlerException;
}
