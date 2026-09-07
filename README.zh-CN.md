# CoreProtect 简体中文汉化分支（i18n-zh）

> 本分支是 [PlayPro/CoreProtect](https://github.com/PlayPro/CoreProtect) 的非官方简体中文汉化分支，
> 基于 CoreProtect 24.0（master）。目标：**开箱即用的完整中文界面**，同时把对上游代码的改动
> 压到最小，保证可以长期顺利合并上游更新。

## 这是什么

CoreProtect 是 Minecraft 服务器最流行的方块查询/回滚（反熊）插件，但官方没有内置简体中文。
本分支在不破坏其架构的前提下，让插件**默认输出完整简体中文**：

- **界面词条 235/235 全量汉化** —— 全部命令反馈、帮助信息、警告提示均为简体中文；
- **方块/物品/实体名称汉化** —— 查询结果中的名称映射为 Mojang 官方简体中文译名
  （如 `stone` → 石头、`creeper` → 苦力怕/爬行者）；
- **首次运行即中文** —— 首次启动自动生成中文版 `language.yml`，无需手动下载语言文件，
  也不依赖 coreprotect.net 的在线翻译服务；
- **`config.yml` 注释汉化** —— 配置文件头部说明为中文，便于理解各配置项；
- **命令权限提示汉化** —— 无权限使用命令时的提示为中文。

设计取向：日志坐标行（`x: ... y: ...`、世界名、时间等）与玩家名、`CoreProtect` 品牌字样
**保持原样**——这些内容机器可读、便于脚本处理，汉化反而有害。

## 与上游的差异（合并时复查清单）

所有改动遵循一个原则：**逻辑与数据尽量放新增文件，上游文件只留最小挂钩点**。

### 新增文件（与上游无冲突）

| 文件 | 用途 |
| --- | --- |
| `README.zh-CN.md` / `LOCALIZATION.md` | 本说明 / 汉化分支维护文档 |
| `lang/zh-cn.yml` | 补全为 235/235 完整简中词条 |
| `src/main/resources/lang/language-zh.yml` | 完整中文词条，首次运行生成默认 language.yml |
| `src/main/resources/lang/names-zh-cn.txt` | 方块/物品/实体英文名 → 官方简中名映射 |
| `src/main/java/net/coreprotect/utility/ZhNameMapper.java` | 名称映射查询类 |
| `tools/check-lang-keys.sh` | 词条键覆盖率校验脚本 |
| `tools/gen-names-zh.py` | 从 MC 官方 `zh_cn.json` 生成名称映射的脚本 |

### 上游文件的挂钩点（合并冲突面）

- `plugin.yml`：3 处 `permission-message` 文案汉化；
- `MaterialUtils.java` / `ItemUtils.java` / `StandardLookupThread.java` /
  `BlockLookup.java` / `EntityInteractionLookup.java`：各 1–3 处，在原返回值外包一层
  `ZhNameMapper.material(...)` / `ZhNameMapper.entity(...)` 映射调用；
- `ConfigFile.java`：首次运行时复制内置中文模板（约 15 行）；
- `Config.java`：`HEADERS` 静态块内 config.yml 注释汉化（唯一的块状改动）。

不改 `Language.java` 的任何英文默认值——它是缺失词条时的回退基准；全部中文通过 yml 覆盖层生效。

## 使用方法

1. 从本分支构建 jar：`mvn package`（产物在 `target/CoreProtect-*.jar`），或直接下载 Release；
2. 像原版一样放入服务器 `plugins/` 目录，重启服务器；
3. 无需任何额外配置——首次运行自动生成中文 `language.yml`。

构建要求：JDK 21+、Maven（同上游）。

## 已知边界

- Modded 方块/实体（CraftEngine、MythicMobs 自定义等）不在 Mojang 官方语言文件内，回退英文名；
- 悬停提示里的附魔/药水效果文本未汉化（仅物品回退名已汉化）；
- 上游后续新增的方块/实体，在名称映射更新前自动回退英文，不影响使用。

## 名称映射覆盖率

基于 paper-api 26.2.build.48-alpha（2026-09 实测），数据源为 Mojang 官方 MC 26.2 `zh_cn.json`：

| 类别 | 覆盖 |
| --- | --- |
| `org.bukkit.Material`（排除 legacy_*） | 1691/1691（100%） |
| `org.bukkit.entity.EntityType` | 158/159（唯一未映射为内部伪类型 `unknown`） |
| 界面词条（Phrase） | 235/235 |

## 合并上游 / 维护

```bash
git fetch origin && git merge origin/master   # 合并上游
bash tools/check-lang-keys.sh                 # 校验词条键覆盖，补翻上游新增词条
mvn package                                   # 重新编译验证
```

详细维护规则（上游新增词条/方块时的处理流程）见 [LOCALIZATION.md](LOCALIZATION.md)。

## 许可与致谢

- 原插件 CoreProtect：© Intelli，[Artistic License 2.0](LICENSE)，官网 [coreprotect.net](https://coreprotect.net)；
- 中文词条翻译基于社区既有翻译（Intelli、DreamVoid、StarWishsama、YuanYuanOwO、Halogly）补全；
- 方块/物品/实体名称来自 Mojang 官方简体中文语言文件；
- 本分支与上游官方无隶属关系，请勿向上游反馈本分支引入的问题。
