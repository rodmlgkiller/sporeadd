package com.sporeadds.sporeaddsmod.Damage;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;

public class Damagetypes2 {

    public static final ResourceKey<DamageType> TERMINAL =
            ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("sporeadd", "terminal"));

    public static final ResourceKey<DamageType> DEHYDRATION =
            ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("sporeadd", "dehydration"));

    /** Ejecución por insubordinación: el 3er strike del evento "punishment" del Proto. */
    public static final ResourceKey<DamageType> PUNISHMENT =
            ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("sporeadd", "punishment"));

    public static DamageSource terminal(LivingEntity entity) {
        return new DamageSource(
                entity.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                        .getHolderOrThrow(TERMINAL)
        );
    }

    public static DamageSource dehydration(LivingEntity entity) {
        return new DamageSource(
                entity.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                        .getHolderOrThrow(DEHYDRATION)
        );
    }

    public static DamageSource punishment(LivingEntity entity) {
        return new DamageSource(
                entity.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                        .getHolderOrThrow(PUNISHMENT)
        );
    }
}