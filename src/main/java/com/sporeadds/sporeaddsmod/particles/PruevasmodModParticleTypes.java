package com.sporeadds.sporeaddsmod.particles;

import net.minecraft.core.registries.BuiltInRegistries;

import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.particles.ParticleType;


public class PruevasmodModParticleTypes {
	public static final DeferredRegister<ParticleType<?>> REGISTRY = DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, "sporeadd");
	public static final DeferredHolder<ParticleType<?>, SimpleParticleType> HORIZONTAL = REGISTRY.register("horizontal", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> VERTICAL = REGISTRY.register("vertical", () -> new SimpleParticleType(false));
}