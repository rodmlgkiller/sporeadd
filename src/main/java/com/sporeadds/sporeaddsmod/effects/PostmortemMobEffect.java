package com.sporeadds.sporeaddsmod.effects;

import com.sporeadds.sporeaddsmod.config.MedicConfig;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.UUID;
import java.util.function.BiConsumer;

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

    /**
     * The three modifiers above are registered with the original amounts, but their real amounts are read from the
     * config each time the effect is applied (medic.defibrillate.postmortem.*). The registration itself can not read
     * the config (it is not loaded yet), so the amounts are applied here instead.
     */
    @Override
    public void addAttributeModifiers(AttributeMap attributeMap, int amplifier) {
        addConfiguredModifier(attributeMap, Attributes.MOVEMENT_SPEED, SPEED_MODIFIER_UUID,
                -MedicConfig.postmortemSpeedReduction() * (amplifier + 1),
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        addConfiguredModifier(attributeMap, Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE_MODIFIER_UUID,
                -MedicConfig.postmortemAttackDamageReduction() * (amplifier + 1),
                AttributeModifier.Operation.ADD_VALUE);
        addConfiguredModifier(attributeMap, Attributes.STEP_HEIGHT, STEP_HEIGHT_MODIFIER_UUID,
                MedicConfig.postmortemStepHeightBonus() * (amplifier + 1),
                AttributeModifier.Operation.ADD_VALUE);
    }

    @Override
    public void createModifiers(int amplifier, BiConsumer<Holder<Attribute>, AttributeModifier> output) {
        output.accept(Attributes.MOVEMENT_SPEED, new AttributeModifier(SPEED_MODIFIER_UUID,
                -MedicConfig.postmortemSpeedReduction() * (amplifier + 1),
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        output.accept(Attributes.ATTACK_DAMAGE, new AttributeModifier(ATTACK_DAMAGE_MODIFIER_UUID,
                -MedicConfig.postmortemAttackDamageReduction() * (amplifier + 1),
                AttributeModifier.Operation.ADD_VALUE));
        output.accept(Attributes.STEP_HEIGHT, new AttributeModifier(STEP_HEIGHT_MODIFIER_UUID,
                MedicConfig.postmortemStepHeightBonus() * (amplifier + 1),
                AttributeModifier.Operation.ADD_VALUE));
    }

    private static void addConfiguredModifier(AttributeMap attributeMap, Holder<Attribute> attribute,
                                              ResourceLocation id, double amount, AttributeModifier.Operation operation) {
        AttributeInstance instance = attributeMap.getInstance(attribute);
        if (instance != null) {
            instance.removeModifier(id);
            instance.addPermanentModifier(new AttributeModifier(id, amount, operation));
        }
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return false;
    }
}