# FoliaTimber

<p align="center">
  <strong>Smart tree chopping with intelligent structure protection for Folia & Paper</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Folia-1.21+-green?style=flat-square" alt="Folia">
  <img src="https://img.shields.io/badge/Paper-1.21+-blue?style=flat-square" alt="Paper">
  <img src="https://img.shields.io/badge/Java-21+-orange?style=flat-square" alt="Java">
  <img src="https://img.shields.io/badge/License-MIT-yellow?style=flat-square" alt="License">
</p>

---

## About

FoliaTimber lets you chop entire trees by breaking a single log. It features intelligent protection that automatically detects and protects player structures, treehouses, and village buildings.

**Built natively for Folia** with thread-safe region scheduling, but also works on Paper.

---

## Features

- **Instant Tree Chopping** - Break one log, the whole tree falls
- **Structure Protection** - Automatically protects player-built structures
- **Treehouse Detection** - Trees with attached ladders, planks, etc. are protected
- **Auto-Collect** - Drops go directly to your inventory
- **Smart Tool Damage** - Only logs damage your axe, not leaves
- **CoreProtect Integration** - Uses block history for accurate protection (optional)
- **WorldGuard Support** - Respects region permissions
- **Multi-Language** - English, Turkish, German

---

## Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/timber` | Toggle timber on/off | `foliatimber.use` |
| `/timber lang <en/tr/de>` | Change language | `foliatimber.reload` |
| `/timber reload` | Reload configuration | `foliatimber.reload` |
| `/timber help` | Show help | `foliatimber.use` |

---

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `foliatimber.use` | Use timber feature | Everyone |
| `foliatimber.bypass` | Bypass structure protection | Nobody |
| `foliatimber.reload` | Reload config and change language | OP |

---

## Configuration

```yaml
language: en  # en, tr, or de

general:
  enabled: true
  require-axe: true
  max-tree-size: 256

protection:
  use-coreprotect: true      # Optional, recommended
  check-treehouse: true
  use-worldguard: true

chopping:
  break-leaves: true
  auto-collect: true
  tool-damage-multiplier: 1.0
```

---

## Installation

1. Download `FoliaTimber-1.0.0.jar`
2. Place in `plugins/` folder
3. Restart server
4. Configure in `plugins/FoliaTimber/config.yml`

---

## Requirements

- **Server**: Folia 1.21+ or Paper 1.21+
- **Java**: 21+
- **Optional**: CoreProtect (for structure protection), WorldGuard

---

## Building

```bash
./gradlew shadowJar
```

---

## License

MIT License

---

## Author

**can61cebi** - [GitHub](https://github.com/can61cebi)
