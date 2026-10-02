/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package com.sporeadds.sporeaddsmod.client.model;

import net.neoforged.fml.common.EventBusSubscriber;

import com.sporeadds.sporeaddsmod.entity.StaticEntity;
import net.neoforged.fml.common.Mod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.api.distmarker.Dist;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, value = {Dist.CLIENT})
public class ModModels {
	@SubscribeEvent
	public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
		event.registerLayerDefinition(ModelMedic_block.LAYER_LOCATION, ModelMedic_block::createBodyLayer);
		event.registerLayerDefinition(Modelscientist_block.LAYER_LOCATION, Modelscientist_block::createBodyLayer);
		event.registerLayerDefinition(Cocon.LAYER_LOCATION, Cocon::createBodyLayer);

	}
}