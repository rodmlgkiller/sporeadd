/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package com.sporeadds.sporeaddsmod.particles;

import net.neoforged.fml.common.EventBusSubscriber;

import net.neoforged.fml.common.Mod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.api.distmarker.Dist;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class PruevasmodModParticles {
	@SubscribeEvent
	public static void registerParticles(RegisterParticleProvidersEvent event) {
		event.registerSpriteSet(PruevasmodModParticleTypes.HORIZONTAL.get(), HorizontalparticleParticle::provider);
        event.registerSpriteSet(PruevasmodModParticleTypes.VERTICAL.get(), VerticalparticleParticle::provider);
	}

}