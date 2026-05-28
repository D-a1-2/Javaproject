package com.crawler.strategy;

import com.crawler.exception.NetworkException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public abstract class AbstractHttpStrategy implements CrawlStrategy {
    protected String get(String url, Charset charset) throws NetworkException {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) URI.create(url).toURL().openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(12000);
            connection.setReadTimeout(12000);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 JavaFinalCrawler/1.0");
            connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            connection.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.7");
            if (url.contains("hq.sinajs.cn")) {
                connection.setRequestProperty("Referer", "https://finance.sina.com.cn/");
            }

            int status = connection.getResponseCode();
            if (status < 200 || status >= 300) {
                throw new NetworkException("HTTP 请求失败，状态码：" + status);
            }

            Charset actualCharset = charset == null ? detectCharset(connection.getContentType()) : charset;
            return read(connection.getInputStream(), actualCharset);
        } catch (IllegalArgumentException e) {
            throw new NetworkException("URL 格式不正确：" + url, e);
        } catch (IOException e) {
            throw new NetworkException("网络请求异常：" + e.getMessage(), e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private Charset detectCharset(String contentType) {
        if (contentType != null) {
            String[] parts = contentType.split(";");
            for (String part : parts) {
                String trimmed = part.trim().toLowerCase();
                if (trimmed.startsWith("charset=")) {
                    String charsetName = trimmed.substring("charset=".length());
                    try {
                        return Charset.forName(charsetName);
                    } catch (Exception ignored) {
                        return StandardCharsets.UTF_8;
                    }
                }
            }
        }
        return StandardCharsets.UTF_8;
    }

    private String read(InputStream inputStream, Charset charset) throws IOException {
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, charset))) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append(System.lineSeparator());
            }
        }
        return builder.toString();
    }
}
