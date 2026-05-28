package com.crawler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Java 期末爬虫项目。
 *
 * 这个单文件源码完整体现课程要求：
 * CLI + MVC + Command 模式 + 策略模式 + 自定义异常体系 + 文件保存。
 */
public class App {
    public static void main(String[] args) {
        ConsoleView view = new ConsoleView(System.in, System.out);
        CrawlerController controller = new CrawlerController(view);
        controller.start();
    }

    /**
     * MVC: Controller，负责命令调度和异常统一处理。
     */
    static class CrawlerController {
        private final ConsoleView view;
        private final List<String> history = new ArrayList<>();
        private final Map<String, Command> commands = new LinkedHashMap<>();

        CrawlerController(ConsoleView view) {
            this.view = view;
            CrawlerStrategyFactory strategyFactory = new CrawlerStrategyFactory();
            ResultStorage storage = new ResultStorage(Paths.get("data"));

            register(new CrawlCommand(strategyFactory, storage, view));
            register(new BatchCommand(strategyFactory, storage, view));
            register(new HistoryCommand(view, history));
            register(new ExitCommand(view));
            register(new HelpCommand(view, commands.values()));
        }

        void start() {
            view.showWelcome();
            boolean running = true;
            while (running) {
                String line = view.readCommand();
                if (line.isBlank()) {
                    continue;
                }

                history.add(line);
                try {
                    running = dispatch(line);
                } catch (CrawlerException e) {
                    view.showError(e.getMessage());
                } catch (RuntimeException e) {
                    view.showError("系统异常：" + e.getMessage());
                }
            }
        }

        private boolean dispatch(String line) throws CrawlerException {
            String[] args = line.split("\\s+");
            Command command = commands.get(args[0].toLowerCase());
            if (command == null) {
                throw new ValidationException("未知命令：" + args[0] + "，请输入 help 查看帮助。");
            }
            return command.execute(args);
        }

        private void register(Command command) {
            commands.put(command.name(), command);
        }
    }

    /**
     * MVC: View，负责命令行输入输出。
     */
    static class ConsoleView {
        private final Scanner scanner;
        private final PrintStream out;

        ConsoleView(InputStream inputStream, PrintStream out) {
            this.scanner = new Scanner(inputStream);
            this.out = out;
        }

        void showWelcome() {
            out.println("Java 期末爬虫项目");
            out.println("CLI + MVC + Command + Strategy + Exception");
            out.println("输入 help 查看命令。");
        }

        String readCommand() {
            out.print("> ");
            if (!scanner.hasNextLine()) {
                return "exit";
            }
            return scanner.nextLine().trim();
        }

        void showMessage(String message) {
            out.println(message);
        }

        void showError(String message) {
            out.println("[错误] " + message);
        }

        void showResult(CrawlResult result, Path savedFile) {
            out.println();
            out.println("爬取完成：" + result.title);
            out.println("来源：" + result.source);
            out.println("方式：" + result.strategyName);
            out.println("保存文件：" + savedFile.toAbsolutePath());
            out.println("内容预览：");
            out.println(result.content.length() > 300 ? result.content.substring(0, 300) + "..." : result.content);
            out.println();
        }
    }

    /**
     * MVC: Model，表示一次爬取任务。
     */
    static class CrawlTask {
        final String strategyKey;
        final String source;
        final String displayName;

        CrawlTask(String strategyKey, String source, String displayName) {
            this.strategyKey = strategyKey;
            this.source = source;
            this.displayName = displayName;
        }
    }

    /**
     * MVC: Model，表示爬取结果。
     */
    static class CrawlResult {
        final String strategyName;
        final String source;
        final String title;
        final String content;
        final LocalDateTime crawledAt;

        CrawlResult(String strategyName, String source, String title, String content) {
            this.strategyName = strategyName;
            this.source = source;
            this.title = title;
            this.content = content;
            this.crawledAt = LocalDateTime.now();
        }

        String toFileText() {
            return "爬取方式：" + strategyName + System.lineSeparator()
                    + "数据来源：" + source + System.lineSeparator()
                    + "爬取时间：" + crawledAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + System.lineSeparator()
                    + "标题：" + title + System.lineSeparator()
                    + "内容：" + System.lineSeparator()
                    + content + System.lineSeparator();
        }
    }

    /**
     * Command 模式：所有命令的统一接口。
     */
    interface Command {
        String name();

        String description();

        boolean execute(String[] args) throws CrawlerException;
    }

    static class CrawlCommand implements Command {
        private final CrawlerStrategyFactory strategyFactory;
        private final ResultStorage storage;
        private final ConsoleView view;

        CrawlCommand(CrawlerStrategyFactory strategyFactory, ResultStorage storage, ConsoleView view) {
            this.strategyFactory = strategyFactory;
            this.storage = storage;
            this.view = view;
        }

        public String name() {
            return "crawl";
        }

        public String description() {
            return "爬取数据：crawl <html|title|stock> <url|stockCode> [名称]";
        }

        public boolean execute(String[] args) throws CrawlerException {
            if (args.length < 3) {
                throw new ValidationException("用法：crawl <html|title|stock> <url|stockCode> [名称]");
            }

            CrawlStrategy strategy = strategyFactory.get(args[1]);
            if (strategy == null) {
                throw new ValidationException("不支持的爬取策略：" + args[1]);
            }

            String displayName = args.length >= 4 ? join(args, 3) : "";
            CrawlResult result = strategy.crawl(args[2], displayName);
            Path savedFile = storage.save(result);
            view.showResult(result, savedFile);
            return true;
        }

        private String join(String[] args, int start) {
            StringBuilder builder = new StringBuilder();
            for (int i = start; i < args.length; i++) {
                if (builder.length() > 0) {
                    builder.append(' ');
                }
                builder.append(args[i]);
            }
            return builder.toString();
        }
    }

    static class BatchCommand implements Command {
        private final CrawlerStrategyFactory strategyFactory;
        private final ResultStorage storage;
        private final ConsoleView view;

        BatchCommand(CrawlerStrategyFactory strategyFactory, ResultStorage storage, ConsoleView view) {
            this.strategyFactory = strategyFactory;
            this.storage = storage;
            this.view = view;
        }

        public String name() {
            return "batch";
        }

        public String description() {
            return "按预设任务爬取 3 个以上网站并保存文件";
        }

        public boolean execute(String[] args) {
            List<CrawlTask> tasks = List.of(
                    new CrawlTask("title", "https://www.example.com", "Example 官网"),
                    new CrawlTask("html", "https://www.example.com", "Example Domain"),
                    new CrawlTask("html", "https://www.iana.org/domains/reserved", "IANA 保留域名说明"),
                    new CrawlTask("title", "https://www.rfc-editor.org", "RFC Editor 首页"));

            int success = 0;
            for (CrawlTask task : tasks) {
                try {
                    CrawlStrategy strategy = strategyFactory.get(task.strategyKey);
                    CrawlResult result = strategy.crawl(task.source, task.displayName);
                    Path savedFile = storage.save(result);
                    view.showResult(result, savedFile);
                    success++;
                } catch (CrawlerException e) {
                    view.showError(task.displayName + " 爬取失败：" + e.getMessage());
                }
            }
            view.showMessage("批量爬取结束，成功任务数：" + success);
            return true;
        }
    }

    static class HelpCommand implements Command {
        private final ConsoleView view;
        private final Collection<Command> commands;

        HelpCommand(ConsoleView view, Collection<Command> commands) {
            this.view = view;
            this.commands = commands;
        }

        public String name() {
            return "help";
        }

        public String description() {
            return "查看所有命令";
        }

        public boolean execute(String[] args) {
            view.showMessage("可用命令：");
            for (Command command : commands) {
                view.showMessage("  " + command.name() + " - " + command.description());
            }
            return true;
        }
    }

    static class HistoryCommand implements Command {
        private final ConsoleView view;
        private final List<String> history;

        HistoryCommand(ConsoleView view, List<String> history) {
            this.view = view;
            this.history = history;
        }

        public String name() {
            return "history";
        }

        public String description() {
            return "查看本次运行输入过的命令";
        }

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

    static class ExitCommand implements Command {
        private final ConsoleView view;

        ExitCommand(ConsoleView view) {
            this.view = view;
        }

        public String name() {
            return "exit";
        }

        public String description() {
            return "退出程序";
        }

        public boolean execute(String[] args) {
            view.showMessage("程序已退出。");
            return false;
        }
    }

    /**
     * 策略模式：不同爬取方式实现同一个接口。
     */
    interface CrawlStrategy {
        String getKey();

        String getName();

        CrawlResult crawl(String source, String displayName) throws CrawlerException;
    }

    static class CrawlerStrategyFactory {
        private final Map<String, CrawlStrategy> strategies = new LinkedHashMap<>();

        CrawlerStrategyFactory() {
            register(new HtmlTextStrategy());
            register(new TitleStrategy());
            register(new SinaStockStrategy());
        }

        CrawlStrategy get(String key) {
            return strategies.get(key.toLowerCase());
        }

        private void register(CrawlStrategy strategy) {
            strategies.put(strategy.getKey(), strategy);
        }
    }

    static abstract class AbstractHttpStrategy implements CrawlStrategy {
        String get(String url, Charset charset) throws NetworkException {
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
                        try {
                            return Charset.forName(trimmed.substring("charset=".length()));
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

    static class HtmlTextStrategy extends AbstractHttpStrategy {
        public String getKey() {
            return "html";
        }

        public String getName() {
            return "普通网页正文爬取";
        }

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
            String resultTitle = displayName == null || displayName.isBlank() ? title : displayName;
            return new CrawlResult(getName(), source, resultTitle, content);
        }

        String extractTitle(String html) {
            java.util.regex.Matcher matcher = java.util.regex.Pattern
                    .compile("(?is)<title[^>]*>(.*?)</title>")
                    .matcher(html);
            if (matcher.find()) {
                return matcher.group(1).replaceAll("\\s+", " ").trim();
            }
            return "未获取到标题";
        }
    }

    static class TitleStrategy extends HtmlTextStrategy {
        public String getKey() {
            return "title";
        }

        public String getName() {
            return "网页标题快速爬取";
        }

        public CrawlResult crawl(String source, String displayName) throws CrawlerException {
            String html = get(source, null);
            String title = extractTitle(html);
            return new CrawlResult(getName(), source, title, "网页标题：" + title);
        }
    }

    static class SinaStockStrategy extends AbstractHttpStrategy {
        public String getKey() {
            return "stock";
        }

        public String getName() {
            return "新浪财经股票数据爬取";
        }

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

    /**
     * Service: 保存爬取结果，满足“数据保存到文件中”的要求。
     */
    static class ResultStorage {
        private final Path outputDirectory;

        ResultStorage(Path outputDirectory) {
            this.outputDirectory = outputDirectory;
        }

        Path save(CrawlResult result) throws StorageException {
            try {
                Files.createDirectories(outputDirectory);
                String timestamp = result.crawledAt.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS"));
                String fileName = timestamp + "_" + sanitize(result.strategyName) + "_" + sanitize(result.title) + ".txt";
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
            return value.length() > 40 ? value.substring(0, 40) : value;
        }
    }

    /**
     * 自定义异常体系。
     */
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
