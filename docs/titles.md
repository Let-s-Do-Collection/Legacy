# Creating your first Title

A **title** is a cosmetic honorific players unlock and wear above their name (exactly one at a time).
Titles are localised and support gendered forms.

Every title is **one JSON file**:

```
data/<namespace>/legacy/titles/<id>.json
```

Any datapack — or mod — can add or override titles just by dropping a file here. `/legacy reload`
re-reads them live.

## Minimal example

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

That's it — `Marco, Dragonslayer` now unlocks when the player kills the dragon.

## Fields

| Field | Required | Default | Description |
|---|---|---|---|
| `id` | ✅ | *(filename)* | Unique id. Defaults to the file name if omitted. |
| `trigger` | ✅ | `{ "type": "custom" }` | The unlock condition. See **[triggers.md](triggers.md)**. |
| `translation_key` | ✅¹ | `title.legacy.<id>` | Language-file key for the display name (enables i18n + gendered forms). |
| `title` | ➖ | – | Literal name, as an alternative to `translation_key` (no localisation/gender). |
| `category` | ➖ | `misc` | Grouping key. |
| `icon` | ➖ | `minecraft:paper` | Item shown in the UI and unlock toast. |
| `rarity` | ➖ | `common` | Rarity → colour. |
| `placement` | ➖ | `suffix` | `prefix` → `Farmer Marco`; `suffix` → `Marco, Terror of the Seas`. |
| `series` | ➖ | – | Series key; the journal reveals only the next open stage in a series. |
| `stage` | ➖ | `0` | Position within a series (1, 2, 3…). |
| `display_priority` | ➖ | `100` | Sort order (lower = higher up). |
| `hidden` | ➖ | `false` | Secret — only appears once unlocked. |

¹ Provide either `translation_key` (recommended) **or** a literal `title`.

### Text: `translation_key` vs `title`

Legacy's own titles use `translation_key` because titles are **localised and gendered**: the display name
resolves `<key>.male` / `<key>.female` with a fallback to `<key>`, across every installed language.

```json
"title.legacy.dragon_slayer": "Dragonslayer",
"title.legacy.dragon_slayer.female": "Dragonslayer"
```

If you don't need localisation or gendered forms, skip the lang file and use a literal `title`:

```json
{ "id": "trailblazer", "title": "Trailblazer", "trigger": { "type": "counter", "counter": "biomes_distinct", "value": 25 } }
```

## Rarity → colour

Applied to **both** the title text and the wearer's name.

| `rarity` | Colour |
|---|---|
| `common` | White |
| `uncommon` | Green |
| `rare` | Aqua |
| `epic` | Light Purple |
| `legendary` | Gold |
| `mythic` | Red |

## Series

Give related titles the same `series` and ascending `stage` values to build a progression the journal
reveals one step at a time:

```json
{ "id": "farmer",        "series": "farming/harvest", "stage": 1, "trigger": { "type": "counter", "counter": "harvest:crops", "value": 64  } }
{ "id": "master_farmer", "series": "farming/harvest", "stage": 2, "trigger": { "type": "counter", "counter": "harvest:crops", "value": 512 } }
```

## Validation & commands

On load and on `/legacy reload`, every file is validated with clear warnings (bad/duplicate `id`, invalid
`rarity`/`placement`/`trigger.type`, unknown `icon`, a trigger missing the field it needs). A single bad
file never breaks the rest.

| Command | Perm | Purpose |
|---|---|---|
| `/legacy titles` | all | List all titles. |
| `/legacy info <id>` | all | Inspect a title's trigger/requirement. |
| `/legacy eval` | all | Force an evaluation for yourself. |
| `/legacy grant\|revoke <id>` | ops | Grant/revoke a title. |
| `/legacy reload` | ops | Reload all titles live. |

## Custom triggers (API)

```java
LegacyAPI.registerCustomTrigger("legacy:fate",
        (serverPlayer, title) -> serverPlayer.getInventory().contains(new ItemStack(Items.NETHER_STAR)));
```

`LegacyAPI` also offers `grant(player, id)` and `revoke(player, id)`.
