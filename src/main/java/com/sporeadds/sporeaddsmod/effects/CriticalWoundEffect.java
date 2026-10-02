package com.sporeadds.sporeaddsmod.effects;

import net.minecraft.resources.ResourceLocation;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.UUID;

public class CriticalWoundEffect extends MobEffect {

    private static final ResourceLocation SPEED_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath("sporeadd", "criticalwoundeffect_speed_modifier_id");
    public static final double MOVEMENT_SPEED_REDUCTION = -0.30D;
    public static final float OUTGOING_DAMAGE_MULTIPLIER = 0.60F;

    public CriticalWoundEffect() {
        super(MobEffectCategory.HARMFUL, 0x8B0000);
    }

    @Override
    public void addAttributeModifiers(net.minecraft.world.entity.ai.attributes.AttributeMap attributeMap, int amplifier) {
        var speedAttribute = attributeMap.getInstance(Attributes.MOVEMENT_SPEED);
        if (speedAttribute != null && speedAttribute.getModifier(SPEED_MODIFIER_ID) == null) {
            speedAttribute.addTransientModifier(new AttributeModifier(
                    SPEED_MODIFIER_ID,
                    MOVEMENT_SPEED_REDUCTION,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            ));
        }
    }

    @Override
    public void removeAttributeModifiers(net.minecraft.world.entity.ai.attributes.AttributeMap attributeMap) {
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