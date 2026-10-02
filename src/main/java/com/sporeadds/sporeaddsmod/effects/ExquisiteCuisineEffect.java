package com.sporeadds.sporeaddsmod.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "sporeadd", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ExquisiteCuisineEffect extends MobEffect {

    public ExquisiteCuisineEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xFF9F1C);

        this.addAttributeModifier(
                Attributes.ATTACK_DAMAGE,
                "d6b5f6c2-1f9a-4d2e-8b7c-6e3f5a1c9b42",
                0.10D,
                AttributeModifier.Operation.MULTIPLY_TOTAL
        );
    }

    @Override
    public void applyEffectTick(LivingEntity living, int amplifier) {
        if (living.level().isClientSide) return;

        living.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 10 * 20, 0, false, false, true));
        living.addEffect(new MobEffectInstance(MobEffects.SATURATION, 1, 0, false, false, true));
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % (9 * 20) == 0;
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity().hasEffect(effects.EXQUISITE_CUISINE.get())) {
            event.setAmount(event.getAmount() * 0.8F);
        }
    }
}