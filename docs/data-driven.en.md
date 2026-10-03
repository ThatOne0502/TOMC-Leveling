# Data-driven Guide — TOMC-Leveling

TOMC-Leveling defines its attributes entirely through JSON files, so you can add new attributes, remove existing ones, or rebalance their scaling without writing any code. This guide documents the format.

## Contents

- [1. Attribute Definitions (`attribute_definition`)](#1-attribute-definitions-attribute_definition)
- [Built-in attributes](#built-in-attributes)

---

## 1. Attribute Definitions (`attribute_definition`)

An attribute definition tells TOMC-Leveling which attribute players can invest in, how many points each level costs, and how the attribute scales per level.

**Location** — `data/leveling/**/*.json` (any subdirectory under the `leveling` namespace, ending in `.json`). Definitions are reloaded on `/reload`.

### Fields

| Field | Type | Default | Description |
| --- | --- | --- | --- |
| `attribute` | string | *required* | The attribute ID, e.g. `minecraft:max_health`. Must be a valid resource location. |
| `operation` | string | *required* | `add_value` (flat addition) or `add_multiplied_total` (percentage). |
| `value_per_level` | number | *one of* | Fixed value added per level. |
| `values` | number[] | *one of* | Per-level values; levels beyond the last reuse the final entry. |
| `cost` | int | `1` | Points spent per level. Must be ≥ 0. |
| `order` | int | *none* | Sort position in the panel (ascending; missing sorts last). |
| `display_key` | string | *none* | Translation key for the attribute's display name. |
| `description_key` | string | *none* | Translation key for the attribute's description. |
| `translation_key` | string | *none* | Legacy fallback translation key. |

### Semantics

- `value_per_level` and `values` are mutually exclusive: provide **exactly one** of them. Providing both, or neither, fails the file.
- `values` must be a **non-empty array of numbers**.
- `operation` must be `add_value` or `add_multiplied_total`. Any other value fails the file.
  - `add_value` adds the level value directly to the attribute.
  - `add_multiplied_total` multiplies the attribute by `1 + value` — so `0.05` means `+5%`.
- `attribute` must parse as a valid resource location (e.g. `minecraft:max_health`).
- Display-name resolution order: `display_key` → `translation_key` → `attribute.name.<path>` (the first present key wins).
- Description resolution order: `description_key` → `<display_key>.description`.
- A file that fails validation is skipped, a warning is logged, and the error is reported to operators in-game.

### Examples

A flat additive attribute — `minecraft:max_health` gains `2.0` per level:

```json
{
  "attribute": "minecraft:max_health",
  "operation": "add_value",
  "value_per_level": 2.0,
  "cost": 1,
  "order": 1
}
```

A percentage-based attribute — `minecraft:attack_damage` gains `5%` per level:

```json
{
  "attribute": "minecraft:attack_damage",
  "operation": "add_multiplied_total",
  "value_per_level": 0.05,
  "cost": 1,
  "order": 3
}
```

Per-level scaling with a `values` array — for example, `minecraft:luck` gains `0.2`, `0.4`, `0.6` on levels 1–3, then stays at `0.6` for every later level:

```json
{
  "attribute": "minecraft:luck",
  "operation": "add_value",
  "values": [0.2, 0.4, 0.6],
  "cost": 1,
  "order": 6
}
```

A fully customized definition with an explicit display name and description:

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

## Built-in attributes

TOMC-Leveling ships with six attribute definitions, all costing `1` point per level:

| Attribute | Operation | Scaling |
| --- | --- | --- |
| `minecraft:max_health` | `add_value` | `+2.0` per level |
| `minecraft:armor` | `add_value` | `+1.0` per level |
| `minecraft:attack_damage` | `add_multiplied_total` | `+5%` per level |
| `minecraft:movement_speed` | `add_multiplied_total` | `+2%` per level |
| `minecraft:oxygen_bonus` | `add_value` | `+0.25` per level |
| `minecraft:luck` | `add_value` | `+0.2` per level |
