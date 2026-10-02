package com.sporeadds.sporeaddsmod.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * "Call of the Hive".
 *
 * Este efecto no aplica ninguna lógica por sí solo: solo existe como marca visual
 * (partículas de color terracota roja, dadas por el color del efecto) y como
 * requisito para que el modo "downed but not out" de la colmena se dispare cuando
 * el jugador recibe daño mortal.
 *
 * Toda la lógica vive en:
 *  - {@link com.sporeadds.sporeaddsmod.hive.CallOfTheHiveHandler} (aplicación / mantenimiento por cercanía a un proto)
 *  - {@link com.sporeadds.sporeaddsmod.hive.HiveDownedManager} (intercepción del daño mortal y secuencia)
 */
public class CallOfTheHiveEffect extends MobEffect {

    /** Terracota roja. */
    private static final int PARTICLE_COLOR = 0x9C4A32;

    public CallOfTheHiveEffect() {
        super(MobEffectCategory.HARMFUL, PARTICLE_COLOR);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return false;
    }
}
