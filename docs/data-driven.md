# 步步高升 · 数据驱动文档

**TOMC-Leveling Data-Driven Reference**

适用版本：Fabric 1.21.11  
模组名称：TOMC-Leveling / 步步高升  
作者：ThatOne、DeepSeek Harness  
协议：GPLv3

---

## 目录

1. [这份文档面向谁](#1-这份文档面向谁)
2. [快速开始](#2-快速开始)
3. [目录结构与加载](#3-目录结构与加载)
4. [完整字段表](#4-完整字段表)
5. [字段详解](#5-字段详解)
6. [运算规则与数值计算](#6-运算规则与数值计算)
7. [重复声明、覆盖与优先级](#7-重复声明覆盖与优先级)
8. [错误处理](#8-错误处理)
9. [内置默认定义](#9-内置默认定义)
10. [玩家数据文件](#10-玩家数据文件)
11. [命令参考](#11-命令参考)
12. [面向三类读者的实践指南](#12-面向三类读者的实践指南)
13. [兼容性与注意事项](#13-兼容性与注意事项)
14. [附录：完整示例集](#14-附录完整示例集)

---

## 1. 这份文档面向谁

本文档面向三类读者：

- **期望自定义体验的玩家**：想改数值、改顺序、改文案、加新属性。
- **期望联动的模组创作者**：想让自己的属性被步步高升识别，或读取玩家成长数据。
- **期望定制的整合包创作者**：想集中管理属性定义、成长曲线、文案风格与调试流程。

步步高升把「哪些属性可以加点、每级加多少、消耗多少点、怎么排序、怎么显示」全部交给数据包。你不需要写一行 Java，就能完成绝大多数自定义。

---

## 2. 快速开始

在你的数据包里新建：

```
data/<你的命名空间>/leveling/<任意文件名>.json
```

最小可用示例：

```json
{
  "attribute": "minecraft:max_health",
  "operation": "add_value",
  "cost": 1,
  "value_per_level": 2.0,
  "order": 1
}
```

进游戏执行 `/reload`，打开属性面板即可看到效果。

> 注意：路径中间那段必须是小写 `leveling`，它是步步高升的识别关键字。第一段 `<你的命名空间>` 可以是任意合法命名空间，不要求与本模组相同。

---

## 3. 目录结构与加载

### 3.1 路径规则

```
data/<namespace>/leveling/<file>.json
```

| 部分 | 含义 | 是否可自定义 |
|---|---|---|
| `<namespace>` | 数据包命名空间 | 可任意，建议用你自己的模组 ID |
| `leveling` | 步步高升的识别关键字 | **固定，不可改** |
| `<file>.json` | 文件名 | 可任意，建议与属性同名 |

示例：

```
data/leveling/leveling/max_health.json
data/leveling/leveling/armor.json
data/my_pack/leveling/extra_speed.json
data/my_mod/leveling/custom_attack.json
```

### 3.2 加载时机

- 加载世界时；
- 玩家进入世界前；
- 执行 `/reload` 时。

每次加载都会重新遍历所有数据包中的 `leveling` 目录，重建属性定义表。

### 3.3 一个文件一条定义

每个 JSON 文件只描述**一个**可加点属性。想加多个属性，就写多个文件。

---

## 4. 完整字段表

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|---|---|---|---|---|
| `attribute` | 字符串 | 是 | 无 | 目标属性 ID，如 `minecraft:max_health` |
| `operation` | 字符串 | 是 | 无 | 修饰符运算方式，见 §5.2 |
| `cost` | 整数 | 否 | `1` | 每次加点消耗的属性点 |
| `value_per_level` | 数值 | 二选一 | 无 | 每级固定提升值 |
| `values` | 数值数组 | 二选一 | 无 | 逐级提升值，见 §5.5 |
| `order` | 整数 | 否 | 无 | 面板排序，可为负，见 §5.6 |
| `display_key` | 字符串 | 否 | 自动推断 | 属性名翻译键，见 §5.7 |
| `description_key` | 字符串 | 否 | 自动推断 | 描述文本翻译键，见 §5.8 |

`value_per_level` 与 `values` 必须**且只能**出现一个，同时出现或同时缺失都会导致该文件被跳过。

---

## 5. 字段详解

### 5.1 `attribute`

目标属性的完整 ID。

```json
"attribute": "minecraft:movement_speed"
```

规则：

- 必须能在当前游戏中解析为已注册属性，否则该文件被跳过；
- 建议使用原版属性，兼容性最好；
- 指向其它模组注册的属性也可以，只要该模组已加载；
- 属性 ID 同时也是玩家数据文件里 `allocations` 的键，一旦改动，旧存档的投入记录将无法对应，等于退款并失效（见 §7.3）。

### 5.2 `operation`

决定这个修饰符如何作用于属性基值。取值：

| 取值 | 效果 | 适用场景 |
|---|---|---|
| `add_value` | 直接加到基值上 | 生命、护甲、氧气、幸运这类「固定值」 |
| `add_multiplied_base` | 基值乘算，在 `add_value` 之前结算 | 少用，做百分比基值加成 |
| `add_multiplied_total` | 在一切结算之后乘算 | 速度、攻击伤害这类「最终百分比」 |

详细计算见 §6。

### 5.3 `cost`

每次加点消耗的属性点数量，必须是正整数。

```json
"cost": 3
```

- 默认 `1`；
- 面板「已投入点数」显示的是**历史总消耗**，不是加点次数；
- 孟婆汤返还的也是历史总消耗；
- 后期改动 `cost` 不会影响已投入的历史记录。

### 5.4 `value_per_level`

每级固定提升值。

```json
"value_per_level": 2.0
```

- 第 n 次加点后的总修饰值 = `value_per_level × n`；
- 对 `add_multiplied_total`，这个值就是乘算系数增量，例如 `0.02` 代表每次 +2%。

### 5.5 `values`

逐级不同提升值。适合做「越加越强」或「越加越贵」的成长曲线。

```json
"values": [2.0, 2.0, 4.0, 4.0, 6.0]
```

规则：

- 第 n 次加点使用 `values[n-1]`；
- **超出数组长度后，使用最后一个值**；
- 总修饰值 = 已使用的各段值之和。

举例：`values` 为 `[2, 2, 4]`，加点 5 次：

```
第1次 +2 → 累计 2
第2次 +2 → 累计 4
第3次 +4 → 累计 8
第4次 +4 → 累计 12（复用最后一个值）
第5次 +4 → 累计 16（复用最后一个值）
```

### 5.6 `order`

面板排序权重，有符号整数。

```json
"order": -10
```

排序规则，按优先级从高到低：

1. `order` 数值小的排前面，大的排后面，允许负数；
2. `order` 相同时，按属性 ID 字母序排列；
3. 没有 `order` 的条目，全部排在所有带 `order` 的条目之后；
4. 同为无 `order` 的条目之间，按属性 ID 字母序排列。

内置默认顺序（`order` 1～6）：

| order | 属性 |
|---|---|
| 1 | `minecraft:max_health` |
| 2 | `minecraft:armor` |
| 3 | `minecraft:attack_damage` |
| 4 | `minecraft:movement_speed` |
| 5 | `minecraft:oxygen_bonus` |
| 6 | `minecraft:luck` |

### 5.7 `display_key`

属性名在面板中使用的翻译键。

```json
"display_key": "attribute.name.max_health"
```

回退链：

1. `display_key` 指定的键；
2. 若未指定或缺失：该属性的默认原版翻译键；
3. 若仍缺失：直接显示属性 ID。

### 5.8 `description_key`

属性描述文本的翻译键，显示在属性名 tooltip 第三行。

```json
"description_key": "attribute.name.max_health.description"
```

回退链：

1. `description_key` 指定的键；
2. 若未指定：自动尝试 `display_key + ".description"`；
3. 若仍不存在：跳过描述行，不显示空行。

---

## 6. 运算规则与数值计算

### 6.1 总修饰值的计算

设某属性已加点 `n` 次。

- 使用 `value_per_level`：

```
总修饰值 = value_per_level × n
```

- 使用 `values`：

```
总修饰值 = sum(values[0 .. min(n, len) - 1]) + max(0, n - len) × values[len - 1]
```

### 6.2 `add_value`

对基值直接相加。

```
最终值 = 基值 + 总修饰值 + 其它模组的 add_value
```

示例：最大生命值基值 20，加点 3 次，每次 +2：

```
20 + 6 = 26
```

### 6.3 `add_multiplied_total`

在一切结算之后乘算。**步步高升采用线性叠加总乘数，不是复利。**

```
最终值 = 之前所有结算结果 × (1 + 总修饰值)
```

示例：速度加点 2 次，每次 +2%：

```
1 + 0.02 × 2 = 1.04   →  ×1.04
```

加点 5 次：

```
1 + 0.02 × 5 = 1.10   →  ×1.10
```

**不是** `1.02^5 = 1.10408`。这是刻意设计，方便玩家心算。

### 6.4 每项属性只维护一个修饰符

无论加了多少次点，步步高升对每个属性只保留**一个**修饰符。加点时：

1. 移除旧修饰符；
2. 按当前次数重新计算总修饰值；
3. 添加新修饰符。

因此外部数据包改动 `value_per_level` 或 `values` 后，`/reload` 即可让**已有加点**按新公式重算，无需玩家重新分配。

> 这一点对整合包作者非常重要：调数值不会毁档。

---

## 7. 重复声明、覆盖与优先级

### 7.1 后加载覆盖先加载

如果多个文件声明了同一个 `attribute`，**后加载的覆盖先加载的**，只保留最后一份定义。

这是正常行为，用于让数据包自定义内置属性。日志里会写一条警告，方便排查「为什么我的数值没生效」。

### 7.2 加载顺序

从低到高：

```
模组内置默认定义
  < 数据包（按数据包加载顺序）
  < /reload 时重新遍历的顺序
```

实践建议：

- 想覆盖内置六项属性，直接在自己的数据包里用同样的 `attribute` 写一份即可；
- 想让覆盖稳定生效，避免同时用多个数据包声明同一个属性；
- `/reload` 后定义表会完全重建，不会残留旧定义。

### 7.3 属性被移除时

如果玩家数据文件里存在某个属性，但当前数据包**没有任何定义**声明它：

1. 返还该属性已投入的全部点数到可用点数；
2. 清空该属性的分配记录；
3. 撤销对应加成。

这是永久性退款，重新加回定义也不会恢复旧的投入记录。

---

## 8. 错误处理

步步高升**不会因为数据包错误而崩溃**。出错时：

- 该文件被跳过，其余文件正常加载；
- 完整错误写入日志；
- 玩家进入游戏后，在聊天栏收到提示，指明哪个路径下的哪个文件被跳过；
- 所有在线玩家都会收到该提示，不做权限区分。

会导致文件被跳过的常见原因：

| 原因 | 说明 |
|---|---|
| JSON 语法错误 | 括号不匹配、多余逗号等 |
| 缺少必填字段 | 缺 `attribute` 或 `operation` |
| `value_per_level` 与 `values` 同时出现 | 二选一 |
| `value_per_level` 与 `values` 同时缺失 | 二选一 |
| `attribute` 无法解析 | 属性不存在，或对应模组未加载 |
| `operation` 取值非法 | 不在 §5.2 列表中 |
| `cost` 不是正整数 | 必须 ≥ 1 |
| `order` 不是整数 | 必须是整数，可为负 |

排查建议：改完数据包先 `/reload`，再看日志，再看进服提示。

---

## 9. 内置默认定义

模组自带以下六项，均可在你的数据包中覆盖。

```json
{
  "attribute": "minecraft:max_health",
  "operation": "add_value",
  "cost": 1,
  "value_per_level": 2.0,
  "order": 1
}
```

```json
{
  "attribute": "minecraft:armor",
  "operation": "add_value",
  "cost": 1,
  "value_per_level": 1.0,
  "order": 2
}
```

```json
{
  "attribute": "minecraft:attack_damage",
  "operation": "add_multiplied_total",
  "cost": 1,
  "value_per_level": 0.05,
  "order": 3
}
```

```json
{
  "attribute": "minecraft:movement_speed",
  "operation": "add_multiplied_total",
  "cost": 1,
  "value_per_level": 0.02,
  "order": 4
}
```

```json
{
  "attribute": "minecraft:oxygen_bonus",
  "operation": "add_value",
  "cost": 1,
  "value_per_level": 0.25,
  "order": 5
}
```

```json
{
  "attribute": "minecraft:luck",
  "operation": "add_value",
  "cost": 1,
  "value_per_level": 0.2,
  "order": 6
}
```

默认不指定 `display_key` 和 `description_key`，走原版属性名键名与自动推断描述键。

---

## 10. 玩家数据文件

面向联动模组创作者与整合包作者。普通玩家了解即可。

### 10.1 路径

```
world/leveling/players/<uuid>.json
```

每个玩家一个文件，按 UUID 映射，**不写入实体 NBT**。

### 10.2 结构

```json
{
  "dataVersion": 1,
  "highestLevel": 0,
  "unspentPoints": 0,
  "allocations": {
    "minecraft:max_health": {
      "levels": 2,
      "spent": 2
    },
    "minecraft:movement_speed": {
      "levels": 5,
      "spent": 5
    }
  }
}
```

| 字段 | 含义 |
|---|---|
| `dataVersion` | 数据格式版本，用于未来迁移 |
| `highestLevel` | 历史最高经验等级 |
| `unspentPoints` | 当前可用属性点 |
| `allocations` | 键为属性 ID |
| `allocations.<id>.levels` | 该属性加点次数 |
| `allocations.<id>.spent` | 该属性历史总消耗点数 |

### 10.3 读写注意事项

- **服务端权威**：所有写入由服务端在主线程完成，外部请勿直接改文件，容易与运行中的服务端冲突；
- 想修改玩家进度，请用 §11 的命令；
- 想读取玩家进度做联动，建议在服务端 tick 之外读取快照，或直接调用模组提供的查询接口（若版本提供）；
- `levels` 与 `spent` 是两份独立记录，`spent` 才是退款依据，不要用 `levels × cost` 反推。

---

## 11. 命令参考

整个 `/leveling` 命令需要 OP 权限。适合整合包作者调试、服务器管理员维护。

### 11.1 属性点

```text
/leveling point <玩家> set <值>
/leveling point <玩家> give <值>
/leveling point <玩家> get
/leveling point <玩家> clear
/leveling point <玩家> clear <值>
/leveling point <玩家> reset
/leveling point <玩家> getspent
/leveling point <玩家> getspent <属性名>
```

| 命令 | 行为 |
|---|---|
| `set` | 设置可用属性点 |
| `give` | 增加可用属性点 |
| `get` | 查询可用属性点 |
| `clear` | 减少到 0 |
| `clear <值>` | 减少指定数量，最低 0 |
| `reset` | 等价于孟婆汤：返还全部已投入点数，清空分配，撤销加成，**不影响历史最高等级** |
| `getspent` | 列出全部属性的分配情况 |
| `getspent <属性名>` | 只列出指定属性 |

### 11.2 历史最高等级

```text
/leveling cap <玩家> set <值>
/leveling cap <玩家> give <值>
/leveling cap <玩家> get
/leveling cap <玩家> clear
/leveling cap <玩家> clear <值>
/leveling cap <玩家> reset
```

| 命令 | 行为 |
|---|---|
| `set` | 设置历史最高等级 |
| `give` | 增加历史最高等级 |
| `get` | 查询历史最高等级 |
| `clear` | 减少到 0 |
| `clear <值>` | 减少指定数量，最低 0 |
| `reset` | 赋值为 0 |

> `clear` 是「减少到 0」，`reset` 是「直接赋值为 0」。结果看似相同，底层语义不同。
>
> 命令修改历史最高等级后**不会立即补发或扣点**，会在下一次登录、重生或升级事件触发检查时按规则处理。

### 11.3 调试流程建议

```
1. 改数据包
2. /reload
3. /leveling point <自己> give 100
4. 打开面板检查显示与排序
5. 加点，检查属性生效
6. /leveling point <自己> getspent 检查记录
7. /leveling point <自己> reset 检查退款
8. /leveling point <自己> get 确认点数回到 100
```

---

## 12. 面向三类读者的实践指南

### 12.1 给想自定义体验的玩家

**只想改数值？** 复制 §9 里对应属性的 JSON，改 `value_per_level` 或 `values`，放进你自己的数据包，`/reload`。

**想改顺序？** 改 `order`，数字越小越靠前。

**想改文案？** 加 `display_key` 和 `description_key`，然后在你数据包的 `lang` 文件里提供对应翻译。

**想加新属性？** 复制一份，把 `attribute` 换成你想加的原版属性 ID 即可，例如 `minecraft:attack_speed`。

**注意**：改 `attribute` 等于换了一个属性，旧存档在该属性上的投入会被退款。

### 12.2 给想联动的模组创作者

当前版本的对外接口是三层：

1. **数据包层**：声明或覆盖可加点属性；
2. **玩家数据文件层**：读取 UUID 文件获取玩家进度；
3. **命令层**：读写玩家进度。

如果你需要 Java 层的事件或 API（例如「玩家加点后触发回调」「查询某玩家某属性总加成」），请与作者联系确认当前版本是否已暴露。在未确认前，推荐用数据包 + 读取玩家文件的方式做松耦合联动。

联动实践建议：

- 你注册的属性可以直接被数据包声明为可加点项；
- 若你的属性有特殊语义（例如非标准消耗逻辑），请在数据包中只声明 `attribute`，语义由你的模组负责；
- 不要直接写玩家数据文件，用命令或等待官方接口。

### 12.3 给想定制的整合包创作者

**推荐做法：把步步高升当机制层，你的整合包当内容层。**

- 在整合包数据包里集中放一份 `data/<pack>/leveling/` 目录，覆盖全部六项默认定义，锁定数值与顺序；
- 用 `order` 把最重要的属性排到最前，用负数把「隐藏/后期」属性压到最前或最后；
- 用 `values` 做成长曲线，例如前期便宜后期昂贵；
- 用 `cost` 做属性之间的性价比差异，例如核心属性 `cost: 1`，强力属性 `cost: 3`；
- 用 `display_key` / `description_key` 统一整合包文案风格；
- 用命令在整合包任务书、教程里给出「如何加点和重置」的说明。

**兼容性提醒：**

- 修改属性定义不会毁档，`/reload` 即重算；
- 删除属性定义会退款并撤销加成；
- 修改 `attribute` 等同于删除旧属性 + 新增新属性；
- 建议在整合包更新日志里说明「哪些属性定义被改动」，避免玩家困惑。

**调试清单：**

```
□ /reload 后无跳过文件提示
□ 面板顺序符合预期
□ 六项属性的 tooltip 三行结构正常
□ 按钮 tooltip 显示消耗与描述
□ /leveling point <玩家> getspent 输出与面板一致
□ 死亡重生后加点仍可点击
□ 孟婆汤退款金额等于 getspent 的 spent 总和
```

---

## 13. 兼容性与注意事项

- **每项属性只维护一个修饰符**，与其它模组的修饰符共存，不冲突；
- `add_multiplied_total` 采用线性叠加，不是复利，与部分模组的复利实现不同，混用时请注意最终数值；
- 模组不做属性上下限限制，也不做反作弊，请自行承担极端数值的后果；
- 玩家数据按 UUID 存储，**跨存档不通用**，换世界等于新玩家；
- 中途安装模组的老玩家，首次登录时按当前等级一次性补发点数；
- 死亡、重生、跨维度、退服、服务器重启都不清除进度；
- 修改数据包后请 `/reload`，不要直接改文件后不重载。

---

## 14. 附录：完整示例集

### 14.1 覆盖内置最大生命值，改成逐级递增

```json
{
  "attribute": "minecraft:max_health",
  "operation": "add_value",
  "cost": 1,
  "values": [2.0, 2.0, 4.0, 4.0, 6.0],
  "order": 1,
  "display_key": "attribute.name.max_health",
  "description_key": "attribute.name.max_health.description"
}
```

### 14.2 新增原版攻击速度为可加点项

```json
{
  "attribute": "minecraft:attack_speed",
  "operation": "add_multiplied_total",
  "cost": 2,
  "value_per_level": 0.03,
  "order": 7
}
```

### 14.3 把幸运值改成昂贵的高阶属性并排到最后

```json
{
  "attribute": "minecraft:luck",
  "operation": "add_value",
  "cost": 5,
  "values": [0.2, 0.4, 0.6, 1.0, 1.0],
  "order": 100
}
```

### 14.4 用负数 order 把某项顶到最前

```json
{
  "attribute": "minecraft:movement_speed",
  "operation": "add_multiplied_total",
  "cost": 1,
  "value_per_level": 0.02,
  "order": -100,
  "display_key": "attribute.name.movement_speed",
  "description_key": "attribute.name.movement_speed.description"
}
```

### 14.5 自定义文案（配合 lang 文件）

数据包 JSON：

```json
{
  "attribute": "minecraft:armor",
  "operation": "add_value",
  "cost": 1,
  "value_per_level": 1.0,
  "order": 2,
  "display_key": "mypack.attribute.armor.name",
  "description_key": "mypack.attribute.armor.desc"
}
```

`assets/mypack/lang/zh_cn.json`：

```json
{
  "mypack.attribute.armor.name": "坚甲",
  "mypack.attribute.armor.desc": "每级增加 1 点护甲值，无需穿戴装备即可生效。"
}
```

---

## 15. 一句话总结

> 你负责写 JSON，步步高升负责把它变成玩家身上真实的成长。

任何数据包层面的问题，先看日志、再看进服提示、再对照本文章节排查。祝你的整合包步步高升。