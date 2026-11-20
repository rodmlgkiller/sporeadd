package com.sporeadds.sporeaddsmod.effects;

import com.sporeadds.sporeaddsmod.config.GhostConfig;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.UUID;

public class AmbushedEffect extends MobEffect {

    private static final UUID SPEED_MODIFIER_ID = UUID.fromString("8f3c1a2e-4b6d-4e21-9a2f-1c7d5e9b3f10");

    // The speed / damage numbers are configurable (ghost.ambushed in the config); the damage multipliers are applied in DamageEventHandler.

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
                    GhostConfig.ambushedMovementSpeedModifier(),
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
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return false;
    }
}