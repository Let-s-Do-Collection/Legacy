# Let's Do: Legacy

An Architectury multi-loader Minecraft mod (Fabric + NeoForge) for Minecraft **1.21.1**.

A purely cosmetic, long-term **title system**: players unlock titles through their activities and wear
exactly one above their name. No skills, no levels, no attributes, no gameplay advantages.

- **Mod ID:** `legacy`
- **Display name:** Let's Do: Legacy
- **Base package:** `net.satisfy.legacy`
- **Maven group:** `net.satisfy`

## Project layout

```
common/     Shared, loader-independent code and resources (assets + data)
fabric/     Fabric loader entry points + client mixins
neoforge/   NeoForge loader entry points + client mixins
```

The mod ships with a curated set of **35 titles** — mostly evocative honorifics plus a few classic
professions — across fishing, mining, caves, nether, end, bosses, combat, pvp, exploration, playtime,
trades and farming. See **[docs/titles.md](docs/titles.md)** for the full `titles.json` reference.

## How it works

- **Definition** — every title is declared in [`data/legacy/titles.json`](common/src/main/resources/data/legacy/titles.json),
  loaded by [`TitleManager`](common/src/main/java/net/satisfy/legacy/core/title/TitleManager.java) as a
  data-pack reload listener (with validation), and synced to clients. Reload live with `/legacy reload`.
- **Unlocking** — [`TitleService`](common/src/main/java/net/satisfy/legacy/server/TitleService.java) evaluates
  each title's [trigger](common/src/main/java/net/satisfy/legacy/core/trigger/TitleTriggers.java) periodically
  per player. Supported triggers: `minecraft_stat`, `advancement`, `dimension`, `biome`, `custom`.
- **Persistence** — per-player unlocked set + active title in
  [`LegacyTitleSavedData`](common/src/main/java/net/satisfy/legacy/core/data/LegacyTitleSavedData.java) (overworld data storage).
- **Display** — the active title is injected into the floating **nametag only** (never chat or tab list)
  via an `EntityRenderer#renderNameTag` mixin, using `prefix` or `suffix` placement. Each title has a
  `rarity` (`common`/`uncommon`/`rare`/`epic`/`legendary`/`mythic`) mapped to a fixed vanilla colour; that
  colour is applied to **both the title and the wearer's name**. Your own name is also shown in third person.
- **Notification** — unlocking shows a vanilla-styled toast (title name + icon + sound); no chat message.
- **Selection** — a custom-icon button next to the recipe-book button in the survival inventory (mixin)
  toggles the [Legacy Journal](common/src/main/java/net/satisfy/legacy/client/JournalPanel.java): a tabbed
  side panel rendered *inside* the inventory (recipe-book style, no separate screen) with Titles /
  Statistics / Milestones tabs. The Titles tab lists unlocked titles with a checkmark on the active one.
  Statistics and Milestones are scaffolds for v1.1.

### Title JSON schema

```json
{
  "id": "dragon_slayer",
  "translation_key": "title.legacy.dragon_slayer",
  "placement": "suffix",
  "trigger": "advancement",
  "requirement": { "advancement": "minecraft:end/kill_dragon" },
  "category": "combat",
  "display_priority": 20,
  "hidden": false,
  "icon": "minecraft:dragon_head",
  "rarity": "legendary"
}
```

Required: `id`, `translation_key`, `placement`, `trigger`, `requirement`.
Optional: `category`, `display_priority`, `hidden`, `icon`, `rarity`.

### Translations

Titles use standard Minecraft language files, e.g. `title.legacy.dragon_slayer` in
[`en_us.json`](common/src/main/resources/assets/legacy/lang/en_us.json) /
[`de_de.json`](common/src/main/resources/assets/legacy/lang/de_de.json).

## Commands

`/legacy status | titles | list | info <id> | eval | clear` (everyone) and
`/legacy reload | grant <id> | revoke <id>` (ops). See [docs/titles.md](docs/titles.md#commands).

## API for other mods

[`LegacyAPI`](common/src/main/java/net/satisfy/legacy/api/LegacyAPI.java) exposes
`registerCustomTrigger`, `grant` and `revoke`, so other mods can drive titles without their own system.

## Building

Requires JDK 21.

```bash
./gradlew build
```

Jars land in `fabric/build/libs/` and `neoforge/build/libs/`.
