package com.sporeadds.sporeaddsmod.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class DelayedDefibrillationMobEffect extends MobEffect {

    public DelayedDefibrillationMobEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x2E8BFF);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return false;
    }
}