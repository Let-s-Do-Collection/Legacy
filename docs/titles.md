# Titles

A title is a cosmetic honorific. A player unlocks it through a trigger and wears one at a time above the
name. Titles are localised and support gendered forms.

Every title is one JSON file:

```
data/<namespace>/legacy/titles/<id>.json
```

Any data pack or mod can add or override titles by dropping a file here. `/legacy reload` re-reads them
live.

## Example

`data/legacy/titles/dragon_slayer.json`

```json
{
  "id": "dragon_slayer",
  "translation_key": "title.legacy.dragon_slayer",
  "category": "bosses",
  "icon": "minecraft:dragon_head",
  "rarity": "legendary",
  "placement": "suffix",
  "trigger": { "type": "advancement", "id": "minecraft:end/kill_dragon" }
}
```

Plus a language entry in `assets/<namespace>/lang/en_us.json`:

```json
"title.legacy.dragon_slayer": "Dragonslayer"
```

Now `Marco, Dragonslayer` unlocks when the player kills the dragon.

## Fields

| Field | Required | Default | Description |
|---|---|---|---|
| `trigger` | yes | `{ "type": "custom" }` | The unlock condition. See [triggers.md](triggers.md). |
| `translation_key` | yes¹ | `title.legacy.<id>` | Language key for the display name. Enables translation and gendered forms. |
| `title` | no¹ | – | Literal name, instead of `translation_key` (no translation or gender). |
| `id` | no | *(filename)* | Unique id. Defaults to the file name. |
| `category` | no | `misc` | Grouping key. |
| `icon` | no | `minecraft:paper` | Item shown in the UI and unlock toast. |
| `rarity` | no | `common` | Rarity, sets the colour. |
| `placement` | no | `suffix` | `prefix` gives `Farmer Marco`, `suffix` gives `Marco, Terror of the Seas`. |
| `series` | no | – | Series key. The journal only reveals the next open stage of a series. |
| `stage` | no | `0` | Position in a series (1, 2, 3, ...). |
| `display_priority` | no | `100` | Sort order. Lower is higher up. |
| `hidden` | no | `false` | Secret. Only appears once unlocked. |

¹ Provide either `translation_key` (recommended) or a literal `title`.

## Text: translation_key or title

Legacy's own titles use `translation_key` because titles are localised and gendered. The display name
resolves `<key>.male` or `<key>.female`, and falls back to `<key>` when a gendered form is missing. This
works for every installed language.

```json
"title.legacy.dragon_slayer": "Dragonslayer",
"title.legacy.dragon_slayer.female": "Dragonslayer"
```

If you do not need translation or gendered forms, skip the language file and use a literal `title`:

```json
{ "id": "trailblazer", "title": "Trailblazer", "trigger": { "type": "counter", "counter": "biomes_distinct", "value": 25 } }
```

## Rarity and colour

The rarity colour is used for both the title text and the wearer's name.

| Rarity | Colour |
|---|---|
| `common` | White |
| `uncommon` | Green |
| `rare` | Aqua |
| `epic` | Light Purple |
| `legendary` | Gold |
| `mythic` | Red |

## Series

Give related titles the same `series` and rising `stage` values. The journal reveals one step at a time.

```json
{ "id": "farmer",        "series": "farming/harvest", "stage": 1, "trigger": { "type": "counter", "counter": "harvest:crops", "value": 64  } }
{ "id": "master_farmer", "series": "farming/harvest", "stage": 2, "trigger": { "type": "counter", "counter": "harvest:crops", "value": 512 } }
```

## Validation

On load and on `/legacy reload`, every file is validated and logs a warning for a bad or duplicate `id`,
an invalid `rarity`, `placement` or `trigger.type`, an unknown `icon`, or a trigger that is missing the
field it needs. A single bad file does not break the rest.

## Commands

| Command | Permission | Purpose |
|---|---|---|
| `/legacy titles` | all | List all titles. |
| `/legacy info <id>` | all | Show a title's trigger and requirement. |
| `/legacy eval` | all | Force an evaluation for yourself. |
| `/legacy grant\|revoke <id>` | ops | Grant or revoke a title. |
| `/legacy reload` | ops | Reload all titles live. |

## Custom triggers (API)

```java
LegacyAPI.registerCustomTrigger("legacy:fate",
        (serverPlayer, title) -> serverPlayer.getInventory().contains(new ItemStack(Items.NETHER_STAR)));
```

`LegacyAPI` also has `grant(player, id)` and `revoke(player, id)`.
