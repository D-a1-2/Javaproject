package com.crawler;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Java 期末爬虫项目：CLI + MVC + Command 模式 + 策略模式 + 异常体系。
 * 为了便于提交和查看，所有类整合在一个源码文件中。
 */
public class App {
    public static void main(String[] args) {
        new Controller(new ConsoleView()).run();
    }

    // ========================= MVC: Controller =========================
    static class Controller {
        private final ConsoleView view;
        private final Map<String, Command> commands = new LinkedHashMap<>();
        private final StrategyFactory factory = new StrategyFactory();
        private final ResultStorage storage = new ResultStorage("data");

        Controller(ConsoleView view) {
            this.view = view;
            commands.put("help", new HelpCommand(commands, view));
            commands.put("crawl", new CrawlCommand(factory, storage, view));
            commands.put("batch", new BatchCommand(factory, storage, view));
            commands.put("exit", new ExitCommand(view));
        }

        void run() {
            view.println("Java 期末爬虫项目，输入 help 查看命令。");
            boolean running = true;
            while (running) {
                try {
                    String line = view.input("> ");
                    if (line.isBlank()) {
                        continue;
                    }
                    String[] args = line.trim().split("\\s+");
                    Command command = commands.get(args[0].toLowerCase());
                    if (command == null) {
                        throw new ValidationException("未知命令：" + args[0]);
                    }
                    running = command.execute(args);
                } catch (CrawlerException e) {
                    view.println("[错误] " + e.getMessage());
                } catch (Exception e) {
                    view.println("[系统错误] " + e.getMessage());
                }
            }
        }
    }

    // ========================= MVC: View =========================
    static class ConsoleView {
        private final Scanner scanner = new Scanner(System.in);

        String input(String prompt) {
            System.out.print(prompt);
            return scanner.hasNextLine() ? scanner.nextLine() : "exit";
        }

        void println(String text) {
            System.out.println(text);
        }

        void showResult(CrawlResult result, Path file) {
            println("\n爬取成功：" + result.title);
            println("来源：" + result.source);
            println("保存：" + file.toAbsolutePath());
            println("预览：" + shortText(result.content) + "\n");
        }

        private String shortText(String text) {
            return text.length() > 180 ? text.substring(0, 180) + "..." : text;
        }
    }

    // ========================= MVC: Model =========================
    static class CrawlTask {
        String type;
        String source;
        String name;

        CrawlTask(String type, String source, String name) {
            this.type = type;
            this.source = source;
            this.name = name;
        }
    }

    static class CrawlResult {
        String strategyName;
        String source;
        String title;
        String content;
        LocalDateTime time = LocalDateTime.now();

        CrawlResult(String strategyName, String source, String title, String content) {
            this.strategyName = strategyName;
            this.source = source;
            this.title = title;
            this.content = content;
        }

        String toFileText() {
            return "爬取方式：" + strategyName + "\n"
                    + "来源：" + source + "\n"
                    + "时间：" + time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n"
                    + "标题：" + title + "\n\n"
                    + content;
        }
    }

    // ========================= Command 模式 =========================
    interface Command {
        boolean execute(String[] args) throws CrawlerException;

        String description();
    }

    static class HelpCommand implements Command {
        private final Map<String, Command> commands;
        private final ConsoleView view;

        HelpCommand(Map<String, Command> commands, ConsoleView view) {
            this.commands = commands;
            this.view = view;
        }

        public boolean execute(String[] args) {
            commands.forEach((name, command) -> view.println(name + " - " + command.description()));
            return true;
        }

        public String description() {
            return "查看帮助";
        }
    }

    static class CrawlCommand implements Command {
        private final StrategyFactory factory;
        private final ResultStorage storage;
        private final ConsoleView view;

        CrawlCommand(StrategyFactory factory, ResultStorage storage, ConsoleView view) {
            this.factory = factory;
            this.storage = storage;
            this.view = view;
        }

        public boolean execute(String[] args) throws CrawlerException {
            if (args.length < 3) {
                throw new ValidationException("用法：crawl <html|title|stock> <url|股票代码> [名称]");
            }
            CrawlStrategy strategy = factory.get(args[1]);
            if (strategy == null) {
                throw new ValidationException("不支持的爬取类型：" + args[1]);
            }
            String name = args.length >= 4 ? join(args, 3) : "";
            CrawlResult result = strategy.crawl(args[2], name);
            view.showResult(result, storage.save(result));
            return true;
        }

        public String description() {
            return "单次爬取，如 crawl html https://www.example.com";
        }
    }

    static class BatchCommand implements Command {
        private final StrategyFactory factory;
        private final ResultStorage storage;
        private final ConsoleView view;

        BatchCommand(StrategyFactory factory, ResultStorage storage, ConsoleView view) {
            this.factory = factory;
            this.storage = storage;
            this.view = view;
        }

        public boolean execute(String[] args) {
            List<CrawlTask> tasks = List.of(
                    new CrawlTask("title", "https://www.example.com", "Example"),
                    new CrawlTask("html", "https://www.iana.org/domains/reserved", "IANA"),
                    new CrawlTask("title", "https://www.rfc-editor.org", "RFC Editor")
            );

            int success = 0;
            for (CrawlTask task : tasks) {
                try {
                    CrawlResult result = factory.get(task.type).crawl(task.source, task.name);
                    view.showResult(result, storage.save(result));
                    success++;
                } catch (CrawlerException e) {
                    view.println("[失败] " + task.name + "：" + e.getMessage());
                }
            }
            view.println("批量爬取完成，成功 " + success + " 个网站。");
            return true;
        }

        public String description() {
            return "批量爬取 3 个以上网站";
        }
    }

    static class ExitCommand implements Command {
        private final ConsoleView view;

        ExitCommand(ConsoleView view) {
            this.view = view;
        }

        public boolean execute(String[] args) {
            view.println("程序结束。");
            return false;
        }

        public String description() {
            return "退出程序";
        }
    }

    // ========================= 策略模式 =========================
    interface CrawlStrategy {
        CrawlResult crawl(String source, String name) throws CrawlerException;
    }

    static class StrategyFactory {
        private final Map<String, CrawlStrategy> strategies = Map.of(
                "html", new HtmlStrategy(),
                "title", new TitleStrategy(),
                "stock", new StockStrategy()
        );

        CrawlStrategy get(String type) {
            return strategies.get(type.toLowerCase());
        }
    }

    static class HtmlStrategy implements CrawlStrategy {
        public CrawlResult crawl(String url, String name) throws CrawlerException {
            String html = HttpTool.get(url, null);
            String title = titleOf(html);
            String text = html.replaceAll("(?is)<script.*?</script>", " ")
                    .replaceAll("(?is)<style.*?</style>", " ")
                    .replaceAll("<[^>]+>", " ")
                    .replace("&nbsp;", " ")
                    .replace("&amp;", "&")
                    .replaceAll("\\s+", " ")
                    .trim();
            if (text.isEmpty()) {
                throw new ParseException("网页正文为空");
            }
            return new CrawlResult("网页正文策略", url, name.isBlank() ? title : name, text);
        }
    }

    static class TitleStrategy implements CrawlStrategy {
        public CrawlResult crawl(String url, String name) throws CrawlerException {
            String title = titleOf(HttpTool.get(url, null));
            return new CrawlResult("网页标题策略", url, title, "网页标题：" + title);
        }
    }

    static class StockStrategy implements CrawlStrategy {
        public CrawlResult crawl(String code, String name) throws CrawlerException {
            String url = "https://hq.sinajs.cn/list=" + code;
            String body = HttpTool.get(url, Charset.forName("GBK"));
            int start = body.indexOf('"'), end = body.lastIndexOf('"');
            if (start < 0 || end <= start) {
                throw new ParseException("股票接口无有效数据");
            }
            String[] p = body.substring(start + 1, end).split(",");
            if (p.length < 10) {
                throw new ParseException("股票字段不足");
            }
            String content = "股票名称：" + p[0] + "\n最新价：" + p[3] + "\n最高价：" + p[4]
                    + "\n最低价：" + p[5] + "\n成交量：" + p[8] + "\n成交额：" + p[9];
            return new CrawlResult("股票策略", url, name.isBlank() ? p[0] : name, content);
        }
    }

    // ========================= 文件保存与 HTTP 工具 =========================
    static class ResultStorage {
        private final Path dir;

        ResultStorage(String dir) {
            this.dir = Paths.get(dir);
        }

        Path save(CrawlResult result) throws StorageException {
            try {
                Files.createDirectories(dir);
                String time = result.time.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                Path file = dir.resolve(time + "_" + clean(result.title) + ".txt");
                Files.writeString(file, result.toFileText(), StandardCharsets.UTF_8);
                return file;
            } catch (IOException e) {
                throw new StorageException("保存文件失败", e);
            }
        }

        private String clean(String text) {
            return text.replaceAll("[\\\\/:*?\"<>|\\s]+", "_");
        }
    }

    static class HttpTool {
        static String get(String url, Charset charset) throws NetworkException {
            HttpURLConnection conn = null;
            try {
                conn = (HttpURLConnection) URI.create(url).toURL().openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 JavaCrawler");
                if (url.contains("hq.sinajs.cn")) {
                    conn.setRequestProperty("Referer", "https://finance.sina.com.cn/");
                }
                if (conn.getResponseCode() != 200) {
                    throw new NetworkException("HTTP 状态码：" + conn.getResponseCode());
                }
                Charset cs = charset == null ? StandardCharsets.UTF_8 : charset;
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), cs))) {
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line).append('\n');
                    }
                    return sb.toString();
                }
            } catch (Exception e) {
                if (e instanceof NetworkException) {
                    throw (NetworkException) e;
                }
                throw new NetworkException("网络请求失败：" + e.getMessage(), e);
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }
    }

    static String titleOf(String html) {
        Matcher matcher = Pattern.compile("(?is)<title[^>]*>(.*?)</title>").matcher(html);
        return matcher.find() ? matcher.group(1).replaceAll("\\s+", " ").trim() : "未获取到标题";
    }

    static String join(String[] args, int start) {
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < args.length; i++) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(args[i]);
        }
        return sb.toString();
    }

    // ========================= 异常体系 =========================
    static class CrawlerException extends Exception {
        CrawlerException(String message) {
            super(message);
        }

        CrawlerException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    static class NetworkException extends CrawlerException {
        NetworkException(String message) {
            super(message);
        }

        NetworkException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    static class ParseException extends CrawlerException {
        ParseException(String message) {
            super(message);
        }
    }

    static class StorageException extends CrawlerException {
        StorageException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    static class ValidationException extends CrawlerException {
        ValidationException(String message) {
            super(message);
        }
    }
}
