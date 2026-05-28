package com.crawler.exception;

public class NetworkException extends CrawlerException {
    public NetworkException(String message) {
        super(message);
    }

    public NetworkException(String message, Throwable cause) {
        super(message, cause);
    }
}
