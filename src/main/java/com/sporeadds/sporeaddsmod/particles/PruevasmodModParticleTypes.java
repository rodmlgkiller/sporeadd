package com.sporeadds.sporeaddsmod.particles;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;

import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.particles.ParticleType;


public class PruevasmodModParticleTypes {
	public static final DeferredRegister<ParticleType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, "sporeadd");
	public static final RegistryObject<SimpleParticleType> HORIZONTAL = REGISTRY.register("horizontal", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> VERTICAL = REGISTRY.register("vertical", () -> new SimpleParticleType(false));
}