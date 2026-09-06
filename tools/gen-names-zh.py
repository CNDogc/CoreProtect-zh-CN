#!/usr/bin/env python3
"""从 Minecraft 官方 zh_cn.json 生成 ZhNameMapper 的名称映射资源。

用法:
    python tools/gen-names-zh.py <zh_cn.json路径> [输出路径]

数据源获取方式（以 MC 26.2 为例）:
    1. 版本清单: https://piston-meta.mojang.com/mc/game/version_manifest_v2.json
    2. 版本 json -> assetIndex.url;  3. 资源索引中取 minecraft/lang/zh_cn.json 的 hash;
    4. 下载 https://resources.download.minecraft.net/<hash前2位>/<hash>

输出: UTF-8 文本，含 [material] 与 [entity] 两个区段，行格式为 key=中文
（key 为小写英文注册名，按字母排序）。方块/物品共用 material 区段，
实体走 entity 区段，避免"生鸡肉/鸡"这类实体与物品同名异译互相覆盖。
"""
import json
import sys


def main() -> int:
    if len(sys.argv) < 2:
        print(__doc__)
        return 2
    src = sys.argv[1]
    dst = sys.argv[2] if len(sys.argv) > 2 else "src/main/resources/lang/names-zh-cn.txt"

    with open(src, encoding="utf-8-sig") as f:
        data = json.load(f)

    material: dict[str, str] = {}
    entity: dict[str, str] = {}
    collisions = []
    for key, value in data.items():
        if not value:
            continue
        if key.startswith("block.minecraft."):
            name = key[len("block.minecraft."):]
            if name and "." not in name:
                if name in material and material[name] != value:
                    collisions.append(f"{name}: {material[name]} / {value}")
                material.setdefault(name, value)
        elif key.startswith("item.minecraft."):
            name = key[len("item.minecraft."):]
            if name and "." not in name:
                if name in material and material[name] != value:
                    collisions.append(f"{name}: {material[name]} / {value}")
                material.setdefault(name, value)
        elif key.startswith("entity.minecraft."):
            name = key[len("entity.minecraft."):]
            if name and "." not in name:
                entity[name] = value

    # 墙式旗帜无独立翻译键，客户端复用站立旗帜的名称
    aliases = {}
    for name, value in material.items():
        if name.endswith("_banner") and not name.endswith("_wall_banner"):
            aliases[f"{name[:-len('_banner')]}_wall_banner"] = value
    for k, v in aliases.items():
        material.setdefault(k, v)

    header = (
        f"# Minecraft 简中名称映射（来源: 官方 zh_cn.json, material {len(material)} 条 / entity {len(entity)} 条）\n"
        "# 生成: python tools/gen-names-zh.py <zh_cn.json>\n"
        "# 未收录的名称（modded 方块等）由 ZhNameMapper 回退英文\n\n"
    )
    body = "[material]\n"
    body += "".join(f"{k}={v}\n" for k, v in sorted(material.items()))
    body += "\n[entity]\n"
    body += "".join(f"{k}={v}\n" for k, v in sorted(entity.items()))
    with open(dst, "w", encoding="utf-8", newline="\n") as f:
        f.write(header + body)

    print(f"已生成 {dst}: material {len(material)} 条, entity {len(entity)} 条")
    if collisions:
        print(f"方块/物品同名冲突 {len(collisions)} 条（保留先出现的 block 侧）:")
        for c in collisions[:10]:
            print("  " + c)
    return 0


if __name__ == "__main__":
    sys.exit(main())
