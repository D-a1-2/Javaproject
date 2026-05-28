package com.crawler.command;

import com.crawler.exception.CrawlerException;
import com.crawler.model.CrawlResult;
import com.crawler.model.CrawlTask;
import com.crawler.service.ResultStorage;
import com.crawler.strategy.CrawlStrategy;
import com.crawler.strategy.CrawlerStrategyFactory;
import com.crawler.view.ConsoleView;

import java.nio.file.Path;
import java.util.List;

public class BatchCommand implements Command {
    private final CrawlerStrategyFactory strategyFactory;
    private final ResultStorage storage;
    private final ConsoleView view;

    public BatchCommand(CrawlerStrategyFactory strategyFactory, ResultStorage storage, ConsoleView view) {
        this.strategyFactory = strategyFactory;
        this.storage = storage;
        this.view = view;
    }

    @Override
    public String name() {
        return "batch";
    }

    @Override
    public String description() {
        return "按预设任务爬取 3 个以上网站并保存文件";
    }

    @Override
    public boolean execute(String[] args) throws CrawlerException {
        List<CrawlTask> tasks = List.of(
                new CrawlTask("title", "https://www.example.com", "Example 官网"),
                new CrawlTask("html", "https://www.example.com", "Example Domain"),
                new CrawlTask("html", "https://www.iana.org/domains/reserved", "IANA 保留域名说明"),
                new CrawlTask("title", "https://www.rfc-editor.org", "RFC Editor 首页"));

        int success = 0;
        for (CrawlTask task : tasks) {
            try {
                CrawlStrategy strategy = strategyFactory.get(task.getStrategyKey());
                CrawlResult result = strategy.crawl(task.getSource(), task.getDisplayName());
                Path savedFile = storage.save(result);
                view.showResult(result, savedFile);
                success++;
            } catch (CrawlerException e) {
                view.showError(task.getDisplayName() + " 爬取失败：" + e.getMessage());
            }
        }
        view.showMessage("批量爬取结束，成功任务数：" + success);
        return true;
    }
}
