package com.sporeadds.sporeaddsmod.config;

import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Surgical implants: the abilities and the attribute modifiers each implant grants.
 *
 * <p>The implants of this mod are NOT plain attribute modifiers: each one has a built-in ability implemented in
 * {@code ImplantBuffEvents} (jump boost, fall immunity, -25% damage taken, +20% damage dealt, a random on-hit effect, an
 * infected-scan). This class exposes the numbers of those abilities and, on top of them, lets every implant type grant
 * any number of attribute modifiers ({@code attribute_modifiers}, none by default), which are applied by
 * {@code ImplantAttributeSync}.
 *
 * <p>Defaults reproduce the behaviour that was hard-coded before these options existed. Read values with
 * {@code .get()} at USE time (never copy them into static finals: the config is not loaded yet while classes
 * initialise). Called from {@link SporeAddsConfig}'s static initialiser, before the spec is built.
 */
public final class ImplantConfig {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Set<String> WARNED = ConcurrentHashMap.newKeySet();

    /** Implant type ids, used as sub-section names: they match {@code ImplantType} lower-cased. */
    public static final List<String> TYPE_IDS = List.of("eye", "torso", "right_arm", "left_arm", "right_leg", "left_leg");

    public static final double DEFAULT_EYE_SCAN_RADIUS = 25.0D;
    public static final int DEFAULT_EYE_SCAN_INTERVAL_TICKS = 20;
    public static final int DEFAULT_EYE_GLOWING_TICKS = 40;
    public static final double DEFAULT_TORSO_DAMAGE_TAKEN_MULTIPLIER = 0.75D;
    public static final double DEFAULT_RIGHT_ARM_DAMAGE_DEALT_MULTIPLIER = 1.20D;
    public static final double DEFAULT_LEFT_ARM_EFFECT_CHANCE = 0.25D;
    public static final List<String> DEFAULT_LEFT_ARM_EFFECTS = List.of(
            "minecraft:poison;120;3",
            "minecraft:weakness;120;2",
            "minecraft:slowness;120;2",
            "minecraft:blindness;40;0"
    );
    public static final double DEFAULT_RIGHT_LEG_FALL_DAMAGE_MULTIPLIER = 0.0D;
    public static final int DEFAULT_LEFT_LEG_JUMP_BOOST_AMPLIFIER = 3;

    /** Syntax only (safe while the spec is built): {@code modid:attribute;amount[;OPERATION]}. */
    private static final Pattern MODIFIER_SYNTAX = Pattern.compile(
            "^\\s*([a-z0-9_.\\-]+:)?[a-z0-9_./\\-]+\\s*;\\s*[+-]?(\\d+(\\.\\d*)?|\\.\\d+)([eE][+-]?\\d+)?\\s*(;\\s*[A-Za-z_]+\\s*)?$");

    public static ForgeConfigSpec.BooleanValue EYE_ENABLED;
    public static ForgeConfigSpec.DoubleValue EYE_SCAN_RADIUS;
    public static ForgeConfigSpec.IntValue EYE_SCAN_INTERVAL_TICKS;
    public static ForgeConfigSpec.IntValue EYE_GLOWING_TICKS;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> EYE_MODIFIERS;

    public static ForgeConfigSpec.BooleanValue TORSO_ENABLED;
    public static ForgeConfigSpec.DoubleValue TORSO_DAMAGE_TAKEN_MULTIPLIER;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> TORSO_MODIFIERS;

    public static ForgeConfigSpec.BooleanValue RIGHT_ARM_ENABLED;
    public static ForgeConfigSpec.DoubleValue RIGHT_ARM_DAMAGE_DEALT_MULTIPLIER;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> RIGHT_ARM_MODIFIERS;

    public static ForgeConfigSpec.BooleanValue LEFT_ARM_ENABLED;
    public static ForgeConfigSpec.DoubleValue LEFT_ARM_EFFECT_CHANCE;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> LEFT_ARM_EFFECTS;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> LEFT_ARM_MODIFIERS;

    public static ForgeConfigSpec.BooleanValue RIGHT_LEG_ENABLED;
    public static ForgeConfigSpec.DoubleValue RIGHT_LEG_FALL_DAMAGE_MULTIPLIER;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> RIGHT_LEG_MODIFIERS;

    public static ForgeConfigSpec.BooleanValue LEFT_LEG_ENABLED;
    public static ForgeConfigSpec.IntValue LEFT_LEG_JUMP_BOOST_AMPLIFIER;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> LEFT_LEG_MODIFIERS;

    private ImplantConfig() {
    }

    /** One resolved {@code attribute;amount;operation} entry. */
    public record ModifierSpec(Attribute attribute, double amount, AttributeModifier.Operation operation) {
    }

    static void define(ForgeConfigSpec.Builder builder) {
        builder.comment(
                "Surgical implants. Every implant keeps its original built-in ability (numbers below, defaults = the original",
                "values) and can ALSO grant any number of attribute modifiers while it is installed ('attribute_modifiers',",
                "none by default). Set 'built_in_ability' to false to keep only the attribute modifiers.",
                "Attribute modifier format: \"modid:attribute;amount;OPERATION\" with OPERATION one of",
                "ADD (adds the amount), MULT_BASE (adds amount x base value: 0.5 = +50%) or MULT_TOTAL (multiplies the",
                "final value by 1 + amount: 0.5 = x1.5). OPERATION defaults to ADD."
        ).push("implant_stats");

        builder.comment("Eye implant: while crouching, infected (team 'spore') creatures around the player glow.").push("eye");
        EYE_ENABLED = defineEnabled(builder, "crouch scan that makes infected creatures glow");
        EYE_SCAN_RADIUS = builder
                .comment("Radius of the crouch scan, in blocks. Default: 25.")
                .defineInRange("scan_radius", DEFAULT_EYE_SCAN_RADIUS, 1.0D, 128.0D);
        EYE_SCAN_INTERVAL_TICKS = builder
                .comment("Ticks between two scans while crouching (20 ticks = 1 second). Default: 20.")
                .defineInRange("scan_interval_ticks", DEFAULT_EYE_SCAN_INTERVAL_TICKS, 1, 1200);
        EYE_GLOWING_TICKS = builder
                .comment("How long each scan makes the creatures glow, in ticks (20 ticks = 1 second). Default: 40.")
                .defineInRange("glowing_duration_ticks", DEFAULT_EYE_GLOWING_TICKS, 1, 12000);
        EYE_MODIFIERS = defineModifiers(builder, "Eye implant");
        builder.pop();

        builder.comment("Torso implant: reduces the damage the player takes.").push("torso");
        TORSO_ENABLED = defineEnabled(builder, "damage reduction");
        TORSO_DAMAGE_TAKEN_MULTIPLIER = builder
                .comment("Multiplier applied to every hit the player receives (applied once per hit, before armor).",
                        "0.75 = -25% damage taken, 1.0 = no change, 1.5 = +50% damage taken. Default: 0.75.")
                .defineInRange("damage_taken_multiplier", DEFAULT_TORSO_DAMAGE_TAKEN_MULTIPLIER, 0.0D, 10.0D);
        TORSO_MODIFIERS = defineModifiers(builder, "Torso implant");
        builder.pop();

        builder.comment("Right arm implant: increases the damage the player deals.").push("right_arm");
        RIGHT_ARM_ENABLED = defineEnabled(builder, "damage bonus");
        RIGHT_ARM_DAMAGE_DEALT_MULTIPLIER = builder
                .comment("Multiplier applied to every hit the player deals.",
                        "1.20 = +20% damage, 1.0 = no change, 2.0 = double damage. Default: 1.2.")
                .defineInRange("damage_dealt_multiplier", DEFAULT_RIGHT_ARM_DAMAGE_DEALT_MULTIPLIER, 0.0D, 10.0D);
        RIGHT_ARM_MODIFIERS = defineModifiers(builder, "Right arm implant");
        builder.pop();

        builder.comment("Left arm implant: a chance to inflict a random negative effect on whatever the player hits.").push("left_arm");
        LEFT_ARM_ENABLED = defineEnabled(builder, "random on-hit effect");
        LEFT_ARM_EFFECT_CHANCE = builder
                .comment("Chance (0.0 - 1.0) that a hit applies one of the effects below to the target. Default: 0.25 (25%).")
                .defineInRange("effect_chance", DEFAULT_LEFT_ARM_EFFECT_CHANCE, 0.0D, 1.0D);
        LEFT_ARM_EFFECTS = builder
                .comment("Effects the left arm implant can inflict; one of them is picked at random (all equally likely).",
                        "Format: \"modid:effect;durationTicks;amplifier\" (amplifier 0 = level I). An empty list means no effect.",
                        "Default: Poison IV and Weakness III and Slowness III for 6 seconds, Blindness for 2 seconds.")
                .defineListAllowEmpty(List.of("effects"), () -> DEFAULT_LEFT_ARM_EFFECTS, ConfigEffects::isValidEntry);
        LEFT_ARM_MODIFIERS = defineModifiers(builder, "Left arm implant");
        builder.pop();

        builder.comment("Right leg implant: fall damage protection.").push("right_leg");
        RIGHT_LEG_ENABLED = defineEnabled(builder, "fall damage protection");
        RIGHT_LEG_FALL_DAMAGE_MULTIPLIER = builder
                .comment("Multiplier applied to the fall damage the player takes.",
                        "0 = complete immunity (the original behaviour), 0.5 = half the damage, 1.0 = no protection. Default: 0.")
                .defineInRange("fall_damage_multiplier", DEFAULT_RIGHT_LEG_FALL_DAMAGE_MULTIPLIER, 0.0D, 10.0D);
        RIGHT_LEG_MODIFIERS = defineModifiers(builder, "Right leg implant");
        builder.pop();

        builder.comment("Left leg implant: permanent Jump Boost.").push("left_leg");
        LEFT_LEG_ENABLED = defineEnabled(builder, "jump boost");
        LEFT_LEG_JUMP_BOOST_AMPLIFIER = builder
                .comment("Amplifier of the Jump Boost effect kept on the player (0 = level I, 3 = level IV). Default: 3.")
                .defineInRange("jump_boost_amplifier", DEFAULT_LEFT_LEG_JUMP_BOOST_AMPLIFIER, 0, 255);
        LEFT_LEG_MODIFIERS = defineModifiers(builder, "Left leg implant");
        builder.pop();

        builder.pop();
    }

    private static ForgeConfigSpec.BooleanValue defineEnabled(ForgeConfigSpec.Builder builder, String ability) {
        return builder
                .comment("If true, the implant keeps its original built-in ability (" + ability + "), tuned by the values below.",
                        "If false, the built-in ability is switched off (the attribute modifiers below still apply).",
                        "Default: true.")
                .define("built_in_ability", true);
    }

    private static ForgeConfigSpec.ConfigValue<List<? extends String>> defineModifiers(ForgeConfigSpec.Builder builder, String implant) {
        return builder
                .comment("Extra attribute modifiers granted while the " + implant + " is installed, on top of its built-in ability.",
                        "Format: \"modid:attribute;amount;OPERATION\" with OPERATION = ADD, MULT_BASE or MULT_TOTAL (default ADD).",
                        "Examples: \"minecraft:generic.max_health;4.0;ADD\" (+2 hearts), \"minecraft:generic.movement_speed;0.1;MULT_BASE\" (+10% speed),",
                        "\"minecraft:generic.armor;2;ADD\".",
                        "The list is re-applied about once per second: edits, removed entries and removed implants take effect within a",
                        "second and no modifier is ever left behind. Default: none (the implants never had any).")
                .defineListAllowEmpty(List.of("attribute_modifiers"), ArrayList::new, ImplantConfig::isValidModifierEntry);
    }

    // ---------------------------------------------------------------------------------------------------------------
    // Attribute modifiers: parsing
    // ---------------------------------------------------------------------------------------------------------------

    /** Validator for {@code defineListAllowEmpty}: checks the syntax only, never the registry. */
    public static boolean isValidModifierEntry(Object o) {
        return o instanceof String s && MODIFIER_SYNTAX.matcher(s).matches();
    }

    public static Optional<ModifierSpec> parseModifier(String raw) {
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        String[] parts = raw.trim().split(";");
        ResourceLocation id = ResourceLocation.tryParse(parts[0].trim());
        if (parts.length < 2 || id == null || !ForgeRegistries.ATTRIBUTES.containsKey(id)) {
            warnOnce(raw);
            return Optional.empty();
        }
        double amount;
        try {
            amount = Double.parseDouble(parts[1].trim());
        } catch (NumberFormatException e) {
            warnOnce(raw);
            return Optional.empty();
        }
        if (!Double.isFinite(amount)) {
            warnOnce(raw);
            return Optional.empty();
        }
        AttributeModifier.Operation operation = AttributeModifier.Operation.ADDITION;
        if (parts.length > 2) {
            operation = parseOperation(parts[2].trim());
            if (operation == null) {
                warnOnce(raw);
                return Optional.empty();
            }
        }
        Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(id);
        if (attribute == null) {
            warnOnce(raw);
            return Optional.empty();
        }
        return Optional.of(new ModifierSpec(attribute, amount, operation));
    }

    private static AttributeModifier.Operation parseOperation(String text) {
        return switch (text.toUpperCase(Locale.ROOT)) {
            case "ADD", "ADDITION", "ADD_VALUE" -> AttributeModifier.Operation.ADDITION;
            case "MULT_BASE", "MULTIPLY_BASE", "ADD_MULTIPLIED_BASE" -> AttributeModifier.Operation.MULTIPLY_BASE;
            case "MULT_TOTAL", "MULTIPLY_TOTAL", "ADD_MULTIPLIED_TOTAL" -> AttributeModifier.Operation.MULTIPLY_TOTAL;
            default -> null;
        };
    }

    /**
     * The attribute modifiers configured for an implant type ({@link #TYPE_IDS}). Entries that do not resolve are
     * skipped (and logged once). Parsed at call time so config edits apply live.
     */
    public static List<ModifierSpec> attributeModifiers(String typeId) {
        List<ModifierSpec> out = new ArrayList<>();
        ForgeConfigSpec.ConfigValue<List<? extends String>> value = modifierValue(typeId);
        if (value != null) {
            for (String raw : value.get()) {
                parseModifier(raw).ifPresent(out::add);
            }
        }
        return out;
    }

    private static ForgeConfigSpec.ConfigValue<List<? extends String>> modifierValue(String typeId) {
        return switch (typeId) {
            case "eye" -> EYE_MODIFIERS;
            case "torso" -> TORSO_MODIFIERS;
            case "right_arm" -> RIGHT_ARM_MODIFIERS;
            case "left_arm" -> LEFT_ARM_MODIFIERS;
            case "right_leg" -> RIGHT_LEG_MODIFIERS;
            case "left_leg" -> LEFT_LEG_MODIFIERS;
            default -> null;
        };
    }

    // ---------------------------------------------------------------------------------------------------------------
    // Tooltip text (the translated implant descriptions still describe the ORIGINAL numbers)
    // ---------------------------------------------------------------------------------------------------------------

    /**
     * Tooltip lines describing what a server owner changed for an implant type: numbers that differ from the original
     * ones, a disabled built-in ability and the configured attribute modifiers. Empty while everything is original.
     */
    public static List<Component> describeChanges(String typeId) {
        List<Component> lines = new ArrayList<>();
        switch (typeId) {
            case "eye" -> {
                if (!EYE_ENABLED.get()) {
                    lines.add(note("Crouch scan disabled"));
                } else {
                    if (differs(EYE_SCAN_RADIUS.get(), DEFAULT_EYE_SCAN_RADIUS)) {
                        lines.add(note("Scan radius: " + number(EYE_SCAN_RADIUS.get()) + " blocks"));
                    }
                    if (EYE_SCAN_INTERVAL_TICKS.get() != DEFAULT_EYE_SCAN_INTERVAL_TICKS) {
                        lines.add(note("Scans every " + EYE_SCAN_INTERVAL_TICKS.get() + " ticks"));
                    }
                    if (EYE_GLOWING_TICKS.get() != DEFAULT_EYE_GLOWING_TICKS) {
                        lines.add(note("Glows targets for " + EYE_GLOWING_TICKS.get() + " ticks"));
                    }
                }
            }
            case "torso" -> {
                if (!TORSO_ENABLED.get()) {
                    lines.add(note("Damage reduction disabled"));
                } else if (differs(TORSO_DAMAGE_TAKEN_MULTIPLIER.get(), DEFAULT_TORSO_DAMAGE_TAKEN_MULTIPLIER)) {
                    lines.add(note("Damage taken: x" + number(TORSO_DAMAGE_TAKEN_MULTIPLIER.get())));
                }
            }
            case "right_arm" -> {
                if (!RIGHT_ARM_ENABLED.get()) {
                    lines.add(note("Damage bonus disabled"));
                } else if (differs(RIGHT_ARM_DAMAGE_DEALT_MULTIPLIER.get(), DEFAULT_RIGHT_ARM_DAMAGE_DEALT_MULTIPLIER)) {
                    lines.add(note("Damage dealt: x" + number(RIGHT_ARM_DAMAGE_DEALT_MULTIPLIER.get())));
                }
            }
            case "left_arm" -> {
                if (!LEFT_ARM_ENABLED.get()) {
                    lines.add(note("On-hit effects disabled"));
                } else {
                    if (differs(LEFT_ARM_EFFECT_CHANCE.get(), DEFAULT_LEFT_ARM_EFFECT_CHANCE)) {
                        lines.add(note("Effect chance: " + number(LEFT_ARM_EFFECT_CHANCE.get() * 100.0D) + "%"));
                    }
                    if (!DEFAULT_LEFT_ARM_EFFECTS.equals(LEFT_ARM_EFFECTS.get())) {
                        lines.add(note("On-hit effects: custom list"));
                    }
                }
            }
            case "right_leg" -> {
                if (!RIGHT_LEG_ENABLED.get()) {
                    lines.add(note("Fall damage protection disabled"));
                } else if (differs(RIGHT_LEG_FALL_DAMAGE_MULTIPLIER.get(), DEFAULT_RIGHT_LEG_FALL_DAMAGE_MULTIPLIER)) {
                    lines.add(note("Fall damage: x" + number(RIGHT_LEG_FALL_DAMAGE_MULTIPLIER.get())));
                }
            }
            case "left_leg" -> {
                if (!LEFT_LEG_ENABLED.get()) {
                    lines.add(note("Jump boost disabled"));
                } else if (LEFT_LEG_JUMP_BOOST_AMPLIFIER.get() != DEFAULT_LEFT_LEG_JUMP_BOOST_AMPLIFIER) {
                    lines.add(note("Jump Boost level " + (LEFT_LEG_JUMP_BOOST_AMPLIFIER.get() + 1)));
                }
            }
            default -> {
            }
        }
        for (ModifierSpec spec : attributeModifiers(typeId)) {
            lines.add(describeModifier(spec));
        }
        if (!lines.isEmpty()) {
            lines.add(0, Component.literal("Server-configured:").withStyle(ChatFormatting.GOLD));
        }
        return lines;
    }

    private static Component note(String text) {
        return Component.literal(text).withStyle(ChatFormatting.GRAY);
    }

    private static Component describeModifier(ModifierSpec spec) {
        double amount = spec.amount();
        String sign = amount >= 0.0D ? "+" : "";
        String text = switch (spec.operation()) {
            case ADDITION -> sign + number(amount) + " ";
            case MULTIPLY_BASE -> sign + number(amount * 100.0D) + "% ";
            default -> "x" + number(1.0D + amount) + " ";
        };
        return Component.literal(text)
                .append(Component.translatable(spec.attribute().getDescriptionId()))
                .withStyle(amount >= 0.0D ? ChatFormatting.GREEN : ChatFormatting.RED);
    }

    private static boolean differs(double value, double original) {
        return Math.abs(value - original) > 1.0E-9D;
    }

    private static String number(double value) {
        if (value == Math.rint(value) && Math.abs(value) < 1.0E9D) {
            return Long.toString((long) value);
        }
        String text = String.format(Locale.ROOT, "%.2f", value);
        if (text.contains(".")) {
            text = text.replaceAll("0+$", "").replaceAll("\\.$", "");
        }
        return text;
    }

    private static void warnOnce(String raw) {
        if (WARNED.add(raw)) {
            LOGGER.warn("[SporeAdds config] ignoring implant attribute modifier '{}' (unknown attribute or bad format; expected modid:attribute;amount;ADD|MULT_BASE|MULT_TOTAL)", raw);
        }
    }
}
