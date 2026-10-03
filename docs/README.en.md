<p align="center"><img src="../src/main/resources/assets/leveling/leveling_logo.png" width="800"></p>

<p align="center"><a href="../README.md">根目录 / Root</a> &nbsp;·&nbsp; <a href="README.zh-CN.md">简体中文</a></p>

# TOMC-Leveling

> 📘 **Data-driven guide** — [data-driven.en.md](data-driven.en.md)

**TOMC-Leveling** is a progression mod for **Fabric** on **Minecraft 1.21.11** that rewards you with attribute points whenever you surpass your historical highest experience level. Spend those points on six attributes, or drink a Mengpo Soup to reset your build and start over.

## Features

### 🎯 Attribute Points by Progression

Every time your experience level rises above the highest level you have ever reached, you earn **one attribute point**. Points are tracked per player and persist across death, dimension changes, logout, and server restarts — they live in a server-authoritative store, never on the entity.

### 📊 Attribute Panel (I key)

Press **I** (rebindable in `Options → Controls → Key Binds`) to open the upgrade panel. It renders in the inventory style and shows, for each attribute:

- **Order** — the configured sort position.
- **Name** — the localized attribute name.
- **Current value** — the live, modded value.
- **Spent** — how many points are already invested.
- **Upgrade** — the `[+]` button to spend points.

The panel supports a search box (matches order, translated name, or attribute ID), mouse-wheel scrolling inside the table, and closes when you press **I** again. Points are spent instantly and applied as attribute modifiers.

### 🍲 Mengpo Soup (Reset Item)

**Mengpo Soup** wipes your entire build: it refunds every spent point, clears all attribute allocations, and — depending on server config — optionally clears your recipes and advancements. It is always edible, takes 96 ticks to drink (three times a normal food), stacks to one, and returns an empty bowl.

### 🛠️ Commands

All commands are gated behind operator level 2 (gamemaster). Manage a player's unspent points with `point`, or their highest-level record with `cap`:

- `/leveling point <target> set|give|get|clear|reset [value]`
- `/leveling point <target> getspend [attribute]`
- `/leveling cap <target> set|give|get|clear|reset [value]`

### 🎁 Loot Injection

Mengpo Soup is injected into the loot tables of **ruined portals, ancient cities, nether bridges, end city treasure**, and **trial chamber corridor pots** at roughly **30%** chance — you can expect to find one in every three or four containers.

### 🔔 Reminders & Sound

When you gain a point you hear a chime and see a reminder. Unspent points can also be polled on a configurable interval and shown on the action bar. Sound and reminder behavior are both client-configurable.

## Design philosophy

TOMC-Leveling keeps the mod's rules **data-driven** and its authority **server-side**. Attribute definitions — which attributes exist, what they cost, and how each level scales — come from JSON files under `data/leveling/`, so servers and packs can add, remove, or rebalance attributes without touching code. The server owns every number: the client only renders what the server sends. See the [data-driven guide](data-driven.en.md) for the full format.

## Requirements

- Minecraft **1.21.11**
- **Fabric Loader** `0.19.3` or newer
- **Fabric API**
- *(optional)* **Mod Menu** — for in-game configuration

## Configuration

Configuration is split between client and server, both editable through **Mod Menu → TOMC-Leveling** (client settings, or server settings when you have operator permissions or are in singleplayer).

- **Client** (`config/leveling-client.json`)
  - `reminderEnabled` — show unspent-point reminders (default `true`).
  - `reminderIntervalSeconds` — how often to poll for unspent points (default `10`, `0` disables polling).
  - `levelingSoundEnabled` — play the point-gained sound (default `true`).
- **Server** (`config/leveling-server.json`)
  - `clearRecipesOnUse` — Mengpo Soup also clears recipes (default `false`).
  - `clearAdvancementsOnUse` — Mengpo Soup also clears advancements (default `false`).

## License

[GPL-3.0](../LICENSE)

---

<div align="center">

📘 **[Data-driven guide](data-driven.en.md)** &nbsp;·&nbsp; **[根目录 / Root](../README.md)** &nbsp;·&nbsp; **[简体中文](README.zh-CN.md)**

</div>
