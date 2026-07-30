# Trigger types

Titles, journeys and milestones all use the same `trigger` block:

```json
"trigger": { "type": "<type>", "...": "..." }
```

The accepted `type` values differ per system. Titles and journeys are polled (checked periodically per
player). Milestones fire on the event itself, to record a world-first.

| `type` | Titles | Journeys | Milestones | Meaning |
|---|:---:|:---:|:---:|---|
| `advancement` | yes | yes | yes | Player has a vanilla or data-pack advancement. |
| `statistic` | yes | yes | no | A vanilla statistic reaches a value. |
| `counter` | yes | yes | no | A Legacy counter reaches a value. |
| `item` | yes | yes | no | Player holds an item in the inventory. |
| `custom` | yes | yes | no | Mod-registered predicate (Java escape hatch). |
| `dimension` | no | no | yes | First player to enter a dimension. |
| `kill` | no | no | yes | First player to kill an entity type. |

Note: dimension and biome presence is also available to titles and journeys as a `counter` (see below).
`dimension` is a separate milestone type because a milestone needs the entry event to name the
world-first.

---

## `advancement`

Fires when the player has the advancement.

```json
"trigger": { "type": "advancement", "id": "minecraft:end/kill_dragon" }
```

| Field | Required | Description |
|---|---|---|
| `id` | yes | Advancement id. |

Retroactive: if the player already has it, the trigger is satisfied on the next check.

---

## `statistic`

Fires when a vanilla statistic reaches `value`.

```json
"trigger": { "type": "statistic", "stat_type": "minecraft:killed", "stat": "minecraft:blaze", "value": 20 }
```

| Field | Required | Default | Description |
|---|---|---|---|
| `stat_type` | no | `minecraft:custom` | Stat-type registry id. |
| `stat` | yes | – | Key inside that stat type's registry. |
| `value` | no | `1` | Threshold. |

| `stat_type` | `stat` refers to | Examples |
|---|---|---|
| `minecraft:custom` | a custom stat | `minecraft:play_time`, `minecraft:mob_kills`, `minecraft:walk_one_cm`, `minecraft:traded_with_villager`, `minecraft:enchant_item` |
| `minecraft:mined` | a block | `minecraft:deepslate` |
| `minecraft:used` | an item | `minecraft:bone_meal` |
| `minecraft:killed` | an entity type | `minecraft:wither` |
| `minecraft:crafted` / `minecraft:broken` / `minecraft:picked_up` / `minecraft:dropped` | an item | `minecraft:diamond` |

Statistics are **retroactive** and reliable. Distances are centimetres, time is ticks (20/second).

Note: `minecraft:mined` counts every block break, also immature crops. For a "harvested a crop"
meaning, use the `counter` type with `harvest:crops` instead.

---

## `counter`

Fires when a **Legacy counter** reaches `value`. Counters are fed by Legacy's own event collectors and
count things vanilla statistics can't (mature-only harvests, bonemeal use, discovery of biomes/dimensions/
structures).

```json
"trigger": { "type": "counter", "counter": "harvest:crops", "value": 512 }
```

| Field | Required | Default | Description |
|---|---|---|---|
| `counter` | yes | – | Counter key (see table). |
| `value` | no | `1` | Threshold. |

### Available counter keys

| Key | Increments when |
|---|---|
| `harvest:crops` | any fully-grown crop is harvested |
| `harvest:<block>` | a specific mature crop is harvested, e.g. `harvest:minecraft:wheat` |
| `place:<block>` | a specific block is placed, e.g. `place:minecraft:oak_sapling` |
| `place:saplings` | any sapling is placed |
| `place:crops` | any crop is planted |
| `bonemeal:used` | bone meal is applied to a plant |
| `dimension:<id>` | the player enters a dimension, e.g. `dimension:minecraft:the_nether` (0/1) |
| `dimensions_distinct` | a *new* dimension is entered |
| `biome:<id>` | the player stands in a biome, e.g. `biome:minecraft:lush_caves` (0/1) |
| `biomes_distinct` | a *new* biome is discovered |
| `structure:<id>` | the player stands in a structure (0/1) |
| `structures_distinct` | a *new* structure is discovered |
| `mine:#<tag>` | a block in a mined-tag is broken: `mine:#minecraft:logs`, `mine:#minecraft:small_flowers`, `mine:#legacy:stones`, `mine:#legacy:ores` |

> Counters are **forward-only**: they start at 0 when Legacy is installed (except dimension/biome, which
> are seeded on join so "you are already in the Nether" is recognised).

---

## `item`

Fires while the player holds the item **anywhere** in their inventory (main, armor or off-hand).
Unlike the `crafted`/`picked_up` statistics, this also fires for items obtained via `/give`, creative,
trading or any other means — ideal for "First X" journeys.

```json
"trigger": { "type": "item", "id": "minecraft:elytra" }
```

| Field | Required | Default | Description |
|---|---|---|---|
| `id` | yes | – | Item id. |
| `value` | no | `1` | How many the player must hold (summed across stacks). |

## `custom`

Fires via a mod-registered Java predicate keyed by `id`.

```json
"trigger": { "type": "custom", "id": "legacy:fate" }
```

```java
LegacyAPI.registerCustomTrigger("legacy:fate",
        (serverPlayer, title) -> serverPlayer.getInventory().contains(new ItemStack(Items.NETHER_STAR)));
```

The `title` argument is `null` when the predicate is evaluated for a journey.

---

## `dimension` (milestones only)

Fires for the first player to enter the dimension.

```json
"trigger": { "type": "dimension", "id": "minecraft:the_nether" }
```

## `kill` (milestones only)

Fires for the first player to kill the entity type.

```json
"trigger": { "type": "kill", "id": "minecraft:ender_dragon" }
```
