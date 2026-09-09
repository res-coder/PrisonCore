# PrisonCore 3.1.1

A production-oriented, all-in-one prison progression core built for **Paper 26.2 build 121**.

PrisonCore 3.1.1 expands the original pickaxe/enchant foundation into a broader prison-server core: configurable mines, auto-sell, ranks/prestiges/rebirths, multiple currencies, mine bombs, mining statistics, advanced enchant configuration, and a polished GUI-driven admin workflow.

## Target
- Paper: **26.2 build 121**
- Runtime Java: **25+**
- Source/build target: Java-compatible plugin classes for the Paper 26.2 API

## Major systems
- Versioned PDC Prison Pickaxes with persistent UUIDs and repairable presentation
- Pickaxe XP and configurable nonlinear leveling
- Fully configurable enchant registry and effect tuning in `enchants.yml`
- Efficiency, Fortune, XP Hoarder, Token Finder, Key Finder, Gem Finder, Money Finder, and Explosive
- Custom Fortune that can affect all normal drop-producing blocks, not just ores
- Exact random Token Finder rewards with configurable chance/range scaling per level
- Bulk enchant upgrade GUI (+1, +5, +10, +25, MAX)
- Secure Rename/Lore scroll chat-input workflow with colors, cancel/refund, timeout, and quit protection
- Internal money, tokens, and gems
- Auto Sell with configurable block values and progression multipliers
- Rank, Prestige, and Rebirth progression
- Mining statistics
- Prison Mines with selection wand, GUI editor, weighted block composition, batched resets, percentage/timed reset conditions, spawn/access controls, and player mine browser
- Mine Bombs
- Async player-data persistence
- MiniMessage/Adventure presentation

## Pickaxe presentation
The default presentation is intentionally compact:

```text
CUSTOM PICKAXE NAME
Custom Lore
━━━━━━━━━━━━━━━━━━━━
Level: 3 XP: 60/440 (14%)
━━━━━━━━━━━━━━━━━━━━
Efficiency X
Fortune V
Token Finder XXV
```

Vanilla enchant tooltip duplication is hidden and PrisonCore controls the visible enchant list.

## Player commands
- `/pickaxe` - open the pickaxe/enchant interface
- `/pickaxe repair` - rebuild the held PrisonCore pickaxe presentation
- `/tokens` - view token balance
- `/tokens shop` - open the token shop
- `/balance`, `/bal`, `/money` - view money/tokens/gems
- `/mine`, `/mines` - open the mine directory
- `/mine <id>` - teleport to an accessible mine
- `/autosell`, `/as` - toggle Auto Sell
- `/rankup` - purchase the next rank when eligible
- `/prestige` - prestige after completing the rank ladder
- `/rebirth` - rebirth after reaching the configured prestige requirement
- `/stats`, `/miningstats` - view mining/progression statistics

## Admin commands
- `/pc createmine [id]` - receive the mine-selection wand and begin a two-point mine selection
- `/pc mines` - open/list mine administration
- `/pc mine edit <id>` - open the mine editor GUI
- `/pc mine reset <id>` - reset a mine immediately
- `/pc mine delete <id>` - delete a mine definition
- `/pc mine setspawn <id>` - set a mine spawn to your location
- `/pc mine tp <id>` - teleport to a mine
- `/pc mine info <id>` - inspect mine information
- `/pc givepickaxe <player>`
- `/pc givescroll <player> <rename|lore|transmog> [material] [amount]`
- `/pc givebomb <player> [radius] [amount]`
- `/pc tokens <set|add|take> <player> <amount>`
- `/pc money <set|add|take> <player> <amount>`
- `/pc gems <add|take> <player> <amount>`
- `/pc setlevel <player> <level>`
- `/pc addxp <player> <amount>`
- `/pc setrank <player> <rank>`
- `/pc setprestige <player> <amount>`
- `/pc setrebirth <player> <amount>`
- `/pc inspect [player]`
- `/pc repair [player]`
- `/pc reload`

## Mine creation workflow
1. Run `/pc createmine [id]`.
2. Left-click the first corner with the PrisonCore Mine Wand.
3. Right-click the opposite corner at the desired depth.
4. PrisonCore automatically builds a permanent **BEDROCK shell** on all four side walls and the bottom floor, then fills the entire mineable interior with **STONE**.
5. After the structure finishes, PrisonCore automatically opens the mine editor.
6. Configure block composition, reset threshold, timed reset, spawn, rank requirement, permission, announcements, and reset batch size.
7. The mine stays stone until you change the composition and run **Reset Mine Now**. Every reset preserves/rebuilds the bedrock shell and applies the configured composition only to the interior.

Mine block composition uses weights. This makes configurations easy to scale without requiring the weights to total exactly 100.

## Configuration
- `config.yml` - global behavior, pickaxe appearance, Auto Sell safety, mine-editor palette
- `enchants.yml` - enchant enable state, costs, caps, requirements, proc chances, reward ranges, messages, commands, radii and effect tuning
- `shop.yml` - Token Shop
- `blocks.yml` - Auto Sell block values
- `ranks.yml` - ranks, prestige, rebirth and sell multipliers
- `mines.yml` - persisted mine definitions and composition
- `player-data.yml` - runtime player economy/progression data (generated)

Existing configuration files are merged with new defaults on startup so upgrades do not require deleting the entire PrisonCore folder.

## Important behavior notes
- Token Finder's displayed reward is the exact random amount actually deposited.
- Guaranteed tokens-per-block default to `0`; Token Finder rewards are therefore easy to audit independently.
- Fortune is a custom PrisonCore multiplier and vanilla Fortune is not double-applied.
- Inventory-holder blocks are deliberately protected from custom Fortune multiplication to prevent container duplication/loss exploits.
- Auto Sell defaults to PrisonCore mine regions only for economy safety.
- Mine creation and resets are batched across ticks rather than attempting to rewrite a large mine in one tick.
- Mine walls and floor are permanent bedrock; reset percentages count only the mineable interior.
- Key Finder commands are examples and should be changed to the exact command syntax used by the server's crate plugin.

## 3.1 mining quality-of-life
Normal block drops are auto-picked up by default (`mining.auto-pickup.enabled`). Fortune and Explosive drops use the same inventory-safe pickup path. Full inventories spill overflow at the player rather than deleting it.

All built-in enchants now default to a maximum level of 10. `enchants.yml` also contains `settings.global-max-enchant-level: 10`, which acts as a hard runtime cap and clamps legacy pickaxes above the cap when they are refreshed. Proc enchants use documented per-block decimal chances plus a per-minute cap so owners can balance high-speed mining safely.
