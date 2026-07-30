# Journeys

A journey is a per-player record of a first-time event, for example the first diamond armor or the first
sunrise. A journey has no reward, no toast and no colour. It is only an entry in the player's Journey tab.

Every journey is one JSON file:

```
data/<namespace>/legacy/journeys/<id>.json
```

## Example

`data/legacy/journeys/first_elytra.json`

```json
{
  "category": "equipment",
  "icon": "minecraft:elytra",
  "translation_key": "journey.legacy.first_elytra",
  "title": "First Elytra",
  "description": "Claim the elytra and take to the skies.",
  "trigger": { "type": "advancement", "id": "minecraft:end/elytra" }
}
```

The Journey tab shows the entry with an empty box until it is earned, then a check mark and a
`Completed - Day N` tooltip.

## Fields

| Field | Required | Default | Description |
|---|---|---|---|
| `trigger` | yes | `{ "type": "custom" }` | The completion condition. See [triggers.md](triggers.md). |
| `title` | yes | – | The name shown in the list and tooltip. Used as fallback when `translation_key` is set. |
| `id` | no | *(filename)* | Unique id. Defaults to the file path. |
| `description` | no | `""` | One-line description shown in the tooltip. |
| `translation_key` | no | – | Language key. When set, the client shows `<key>` and `<key>.desc` localised, and falls back to `title`/`description`. |
| `category` | no | `misc` | Section header in the Journey tab. |
| `icon` | no | `minecraft:paper` | Item shown next to the entry. |

### Text: literal or localised

The simplest way is a literal `title` and `description` in the file, so no language file is needed. If
you want the journey translated, set a `translation_key` and add `<key>` and `<key>.desc` to your
language files. The literal text stays as fallback.

### Categories

The Journey tab groups entries under these headers, in this order. Any other value gets its own header:

`equipment`, `exploration`, `survival`, `farming`, `villagers`, `combat`, `magic`, `misc`

## Notes

- Journeys use the same trigger engine as titles. Any `advancement`, `statistic`, `counter`, `item` or
  `custom` trigger works. See [triggers.md](triggers.md).
- Completion is one-shot and stores the world day. It never un-completes.
- Completion is checked on the next evaluation (about every 2 seconds), so the day is when Legacy
  notices, not a retroactive date.

## Command

| Command | Purpose |
|---|---|
| `/legacy journeys` | List your journeys with completion status and day. |
