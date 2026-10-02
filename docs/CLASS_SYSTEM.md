# The `/class` system of SporeAdds (blueprint for reimplementing it in another mod)

Goal of this document: describe exactly what the system does so it can be rebuilt from scratch in another mod (NeoForge
1.21.1) **and stay compatible with SporeAdds**, meaning both mods read and write the same per-player class.

Source of truth in this repo (`src/main/java/com/sporeadds/sporeaddsmod/`):
`commands/ClassCommand`, `PlayerData/SporeIdentifierData` + `SporeIdentifierProvider`, `util/SporeIdentifierUtil`,
`util/ClassAssignmentUtil`, `util/SporeClassUtil`, `network/SyncSporeIdentifierPacket`, `network/SelectClassPacket`,
`data/ClassPopulationData`, `event/ForgeEvents` (login/clone/respawn/dimension hooks).

## 1. What a "class" is

* Per player: `identifier` (string) and `subclass` (string, default `"none"`).
* Valid identifiers are lowercase: `kommandant`, `ghost`, `medic`, `scientist`, `berserker`, `none` (default).
  Legacy `slasher` is migrated to `berserker` when loading NBT.
* Subclasses: only `kommandant` has real ones (`caustic`, `none`, `abyssal`, `gluttonous`); every other class uses `none`.
* Extra fields in the same record: `camouflaged`, `camouflageCooldown`, `receivedTrainingBook` (SporeAdds-specific).
* The class is checked everywhere with `SporeClassUtil.hasClass(player, "medic")`, i.e. `identifier.equals(id)`.

## 2. Persistent data (the contract that must be shared)

SporeAdds stores it as a NeoForge data attachment `sporeadd:spore_identifier`, serialized as a `CompoundTag`:

| NBT key | Type | Meaning |
|---------|------|---------|
| `SporeRoleId` | string | identifier |
| `SporeSubclassId` | string | subclass |
| `SporeCamouflaged` | boolean | ghost disguise active |
| `SporeCamouflageCooldown` | int | ticks |
| `SporeReceivedTrainingBook` | boolean | first-join book given |

Load rules worth copying: if the tag has no `SporeRoleId` do nothing; a stored `"none"` never overwrites an already set class;
unknown ids are ignored. The attachment is `copyOnDeath` (class survives death and the End return).

## 3. Setting a class (server side, `/class` and the Training Book both end up here)

Order of operations in `ClassAssignmentUtil.applyClass` / `ClassCommand.setClassAction`:

1. Reject if identifier and subclass already equal the current ones.
2. `SporeIdentifierUtil.setIdentifierAndSync`: validate id, set it (resets subclass to `none`), then
   reset the ability "switches" string (`"00000000000000"`), update `ClassPopulationData` (SavedData map UUID -> class,
   removed when `none`; used for the Training Book counters), clear all effects when becoming `kommandant`, reset Pehkui scales,
   clear berserker ability state when leaving berserker, then sync to clients and sync Origins.
3. `setSubclassAndSync`, then `Levelstats.forceReapplyStats` (re-applies level-based attribute modifiers).
4. Team: `kommandant` -> scoreboard team `spore`, every other class -> team `mercs` (teams are created on demand, the player is
   removed from any previous team first). Several mixins and handlers key off the `spore` team name.
5. Class items: medic gets injector + 16 throwable bandages + "Insanity mask" gas mask (and loses them when leaving);
   scientist gets/loses the scalpel.
6. Chat feedback via translation keys `message.sporeadd.class.updated`, `command.sporeadd.class.set.success`,
   `command.sporeadd.class.error.already_has`, `command.sporeadd.class.error.invalid_identifier`.

## 4. Command

`/class <player> <identifier> [<subclass>] [animated]`, permission level 2, `player` is an `EntityArgument.player()`.
Identifier suggestions come from the valid-id list; subclass suggestions are the kommandant subclasses when the identifier is
`kommandant`, otherwise just `none`. The optional `animated` literal starts the hive "induction" cinematic; without it there is
no animation or teleport.

## 5. Synchronisation

* Server -> clients: `SyncSporeIdentifierPacket(playerId:int, identifier:utf, subclass:utf)` sent to *tracking players and self*
  (`PacketDistributor` tracking-entity-and-self) after every change. The client handler mirrors it into the client-side
  attachment of that entity (needed for renderers, HUD and tooltips).
* Re-sent on: login, respawn, dimension change, and after `PlayerEvent.Clone` (see `ForgeEvents.syncSporeIdentifier`).
* Client -> server: `SelectClassPacket(identifier, hand)` from the Training Book (validates id, config toggle, and that the
  player is holding the book, then calls `applyClass`).

## 6. Rebuilding it in another mod and keeping it compatible

Two mods cannot register the same attachment id, so "same data" needs an agreed *shared store*. Recommended contract
(needs a small change in SporeAdds, **not done yet**):

1. Store the record under a fixed key in the player's persistent data, `player.getPersistentData()` -> compound `class_system`
   with exactly the keys of section 2 (`SporeRoleId`, `SporeSubclassId`, ...). Neither mod owns it; both read/write it.
2. Copy that compound in a `PlayerEvent.Clone` handler (persistent data is not copied automatically) and re-sync afterwards.
3. Each mod keeps its own client mirror (attachment or plain map) fed by its own sync packet, and both send their packet whenever
   either mod changes the class (so it is a good idea for the new mod to listen for the other's change through a vanilla signal,
   e.g. re-read on tick when the value differs from the last seen one).
4. Share the id list and the `spore`/`mercs` team names. If both mods register `/class`, Brigadier merges the literal, so only
   one set of children/executor should exist: the new mod should only register it when SporeAdds is absent
   (`ModList.get().isLoaded("sporeadd")`), otherwise it should just read the shared value.
5. Treat unknown identifiers defensively (a class added by one mod will exist in the shared store but not in the other).

Alternative if a hard dependency is acceptable: the new mod depends on SporeAdds and calls
`ClassAssignmentUtil.applyClass(player, id, subclass)` / `SporeClassUtil.hasClass(...)`. That is far less work but not "from scratch".

## 7. Minimal checklist for the new mod

- [ ] Shared data record (section 2) + Clone copy + login/respawn/dimension sync
- [ ] Sync packet + client mirror
- [ ] `/class` command (perm 2, suggestions, `animated` optional)
- [ ] `applyClass` pipeline (section 3), minus the SporeAdds-only items/effects
- [ ] Team assignment by class, class population SavedData (only if you want counters)
- [ ] Translation keys and a `hasClass` helper
