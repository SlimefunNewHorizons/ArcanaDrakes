<p align="center">
  <img src="https://raw.githubusercontent.com/DrakesCraft-Labs/ArcanaDrakes/main/banner.svg" width="100%" alt="ArcanaDrakes animated banner" />
</p>

# ArcanaDrakes

> ### 🏰 ¡Únete a la Comunidad Oficial de DrakesCraft!
> 
> * 🎮 **IP del Servidor**: `play.drakescraft.cl` *(Java 1.21.11 & Bedrock)*
> * 💬 **Discord Oficial**: [discord.gg/drakescraft](https://discord.gg/rv3vtXZTk7)
> * 🌐 **Web & Guía**: [web.drakescraft.cl](https://web.drakescraft.cl) — 🛒 **Tienda**: [web.drakescraft.cl/store](https://web.drakescraft.cl/store.html)
> 
> *¡Juega con este addon y más de 80 expansiones optimizadas en vivo en nuestra network de supervivencia técnica!*

---

**Elemental progression for DrakesCraft, built for Paper/Purpur 1.21.11.** Arcana gives survival players a parallel path based on exploration, controlled spell spectacle, ranks, sigils, mines, and material progression without turning Slimefun endgame items into shop currency.

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-ea8b23?style=flat-square" alt="Java 21" />
  <img src="https://img.shields.io/badge/Platform-Paper%20%2F%20Purpur%201.21.11-4b8bbe?style=flat-square" alt="Paper Purpur 1.21.11" />
  <img src="https://img.shields.io/badge/Combat-Claim%20safe-8b5cf6?style=flat-square" alt="Claim safe" />
  <a href="https://reporanker.com/repos/DrakesCraft-Labs/ArcanaDrakes"><img src="https://reporanker.com/badge/DrakesCraft-Labs/ArcanaDrakes" alt="RepoRanker" /></a>
</p>

## Design principles

- **Every player starts different.** A first join assigns one primary affinity and one compatible origin at random. Staff can correct a bad roll through a restricted command.
- **Spectacle without griefing.** Spells use particles, telegraphs, sound and bounded entity damage. They never place, break, replace, move, or ignite blocks.
- **A survival complement, not an item dupe.** Arcana rewards exploration materials, sigils and progression. It does not sell finished Infinity, Supreme, top-tier Slimefun weapons, armor, or machines.
- **Claims are sacred.** Offensive effects are PvE-only by default and only in configured worlds. A future Arcana PvP zone must be deliberately enabled, not accidentally inherited from survival.
- **Configuration first.** Cooldowns, worlds, damage targets, mine rewards, trader policy and future equipment gates live in `config.yml`, so normal balancing does not require a new build.

## Affinities and origins

| Affinity | Origins | Identity |
| --- | --- | --- |
| Fire | Phoenix, Blaze | Pressure, embers and explosive momentum. |
| Earth | Iron Guardian, Ravager | Fortitude, terrain resonance and controlled force. |
| Air | Breeze, Phantom | Mobility, gusts and evasive movement. |
| Water | Guardian, Drowned | Flow, recovery and pressure. |
| Ice | Stray, Snow Guardian | Restraint, precision and cold control. |
| Electro | Ender Walker, Storm Caller | Charges, blink-like movement and storm impact. |

Origins are a gameplay identity, not a permanent creature disguise. **Light** and **Shadow** are advanced disciplines earned through progression rather than extra random starting rolls.

## Current gameplay surface

| Feature | Status |
| --- | --- |
| Persistent profiles | SQLite records affinity, origin, experience and sigils per UUID. |
| Codex | Players can obtain an in-game reference book for their Arcana identity. |
| Pulse | Short-range elemental projectile with particles, hit feedback and a configurable cooldown. |
| Domain | An eight-second area ultimate with a global concurrency budget, telegraph circle, sound and configurable cooldown. |
| Spirituality | `/arcana meditate` grants bounded Arcana Essence, experience and Sigils; `/arcana spirit` explains the active resonance. |
| Boss compatibility | Offensive spells are permitted in `drakes_bosses` through DrakesBosses when enabled in configuration. |
| Ranks | Public ladder: Awakening, Explorer, Catalyst, Vanguard, Ascended and Archon. |
| Mines, traders and gear | Configuration contract and progression policy are present; the related gameplay modules are expanded incrementally rather than shipped as untested endgame content. |

## Commands

| Command | Purpose |
| --- | --- |
| `/arcana` | Opens the interactive Arcana Guide with the current profile and safe action buttons. |
| `/arcana guide` | Reopens the interactive guide. |
| `/arcana info` | Shows the current affinity, origin and disciplines. |
| `/arcana book` | Gives the Arcana Codex. |
| `/arcana cast pulse` | Casts the short-range PvE Pulse. |
| `/arcana cast domain` | Casts the area Domain ultimate. |
| `/arcana staff set <player> <affinity>` | Staff-only affinity correction and testing. |

Permissions, claims and configured worlds remain the final authority. Do not expose the staff command to ordinary players.

## Spirituality and DiosesDrakes

Arcana spirituality is a bridge between an elemental identity and a player's divine path, not a second pantheon economy.

1. **Meditation**: `/arcana meditate` is a cooldown-bound ritual that grants Arcana-only Essence, experience and Sigils. It creates an enchantment effect and sound, but no blocks, entities, containers or claim changes.
2. **Patron resonance**: when the optional `DiosesDrakes` public service is available, Arcana reads the selected patron and current favor. A compatible affinity/patron pairing applies the modest multiplier configured under `spirituality.resonance`.
3. **No favor minting**: Arcana never writes to the DiosesDrakes database, awards divine favor, spends favor, or changes a patron. Bosses remain DrakesBosses territory and Convergence remains DiosesDrakes territory.
4. **Graceful fallback**: if DiosesDrakes is missing, disabled, or has no selected patron for a player, meditation remains available at its base reward.

Compatibility is intentionally data-driven. Server operators can map any deity to any primary affinity in `config.yml` without compiling Arcana again.

## Safety model

Arcana damage is intentionally conservative:

- By default, it targets monsters only.
- It is active only in `world` and DrakesBosses' `drakes_bosses`.
- Player-versus-player Arcana is disabled.
- Domains have duration, cooldown and global concurrency limits.
- Visual effects do not alter blocks, containers, machines, claims or protection flags.
- ProtectionStones and WorldGuard are declared integration boundaries before any future claim-aware PvP mode is enabled.

## Configuration

The main operational file is `plugins/DrakesArcana/config.yml`. It groups the values administrators are likely to balance:

| Section | Examples |
| --- | --- |
| `profiles` | Random first-join assignment, allowed affinities and compatible origins. |
| `combat` | Offensive worlds, PvP policy, monster-only targeting, base Pulse/Domain values. |
| `effects` | Cooldowns, domain duration, concurrency and particle budgets. |
| `progression` | Rank thresholds, sigil policy and weekly-maintenance switch. |
| `spirituality` | Meditation cooldown/rewards, Essence ceiling and compatible deity resonance. |
| `mines` | Mine world, regeneration cadence, reward rates and safe ore palette. |
| `traders` | Sigil trader policy and refresh cadence. |
| `equipment` | Rank gates and material-only crafting tiers. |
| `integrations` | DrakesBosses, DiosesDrakes, AuraSkills, ElementManipulation and protection bridges. |

Edit the values, retain YAML indentation, then restart during a maintenance window. A configuration reload command can be added once every subsystem owns safe reload semantics; until then, a restart avoids half-reloaded gameplay state.

## Integrations

- **DrakesBosses**: `drakes_bosses` is a sanctioned combat world for bosses and Arcana effects.
- **DiosesDrakes**: divine lore and blessings may enrich Arcana through an optional API, never a hard boot dependency.
- **AuraSkills**: remains the base-stat system. Arcana does not duplicate AuraSkills experience.
- **ElementManipulation**: stays an independent Slimefun addon and implementation reference; Arcana does not copy its item catalog.
- **ProtectionStones / WorldGuard**: claim-aware boundaries for future PvP and effect permissions.

## Build and test

```bash
mvn test
mvn package
```

The deployable JAR is produced under `target/`. Build success is not deployment verification: before replacing anything on a live server, back up the current JAR/config/database, stage one artifact only, restart in a planned window, and confirm the startup log.

## Roadmap discipline

Arcana is intentionally being built in vertical slices. The active affinity/profile/combat core is testable now; mines, traders and equipment will be added as gameplay modules with their own tests, claim checks and balance validation. This keeps a large progression system from becoming a loose collection of overpowered items.

---

## 📄 License & Intellectual Property

Copyright © 2026 [**JackStar6677-1**](https://github.com/JackStar6677-1) · [**DrakesCraft Labs**](https://github.com/DrakesCraft-Labs). All Rights Reserved.

This software is **Source-Available** for public inspection and technical audit. Redistribution, commercial repackaging, or unauthorized derivative distribution without explicit written permission from the author is strictly prohibited.
