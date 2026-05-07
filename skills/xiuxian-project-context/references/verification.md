# Verification

## Maven

优先使用：

`D:\soft\apache-maven-3.9.12\bin\mvn.cmd`

## 常用命令

```powershell
D:\soft\apache-maven-3.9.12\bin\mvn.cmd compile
D:\soft\apache-maven-3.9.12\bin\mvn.cmd test -DskipITs
D:\soft\apache-maven-3.9.12\bin\mvn.cmd spotbugs:spotbugs
```

## 前端脚本语法检查

```powershell
node --check src/main/resources/static/js/xxx.js
```

## 注意事项

- 不要默认 `mvn` 已在 `PATH`。
- `node --check` 适合语法检查，不适合运行依赖浏览器环境的脚本。
- 如果执行过 `mvn clean`，需要重新跑 SpotBugs 后再看 `target/spotbugsXml.xml`。
- `logs/` 是后端排查的重要辅助目录。
