package com.crawler.controller;

import com.crawler.command.BatchCommand;
import com.crawler.command.Command;
import com.crawler.command.CrawlCommand;
import com.crawler.command.ExitCommand;
import com.crawler.command.HelpCommand;
import com.crawler.command.HistoryCommand;
import com.crawler.exception.CrawlerException;
import com.crawler.exception.ValidationException;
import com.crawler.service.ResultStorage;
import com.crawler.strategy.CrawlerStrategyFactory;
import com.crawler.view.ConsoleView;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CrawlerController {
    private final ConsoleView view;
    private final List<String> history = new ArrayList<>();
    private final Map<String, Command> commands = new LinkedHashMap<>();

    public CrawlerController(ConsoleView view) {
        this.view = view;
        CrawlerStrategyFactory strategyFactory = new CrawlerStrategyFactory();
        ResultStorage storage = new ResultStorage(Paths.get("data"));

        register(new CrawlCommand(strategyFactory, storage, view));
        register(new BatchCommand(strategyFactory, storage, view));
        register(new HistoryCommand(view, history));
        register(new ExitCommand(view));
        register(new HelpCommand(view, commands.values()));
    }

    public void start() {
        view.showWelcome();
        boolean running = true;
        while (running) {
            String line = view.readCommand();
            if (line.isBlank()) {
                continue;
            }
            history.add(line);
            try {
                running = dispatch(line);
            } catch (CrawlerException e) {
                view.showError(e.getMessage());
            } catch (RuntimeException e) {
                view.showError("系统异常：" + e.getMessage());
            }
        }
    }

    private boolean dispatch(String line) throws CrawlerException {
        String[] args = line.split("\\s+");
        Command command = commands.get(args[0].toLowerCase());
        if (command == null) {
            throw new ValidationException("未知命令：" + args[0] + "，请输入 help 查看帮助。");
        }
        return command.execute(args);
    }

    private void register(Command command) {
        commands.put(command.name(), command);
    }
}
