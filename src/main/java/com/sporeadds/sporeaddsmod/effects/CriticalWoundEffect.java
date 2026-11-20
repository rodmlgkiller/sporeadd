package com.sporeadds.sporeaddsmod.effects;

import com.mojang.logging.LogUtils;
import com.sporeadds.sporeaddsmod.config.GhostConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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

    private static final UUID SPEED_MODIFIER_ID = UUID.fromString("3d9f2b6a-1c4e-4a8b-9f7d-2e6c8b4a5d10");

    /** One resolved entry of {@code ghost.critical_wound.attribute_modifiers}. */
    private record ExtraModifier(Attribute attribute, ResourceLocation key, double amount, AttributeModifier.Operation operation) {
    }

    public CriticalWoundEffect() {
        super(MobEffectCategory.HARMFUL, 0x8B0000);
    }

    @Override
    public void addAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        var speedAttribute = attributeMap.getInstance(Attributes.MOVEMENT_SPEED);
        if (speedAttribute != null && speedAttribute.getModifier(SPEED_MODIFIER_ID) == null) {
            speedAttribute.addTransientModifier(new AttributeModifier(
                    SPEED_MODIFIER_ID,
                    "Critical Wound speed penalty",
                    GhostConfig.criticalWoundMovementSpeedModifier(),
                    AttributeModifier.Operation.MULTIPLY_TOTAL
            ));
        }

        addExtraModifiers(attributeMap);
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        var speedAttribute = attributeMap.getInstance(Attributes.MOVEMENT_SPEED);
        if (speedAttribute != null) {
            speedAttribute.removeModifier(SPEED_MODIFIER_ID);
        }

        removeExtraModifiers(attributeMap);
    }

    private static UUID extraModifierId(ResourceLocation attributeKey) {
        return UUID.nameUUIDFromBytes(("sporeadd:critical_wound_attribute/" + attributeKey).getBytes(StandardCharsets.UTF_8));
    }

    /** Adds the configured extra attribute debuffs (parsed now, so config edits apply to the next wound). */
    private static void addExtraModifiers(AttributeMap attributeMap) {
        for (ExtraModifier extra : parseExtraModifiers()) {
            AttributeInstance instance = attributeMap.getInstance(extra.attribute());
            if (instance == null) {
                continue;
            }

            UUID id = extraModifierId(extra.key());
            instance.removeModifier(id);
            instance.addTransientModifier(new AttributeModifier(
                    id,
                    "Critical Wound custom debuff",
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
        for (Attribute attribute : ForgeRegistries.ATTRIBUTES.getValues()) {
            ResourceLocation key = ForgeRegistries.ATTRIBUTES.getKey(attribute);
            if (key == null) {
                continue;
            }

            AttributeInstance instance = attributeMap.getInstance(attribute);
            if (instance != null) {
                instance.removeModifier(extraModifierId(key));
            }
        }
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
        if (key == null || parts.length < 2 || !ForgeRegistries.ATTRIBUTES.containsKey(key)) {
            warnOnce(raw);
            return null;
        }

        try {
            double amount = Double.parseDouble(parts[1].trim());
            AttributeModifier.Operation operation = parts.length > 2
                    ? parseOperation(parts[2].trim())
                    : AttributeModifier.Operation.MULTIPLY_TOTAL;
            if (operation == null) {
                warnOnce(raw);
                return null;
            }
            return new ExtraModifier(ForgeRegistries.ATTRIBUTES.getValue(key), key, amount, operation);
        } catch (NumberFormatException e) {
            warnOnce(raw);
            return null;
        }
    }

    private static AttributeModifier.Operation parseOperation(String name) {
        return switch (name.toLowerCase(Locale.ROOT)) {
            case "add" -> AttributeModifier.Operation.ADDITION;
            case "multiply_base" -> AttributeModifier.Operation.MULTIPLY_BASE;
            case "multiply_total" -> AttributeModifier.Operation.MULTIPLY_TOTAL;
            default -> null;
        };
    }

    private static void warnOnce(String raw) {
        if (WARNED.add(raw)) {
            LOGGER.warn("[SporeAdds config] ignoring critical wound attribute entry '{}' (unknown attribute or bad format; expected modid:attribute;amount;add|multiply_base|multiply_total)", raw);
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return false;
    }
}