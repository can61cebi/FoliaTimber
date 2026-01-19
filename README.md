# FoliaTimber 🪓

<p align="center">
  <img src="logo.png" alt="FoliaTimber Logo" width="200">
</p>

<p align="center">
  <strong>Smart tree chopping with intelligent structure protection for Folia & Paper</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Folia-1.21.11-green?style=for-the-badge" alt="Folia">
  <img src="https://img.shields.io/badge/Paper-1.21.x-blue?style=for-the-badge" alt="Paper">
  <img src="https://img.shields.io/badge/Java-21-orange?style=for-the-badge" alt="Java">
  <img src="https://img.shields.io/badge/License-MIT-yellow?style=for-the-badge" alt="License">
</p>

---

## 🌟 About

FoliaTimber is a high-performance Minecraft plugin designed specifically for the multithreaded **Folia** environment. It allows players to chop entire trees by breaking just a single log, while ensuring that player-built structures, treehouses, and village buildings remain completely safe.

**Built natively for Folia** with thread-safe region scheduling, but also fully compatible with **Paper**.

---

## 🔥 Features

- ⚡ **Instant Tree Chopping** - Break one log, the whole tree falls (with smooth animations).
- 🏠 **Structure Protection** - Automatically detects and protects player-built structures.
- 🌳 **Treehouse Detection** - Trees with attached ladders, planks, or other blocks are ignored to protect your base.
- 📦 **Auto-Collect** - Drops are automatically added to your inventory to prevent lag and item clutter.
- 🛠️ **Smart Tool Damage** - Durability is only consumed for the logs, saving your axe from leaf decay.
- 🔍 **CoreProtect Integration** - Leverages block history for pixel-perfect protection.
- 🛡️ **WorldGuard Support** - Respects all region flags and permissions.
- 🌍 **Multi-Language Support** - English (`en`), Turkish (`tr`), and German (`de`) out of the box.
- 🐞 **Advanced Debug Mode** - Per-player `/timber debug` command to see why a tree was (or wasn't) chopped.

---

## 🎮 Commands

| Command | Description | Permission |
|:---|:---|:---|
| `/timber` | Toggle timber feature on/off | `foliatimber.use` |
| `/timber debug` | Toggle your personal debug mode | `foliatimber.use` |
| `/timber lang <en|tr|de>` | Change your preferred language | `foliatimber.use` |
| `/timber reload` | Reload the plugin configuration | `foliatimber.reload` |
| `/timber help` | Show this help menu | `foliatimber.use` |

---

## 🔐 Permissions

- `foliatimber.use`: Allows using the timber feature and basic commands. (Default: Everyone)
- `foliatimber.reload`: Allows reloading the config and changing global language settings. (Default: OP)
- `foliatimber.bypass`: Allows chopping logs that are part of protected structures. (Default: Nobody)

---

## ⚙️ Configuration

The `config.yml` is simple and powerful:

```yaml
language: tr  # en, tr, or de

# Debug mode - Global toggle
debug: false

general:
  require-axe: true
  max-tree-size: 256

protection:
  use-coreprotect: true
  check-treehouse: true
  use-worldguard: true
```

---

## 🛠️ Building

To build the project yourself:

```bash
./gradlew clean build
```

---

## 📜 License

Distributed under the **MIT License**. See `LICENSE` for more information.

---

<p align="center">
  Made with ❤️ by <strong>can61cebi</strong>
</p>
