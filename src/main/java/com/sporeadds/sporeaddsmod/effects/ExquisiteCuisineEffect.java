package com.sporeadds.sporeaddsmod.effects;

import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import net.neoforged.fml.common.EventBusSubscriber;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

@EventBusSubscriber(modid = "sporeadd", bus = EventBusSubscriber.Bus.GAME)
public class ExquisiteCuisineEffect extends MobEffect {

    public ExquisiteCuisineEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xFF9F1C);

        this.addAttributeModifier(
                Attributes.ATTACK_DAMAGE, ResourceLocation.fromNamespaceAndPath("sporeadd", "exquisitecuisineeffect_mod1"),
                0.10D,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );
    }

    @Override
    public boolean applyEffectTick(LivingEntity living, int amplifier) {
        if (living.level().isClientSide) return true;

        living.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 10 * 20, 0, false, false, true));
        living.addEffect(new MobEffectInstance(MobEffects.SATURATION, 1, 0, false, false, true));
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % (9 * 20) == 0;
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingDamageEvent.Pre event) {
        if (event.getEntity().hasEffect(effects.EXQUISITE_CUISINE)) {
            event.setNewDamage(event.getNewDamage() * 0.8F);
        }
    }
}