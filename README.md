> [!WARNING]
> **早期开发阶段 / Early Development**
>
> 本项目仍处于非常早期的开发阶段。功能、数据格式、命令和 API 都可能发生重大变更，且不保证向后兼容。请勿用于生产环境或长期存档，升级前务必备份。
>
> This project is in a very early stage of development. Features, data formats, commands, and APIs may change significantly without backward compatibility. Not recommended for production or long-term worlds. Always back up before updating.
>
> **AI 辅助创作 / AI-Assisted**
>
> 本项目在开发过程中使用了 AI 辅助（DeepSeek Harness）。且并不是所有代码、文档与设计均经过人工审查与测试。
>
> This project was developed with assistance from AI (DeepSeek Harness). NOT all code, documentation, and design have been reviewed and tested by humans.

# TOMC-Leveling / 步步高升

> 让你的每一级，都不白练。

**TOMC-Leveling** 是一款为 Fabric 1.21.11 设计的轻量级属性成长模组。它把经验等级从一次性的附魔消耗品，变成伴随你整个存档的永久成长线——每突破一次自己的历史最高等级，就获得一点属性点，由你亲手分配到六项基础属性上。

死亡不掉、重生不丢、退服不重置。你练过的每一级，都算数。

---

## 功能特性

- **历史最高等级制**：只有超越曾经抵达过的最高等级，才会获得属性点。死亡掉级不会倒扣，已获得的加成也不会消失。
- **六项属性自由加点**：最大生命值、护甲值、攻击伤害、速度、额外氧气、幸运值。
- **永久成长**：死亡、重生、跨维度、退服、服务器重启均不清除进度。
- **孟婆汤重置**：特殊食物，返还所有已投入点数，撤销全部加成，但不影响历史最高等级。
- **数据驱动**：所有属性定义均可通过数据包配置，无需编写 Java。
- **UUID 独立存储**：每个玩家一个文件，不写入实体 NBT，避免存档膨胀。
- **服务端权威**：所有加点、扣点、补发均由服务端验证，防刷点。
- **可滚动面板**：按 `I` 键打开，表格化显示属性名、当前值、已投入点数、加点按钮。
- **中英双语**：内置 `en_us` 与 `zh_cn` 翻译。

---

## 环境要求

| 项目 | 版本 |
|---|---|
| Minecraft | 1.21.11 |
| Fabric Loader | 0.19.3 或更高 |
| Fabric API | 对应 1.21.11 的版本 |
| Java | 21 |

---

## 安装

1. 安装 **Fabric Loader**（版本 0.19.3+）。
2. 下载 **Fabric API**，放入 `mods/` 目录。
3. 下载 **TOMC-Leveling** 的 jar，放入 `mods/` 目录。
4. 重启客户端或服务器。

---

## 构建

```sh
# Windows
gradlew.bat build

# Linux / macOS
./gradlew build
```

首次构建会下载 Gradle、JDK 21 工具链和依赖，可能需要 5–15 分钟。

构建产物位于：

```
build/libs/leveling-0.1.0.jar
```

> 若修改了 `gradle.properties` 中的 `archives_base_name`，产物文件名会相应变化。

---

## 使用

### 属性面板

- 按 `I` 键（可在客户端配置中修改）打开属性升级面板。
- 面板以可滚动表格呈现，每行显示：属性名、当前值、已投入点数、加点按钮。
- 表头显示当前可用属性点数量，以及下一次获得属性点所需等级（`highestLevel + 1`）。
- 字符串过长时自动截断，鼠标悬停显示完整内容与属性描述。

### 孟婆汤

- 特殊食物，满饥饿也可食用。
- 食用时间为一般食物的三倍。
- 不可堆叠，使用后消耗自身并返回碗。
- 返还所有已投入属性点，撤销全部加成，不影响历史最高等级。

---

## 命令

整个 `/leveling` 命令需要 OP 权限。

### 属性点

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
| `reset` | 等价于孟婆汤：返还全部已投入点数，清空分配，撤销加成，不影响历史最高等级 |
| `getspent` | 列出全部属性的分配情况 |
| `getspent <属性名>` | 只列出指定属性 |

### 历史最高等级

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

> 命令修改历史最高等级后不会立即补发或扣点，会在下一次登录、重生或升级事件触发检查时按规则处理。

---

## 数据驱动

所有属性定义均可通过数据包配置，路径为：

```
data/<namespace>/leveling/<file>.json
```

最小示例：

```json
{
  "attribute": "minecraft:max_health",
  "operation": "add_value",
  "cost": 1,
  "value_per_level": 2.0,
  "order": 1
}
```

支持字段：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `attribute` | 字符串 | 是 | 目标属性 ID |
| `operation` | 字符串 | 是 | `add_value` / `add_multiplied_base` / `add_multiplied_total` |
| `cost` | 整数 | 否 | 每次加点消耗，默认 1 |
| `value_per_level` | 数值 | 二选一 | 每级固定提升值 |
| `values` | 数值数组 | 二选一 | 逐级不同提升值 |
| `order` | 整数 | 否 | 面板排序，可为负 |
| `display_key` | 字符串 | 否 | 属性名翻译键 |
| `description_key` | 字符串 | 否 | 描述翻译键 |

详细说明请见 [数据驱动文档](docs/data-driven.md)。

---

## 项目结构

```
src/main/java/com/tomc/leveling/Leveling.java   — 模组入口
src/main/resources/fabric.mod.json              — 模组元数据
build.gradle                                    — fabric-loom + fabric-loader + Fabric API
```

---

## 协议

本项目采用 **GNU General Public License v3.0** 协议。

详见 [LICENSE](LICENSE) 文件。

---

## 作者

- **ThatOne** — 主要作者
- **DeepSeek Harness** — 开发协助

---

## 链接

- 源码仓库：https://github.com/ThatOne0502/TOMC-Leveling
- 问题反馈：https://github.com/ThatOne0502/TOMC-Leveling/issues

---

> **步步高升**——你练过的每一级，都留在身上。