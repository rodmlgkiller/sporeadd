package com.sporeadds.sporeaddsmod.effects;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.UUID;

public class AmbushedEffect extends MobEffect {

    private static final UUID SPEED_MODIFIER_ID = UUID.fromString("8f3c1a2e-4b6d-4e21-9a2f-1c7d5e9b3f10");
    public static final double MOVEMENT_SPEED_REDUCTION = -0.60D;
    public static final float OUTGOING_DAMAGE_MULTIPLIER = 0.50F;
    public static final float INCOMING_DAMAGE_MULTIPLIER = 2.0F;

    public AmbushedEffect() {
        super(MobEffectCategory.HARMFUL, 0x4A2E1A);
    }

    @Override
    public void addAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        var speedAttribute = attributeMap.getInstance(Attributes.MOVEMENT_SPEED);
        if (speedAttribute != null && speedAttribute.getModifier(SPEED_MODIFIER_ID) == null) {
            speedAttribute.addTransientModifier(new AttributeModifier(
                    SPEED_MODIFIER_ID,
                    "Ambushed speed penalty",
                    MOVEMENT_SPEED_REDUCTION,
                    AttributeModifier.Operation.MULTIPLY_TOTAL
            ));
        }
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        var speedAttribute = attributeMap.getInstance(Attributes.MOVEMENT_SPEED);
        if (speedAttribute != null) {
            speedAttribute.removeModifier(SPEED_MODIFIER_ID);
        }
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return false;
    }
}