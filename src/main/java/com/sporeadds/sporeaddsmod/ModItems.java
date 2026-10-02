package com.sporeadds.sporeaddsmod;

import net.neoforged.fml.common.EventBusSubscriber;

import net.minecraft.core.registries.BuiltInRegistries;

import com.sporeadds.sporeaddsmod.entity.ModEntities;
import com.sporeadds.sporeaddsmod.items.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.fml.common.Mod;
import com.sporeadds.sporeaddsmod.blocks.modblocks;


@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(BuiltInRegistries.ITEM, "sporeadd");

    public static final DeferredHolder<Item, Item> BUCKET_OF_REMAINS = ITEMS.register(
            "bucket_of_remains",
            () -> new BucketOfRemainsItem(new Item.Properties().stacksTo(1).craftRemainder(Items.BUCKET))
    );

    public static final DeferredHolder<Item, Item> IMPROVISED_LOCATOR = ITEMS.register(
            "improvised_locator",
            () -> new ImprovisedLocatorItem(new Item.Properties().stacksTo(1))
    );

    public static final DeferredHolder<Item, Item> CORE_MOUND_LOCATOR = ITEMS.register(
            "core_mound_locator",
            () -> new CoreMoundLocatorItem(new Item.Properties().stacksTo(1))
    );

    public static final DeferredHolder<Item, Item> PROTO_LOCATOR = ITEMS.register(
            "proto_locator",
            () -> new ProtoLocatorItem(new Item.Properties().stacksTo(1))
    );

    public static final DeferredHolder<Item, Item> INJECTOR = ITEMS.register(
            "injector", () -> new InjectorItem(new Item.Properties())
    );

    public static final DeferredHolder<Item, Item> SYRINGE = ITEMS.register(
            "syringe", () -> new SyringeItem(new Item.Properties().stacksTo(1))
    );

    public static final DeferredHolder<Item, Item> VACCINE = ITEMS.register(
            "vaccine", () -> new VaccineItem(new Item.Properties().stacksTo(1))
    );

    public static final DeferredHolder<Item, Item> BIOMASS_CORE = ITEMS.register(
            "biomass_core", () -> new BiomassCoreItem(new Item.Properties().stacksTo(1))
    );

    public static final DeferredHolder<Item, Item> NANO_INJECTOR = ITEMS.register(
            "nano_injector",
            () -> new NanoInjectorItem(new Item.Properties().stacksTo(1))
    );

    // ← NUEVOS IMPLANTS
    // Asegúrate de tener este import en la parte superior de tu archivo de registro:
// import com.sporeadds.sporeaddsmod.items.ImplantItem;

    public static final DeferredHolder<Item, Item> EYE_IMPLANT = ITEMS.register(
            "eye_implant",
            () -> new ImplantItem(new Item.Properties().stacksTo(1), "tooltip.sporeadds.implant.eye")
    );

    public static final DeferredHolder<Item, Item> TORSO_IMPLANT = ITEMS.register(
            "torso_implant",
            () -> new ImplantItem(new Item.Properties().stacksTo(1), "tooltip.sporeadds.implant.torso")
    );

    public static final DeferredHolder<Item, Item> RIGHTARM_IMPLANT = ITEMS.register(
            "rightarm_implant",
            () -> new ImplantItem(new Item.Properties().stacksTo(1), "tooltip.sporeadds.implant.right_arm")
    );

    public static final DeferredHolder<Item, Item> LEFTARM_IMPLANT = ITEMS.register(
            "leftarm_implant",
            () -> new ImplantItem(new Item.Properties().stacksTo(1), "tooltip.sporeadds.implant.left_arm")
    );

    public static final DeferredHolder<Item, Item> RIGHTLEG_IMPLANT = ITEMS.register(
            "rightleg_implant",
            () -> new ImplantItem(new Item.Properties().stacksTo(1), "tooltip.sporeadds.implant.right_leg")
    );

    public static final DeferredHolder<Item, Item> LEFTLEG_IMPLANT = ITEMS.register(
            "leftleg_implant",
            () -> new ImplantItem(new Item.Properties().stacksTo(1), "tooltip.sporeadds.implant.left_leg")
    );

    public static final DeferredHolder<Item, Item> SURGICAL_IMPLANTATOR = ITEMS.register(
            "surgical_implantator",
            () -> new SurgicalImplantatorItem(new Item.Properties().stacksTo(1))
    );
    public static final DeferredHolder<Item, Item> COCOON_SPAWN_EGG = ITEMS.register(
            "cocoon_spawn_egg",
            () -> new net.neoforged.neoforge.common.ForgeSpawnEggItem(
                    () -> ModEntities.COCOON.get(),
                    0x8B4513, // Color base (marrón/púrpura)
                    0x654321, // Color spots (más oscuro)
                    new Item.Properties()
            )
    );
    public static final DeferredHolder<Item, Item> RESEARCH_50 = ITEMS.register("research_50",
            () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON)));

    public static final DeferredHolder<Item, Item> RESEARCH_100 = ITEMS.register("research_100",
            () -> new Item(new Item.Properties().rarity(Rarity.RARE)));

    public static final DeferredHolder<Item, Item> RESEARCH_150 = ITEMS.register("research_150",
            () -> new Item(new Item.Properties().rarity(Rarity.EPIC)));
    public static final DeferredHolder<Item, Item> FREEZER_BLOCK = ITEMS.register(
            "freezer_block",
            () -> new FreezerBlockItem(modblocks.FREEZER_BLOCK.get(), new Item.Properties().stacksTo(64))
    );
    public static final DeferredHolder<Item, Item> MUTATED_UNDEAD = ITEMS.register(
            "mutated_undead",
            () -> new MutatedUndead(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON))
    );

    // ← NUEVOS OBJETOS (Basados en las imágenes)
    public static final DeferredHolder<Item, Item> WEAKENED_ENDER_EYE = ITEMS.register(
            "weakened_ender_eye",
            () -> new Item(new Item.Properties().stacksTo(16).rarity(Rarity.RARE))
    );

    public static final DeferredHolder<Item, Item> AIRBORNE_REFINATED_MYCELIUM = ITEMS.register(
            "airborne_refinated_mycelium",
            () -> new Item(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC))
    );

    public static final DeferredHolder<Item, Item> CHEMIST_MIND = ITEMS.register(
            "chemist_mind",
            () -> new Item(new Item.Properties().stacksTo(8).rarity(Rarity.RARE))
    );

    public static final DeferredHolder<Item, Item> HIVE_EYE = ITEMS.register(
            "hive_eye",
            () -> new Item(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC))
    );

    public static final DeferredHolder<Item, Item> INDIVIDUALIST_MIND = ITEMS.register(
            "individualist_mind",
            () -> new Item(new Item.Properties().stacksTo(8).rarity(Rarity.UNCOMMON))
    );

    public static final DeferredHolder<Item, Item> DANGEROUS_MIND = ITEMS.register(
            "dangerous_mind",
            () -> new Item(new Item.Properties().stacksTo(8).rarity(Rarity.EPIC))
    );

    public static final DeferredHolder<Item, Item> INTACT_WITHER_MARROW = ITEMS.register(
            "intact_wither_marrow",
            () -> new Item(new Item.Properties().stacksTo(16).rarity(Rarity.RARE))
    );

    public static final DeferredHolder<Item, Item> LOCATOR_ORGAN = ITEMS.register(
            "locator_organ",
            () -> new Item(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC))
    );

    public static final DeferredHolder<Item, Item> MUTATION_ESSENCE = ITEMS.register(
            "mutation_essence",
            () -> new Item(new Item.Properties().stacksTo(16).rarity(Rarity.EPIC))
    );

    public static final DeferredHolder<Item, Item> REINFORCED_ORGANOID_TISSUE = ITEMS.register(
            "reinforced_organoid_tissue",
            () -> new Item(new Item.Properties().stacksTo(8).rarity(Rarity.RARE))
    );

    public static final DeferredHolder<Item, Item> MYCELIAL_NETWORK_CULTIVE = ITEMS.register(
            "mycelial_network_cultive",
            () -> new Item(new Item.Properties().stacksTo(1).rarity(Rarity.RARE))
    );

    public static final DeferredHolder<Item, Item> AGGRESSIVE_MIXTURE = ITEMS.register(
            "aggressive_mixture",
            () -> new Item(new Item.Properties().stacksTo(8).rarity(Rarity.EPIC))
    );
    public static final DeferredHolder<Item, Item> MEAT_ABOMINATION_SPAWN_EGG = ITEMS.register(
            "meat_abomination_spawn_egg",
            () -> new MeatAbominationSpawnEggItem(
                    ModEntities.MEAT_ABOMINATION::get,
                    0x7A1E1E,
                    0xD14A4A,
                    new Item.Properties()
            )
    );
    public static final DeferredHolder<Item, Item> MUTAGENIC_COMPOUND = ITEMS.register(
            "mutagenic_compound",
            () -> new MutagenicCompoundItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC))
    );
    public static final DeferredHolder<Item, Item> REINFORCED_COMBAT_CHAINS = ITEMS.register(
            "reinforced_combat_chains",
            () -> new ReinforcedCombatChainsItem(new Item.Properties().stacksTo(1))
    );
    public static final DeferredHolder<Item, Item> SMALL_HOOK = ITEMS.register(
            "small_hook",
            () -> new Item(new Item.Properties().stacksTo(64))
    );

    public static final DeferredHolder<Item, Item> REINFORCED_CHAIN = ITEMS.register(
            "reinforced_chain",
            () -> new Item(new Item.Properties().stacksTo(64))
    );

    public static final DeferredHolder<Item, Item> PORTABLE_AIR_PURIFIER = ITEMS.register(
            "portable_air_purifier",
            () -> new PortableAirPurifierItem(new Item.Properties().rarity(Rarity.UNCOMMON))
    );

    public static final DeferredHolder<Item, Item> SMALL_SPONGE = ITEMS.register(
            "small_sponge",
            () -> new Item(new Item.Properties().stacksTo(64))
    );

    public static final DeferredHolder<Item, Item> IMPROVISED_AIR_FILTER = ITEMS.register(
            "improvised_air_filter",
            () -> new Item(new Item.Properties().stacksTo(64))
    );

    public static final DeferredHolder<Item, Item> OVERCHARGED_SCENT_SPAWN_EGG = ITEMS.register(
            "overcharged_scent_spawn_egg",
            () -> new OverchargedScentSpawnEggItem(new Item.Properties().stacksTo(64))
    );

    public static final DeferredHolder<Item, Item> POTENCY_BAIT_I = ITEMS.register(
            "potency_bait_i",
            () -> new Item(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON))
    );

    public static final DeferredHolder<Item, Item> POTENCY_BAIT_II = ITEMS.register(
            "potency_bait_ii",
            () -> new Item(new Item.Properties().stacksTo(1).rarity(Rarity.RARE))
    );

    public static final DeferredHolder<Item, Item> POTENCY_BAIT_III = ITEMS.register(
            "potency_bait_iii",
            () -> new Item(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC))
    );

    public static final DeferredHolder<Item, Item> BIOMASS_BAIT = ITEMS.register(
            "biomass_bait",
            () -> new Item(new Item.Properties().stacksTo(64).rarity(Rarity.UNCOMMON))
    );

    public static final DeferredHolder<Item, Item> DATA = ITEMS.register(
            "data",
            () -> new Item(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON))
    );

    public static final DeferredHolder<Item, Item> SCALPEL = ITEMS.register(
            "scalpel",
            () -> new ScalpelItem(new Item.Properties())
    );
    public static final DeferredHolder<Item, Item> THROWABLE_BANDAGES = ITEMS.register("throwable_bandages",
            () -> new ThrowableBandagesItem(new Item.Properties()));
    // Sigue aquí registrando más items...
    public static final DeferredHolder<Item, Item> TRAINING_BOOK = ITEMS.register("training_book",
            () -> new TrainingBookItem(new Item.Properties().stacksTo(1)));
}
