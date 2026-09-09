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

## Build
```bash
mvn clean package
```
The JAR will be created at `target/prisoncore-2.0.0.jar`.

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

## Production validation checklist
Before using on a live economy:
1. Build with Java 21.
2. Boot a clean Paper 1.21.10 test server.
3. Verify `/plugins` shows PrisonCore enabled.
4. Test starter pickaxe, mining progression, each enchant purchase, each scroll, relog, restart, and token persistence.
5. Configure Key Finder to your actual crate plugin command.
6. Stress-test with your mine-reset/block-break plugins because some mine plugins alter or cancel `BlockBreakEvent`.

## Design notes
Authoritative gameplay state is stored in namespaced PDC rather than display text. Lore and names are presentation. Admin repair reconstructs the visible state from PDC. Data persistence avoids periodic disk writes on the main server thread.
