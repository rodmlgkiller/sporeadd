# SporeAdds

A mod that allows the player to play as a spore infected miniboss or choose 4 diferent survivor classes with different skills to fight the infection. Each class is a strong gamechanger originally balanced for pvp so have that in account.

SporeAdds is an addon for the **Spore** mod. Minecraft 1.20.1, Forge 47.4.0+.

## Classes

**Kommandant** — The infected commander. Channels the hive itself, spreading corruption and infection, with unique powers depending on the subclass you choose: *Caustic* drenches enemies in acid and corrosive gas, *Abyssal* calls down oceanic storms and lightning, and *Gluttonous* fights with a stockpile of scavenged ammo.

**Medic** — The team's lifeline. Heals allies (or yourself) with an injector and throwable bandages, and can spot critically wounded players through walls. *Defibrillate* turns a killing blow on an ally into a shockwave and a full heal, while *Self Defibrillate* is a high-stakes last-resort minigame.

**Ghost** — Stealth and assassination. Backstabs hit harder when fully disguised and can silently finish off weakened enemies without alerting their group. *Disguise* turns you invisible (hidden as a bush) and *Decoy* plants bait to pull mobs away from you.

**Scientist** — Turns every kill into knowledge. The Scalpel harvests data from spore infected, feeding *Field Research* (a record of every entity you've fought) and *Expose Weakness*, a scan that reveals weakpoints on anything you've killed before, with bonus damage that scales with the research of every Scientist on the server.

**Berserker** — Risk-and-reward melee. *Counter* turns a well-timed defense into a riposte, *Claws of Brutality* rewards sustained aggression with a temporary clawed rampage, and *Compounds* lets you load syringes that reshape that rampage.

An in-game **Training Book** describes every class in detail, and most systems are configurable.

## Requirements

| Mod | Version | Notes |
|-----|---------|-------|
| Minecraft Forge | 47.4.0+ (1.20.1) | required |
| Spore | 2.2.0j | required |
| Pehkui | 3.8.2 | required |
| Origins (Forge port) | 1.10.0.9 | optional |
| JEI | 15.20.0.111 | optional |

**Origins is optional.** If installed, picking an Origin and picking a class stay in sync. This sync is written against the Forge/Architectury port of Origins (`io.github.edwinmindcraft.origins`). Other implementations that register the `origins` mod id (for example the Fabric version running through Sinytra Connector) will load without errors, but the sync does nothing.

## Configuration

Almost every number and rule of the mod can be changed without recompiling, so server owners can tune the difficulty.

- **In game:** *Mods → SporeAdds → Config*. The mod's own screen lists the gameplay and HUD settings section by section; hover a value to see what it does, its unit, range and default, use *Reset* to restore one default, and edit lists with the *Add entry* button.
- **By file:** `config/sporeadds-config.toml` (gameplay) and `config/sporeadds-client.toml` (HUD). They are created on first launch, every option is documented inside the file, and options added by an update appear in an existing file automatically.
- **On servers:** gameplay options are read by the server (or by the host of a single-player / LAN world). On a dedicated server edit the server's own `config/sporeadds-config.toml`; the in-game screen only edits the local game's file. A few displayed numbers (tooltips, HUD) read the local copy, so keep client and server files equal.
- **Defaults reproduce the original balance**: nothing changes until you edit a value.
- Lists use short formats: items `modid:item;count`, potion effects `modid:effect;durationTicks;amplifier`, attribute modifiers `modid:attribute;amount;ADD|MULT_BASE|MULT_TOTAL`.

| Section | What you can tune |
|---------|-------------------|
| `ghost` | backstab and disguise multipliers, disguise stacks, decoys, Bucket of Remains, execution threshold (HP or percent), Critical Wound, cooldowns and durations |
| `medic` | injector (optional self-injection), bandages, defibrillation, the Self Defibrillate minigame (time, icons, size, movement, damage reduction, health threshold), triage |
| `scientist` | research data and pickup radius, Field Research layout and cooldown, Expose Weakness, weak points, prestige loot and requirements, Nano Injector |
| `berserker` | blood samples (limits and stats per kind), Reinforced Combat Chains, Counter, Claws |
| `kommandant_abilities` | abilities 1-7 (cocoon, extraction, mounds, Devour, manipulation, forced mutation, Second Authority), unlock level of every ability, biomass bar per level, maximum evolution level, class gain rules, ESP highlights |
| `kommandant_abilities_advanced` | abilities 8-13 (horde, infectious burst, Proto tracking, Vigil, biomass armor, nuke) and their biomass costs |
| `locators`, `implant_stats`, `starting_gear` | locator costs and cooldowns, implant abilities and extra attribute modifiers, the starting kit of each class |
| `biomass_core`, `ability_toggles` | the optional Biomass Core action wheel and keeping ability toggles across logins (both off by default) |
| older sections | evolution costs, Verwa lists, punishment and devotion events, Training Book, item restrictions, ... |

## Building

Requires **JDK 17**.

1. Clone the repository.
2. Create a `libs/` folder in the project root and put these jars in it. They are not included in the repository because they belong to their authors (and Spore alone is over GitHub's file size limit):
   - `spore_1.20.1_2.2.0j.jar`
   - `Pehkui-3.8.2+1.20.1-forge.jar`
   - `origins-forge-1.20.1-1.10.0.9-all.jar`
   - `jei-1.20.1-forge-15.20.0.111.jar`
3. Build:
   ```bash
   ./gradlew build
   ```
   The mod jar is written to `build/libs/sporeadd-<version>.jar`.

To run a development client: `./gradlew runClient`.

Notes for the development environment:
- The jars in `libs/` are declared through the `flatDir` repository so that ForgeGradle deobfuscates them (without that, Pehkui's and Spore's mixins cannot find their targets in a dev runtime that uses official names). Spore's file name has no hyphen before the version, so `build.gradle` keeps a copy named `spore-1.20.1_2.2.0j.jar` in `build/devlibs`; nothing in `libs/` is renamed.
- Origins is compile-only in `runClient`: at runtime it needs the Caelus mod, which is not a dependency of this project. To test with Origins, add Caelus (`top.theillusivec4.caelus:caelus-forge:3.2.0+1.20.1`, from `https://maven.theillusivec4.top/`) as `runtimeOnly` and make Origins `implementation`.
- The mixins that target Spore classes use official (Mojang) method names. The Mixin annotation processor writes the production SRG names into `sporeadds.refmap.json`, so do not add `remap = false` to them.

## License

All Rights Reserved. See [LICENSE.txt](LICENSE.txt).
