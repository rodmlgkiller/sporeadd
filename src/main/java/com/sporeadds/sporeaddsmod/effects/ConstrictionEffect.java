package com.sporeadds.sporeaddsmod.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class ConstrictionEffect extends MobEffect {
    public ConstrictionEffect() {
        super(MobEffectCategory.HARMFUL, 0x1a1a24); // Color oscuro/abismal
    }

    // No usamos applyEffectTick estándar para el daño porque el oxígeno se controla mejor por evento
}