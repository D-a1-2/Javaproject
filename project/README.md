# Java 期末爬虫项目

本项目实现了一个基于 CLI 的多策略爬虫程序，符合课程要求中的 MVC、Command 模式、策略模式、异常体系、三站点爬取和文件保存要求。

## 编译运行

使用 Maven：

```bash
mvn compile
mvn exec:java -Dexec.mainClass="com.crawler.App"
```

如果没有 Maven，也可以直接使用 JDK：

```bash
javac -encoding UTF-8 -d out $(find src/main/java -name "*.java")
java -cp out com.crawler.App
```

Windows PowerShell：

```powershell
$files = Get-ChildItem -Recurse src/main/java -Filter *.java | ForEach-Object FullName
javac -encoding UTF-8 -d out $files
java -cp out com.crawler.App
```

## 命令说明

- `help`：查看命令帮助。
- `crawl html <url>`：爬取普通网页并提取标题、正文摘要。
- `crawl title <url>`：快速抓取网页标题。
- `crawl stock <stockCode> [stockName]`：爬取新浪财经股票数据，例如 `crawl stock sh600519 贵州茅台`。
- `batch`：按预设任务一次性爬取 3 个以上网站。
- `history`：查看本次运行历史。
- `exit`：退出程序。

爬取结果默认保存到 `data/` 目录中。
