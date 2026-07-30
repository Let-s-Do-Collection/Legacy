# Milestones

A milestone is a server-wide world-first. When the first player on the server does something, it is
recorded with the player's name, world day and real date, a toast is broadcast to everyone, and that
player earns a wearable milestone title.

Every milestone is one JSON file:

```
data/<namespace>/legacy/milestones/<id>.json
```

## Example

`data/legacy/milestones/liberator_of_the_end.json`

```json
{
  "id": "liberator_of_the_end",
  "icon": "minecraft:dragon_head",
  "rarity": "legendary",
  "shared": true,
  "trigger": { "type": "kill", "id": "minecraft:ender_dragon" },
  "retro_advancement": "minecraft:end/kill_dragon"
}
```

The display name defaults to `milestone.legacy.<id>`, so add it to your language files.

## Fields

| Field | Required | Default | Description |
|---|---|---|---|
| `trigger` | yes | `{ "type": "advancement" }` | The world-first condition: `dimension`, `kill` or `advancement`. See [triggers.md](triggers.md). |
| `id` | no | *(filename)* | Unique id. Defaults to the file name. |
| `translation_key` | no | `milestone.legacy.<id>` | Display-name language key. |
| `icon` | no | `minecraft:paper` | Item shown in the UI and toast. |
| `rarity` | no | `epic` | Rarity, sets the colour. |
| `shared` | no | `false` | If true, others can also earn it during the grace window (a co-op boss kill). |
| `grace_period` | no | `300` | Grace window in seconds, only for `shared` milestones. |
| `retro_advancement` | no | *(the trigger's advancement)* | Advancement used to mark the milestone as pre-existing on worlds that already had it before Legacy was installed, so it cannot be claimed later. |

## How claiming works

1. The first player to satisfy the trigger claims the milestone. Their name, world day and date are
   stored, everyone gets a toast, and they receive the `milestone_<id>` title.
2. If `shared` is true, any other player who satisfies it within `grace_period` seconds is added as a
   co-recipient, so a team that kills the dragon together all earn it.
3. On join, players are checked against `retro_advancement`. If the world already had the achievement
   before records began, the milestone is marked pre-existing and cannot be claimed later.

## Milestone trigger types

Milestones fire on the event itself, not on a poll, so they use event-based types:

| `type` | `id` | Fires when |
|---|---|---|
| `dimension` | dimension id, e.g. `minecraft:the_nether` | first player enters it |
| `kill` | entity id, e.g. `minecraft:wither` | first player kills one |
| `advancement` | advancement id | first player earns it |

## Command

| Command | Purpose |
|---|---|
| `/legacy milestones` | List all milestones and who claimed each. |
