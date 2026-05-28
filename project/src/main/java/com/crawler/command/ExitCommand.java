package com.crawler.command;

import com.crawler.view.ConsoleView;

public class ExitCommand implements Command {
    private final ConsoleView view;

    public ExitCommand(ConsoleView view) {
        this.view = view;
    }

    @Override
    public String name() {
        return "exit";
    }

    @Override
    public String description() {
        return "退出程序";
    }

    @Override
    public boolean execute(String[] args) {
        view.showMessage("程序已退出。");
        return false;
    }
}
