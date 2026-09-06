#!/usr/bin/env bash
# 校验中文词条文件是否覆盖 Phrase 枚举的全部词条键（i18n-zh 分支维护工具）。
# 用法: bash tools/check-lang-keys.sh   （在仓库根目录或任意位置执行均可）
# 合并上游后运行本脚本，即可知道上游新增了哪些待翻译词条。
set -u
cd "$(dirname "$0")/.."

phrase_file="src/main/java/net/coreprotect/language/Phrase.java"
lang_files=("lang/zh-cn.yml" "src/main/resources/lang/language-zh.yml")

# 从 Phrase.java 提取全部枚举常量名（权威全集）
enum_keys=$(tr -d '\r' < "$phrase_file" | sed -n 's/^[[:space:]]\{4\}\([A-Z][A-Z_0-9]*\)[,;]*/\1/p' | sort -u)
total=$(printf '%s\n' "$enum_keys" | wc -l)
echo "Phrase 枚举词条总数: $total"

fail=0
for f in "${lang_files[@]}"; do
  if [[ ! -f "$f" ]]; then
    echo "[$f] 文件不存在"
    fail=1
    continue
  fi
  declare -A seen=()
  while IFS= read -r k; do
    [[ -n "$k" ]] && seen["$k"]=1
  done < <(tr -d '\r' < "$f" | sed -n 's/^\([A-Z][A-Z_0-9]*\):.*/\1/p')
  miss=0
  while IFS= read -r k; do
    if [[ -z "${seen[$k]:-}" ]]; then
      [[ $miss -eq 0 ]] && echo "[$f] 缺少以下词条:"
      echo "  $k"
      miss=$((miss + 1))
    fi
  done < <(printf '%s\n' "$enum_keys")
  echo "[$f] 覆盖 ${#seen[@]}/$total（缺少 $miss）"
  [[ $miss -eq 0 ]] || fail=1
done

# 两份文件应保持内容一致（resources 副本是打进 jar 的默认模板）
if [[ -f "${lang_files[0]}" && -f "${lang_files[1]}" ]] && ! cmp -s "${lang_files[0]}" "${lang_files[1]}"; then
  echo "注意: ${lang_files[0]} 与 ${lang_files[1]} 内容不一致，请同步"
  fail=1
fi

if [[ $fail -eq 0 ]]; then
  echo "OK: 全部词条已覆盖"
fi
exit $fail
