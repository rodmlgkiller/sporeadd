package com.sporeadds.sporeaddsmod.effects;

import net.minecraft.core.Holder;

import net.minecraft.core.registries.BuiltInRegistries;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.UUID;

/**
 * "Devotion". Recompensa que el Proto concede a los Kommandant tras matar (o ser responsables
 * de la muerte de) un jugador no-Kommandant. Escala con el nivel (amplifier + 1):
 *  - +10% de daño de ataque por nivel.
 *  - +10% de reducción del daño recibido por nivel (la parte "resistencia" del efecto).
 * Independiente del nivel:
 *  - mantiene al jugador con spore:marker amplifier 3 mientras el efecto siga activo (mismo
 *    patrón que {@link SeasonedEffect}: se reaplica cada segundo con una duración corta, así
 *    desaparece solo en cuanto Devotion termina).
 *
 * La reducción de daño recibido va por evento ({@link DevotionEventHandler}); aquí solo el
 * daño de ataque (atributo) y el marker (tick).
 */
public class DevotionEffect extends MobEffect {

    private static final ResourceLocation DAMAGE_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath("sporeadd", "devotioneffect_".lc("DAMAGE_MODIFIER_ID"));
    private static final ResourceLocation MARKER_ID = ResourceLocation.fromNamespaceAndPath("spore", "marker");
    private static final int MARKER_AMPLIFIER = 3;

    /** +10% de daño de ataque por nivel. */
    public static final double ATTACK_DAMAGE_PER_LEVEL = 0.10D;
    /** +10% de reducción del daño recibido por nivel. */
    public static final float INCOMING_DAMAGE_REDUCTION_PER_LEVEL = 0.10F;

    public DevotionEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xC01A22);
    }

    @Override
    public void addAttributeModifiers(net.minecraft.world.entity.ai.attributes.AttributeMap attributeMap, int amplifier) {
        AttributeInstance damage = attributeMap.getInstance(Attributes.ATTACK_DAMAGE);
        if (damage != null) {
            damage.removeModifier(DAMAGE_MODIFIER_ID);
            damage.addTransientModifier(new AttributeModifier(
                    DAMAGE_MODIFIER_ID,
                    ATTACK_DAMAGE_PER_LEVEL * (amplifier + 1),
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            ));
        }
    }

    @Override
    public void removeAttributeModifiers(net.minecraft.world.entity.ai.attributes.AttributeMap attributeMap) {
        AttributeInstance damage = attributeMap.getInstance(Attributes.ATTACK_DAMAGE);
        if (damage != null) {
            damage.removeModifier(DAMAGE_MODIFIER_ID);
        }
    }

    @Override
    public boolean applyEffectTick(LivingEntity living, int amplifier) {
        if (living.level().isClientSide) return true;

        Holder<MobEffect> marker = BuiltInRegistries.MOB_EFFECT.getHolder(MARKER_ID).orElse(null);
        if (marker != null) {
            living.addEffect(new MobEffectInstance(marker, 100, MARKER_AMPLIFIER, false, false));
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % 20 == 0;
    }
}
