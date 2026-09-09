# PrisonCore 2.0.0

A production-oriented Prison progression core for modern Paper servers.

## Target
- Minecraft / Paper: **1.21.10**
- Java: **21**
- Build: Maven

## Included systems
- Versioned PDC Prison Pickaxes with unique UUIDs and repairable presentation
- Pickaxe XP / configurable nonlinear leveling / level cap
- Internal persistent token economy
- Token shop GUI
- Enchant GUI with configurable level requirements, caps, and exponential costs
- Efficiency, Fortune, XP Hoarder, Key Finder, Token Finder
- Configurable Key Finder console-command rewards (ExcellentCrates-friendly without a hard dependency)
- Per-player reward rate limits
- Rename, Lore, and Transmog scrolls
- Protected drop/despawn behavior
- Starter pickaxe flow
- Admin inspection, repair, progression, token, and item tools
- Async periodic player-data persistence
- MiniMessage / Adventure presentation
- Unit-tested progression math

## Commands
### Players
- `/pickaxe` - open enchant GUI
- `/pickaxe rename <name>` - use a consumed Rename Scroll credit
- `/pickaxe lore <text>` - use a consumed Lore Scroll credit
- `/pickaxe repair` - rebuild the held pickaxe presentation
- `/tokens` - view balance
- `/tokens shop` - open token shop

### Admin
- `/pc givepickaxe <player>`
- `/pc givescroll <player> <rename|lore|transmog> [material] [amount]`
- `/pc tokens <set|add|take> <player> <amount>`
- `/pc setlevel <player> <level>`
- `/pc addxp <player> <amount>`
- `/pc inspect [player]`
- `/pc repair [player]`
- `/pc reload`

## Important deployment note
Key Finder's default command is an example ExcellentCrates-style command and may need to be changed to the exact command used by your crate plugin. Edit `config.yml -> rewards.key-finder.commands` before production.
