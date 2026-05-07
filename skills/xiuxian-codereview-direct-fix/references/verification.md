# Verification

```powershell
D:\soft\apache-maven-3.9.12\bin\mvn.cmd compile
D:\soft\apache-maven-3.9.12\bin\mvn.cmd test -DskipITs
D:\soft\apache-maven-3.9.12\bin\mvn.cmd spotbugs:spotbugs
node --check src/main/resources/static/js/xxx.js
```

## 说明

- 如果刚 `clean` 过，要重新生成 SpotBugs 结果。
- `node --check` 用于静态 JS 语法门禁。
- 前端没环境时，仍要做调用链和字段契约核对。
