package com.sporeadds.sporeaddsmod.particles;

import com.sporeadds.sporeaddsmod.client.particle.ElectricityParticle;
import com.sporeadds.sporeaddsmod.client.particle.GasParticle;
import com.sporeadds.sporeaddsmod.client.particle.GasSmallParticle;
import com.sporeadds.sporeaddsmod.client.particle.RainParticle;
import com.sporeadds.sporeaddsmod.client.particle.RainSplashParticle;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.api.distmarker.Dist;


@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class SporeaddParticles {

    @SubscribeEvent
    public static void registerParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(SporeaddParticleTypes.HORIZONTAL.get(), HorizontalparticleParticle::provider);
        event.registerSpriteSet(SporeaddParticleTypes.VERTICAL.get(), VerticalparticleParticle::provider);
        event.registerSpriteSet(SporeaddParticleTypes.GORE.get(), GoreParticle.Provider::new);
        event.registerSpriteSet(SporeaddParticleTypes.ELECTRICITY.get(), ElectricityParticle.Provider::new);
        event.registerSpriteSet(SporeaddParticleTypes.GAS.get(), GasParticle.Provider::new);
        event.registerSpriteSet(SporeaddParticleTypes.GAS_SMALL.get(), GasSmallParticle.Provider::new);
        event.registerSpriteSet(SporeaddParticleTypes.RAIN.get(), RainParticle.Provider::new);
        event.registerSpriteSet(SporeaddParticleTypes.RAIN_SPLASH.get(), RainSplashParticle.Provider::new);
    }
}