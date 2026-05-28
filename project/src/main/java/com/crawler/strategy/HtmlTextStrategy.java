package com.crawler.strategy;

import com.crawler.exception.CrawlerException;
import com.crawler.exception.ParseException;
import com.crawler.model.CrawlResult;

public class HtmlTextStrategy extends AbstractHttpStrategy {
    @Override
    public String getKey() {
        return "html";
    }

    @Override
    public String getName() {
        return "普通网页正文爬取";
    }

    @Override
    public CrawlResult crawl(String source, String displayName) throws CrawlerException {
        String html = get(source, null);
        String title = extractTitle(html);
        String text = html.replaceAll("(?is)<script.*?</script>", " ")
                .replaceAll("(?is)<style.*?</style>", " ")
                .replaceAll("(?is)<[^>]+>", " ")
                .replace("&nbsp;", " ")
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replaceAll("\\s+", " ")
                .trim();

        if (text.isEmpty()) {
            throw new ParseException("网页正文为空，无法提取有效文本");
        }

        String content = text.length() > 1200 ? text.substring(0, 1200) + "..." : text;
        return new CrawlResult(getName(), source, displayName == null || displayName.isBlank() ? title : displayName, content);
    }

    protected String extractTitle(String html) {
        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("(?is)<title[^>]*>(.*?)</title>")
                .matcher(html);
        if (matcher.find()) {
            return matcher.group(1).replaceAll("\\s+", " ").trim();
        }
        return "未获取到标题";
    }
}
