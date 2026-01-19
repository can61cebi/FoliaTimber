# FoliaTimber

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

### A tree feller that respects your builds

FoliaTimber is a high-performance plugin built for **Folia** and **Paper** 1.21+. It allows players to harvest entire trees by breaking just one log, while intelligently identifying and protecting player-built structures and houses.

---

## Technical Visuals

### Efficiency in Action
<img src="assets/chopping.gif" width="600" alt="Tree Chopping Demo">
*Harvesting a natural tree with a single axe swing.*

### Structure Awareness
<img src="assets/protection.gif" width="600" alt="Structure Protection Demo">
*With debug mode enabled, the plugin distinguishes between natural logs and protected structures.*

---

## Why FoliaTimber?

Unlike legacy timber plugins, FoliaTimber was built from the ground up for the multithreaded architecture of **Folia**, utilizing region-aware scheduling to ensure zero impact on server performance.

- **Structure Recognition:** Automatically detects ladders, planks, and signs. Built-in logic prevents accidental damage to player bases.
- **Inventory Management:** Harvested items are added directly to the player's inventory, preventing entity lag and dropped-item clutter.
- **Smart Tool Damage:** Axe durability is only consumed for wood blocks, preserving your tools during leaf decay.
- **Localization:** Full support for English, Turkish, and German.
- **Real-time Debugging:** Toggle `/timber debug` to see exactly why a block was ignored or processed.

---

## Commands

<img src="assets/help.png" width="500" alt="Commands Help">

| Command | Function | Permission |
|:---|:---|:---|
| `/timber` | Toggle the timber feature | `foliatimber.use` |
| `/timber debug` | Toggle real-time detection feedback | `foliatimber.use` |
| `/timber lang` | Switch between English, Turkish, and German | `foliatimber.use` |
| `/timber reload` | Reload the configuration file | `foliatimber.reload` |

---

## Installation

1. Place the JAR in your `plugins` folder.
2. Restart your server.
3. (Optional) Enhance protection with **CoreProtect** and **WorldGuard**.

**Server Requirements:** Folia 1.21+ or Paper 1.21+  
**Java Version:** 21

---

Distributed under the MIT License. Created by can61cebi.
