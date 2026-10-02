/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package com.sporeadds.sporeaddsmod.client.model;

import net.neoforged.fml.common.EventBusSubscriber;

import com.sporeadds.sporeaddsmod.client.renderer.*;
import com.sporeadds.sporeaddsmod.entity.ModEntities;
import net.neoforged.fml.common.Mod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.api.distmarker.Dist;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModEntityRenderers {

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.COCOON.get(), SporeeggRenderer::new);
        event.registerEntityRenderer(ModEntities.TENTACLE_PROJECTILE.get(), TentacleProjectileRenderer::new);
        event.registerEntityRenderer(ModEntities.VARIANT_VOMIT.get(), VariantVomitRenderer::new);
        event.registerEntityRenderer(ModEntities.GAS_GLOB_PROJECTILE.get(), GasGlobProjectileRenderer::new);
        event.registerEntityRenderer(ModEntities.MEAT_ABOMINATION.get(), MeatAbominationRenderer::new);
        event.registerEntityRenderer(ModEntities.CHAIN_PROJECTILE.get(), ChainProjectileRenderer::new);
        event.registerEntityRenderer(ModEntities.THROWABLE_BANDAGES.get(), ThrowableBandagesRenderer::new);
        event.registerEntityRenderer(ModEntities.DECOY.get(), DecoyRenderer::new);
        event.registerEntityRenderer(ModEntities.DATA_DROP_ITEM.get(), DataDropItemRenderer::new);

    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(MeatAbominationModel.LAYER_LOCATION, MeatAbominationModel::createBodyLayer);
    }
}