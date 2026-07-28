# `titles.json` – Reference

All titles are defined in a single data-pack file:

```
data/legacy/titles.json
```

Because it is a data pack file, other data packs (and mods) can add or override titles, and
`/legacy reload` re-reads it live without restarting the server.

## Top-level structure

```json
{
  "titles": [
    { "id": "...", "...": "..." },
    { "id": "...", "...": "..." }
  ]
}
```

A bare array (without the `"titles"` wrapper) is also accepted.

## Fields

| Field | Required | Type | Default | Description |
|---|---|---|---|---|
| `id` | ✅ | string | – | Unique identifier, e.g. `"dragon_slayer"`. Must be unique; duplicates are ignored with a warning. |
| `translation_key` | ✅¹ | string | `title.legacy.<id>` | Language-file key for the display name. |
| `placement` | ✅ | string | `suffix` | `prefix` → `Farmer Marco`, `suffix` → `Marco, Terror of the Seas`. |
| `trigger` | ✅ | string | `custom` | One of `minecraft_stat`, `advancement`, `dimension`, `biome`, `custom`. |
| `requirement` | ✅ | object | `{}` | Trigger parameters (see below). |
| `category` | ➖ | string | `""` | Grouping key, used for sorting / future filtering. |
| `display_priority` | ➖ | int | `100` | Sort order in the selection screen (lower = higher up). |
| `hidden` | ➖ | bool | `false` | Secret title – only appears in the list once unlocked. |
| `icon` | ➖ | string | `minecraft:paper` | Item id shown in the selection screen and unlock toast. |
| `rarity` | ➖ | string | `common` | Rarity → colour (see below). |

¹ Technically optional (falls back to `title.legacy.<id>`), but you should always provide it and add the
key to your language files.

## Rarity → colour

The rarity colour is applied to **both** the title text and the wearer's name.

| `rarity` | Colour |
|---|---|
| `common` | White |
| `uncommon` | Green |
| `rare` | Aqua |
| `epic` | Light Purple |
| `legendary` | Gold |
| `mythic` | Red |

## Triggers

### `minecraft_stat`

Unlocks when a vanilla statistic reaches `amount`.

```json
"trigger": "minecraft_stat",
"requirement": {
  "stat_type": "minecraft:custom",
  "stat": "minecraft:fish_caught",
  "amount": 100
}
```

`stat_type` is a stat-type registry id; `stat` is the key inside that type's registry:

| `stat_type` | `stat` refers to | Example |
|---|---|---|
| `minecraft:custom` | a custom stat | `minecraft:play_time`, `minecraft:mob_kills`, `minecraft:walk_one_cm` |
| `minecraft:mined` | a block | `minecraft:deepslate` |
| `minecraft:used` | an item | `minecraft:bone_meal` |
| `minecraft:killed` | an entity type | `minecraft:blaze` |
| `minecraft:crafted` / `minecraft:broken` / `minecraft:picked_up` / `minecraft:dropped` | item | `minecraft:diamond` |

Distances are in centimetres (`minecraft:walk_one_cm`), time is in ticks (`minecraft:play_time`, 20 ticks/second).

### `advancement`

Unlocks when the player has the advancement.

```json
"trigger": "advancement",
"requirement": { "advancement": "minecraft:end/kill_dragon" }
```

### `dimension`

Unlocks while the player is in the dimension.

```json
"trigger": "dimension",
"requirement": { "dimension": "minecraft:the_nether" }
```

### `biome`

Unlocks while the player stands in the biome.

```json
"trigger": "biome",
"requirement": { "biome": "minecraft:lush_caves" }
```

### `custom`

Unlocks via mod-registered logic keyed by `id`. See the API below.

```json
"trigger": "custom",
"requirement": { "id": "legacy:fate" }
```

## Full example

```json
{
  "id": "dragon_slayer",
  "translation_key": "title.legacy.dragon_slayer",
  "placement": "suffix",
  "trigger": "advancement",
  "requirement": { "advancement": "minecraft:end/kill_dragon" },
  "category": "bosses",
  "display_priority": 250,
  "hidden": false,
  "icon": "minecraft:dragon_head",
  "rarity": "legendary"
}
```

Plus a language entry (`assets/legacy/lang/en_us.json`):

```json
"title.legacy.dragon_slayer": "Dragonslayer"
```

## Custom triggers (API)

```java
LegacyAPI.registerCustomTrigger("legacy:fate",
        (serverPlayer, title) -> serverPlayer.getInventory().contains(new ItemStack(Items.NETHER_STAR)));
```

`LegacyAPI` also offers `grant(player, id)` and `revoke(player, id)`.

## Validation

On load (and on `/legacy reload`), Legacy validates every entry and logs clear warnings for:

- missing/blank `id` (entry skipped) and duplicate `id` (later ones skipped);
- invalid `placement`, `trigger` or `rarity` values (default used);
- missing `translation_key`;
- malformed or unknown `icon` item;
- a `requirement` that is missing the field its `trigger` needs (the title could never unlock).

A single bad entry never breaks the rest of the file.

## Commands

| Command | Permission | Purpose |
|---|---|---|
| `/legacy status` | all | Your server-side title state. |
| `/legacy titles` | all | List all titles with rarity/trigger. |
| `/legacy list` | all | List all title ids. |
| `/legacy info <id>` | all | Show a title's trigger, requirement, etc. |
| `/legacy eval` | all | Force a trigger evaluation for yourself. |
| `/legacy clear` | all | Clear your active title. |
| `/legacy reload` | ops (2) | Reload `titles.json` live. |
| `/legacy grant <id>` | ops (2) | Grant a title. |
| `/legacy revoke <id>` | ops (2) | Revoke a title. |
