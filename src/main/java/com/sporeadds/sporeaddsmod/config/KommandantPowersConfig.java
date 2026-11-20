package com.sporeadds.sporeaddsmod.config;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.ForgeConfigSpec;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Kommandant abilities 1-7 (evolution/cocoon, infection, mounds, ESP, harvest and feeding, second authority, flight) and general Kommandant rules.
 *
 * <p>Defaults reproduce the behaviour that was hard-coded before these options existed. Read values with
 * {@code .get()} at USE time (never copy them into static finals: the config is not loaded yet while classes
 * initialise). Called from {@link SporeAddsConfig}'s static initialiser, before the spec is built.
 *
 * <p>Ability numbers used in the section names: 1 Rampant Evolution (cocoon), 2 Contaminate and extract,
 * 3 Summon Core Mound, 4 Devour, 5 Biomass Manipulation, 6 Forced Mutation, 7 Second Authority (mount control).
 */
public final class KommandantPowersConfig {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Set<String> WARNED = ConcurrentHashMap.newKeySet();

    /** Syntax only (the registries are empty while the spec is built): {@code modid:thing}, the namespace is optional. */
    private static final Pattern ID_SYNTAX = Pattern.compile("^\\s*([a-zA-Z0-9_.\\-]+:)?[a-zA-Z0-9_./\\-]+\\s*$");
    private static final Pattern EXTRACTION_SYNTAX = Pattern.compile(
            "^\\s*([a-zA-Z0-9_.\\-]+:)?[a-zA-Z0-9_./\\-]+\\s*;\\s*([a-zA-Z0-9_.\\-]+:)?[a-zA-Z0-9_./\\-]+\\s*;\\s*\\d{1,3}(\\s*;\\s*\\d{1,3})?\\s*$");
    private static final Pattern KILL_OVERRIDE_SYNTAX = Pattern.compile(
            "^\\s*([a-zA-Z0-9_.\\-]+:)?[a-zA-Z0-9_./\\-]+\\s*;\\s*\\d+(\\.\\d+)?\\s*$");

    // ------------------------------------------------------------------ general
    public static ForgeConfigSpec.IntValue MAX_EVOLUTION_LEVEL;
    /** Index 1..13 = evolution level at which that ability unlocks (index 0 unused). */
    public static final ForgeConfigSpec.IntValue[] UNLOCK_LEVELS = new ForgeConfigSpec.IntValue[14];
    /** Index 0..9 = biomass bar maximum while the Kommandant has that evolution level. */
    public static final ForgeConfigSpec.IntValue[] BAR_MAX_BY_LEVEL = new ForgeConfigSpec.IntValue[10];

    // ------------------------------------------------------------------ biomass sources
    public static ForgeConfigSpec.IntValue KILL_BIOMASS;
    public static ForgeConfigSpec.DoubleValue KILL_LOW_HEALTH_THRESHOLD;
    public static ForgeConfigSpec.DoubleValue KILL_LOW_HEALTH_BIOMASS;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> KILL_OVERRIDES;
    public static ForgeConfigSpec.BooleanValue KILL_ONLY_LISTED;

    public static ForgeConfigSpec.BooleanValue FOOD_BIOMASS_ENABLED;
    public static ForgeConfigSpec.DoubleValue FOOD_BIOMASS_PER_HUNGER;
    public static ForgeConfigSpec.DoubleValue FOOD_BIOMASS_PER_SATURATION;

    public static ForgeConfigSpec.IntValue STARVATION_INTERVAL_TICKS;
    public static ForgeConfigSpec.IntValue STARVATION_BIOMASS_DRAIN;
    public static ForgeConfigSpec.DoubleValue STARVATION_DAMAGE;

    // ------------------------------------------------------------------ power 1: cocoon
    public static ForgeConfigSpec.DoubleValue COCOON_HP_PER_LEVEL;
    public static ForgeConfigSpec.DoubleValue COCOON_MAX_LEVEL_HP;
    public static ForgeConfigSpec.IntValue COCOON_BASE_SECONDS;
    public static ForgeConfigSpec.IntValue COCOON_STEP_SECONDS;
    public static ForgeConfigSpec.IntValue COCOON_LEVELS_PER_STEP;
    public static ForgeConfigSpec.IntValue COCOON_MAX_LEVEL_SECONDS;
    public static ForgeConfigSpec.IntValue COCOON_MAX_LEVEL_BIOMASS_COST;
    public static ForgeConfigSpec.IntValue COCOON_INTERRUPTED_SLOWNESS_TICKS;
    public static ForgeConfigSpec.IntValue COCOON_INTERRUPTED_SLOWNESS_AMPLIFIER;
    public static ForgeConfigSpec.IntValue COCOON_INTERRUPTED_WEAKNESS_TICKS;
    public static ForgeConfigSpec.IntValue COCOON_INTERRUPTED_WEAKNESS_AMPLIFIER;
    public static ForgeConfigSpec.IntValue COCOON_COMPLETED_RESISTANCE_TICKS;
    public static ForgeConfigSpec.IntValue COCOON_COMPLETED_RESISTANCE_AMPLIFIER;
    public static ForgeConfigSpec.IntValue EVOLUTION_REQUIRES_PROTO_FROM_LEVEL;
    public static ForgeConfigSpec.DoubleValue EVOLUTION_PROTO_RADIUS;

    // ------------------------------------------------------------------ power 2: extraction
    public static ForgeConfigSpec.IntValue MYCELIUM_SECONDS_PER_LEVEL;
    public static ForgeConfigSpec.IntValue MYCELIUM_MIN_SECONDS;
    public static ForgeConfigSpec.IntValue MYCELIUM_LEVELS_PER_AMPLIFIER;

    public static ForgeConfigSpec.EnumValue<ThresholdMode> EXTRACTION_THRESHOLD_MODE;
    public static ForgeConfigSpec.DoubleValue EXTRACTION_THRESHOLD_FLAT;
    public static ForgeConfigSpec.DoubleValue EXTRACTION_THRESHOLD_PERCENT;

    public static ForgeConfigSpec.IntValue CHANCE_PLAYER;
    public static ForgeConfigSpec.IntValue CHANCE_INFECTED_PLAYER_GENERATED;
    public static ForgeConfigSpec.IntValue CHANCE_INFECTED_PLAYER_NATURAL_DEDICATED;
    public static ForgeConfigSpec.IntValue CHANCE_INFECTED_PLAYER_NATURAL_SINGLEPLAYER;
    public static ForgeConfigSpec.IntValue CHANCE_VILLAGER;
    public static ForgeConfigSpec.IntValue CHANCE_ZOMBIE_VILLAGER;
    public static ForgeConfigSpec.IntValue CHANCE_WITHER_SKELETON;
    public static ForgeConfigSpec.IntValue CHANCE_WITCH;
    public static ForgeConfigSpec.IntValue CHANCE_ENDERMAN;
    public static ForgeConfigSpec.IntValue CHANCE_UNDEAD_BASE;
    public static ForgeConfigSpec.IntValue CHANCE_UNDEAD_PER_LEVEL;
    public static ForgeConfigSpec.IntValue UNDEAD_BIOMASS_COST;
    public static ForgeConfigSpec.IntValue WITHER_FAILURE_TICKS;
    public static ForgeConfigSpec.IntValue WITHER_FAILURE_AMPLIFIER;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> EXTRA_EXTRACTIONS;

    // ------------------------------------------------------------------ power 3: mounds
    public static ForgeConfigSpec.IntValue MOUND_BIOMASS_COST;
    public static ForgeConfigSpec.IntValue MAX_MOUNDS;
    public static ForgeConfigSpec.DoubleValue MOUND_PLACEMENT_RANGE;
    public static ForgeConfigSpec.BooleanValue MOUND_REFUSE_WHEN_FULL;
    public static ForgeConfigSpec.BooleanValue MOUND_REMOVE_DESTROYS;

    // ------------------------------------------------------------------ power 4: devour
    public static ForgeConfigSpec.IntValue DEVOUR_BASIC_MIN;
    public static ForgeConfigSpec.IntValue DEVOUR_BASIC_MAX;
    public static ForgeConfigSpec.IntValue DEVOUR_STRONG_MIN;
    public static ForgeConfigSpec.IntValue DEVOUR_STRONG_MAX;
    public static ForgeConfigSpec.IntValue DEVOUR_ANTIFARM_BIOMASS;
    public static ForgeConfigSpec.IntValue DEVOUR_MEAT_ABOMINATION_BIOMASS;
    public static ForgeConfigSpec.IntValue DEVOUR_REMAINS_BIOMASS;
    public static ForgeConfigSpec.IntValue DEVOUR_BULB_BIOMASS;
    public static ForgeConfigSpec.IntValue DEVOUR_BULB_CHANCE;
    public static ForgeConfigSpec.IntValue DEVOUR_COOLDOWN_TICKS;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> DEVOUR_BASIC_ENTITIES;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> DEVOUR_STRONG_ENTITIES;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> DEVOUR_REMAINS_BLOCKS;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> DEVOUR_BULB_BLOCKS;

    // ------------------------------------------------------------------ power 5: manipulation
    public static ForgeConfigSpec.IntValue FEED_BIOMASS_COST;
    public static ForgeConfigSpec.BooleanValue FEED_MOUNDS;
    public static ForgeConfigSpec.IntValue MANIPULATION_COOLDOWN_MS;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> HARVESTABLE_BLOCKS;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> FEEDABLE_BLOCKS;

    // ------------------------------------------------------------------ power 6: forced mutation
    public static ForgeConfigSpec.IntValue MUTATION_COST_MAX;
    public static ForgeConfigSpec.IntValue MUTATION_COST_MIN;
    public static ForgeConfigSpec.IntValue MUTATION_DISCOUNT_PER_KILL;
    public static ForgeConfigSpec.BooleanValue MUTATION_COST_SCALES_WITH_POINTS;
    public static ForgeConfigSpec.DoubleValue MUTATION_HP_PER_LEVEL;
    public static ForgeConfigSpec.IntValue MUTATION_MAX_PER_ENTITY;
    public static ForgeConfigSpec.IntValue MUTATION_BUFF_BASE_SECONDS;
    public static ForgeConfigSpec.IntValue MUTATION_BUFF_SECONDS_PER_LEVEL;
    public static ForgeConfigSpec.IntValue MUTATION_STRENGTH_MAX_POWER;
    public static ForgeConfigSpec.IntValue MUTATION_STRENGTH_LEVELS_PER_POWER;
    public static ForgeConfigSpec.BooleanValue MUTATION_RESISTANCE;
    public static ForgeConfigSpec.BooleanValue MUTATION_STRENGTH;
    public static ForgeConfigSpec.BooleanValue MUTATION_SPEED;
    public static ForgeConfigSpec.BooleanValue MUTATION_GLOWING;
    public static ForgeConfigSpec.IntValue MUTATION_COOLDOWN_MS;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> MUTATION_EVOLVABLE_MOBS;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> MUTATION_HYPER_EVOLVABLE_MOBS;
    public static ForgeConfigSpec.DoubleValue GLUTTONOUS_MUTATION_HP_HIGH;
    public static ForgeConfigSpec.DoubleValue GLUTTONOUS_MUTATION_HP_MID;
    public static ForgeConfigSpec.IntValue GLUTTONOUS_MUTATION_BIOMASS_HIGH;
    public static ForgeConfigSpec.IntValue GLUTTONOUS_MUTATION_BIOMASS_MID;
    public static ForgeConfigSpec.IntValue GLUTTONOUS_MUTATION_BIOMASS_LOW;

    // ------------------------------------------------------------------ power 7: second authority
    public static ForgeConfigSpec.DoubleValue MOUNT_MOVEMENT_STRENGTH;
    public static ForgeConfigSpec.DoubleValue MOUNT_JUMP_STRENGTH;
    public static ForgeConfigSpec.DoubleValue MOUNT_AQUATIC_MULTIPLIER;
    public static ForgeConfigSpec.BooleanValue MOUNT_GRANTS_RESISTANCE;
    public static ForgeConfigSpec.IntValue MOUNT_RESISTANCE_BASE_AMPLIFIER;
    public static ForgeConfigSpec.IntValue MOUNT_RESISTANCE_TIER1_LEVEL;
    public static ForgeConfigSpec.IntValue MOUNT_RESISTANCE_TIER1_AMPLIFIER;
    public static ForgeConfigSpec.IntValue MOUNT_RESISTANCE_TIER2_LEVEL;
    public static ForgeConfigSpec.IntValue MOUNT_RESISTANCE_TIER2_AMPLIFIER;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> MOUNT_FLYING_MOBS;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> MOUNT_AQUATIC_MOBS;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> MOUNT_BLACKLISTED_MOBS;

    // ------------------------------------------------------------------ highlights (ESP / X-ray), CLIENT side
    public static ForgeConfigSpec.IntValue ESP_BLOCK_RANGE;
    public static ForgeConfigSpec.IntValue ESP_BLOCK_LIMIT;
    public static ForgeConfigSpec.IntValue ESP_ENTITY_LIMIT;
    public static ForgeConfigSpec.DoubleValue ESP_MARKER_BASE_RANGE;
    public static ForgeConfigSpec.DoubleValue ESP_MARKER_RANGE_PER_AMPLIFIER;
    public static ForgeConfigSpec.DoubleValue ESP_UNEASY_RANGE;
    public static ForgeConfigSpec.DoubleValue ESP_SEASONED_RANGE;
    public static ForgeConfigSpec.DoubleValue ESP_WATER_RANGE;
    public static ForgeConfigSpec.ConfigValue<String> ESP_COLOR_BLOCK_REMAINS;
    public static ForgeConfigSpec.ConfigValue<String> ESP_COLOR_BLOCK_HIVE_SPAWN;
    public static ForgeConfigSpec.ConfigValue<String> ESP_COLOR_BLOCK_BIOMASS_LUMP;
    public static ForgeConfigSpec.ConfigValue<String> ESP_COLOR_BLOCK_OVERGROWN_SPAWNER;
    public static ForgeConfigSpec.ConfigValue<String> ESP_COLOR_BLOCK_BIOMASS_BULB;
    public static ForgeConfigSpec.ConfigValue<String> ESP_COLOR_PLAYER_WATER;
    public static ForgeConfigSpec.ConfigValue<String> ESP_COLOR_PLAYER_MARKER;
    public static ForgeConfigSpec.ConfigValue<String> ESP_COLOR_PLAYER_UNEASY;
    public static ForgeConfigSpec.ConfigValue<String> ESP_COLOR_ENTITY_MARKER;
    public static ForgeConfigSpec.ConfigValue<String> ESP_COLOR_ENTITY_UNEASY;
    public static ForgeConfigSpec.ConfigValue<String> ESP_COLOR_ENTITY_WATER;
    public static ForgeConfigSpec.ConfigValue<String> ESP_COLOR_ENTITY_SEASONED;
    public static ForgeConfigSpec.ConfigValue<String> ESP_COLOR_ENTITY_FALLBACK;

    // ------------------------------------------------------------------ class gain (Call of the Hive, Proto, induction)
    public static ForgeConfigSpec.DoubleValue PROTO_CALL_RADIUS;
    public static ForgeConfigSpec.BooleanValue INDUCTION_TELEPORT;
    public static ForgeConfigSpec.BooleanValue SURRENDER_TELEPORT;
    public static ForgeConfigSpec.EnumValue<TeleportTarget> TELEPORT_TARGET;
    public static ForgeConfigSpec.DoubleValue TELEPORT_PROTO_SEARCH_RADIUS;
    public static ForgeConfigSpec.ConfigValue<String> TELEPORT_STRUCTURE;
    public static ForgeConfigSpec.IntValue TELEPORT_FAR_MIN;
    public static ForgeConfigSpec.IntValue TELEPORT_FAR_MAX;
    public static ForgeConfigSpec.IntValue SURRENDER_COCOON_SECONDS;
    public static ForgeConfigSpec.DoubleValue SURRENDER_COCOON_HP;
    public static ForgeConfigSpec.IntValue SURRENDER_REGENERATION_TICKS;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> BOOK_REQUIRED_ITEMS;
    public static ForgeConfigSpec.IntValue BOOK_REQUIRED_XP_LEVELS;

    /** How the HP limit of the extraction (ability 2) is measured. */
    public enum ThresholdMode {
        /** The target must have fewer HP than {@code threshold_flat_hp} (today's rule: 5 HP). */
        FLAT,
        /** The target must have less than {@code threshold_percent} of its own maximum health. */
        PERCENT
    }

    /** Where the new Kommandant is carried when they are inducted into the hive. */
    public enum TeleportTarget {
        /** Next to the nearest Proto; if there is none, to the structure; if there is none, far from where they stood (today). */
        PROTO_FIRST,
        /** Ignore protos: straight to the structure (or far from where they stood when it is not found). */
        STRUCTURE_ONLY
    }

    /** One {@code entity;item;chance[;count]} entry of the extra extraction list. */
    public record ExtraExtraction(ResourceLocation entity, ConfigItems.Entry item, int chancePercent) {
    }

    private KommandantPowersConfig() {
    }

    static void define(ForgeConfigSpec.Builder builder) {
        builder.comment(
                "Kommandant abilities 1-7 and the general Kommandant rules.",
                "Ability numbers: 1 Rampant Evolution (cocoon), 2 Contaminate and extract, 3 Summon Core Mound, 4 Devour,",
                "5 Biomass Manipulation, 6 Forced Mutation, 7 Second Authority (mount control). Abilities 8-13 are in",
                "kommandant_abilities_advanced. Every default reproduces the behaviour before these options existed.",
                "Durations are in ticks (20 ticks = 1 second) unless the key says _seconds or _ms."
        ).push("kommandant_abilities");

        defineGeneral(builder);
        defineBiomassSources(builder);
        definePower1(builder);
        definePower2(builder);
        definePower3(builder);
        definePower4(builder);
        definePower5(builder);
        definePower6(builder);
        definePower7(builder);
        defineEsp(builder);
        defineClassGain(builder);

        builder.pop();
    }

    // ============================================================================================ general

    private static void defineGeneral(ForgeConfigSpec.Builder builder) {
        builder.push("general");
        MAX_EVOLUTION_LEVEL = builder
                .comment("Highest evolution level a Kommandant can reach. Default 9 (the original maximum).",
                        "Only LOWERING is supported (1-9): the stat tables, the evolution_costs and the ability unlock levels",
                        "were designed for levels 0-9. Players already above the new maximum are brought down to it.",
                        "Abilities whose unlock level is above this value can never be used.")
                .defineInRange("max_evolution_level", 9, 1, 9);
        builder.pop();

        builder.comment(
                "Evolution level at which each ability unlocks (the ability selector, the keys and the server all use these).",
                "Default: ability N unlocks at level N-4 (the first four abilities are available from level 0).",
                "The server refuses to use an ability whose unlock level the player has not reached.",
                "The selector and the keys read the CLIENT's own copy of this file: on a dedicated server give every player the same values."
        ).push("unlock_levels");
        for (int power = 1; power <= 13; power++) {
            UNLOCK_LEVELS[power] = builder
                    .comment("Evolution level (0-9) that unlocks ability " + power + ". Default " + defaultUnlockLevel(power) + ".")
                    .defineInRange("power_" + power, defaultUnlockLevel(power), 0, 9);
        }
        builder.pop();

        builder.comment(
                "Maximum of the biomass bar for each evolution level (the bar counts whole biomass units).",
                "Default 50 for every level (the original fixed maximum). The HUD scales the bar to the maximum of the",
                "player's current level. An evolution that costs more than the maximum can be paid with a full bar."
        ).push("biomass_bar");
        for (int level = 0; level <= 9; level++) {
            BAR_MAX_BY_LEVEL[level] = builder
                    .comment("Biomass bar maximum at evolution level " + level + ". Default 50.")
                    .defineInRange("level_" + level, 50, 1, 1000);
        }
        builder.pop();
    }

    private static int defaultUnlockLevel(int power) {
        return Math.max(0, power - 4);
    }

    // ============================================================================================ biomass sources

    private static void defineBiomassSources(ForgeConfigSpec.Builder builder) {
        builder.comment("Biomass a Kommandant (or their tentacle) gains for killing something in melee. Arrows, magic, explosions and tridents give nothing.")
                .push("biomass_from_kills");
        KILL_BIOMASS = builder
                .comment("Whole biomass units gained per kill. Default 1.")
                .defineInRange("biomass_per_kill", 1, 0, 50);
        KILL_LOW_HEALTH_THRESHOLD = builder
                .comment("Victims whose MAXIMUM health is below this (HP, 2 HP = 1 heart) count as weak and give the small amount below.",
                        "Default 4.0.")
                .defineInRange("low_health_threshold", 4.0D, 0.0D, 1000.0D);
        KILL_LOW_HEALTH_BIOMASS = builder
                .comment("Biomass gained for killing a weak victim. It is fractional: the bar fills when the pieces add up to 1.",
                        "Default 0.2 (five weak kills = 1 biomass).")
                .defineInRange("low_health_biomass", 0.2D, 0.0D, 50.0D);
        KILL_OVERRIDES = builder
                .comment("Per-entity override of the biomass a kill gives, as modid:entity;amount (amount may be fractional, 0 = gives nothing).",
                        "Example: minecraft:cow;0.5 or minecraft:warden;10. Entities not listed use the two rules above. Default: none.")
                .defineListAllowEmpty(List.of("entity_overrides"), () -> List.<String>of(),
                        o -> o instanceof String s && KILL_OVERRIDE_SYNTAX.matcher(s).matches());
        KILL_ONLY_LISTED = builder
                .comment("If true, ONLY the entities named in entity_overrides give biomass when killed; every other kill gives nothing.",
                        "Default false (every kill gives biomass).")
                .define("only_listed_entities", false);
        builder.pop();

        builder.comment(
                "By default a Kommandant can only eat the foods of diet_restrictions.kommandant_edible_items and eating gives no biomass.",
                "With food_to_biomass ON every edible item may be eaten and each bite fills the biomass bar."
        ).push("biomass_from_eating");
        FOOD_BIOMASS_ENABLED = builder
                .comment("If true, a Kommandant may eat ANY food (the diet whitelist is ignored) and gains biomass from it.",
                        "Default false (the whitelist behaviour of diet_restrictions applies and eating gives no biomass).")
                .define("food_to_biomass", false);
        FOOD_BIOMASS_PER_HUNGER = builder
                .comment("Biomass gained per hunger point (half a drumstick) the food restores. Only used when food_to_biomass is true. Default 0.5.")
                .defineInRange("biomass_per_hunger_point", 0.5D, 0.0D, 20.0D);
        FOOD_BIOMASS_PER_SATURATION = builder
                .comment("Biomass gained per saturation point of the food, on top of the hunger part. Only used when food_to_biomass is true. Default 0.0.")
                .defineInRange("biomass_per_saturation_point", 0.0D, 0.0D, 20.0D);
        builder.pop();

        builder.comment(
                "A starving Kommandant (hunger bar at 0) drains the biomass bar first and only then takes damage.",
                "This is the closest thing to 'hunger feeding the biomass bar': the bar is the hunger reserve.",
                "(Replacing the vanilla hunger bar with the biomass bar is not supported.)"
        ).push("starvation");
        STARVATION_INTERVAL_TICKS = builder
                .comment("Ticks between two starvation steps while the hunger bar is at 0. Default 100 (5 seconds).")
                .defineInRange("interval_ticks", 100, 1, 24000);
        STARVATION_BIOMASS_DRAIN = builder
                .comment("Biomass lost per starvation step while the bar has biomass. Default 1.")
                .defineInRange("biomass_drain", 1, 0, 1000);
        STARVATION_DAMAGE = builder
                .comment("Damage (HP, 2 HP = 1 heart) taken per starvation step once the biomass bar is empty. Default 3.0.")
                .defineInRange("damage", 3.0D, 0.0D, 1000.0D);
        builder.pop();
    }

    // ============================================================================================ power 1

    private static void definePower1(ForgeConfigSpec.Builder builder) {
        builder.comment(
                "Ability 1, Rampant Evolution: the Kommandant is sealed in a cocoon and evolves one level when the incubation ends.",
                "Item and biomass costs of each level are in the top-level evolution_costs section."
        ).push("power_1_cocoon");
        COCOON_HP_PER_LEVEL = builder
                .comment("Cocoon HP per current evolution level: cocoon HP = (current level + 1) x this. Default 100.0.")
                .defineInRange("cocoon_hp_per_level", 100.0D, 1.0D, 1000000.0D);
        COCOON_MAX_LEVEL_HP = builder
                .comment("Cocoon HP when a max-level Kommandant re-enters the cocoon without a Mutagenic Compound. Default 300.0.")
                .defineInRange("max_level_cocoon_hp", 300.0D, 1.0D, 1000000.0D);
        COCOON_BASE_SECONDS = builder
                .comment("Base incubation time in seconds. Default 30.")
                .defineInRange("incubation_base_seconds", 30, 1, 3600);
        COCOON_STEP_SECONDS = builder
                .comment("Seconds added to the incubation time for every full step of levels (see incubation_levels_per_step). Default 30.",
                        "Incubation = base + step_seconds x (current level / levels_per_step, rounded down).")
                .defineInRange("incubation_step_seconds", 30, 0, 3600);
        COCOON_LEVELS_PER_STEP = builder
                .comment("Evolution levels per incubation step. Default 3.")
                .defineInRange("incubation_levels_per_step", 3, 1, 9);
        COCOON_MAX_LEVEL_SECONDS = builder
                .comment("Incubation time in seconds of a max-level cocoon without a Mutagenic Compound. Default 30.")
                .defineInRange("max_level_incubation_seconds", 30, 1, 3600);
        COCOON_MAX_LEVEL_BIOMASS_COST = builder
                .comment("Biomass paid to enter the cocoon again at max level (no items needed). Default 10.",
                        "Normal evolutions use the 'cost:' part of evolution_costs instead.")
                .defineInRange("max_level_biomass_cost", 10, 0, 1000);
        COCOON_INTERRUPTED_SLOWNESS_TICKS = builder
                .comment("Slowness duration in ticks when the cocoon is broken or the player leaves it (0 = no slowness). Default 400 (20 seconds).")
                .defineInRange("interrupted_slowness_ticks", 400, 0, 1000000);
        COCOON_INTERRUPTED_SLOWNESS_AMPLIFIER = builder
                .comment("Slowness level of that penalty minus 1 (0 = Slowness I, 1 = Slowness II). Default 1.")
                .defineInRange("interrupted_slowness_amplifier", 1, 0, 10);
        COCOON_INTERRUPTED_WEAKNESS_TICKS = builder
                .comment("Extra Weakness duration in ticks when a MAX-LEVEL cocoon is broken (0 = none). Default 400 (20 seconds).")
                .defineInRange("interrupted_weakness_ticks", 400, 0, 1000000);
        COCOON_INTERRUPTED_WEAKNESS_AMPLIFIER = builder
                .comment("Weakness level of that penalty minus 1 (2 = Weakness III). Default 2.")
                .defineInRange("interrupted_weakness_amplifier", 2, 0, 10);
        COCOON_COMPLETED_RESISTANCE_TICKS = builder
                .comment("Resistance duration in ticks granted when a max-level cocoon finishes. Default 1200 (60 seconds).")
                .defineInRange("completed_resistance_ticks", 1200, 0, 1000000);
        COCOON_COMPLETED_RESISTANCE_AMPLIFIER = builder
                .comment("Resistance level of that reward minus 1 (1 = Resistance II). Default 1.")
                .defineInRange("completed_resistance_amplifier", 1, 0, 10);
        EVOLUTION_REQUIRES_PROTO_FROM_LEVEL = builder
                .comment("Extra requirement: evolving INTO this level or higher needs a living Proto (Protobrain) within proto_radius blocks.",
                        "0 = no such requirement (default). 1-9 = the first evolution level that needs a nearby Proto.")
                .defineInRange("requires_proto_from_level", 0, 0, 9);
        EVOLUTION_PROTO_RADIUS = builder
                .comment("Radius in blocks used by requires_proto_from_level. Default 128.0.")
                .defineInRange("proto_radius", 128.0D, 8.0D, 4096.0D);
        builder.pop();
    }

    // ============================================================================================ power 2

    private static void definePower2(ForgeConfigSpec.Builder builder) {
        builder.comment(
                "Ability 2, Contaminate and extract. While it is toggled on, melee hits infect with Mycelium, and right-clicking a",
                "weakened entity tears an item out of it (the target dies whatever the outcome).",
                "All chances are percentages from 0 to 100."
        ).push("power_2_extraction");
        MYCELIUM_SECONDS_PER_LEVEL = builder
                .comment("Mycelium infection duration in seconds per evolution level: duration = level x this (never below mycelium_min_seconds). Default 5.")
                .defineInRange("mycelium_seconds_per_level", 5, 0, 3600);
        MYCELIUM_MIN_SECONDS = builder
                .comment("Shortest Mycelium infection in seconds. Default 5.")
                .defineInRange("mycelium_min_seconds", 5, 1, 3600);
        MYCELIUM_LEVELS_PER_AMPLIFIER = builder
                .comment("Evolution levels needed for each extra Mycelium level: amplifier = level / this (rounded down). Default 3.")
                .defineInRange("mycelium_levels_per_amplifier", 3, 1, 9);

        EXTRACTION_THRESHOLD_MODE = builder
                .comment("How the HP limit of the extraction is measured: FLAT (a number of HP, default) or PERCENT (a share of the target's max health).")
                .defineEnum("threshold_mode", ThresholdMode.FLAT);
        EXTRACTION_THRESHOLD_FLAT = builder
                .comment("FLAT mode: the target must have FEWER HP than this (2 HP = 1 heart). Default 5.0.",
                        "Devour (ability 4) refuses entities under this limit as well, so they can be extracted instead.")
                .defineInRange("threshold_flat_hp", 5.0D, 0.0D, 100000.0D);
        EXTRACTION_THRESHOLD_PERCENT = builder
                .comment("PERCENT mode: the target must have less than this fraction of its maximum health (0.0-1.0). Default 0.25.")
                .defineInRange("threshold_percent", 0.25D, 0.0D, 1.0D);

        CHANCE_PLAYER = builder
                .comment("Chance in % of a Dangerous Mind when extracting from a player. Default 95.")
                .defineInRange("chance_player", 95, 0, 100);
        CHANCE_INFECTED_PLAYER_GENERATED = builder
                .comment("Chance in % of a Dangerous Mind from an infected adventurer made from a dead player. Default 65.")
                .defineInRange("chance_infected_player_generated", 65, 0, 100);
        CHANCE_INFECTED_PLAYER_NATURAL_DEDICATED = builder
                .comment("Chance in % of a Dangerous Mind from a naturally spawned infected adventurer on a DEDICATED server. Default 1.")
                .defineInRange("chance_infected_player_natural_dedicated", 1, 0, 100);
        CHANCE_INFECTED_PLAYER_NATURAL_SINGLEPLAYER = builder
                .comment("Same as above in single player / LAN. Default 10.")
                .defineInRange("chance_infected_player_natural_singleplayer", 10, 0, 100);
        CHANCE_VILLAGER = builder
                .comment("Chance in % of an Individualist Mind from a villager. Default 75.")
                .defineInRange("chance_villager", 75, 0, 100);
        CHANCE_ZOMBIE_VILLAGER = builder
                .comment("Chance in % of an Individualist Mind from a zombie villager. Default 50.")
                .defineInRange("chance_zombie_villager", 50, 0, 100);
        CHANCE_WITHER_SKELETON = builder
                .comment("Chance in % of an Intact Wither Marrow from a wither skeleton. Default 75.")
                .defineInRange("chance_wither_skeleton", 75, 0, 100);
        CHANCE_WITCH = builder
                .comment("Chance in % of a Chemist Mind from a witch. Default 85.")
                .defineInRange("chance_witch", 85, 0, 100);
        CHANCE_ENDERMAN = builder
                .comment("Chance in % of a Weakened Ender Eye from an enderman (a failure leaves nothing, not even a sound). Default 20.")
                .defineInRange("chance_enderman", 20, 0, 100);
        CHANCE_UNDEAD_BASE = builder
                .comment("Chance in % of a Mutated Undead from a zombie, drowned or husk at evolution level 0. Default 20.")
                .defineInRange("chance_undead_base", 20, 0, 100);
        CHANCE_UNDEAD_PER_LEVEL = builder
                .comment("Extra % added to the undead chance per evolution level (chance = base + level x this, capped at 100). Default 5.")
                .defineInRange("chance_undead_per_level", 5, 0, 100);
        UNDEAD_BIOMASS_COST = builder
                .comment("Biomass paid to extract from a zombie, drowned or husk (without enough biomass nothing is extracted). Default 1.")
                .defineInRange("undead_biomass_cost", 1, 0, 1000);
        WITHER_FAILURE_TICKS = builder
                .comment("Wither effect duration in ticks inflicted on the player when a wither skeleton extraction fails (0 = none). Default 200 (10 seconds).")
                .defineInRange("wither_failure_ticks", 200, 0, 1000000);
        WITHER_FAILURE_AMPLIFIER = builder
                .comment("Wither level of that penalty minus 1 (1 = Wither II). Default 1.")
                .defineInRange("wither_failure_amplifier", 1, 0, 10);
        EXTRA_EXTRACTIONS = builder
                .comment("Extra victims, one entry each: modid:entity;modid:item;chance_percent or modid:entity;modid:item;chance_percent;count.",
                        "Example: minecraft:piglin;sporeadd:individualist_mind;40. Entries here are checked BEFORE the built-in ones above,",
                        "so they can also change what a built-in victim gives. A failed roll just destroys the target. Default: none.")
                .defineListAllowEmpty(List.of("extra_extractions"), () -> List.<String>of(),
                        o -> o instanceof String s && EXTRACTION_SYNTAX.matcher(s).matches());
        builder.pop();
    }

    // ============================================================================================ power 3

    private static void definePower3(ForgeConfigSpec.Builder builder) {
        builder.comment("Ability 3, Summon Core Mound: places a core mound (respawn / travel point) where the player looks.")
                .push("power_3_mounds");
        MOUND_BIOMASS_COST = builder
                .comment("Biomass paid to summon a mound. Default 15.")
                .defineInRange("biomass_cost", 15, 0, 1000);
        MAX_MOUNDS = builder
                .comment("How many core mounds one Kommandant can have registered. Default 10.",
                        "Only 1-10 is supported (the ability selector and /vervarequest have 10 slots).",
                        "When the limit is reached the OLDEST mound is dropped from the list (its block stays in the world),",
                        "unless refuse_when_full is true. Lowering this also trims lists that are already longer, on the next summon.")
                .defineInRange("max_mounds", 10, 1, 10);
        MOUND_REFUSE_WHEN_FULL = builder
                .comment("If true, summoning is refused (and costs nothing) while the player already has max_mounds mounds. Default false.")
                .define("refuse_when_full", false);
        MOUND_PLACEMENT_RANGE = builder
                .comment("Maximum distance in blocks at which the mound is placed along the player's line of sight. Default 20.0.")
                .defineInRange("placement_range", 20.0D, 1.0D, 128.0D);
        MOUND_REMOVE_DESTROYS = builder
                .comment("If true, '/mound <slot|name> remove' also destroys the mound in the world (and frees its loaded chunk).",
                        "If false (default) the command only takes it off the player's list and the mound stays where it is.")
                .define("remove_command_destroys_mound", false);
        builder.pop();
    }

    // ============================================================================================ power 4

    private static void definePower4(ForgeConfigSpec.Builder builder) {
        builder.comment("Ability 4, Devour: right-click infected or certain blocks to eat them (biomass plus food).")
                .push("power_4_devour");
        DEVOUR_BASIC_MIN = builder
                .comment("Fewest biomass units gained from a BASIC infected (the basic_entities list). Default 2.")
                .defineInRange("basic_biomass_min", 2, 0, 100);
        DEVOUR_BASIC_MAX = builder
                .comment("Most biomass units gained from a BASIC infected. Default 3.")
                .defineInRange("basic_biomass_max", 3, 0, 100);
        DEVOUR_STRONG_MIN = builder
                .comment("Fewest biomass units gained from a STRONG (evolved) infected (the strong_entities list). Default 3.")
                .defineInRange("strong_biomass_min", 3, 0, 100);
        DEVOUR_STRONG_MAX = builder
                .comment("Most biomass units gained from a STRONG infected. Default 4.")
                .defineInRange("strong_biomass_max", 4, 0, 100);
        DEVOUR_ANTIFARM_BIOMASS = builder
                .comment("Biomass gained from an entity carrying the 'antifarm' tag (farmed ones), instead of the ranges above. Default 1.")
                .defineInRange("antifarm_biomass", 1, 0, 100);
        DEVOUR_MEAT_ABOMINATION_BIOMASS = builder
                .comment("Biomass gained per bite of a Meat Abomination. Default 1.")
                .defineInRange("meat_abomination_biomass", 1, 0, 100);
        DEVOUR_REMAINS_BIOMASS = builder
                .comment("Biomass gained from a block of the remains_blocks list. Default 3.")
                .defineInRange("remains_biomass", 3, 0, 100);
        DEVOUR_BULB_BIOMASS = builder
                .comment("Biomass gained from a block of the bulb_blocks list. Default 1.")
                .defineInRange("bulb_biomass", 1, 0, 100);
        DEVOUR_BULB_CHANCE = builder
                .comment("Chance in % that a bulb block gives its biomass (it is always consumed and always feeds). Default 100.",
                        "The ability tooltip text says 40%, but the code always gave the biomass.")
                .defineInRange("bulb_biomass_chance", 100, 0, 100);
        DEVOUR_COOLDOWN_TICKS = builder
                .comment("Ticks between two devoured entities (stops stacked corpses from chaining rewards in one instant). Default 4.")
                .defineInRange("entity_cooldown_ticks", 4, 0, 200);
        DEVOUR_BASIC_ENTITIES = builder
                .comment("Entities that count as BASIC infected for Devour. Default: the original list.")
                .defineListAllowEmpty(List.of("basic_entities"), () -> List.of(
                        "spore:inf_human", "spore:inf_husk", "spore:inf_player", "spore:inf_villager", "spore:inf_witch",
                        "spore:inf_pillager", "spore:inf_vindicator", "spore:inf_evoker", "spore:inf_wanderer",
                        "spore:inf_drowned", "spore:inf_hazmat", "spore:plagued", "spore:lacerator", "spore:biobloob",
                        "spore:saugling", "spore:scamper", "spore:chemist", "spore:inf_diseased_villager", "spore:claw",
                        "spore:hevoker_arm", "spore:bairn"
                ), o -> o instanceof String s && ID_SYNTAX.matcher(s.trim()).matches());
        DEVOUR_STRONG_ENTITIES = builder
                .comment("Entities that count as STRONG infected for Devour. Default: the original list.",
                        "An entity in neither list cannot be devoured (and the Meat Abomination never can).")
                .defineListAllowEmpty(List.of("strong_entities"), () -> List.of(
                        "spore:knight", "spore:griefer", "spore:braiomil", "spore:busser", "spore:leaper", "spore:slasher",
                        "spore:spitter", "spore:howler", "spore:stalker", "spore:brute", "spore:volatile", "spore:inebriater",
                        "spore:thorn", "spore:jagd", "spore:scavenger", "spore:bloater", "spore:nuclea", "spore:protector",
                        "spore:gastgaber", "spore:specter", "spore:howit_arm", "spore:sieger_tail", "spore:licker",
                        "spore:corpse_piece", "spore:conductor", "spore:gargoyle", "spore:reaper", "spore:vanguard",
                        "spore:naiad", "spore:mephitic", "spore:gorgon"
                ), o -> o instanceof String s && ID_SYNTAX.matcher(s.trim()).matches());
        DEVOUR_REMAINS_BLOCKS = builder
                .comment("Blocks Devour eats as REMAINS (gives remains_biomass and 5 ticks of Saturation). Default: spore:remains, spore:wall_remains.")
                .defineListAllowEmpty(List.of("remains_blocks"), () -> List.of(
                        "spore:remains", "spore:wall_remains"
                ), o -> o instanceof String s && ID_SYNTAX.matcher(s.trim()).matches());
        DEVOUR_BULB_BLOCKS = builder
                .comment("Blocks Devour eats as BULBS (gives bulb_biomass and 3 ticks of Saturation). Default: spore:biomass_bulb, spore:drowned_lump.")
                .defineListAllowEmpty(List.of("bulb_blocks"), () -> List.of(
                        "spore:biomass_bulb", "spore:drowned_lump"
                ), o -> o instanceof String s && ID_SYNTAX.matcher(s.trim()).matches());
        builder.pop();
    }

    // ============================================================================================ power 5

    private static void definePower5(ForgeConfigSpec.Builder builder) {
        builder.comment("Ability 5, Biomass Manipulation: Shift + right-click collects structure blocks, right-click feeds living blocks with biomass.")
                .push("power_5_manipulation");
        FEED_BIOMASS_COST = builder
                .comment("Biomass paid for each feeding of a living block or mound. Default 1.")
                .defineInRange("feed_biomass_cost", 1, 0, 1000);
        FEED_MOUNDS = builder
                .comment("If true, right-clicking a Spore mound (the mound ENTITY, spore:mound) with this ability on also feeds it: it costs",
                        "feed_biomass_cost and the mound grows one age stage (never above its own maximum age).",
                        "Default false (only living blocks can be fed, as always).")
                .define("feed_mounds", false);
        MANIPULATION_COOLDOWN_MS = builder
                .comment("Milliseconds between two uses of this ability (a debounce against click spam). Default 200.")
                .defineInRange("use_cooldown_ms", 200, 0, 10000);
        HARVESTABLE_BLOCKS = builder
                .comment("Blocks that Shift + right-click collects into the inventory. Default: spore:remains, spore:wall_remains, spore:hive_spawn, spore:biomass_lump.")
                .defineListAllowEmpty(List.of("harvestable_blocks"), () -> List.of(
                        "spore:remains", "spore:wall_remains", "spore:hive_spawn", "spore:biomass_lump"
                ), o -> o instanceof String s && ID_SYNTAX.matcher(s.trim()).matches());
        FEEDABLE_BLOCKS = builder
                .comment("Living blocks that right-click can feed with biomass (they must be Spore living-structure blocks to react).",
                        "Right-clicking a block of this list is cancelled for EVERY player (as it always was), so only list living blocks.",
                        "Default: spore:hive_spawn, spore:biomass_lump.")
                .defineListAllowEmpty(List.of("feedable_blocks"), () -> List.of(
                        "spore:hive_spawn", "spore:biomass_lump"
                ), o -> o instanceof String s && ID_SYNTAX.matcher(s.trim()).matches());
        builder.pop();
    }

    // ============================================================================================ power 6

    private static void definePower6(ForgeConfigSpec.Builder builder) {
        builder.comment("Ability 6, Forced Mutation: right-click an infected or utility entity to force its evolution and buff it.")
                .push("power_6_forced_mutation");
        MUTATION_COST_MAX = builder
                .comment("Highest biomass cost of one mutation. Default 5.")
                .defineInRange("biomass_cost_max", 5, 0, 1000);
        MUTATION_COST_MIN = builder
                .comment("Lowest biomass cost after the kill discount. Default 1.")
                .defineInRange("biomass_cost_min", 1, 0, 1000);
        MUTATION_DISCOUNT_PER_KILL = builder
                .comment("Biomass discount for every kill the target already has (cost = base - kills x this, never below the minimum). Default 1.")
                .defineInRange("discount_per_kill", 1, 0, 1000);
        MUTATION_COST_SCALES_WITH_POINTS = builder
                .comment("If true (default), evolvable infected cost only the biomass for the evolution points they still lack (at most the maximum cost).",
                        "If false every mutation costs the maximum cost before the kill discount.")
                .define("cost_scales_with_missing_points", true);
        MUTATION_HP_PER_LEVEL = builder
                .comment("Permanent max HP given to the target per evolution level of the Kommandant (2 HP = 1 heart). Default 17.5.")
                .defineInRange("extra_hp_per_level", 17.5D, 0.0D, 100000.0D);
        MUTATION_MAX_PER_ENTITY = builder
                .comment("How many times one entity can be force-mutated (the evolution limit). Default 1.")
                .defineInRange("max_mutations_per_entity", 1, 1, 100);
        MUTATION_BUFF_BASE_SECONDS = builder
                .comment("Base duration in seconds of the temporary buffs (Resistance, Strength, Speed, Glowing). Default 20.")
                .defineInRange("buff_base_seconds", 20, 0, 36000);
        MUTATION_BUFF_SECONDS_PER_LEVEL = builder
                .comment("Seconds added to the buffs per evolution level of the Kommandant. Default 3.")
                .defineInRange("buff_seconds_per_level", 3, 0, 36000);
        MUTATION_STRENGTH_MAX_POWER = builder
                .comment("Strength power cap: the Strength level given is min(this, level / levels_per_power) (power 1 = Strength I). Default 5.")
                .defineInRange("strength_max_power", 5, 0, 10);
        MUTATION_STRENGTH_LEVELS_PER_POWER = builder
                .comment("Evolution levels of the Kommandant per Strength power. Default 2.")
                .defineInRange("strength_levels_per_power", 2, 1, 9);
        MUTATION_RESISTANCE = builder
                .comment("Give the target Resistance I. Default true.")
                .define("buff_resistance", true);
        MUTATION_STRENGTH = builder
                .comment("Give the target Strength. Default true.")
                .define("buff_strength", true);
        MUTATION_SPEED = builder
                .comment("Give the target Speed I. Default true.")
                .define("buff_speed", true);
        MUTATION_GLOWING = builder
                .comment("Make the target glow. Default true.")
                .define("buff_glowing", true);
        MUTATION_COOLDOWN_MS = builder
                .comment("Milliseconds between two uses of this ability (a debounce against click spam). Default 200.")
                .defineInRange("use_cooldown_ms", 200, 0, 10000);
        MUTATION_EVOLVABLE_MOBS = builder
                .comment("Entities whose mutation also fills their evolution points (their cost follows the points they lack). Default: the original list.")
                .defineListAllowEmpty(List.of("evolvable_mobs"), () -> List.of(
                        "spore:inf_human", "spore:inf_husk", "spore:inf_drowned", "spore:inf_villager",
                        "spore:inf_diseased_villager", "spore:inf_wanderer", "spore:inf_witch", "spore:inf_pillager",
                        "spore:inf_player", "spore:inf_hazmat", "spore:knight", "spore:braiomil", "spore:leaper",
                        "spore:stalker", "spore:brute", "spore:inf_vindicator", "spore:inf_evoker"
                ), o -> o instanceof String s && ID_SYNTAX.matcher(s.trim()).matches());
        MUTATION_HYPER_EVOLVABLE_MOBS = builder
                .comment("Entities that need the Spore mod's hyper-evolution kill count instead of the normal one. Default: the original list.")
                .defineListAllowEmpty(List.of("hyper_evolvable_mobs"), () -> List.of(
                        "spore:knight", "spore:braiomil", "spore:leaper", "spore:stalker", "spore:brute",
                        "spore:inf_vindicator", "spore:inf_evoker"
                ), o -> o instanceof String s && ID_SYNTAX.matcher(s.trim()).matches());
        GLUTTONOUS_MUTATION_HP_HIGH = builder
                .comment("Gluttonous variant: targets with MORE HP than this become a Meat Abomination worth the 'high' biomass. Default 40.0.")
                .defineInRange("gluttonous_hp_high", 40.0D, 0.0D, 100000.0D);
        GLUTTONOUS_MUTATION_HP_MID = builder
                .comment("Gluttonous variant: targets with MORE HP than this (and not above the high limit) give the 'mid' biomass. Default 20.0.")
                .defineInRange("gluttonous_hp_mid", 20.0D, 0.0D, 100000.0D);
        GLUTTONOUS_MUTATION_BIOMASS_HIGH = builder
                .comment("Gluttonous variant: biomass stored in the Meat Abomination made from a high-HP target. Default 3.")
                .defineInRange("gluttonous_biomass_high", 3, 0, 1000);
        GLUTTONOUS_MUTATION_BIOMASS_MID = builder
                .comment("Gluttonous variant: biomass for a mid-HP target. Default 2.")
                .defineInRange("gluttonous_biomass_mid", 2, 0, 1000);
        GLUTTONOUS_MUTATION_BIOMASS_LOW = builder
                .comment("Gluttonous variant: biomass for a low-HP target. Default 1.")
                .defineInRange("gluttonous_biomass_low", 1, 0, 1000);
        builder.pop();
    }

    // ============================================================================================ power 7

    private static void definePower7(ForgeConfigSpec.Builder builder) {
        builder.comment("Ability 7, Second Authority: right-click an infected, utility entity or calamity to ride and steer it.")
                .push("power_7_second_authority");
        MOUNT_MOVEMENT_STRENGTH = builder
                .comment("Ground / water speed given to the mount while steering (blocks per tick). Default 0.35.")
                .defineInRange("movement_strength", 0.35D, 0.01D, 5.0D);
        MOUNT_JUMP_STRENGTH = builder
                .comment("Upward speed of the mount's jump on Spacebar. Default 1.0.")
                .defineInRange("jump_strength", 1.0D, 0.1D, 5.0D);
        MOUNT_AQUATIC_MULTIPLIER = builder
                .comment("Speed multiplier for aquatic mounts while they are in water. Default 1.25 (+25%).")
                .defineInRange("aquatic_speed_multiplier", 1.25D, 0.1D, 5.0D);
        MOUNT_GRANTS_RESISTANCE = builder
                .comment("If true (default) the ridden entity keeps a Resistance effect while it is ridden.")
                .define("grants_resistance", true);
        MOUNT_RESISTANCE_BASE_AMPLIFIER = builder
                .comment("Resistance level minus 1 given to the mount below the tiers (0 = Resistance I). Default 0.")
                .defineInRange("resistance_base_amplifier", 0, 0, 4);
        MOUNT_RESISTANCE_TIER1_LEVEL = builder
                .comment("Evolution level from which the mount gets the tier 1 Resistance. Default 7.")
                .defineInRange("resistance_tier1_level", 7, 0, 9);
        MOUNT_RESISTANCE_TIER1_AMPLIFIER = builder
                .comment("Resistance level minus 1 of tier 1 (1 = Resistance II). Default 1.")
                .defineInRange("resistance_tier1_amplifier", 1, 0, 4);
        MOUNT_RESISTANCE_TIER2_LEVEL = builder
                .comment("Evolution level from which the mount gets the tier 2 Resistance (checked first). Default 8.")
                .defineInRange("resistance_tier2_level", 8, 0, 9);
        MOUNT_RESISTANCE_TIER2_AMPLIFIER = builder
                .comment("Resistance level minus 1 of tier 2 (2 = Resistance III). Default 2.")
                .defineInRange("resistance_tier2_amplifier", 2, 0, 4);
        MOUNT_FLYING_MOBS = builder
                .comment("Mounts that fly (Spacebar ascends, Ctrl descends). Default: spore:busser, spore:hindenburg, spore:gargoyle, spore:verfalldrachen.")
                .defineListAllowEmpty(List.of("flying_mobs"), () -> List.of(
                        "spore:busser", "spore:hindenburg", "spore:gargoyle", "spore:verfalldrachen"
                ), o -> o instanceof String s && ID_SYNTAX.matcher(s.trim()).matches());
        MOUNT_AQUATIC_MOBS = builder
                .comment("Mounts that swim (faster and free vertical movement in water). Default: spore:inf_drowned, spore:bloater, spore:naiad, spore:leviathan, spore:kraken.")
                .defineListAllowEmpty(List.of("aquatic_mobs"), () -> List.of(
                        "spore:inf_drowned", "spore:bloater", "spore:naiad", "spore:leviathan", "spore:kraken"
                ), o -> o instanceof String s && ID_SYNTAX.matcher(s.trim()).matches());
        MOUNT_BLACKLISTED_MOBS = builder
                .comment("Entities that can never be ridden. Default: the original list of parts, projectiles and clouds.")
                .defineListAllowEmpty(List.of("blacklisted_mobs"), () -> List.of(
                        "spore:arena_tendril", "spore:corpse_piece", "spore:illusion", "spore:tendril", "spore:claw",
                        "spore:nuke", "spore:scent", "spore:wave", "spore:hohlfresser"
                ), o -> o instanceof String s && ID_SYNTAX.matcher(s.trim()).matches());
        builder.pop();
    }

    // ============================================================================================ highlights

    private static void defineEsp(ForgeConfigSpec.Builder builder) {
        builder.comment(
                "Through-the-wall highlights of the Kommandant (blocks shown by abilities 4 and 5, entities with the Marker, Uneasy",
                "or Seasoned effect). CLIENT-SIDE: they are drawn by each player's own game, which reads its OWN copy of this file",
                "(config/sporeadds-config.toml), so on a dedicated server every player must edit their file (or the pack must ship it).",
                "Colours are hex RRGGBB (for example FF8000 = orange). An invalid colour falls back to the default."
        ).push("highlights");
        ESP_BLOCK_RANGE = builder
                .comment("Radius in blocks of the cube scanned for highlighted blocks. Default 36. Large values cost framerate (cube volume).")
                .defineInRange("block_range", 36, 1, 64);
        ESP_BLOCK_LIMIT = builder
                .comment("Most blocks highlighted at once (the nearest ones win). 0 = no limit (default).")
                .defineInRange("block_limit", 0, 0, 100000);
        ESP_ENTITY_LIMIT = builder
                .comment("Most entities highlighted at once (the nearest ones win). 0 = no limit (default).")
                .defineInRange("entity_limit", 0, 0, 10000);
        ESP_MARKER_BASE_RANGE = builder
                .comment("Distance in blocks at which a Marker I entity is highlighted. Default 24.0.")
                .defineInRange("marker_base_range", 24.0D, 0.0D, 512.0D);
        ESP_MARKER_RANGE_PER_AMPLIFIER = builder
                .comment("Extra blocks of Marker range for each Marker level above I. Default 12.0.")
                .defineInRange("marker_range_per_amplifier", 12.0D, 0.0D, 512.0D);
        ESP_UNEASY_RANGE = builder
                .comment("Distance in blocks at which an Uneasy entity is highlighted. Default 64.0.")
                .defineInRange("uneasy_range", 64.0D, 0.0D, 512.0D);
        ESP_SEASONED_RANGE = builder
                .comment("Distance in blocks at which a Seasoned entity is highlighted. Default 42.0.")
                .defineInRange("seasoned_range", 42.0D, 0.0D, 512.0D);
        ESP_WATER_RANGE = builder
                .comment("Distance in blocks at which an Abyssal Kommandant sees players that are in water. Default 64.0.")
                .defineInRange("water_range", 64.0D, 0.0D, 512.0D);

        ESP_COLOR_BLOCK_REMAINS = builder
                .comment("Block outline colour of remains / wall remains. Default FF8000.")
                .define("color_block_remains", "FF8000");
        ESP_COLOR_BLOCK_HIVE_SPAWN = builder
                .comment("Block outline colour of hive spawn blocks. Default FF0000.")
                .define("color_block_hive_spawn", "FF0000");
        ESP_COLOR_BLOCK_BIOMASS_LUMP = builder
                .comment("Block outline colour of biomass lumps. Default FFFF00.")
                .define("color_block_biomass_lump", "FFFF00");
        ESP_COLOR_BLOCK_OVERGROWN_SPAWNER = builder
                .comment("Block outline colour of overgrown spawners. Default 9900FF.")
                .define("color_block_overgrown_spawner", "9900FF");
        ESP_COLOR_BLOCK_BIOMASS_BULB = builder
                .comment("Block outline colour of biomass bulbs. Default 00FF00.")
                .define("color_block_biomass_bulb", "00FF00");
        ESP_COLOR_PLAYER_WATER = builder
                .comment("Box colour of players in water seen by an Abyssal Kommandant. Default 00FFFF.")
                .define("color_player_water", "00FFFF");
        ESP_COLOR_PLAYER_MARKER = builder
                .comment("Box colour of marked players seen by an Abyssal Kommandant. Default FF7373.")
                .define("color_player_marker", "FF7373");
        ESP_COLOR_PLAYER_UNEASY = builder
                .comment("Box colour of uneasy players seen by an Abyssal Kommandant. Default 9900FF.")
                .define("color_player_uneasy", "9900FF");
        ESP_COLOR_ENTITY_MARKER = builder
                .comment("Glow colour of Marker entities. Default FF7373.")
                .define("color_entity_marker", "FF7373");
        ESP_COLOR_ENTITY_UNEASY = builder
                .comment("Glow colour of Uneasy entities. Default A865C9.")
                .define("color_entity_uneasy", "A865C9");
        ESP_COLOR_ENTITY_WATER = builder
                .comment("Glow colour of players in water seen by an Abyssal Kommandant. Default 00FFFF.")
                .define("color_entity_water", "00FFFF");
        ESP_COLOR_ENTITY_SEASONED = builder
                .comment("Glow colour of Seasoned entities. Default AA0000.")
                .define("color_entity_seasoned", "AA0000");
        ESP_COLOR_ENTITY_FALLBACK = builder
                .comment("Glow colour when no other rule applies. Default 00FF00.")
                .define("color_entity_fallback", "00FF00");
        builder.pop();
    }

    // ============================================================================================ class gain

    private static void defineClassGain(ForgeConfigSpec.Builder builder) {
        builder.comment(
                "How a player becomes a Kommandant and what happens to them. Related sections: call_of_the_hive (chance, force_surrender),",
                "training_book (uses, kommandant_locked, enabled_classes), training_book_class_slots (kommandant slots),",
                "evolution_costs, proto_detection and punishment_event.",
                "",
                "What is a Proto? The Proto (also called Protobrain, Protohive or proto-hivemind in this mod's texts) is the",
                "Spore mod's central hive-mind organoid (entity spore:proto): a huge stationary brain of the infection that",
                "commands the infected, detects human intruders, calls its Kommandants to defend it and punishes the ones that",
                "disobey. Infected hits only give the Call of the Hive effect near a Proto, and a Kommandant that is inducted",
                "is carried to one. Several Protos can exist at the same time (several Protohives)."
        ).push("class_gain");
        PROTO_CALL_RADIUS = builder
                .comment("Radius in blocks around a Proto inside which an infected hit can give the Call of the Hive effect, and in which the",
                        "effect and the downed 'join or die' screen keep working (unless call_of_the_hive_persists_without_proto). Default 300.0.")
                .defineInRange("proto_call_radius", 300.0D, 8.0D, 10000.0D);

        builder.comment(
                "When someone becomes a Kommandant (picking the class with the Training Book or /class, or surrendering to the Call of the Hive)",
                "a Verwa carries them underground and sets them down at the hive, where they hatch from a cocoon. With the teleport",
                "turned off they hatch from the cocoon right where they stand."
        ).push("induction_teleport");
        INDUCTION_TELEPORT = builder
                .comment("If true (default), a player who CHOOSES the Kommandant class (Training Book or /class) is carried to the hive.")
                .define("on_class_selection", true);
        SURRENDER_TELEPORT = builder
                .comment("If true (default), a player who gives up to the Call of the Hive is carried to the hive.")
                .define("on_call_of_the_hive", true);
        TELEPORT_TARGET = builder
                .comment("Where they are carried. PROTO_FIRST (default): next to the nearest Proto, else to the structure below, else far from where",
                        "they stood. STRUCTURE_ONLY: ignore Protos and go to the structure (or far away when it is not found).")
                .defineEnum("target", TeleportTarget.PROTO_FIRST);
        TELEPORT_PROTO_SEARCH_RADIUS = builder
                .comment("Radius in blocks around the player in which the nearest Proto is looked for. Default 512.0.")
                .defineInRange("proto_search_radius", 512.0D, 8.0D, 10000.0D);
        TELEPORT_STRUCTURE = builder
                .comment("Structure id used when there is no Proto (the nearest one within 100 chunks of 0,0 is used). Default spore:mass_grave.",
                        "Leave empty to skip the structure step.")
                .define("fallback_structure", "spore:mass_grave");
        TELEPORT_FAR_MIN = builder
                .comment("When there is neither a Proto nor the structure: minimum distance in blocks from where the player stood. Default 850.")
                .defineInRange("far_distance_min", 850, 0, 100000);
        TELEPORT_FAR_MAX = builder
                .comment("Maximum distance in blocks for that case (must not be below the minimum). Default 1300.")
                .defineInRange("far_distance_max", 1300, 0, 100000);
        builder.pop();

        builder.comment("The cocoon a new Kommandant hatches from (class selection and Call of the Hive). It is NOT the evolution cocoon of ability 1.")
                .push("surrender_cocoon");
        SURRENDER_COCOON_SECONDS = builder
                .comment("Incubation time in seconds. Default 15.")
                .defineInRange("seconds", 15, 1, 3600);
        SURRENDER_COCOON_HP = builder
                .comment("Cocoon HP (breaking it interrupts the induction). Default 300.0.")
                .defineInRange("hp", 300.0D, 1.0D, 1000000.0D);
        SURRENDER_REGENERATION_TICKS = builder
                .comment("Regeneration V duration in ticks given when the new Kommandant emerges. Default 200 (10 seconds).")
                .defineInRange("regeneration_ticks", 200, 0, 1000000);
        builder.pop();

        builder.comment("Extra price of choosing the Kommandant class with the Training Book (checked and paid only when the class change succeeds).")
                .push("training_book_requirements");
        BOOK_REQUIRED_ITEMS = builder
                .comment("Items taken from the player's inventory, as modid:item or modid:item;count. Default: none.")
                .defineListAllowEmpty(List.of("required_items"), () -> List.<String>of(), ConfigItems::isValidEntry);
        BOOK_REQUIRED_XP_LEVELS = builder
                .comment("Experience levels taken from the player. Default 0.")
                .defineInRange("required_xp_levels", 0, 0, 1000);
        builder.pop();

        builder.pop();
    }

    // ============================================================================================ accessors: general

    /** Highest evolution level a Kommandant can reach (1-9, default 9). */
    public static int maxEvolutionLevel() {
        try {
            return MAX_EVOLUTION_LEVEL.get();
        } catch (RuntimeException e) {
            return 9;
        }
    }

    /** Evolution level at which ability {@code power} (1-13) unlocks. Default {@code max(0, power - 4)}. */
    public static int unlockLevel(int power) {
        if (power < 1 || power > 13) {
            return Math.max(0, power - 4);
        }
        try {
            return UNLOCK_LEVELS[power].get();
        } catch (RuntimeException e) {
            return defaultUnlockLevel(power);
        }
    }

    /** True when a Kommandant of evolution level {@code level} may use ability {@code power}. */
    public static boolean isUnlocked(int level, int power) {
        return level >= unlockLevel(power);
    }

    /** Biomass bar maximum for a Kommandant of evolution level {@code level} (default 50). */
    public static int biomassBarMax(int level) {
        int index = Math.max(0, Math.min(level, BAR_MAX_BY_LEVEL.length - 1));
        try {
            return BAR_MAX_BY_LEVEL[index].get();
        } catch (RuntimeException e) {
            return 50;
        }
    }

    /**
     * Base biomass cost of ability {@code power} (1-7) for the selector tooltips: 1 = the cost of re-entering the cocoon at max level
     * (normal evolutions use evolution_costs), 2 = extracting from undead, 3 = a mound, 5 = one feeding, 6 = the maximum mutation cost.
     * Abilities 4 and 7 cost nothing (0). Abilities 8-13 are not covered here.
     */
    public static int biomassCost(int power) {
        return switch (power) {
            case 1 -> COCOON_MAX_LEVEL_BIOMASS_COST.get();
            case 2 -> UNDEAD_BIOMASS_COST.get();
            case 3 -> MOUND_BIOMASS_COST.get();
            case 5 -> FEED_BIOMASS_COST.get();
            case 6 -> MUTATION_COST_MAX.get();
            default -> 0;
        };
    }

    // ============================================================================================ accessors: power 1

    /** Cocoon HP for a Kommandant at {@code level} (the selector tooltip uses the same numbers). */
    public static double cocoonHp(int level, boolean maxLevelNoVariant) {
        return maxLevelNoVariant ? COCOON_MAX_LEVEL_HP.get() : (level + 1) * COCOON_HP_PER_LEVEL.get();
    }

    /** Incubation time in seconds for a Kommandant at {@code level} (the selector tooltip uses the same numbers). */
    public static int cocoonSeconds(int level, boolean maxLevelNoVariant) {
        if (maxLevelNoVariant) {
            return COCOON_MAX_LEVEL_SECONDS.get();
        }
        return COCOON_BASE_SECONDS.get() + (COCOON_STEP_SECONDS.get() * (level / COCOON_LEVELS_PER_STEP.get()));
    }

    // ============================================================================================ accessors: power 2

    /** Seconds of Mycelium infection a melee hit gives at evolution {@code level} (the selector tooltip shows the same number). */
    public static int myceliumSeconds(int level) {
        return Math.max(level * MYCELIUM_SECONDS_PER_LEVEL.get(), MYCELIUM_MIN_SECONDS.get());
    }

    /** Mycelium level minus 1 of a melee hit at evolution {@code level} (0 = Mycelium I). */
    public static int myceliumAmplifier(int level) {
        return level / MYCELIUM_LEVELS_PER_AMPLIFIER.get();
    }

    /** HP limit under which {@code target} can be extracted (the target must have strictly fewer HP). */
    public static float extractionHealthLimit(LivingEntity target) {
        if (EXTRACTION_THRESHOLD_MODE.get() == ThresholdMode.PERCENT) {
            return (float) (EXTRACTION_THRESHOLD_PERCENT.get() * target.getMaxHealth());
        }
        return EXTRACTION_THRESHOLD_FLAT.get().floatValue();
    }

    /** True when {@code target} has few enough HP to be extracted (also what makes Devour refuse it). */
    public static boolean isBelowExtractionThreshold(LivingEntity target) {
        return target.getHealth() < extractionHealthLimit(target);
    }

    /** First extra extraction entry for this entity type, or empty. Parsed at use time so config edits apply live. */
    public static Optional<ExtraExtraction> findExtraExtraction(ResourceLocation entityId) {
        List<? extends String> raw = EXTRA_EXTRACTIONS.get();
        if (raw == null || entityId == null) {
            return Optional.empty();
        }
        for (String line : raw) {
            if (line == null || !EXTRACTION_SYNTAX.matcher(line).matches()) {
                continue;
            }
            String[] parts = line.trim().split(";");
            ResourceLocation entity = ResourceLocation.tryParse(parts[0].trim().toLowerCase(Locale.ROOT));
            if (entity == null || !entity.equals(entityId)) {
                continue;
            }
            String itemId = parts[1].trim().toLowerCase(Locale.ROOT);
            int chance = Math.max(0, Math.min(100, Integer.parseInt(parts[2].trim())));
            String count = parts.length > 3 ? parts[3].trim() : "1";
            Optional<ConfigItems.Entry> item = ConfigItems.parse(itemId + ";" + count);
            if (item.isPresent()) {
                return Optional.of(new ExtraExtraction(entity, item.get(), chance));
            }
        }
        return Optional.empty();
    }

    // ============================================================================================ accessors: powers 6 and 7

    /** Seconds the temporary buffs of a forced mutation last at evolution {@code level} (the selector tooltip shows the same number). */
    public static int mutationBuffSeconds(int level) {
        return MUTATION_BUFF_BASE_SECONDS.get() + level * MUTATION_BUFF_SECONDS_PER_LEVEL.get();
    }

    /** Strength power of a forced mutation at evolution {@code level} (1 = Strength I, 0 = none). */
    public static int mutationStrengthPower(int level) {
        return Math.min(MUTATION_STRENGTH_MAX_POWER.get(), (int) Math.floor(level / (double) MUTATION_STRENGTH_LEVELS_PER_POWER.get()));
    }

    /** Permanent max HP a forced mutation adds at evolution {@code level} (2 HP = 1 heart). */
    public static double mutationExtraHp(int level) {
        return MUTATION_HP_PER_LEVEL.get() * level;
    }

    /** Resistance level minus 1 the mount of a Second Authority rider gets at evolution {@code level} (0 = Resistance I). */
    public static int mountResistanceAmplifier(int level) {
        if (level >= MOUNT_RESISTANCE_TIER2_LEVEL.get()) {
            return MOUNT_RESISTANCE_TIER2_AMPLIFIER.get();
        }
        if (level >= MOUNT_RESISTANCE_TIER1_LEVEL.get()) {
            return MOUNT_RESISTANCE_TIER1_AMPLIFIER.get();
        }
        return MOUNT_RESISTANCE_BASE_AMPLIFIER.get();
    }

    // ============================================================================================ accessors: lists and values

    private static final IdSetCache BASIC_CACHE = new IdSetCache();
    private static final IdSetCache STRONG_CACHE = new IdSetCache();
    private static final IdSetCache REMAINS_CACHE = new IdSetCache();
    private static final IdSetCache BULB_CACHE = new IdSetCache();
    private static final IdSetCache HARVESTABLE_CACHE = new IdSetCache();
    private static final IdSetCache FEEDABLE_CACHE = new IdSetCache();
    private static final IdSetCache EVOLVABLE_CACHE = new IdSetCache();
    private static final IdSetCache HYPER_CACHE = new IdSetCache();
    private static final IdSetCache FLYING_CACHE = new IdSetCache();
    private static final IdSetCache AQUATIC_CACHE = new IdSetCache();
    private static final IdSetCache BLACKLIST_CACHE = new IdSetCache();

    public static boolean isBasicDevourEntity(ResourceLocation id) {
        return BASIC_CACHE.contains(DEVOUR_BASIC_ENTITIES.get(), id);
    }

    public static boolean isStrongDevourEntity(ResourceLocation id) {
        return STRONG_CACHE.contains(DEVOUR_STRONG_ENTITIES.get(), id);
    }

    public static boolean isRemainsBlock(ResourceLocation id) {
        return REMAINS_CACHE.contains(DEVOUR_REMAINS_BLOCKS.get(), id);
    }

    public static boolean isBulbBlock(ResourceLocation id) {
        return BULB_CACHE.contains(DEVOUR_BULB_BLOCKS.get(), id);
    }

    public static boolean isHarvestableBlock(ResourceLocation id) {
        return HARVESTABLE_CACHE.contains(HARVESTABLE_BLOCKS.get(), id);
    }

    public static boolean isFeedableBlock(ResourceLocation id) {
        return FEEDABLE_CACHE.contains(FEEDABLE_BLOCKS.get(), id);
    }

    public static boolean isEvolvableMob(String id) {
        return EVOLVABLE_CACHE.contains(MUTATION_EVOLVABLE_MOBS.get(), id);
    }

    public static boolean isHyperEvolvableMob(String id) {
        return HYPER_CACHE.contains(MUTATION_HYPER_EVOLVABLE_MOBS.get(), id);
    }

    public static boolean isFlyingMount(String id) {
        return FLYING_CACHE.contains(MOUNT_FLYING_MOBS.get(), id);
    }

    public static boolean isAquaticMount(String id) {
        return AQUATIC_CACHE.contains(MOUNT_AQUATIC_MOBS.get(), id);
    }

    public static boolean isBlacklistedMount(String id) {
        return BLACKLIST_CACHE.contains(MOUNT_BLACKLISTED_MOBS.get(), id);
    }

    /** Inclusive random range that tolerates min &gt; max (the larger value wins). */
    public static int rollRange(net.minecraft.util.RandomSource random, int min, int max) {
        int hi = Math.max(min, max);
        return hi <= min ? min : random.nextIntBetweenInclusive(min, hi);
    }

    /** Biomass a kill of {@code entityId} gives, if the override list names it (a fractional amount, may be 0). */
    public static Optional<Double> killBiomassOverride(ResourceLocation entityId) {
        List<? extends String> raw = KILL_OVERRIDES.get();
        if (raw == null || entityId == null) {
            return Optional.empty();
        }
        for (String line : raw) {
            if (line == null || !KILL_OVERRIDE_SYNTAX.matcher(line).matches()) {
                continue;
            }
            String[] parts = line.trim().split(";");
            ResourceLocation id = ResourceLocation.tryParse(parts[0].trim().toLowerCase(Locale.ROOT));
            if (id != null && id.equals(entityId)) {
                try {
                    return Optional.of(Double.parseDouble(parts[1].trim()));
                } catch (NumberFormatException ignored) {
                    return Optional.empty();
                }
            }
        }
        return Optional.empty();
    }

    // ============================================================================================ colours

    /** Parses a {@code RRGGBB} (or {@code #RRGGBB}) string; {@code fallback} when it is not valid. */
    public static int colorRgb(ForgeConfigSpec.ConfigValue<String> value, int fallback) {
        String raw;
        try {
            raw = value.get();
        } catch (RuntimeException e) {
            return fallback;
        }
        if (raw == null) {
            return fallback;
        }
        String s = raw.trim();
        if (s.startsWith("#")) {
            s = s.substring(1);
        }
        if (s.length() != 6) {
            warnOnce("colour '" + raw + "' (expected 6 hex digits, RRGGBB)");
            return fallback;
        }
        try {
            return Integer.parseInt(s, 16) & 0xFFFFFF;
        } catch (NumberFormatException e) {
            warnOnce("colour '" + raw + "' (expected 6 hex digits, RRGGBB)");
            return fallback;
        }
    }

    /** Same as {@link #colorRgb} but as {@code {r, g, b, 1}} floats for the X-ray line renderer. */
    public static float[] colorFloats(ForgeConfigSpec.ConfigValue<String> value, int fallback) {
        int rgb = colorRgb(value, fallback);
        return new float[]{((rgb >> 16) & 0xFF) / 255.0F, ((rgb >> 8) & 0xFF) / 255.0F, (rgb & 0xFF) / 255.0F, 1.0F};
    }

    private static void warnOnce(String what) {
        if (WARNED.add(what)) {
            LOGGER.warn("[SporeAdds config] ignoring {}", what);
        }
    }

    // ============================================================================================ helpers

    /** Remembers the parsed form of a config list until the list instance changes (config reload). */
    private static final class IdSetCache {
        private List<?> source;
        private Set<String> ids = Set.of();

        boolean contains(List<? extends String> raw, ResourceLocation id) {
            return id != null && contains(raw, id.toString());
        }

        synchronized boolean contains(List<? extends String> raw, String id) {
            if (id == null) {
                return false;
            }
            if (raw != source) {
                Set<String> out = new HashSet<>();
                if (raw != null) {
                    for (String entry : raw) {
                        if (entry == null) {
                            continue;
                        }
                        ResourceLocation parsed = ResourceLocation.tryParse(entry.trim().toLowerCase(Locale.ROOT));
                        if (parsed != null) {
                            out.add(parsed.toString());
                        }
                    }
                }
                ids = out;
                source = raw;
            }
            return ids.contains(id);
        }
    }
}
