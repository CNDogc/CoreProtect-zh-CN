# 汉化分支维护说明（i18n-zh）

本分支面向中国服务器用户，目标：**默认生成完整中文界面**，同时把对上游
[PlayPro/CoreProtect](https://github.com/PlayPro/CoreProtect) 的改动接触面积压到最小，
方便长期合并上游。

## 设计原则

1. **不改 `Language.java` 的英文默认值**——它是回退基准。中文一律通过 yml 覆盖层生效。
2. 翻译逻辑与数据全部放**新增文件**（`ZhNameMapper`、资源文件），上游文件只留单行挂钩点。
3. `config.yml` 的 `language` 保持默认 `en`：设为 `zh-CN` 会触发向 coreprotect.net
   在线拉取并用其服务端翻译覆盖本地（更完整的）中文。本分支以本地文件为唯一事实来源。
4. 坐标行 `^(x:.../world) (a:block)` 有意保持原样，不汉化。

## 本分支相对上游的全部差异（合并上游后照单复查）

### 新增文件（git 视为无冲突）
| 文件 | 用途 |
| --- | --- |
| `LOCALIZATION.md` | 本文件 |
| `lang/zh-cn.yml` | 补全为 235/235 完整简中词条（上游原文件不全） |
| `src/main/resources/lang/language-zh.yml` | 完整中文词条，首次运行生成默认 language.yml 用 |
| `src/main/resources/lang/names-zh-cn.txt` | 方块/物品/实体英文名 → 简中名映射（来自官方 zh_cn.json） |
| `src/main/java/net/coreprotect/utility/ZhNameMapper.java` | 名称映射查询类 |
| `tools/check-lang-keys.sh` | 词条键覆盖率校验脚本 |

### 上游文件的单行挂钩点
| 文件 | 位置 | 改动 |
| --- | --- | --- |
| `plugin.yml` | 3 处 `permission-message` | 文案汉化 |
| `utility/MaterialUtils.java` | `getBlockDisplayName` 返回行 | 包一层 `ZhNameMapper.material(...)` |
| `command/lookup/StandardLookupThread.java` | 容器物品名、实体名、汇总回退名（约 :459/:547/:695） | 各包一层映射 |
| `database/lookup/BlockLookup.java` | 实体名（约 :139）、"无数据"提示方块名（约 :179） | 各包一层映射 |
| `database/lookup/EntityInteractionLookup.java` | `entityName` 返回行 | 包一层 `ZhNameMapper.entity(...)` |
| `utility/ItemUtils.java` | `getEnchantments` 回退名（约 :709） | 换用映射查询 |
| `config/ConfigFile.java` | `loadFiles` else 分支 | 首次运行复制内置中文模板 |
| `config/Config.java` | `HEADERS` 静态块（约 :178-226） | config.yml 注释汉化（唯一块状改动） |

## 合并上游后的固定动作

```bash
git fetch origin && git rebase origin/master   # 或 merge
bash tools/check-lang-keys.sh                  # 跑键校验，补上游新增词条的翻译
mvn package                                    # 重新编译验证
```

上游新增短语时：翻译补进 `lang/zh-cn.yml` 与
`src/main/resources/lang/language-zh.yml` 两处（保持一致）；
缺失的键会在已有服务器的 language.yml 里以英文回退，不会报错。

上游新增方块/实体时：重新生成 `names-zh-cn.txt`（数据源见脚本头注释），
新名称在映射更新前自动回退英文。

## 已知边界

- modded 方块/实体（CraftEngine、MythicMobs 自定义等）不在官方语言文件内，回退英文名。
- 悬停提示里的附魔/药水效果文本未汉化（仅物品回退名已汉化）。
- `CoreProtect` 品牌字样、玩家名、世界名、坐标保持原样。

## 名称映射覆盖率（paper-api 26.2.build.48-alpha，2026-09 实测）

| 类别 | 覆盖 | 说明 |
| --- | --- | --- |
| `org.bukkit.Material`（排除 legacy_*） | 1691/1691 | 100%；`*_wall_banner` 通过站立旗帜别名补齐 |
| `org.bukkit.entity.EntityType` | 158/159 | 唯一未映射是 CoreProtect 内部伪类型 `unknown` |
| 词条（Phrase） | 235/235 | `bash tools/check-lang-keys.sh` 校验 |

名称数据来源：Mojang 官方 MC 26.2 `zh_cn.json`（经资源索引 CDN 下载，
生成命令见 `tools/gen-names-zh.py` 头注释）。`wheat` 存在方块/物品异译
（小麦植株/小麦），保留方块侧。
