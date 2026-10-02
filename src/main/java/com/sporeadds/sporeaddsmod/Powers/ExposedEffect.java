package com.sporeadds.sporeaddsmod.Powers;

import net.minecraft.core.Holder;

import net.minecraft.core.registries.BuiltInRegistries;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import org.joml.Vector3f;

// Importamos tu clase ModItems para poder acceder a MUTATION_ESSENCE
import com.sporeadds.sporeaddsmod.ModItems;

public class ExposedEffect extends MobEffect {

    private static final ResourceLocation MYCELIUM_EFFECT_ID = ResourceLocation.fromNamespaceAndPath("spore", "mycelium_ef");
    private static final ResourceLocation SOUND_REAGENT_ID = ResourceLocation.fromNamespaceAndPath("spore", "reagent");
    private static final String TRIGGERED_TAG = "exposed_triggered";

    public ExposedEffect() {
        super(MobEffectCategory.HARMFUL, 0xFF0000); // Color rojo y dañino
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.getHealth() <= entity.getMaxHealth() * 0.25f) {
            Level level = entity.level();
            if (!level.isClientSide()) {

                // Verifica si ya se disparó esta secuencia para evitar repetición
                if (!entity.getPersistentData().getBoolean(TRIGGERED_TAG)) {
                    // Marca que ya se disparó
                    entity.getPersistentData().putBoolean(TRIGGERED_TAG, true);

                    Holder<MobEffect> myceliumEffect = BuiltInRegistries.MOB_EFFECT.getHolder(MYCELIUM_EFFECT_ID).orElse(null);

                    // Paso 1: Aplica el efecto de mycelium siempre
                    if (myceliumEffect != null) {
                        entity.addEffect(new MobEffectInstance(myceliumEffect, 600, 0, false, true));
                    }

                    // Paso 2: Genera efectos visuales y de audio sin comprobaciones
                    SoundEvent sound = SoundEvent.createVariableRangeEvent(SOUND_REAGENT_ID);
                    level.playSound(
                            null,
                            entity.getX(),
                            entity.getY(),
                            entity.getZ(),
                            sound,
                            SoundSource.MASTER,
                            3.0f,
                            1.0f
                    );

                    if (level instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(
                                new DustParticleOptions(new Vector3f(1f, 0f, 0f), 1.0f),
                                entity.getX(), entity.getY() + 1, entity.getZ(),
                                100,
                                0.5, 1, 0.5,
                                0.05
                        );
                    }

                    // NUEVO PASO: 5% de probabilidad de dropear mutation_essence
                    // Utilizamos el generador de números aleatorios de la entidad (nextFloat da un número de 0.0 a 1.0)
                    if (entity.getRandom().nextFloat() < 0.05f) {
                        entity.spawnAtLocation(ModItems.MUTATION_ESSENCE.get());
                    }

                    // Paso 3: Mata a la entidad
                    entity.hurt(entity.damageSources().generic(), Float.MAX_VALUE);
                }
            }
        } else {
            // Si la salud sube por encima del 25%, reinicia la bandera para posible futuro trigger
            if (entity.getPersistentData().getBoolean(TRIGGERED_TAG)) {
                entity.getPersistentData().putBoolean(TRIGGERED_TAG, false);
            }
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }
}