package com.sporeadds.sporeaddsmod.blocks.blocks_entity;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

/** Exposes the block entities' inventories to hoppers and other item-handler consumers. */
@EventBusSubscriber(modid = "sporeadd", bus = EventBusSubscriber.Bus.MOD)
public final class BlockEntityCapabilities {

    private BlockEntityCapabilities() {
    }

    @SubscribeEvent
    public static void register(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, modblocksentity.CRYO.get(),
                (be, side) -> be.getItemHandler());
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, modblocksentity.MEDIC_BLOCK_ENTITY.get(),
                (be, side) -> be.getItemHandler());
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, modblocksentity.SCIENTIST_BLOCK_ENTITY.get(),
                (be, side) -> be.getItemHandler());
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, modblocksentity.MOUND_TERRARIUM_ENTITY.get(),
                (be, side) -> be.getItemHandler());
    }
}
