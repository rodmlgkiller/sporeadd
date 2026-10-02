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

## License

All Rights Reserved. See [LICENSE.txt](LICENSE.txt).
