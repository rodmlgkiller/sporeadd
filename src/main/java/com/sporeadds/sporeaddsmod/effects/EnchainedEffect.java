package com.sporeadds.sporeaddsmod.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

public class EnchainedEffect extends MobEffect {

    public EnchainedEffect() {
        super(MobEffectCategory.HARMFUL, 0x5E6F7A);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.level().isClientSide) {
            entity.addEffect(new MobEffectInstance(
                    effects.CLIPPED_WINGS.get(),
                    2,
                    0,
                    false,
                    false,
                    true
            ));
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}