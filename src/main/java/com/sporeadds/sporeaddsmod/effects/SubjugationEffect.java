package com.sporeadds.sporeaddsmod.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.List;

public class SubjugationEffect extends MobEffect {
    public SubjugationEffect() {
        super(MobEffectCategory.HARMFUL, 0x4A2A6A);
    }
    @Override
    public void fillEffectCures(java.util.Set<net.neoforged.neoforge.common.EffectCure> cures, net.minecraft.world.effect.MobEffectInstance effectInstance) {
        cures.remove(net.neoforged.neoforge.common.EffectCures.MILK);
    }
}