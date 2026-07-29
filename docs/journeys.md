# Creating your first Journey

A **journey** is a quiet, personal diary entry — the first time *you* did something. Journeys have
**no reward, no toast, no colour**. They are deliberately understated: journeys whisper, titles reward,
milestones celebrate. That hierarchy is a feature; please keep journeys silent.

Every journey is **one JSON file**:

```
data/<namespace>/legacy/journeys/<id>.json
```

## Minimal example

`data/legacy/journeys/first_elytra.json`

```json
{
  "category": "equipment",
  "icon": "minecraft:elytra",
  "title": "Wings of Freedom",
  "description": "Claim the elytra and take to the skies.",
  "trigger": { "type": "advancement", "id": "minecraft:end/elytra" }
}
```

No language file needed — journeys carry their text **literally**, so a datapack author is done after
one file. The player's Journey tab now shows *Wings of Freedom* as `☐` until earned, then `✔` with a
`Completed · Day N` tooltip.

## Fields

| Field | Required | Default | Description |
|---|---|---|---|
| `id` | ➖ | *(filename)* | Unique id; defaults to the file path. |
| `trigger` | ✅ | `{ "type": "custom" }` | The completion condition. See **[triggers.md](triggers.md)**. |
| `title` | ✅ | – | Literal name shown in the list and tooltip. |
| `description` | ➖ | `""` | Literal one-line description shown in the tooltip. |
| `category` | ➖ | `misc` | Section header in the Journey tab. |
| `icon` | ➖ | `minecraft:paper` | Item shown next to the entry. |

### Categories

The Journey tab groups entries under these headers (in this order); any other value falls under its own
heading:

`equipment`, `combat`, `exploration`, `building`, `farming`, `villagers`, `magic`, `misc`

## Notes

- Journeys reuse the **same trigger engine** as titles. Any `advancement`, `statistic`, `counter` or
  `custom` trigger works — see **[triggers.md](triggers.md)**.
- Completion is one-shot and records the world day it happened; it never un-completes.
- Completion is detected on the next evaluation (within ~2 seconds), so "first time" is recorded when
  Legacy notices, not retroactively dated to before install.

## Command

| Command | Purpose |
|---|---|
| `/legacy journeys` | List your journeys with completion status and day. |
