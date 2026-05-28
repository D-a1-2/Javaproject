package com.crawler.strategy;

import com.crawler.exception.CrawlerException;
import com.crawler.exception.ParseException;
import com.crawler.model.CrawlResult;

import java.nio.charset.Charset;

public class SinaStockStrategy extends AbstractHttpStrategy {
    @Override
    public String getKey() {
        return "stock";
    }

    @Override
    public String getName() {
        return "新浪财经股票数据爬取";
    }

    @Override
    public CrawlResult crawl(String stockCode, String displayName) throws CrawlerException {
        String url = "https://hq.sinajs.cn/list=" + stockCode;
        String response = get(url, Charset.forName("GBK"));
        String[] parts = extractParts(response);
        String stockName = displayName == null || displayName.isBlank() ? parts[0] : displayName;

        double current = parseDouble(parts[3]);
        double previousClose = parseDouble(parts[2]);
        double change = current - previousClose;
        double changePercent = previousClose == 0 ? 0 : change / previousClose * 100;

        String content = String.join(System.lineSeparator(),
                "股票名称：" + parts[0],
                "股票代码：" + stockCode,
                "开盘价：" + parts[1],
                "昨收价：" + parts[2],
                "最新价：" + parts[3],
                "最高价：" + parts[4],
                "最低价：" + parts[5],
                "涨跌额：" + String.format("%.3f", change),
                "涨跌幅：" + String.format("%.2f%%", changePercent),
                "成交量：" + parts[8],
                "成交额：" + parts[9],
                "数据日期：" + (parts.length > 30 ? parts[30] : "未知"),
                "数据时间：" + (parts.length > 31 ? parts[31] : "未知"));

        return new CrawlResult(getName(), url, stockName, content);
    }

    private String[] extractParts(String response) throws ParseException {
        int start = response.indexOf('"');
        int end = response.lastIndexOf('"');
        if (start < 0 || end <= start) {
            throw new ParseException("新浪财经接口没有返回有效数据");
        }

        String[] parts = response.substring(start + 1, end).split(",");
        if (parts.length < 10 || parts[0].isBlank()) {
            throw new ParseException("股票数据字段不足或股票代码不存在");
        }
        return parts;
    }

    private double parseDouble(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
