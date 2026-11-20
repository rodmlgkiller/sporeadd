# SporeAdds

A mod that allows the player to play as a spore infected miniboss or choose 4 diferent survivor classes with different skills to fight the infection. Each class is a strong gamechanger originally balanced for pvp so have that in account.

SporeAdds is an addon for the **Spore** mod. Minecraft **1.21.1**, **NeoForge** 21.1.x.

> This is the `1.21.1` branch. The Minecraft 1.20.1 (Forge) version lives on the `main` branch and is maintained in parallel; see [PORTING.md](PORTING.md) for what changed in the port.

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
| NeoForge | 21.1.212+ (Minecraft 1.21.1) | required |
| Spore | 2.2.0j (NeoForge 1.21.1 build) | required |
| Pehkui | 3.8.3 (NeoForge 1.21) | required |
| JEI | 19.57+ | optional |

**Origins:** the Origins integration of the 1.20.1 version needs the Forge port of Origins, which does not exist for NeoForge 1.21.1. On this branch the integration is a no-op stub (`OriginApiBridge`) and the class/origin sync stays disabled until an Origins build for 1.21.1 is available.

## Configuration

Almost every number and rule of the mod can be changed without recompiling, so server owners can tune the difficulty.

- **In game:** *Mods → SporeAdds → Config* (NeoForge's configuration screen). Browse the gameplay (*Common*) and HUD (*Client*) settings section by section and hover a value to see what it does, its unit, range and default.
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

Requires **JDK 21**.

1. Clone the repository.
2. Create a `libs/` folder in the project root and put these jars in it. They are not included in the repository because they belong to their authors (and Spore alone is over GitHub's file size limit):
   - `spore_1.21.1_2.2.0j_neo.jar`
   - `Pehkui-3.8.3+1.21-neoforge.jar`
   - `jei-1.21.1-neoforge-19.57.0.450.jar` (optional, only needed to compile the JEI plugin)
3. Build:
   ```bash
   ./gradlew build
   ```
   The mod jar is written to `build/libs/sporeadd-1.21.1-<version>.jar`.

To run a development client: `./gradlew runClient`.

## License

All Rights Reserved. See [LICENSE.txt](LICENSE.txt).
