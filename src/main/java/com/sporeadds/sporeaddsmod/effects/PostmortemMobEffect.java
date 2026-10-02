package com.sporeadds.sporeaddsmod.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.common.ForgeMod;

import java.util.UUID;

public class PostmortemMobEffect extends MobEffect {

    private static final UUID SPEED_MODIFIER_UUID = UUID.fromString("d3f1a2b4-5c6d-4e7f-8a9b-0c1d2e3f4a5b");
    private static final double SPEED_REDUCTION = -0.60D;

    private static final UUID ATTACK_DAMAGE_MODIFIER_UUID = UUID.fromString("e4a2b3c5-6d7e-4f8a-9b0c-1d2e3f4a5b6c");
    private static final double ATTACK_DAMAGE_REDUCTION = -5.0D;

    private static final UUID STEP_HEIGHT_MODIFIER_UUID = UUID.fromString("f5b3c4d6-7e8f-4a9b-0c1d-2e3f4a5b6c7d");
    private static final double STEP_HEIGHT_ADDITION = 1.0D;

    public PostmortemMobEffect() {
        super(MobEffectCategory.HARMFUL, 0x555555);
        this.addAttributeModifier(
                Attributes.MOVEMENT_SPEED,
                SPEED_MODIFIER_UUID.toString(),
                SPEED_REDUCTION,
                AttributeModifier.Operation.MULTIPLY_TOTAL
        );
        this.addAttributeModifier(
                Attributes.ATTACK_DAMAGE,
                ATTACK_DAMAGE_MODIFIER_UUID.toString(),
                ATTACK_DAMAGE_REDUCTION,
                AttributeModifier.Operation.ADDITION
        );
        this.addAttributeModifier(
                ForgeMod.STEP_HEIGHT_ADDITION.get(),
                STEP_HEIGHT_MODIFIER_UUID.toString(),
                STEP_HEIGHT_ADDITION,
                AttributeModifier.Operation.ADDITION
        );
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return false;
    }
}