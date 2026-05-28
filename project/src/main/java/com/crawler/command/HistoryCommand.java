package com.crawler.command;

import com.crawler.view.ConsoleView;

import java.util.List;

public class HistoryCommand implements Command {
    private final ConsoleView view;
    private final List<String> history;

    public HistoryCommand(ConsoleView view, List<String> history) {
        this.view = view;
        this.history = history;
    }

    @Override
    public String name() {
        return "history";
    }

    @Override
    public String description() {
        return "查看本次运行输入过的命令";
    }

    @Override
    public boolean execute(String[] args) {
        if (history.isEmpty()) {
            view.showMessage("暂无历史命令。");
            return true;
        }

        for (int i = 0; i < history.size(); i++) {
            view.showMessage((i + 1) + ". " + history.get(i));
        }
        return true;
    }
}
