package com.sporeadds.sporeaddsmod.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class ExposedWeaknessEffect extends MobEffect {

    private static final int CYAN_COLOR = 0x00FFFF;

    public ExposedWeaknessEffect() {
        super(MobEffectCategory.HARMFUL, CYAN_COLOR);
    }
}