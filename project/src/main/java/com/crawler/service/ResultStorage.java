package com.crawler.service;

import com.crawler.exception.StorageException;
import com.crawler.model.CrawlResult;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;

public class ResultStorage {
    private final Path outputDirectory;

    public ResultStorage(Path outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public Path save(CrawlResult result) throws StorageException {
        try {
            Files.createDirectories(outputDirectory);
            String timestamp = result.getCrawledAt().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS"));
            String fileName = timestamp + "_" + sanitize(result.getStrategyName()) + "_" + sanitize(result.getTitle()) + ".txt";
            Path file = outputDirectory.resolve(fileName);
            Files.writeString(file, result.toFileText(), StandardCharsets.UTF_8);
            return file;
        } catch (IOException e) {
            throw new StorageException("保存爬取结果失败", e);
        }
    }

    private String sanitize(String text) {
        String value = text == null || text.isBlank() ? "crawl-result" : text;
        value = value.replaceAll("[\\\\/:*?\"<>|\\s]+", "_");
        if (value.length() > 40) {
            value = value.substring(0, 40);
        }
        return value;
    }
}
