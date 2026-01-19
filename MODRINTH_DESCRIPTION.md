# FoliaTimber

**A smart tree feller built for performance and protection.**

FoliaTimber allows for efficient tree harvesting by breaking a single log, while ensuring player structures remain safe. Native support for high-performance **Folia** (1.21.11+) and **Paper** environments.

---

## Key Features

- **Instant Harvesting:** Break the base log to feller the entire tree.
- **Structure Recognition:** Intelligently ignores player builds, villager houses, and treehouses.
- **Optimized Inventory:** Items are added directly to the inventory to eliminate entity lag.
- **Axe Preservation:** Durability is only spent on logs, not decaying leaves.
- **Integration:** Compatible with **CoreProtect** and **WorldGuard** for enhanced safety.
- **Localization:** Native support for English, Turkish, and German.
- **Debug Insights:** Use `/timber debug` to analyze how the plugin identifies blocks in real-time.

---

## Demonstrations

### Tree Harvesting
![chopping demo](https://cdn.modrinth.com/data/cached_images/e8c2ffe684985cd2f0adf722d5814337d1faf303.gif)

### Structure Protection
(Please upload your "Protection Demo" GIF here via Modrinth settings)

---

## Commands

- `/timber` — Toggle the timber feature.
- `/timber debug` — Toggle per-player debug messages.
- `/timber lang <en/tr/de>` — Change preferred language.
- `/timber reload` — Reload configuration (Admin).

---

## Implementation

FoliaTimber uses region-aware scheduling to maintain peak performance on Folia servers. By analyzing connected blocks and structure history, it distinguishes between natural world-gen and player-placed architecture.

---

MIT License - Copyright (c) 2026 can61cebi
