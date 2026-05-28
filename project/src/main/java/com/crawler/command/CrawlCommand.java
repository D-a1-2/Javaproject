package com.crawler.command;

import com.crawler.exception.CrawlerException;
import com.crawler.exception.ValidationException;
import com.crawler.model.CrawlResult;
import com.crawler.service.ResultStorage;
import com.crawler.strategy.CrawlStrategy;
import com.crawler.strategy.CrawlerStrategyFactory;
import com.crawler.view.ConsoleView;

import java.nio.file.Path;

public class CrawlCommand implements Command {
    private final CrawlerStrategyFactory strategyFactory;
    private final ResultStorage storage;
    private final ConsoleView view;

    public CrawlCommand(CrawlerStrategyFactory strategyFactory, ResultStorage storage, ConsoleView view) {
        this.strategyFactory = strategyFactory;
        this.storage = storage;
        this.view = view;
    }

    @Override
    public String name() {
        return "crawl";
    }

    @Override
    public String description() {
        return "爬取数据：crawl <html|title|stock> <url|stockCode> [名称]";
    }

    @Override
    public boolean execute(String[] args) throws CrawlerException {
        if (args.length < 3) {
            throw new ValidationException("用法：crawl <html|title|stock> <url|stockCode> [名称]");
        }

        String strategyKey = args[1];
        String source = args[2];
        String displayName = args.length >= 4 ? join(args, 3) : "";
        CrawlStrategy strategy = strategyFactory.get(strategyKey);
        if (strategy == null) {
            throw new ValidationException("不支持的爬取策略：" + strategyKey);
        }

        CrawlResult result = strategy.crawl(source, displayName);
        Path savedFile = storage.save(result);
        view.showResult(result, savedFile);
        return true;
    }

    private String join(String[] args, int start) {
        StringBuilder builder = new StringBuilder();
        for (int i = start; i < args.length; i++) {
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(args[i]);
        }
        return builder.toString();
    }
}
