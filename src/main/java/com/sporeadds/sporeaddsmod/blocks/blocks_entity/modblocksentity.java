package com.sporeadds.sporeaddsmod.blocks.blocks_entity;

import net.minecraft.core.registries.BuiltInRegistries;

import com.sporeadds.sporeaddsmod.blocks.modblocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class modblocksentity {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, "sporeadd");

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MedicBlockEntity>> MEDIC_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("medic_block_entity", () ->
                    BlockEntityType.Builder.of(MedicBlockEntity::new,
                            modblocks.MEDIC_BLOCK.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MedicBlockCrafterEntity>> MEDIC_BLOCK_CONSTRUCTOR_ENTITY =
            BLOCK_ENTITIES.register("medic_block_constructor_entity", () ->
                    BlockEntityType.Builder.of(MedicBlockCrafterEntity::new,
                            modblocks.MEDIC_CONTRUCTOR_BLOCK.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ScientistBlockEntity>> SCIENTIST_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("scientist_block_entity", () ->
                    BlockEntityType.Builder.of(ScientistBlockEntity::new,
                            modblocks.SCIENTIST_BLOCK.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FreezerBlockEntity>> FREEZER =
            BLOCK_ENTITIES.register("freezer_block_entity", () ->
                    BlockEntityType.Builder.of(FreezerBlockEntity::new,
                            modblocks.FREEZER_BLOCK.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<cryoblocke>> CRYO =
            BLOCK_ENTITIES.register("cryo_block_entity", () ->
                    BlockEntityType.Builder.of(cryoblocke::new,
                            modblocks.CRYO_BLOCK.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MoundTerrariumBlockEntity>> MOUND_TERRARIUM_ENTITY =
            BLOCK_ENTITIES.register("mound_terrarium_entity", () ->
                    BlockEntityType.Builder.of(MoundTerrariumBlockEntity::new,
                            modblocks.MOUND_TERRARIUM.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RaidControlerBlockEntity>> RAID_CONTROLER =
            BLOCK_ENTITIES.register("raid_controler",
                    () -> BlockEntityType.Builder.of(RaidControlerBlockEntity::new,
                            modblocks.RAID_CONTROLER.get()
                    ).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}