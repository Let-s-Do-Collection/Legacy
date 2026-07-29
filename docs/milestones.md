# Creating your first Server Milestone

A **milestone** is a server-wide world-first: the biggest tier of celebration. When the first player on
the server does something, it is recorded forever (with the player's name, world day and real date), a
toast is broadcast to everyone, and that player earns a wearable milestone title. Milestones are the
loud, shared events — one hierarchy step above titles.

Every milestone is **one JSON file**:

```
data/<namespace>/legacy/milestones/<id>.json
```

## Minimal example

`data/legacy/milestones/first_dragon.json`

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

The display name defaults to `milestone.legacy.<id>` (add it to your lang files), and the earned toast
reads *"<Player> earned / Liberator of the End"*.

## Fields

| Field | Required | Default | Description |
|---|---|---|---|
| `id` | ➖ | *(filename)* | Unique id; defaults to the file name. |
| `trigger` | ✅ | `{ "type": "advancement" }` | The world-first condition — `dimension`, `kill` or `advancement`. See **[triggers.md](triggers.md)**. |
| `translation_key` | ➖ | `milestone.legacy.<id>` | Display-name language key. |
| `icon` | ➖ | `minecraft:paper` | Item shown in the UI and toast. |
| `rarity` | ➖ | `epic` | Rarity → colour. |
| `shared` | ➖ | `false` | If true, others can also earn it during the grace window (a co-op boss kill). |
| `grace_period` | ➖ | `300` | Grace window in **seconds** for `shared` milestones. |
| `retro_advancement` | ➖ | *(the trigger's advancement)* | Advancement used to mark the milestone as *pre-existing* on worlds that already achieved it before Legacy was installed, so it can't be falsely "claimed" later. |

## How claiming works

1. The **first** player to satisfy the trigger claims the milestone: their name, world day and date are
   recorded, everyone gets a toast, and they receive the `milestone_<id>` title.
2. If `shared` is true, any other player who satisfies it **within `grace_period` seconds** is added as a
   co-recipient (so a team that downs the dragon together all earn it).
3. On join, players are checked against `retro_advancement`: if the world already had the achievement
   before records began, the milestone is marked *pre-existing* and can never be claimed retroactively.

## Milestone trigger types

Milestones fire on the event itself (not a poll), so they use event-scoped types:

| `type` | `id` | Fires when… |
|---|---|---|
| `dimension` | dimension id, e.g. `minecraft:the_nether` | first player enters it |
| `kill` | entity id, e.g. `minecraft:wither` | first player kills one |
| `advancement` | advancement id | first player earns it |

## Command

| Command | Purpose |
|---|---|
| `/legacy milestones` | List all milestones and who claimed each. |
