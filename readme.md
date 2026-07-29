# Let's Do: Legacy

Legacy is a Minecraft mod for Minecraft 1.21.1, built with Architectury for Fabric and NeoForge.
ham
It adds cosmetic progression through titles, personal journeys and server milestones. The mod does not
introduce skills, levels, attributes or gameplay bonuses.

- **Mod ID:** `legacy`
- **Display name:** Let's Do: Legacy
- **Base package:** `net.satisfy.legacy`
- **Maven group:** `net.satisfy`

## Project layout

```
common/     Shared code and resources
fabric/     Fabric entry points
neoforge/   NeoForge entry points
```

## Data packs

Legacy loads titles, journeys and milestones from data packs using Minecraft's resource reload system.
Each definition is stored in its own JSON file.

```
data/<namespace>/legacy/
  titles/
  journeys/
  milestones/
```

All three systems use the same trigger format.

```json
"trigger": {
  "type": "counter",
  "counter": "harvest:crops",
  "value": 64
}
```

See the documentation in [`docs/`](docs/README.md) for the JSON format and available trigger types.

## Systems

| System | Description |
| --- | --- |
| Titles | Cosmetic titles displayed above the player's name. |
| Journeys | A per-player record of completed milestones and first-time events. |
| Milestones | World-first events shared across the server. |
| Server History | A chronological record of completed server milestones. |

## Implementation

- Definitions are loaded by `TitleManager`, `JourneyManager` and `MilestoneManager`.
- Titles and journeys use the same trigger evaluation system.
- Milestones are evaluated separately because they record world-first events.
- Player progress is stored in `LegacyTitleSavedData`.
- Server milestone data is stored in `LegacyMilestoneSavedData`.
- The active title is rendered above the player's nametag.
- The journal is available through a button next to the recipe book.

## Commands

```
/legacy status
/legacy titles
/legacy journeys
/legacy milestones
/legacy reload
...
```

See `docs/titles.md` for the complete command reference.

## API

`LegacyAPI` exposes methods for registering custom triggers and granting or revoking titles.

Most integrations do not require Java code. Additional titles, journeys and milestones can be added
through data packs.

## Building

Requires JDK 21.

```bash
./gradlew build
```

Build artifacts are written to:

```
fabric/build/libs/
neoforge/build/libs/
```