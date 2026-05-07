# Skills Directory

这个目录存放的是给 Codex 使用的项目技能，不是游戏业务文档目录。

当前建议优先使用的项目技能：

- `xiuxian-project-context`
- `xiuxian-bug-locate`
- `xiuxian-api-trace`
- `xiuxian-feature-implementation`
- `xiuxian-development-direction`
- `project-development-direction`
- `xiuxian-codereview-direct-fix`

快速入口看 [INDEX.md](D:/个人/充电/练手项目/xiuxian/skills/INDEX.md)。

补充说明：

- 核心 skill 现在已经开始使用 `references/` 做按需加载。
- `SKILL.md` 保持轻量，项目结构、验证命令、缺陷模式和实现清单优先放在 `references/*.md`。
- 当前 7 个项目技能都已具备合法 frontmatter，且都已补齐 `agents/openai.yaml`。

维护约束：

- `SKILL.md` 只写触发条件、流程、约束和输出模板。
- 技能内容必须贴合这个仓库的真实结构，不写泛化空话。
- 如果仓库结构、验证命令或高频坑点变化，要同步更新对应 skill。
- 不要把普通项目文档、玩法设计文档或业务资料直接放进 `skills/README.md`。
