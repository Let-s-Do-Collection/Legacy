# Legacy — Datapack Guide

Legacy is a **progression platform** for Minecraft. Everything it adds is data-driven: if you can write
a JSON file, you can extend Legacy. No registries, no Java classes, no mixins.

## The four systems

| System | What it is | Loudness | Folder |
|---|---|---|---|
| **[Titles](titles.md)** | Cosmetic honorifics worn above the name | 🔔 small reward (toast) | `legacy/titles/` |
| **[Journeys](journeys.md)** | A personal, silent "first time I did X" diary | 🤫 silent | `legacy/journeys/` |
| **[Milestones](milestones.md)** | Server-wide world-firsts | 📣 broadcast to everyone | `legacy/milestones/` |

The remaining system, **Server History**, is the read-only chronicle of milestones already claimed — it
needs no authoring.

## One file, one thing

Each definition is a single JSON file, discovered exactly like advancements and loot tables:

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

Any datapack (in any namespace) can add files here, and any mod ships them the same way. `/legacy reload`
re-reads titles live; a vanilla `/reload` refreshes all three.

## One trigger block everywhere

All three systems share the **same** trigger block — learn it once:

```json
"trigger": { "type": "advancement", "id": "minecraft:story/mine_diamond" }
"trigger": { "type": "counter",     "counter": "harvest:crops", "value": 64 }
"trigger": { "type": "statistic",   "stat_type": "minecraft:killed", "stat": "minecraft:wither", "value": 1 }
```

Full reference: **[triggers.md](triggers.md)**.

## Shared core, system-specific extras

Every definition shares a core, then adds only what its system needs:

```jsonc
// core (all systems)
{ "id": "...", "icon": "...", "category": "...", "trigger": { ... } }

// title adds
"translation_key": "...", "rarity": "epic", "placement": "suffix", "series": "...", "stage": 2

// journey adds
"title": "First Diamond Armor", "description": "Craft your first set of Diamond Armor."

// milestone adds
"shared": true, "grace_period": 300, "retro_advancement": "..."
```

## Start here

1. **[Creating your first Title](titles.md)**
2. **[Creating your first Journey](journeys.md)**
3. **[Creating your first Server Milestone](milestones.md)**
4. **[Supported Trigger Types](triggers.md)**
