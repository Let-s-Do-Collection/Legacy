# Legacy Data Pack Reference

Legacy is data-driven: titles, journeys and milestones are defined in JSON and loaded from data packs.
Extending Legacy requires no registries, Java classes or mixins.

## The four systems

| System | Description | Folder |
|---|---|---|
| [Titles](titles.md) | Cosmetic honorifics worn above the player's name, unlocked by triggers. | `legacy/titles/` |
| [Journeys](journeys.md) | A per-player record of first-time events. No rewards, toasts or colour. | `legacy/journeys/` |
| [Milestones](milestones.md) | Server-wide world-firsts, broadcast and recorded once. | `legacy/milestones/` |

Server History is the read-only chronicle of claimed milestones and needs no authoring.

## One file per definition

Each definition is a single JSON file, discovered like advancements and loot tables:

```
data/<namespace>/legacy/
  titles/
    farmer.json
    dragon_slayer.json
  journeys/
    first_elytra.json
    first_trade.json
  milestones/
    liberator_of_the_end.json
    infernal.json
```

Any data pack (in any namespace) can add files here. `/legacy reload` re-reads titles live; a vanilla
`/reload` refreshes all three systems.

## Shared trigger block

All three systems use the same trigger block:

```json
"trigger": { "type": "advancement", "id": "minecraft:story/mine_diamond" }
"trigger": { "type": "counter",     "counter": "harvest:crops", "value": 64 }
"trigger": { "type": "statistic",   "stat_type": "minecraft:killed", "stat": "minecraft:wither", "value": 1 }
```

See [triggers.md](triggers.md) for the full list of trigger types.

## Shared core, system-specific fields

Every definition shares a core, then adds fields specific to its system:

```jsonc
// core (all systems)
{ "id": "...", "icon": "...", "category": "...", "trigger": { ... } }

// title
"translation_key": "...", "rarity": "epic", "placement": "suffix", "series": "...", "stage": 2

// journey
"translation_key": "...", "title": "First Diamond Armor", "description": "Craft a full set of Diamond Armor."

// milestone
"shared": true, "grace_period": 300, "retro_advancement": "..."
```

## Contents

1. [Titles](titles.md)
2. [Journeys](journeys.md)
3. [Milestones](milestones.md)
4. [Trigger types](triggers.md)
