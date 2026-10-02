package com.sporeadds.sporeaddsmod.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class AnticipationEffect extends MobEffect {

    public AnticipationEffect() {
        super(MobEffectCategory.HARMFUL, 0xFF0000);
    }

    @Override
    public void fillEffectCures(java.util.Set<net.neoforged.neoforge.common.EffectCure> cures, net.minecraft.world.effect.MobEffectInstance effectInstance) {
        cures.remove(net.neoforged.neoforge.common.EffectCures.MILK);
        cures.remove(net.neoforged.neoforge.common.EffectCures.HONEY_BOTTLE);
    }
}