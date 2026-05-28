package com.crawler;

import java.io.*;
import java.net.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.regex.*;

// CLI + MVC + Command + Strategy + Exception，单文件版课程项目源码
public class App {
    public static void main(String[] args) {
        new Controller(new View()).run();
    }

    // MVC: Controller
    static class Controller {
        View view;
        Storage storage = new Storage("data");
        StrategyFactory factory = new StrategyFactory();
        Map<String, Command> commands = new LinkedHashMap<>();

        Controller(View view) {
            this.view = view;
            commands.put("help", a -> {
                commands.forEach((k, v) -> view.out(k + " - " + v.desc()));
                return true;
            });
            commands.put("crawl", new CrawlCommand(factory, storage, view));
            commands.put("batch", new BatchCommand(factory, storage, view));
            commands.put("exit", a -> {
                view.out("程序结束");
                return false;
            });
        }

        void run() {
            view.out("Java爬虫项目，输入 help 查看命令");
            boolean running = true;
            while (running) {
                try {
                    String line = view.in("> ");
                    if (line.isBlank()) continue;
                    String[] args = line.trim().split("\\s+");
                    Command cmd = commands.get(args[0]);
                    if (cmd == null) throw new ValidateError("未知命令：" + args[0]);
                    running = cmd.exec(args);
                } catch (CrawlerError e) {
                    view.out("[错误] " + e.getMessage());
                } catch (Exception e) {
                    view.out("[系统错误] " + e.getMessage());
                }
            }
        }
    }

    // MVC: View
    static class View {
        Scanner scanner = new Scanner(System.in);
        String in(String tip) {
            System.out.print(tip);
            return scanner.hasNextLine() ? scanner.nextLine() : "exit";
        }
        void out(String s) { System.out.println(s); }
        void result(Result r, Path file) {
            String text = r.content.length() > 160 ? r.content.substring(0, 160) + "..." : r.content;
            out("\n完成：" + r.title + "\n来源：" + r.source + "\n保存：" + file.toAbsolutePath() + "\n预览：" + text + "\n");
        }
    }

    // MVC: Model
    static class Task {
        String type, source, name;
        Task(String type, String source, String name) {
            this.type = type; this.source = source; this.name = name;
        }
    }

    static class Result {
        String way, source, title, content;
        LocalDateTime time = LocalDateTime.now();
        Result(String way, String source, String title, String content) {
            this.way = way; this.source = source; this.title = title; this.content = content;
        }
        String fileText() {
            return "方式：" + way + "\n来源：" + source + "\n时间："
                    + time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                    + "\n标题：" + title + "\n\n" + content;
        }
    }

    // Command 模式
    interface Command {
        boolean exec(String[] args) throws CrawlerError;
        default String desc() { return "执行命令"; }
    }

    static class CrawlCommand implements Command {
        StrategyFactory factory; Storage storage; View view;
        CrawlCommand(StrategyFactory factory, Storage storage, View view) {
            this.factory = factory; this.storage = storage; this.view = view;
        }
        public boolean exec(String[] args) throws CrawlerError {
            if (args.length < 3) throw new ValidateError("用法：crawl <html|title|stock> <url|股票代码> [名称]");
            CrawlStrategy strategy = factory.get(args[1]);
            if (strategy == null) throw new ValidateError("不支持：" + args[1]);
            String name = args.length > 3 ? join(args, 3) : "";
            Result r = strategy.crawl(args[2], name);
            view.result(r, storage.save(r));
            return true;
        }
        public String desc() { return "单次爬取"; }
    }

    static class BatchCommand implements Command {
        StrategyFactory factory; Storage storage; View view;
        BatchCommand(StrategyFactory factory, Storage storage, View view) {
            this.factory = factory; this.storage = storage; this.view = view;
        }
        public boolean exec(String[] args) {
            List<Task> tasks = List.of(
                    new Task("title", "https://www.example.com", "Example"),
                    new Task("html", "https://www.iana.org/domains/reserved", "IANA"),
                    new Task("title", "https://www.rfc-editor.org", "RFC"));
            int ok = 0;
            for (Task t : tasks) {
                try {
                    Result r = factory.get(t.type).crawl(t.source, t.name);
                    view.result(r, storage.save(r));
                    ok++;
                } catch (CrawlerError e) {
                    view.out(t.name + " 失败：" + e.getMessage());
                }
            }
            view.out("批量完成，成功爬取 " + ok + " 个网站");
            return true;
        }
        public String desc() { return "批量爬取3个以上网站"; }
    }

    // Strategy 模式
    interface CrawlStrategy {
        Result crawl(String source, String name) throws CrawlerError;
    }

    static class StrategyFactory {
        Map<String, CrawlStrategy> map = Map.of(
                "html", new HtmlStrategy(),
                "title", new TitleStrategy(),
                "stock", new StockStrategy());
        CrawlStrategy get(String type) { return map.get(type); }
    }

    static class HtmlStrategy implements CrawlStrategy {
        public Result crawl(String url, String name) throws CrawlerError {
            String html = Http.get(url, StandardCharsets.UTF_8);
            String title = title(html);
            String text = html.replaceAll("(?is)<script.*?</script>|<style.*?</style>", " ")
                    .replaceAll("<[^>]+>", " ")
                    .replace("&nbsp;", " ")
                    .replace("&amp;", "&")
                    .replaceAll("\\s+", " ")
                    .trim();
            if (text.isEmpty()) throw new ParseError("未解析到正文");
            return new Result("网页正文策略", url, name.isBlank() ? title : name, text);
        }
    }

    static class TitleStrategy implements CrawlStrategy {
        public Result crawl(String url, String name) throws CrawlerError {
            String title = title(Http.get(url, StandardCharsets.UTF_8));
            return new Result("网页标题策略", url, title, "网页标题：" + title);
        }
    }

    static class StockStrategy implements CrawlStrategy {
        public Result crawl(String code, String name) throws CrawlerError {
            String url = "https://hq.sinajs.cn/list=" + code;
            String data = Http.get(url, Charset.forName("GBK"));
            int s = data.indexOf('"'), e = data.lastIndexOf('"');
            if (s < 0 || e <= s) throw new ParseError("股票接口无数据");
            String[] p = data.substring(s + 1, e).split(",");
            if (p.length < 10) throw new ParseError("股票字段不足");
            String text = "股票：" + p[0] + "\n最新价：" + p[3] + "\n最高价：" + p[4]
                    + "\n最低价：" + p[5] + "\n成交量：" + p[8] + "\n成交额：" + p[9];
            return new Result("股票策略", url, name.isBlank() ? p[0] : name, text);
        }
    }

    // 文件保存
    static class Storage {
        Path dir;
        Storage(String dir) { this.dir = Paths.get(dir); }
        Path save(Result r) throws SaveError {
            try {
                Files.createDirectories(dir);
                String time = r.time.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                Path file = dir.resolve(time + "_" + r.title.replaceAll("[\\\\/:*?\"<>|\\s]+", "_") + ".txt");
                Files.writeString(file, r.fileText(), StandardCharsets.UTF_8);
                return file;
            } catch (IOException e) {
                throw new SaveError("保存失败", e);
            }
        }
    }

    // HTTP 工具
    static class Http {
        static String get(String url, Charset charset) throws NetError {
            HttpURLConnection c = null;
            try {
                c = (HttpURLConnection) URI.create(url).toURL().openConnection();
                c.setRequestMethod("GET");
                c.setConnectTimeout(10000);
                c.setReadTimeout(10000);
                c.setRequestProperty("User-Agent", "Mozilla/5.0 JavaCrawler");
                if (url.contains("sinajs")) c.setRequestProperty("Referer", "https://finance.sina.com.cn/");
                if (c.getResponseCode() != 200) throw new NetError("HTTP状态码：" + c.getResponseCode());
                BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream(), charset));
                StringBuilder sb = new StringBuilder();
                for (String line; (line = br.readLine()) != null; ) sb.append(line).append('\n');
                br.close();
                return sb.toString();
            } catch (CrawlerError e) {
                throw e;
            } catch (Exception e) {
                throw new NetError("网络请求失败：" + e.getMessage(), e);
            } finally {
                if (c != null) c.disconnect();
            }
        }
    }

    static String title(String html) {
        Matcher m = Pattern.compile("(?is)<title[^>]*>(.*?)</title>").matcher(html);
        return m.find() ? m.group(1).replaceAll("\\s+", " ").trim() : "未获取到标题";
    }

    static String join(String[] a, int start) {
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < a.length; i++) sb.append(i == start ? "" : " ").append(a[i]);
        return sb.toString();
    }

    // 异常体系
    static class CrawlerError extends Exception {
        CrawlerError(String m) { super(m); }
        CrawlerError(String m, Throwable e) { super(m, e); }
    }
    static class NetError extends CrawlerError {
        NetError(String m) { super(m); }
        NetError(String m, Throwable e) { super(m, e); }
    }
    static class ParseError extends CrawlerError {
        ParseError(String m) { super(m); }
    }
    static class SaveError extends CrawlerError {
        SaveError(String m, Throwable e) { super(m, e); }
    }
    static class ValidateError extends CrawlerError {
        ValidateError(String m) { super(m); }
    }
}
