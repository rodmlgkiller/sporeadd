package com.sporeadds.sporeaddsmod.config;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

/**
 * Core Mound Locator, Improvised Locator and Proto Locator: cost items and cooldowns.
 *
 * <p>Defaults reproduce the behaviour that was hard-coded before these options existed. Read values with
 * {@code .get()} at USE time (never copy them into static finals: the config is not loaded yet while classes
 * initialise). Called from {@link SporeAddsConfig}'s static initialiser, before the spec is built.
 *
 * <p>Costs use the shared {@link ConfigItems} format ({@code modid:item;count}); an empty list means "free".
 * Cooldowns are in ticks (20 = 1 second), 0 = no cooldown.
 */
public final class LocatorConfig {
    /** The original Core Mound Locator cost (30 redstone blocks). */
    public static final List<String> DEFAULT_CORE_MOUND_COST = List.of("minecraft:redstone_block;30");
    /** The original Improvised Locator cost (10 redstone blocks). */
    public static final List<String> DEFAULT_IMPROVISED_COST = List.of("minecraft:redstone_block;10");
    /** The original Proto Locator cooldown (30 minutes). */
    public static final int DEFAULT_PROTO_COOLDOWN_TICKS = 30 * 60 * 20;
    /** The original time the Proto Locator keeps the Protos it finds glowing (5 minutes). */
    public static final int DEFAULT_PROTO_MARKED_DURATION_TICKS = 5 * 60 * 20;

    private static final int MAX_TICKS = 72_000_000;
    /** The Proto Locator's cooldown has always run on the wall clock, so ticks are converted to milliseconds. */
    private static final long MILLIS_PER_TICK = 50L;

    public static ModConfigSpec.ConfigValue<List<? extends String>> CORE_MOUND_COST;
    public static ModConfigSpec.BooleanValue CORE_MOUND_CONSUME_COST;
    public static ModConfigSpec.IntValue CORE_MOUND_COOLDOWN_TICKS;

    public static ModConfigSpec.ConfigValue<List<? extends String>> IMPROVISED_COST;
    public static ModConfigSpec.BooleanValue IMPROVISED_CONSUME_COST;
    public static ModConfigSpec.IntValue IMPROVISED_COOLDOWN_TICKS;

    public static ModConfigSpec.ConfigValue<List<? extends String>> PROTO_COST;
    public static ModConfigSpec.BooleanValue PROTO_CONSUME_COST;
    public static ModConfigSpec.IntValue PROTO_COOLDOWN_TICKS;
    public static ModConfigSpec.IntValue PROTO_MARKED_DURATION_TICKS;

    private LocatorConfig() {
    }

    static void define(ModConfigSpec.Builder builder) {
        builder.comment(
                "Cost and cooldown of the three Scientist locators.",
                "Every default reproduces the original behaviour of the mod."
        ).push("locators");

        builder.comment("Core Mound Locator: reports the coordinates of every core mound.").push("core_mound_locator");
        CORE_MOUND_COST = defineCost(builder, "Core Mound Locator", DEFAULT_CORE_MOUND_COST,
                "Default: 30 redstone blocks.");
        CORE_MOUND_CONSUME_COST = defineConsume(builder, "Core Mound Locator");
        CORE_MOUND_COOLDOWN_TICKS = builder
                .comment("Cooldown between two uses of the Core Mound Locator, in ticks (20 ticks = 1 second).",
                        "0 = no cooldown. Default: 0 (it never had one).",
                        "While it runs the item shows the vanilla cooldown overlay and cannot be used.",
                        "The cooldown is shared by every Core Mound Locator the player carries.")
                .defineInRange("cooldown_ticks", 0, 0, MAX_TICKS);
        builder.pop();

        builder.comment("Improvised Locator: points at the nearest Kommandant.").push("improvised_locator");
        IMPROVISED_COST = defineCost(builder, "Improvised Locator", DEFAULT_IMPROVISED_COST,
                "Default: 10 redstone blocks.");
        IMPROVISED_CONSUME_COST = defineConsume(builder, "Improvised Locator");
        IMPROVISED_COOLDOWN_TICKS = builder
                .comment("Cooldown between two uses of the Improvised Locator, in ticks (20 ticks = 1 second).",
                        "0 = no cooldown. Default: 0 (it never had one).",
                        "While it runs the item shows the vanilla cooldown overlay and cannot be used.",
                        "The cooldown is shared by every Improvised Locator the player carries.")
                .defineInRange("cooldown_ticks", 0, 0, MAX_TICKS);
        builder.pop();

        builder.comment("Proto Locator: finds and marks every Proto.").push("proto_locator");
        PROTO_COST = defineCost(builder, "Proto Locator", List.of(),
                "Default: empty (the Proto Locator only ever had a cooldown, no item cost).");
        PROTO_CONSUME_COST = defineConsume(builder, "Proto Locator");
        PROTO_COOLDOWN_TICKS = builder
                .comment("Cooldown between two uses of the same Proto Locator item, in ticks (20 ticks = 1 second).",
                        "0 = no cooldown. Default: 36000 (30 minutes).",
                        "It is measured in real time (1 tick = 50 ms) and stored inside the item, so every Proto Locator",
                        "has its own timer and it keeps running while the world is closed, exactly as before.")
                .defineInRange("cooldown_ticks", DEFAULT_PROTO_COOLDOWN_TICKS, 0, MAX_TICKS);
        PROTO_MARKED_DURATION_TICKS = builder
                .comment("How long the Protos found by the Proto Locator keep glowing, in ticks (20 ticks = 1 second).",
                        "Default: 6000 (5 minutes).")
                .defineInRange("marked_duration_ticks", DEFAULT_PROTO_MARKED_DURATION_TICKS, 1, MAX_TICKS);
        builder.pop();

        builder.pop();
    }

    private static ModConfigSpec.ConfigValue<List<? extends String>> defineCost(
            ModConfigSpec.Builder builder, String locator, List<String> defaults, String defaultText) {
        return builder
                .comment("Items the " + locator + " needs every time it is used. " + defaultText,
                        "Format: \"modid:item\" or \"modid:item;count\" (count defaults to 1), e.g. \"minecraft:diamond;2\".",
                        "The player must carry ALL the listed items (main inventory and hotbar; the offhand does not count).",
                        "An empty list ([]) makes the locator free to use.")
                .defineListAllowEmpty(List.of("cost"), () -> defaults, () -> "", ConfigItems::isValidEntry);
    }

    private static ModConfigSpec.BooleanValue defineConsume(ModConfigSpec.Builder builder, String locator) {
        return builder
                .comment("If true, the cost is taken from the player's inventory every time the " + locator + " is used.",
                        "If false, the player only has to carry the items (they act as a key and are not used up).",
                        "Default: true.")
                .define("consume_cost", true);
    }

    // ---------------------------------------------------------------------------------------------------------------
    // Accessors (also meant for tooltips / GUIs). Costs are parsed at call time so config edits apply live.
    // ---------------------------------------------------------------------------------------------------------------

    /** Items the Core Mound Locator needs (empty = free). Entries that do not resolve to an item are skipped. */
    public static List<ConfigItems.Entry> coreMoundCost() {
        return ConfigItems.parseAll(CORE_MOUND_COST.get());
    }

    /** Items the Improvised Locator needs (empty = free). */
    public static List<ConfigItems.Entry> improvisedCost() {
        return ConfigItems.parseAll(IMPROVISED_COST.get());
    }

    /** Items the Proto Locator needs (empty = free, the default). */
    public static List<ConfigItems.Entry> protoCost() {
        return ConfigItems.parseAll(PROTO_COST.get());
    }

    /** Core Mound Locator cooldown in ticks (0 = none). */
    public static int coreMoundCooldownTicks() {
        return CORE_MOUND_COOLDOWN_TICKS.get();
    }

    /** Improvised Locator cooldown in ticks (0 = none). */
    public static int improvisedCooldownTicks() {
        return IMPROVISED_COOLDOWN_TICKS.get();
    }

    /** Proto Locator cooldown in ticks (0 = none). */
    public static int protoCooldownTicks() {
        return PROTO_COOLDOWN_TICKS.get();
    }

    /** Proto Locator cooldown in real-time milliseconds, the unit its per-item timer is kept in. */
    public static long protoCooldownMillis() {
        return PROTO_COOLDOWN_TICKS.get() * MILLIS_PER_TICK;
    }

    /** Ticks the Protos found by the Proto Locator keep glowing. */
    public static int protoMarkedDurationTicks() {
        return PROTO_MARKED_DURATION_TICKS.get();
    }
}
