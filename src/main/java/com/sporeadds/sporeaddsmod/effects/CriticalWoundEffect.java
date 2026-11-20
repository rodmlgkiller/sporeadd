package com.sporeadds.sporeaddsmod.effects;

import com.mojang.logging.LogUtils;
import com.sporeadds.sporeaddsmod.config.GhostConfig;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Critical Wound (put on the target of a disguised Ghost's backstab). Its numbers come from the config
 * ({@code ghost.critical_wound}); the damage multipliers are applied in {@code DamageEventHandler}.
 */
public class CriticalWoundEffect extends MobEffect {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Set<String> WARNED = ConcurrentHashMap.newKeySet();

    private static final ResourceLocation SPEED_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath("sporeadd", "criticalwoundeffect_speed_modifier_id");

    /** One resolved entry of {@code ghost.critical_wound.attribute_modifiers}. */
    private record ExtraModifier(Holder<Attribute> attribute, ResourceLocation modifierId, double amount, AttributeModifier.Operation operation) {
    }

    public CriticalWoundEffect() {
        super(MobEffectCategory.HARMFUL, 0x8B0000);
    }

    @Override
    public void addAttributeModifiers(net.minecraft.world.entity.ai.attributes.AttributeMap attributeMap, int amplifier) {
        var speedAttribute = attributeMap.getInstance(Attributes.MOVEMENT_SPEED);
        if (speedAttribute != null && speedAttribute.getModifier(SPEED_MODIFIER_ID) == null) {
            speedAttribute.addTransientModifier(new AttributeModifier(
                    SPEED_MODIFIER_ID,
                    GhostConfig.criticalWoundMovementSpeedModifier(),
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            ));
        }

        addExtraModifiers(attributeMap);
    }

    @Override
    public void removeAttributeModifiers(net.minecraft.world.entity.ai.attributes.AttributeMap attributeMap) {
        var speedAttribute = attributeMap.getInstance(Attributes.MOVEMENT_SPEED);
        if (speedAttribute != null) {
            speedAttribute.removeModifier(SPEED_MODIFIER_ID);
        }

        removeExtraModifiers(attributeMap);
    }

    private static ResourceLocation extraModifierId(ResourceLocation attributeKey) {
        return ResourceLocation.fromNamespaceAndPath("sporeadd",
                "critical_wound/" + attributeKey.getNamespace() + "/" + attributeKey.getPath());
    }

    /** Adds the configured extra attribute debuffs (parsed now, so config edits apply to the next wound). */
    private static void addExtraModifiers(AttributeMap attributeMap) {
        for (ExtraModifier extra : parseExtraModifiers()) {
            AttributeInstance instance = attributeMap.getInstance(extra.attribute());
            if (instance == null) {
                continue;
            }

            instance.addOrUpdateTransientModifier(new AttributeModifier(
                    extra.modifierId(),
                    extra.amount(),
                    extra.operation()
            ));
        }
    }

    /**
     * Removes every extra debuff this effect may have added. It goes through all registered attributes (instead of the
     * current config list) so the debuffs are cleaned up even if the config was edited while the wound was active.
     */
    private static void removeExtraModifiers(AttributeMap attributeMap) {
        BuiltInRegistries.ATTRIBUTE.holders().forEach(holder -> {
            AttributeInstance instance = attributeMap.getInstance(holder);
            if (instance != null) {
                instance.removeModifier(extraModifierId(holder.key().location()));
            }
        });
    }

    private static List<ExtraModifier> parseExtraModifiers() {
        List<ExtraModifier> out = new ArrayList<>();
        for (String raw : GhostConfig.CRITICAL_WOUND_ATTRIBUTE_MODIFIERS.get()) {
            ExtraModifier parsed = parseExtraModifier(raw);
            if (parsed != null) {
                out.add(parsed);
            }
        }
        return out;
    }

    private static ExtraModifier parseExtraModifier(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }

        String[] parts = raw.trim().split(";");
        ResourceLocation key = ResourceLocation.tryParse(parts[0].trim());
        Optional<Holder.Reference<Attribute>> attribute = key == null
                ? Optional.empty()
                : BuiltInRegistries.ATTRIBUTE.getHolder(key);
        if (attribute.isEmpty() || parts.length < 2) {
            warnOnce(raw);
            return null;
        }

        try {
            double amount = Double.parseDouble(parts[1].trim());
            AttributeModifier.Operation operation = parts.length > 2
                    ? parseOperation(parts[2].trim())
                    : AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL;
            if (operation == null) {
                warnOnce(raw);
                return null;
            }
            return new ExtraModifier(attribute.get(), extraModifierId(key), amount, operation);
        } catch (NumberFormatException e) {
            warnOnce(raw);
            return null;
        }
    }

    private static AttributeModifier.Operation parseOperation(String name) {
        return switch (name.toLowerCase(Locale.ROOT)) {
            case "add" -> AttributeModifier.Operation.ADD_VALUE;
            case "multiply_base" -> AttributeModifier.Operation.ADD_MULTIPLIED_BASE;
            case "multiply_total" -> AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL;
            default -> null;
        };
    }

    private static void warnOnce(String raw) {
        if (WARNED.add(raw)) {
            LOGGER.warn("[SporeAdds config] ignoring critical wound attribute entry '{}' (unknown attribute or bad format; expected modid:attribute;amount;add|multiply_base|multiply_total)", raw);
        }
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return false;
    }
}