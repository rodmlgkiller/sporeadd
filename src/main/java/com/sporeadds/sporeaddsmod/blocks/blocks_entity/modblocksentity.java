package com.sporeadds.sporeaddsmod.blocks.blocks_entity;

import com.sporeadds.sporeaddsmod.blocks.modblocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class modblocksentity {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, "sporeadd");

    public static final RegistryObject<BlockEntityType<MedicBlockEntity>> MEDIC_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("medic_block_entity", () ->
                    BlockEntityType.Builder.of(MedicBlockEntity::new,
                            modblocks.MEDIC_BLOCK.get()).build(null));

    public static final RegistryObject<BlockEntityType<MedicBlockCrafterEntity>> MEDIC_BLOCK_CONSTRUCTOR_ENTITY =
            BLOCK_ENTITIES.register("medic_block_constructor_entity", () ->
                    BlockEntityType.Builder.of(MedicBlockCrafterEntity::new,
                            modblocks.MEDIC_CONTRUCTOR_BLOCK.get()).build(null));

    public static final RegistryObject<BlockEntityType<ScientistBlockEntity>> SCIENTIST_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("scientist_block_entity", () ->
                    BlockEntityType.Builder.of(ScientistBlockEntity::new,
                            modblocks.SCIENTIST_BLOCK.get()).build(null));

    public static final RegistryObject<BlockEntityType<FreezerBlockEntity>> FREEZER =
            BLOCK_ENTITIES.register("freezer_block_entity", () ->
                    BlockEntityType.Builder.of(FreezerBlockEntity::new,
                            modblocks.FREEZER_BLOCK.get()).build(null));

    public static final RegistryObject<BlockEntityType<cryoblocke>> CRYO =
            BLOCK_ENTITIES.register("cryo_block_entity", () ->
                    BlockEntityType.Builder.of(cryoblocke::new,
                            modblocks.CRYO_BLOCK.get()).build(null));

    public static final RegistryObject<BlockEntityType<MoundTerrariumBlockEntity>> MOUND_TERRARIUM_ENTITY =
            BLOCK_ENTITIES.register("mound_terrarium_entity", () ->
                    BlockEntityType.Builder.of(MoundTerrariumBlockEntity::new,
                            modblocks.MOUND_TERRARIUM.get()).build(null));
    public static final RegistryObject<BlockEntityType<RaidControlerBlockEntity>> RAID_CONTROLER =
            BLOCK_ENTITIES.register("raid_controler",
                    () -> BlockEntityType.Builder.of(RaidControlerBlockEntity::new,
                            modblocks.RAID_CONTROLER.get()
                    ).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}