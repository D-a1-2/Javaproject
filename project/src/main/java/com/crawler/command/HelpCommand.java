package com.crawler.command;

import com.crawler.view.ConsoleView;

import java.util.Collection;

public class HelpCommand implements Command {
    private final ConsoleView view;
    private final Collection<Command> commands;

    public HelpCommand(ConsoleView view, Collection<Command> commands) {
        this.view = view;
        this.commands = commands;
    }

    @Override
    public String name() {
        return "help";
    }

    @Override
    public String description() {
        return "查看所有命令";
    }

    @Override
    public boolean execute(String[] args) {
        view.showMessage("可用命令：");
        for (Command command : commands) {
            view.showMessage("  " + command.name() + " - " + command.description());
        }
        return true;
    }
}
