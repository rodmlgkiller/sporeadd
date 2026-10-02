# Porting notes: 1.20.1 (Forge) -> 1.21.1 (NeoForge)

The `main` branch is the Minecraft 1.20.1 / Forge version and is untouched by this port. This branch (`1.21.1`) lives in a
separate git worktree so both versions can be updated side by side.

## Status

* Compiles cleanly against NeoForge 21.1.252, Spore 2.2.0j (NeoForge 1.21.1 build) and Pehkui 3.8.3.
* Verified by running the dev **dedicated server** (mod loads, every registry, mixin and event subscriber resolves, world
  starts) and the dev **client** up to the main menu (all client mixins apply, resources reload).
* **Not played through yet.** Gameplay, GUIs, rendering and the abilities still need an in-game pass.

## Strategy

Most of the port was done with repeatable Perl scripts in [`tools/`](tools) (`port1.pl` ... `port14.pl`), applied to the
whole source tree, followed by hand fixes. A few compatibility shims keep call sites almost identical to the 1.20.1 code,
which makes it much easier to forward-port future changes between the two branches:

| 1.20.1 API | 1.21.1 replacement |
|------------|--------------------|
| `SimpleChannel` / `NetworkEvent.Context` / `PacketDistributor` / `NetworkHooks` | thin shims in `network/` on top of NeoForge payloads (`NetworkHandle.register`) |
| `DistExecutor` | `util/DistExecutor` shim; client halves of packets live in `client/ClientPacketHooks` |
| Forge capabilities (`getCapability`, `LazyOptional`) | `capabilities/Capability` + `LazyOptional` shims over **data attachments** (`Capability.of`) |
| item NBT (`getOrCreateTag`, `getTag`...) | `util/ItemNbt` over the `CUSTOM_DATA` component |
| `CustomModelData` tag | `custom_model_data` component (`ItemNbt.setCustomModelData`) |
| enchantments | `util/EnchantUtil` (enchantments are data-driven and need registry access) |
| `FriendlyByteBuf#writeItem/readItem/writeComponent` | `util/BufUtil` |

Useful dev helpers: `tools/cc.sh` (compile + error summary), `tools/errs.pl` (errors with source lines),
`tools/jp.sh` (javap against Minecraft/NeoForge/Spore), `tools/mixaudit.pl` (checks every mixin target/method against the real
classes), `tools/evcheck.pl` and `tools/subscribers.pl` (event subscriber sanity checks), `tools/smoke.sh client|server`.

## Behaviour that differs from the 1.20.1 version

* **Origins integration is a stub.** There is no Origins for NeoForge 1.21.1, so `OriginApiBridge` does nothing and the
  origin <-> class sync is inactive. The hide-class datapack code is kept but is inert.
* **Effects that reacted to their own removal** (`Termina`, `Camouflaged`, `Seasoned`, `Dehydration`) now implement
  `EffectRemovalEvents.RemovalAware` (1.21 removed the entity argument from `removeAttributeModifiers`). Dehydration applies the
  creative-flight speed exception from its tick instead of from `addAttributeModifiers`.
* **Mixins removed or replaced**
  * `BileLiquidMixin` -> `SporeTeamBileHandler` (Spore 1.21.1 handles bile in its tick event, no longer in `BileLiquid#move`).
  * `AbyssalAquaAffinityMixin` removed (Aqua Affinity is an attribute now; `AbyssalMiningSpeedHandler` already covers it).
  * `InfectedWeakPointLootMixin` removed (Spore moved `dropCustomDeathLoot` up to `UtilityEntity`; `OrganoidWeakPointLootMixin` covers it).
  * `ForgeEventsMixin` removed (it was an empty debug hook on a class Spore no longer has).
  * SRG (`m_xxxx_`) method names were replaced by Mojang names.
* **Enchanting-table rules** for the Portable Air Purifier and Reinforced Combat Chains are now expressed through the
  `minecraft:enchantable/durability` item tag, so the Purifier can also take Mending.
* Tentacle's "arthropod" / "breathes underwater" behaviour is now an entity-type tag (`data/minecraft/tags/entity_type`).
* `StaticEntity` lost its passenger riding offset override (removed in 1.21; needs a visual check).
* `Unbreakable` and gas mask attributes/lore/name use data components instead of NBT (`util/GasMaskFactory`).
* Data folders were renamed to the 1.21 singular names (`recipe`, `loot_table`, `tags/block`, `tags/item`) and recipes were
  converted (`neoforge:conditions`, `neoforge:components` ingredients, `id`/`components` results).
* `medic_block` had an empty loot table file (it dropped nothing and logged a parse error). It now drops itself.
* Four unused duplicate textures with uppercase file names were deleted (1.21 rejects uppercase resource paths).

## Things worth testing in game first

Class selection and abilities, Training Book, Kommandant armor/HP and the caustic/abyssal/gluttonous powers, Medic injector and
defibrillation, Scientist scalpel/research GUI, implants GUI, Compounds GUI, block entities (medic/scientist/cryo/terrarium,
hopper interaction), JEI recipes, key bindings and HUD overlays, sync after death/respawn/dimension change, and multiplayer.
