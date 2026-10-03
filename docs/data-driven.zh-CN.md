# 数据驱动教程 — TOMC-Leveling

TOMC-Leveling 的属性完全由 JSON 文件定义，因此你无需改动任何代码即可新增属性、删除已有属性或重新平衡其成长曲线。本文档说明其格式。

## 目录

- [1. 属性定义（`attribute_definition`）](#1-属性定义attribute_definition)
- [内置属性](#内置属性)

---

## 1. 属性定义（`attribute_definition`）

属性定义告诉 TOMC-Leveling 玩家可以投入哪些属性、每级消耗多少点数，以及属性每级如何成长。

**位置** —— `data/leveling/**/*.json`（`leveling` 命名空间下任意子目录、以 `.json` 结尾）。定义会在 `/reload` 时重新加载。

### 字段

| 字段 | 类型 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `attribute` | string | *必需* | 属性 ID，例如 `minecraft:max_health`。必须是有效的资源位置。 |
| `operation` | string | *必需* | `add_value`（加法）或 `add_multiplied_total`（百分比）。 |
| `value_per_level` | number | *二选一* | 每级固定增加的数值。 |
| `values` | number[] | *二选一* | 按级取值的数组；超出最后一个的等级沿用末值。 |
| `cost` | int | `1` | 每级消耗的点数。必须 ≥ 0。 |
| `order` | int | *无* | 面板中的排序位置（升序；缺省排最后）。 |
| `display_key` | string | *无* | 属性显示名的译名键。 |
| `description_key` | string | *无* | 属性描述的译名键。 |
| `translation_key` | string | *无* | 旧版回退译名键。 |

### 语义

- `value_per_level` 与 `values` 互斥：必须**恰好提供其一**。两者都提供或都不提供会导致该文件加载失败。
- `values` 必须是**非空的数字数组**。
- `operation` 必须为 `add_value` 或 `add_multiplied_total`，其他值会导致加载失败。
  - `add_value` 将等级数值直接加到属性上。
  - `add_multiplied_total` 将属性乘以 `1 + 数值`——因此 `0.05` 表示 `+5%`。
- `attribute` 必须能解析为有效的资源位置（例如 `minecraft:max_health`）。
- 显示名解析顺序：`display_key` → `translation_key` → `attribute.name.<path>`（取第一个存在的键）。
- 描述解析顺序：`description_key` → `<display_key>.description`。
- 校验失败的文件会被跳过，记录一条警告，并将错误报告给游戏内的操作员。

### 示例

一个纯加法的属性——`minecraft:max_health` 每级增加 `2.0`：

```json
{
  "attribute": "minecraft:max_health",
  "operation": "add_value",
  "value_per_level": 2.0,
  "cost": 1,
  "order": 1
}
```

一个百分比属性——`minecraft:attack_damage` 每级增加 `5%`：

```json
{
  "attribute": "minecraft:attack_damage",
  "operation": "add_multiplied_total",
  "value_per_level": 0.05,
  "cost": 1,
  "order": 3
}
```

使用 `values` 数组的分级成长——例如 `minecraft:luck` 在第 1–3 级分别增加 `0.2`、`0.4`、`0.6`，之后每级都保持 `0.6`：

```json
{
  "attribute": "minecraft:luck",
  "operation": "add_value",
  "values": [0.2, 0.4, 0.6],
  "cost": 1,
  "order": 6
}
```

带有显式显示名与描述的完整定义：

```json
{
  "attribute": "minecraft:max_health",
  "display_key": "attribute.name.max_health",
  "description_key": "attribute.name.max_health.description",
  "operation": "add_value",
  "value_per_level": 2.0,
  "cost": 1,
  "order": 1
}
```

## 内置属性

TOMC-Leveling 自带六种属性定义，每级均消耗 `1` 点：

| 属性 | 运算 | 成长 |
| --- | --- | --- |
| `minecraft:max_health` | `add_value` | 每级 `+2.0` |
| `minecraft:armor` | `add_value` | 每级 `+1.0` |
| `minecraft:attack_damage` | `add_multiplied_total` | 每级 `+5%` |
| `minecraft:movement_speed` | `add_multiplied_total` | 每级 `+2%` |
| `minecraft:oxygen_bonus` | `add_value` | 每级 `+0.25` |
| `minecraft:luck` | `add_value` | 每级 `+0.2` |
