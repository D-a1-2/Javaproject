package com.crawler.command;

import com.crawler.exception.CrawlerException;

public interface Command {
    String name();

    String description();

    boolean execute(String[] args) throws CrawlerException;
}
