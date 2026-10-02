package com.sporeadds.sporeaddsmod.particles;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;

import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.particles.ParticleType;

public class SporeaddParticleTypes {
    public static final DeferredRegister<ParticleType<?>> REGISTRY =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, "sporeadd");

    public static final RegistryObject<SimpleParticleType> HORIZONTAL =
            REGISTRY.register("horizontal", () -> new SimpleParticleType(false));

    public static final RegistryObject<SimpleParticleType> VERTICAL =
            REGISTRY.register("vertical", () -> new SimpleParticleType(false));

    public static final RegistryObject<ParticleType<GoreParticleData>> GORE =
            REGISTRY.register("gore", () -> new GoreParticleType(false));

    public static final RegistryObject<SimpleParticleType> ELECTRICITY =
            REGISTRY.register("electricity", () -> new SimpleParticleType(false));

    public static final RegistryObject<SimpleParticleType> GAS =
            REGISTRY.register("gas", () -> new SimpleParticleType(false));

    public static final RegistryObject<SimpleParticleType> GAS_SMALL =
            REGISTRY.register("gas_small", () -> new SimpleParticleType(false));

    public static final RegistryObject<SimpleParticleType> RAIN =
            REGISTRY.register("rain", () -> new SimpleParticleType(false));

    public static final RegistryObject<SimpleParticleType> RAIN_SPLASH =
            REGISTRY.register("rain_splash", () -> new SimpleParticleType(false));
}