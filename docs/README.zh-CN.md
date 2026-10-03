<p align="center"><img src="../src/main/resources/assets/leveling/leveling_logo.png" width="800"></p>

<p align="center"><a href="../README.md">根目录 / Root</a> &nbsp;·&nbsp; <a href="README.en.md">English</a></p>

# TOMC-Leveling

> 📘 **数据驱动指南** — [data-driven.zh-CN.md](data-driven.zh-CN.md)

**TOMC-Leveling** 是一款面向 **Fabric**（**Minecraft 1.21.11**）的成长类模组：每当你突破自己的历史最高经验等级，就会获得**属性点**。把这些点数投入到六种属性上，或者喝一碗孟婆汤来重置你的加点、从头再来。

## 特性

### 🎯 随成长获得的属性点

每当你的经验等级超过自己曾达到过的最高等级，你都会获得**一点属性点**。点数按玩家独立记录，死亡、切换维度、登出乃至服务器重启都不会丢失——它们存放在服务端权威的存储中，从不写入实体。

### 📊 属性面板（I 键）

按 **I**（可在 `选项 → 控制 → 按键绑定` 中改键）打开加点面板。面板采用原版物品栏风格，为每个属性显示：

- **序号** —— 配置的排序位置。
- **名称** —— 本地化的属性名。
- **当前值** —— 实时的、被修改后的数值。
- **已投入** —— 已投入的点数。
- **提升** —— 用于消耗点数的 `[+]` 按钮。

面板支持搜索框（匹配序号、译名或属性 ID）、表格区域内的鼠标滚轮滚动，再次按 **I** 即可关闭。点数即时消耗并作为属性修饰符生效。

### 🍲 孟婆汤（重置物品）

**孟婆汤**会清空你的整套加点：返还所有已投入点数、清除全部属性分配，并——视服务端配置而定——可选地清除你的配方与进度。它总是可食用，饮用需 96 tick（普通食物的三倍），堆叠上限为 1，饮毕退还一只空碗。

### 🛠️ 命令

所有命令均需操作员等级 2（gamemaster）权限。用 `point` 管理玩家的可用点数，用 `cap` 管理其历史最高等级记录：

- `/leveling point <目标> set|give|get|clear|reset [数值]`
- `/leveling point <目标> getspend [属性]`
- `/leveling cap <目标> set|give|get|clear|reset [数值]`

### 🎁 战利品注入

孟婆汤会被注入**废弃传送门、远古城市、下界要塞、末地城宝藏**与**试炼密室走廊陶罐**的战利品表，概率约为 **30%**——大约每三四个容器就能找到一个。

### 🔔 提醒与音效

获得属性点时你会听到一声提示音并看到提醒。可用点数还可按可配置的间隔进行轮询并显示在快捷栏上方。音效与提醒行为均可在客户端配置。

## 设计理念

TOMC-Leveling 将模组的规则做成**数据驱动**、将权威性放在**服务端**。属性定义——存在哪些属性、它们消耗多少点数、每级如何成长——都来自 `data/leveling/` 下的 JSON 文件，因此服务器与整合包无需改动代码即可增删或重新平衡属性。服务端掌管一切数值：客户端只渲染服务端发送的内容。完整格式见[数据驱动指南](data-driven.zh-CN.md)。

## 环境要求

- Minecraft **1.21.11**
- **Fabric Loader** `0.19.3` 或更高
- **Fabric API**
- *（可选）* **Mod Menu** —— 用于游戏内配置

## 配置

配置分为客户端与服务端两部分，均可通过 **Mod Menu → TOMC-Leveling** 编辑（客户端设置，或在拥有操作员权限或单人模式下编辑服务端设置）。

- **客户端**（`config/leveling-client.json`）
  - `reminderEnabled` —— 是否显示可用点数提醒（默认 `true`）。
  - `reminderIntervalSeconds` —— 轮询可用点数的间隔秒数（默认 `10`，`0` 表示不轮询）。
  - `levelingSoundEnabled` —— 是否播放获得属性点音效（默认 `true`）。
- **服务端**（`config/leveling-server.json`）
  - `clearRecipesOnUse` —— 孟婆汤是否同时清除配方（默认 `false`）。
  - `clearAdvancementsOnUse` —— 孟婆汤是否同时清除进度（默认 `false`）。

## 许可证

[GPL-3.0](../LICENSE)

---

<div align="center">

📘 **[数据驱动指南](data-driven.zh-CN.md)** &nbsp;·&nbsp; **[根目录 / Root](../README.md)** &nbsp;·&nbsp; **[English](README.en.md)**

</div>
