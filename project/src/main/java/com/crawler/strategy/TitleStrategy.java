package com.crawler.strategy;

import com.crawler.exception.CrawlerException;
import com.crawler.model.CrawlResult;

public class TitleStrategy extends HtmlTextStrategy {
    @Override
    public String getKey() {
        return "title";
    }

    @Override
    public String getName() {
        return "网页标题快速爬取";
    }

    @Override
    public CrawlResult crawl(String source, String displayName) throws CrawlerException {
        String html = get(source, null);
        String title = extractTitle(html);
        return new CrawlResult(getName(), source, title, "网页标题：" + title);
    }
}
