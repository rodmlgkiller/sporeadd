package com.sporeadds.sporeaddsmod;

import com.sporeadds.sporeaddsmod.util.ItemNbt;

import com.sporeadds.sporeaddsmod.blocks.modblocks;
import com.sporeadds.sporeaddsmod.items.MutagenicCompoundItem;
import com.sporeadds.sporeaddsmod.items.MutagenicCompoundVariant;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, "sporeadd");

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> SPOREADD_TAB = TABS.register("sporeadd_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.sporeadd_tab"))
                    .icon(() -> new ItemStack(ModItems.BUCKET_OF_REMAINS.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.TRAINING_BOOK.get());

                        output.accept(ModItems.BUCKET_OF_REMAINS.get());
                        output.accept(ModItems.IMPROVISED_LOCATOR.get());
                        output.accept(ModItems.CORE_MOUND_LOCATOR.get());
                        output.accept(ModItems.PROTO_LOCATOR.get());
                        output.accept(ModItems.INJECTOR.get());
                        output.accept(ModItems.SYRINGE.get());
                        output.accept(ModItems.VACCINE.get());
                        output.accept(ModItems.BIOMASS_CORE.get());
                        output.accept(ModItems.NANO_INJECTOR.get());
                        output.accept(ModItems.SCALPEL.get());

                        output.accept(ModItems.EYE_IMPLANT.get());
                        output.accept(ModItems.TORSO_IMPLANT.get());
                        output.accept(ModItems.RIGHTARM_IMPLANT.get());
                        output.accept(ModItems.LEFTARM_IMPLANT.get());
                        output.accept(ModItems.RIGHTLEG_IMPLANT.get());
                        output.accept(ModItems.LEFTLEG_IMPLANT.get());
                        output.accept(ModItems.SURGICAL_IMPLANTATOR.get());

                        output.accept(ModItems.COCOON_SPAWN_EGG.get());
                        output.accept(ModItems.OVERCHARGED_SCENT_SPAWN_EGG.get());
                        output.accept(ModItems.MEAT_ABOMINATION_SPAWN_EGG.get());

                        output.accept(ModItems.SMALL_HOOK.get());
                        output.accept(ModItems.REINFORCED_CHAIN.get());
                        output.accept(ModItems.REINFORCED_COMBAT_CHAINS.get());
                        output.accept(ModItems.SMALL_SPONGE.get());
                        output.accept(ModItems.IMPROVISED_AIR_FILTER.get());
                        output.accept(ModItems.PORTABLE_AIR_PURIFIER.get());

                        output.accept(modblocks.MEDIC_BLOCK.get().asItem());
                        output.accept(modblocks.MEDIC_CONTRUCTOR_BLOCK.get().asItem());
                        output.accept(ModItems.THROWABLE_BANDAGES.get());

                        output.accept(modblocks.SCIENTIST_BLOCK.get());
                        output.accept(ModItems.RESEARCH_50.get());
                        output.accept(ModItems.RESEARCH_100.get());
                        output.accept(ModItems.RESEARCH_150.get());
                        output.accept(modblocks.FREEZER_BLOCK.get());
                        output.accept(modblocks.CRYO_BLOCK.get());
                        output.accept(modblocks.RAID_CONTROLER.get());

                        output.accept(ModItems.POTENCY_BAIT_I.get());
                        output.accept(ModItems.POTENCY_BAIT_II.get());
                        output.accept(ModItems.POTENCY_BAIT_III.get());
                        output.accept(ModItems.BIOMASS_BAIT.get());

                        ItemStack emptyTerrarium = new ItemStack(modblocks.MOUND_TERRARIUM.get());
                        CompoundTag emptyTag = ItemNbt.getOrCreateTag(emptyTerrarium);
                        emptyTag.putBoolean("HasMound", false);
                        emptyTag.putBoolean("Linked", false);
                        output.accept(emptyTerrarium);

                        ItemStack fullTerrarium = new ItemStack(modblocks.MOUND_TERRARIUM.get());
                        CompoundTag fullTag = ItemNbt.getOrCreateTag(fullTerrarium);
                        fullTag.putBoolean("HasMound", true);
                        fullTag.putBoolean("Linked", false);
                        ItemNbt.setCustomModelData(fullTerrarium, 1);
                        output.accept(fullTerrarium);

                        output.accept(ModItems.MUTATED_UNDEAD.get());
                        output.accept(ModItems.WEAKENED_ENDER_EYE.get());
                        output.accept(ModItems.AIRBORNE_REFINATED_MYCELIUM.get());
                        output.accept(ModItems.CHEMIST_MIND.get());
                        output.accept(ModItems.HIVE_EYE.get());
                        output.accept(ModItems.DANGEROUS_MIND.get());
                        output.accept(ModItems.INTACT_WITHER_MARROW.get());
                        output.accept(ModItems.LOCATOR_ORGAN.get());
                        output.accept(ModItems.MUTATION_ESSENCE.get());
                        output.accept(ModItems.REINFORCED_ORGANOID_TISSUE.get());
                        output.accept(ModItems.INDIVIDUALIST_MIND.get());
                        output.accept(ModItems.MYCELIAL_NETWORK_CULTIVE.get());
                        output.accept(ModItems.AGGRESSIVE_MIXTURE.get());

                        ItemStack mutagenicOriginal = new ItemStack(ModItems.MUTAGENIC_COMPOUND.get());
                        MutagenicCompoundItem.setVariant(mutagenicOriginal, MutagenicCompoundVariant.ORIGINAL);
                        output.accept(mutagenicOriginal);

                        ItemStack mutagenicCaustic = new ItemStack(ModItems.MUTAGENIC_COMPOUND.get());
                        MutagenicCompoundItem.setVariant(mutagenicCaustic, MutagenicCompoundVariant.CAUSTIC);
                        output.accept(mutagenicCaustic);

                        ItemStack mutagenicAbyssal = new ItemStack(ModItems.MUTAGENIC_COMPOUND.get());
                        MutagenicCompoundItem.setVariant(mutagenicAbyssal, MutagenicCompoundVariant.ABYSSAL);
                        output.accept(mutagenicAbyssal);

                        ItemStack mutagenicGluttonous = new ItemStack(ModItems.MUTAGENIC_COMPOUND.get());
                        MutagenicCompoundItem.setVariant(mutagenicGluttonous, MutagenicCompoundVariant.GLUTTONOUS);
                        output.accept(mutagenicGluttonous);
                    })
                    .build());
}