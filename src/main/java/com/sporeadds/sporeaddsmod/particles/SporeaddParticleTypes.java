package com.sporeadds.sporeaddsmod.particles;

import net.minecraft.core.registries.BuiltInRegistries;

import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.particles.ParticleType;

public class SporeaddParticleTypes {
    public static final DeferredRegister<ParticleType<?>> REGISTRY =
            DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, "sporeadd");

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> HORIZONTAL =
            REGISTRY.register("horizontal", () -> new SimpleParticleType(false));

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> VERTICAL =
            REGISTRY.register("vertical", () -> new SimpleParticleType(false));

    public static final DeferredHolder<ParticleType<?>, ParticleType<GoreParticleData>> GORE =
            REGISTRY.register("gore", () -> new GoreParticleType(false));

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> ELECTRICITY =
            REGISTRY.register("electricity", () -> new SimpleParticleType(false));

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> GAS =
            REGISTRY.register("gas", () -> new SimpleParticleType(false));

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> GAS_SMALL =
            REGISTRY.register("gas_small", () -> new SimpleParticleType(false));

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> RAIN =
            REGISTRY.register("rain", () -> new SimpleParticleType(false));

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> RAIN_SPLASH =
            REGISTRY.register("rain_splash", () -> new SimpleParticleType(false));
}