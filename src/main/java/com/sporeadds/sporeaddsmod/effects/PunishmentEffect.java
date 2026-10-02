package com.sporeadds.sporeaddsmod.effects;

import net.minecraft.resources.ResourceLocation;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.UUID;

/**
 * "Punishment" (Castigo). Efecto de castigo cuya potencia escala con el nivel (amplifier + 1):
 *  - overlay oscuro pulsante en los bordes de la pantalla (cliente).
 *  - -2 de daño infligido por nivel.
 *  - -30% de velocidad de movimiento por nivel.
 *  - -30% de volumen de sonido por nivel (máx. 90%) (cliente).
 *
 * Independiente del nivel:
 *  - inercia horizontal del salto reducida a 0 (evento).
 *  - +1 de forge:step_height_addition (puede subir bloques sin saltar).
 *
 * Aquí se aplican los modificadores de atributo (velocidad, altura de paso); daño y salto van
 * por eventos y el overlay y el sonido son de cliente.
 */
public class PunishmentEffect extends MobEffect {

    private static final ResourceLocation SPEED_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath("sporeadd", "punishmenteffect_speed_modifier_id");
    private static final ResourceLocation STEP_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath("sporeadd", "punishmenteffect_step_modifier_id");

    /** -30% de velocidad por nivel, limitado para no dejar al jugador totalmente clavado. */
    public static final double MOVE_SPEED_PER_LEVEL = -0.30D;
    private static final double MOVE_SPEED_MAX_REDUCTION = -0.95D;

    /** +1 de altura de paso (independiente del nivel). */
    public static final double STEP_HEIGHT_BONUS = 1.0D;

    /** -2 de daño infligido por nivel. */
    public static final float OUTGOING_DAMAGE_PER_LEVEL = 2.0F;
    /** -30% de volumen por nivel, hasta un máximo del 90%. */
    public static final float SOUND_REDUCTION_PER_LEVEL = 0.30F;
    public static final float SOUND_REDUCTION_MAX = 0.90F;

    public PunishmentEffect() {
        super(MobEffectCategory.HARMFUL, 0x0A0A0A);
    }

    private static double speedReductionFor(int amplifier) {
        return Math.max(MOVE_SPEED_MAX_REDUCTION, MOVE_SPEED_PER_LEVEL * (amplifier + 1));
    }

    @Override
    public void addAttributeModifiers(net.minecraft.world.entity.ai.attributes.AttributeMap attributeMap, int amplifier) {
        AttributeInstance speed = attributeMap.getInstance(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            speed.removeModifier(SPEED_MODIFIER_ID);
            speed.addTransientModifier(new AttributeModifier(
                    SPEED_MODIFIER_ID,
                    speedReductionFor(amplifier),
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            ));
        }

        net.minecraft.core.Holder<Attribute> stepAttr = net.minecraft.world.entity.ai.attributes.Attributes.STEP_HEIGHT;
        AttributeInstance step = stepAttr == null ? null : attributeMap.getInstance(stepAttr);
        if (step != null) {
            step.removeModifier(STEP_MODIFIER_ID);
            step.addTransientModifier(new AttributeModifier(
                    STEP_MODIFIER_ID,
                    STEP_HEIGHT_BONUS,
                    AttributeModifier.Operation.ADD_VALUE
            ));
        }
    }

    @Override
    public void removeAttributeModifiers(net.minecraft.world.entity.ai.attributes.AttributeMap attributeMap) {
        AttributeInstance speed = attributeMap.getInstance(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            speed.removeModifier(SPEED_MODIFIER_ID);
        }

        net.minecraft.core.Holder<Attribute> stepAttr = net.minecraft.world.entity.ai.attributes.Attributes.STEP_HEIGHT;
        AttributeInstance step = stepAttr == null ? null : attributeMap.getInstance(stepAttr);
        if (step != null) {
            step.removeModifier(STEP_MODIFIER_ID);
        }
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return false;
    }
}
