/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package com.sporeadds.sporeaddsmod.client.model;

import com.sporeadds.sporeaddsmod.entity.StaticEntity;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.api.distmarker.Dist;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = {Dist.CLIENT})
public class ModModels {
	@SubscribeEvent
	public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
		event.registerLayerDefinition(ModelMedic_block.LAYER_LOCATION, ModelMedic_block::createBodyLayer);
		event.registerLayerDefinition(Modelscientist_block.LAYER_LOCATION, Modelscientist_block::createBodyLayer);
		event.registerLayerDefinition(Cocon.LAYER_LOCATION, Cocon::createBodyLayer);

	}
}