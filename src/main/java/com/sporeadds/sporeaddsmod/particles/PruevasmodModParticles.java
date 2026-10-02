/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package com.sporeadds.sporeaddsmod.particles;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.api.distmarker.Dist;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class PruevasmodModParticles {
	@SubscribeEvent
	public static void registerParticles(RegisterParticleProvidersEvent event) {
		event.registerSpriteSet(PruevasmodModParticleTypes.HORIZONTAL.get(), HorizontalparticleParticle::provider);
        event.registerSpriteSet(PruevasmodModParticleTypes.VERTICAL.get(), VerticalparticleParticle::provider);
	}

}