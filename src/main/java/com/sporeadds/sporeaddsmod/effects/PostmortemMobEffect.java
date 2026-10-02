package com.sporeadds.sporeaddsmod.effects;

import net.minecraft.resources.ResourceLocation;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.UUID;

public class PostmortemMobEffect extends MobEffect {

    private static final ResourceLocation SPEED_MODIFIER_UUID = ResourceLocation.fromNamespaceAndPath("sporeadd", "postmortemmobeffect_speed_modifier_uuid");
    private static final double SPEED_REDUCTION = -0.60D;

    private static final ResourceLocation ATTACK_DAMAGE_MODIFIER_UUID = ResourceLocation.fromNamespaceAndPath("sporeadd", "postmortemmobeffect_attack_damage_modifier_uuid");
    private static final double ATTACK_DAMAGE_REDUCTION = -5.0D;

    private static final ResourceLocation STEP_HEIGHT_MODIFIER_UUID = ResourceLocation.fromNamespaceAndPath("sporeadd", "postmortemmobeffect_step_height_modifier_uuid");
    private static final double STEP_HEIGHT_ADDITION = 1.0D;

    public PostmortemMobEffect() {
        super(MobEffectCategory.HARMFUL, 0x555555);
        this.addAttributeModifier(
                Attributes.MOVEMENT_SPEED,
                SPEED_MODIFIER_UUID,
                SPEED_REDUCTION,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );
        this.addAttributeModifier(
                Attributes.ATTACK_DAMAGE,
                ATTACK_DAMAGE_MODIFIER_UUID,
                ATTACK_DAMAGE_REDUCTION,
                AttributeModifier.Operation.ADD_VALUE
        );
        this.addAttributeModifier(
                net.minecraft.world.entity.ai.attributes.Attributes.STEP_HEIGHT,
                STEP_HEIGHT_MODIFIER_UUID,
                STEP_HEIGHT_ADDITION,
                AttributeModifier.Operation.ADD_VALUE
        );
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return false;
    }
}