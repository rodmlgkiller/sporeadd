package com.sporeadds.sporeaddsmod.config;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.ArrayList;
import java.util.List;

/**
 * Kommandant abilities 8-13 (horde, infectious burst, proto tracking, vigil, biomass armor, nuke).
 *
 * <p>Defaults reproduce the behaviour that was hard-coded before these options existed. Read values with
 * {@code .get()} at USE time (never copy them into static finals: the config is not loaded yet while classes
 * initialise). Called from {@link SporeAddsConfig}'s static initialiser, before the spec is built.
 *
 * <p>Effect lists use the shared {@link ConfigEffects} format {@code modid:effect;durationTicks;amplifier}.
 * The existing {@code nuclear_powers} section of {@link SporeAddsConfig} (termina duration, level penalty, effect
 * levels of the nuke) is still the source of truth for those numbers and is read by the nuke code next to the options
 * defined here.
 */
public final class KommandantAdvancedConfig {
    private KommandantAdvancedConfig() {
    }

    /** What happens to the armor a player is already wearing when Biomass Armor (power 12) is activated. */
    public enum PreviousArmorMode {
        /** Thrown on the ground (the original behaviour). */
        DROP,
        /** Moved to the inventory; only dropped when the inventory is full. */
        INVENTORY
    }

    // ------------------------------------------------------------------ power 8: horde
    public static ForgeConfigSpec.IntValue HORDE_BIOMASS_COST;
    public static ForgeConfigSpec.ConfigValue<List<? extends Number>> HORDE_LEVEL_BUDGETS;
    public static ForgeConfigSpec.IntValue HORDE_BASE_VERWAS;
    public static ForgeConfigSpec.IntValue HORDE_MAX_VERWAS;
    public static ForgeConfigSpec.IntValue HORDE_VERWAS_PER_PROTO;
    public static ForgeConfigSpec.IntValue HORDE_BUDGET_PER_PROTO;
    public static ForgeConfigSpec.IntValue HORDE_BUDGET_PER_PROTO_MAXED;
    public static ForgeConfigSpec.IntValue HORDE_VERWA_CAP_MIN;
    public static ForgeConfigSpec.IntValue HORDE_VERWA_CAP_MAX;
    public static ForgeConfigSpec.IntValue HORDE_VANGUARD_CAP;
    public static ForgeConfigSpec.DoubleValue HORDE_SPAWN_RADIUS;
    public static ForgeConfigSpec.IntValue TRANSPORT_BIOMASS_COST;
    public static ForgeConfigSpec.IntValue TRANSPORT_WAIT_TICKS;
    public static ForgeConfigSpec.DoubleValue TRANSPORT_STAY_RADIUS;
    public static ForgeConfigSpec.IntValue ABYSSAL_NAIAD_BASE;
    public static ForgeConfigSpec.IntValue ABYSSAL_NAIAD_PER_PROTO;
    public static ForgeConfigSpec.DoubleValue ABYSSAL_NAIAD_SCALE;
    public static ForgeConfigSpec.ConfigValue<List<? extends Number>> ABYSSAL_NAIAD_HEALTH;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> ABYSSAL_NAIAD_EFFECTS;

    // ------------------------------------------------------------------ power 9: infectious burst
    public static ForgeConfigSpec.IntValue BURST_BIOMASS_COST;
    public static ForgeConfigSpec.DoubleValue BURST_RADIUS;
    public static ForgeConfigSpec.IntValue BURST_DURATION_BASE_SECONDS;
    public static ForgeConfigSpec.IntValue BURST_DURATION_PER_LEVEL_SECONDS;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> BURST_EFFECTS_ALL;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> BURST_EFFECTS_ENEMIES;
    public static ForgeConfigSpec.IntValue BURST_SPREAD_PASSES;
    public static ForgeConfigSpec.DoubleValue BURST_SPREAD_RADIUS;
    public static ForgeConfigSpec.BooleanValue BURST_CONVERT_BULBS;

    public static ForgeConfigSpec.BooleanValue EXECUTION_ENABLED;
    public static ForgeConfigSpec.DoubleValue EXECUTION_BASE_THRESHOLD;
    public static ForgeConfigSpec.DoubleValue EXECUTION_THRESHOLD_PER_AMPLIFIER;
    public static ForgeConfigSpec.DoubleValue EXECUTION_ESSENCE_CHANCE;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> EXECUTION_EFFECTS;

    public static ForgeConfigSpec.DoubleValue CALL_RADIUS;
    public static ForgeConfigSpec.DoubleValue CALL_DEBUFF_RADIUS;
    public static ForgeConfigSpec.IntValue CALL_MAX_TARGETS;
    public static ForgeConfigSpec.IntValue CALL_DURATION_TICKS;
    public static ForgeConfigSpec.DoubleValue CALL_MOVE_SPEED;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> CALL_WEAKNESS_EFFECTS;
    public static ForgeConfigSpec.DoubleValue CALL_ESSENCE_CHANCE;

    public static ForgeConfigSpec.DoubleValue VORTEX_RADIUS;
    public static ForgeConfigSpec.IntValue VORTEX_SLOW_TICKS;
    public static ForgeConfigSpec.DoubleValue VORTEX_SLOW_BASE;
    public static ForgeConfigSpec.DoubleValue VORTEX_SLOW_PER_LEVEL;

    // ------------------------------------------------------------------ power 10: proto tracking
    public static ForgeConfigSpec.IntValue SONAR_BIOMASS_COST;
    public static ForgeConfigSpec.DoubleValue SONAR_PROTO_BASE_RANGE;
    public static ForgeConfigSpec.DoubleValue SONAR_PROTO_RANGE_PER_PROTO;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> SONAR_TARGET_EFFECTS;
    public static ForgeConfigSpec.BooleanValue SONAR_SPAWN_VIGIL;
    public static ForgeConfigSpec.IntValue SONAR_VIGIL_MIN_DISTANCE;
    public static ForgeConfigSpec.IntValue SONAR_VIGIL_MAX_DISTANCE;

    // ------------------------------------------------------------------ power 11: vigil
    public static ForgeConfigSpec.IntValue VIGIL_BIOMASS_COST;
    public static ForgeConfigSpec.DoubleValue VIGIL_PLACEMENT_RANGE;
    public static ForgeConfigSpec.DoubleValue VIGIL_HEALTH;
    public static ForgeConfigSpec.DoubleValue VIGIL_HEALTH_PER_PROTO;
    public static ForgeConfigSpec.DoubleValue VIGIL_ARMOR;
    public static ForgeConfigSpec.DoubleValue VIGIL_SCALE_BASE;
    public static ForgeConfigSpec.DoubleValue VIGIL_SCALE_PER_PROTO;
    public static ForgeConfigSpec.IntValue VIGIL_REGEN_AMPLIFIER;
    public static ForgeConfigSpec.DoubleValue VIGIL_FREEZE_MULTIPLIER;
    public static ForgeConfigSpec.IntValue VIGIL_LIFETIME_TICKS;
    public static ForgeConfigSpec.IntValue VIGIL_ALERT_MIN_LEVEL;
    public static ForgeConfigSpec.IntValue VIGIL_SCAN_INITIAL_RADIUS;
    public static ForgeConfigSpec.IntValue VIGIL_SCAN_GROWTH;
    public static ForgeConfigSpec.IntValue VIGIL_SCAN_MAX_RADIUS;
    public static ForgeConfigSpec.IntValue VIGIL_SCAN_INTERVAL_TICKS;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> VIGIL_SCAN_EFFECTS;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> VIGIL_SCAN_EFFECTS_NO_PARTICLES;
    public static ForgeConfigSpec.DoubleValue VIGIL_SCAN_EFFECT_CHANCE;
    public static ForgeConfigSpec.IntValue VIGIL_REPORT_TICKS;
    public static ForgeConfigSpec.DoubleValue VIGIL_JAM_CHANCE;
    public static ForgeConfigSpec.IntValue VIGIL_JAM_INTERVAL_TICKS;
    public static ForgeConfigSpec.DoubleValue VIGIL_MOUND_HEALTH;
    public static ForgeConfigSpec.DoubleValue VIGIL_MOUND_ARMOR;

    // ------------------------------------------------------------------ power 12: biomass armor
    public static ForgeConfigSpec.IntValue ARMOR_BIOMASS_COST;
    public static ForgeConfigSpec.IntValue ARMOR_MAX_HP;
    public static ForgeConfigSpec.IntValue ARMOR_MAX_HP_ABYSSAL;
    public static ForgeConfigSpec.IntValue ARMOR_MAX_HP_GLUTTONOUS;
    public static ForgeConfigSpec.IntValue ARMOR_MAX_HP_CAUSTIC;
    public static ForgeConfigSpec.DoubleValue ARMOR_DAMAGE_REDUCTION;
    public static ForgeConfigSpec.BooleanValue ARMOR_VANILLA_REDUCTION;
    public static ForgeConfigSpec.DoubleValue ARMOR_RESISTANCE_PER_LEVEL;
    public static ForgeConfigSpec.DoubleValue ARMOR_CUISINE_MULTIPLIER;
    public static ForgeConfigSpec.IntValue ARMOR_MIN_DAMAGE;
    public static ForgeConfigSpec.BooleanValue ARMOR_EQUIP_PIECES;
    public static ForgeConfigSpec.EnumValue<PreviousArmorMode> ARMOR_PREVIOUS_MODE;
    public static ForgeConfigSpec.DoubleValue ARMOR_KNOCKBACK_RESISTANCE;
    public static ForgeConfigSpec.IntValue ARMOR_REGEN_PER_SECOND;
    public static ForgeConfigSpec.DoubleValue ARMOR_ABYSSAL_DRY_MULTIPLIER;
    public static ForgeConfigSpec.IntValue ARMOR_REGEN_EFFECT_BONUS;
    public static ForgeConfigSpec.IntValue ARMOR_PCI_DAMAGE_PER_CHARGE;
    public static ForgeConfigSpec.DoubleValue ARMOR_PCI_MAX_FRACTION;
    public static ForgeConfigSpec.IntValue ARMOR_PCI_FREEZE_TICKS;
    public static ForgeConfigSpec.IntValue ARMOR_PCI_COOLDOWN_PER_CHARGE;
    public static ForgeConfigSpec.IntValue ARMOR_PCI_MIN_COOLDOWN;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> ARMOR_ABYSSAL_EFFECTS;
    public static ForgeConfigSpec.IntValue GLUTTONOUS_FAMINED_CAP;
    public static ForgeConfigSpec.IntValue GLUTTONOUS_HELM_BREAK_HP;
    public static ForgeConfigSpec.IntValue GLUTTONOUS_HELM_RESTORE_TICKS;
    public static ForgeConfigSpec.DoubleValue GLUTTONOUS_BROKEN_HELM_MULTIPLIER;
    public static ForgeConfigSpec.IntValue GLUTTONOUS_MID_HP;
    public static ForgeConfigSpec.IntValue GLUTTONOUS_LOW_HP;
    public static ForgeConfigSpec.IntValue GLUTTONOUS_REGEN_HIGH;
    public static ForgeConfigSpec.IntValue GLUTTONOUS_REGEN_MID;
    public static ForgeConfigSpec.IntValue GLUTTONOUS_REGEN_LOW;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> GLUTTONOUS_SLOW_EFFECTS;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> GLUTTONOUS_MID_EFFECTS;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> GLUTTONOUS_LOW_EFFECTS;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> GLUTTONOUS_LOW_EFFECTS_NO_PARTICLES;
    public static ForgeConfigSpec.IntValue GLUTTONOUS_SCENT_COOLDOWN;
    public static ForgeConfigSpec.IntValue GLUTTONOUS_SCENT_COOLDOWN_LOW;

    // ------------------------------------------------------------------ power 13: nuke
    public static ForgeConfigSpec.IntValue NUKE_BIOMASS_COST;
    public static ForgeConfigSpec.IntValue NUKE_SPAWN_HEIGHT;
    public static ForgeConfigSpec.IntValue NUKE_DETONATION_DELAY_TICKS;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> NUKE_PLAYER_EFFECTS;
    public static ForgeConfigSpec.DoubleValue NUKE_EXPLOSION_RADIUS;
    public static ForgeConfigSpec.BooleanValue NUKE_EXPLOSION_FIRE;
    public static ForgeConfigSpec.BooleanValue NUKE_EXPLOSION_BREAKS_BLOCKS;
    public static ForgeConfigSpec.DoubleValue NUKE_DAMAGE_RADIUS;
    public static ForgeConfigSpec.DoubleValue NUKE_DAMAGE;
    public static ForgeConfigSpec.DoubleValue NUKE_KNOCKBACK_HORIZONTAL;
    public static ForgeConfigSpec.DoubleValue NUKE_KNOCKBACK_VERTICAL;
    public static ForgeConfigSpec.IntValue NUKE_BLAST_EFFECT_TICKS;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> NUKE_BLAST_EXTRA_EFFECTS;
    public static ForgeConfigSpec.IntValue NUKE_CLOUD_LIFETIME_TICKS;
    public static ForgeConfigSpec.DoubleValue NUKE_CLOUD_START_RADIUS;
    public static ForgeConfigSpec.DoubleValue NUKE_CLOUD_SHRINK;
    public static ForgeConfigSpec.DoubleValue NUKE_CLOUD_HEIGHT;
    public static ForgeConfigSpec.IntValue NUKE_CLOUD_EFFECT_TICKS;
    public static ForgeConfigSpec.IntValue NUKE_CLOUD_SPREAD_CYCLES;
    public static ForgeConfigSpec.IntValue NUKE_CLOUD_SPREAD_INTERVAL_TICKS;
    public static ForgeConfigSpec.DoubleValue NUKE_CLOUD_ACID_DENSITY;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> NUKE_CLOUD_EXTRA_EFFECTS;

    public static ForgeConfigSpec.IntValue STORM_DURATION_TICKS;
    public static ForgeConfigSpec.DoubleValue STORM_LIGHTNING_DAMAGE;
    public static ForgeConfigSpec.DoubleValue STORM_LIGHTNING_RADIUS;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> STORM_RAIN_EFFECTS;

    public static ForgeConfigSpec.DoubleValue DOME_MAX_RADIUS;
    public static ForgeConfigSpec.IntValue DOME_GROW_TICKS;
    public static ForgeConfigSpec.IntValue DOME_SHRINK_TICKS_PER_BLOCK;
    public static ForgeConfigSpec.DoubleValue DOME_MOUND_HEALTH;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> DOME_EFFECTS;

    public static ForgeConfigSpec.IntValue GLUTTONOUS_NUKE_FAMINED_TICKS;
    public static ForgeConfigSpec.DoubleValue GLUTTONOUS_NUKE_RADIUS;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> GLUTTONOUS_NUKE_EFFECTS;

    static void define(ForgeConfigSpec.Builder builder) {
        builder.comment(
                "Kommandant abilities 8-13: horde, infectious burst, proto tracking, vigil, biomass armor and nuke.",
                "Every default reproduces the behaviour the mod always had.",
                "Effect lists use the format modid:effect;durationTicks;amplifier (20 ticks = 1 second, amplifier 0 = level I)."
        ).push("kommandant_abilities_advanced");

        // ============================================================ power 8
        builder.comment("Power 8 - Summon the Horde (Verwas that erupt into infected, paid from a biomass budget).")
                .push("power_8_horde");

        HORDE_BIOMASS_COST = builder
                .comment("Biomass spent per use of the Horde (both the normal and the Abyssal version). Default 15.")
                .defineInRange("biomass_cost", 15, 0, 100000);

        HORDE_LEVEL_BUDGETS = builder
                .comment("Biomass budget of the horde per Kommandant level, spent on the mobs of verwa_horde_pool.",
                        "First entry = level 0, second = level 1 ... tenth = level 9 (the maximum level). By default the ability",
                        "needs level 4 (kommandant_abilities.unlock_levels.power_8), so the first four entries only matter if",
                        "you lower that unlock level.",
                        "Default: 7, 7, 7, 7, 11, 15, 18, 22, 26, 30. An empty list restores the defaults.")
                .defineListAllowEmpty(List.of("level_budgets"), () -> List.of(7, 7, 7, 7, 11, 15, 18, 22, 26, 30),
                        o -> o instanceof Number);

        HORDE_BASE_VERWAS = builder
                .comment("Number of Verwas summoned with no Protohive alive. Default 6.")
                .defineInRange("base_verwas", 6, 0, 100);

        HORDE_MAX_VERWAS = builder
                .comment("Maximum number of Verwas of one horde (Protohives cannot add more). Default 10.")
                .defineInRange("max_verwas", 10, 1, 200);

        HORDE_VERWAS_PER_PROTO = builder
                .comment("Extra Verwas gained for every Protohive alive in the world (up to max_verwas). Default 1.")
                .defineInRange("verwas_per_proto", 1, 0, 100);

        HORDE_BUDGET_PER_PROTO = builder
                .comment("Extra biomass budget gained for every Protohive alive while the Verwa count is not yet at",
                        "max_verwas. Default 7.")
                .defineInRange("budget_per_proto", 7, 0, 10000);

        HORDE_BUDGET_PER_PROTO_MAXED = builder
                .comment("Extra biomass budget gained for every Protohive once the Verwa count is already at max_verwas.",
                        "Default 5.")
                .defineInRange("budget_per_proto_when_verwas_maxed", 5, 0, 10000);

        HORDE_VERWA_CAP_MIN = builder
                .comment("Each Verwa gets a random cap on how much of the budget it can swallow, between this value",
                        "and verwa_cost_cap_max (both inclusive, in biomass points). Default 5.")
                .defineInRange("verwa_cost_cap_min", 5, 1, 1000);

        HORDE_VERWA_CAP_MAX = builder
                .comment("Upper end of the random per-Verwa cost cap (see verwa_cost_cap_min). Default 9.")
                .defineInRange("verwa_cost_cap_max", 9, 1, 1000);

        HORDE_VANGUARD_CAP = builder
                .comment("One random Verwa of every horde is the 'vanguard' and gets this cost cap instead (the biggest",
                        "mob of the horde). Default 10.")
                .defineInRange("vanguard_cost_cap", 10, 1, 1000);

        HORDE_SPAWN_RADIUS = builder
                .comment("The Verwas erupt at a random spot up to this many blocks (each horizontal axis) around the",
                        "caster. Default 15.0.")
                .defineInRange("spawn_radius", 15.0D, 1.0D, 256.0D);

        builder.comment("Verwa Transport (the /vervarequest command; the same countdown is used by every Verwa ride, e.g. the",
                        "ones a Protohive sends to fetch humans): the player waits in place, then a Verwa carries them away.")
                .push("transport");

        TRANSPORT_BIOMASS_COST = builder
                .comment("Biomass the /vervarequest command costs. Default 5.")
                .defineInRange("biomass_cost", 5, 0, 100000);

        TRANSPORT_WAIT_TICKS = builder
                .comment("Countdown in ticks before the transport Verwa emerges (the player has to stay in the marked",
                        "circle meanwhile). Applies to every Verwa transport. Default 500 (25 s).")
                .defineInRange("wait_ticks", 500, 20, 72000);

        TRANSPORT_STAY_RADIUS = builder
                .comment("Blocks the player may drift from where the countdown started; further away when it ends and",
                        "the transport is cancelled. It is also the radius of the marked circle. Default 5.0.")
                .defineInRange("stay_radius", 5.0D, 1.0D, 64.0D);

        builder.pop();

        builder.comment("Abyssal Horde: instead of Verwas, the Abyssal Kommandant calls Triton naiads in dark water.")
                .push("abyssal");

        ABYSSAL_NAIAD_BASE = builder
                .comment("Naiads summoned with no Protohive alive. Default 2.")
                .defineInRange("naiad_base_count", 2, 0, 100);

        ABYSSAL_NAIAD_PER_PROTO = builder
                .comment("Extra naiads for every Protohive alive in the world. Default 2.")
                .defineInRange("naiad_per_proto", 2, 0, 100);

        ABYSSAL_NAIAD_SCALE = builder
                .comment("Size multiplier of the summoned naiads. Default 1.3.")
                .defineInRange("naiad_scale", 1.3D, 0.1D, 10.0D);

        ABYSSAL_NAIAD_HEALTH = builder
                .comment("Max health of each summoned naiad per Kommandant level (first entry = level 0 ... tenth = level 9).",
                        "Default: 20, 25, 30, 40, 50, 65, 80, 100, 120, 150. An empty list restores the defaults.")
                .defineListAllowEmpty(List.of("naiad_health_by_level"), () -> List.of(20, 25, 30, 40, 50, 65, 80, 100, 120, 150),
                        o -> o instanceof Number);

        ABYSSAL_NAIAD_EFFECTS = builder
                .comment("Effects every summoned naiad receives (no potion particles).",
                        "Default: Resistance III for 30 s and Marker VI for 100 s.")
                .defineListAllowEmpty(List.of("naiad_effects"),
                        () -> List.of("minecraft:resistance;600;2", "spore:marker;2000;5"), ConfigEffects::isValidEntry);

        builder.pop();
        builder.pop();

        // ============================================================ power 9
        builder.comment("Power 9 - Infectious Burst (spore explosion around the caster).")
                .push("power_9_infectious_burst");

        BURST_BIOMASS_COST = builder
                .comment("Biomass spent per use of the Infectious Burst (every variant). Default 5.")
                .defineInRange("biomass_cost", 5, 0, 100000);

        BURST_RADIUS = builder
                .comment("Radius in blocks of the standard (subclass-less) burst: the entities it affects and the biomass",
                        "bulbs that become fang lumps. The infection foliage has its own radius (foliage_spread_radius).",
                        "Default 17.0.")
                .defineInRange("radius", 17.0D, 1.0D, 256.0D);

        BURST_DURATION_BASE_SECONDS = builder
                .comment("Base duration in SECONDS of the burst effects that use the level-scaled duration (a duration of 0",
                        "in the effect lists below). Total = base + per_level * Kommandant level. Default 30.")
                .defineInRange("duration_base_seconds", 30, 0, 100000);

        BURST_DURATION_PER_LEVEL_SECONDS = builder
                .comment("Seconds added to the level-scaled burst duration per Kommandant level. Default 5.")
                .defineInRange("duration_per_level_seconds", 5, 0, 100000);

        BURST_EFFECTS_ALL = builder
                .comment("Effects applied to EVERY living entity in range except the caster (allies included).",
                        "A duration of 0 means 'use the level-scaled duration' (base + per_level * level seconds).",
                        "Default: Mycelium Infection III.")
                .defineListAllowEmpty(List.of("effects_all"), () -> List.of("spore:mycelium_ef;0;2"),
                        ConfigEffects::isValidEntry);

        BURST_EFFECTS_ENEMIES = builder
                .comment("Effects applied only to entities that are NOT on the 'spore' team (same 0 = level-scaled",
                        "duration rule). Default: Exposed II.")
                .defineListAllowEmpty(List.of("effects_enemies"), () -> List.of("sporeadd:exposed;0;1"),
                        ConfigEffects::isValidEntry);

        BURST_SPREAD_PASSES = builder
                .comment("How many times the burst spreads infection foliage around the caster (0 = none). Default 20.")
                .defineInRange("foliage_spread_passes", 20, 0, 1000);

        BURST_SPREAD_RADIUS = builder
                .comment("Radius in blocks of every foliage spread pass. Default 15.0.")
                .defineInRange("foliage_spread_radius", 15.0D, 1.0D, 128.0D);

        BURST_CONVERT_BULBS = builder
                .comment("If true, biomass bulbs inside the burst radius turn into fang lumps. Default true.")
                .define("convert_biomass_bulbs", true);

        builder.comment("Exposed effect (applied by the burst and by the nuke cloud): a target that has it and falls",
                        "to or below a health threshold is infected and killed instantly.")
                .push("exposed_execution");

        EXECUTION_ENABLED = builder
                .comment("If false, Exposed never executes anyone (it becomes a plain marker). Default true.")
                .define("enabled", true);

        EXECUTION_BASE_THRESHOLD = builder
                .comment("Health fraction (0.0-1.0 of max health) at or below which a target with Exposed I is executed.",
                        "Default 0.25 (25% of max health).")
                .defineInRange("base_threshold", 0.25D, 0.0D, 1.0D);

        EXECUTION_THRESHOLD_PER_AMPLIFIER = builder
                .comment("Fraction added to the threshold for every Exposed level above I (0.0-1.0). The final threshold is",
                        "capped at 1.0. Default 0.05 (Exposed II = 30%, Exposed V = 45%).")
                .defineInRange("threshold_per_amplifier", 0.05D, 0.0D, 1.0D);

        EXECUTION_ESSENCE_CHANCE = builder
                .comment("Chance (0.0-1.0) that an executed entity drops a Mutation Essence. Default 0.05 (5%).")
                .defineInRange("mutation_essence_chance", 0.05D, 0.0D, 1.0D);

        EXECUTION_EFFECTS = builder
                .comment("Effects applied to the victim right before it is executed. Default: Mycelium Infection I for 30 s.")
                .defineListAllowEmpty(List.of("effects_on_execution"), () -> List.of("spore:mycelium_ef;600;0"),
                        ConfigEffects::isValidEntry);

        builder.pop();

        builder.comment("Gluttonous variant: calls spore creatures to the caster so they can be eaten.")
                .push("gluttonous_call");

        CALL_RADIUS = builder
                .comment("Radius in blocks in which harvestable spore creatures are called. Default 42.0.")
                .defineInRange("radius", 42.0D, 1.0D, 512.0D);

        CALL_DEBUFF_RADIUS = builder
                .comment("Radius in blocks of the opening debuff on everything around the caster that is not on the",
                        "'spore' team. Default 12.0.")
                .defineInRange("debuff_radius", 12.0D, 0.0D, 256.0D);

        CALL_MAX_TARGETS = builder
                .comment("Maximum number of creatures called (the nearest ones). Default 4.")
                .defineInRange("max_targets", 4, 1, 100);

        CALL_DURATION_TICKS = builder
                .comment("How long the called creatures keep walking toward the caster, in ticks. Default 200 (10 s).")
                .defineInRange("duration_ticks", 200, 1, 72000);

        CALL_MOVE_SPEED = builder
                .comment("Walking speed multiplier of the called creatures. Default 1.4.")
                .defineInRange("move_speed", 1.4D, 0.1D, 10.0D);

        CALL_WEAKNESS_EFFECTS = builder
                .comment("Effects applied at the start to everything inside debuff_radius that is not on the 'spore' team.",
                        "Default: Weakness III for 5 s.")
                .defineListAllowEmpty(List.of("debuff_effects"), () -> List.of("minecraft:weakness;100;2"),
                        ConfigEffects::isValidEntry);

        CALL_ESSENCE_CHANCE = builder
                .comment("Chance (0.0-1.0) that a creature eaten by the call drops a Mutation Essence. Default 0.05.")
                .defineInRange("mutation_essence_chance", 0.05D, 0.0D, 1.0D);

        builder.pop();

        builder.comment("Abyssal variant: a vortex that drags everything in the water toward the caster.")
                .push("abyssal_vortex");

        VORTEX_RADIUS = builder
                .comment("Radius in blocks of the vortex. Default 15.0.")
                .defineInRange("radius", 15.0D, 1.0D, 256.0D);

        VORTEX_SLOW_TICKS = builder
                .comment("How long the swim-speed penalty on dragged entities lasts, in ticks. Default 250.")
                .defineInRange("swim_slow_ticks", 250, 1, 72000);

        VORTEX_SLOW_BASE = builder
                .comment("Swim-speed penalty applied to dragged entities (subtracted from their swim speed). Default 0.5.")
                .defineInRange("swim_slow_base", 0.5D, 0.0D, 10.0D);

        VORTEX_SLOW_PER_LEVEL = builder
                .comment("Extra swim-speed penalty for every Kommandant level above 5. Default 0.1.")
                .defineInRange("swim_slow_per_level_above_5", 0.1D, 0.0D, 10.0D);

        builder.pop();
        builder.pop();

        // ============================================================ power 10
        builder.comment("Power 10 - Hive Sonar (proto tracking: reveals the nearest enemy and everyone near a Protohive).")
                .push("power_10_proto_tracking");

        SONAR_BIOMASS_COST = builder
                .comment("Biomass spent per use of the Hive Sonar. Default 25.")
                .defineInRange("biomass_cost", 25, 0, 100000);

        SONAR_PROTO_BASE_RANGE = builder
                .comment("Scan range in blocks of every Protohive (the range is a sphere around each one). Default 250.0.")
                .defineInRange("proto_scan_base_range", 250.0D, 0.0D, 100000.0D);

        SONAR_PROTO_RANGE_PER_PROTO = builder
                .comment("Range in blocks added to every Protohive's scan for each Protohive alive in the world",
                        "(range = base + this * number of Protohives). Default 50.0.")
                .defineInRange("proto_scan_range_per_proto", 50.0D, 0.0D, 100000.0D);

        SONAR_TARGET_EFFECTS = builder
                .comment("Effects applied to every revealed player.",
                        "Default: Glowing for 2 min, Marker III for 2 min and Uneasy for 5 min.")
                .defineListAllowEmpty(List.of("target_effects"),
                        () -> List.of("minecraft:glowing;2400;0", "spore:marker;2400;2", "spore:uneasy;6000;0"),
                        ConfigEffects::isValidEntry);

        SONAR_SPAWN_VIGIL = builder
                .comment("If true, the sonar also spawns a Vigil near the nearest detected player. Default true.")
                .define("spawn_vigil_near_target", true);

        SONAR_VIGIL_MIN_DISTANCE = builder
                .comment("Minimum distance in blocks between the detected player and the Vigil that is spawned. Default 15.")
                .defineInRange("vigil_min_distance", 15, 1, 256);

        SONAR_VIGIL_MAX_DISTANCE = builder
                .comment("Maximum distance in blocks between the detected player and the Vigil that is spawned",
                        "(values below the minimum are treated as the minimum). Default 28.")
                .defineInRange("vigil_max_distance", 28, 1, 256);

        builder.pop();

        // ============================================================ power 11
        builder.comment("Power 11 - Vigil Assault (a stronger Vigil that scans in growing pulses and jams cryo machines).")
                .push("power_11_vigil");

        VIGIL_BIOMASS_COST = builder
                .comment("Biomass spent per use of the Vigil Assault. Default 20.")
                .defineInRange("biomass_cost", 20, 0, 100000);

        VIGIL_PLACEMENT_RANGE = builder
                .comment("The Vigil lands where the caster is looking, up to this many blocks away. Default 150.0.")
                .defineInRange("placement_range", 150.0D, 5.0D, 1000.0D);

        VIGIL_HEALTH = builder
                .comment("Base max health of the summoned Vigil (before the Protohive bonus). Default 120.0.")
                .defineInRange("health", 120.0D, 1.0D, 1000000.0D);

        VIGIL_HEALTH_PER_PROTO = builder
                .comment("Max health added to the Vigil for every Protohive alive in the world. Default 50.0.")
                .defineInRange("health_per_proto", 50.0D, 0.0D, 1000000.0D);

        VIGIL_ARMOR = builder
                .comment("Armor points of the Vigil (Minecraft caps armor at 30). Default 10.0.")
                .defineInRange("armor", 10.0D, 0.0D, 30.0D);

        VIGIL_SCALE_BASE = builder
                .comment("Size multiplier of the Vigil before the Protohive bonus. Default 1.2.")
                .defineInRange("scale", 1.2D, 0.1D, 20.0D);

        VIGIL_SCALE_PER_PROTO = builder
                .comment("Size multiplier added to the Vigil for every Protohive alive. Default 0.2.")
                .defineInRange("scale_per_proto", 0.2D, 0.0D, 10.0D);

        VIGIL_REGEN_AMPLIFIER = builder
                .comment("Level of the permanent Regeneration effect of the Vigil (0 = Regeneration I, 1 = II ...).",
                        "-1 removes it. Default 1 (Regeneration II).")
                .defineInRange("regeneration_amplifier", 1, -1, 255);

        VIGIL_FREEZE_MULTIPLIER = builder
                .comment("Multiplier applied to the freezing damage the Vigil takes (0.0-1.0). Default 0.30 (-70%).")
                .defineInRange("freeze_damage_multiplier", 0.30D, 0.0D, 1.0D);

        VIGIL_LIFETIME_TICKS = builder
                .comment("Ticks the Vigil of this ability stays alive before it disappears. 0 = it never expires",
                        "(the original behaviour). 20 ticks = 1 second.")
                .defineInRange("lifetime_ticks", 0, 0, Integer.MAX_VALUE);

        VIGIL_ALERT_MIN_LEVEL = builder
                .comment("Minimum evolution level (0-9) a Kommandant needs to be alerted - and sent a Verwa transport - when",
                        "ANY Vigil (not only the ones of this power) spots a survivor. Default 7.")
                .defineInRange("sighting_alert_min_level", 7, 0, 9);

        VIGIL_REPORT_TICKS = builder
                .comment("How long the Vigil's scan report stays on the Kommandant's screen, in ticks (CLIENT side value:",
                        "read from the player's own config file). Default 300 (15 s).")
                .defineInRange("report_display_ticks", 300, 20, 72000);

        builder.comment("Scan pulses of the Vigil.").push("scan");

        VIGIL_SCAN_INITIAL_RADIUS = builder
                .comment("Radius in blocks of the very first pulse. Default 15.")
                .defineInRange("initial_radius", 15, 1, 2000);

        VIGIL_SCAN_GROWTH = builder
                .comment("Blocks added to the radius after every pulse. Default 15.")
                .defineInRange("growth_per_pulse", 15, 0, 2000);

        VIGIL_SCAN_MAX_RADIUS = builder
                .comment("The pulse radius never grows beyond this many blocks. Default 150.")
                .defineInRange("max_radius", 150, 1, 4000);

        VIGIL_SCAN_INTERVAL_TICKS = builder
                .comment("Ticks between two pulses. Default 200 (10 s).")
                .defineInRange("interval_ticks", 200, 20, 72000);

        VIGIL_SCAN_EFFECTS = builder
                .comment("Effects every pulse applies to the players and villagers it reaches (Kommandants excluded).",
                        "Default: Marker II for 15 s.")
                .defineListAllowEmpty(List.of("effects"), () -> List.of("spore:marker;300;1"),
                        ConfigEffects::isValidEntry);

        VIGIL_SCAN_EFFECTS_NO_PARTICLES = builder
                .comment("Same as 'effects' but applied without potion particles. Default: Glowing II for 15 s.")
                .defineListAllowEmpty(List.of("effects_no_particles"), () -> List.of("minecraft:glowing;300;1"),
                        ConfigEffects::isValidEntry);

        VIGIL_SCAN_EFFECT_CHANCE = builder
                .comment("Chance (0.0-1.0) that each reached target actually receives the pulse effects. Default 1.0.")
                .defineInRange("effect_chance", 1.0D, 0.0D, 1.0D);

        builder.pop();

        builder.comment("Sabotage of the cryo machines (freezers and C.D.U.s) found by the pulses.").push("sabotage");

        VIGIL_JAM_CHANCE = builder
                .comment("Chance (0.0-1.0) per pulse that a machine that is not being jammed gets a jamming mound",
                        "spawned on top of it. Default 0.30 (30%).")
                .defineInRange("jam_chance", 0.30D, 0.0D, 1.0D);

        VIGIL_JAM_INTERVAL_TICKS = builder
                .comment("Ticks between two jams of the same machine by its mound. Default 500 (25 s).")
                .defineInRange("jam_interval_ticks", 500, 20, 72000);

        VIGIL_MOUND_HEALTH = builder
                .comment("Max health of a jamming mound. Default 10.0.")
                .defineInRange("mound_health", 10.0D, 1.0D, 100000.0D);

        VIGIL_MOUND_ARMOR = builder
                .comment("Armor points of a jamming mound (Minecraft caps armor at 30). Default 30.0.")
                .defineInRange("mound_armor", 30.0D, 0.0D, 30.0D);

        builder.pop();
        builder.pop();

        // ============================================================ power 12
        builder.comment("Power 12 - Biomass Armor (an ablative Armor HP pool that absorbs the damage instead of the player).")
                .push("power_12_biomass_armor");

        ARMOR_BIOMASS_COST = builder
                .comment("Biomass spent per use of the Biomass Armor (every variant). Default 30.")
                .defineInRange("biomass_cost", 30, 0, 100000);

        builder.comment("Armor HP pool per subclass. Using the power refills the pool to this value.").push("hp");

        ARMOR_MAX_HP = builder
                .comment("Armor HP of a Kommandant with no subclass. Default 250.")
                .defineInRange("max_hp", 250, 1, 1000000);

        ARMOR_MAX_HP_ABYSSAL = builder
                .comment("Armor HP of an Abyssal Kommandant. Default 220.")
                .defineInRange("max_hp_abyssal", 220, 1, 1000000);

        ARMOR_MAX_HP_GLUTTONOUS = builder
                .comment("Armor HP of a Gluttonous Kommandant. Default 500.")
                .defineInRange("max_hp_gluttonous", 500, 1, 1000000);

        ARMOR_MAX_HP_CAUSTIC = builder
                .comment("Armor HP of a Caustic Kommandant. Default 180.")
                .defineInRange("max_hp_caustic", 180, 1, 1000000);

        builder.pop();

        builder.comment("How incoming damage is turned into Armor HP loss.").push("damage");

        ARMOR_DAMAGE_REDUCTION = builder
                .comment("Extra damage reduction (0.0-1.0) applied to every hit after all the other reductions below.",
                        "0.0 = none (default), 0.5 = every hit costs half as much Armor HP.")
                .defineInRange("damage_reduction", 0.0D, 0.0D, 1.0D);

        ARMOR_VANILLA_REDUCTION = builder
                .comment("If true, every hit is first reduced by the player's own armor points and toughness (the vanilla",
                        "armor formula; damage that ignores armor is not reduced) before it is subtracted from the Armor HP.",
                        "Default false: the Armor HP takes the raw hit, as it always did.")
                .define("apply_vanilla_armor_reduction", false);

        ARMOR_RESISTANCE_PER_LEVEL = builder
                .comment("Damage reduction (0.0-1.0) per level of the Resistance effect. At 1.0 or more in total the hit",
                        "costs nothing. Default 0.20 (20% per level, so Resistance V makes you immune).")
                .defineInRange("resistance_reduction_per_level", 0.20D, 0.0D, 1.0D);

        ARMOR_CUISINE_MULTIPLIER = builder
                .comment("Multiplier applied to the damage while the player has Exquisite Cuisine. Default 0.8.")
                .defineInRange("exquisite_cuisine_multiplier", 0.8D, 0.0D, 10.0D);

        ARMOR_MIN_DAMAGE = builder
                .comment("Minimum Armor HP a single hit costs. Default 1 (even a scratch costs 1). 0 lets small hits",
                        "cost nothing.")
                .defineInRange("min_damage_per_hit", 1, 0, 1000000);

        builder.pop();

        builder.comment("The armor pieces the power puts on the player.").push("equipment");

        ARMOR_EQUIP_PIECES = builder
                .comment("If true (default) the four Spore armor pieces appear on the player when the power is used and",
                        "vanish when the Armor HP runs out. If false only the Armor HP pool exists and the player's own armor",
                        "is never touched.")
                .define("equip_armor_pieces", true);

        ARMOR_PREVIOUS_MODE = builder
                .comment("What happens to the armor the player was already wearing when the pieces are put on:",
                        "DROP = thrown on the ground (default); INVENTORY = moved to the inventory (dropped only if it is",
                        "full). Also used when the Gluttonous helmet is restored.")
                .defineEnum("previous_armor_mode", PreviousArmorMode.DROP);

        ARMOR_KNOCKBACK_RESISTANCE = builder
                .comment("Knockback resistance (0.0-1.0) granted while the Armor HP lasts. Default 1.0 (100%).")
                .defineInRange("knockback_resistance", 1.0D, 0.0D, 1.0D);

        builder.pop();

        builder.comment("Passive recovery of the Armor HP (it is checked once per second).").push("regeneration");

        ARMOR_REGEN_PER_SECOND = builder
                .comment("Armor HP recovered per second by a Kommandant with no subclass or a Caustic one. Default 5.",
                        "Starvation always disables the recovery.")
                .defineInRange("per_second", 5, 0, 100000);

        ARMOR_ABYSSAL_DRY_MULTIPLIER = builder
                .comment("Multiplier (0.0-1.0) of the recovery of an Abyssal Kommandant while out of water and rain.",
                        "The result is rounded down but never below 1 per second. Default 0.25.")
                .defineInRange("abyssal_dry_multiplier", 0.25D, 0.0D, 1.0D);

        ARMOR_REGEN_EFFECT_BONUS = builder
                .comment("Extra Armor HP per second for every level of the Regeneration effect. Default 2.")
                .defineInRange("regeneration_effect_bonus_per_level", 2, 0, 100000);

        builder.pop();

        builder.comment("Hits from Spore's P.C.I. item against a Kommandant that still has Armor HP.",
                        "damage_per_charge, freeze_ticks and the two cooldown values are also used when a P.C.I. hits a",
                        "Spore-team player that has no Armor HP left.").push("pci");

        ARMOR_PCI_DAMAGE_PER_CHARGE = builder
                .comment("Armor HP a PCI charge removes. Default 6.")
                .defineInRange("damage_per_charge", 6, 1, 100000);

        ARMOR_PCI_MAX_FRACTION = builder
                .comment("A single PCI hit can remove at most this fraction (0.0-1.0) of the target's max Armor HP",
                        "(never less than 1). Default 0.1 (10%).")
                .defineInRange("max_fraction_per_hit", 0.1D, 0.0D, 1.0D);

        ARMOR_PCI_FREEZE_TICKS = builder
                .comment("Freezing ticks the target receives from a PCI hit. Default 600.")
                .defineInRange("freeze_ticks", 600, 0, 100000);

        ARMOR_PCI_COOLDOWN_PER_CHARGE = builder
                .comment("Ticks of PCI cooldown added per charge used. Default 10.")
                .defineInRange("cooldown_ticks_per_charge", 10, 0, 100000);

        ARMOR_PCI_MIN_COOLDOWN = builder
                .comment("Minimum PCI cooldown after a hit, in ticks. Default 20.")
                .defineInRange("min_cooldown_ticks", 20, 0, 100000);

        builder.pop();

        builder.comment("Effects applied by the armor.").push("effects");

        ARMOR_ABYSSAL_EFFECTS = builder
                .comment("Effects an Abyssal Kommandant with Armor HP gets every tick while in water or rain in the dark",
                        "(applied without particles or icon). Default: Resistance II for 2 s.")
                .defineListAllowEmpty(List.of("abyssal_dark_water_effects"), () -> List.of("minecraft:resistance;40;1"),
                        ConfigEffects::isValidEntry);

        builder.pop();

        builder.comment("Gluttonous armor: its numbers are absolute Armor HP values tuned for 500 Armor HP, so adjust them",
                        "together with hp.max_hp_gluttonous.")
                .push("gluttonous");

        GLUTTONOUS_FAMINED_CAP = builder
                .comment("While Famined, the Gluttonous Armor HP cannot be above this value (excess is drained when the",
                        "effect starts). Default 150.")
                .defineInRange("famined_cap", 150, 1, 1000000);

        GLUTTONOUS_HELM_BREAK_HP = builder
                .comment("At or below this Armor HP the helmet shatters (stunning and pushing back everyone around).",
                        "It is restored once the Armor HP is above it. Default 150.")
                .defineInRange("helmet_break_hp", 150, 0, 1000000);

        GLUTTONOUS_HELM_RESTORE_TICKS = builder
                .comment("Ticks the helmet stays broken after the Armor HP went back above the break threshold. Default 100.")
                .defineInRange("helmet_restore_delay_ticks", 100, 0, 72000);

        GLUTTONOUS_BROKEN_HELM_MULTIPLIER = builder
                .comment("Multiplier on the damage taken while the helmet is broken. Default 1.2.")
                .defineInRange("broken_helmet_damage_multiplier", 1.2D, 0.0D, 10.0D);

        GLUTTONOUS_MID_HP = builder
                .comment("Armor HP below which the middle tier starts (hunger effects, faster recovery). Default 300.")
                .defineInRange("mid_tier_hp", 300, 0, 1000000);

        GLUTTONOUS_LOW_HP = builder
                .comment("Armor HP below which the low tier starts (stronger hunger, weakness, fastest recovery,",
                        "faster scent spawning). Default 200.")
                .defineInRange("low_tier_hp", 200, 0, 1000000);

        GLUTTONOUS_REGEN_HIGH = builder
                .comment("Armor HP recovered per second while at or above mid_tier_hp. Default 1.")
                .defineInRange("regen_per_second_high", 1, 0, 100000);

        GLUTTONOUS_REGEN_MID = builder
                .comment("Armor HP recovered per second between low_tier_hp and mid_tier_hp. Default 3.")
                .defineInRange("regen_per_second_mid", 3, 0, 100000);

        GLUTTONOUS_REGEN_LOW = builder
                .comment("Armor HP recovered per second below low_tier_hp. Default 10.")
                .defineInRange("regen_per_second_low", 10, 0, 100000);

        GLUTTONOUS_SLOW_EFFECTS = builder
                .comment("Effects a Gluttonous Kommandant with Armor HP gets every tick (no particles).",
                        "Default: Slowness II for 2 s.")
                .defineListAllowEmpty(List.of("passive_effects"), () -> List.of("minecraft:slowness;40;1"),
                        ConfigEffects::isValidEntry);

        GLUTTONOUS_MID_EFFECTS = builder
                .comment("Effects applied every tick while the Armor HP is in the middle tier. Default: Hunger XVI.")
                .defineListAllowEmpty(List.of("mid_tier_effects"), () -> List.of("minecraft:hunger;25;15"),
                        ConfigEffects::isValidEntry);

        GLUTTONOUS_LOW_EFFECTS = builder
                .comment("Effects applied every tick while the Armor HP is in the low tier. Default: Hunger LXI.")
                .defineListAllowEmpty(List.of("low_tier_effects"), () -> List.of("minecraft:hunger;25;60"),
                        ConfigEffects::isValidEntry);

        GLUTTONOUS_LOW_EFFECTS_NO_PARTICLES = builder
                .comment("Like low_tier_effects but without potion particles. Default: Weakness II.")
                .defineListAllowEmpty(List.of("low_tier_effects_no_particles"), () -> List.of("minecraft:weakness;40;1"),
                        ConfigEffects::isValidEntry);

        GLUTTONOUS_SCENT_COOLDOWN = builder
                .comment("Minimum ticks between two scent spawns triggered by big hits (more than 20 Armor HP at once).",
                        "Default 225.")
                .defineInRange("scent_cooldown_ticks", 225, 0, 72000);

        GLUTTONOUS_SCENT_COOLDOWN_LOW = builder
                .comment("Same cooldown while the Armor HP is in the low tier. Default 125.")
                .defineInRange("scent_cooldown_ticks_low_tier", 125, 0, 72000);

        builder.pop();
        builder.pop();

        // ============================================================ power 13
        builder.comment("Power 13 - Enhanced Tumoroid Nuke. Termina duration, level penalty and the effect levels of the",
                        "blast and cloud are in the existing 'nuclear_powers' section.")
                .push("power_13_nuke");

        NUKE_BIOMASS_COST = builder
                .comment("Biomass spent per use of the Nuke (the Gluttonous version costs none). Default 20.")
                .defineInRange("biomass_cost", 20, 0, 100000);

        NUKE_SPAWN_HEIGHT = builder
                .comment("Blocks above the caster where the nuke appears. Default 50.")
                .defineInRange("spawn_height", 50, 1, 1000);

        NUKE_DETONATION_DELAY_TICKS = builder
                .comment("Ticks between the call and the detonation. Default 200 (10 s).")
                .defineInRange("detonation_delay_ticks", 200, 1, 72000);

        NUKE_PLAYER_EFFECTS = builder
                .comment("Effects the caster gets when the nuke is called. Default: Resistance with amplifier 224 for 15 s",
                        "(survives the blast) and Anticipation for 10 s. Keep the Resistance lasting at least as long as",
                        "detonation_delay_ticks, or the caster will not survive the blast.")
                .defineListAllowEmpty(List.of("caster_effects"),
                        () -> List.of("minecraft:resistance;300;224", "sporeadd:anticipation;200;0"),
                        ConfigEffects::isValidEntry);

        builder.comment("The blast itself.").push("blast");

        NUKE_EXPLOSION_RADIUS = builder
                .comment("Radius in blocks of the explosion that destroys terrain. Default 24.0.")
                .defineInRange("explosion_radius", 24.0D, 0.0D, 256.0D);

        NUKE_EXPLOSION_FIRE = builder
                .comment("If true the explosion sets fires. Default true.")
                .define("explosion_fire", true);

        NUKE_EXPLOSION_BREAKS_BLOCKS = builder
                .comment("If false the explosion does not destroy any block (the damage below still applies).",
                        "Default true.")
                .define("explosion_breaks_blocks", true);

        NUKE_DAMAGE_RADIUS = builder
                .comment("Radius in blocks in which every living entity that is not on the 'spore' team is damaged and",
                        "gets the blast effects. Default 100.0.")
                .defineInRange("damage_radius", 100.0D, 1.0D, 2000.0D);

        NUKE_DAMAGE = builder
                .comment("Damage dealt to every entity inside damage_radius. Default 350.0.")
                .defineInRange("damage", 350.0D, 0.0D, 1000000.0D);

        NUKE_KNOCKBACK_HORIZONTAL = builder
                .comment("Horizontal push strength away from the blast. Default 4.0.")
                .defineInRange("knockback_horizontal", 4.0D, 0.0D, 100.0D);

        NUKE_KNOCKBACK_VERTICAL = builder
                .comment("Vertical push strength. Default 2.0.")
                .defineInRange("knockback_vertical", 2.0D, 0.0D, 100.0D);

        NUKE_BLAST_EFFECT_TICKS = builder
                .comment("Duration in ticks of the Mycelium Infection the blast gives (the Caustic variant has no blast: it",
                        "uses a gas dome). The Termina duration has its own setting in 'nuclear_powers'. Default 6000 (5 min).")
                .defineInRange("effect_ticks", 6000, 1, 10000000);

        NUKE_BLAST_EXTRA_EFFECTS = builder
                .comment("Additional effects applied to everything the blast hits (not the 'spore' team). Default: none.")
                .defineListAllowEmpty(List.of("extra_effects"), () -> List.<String>of(), ConfigEffects::isValidEntry);

        builder.pop();

        builder.comment("The toxic cloud that remains after the blast.").push("cloud");

        NUKE_CLOUD_LIFETIME_TICKS = builder
                .comment("How long the cloud lasts, in ticks. Default 1200 (1 min).")
                .defineInRange("lifetime_ticks", 1200, 20, 1000000);

        NUKE_CLOUD_START_RADIUS = builder
                .comment("Radius in blocks of the cloud when it starts. Default 60.0.")
                .defineInRange("start_radius", 60.0D, 1.0D, 2000.0D);

        NUKE_CLOUD_SHRINK = builder
                .comment("Blocks the radius shrinks over the whole lifetime of the cloud. Default 12.0.")
                .defineInRange("shrink_blocks", 12.0D, 0.0D, 2000.0D);

        NUKE_CLOUD_HEIGHT = builder
                .comment("Vertical reach in blocks (above and below) of the cloud. Default 10.0.")
                .defineInRange("height", 10.0D, 1.0D, 512.0D);

        NUKE_CLOUD_EFFECT_TICKS = builder
                .comment("Duration in ticks of the effects the cloud applies (refreshed every half second). Default 1000.")
                .defineInRange("effect_ticks", 1000, 1, 10000000);

        NUKE_CLOUD_SPREAD_CYCLES = builder
                .comment("How many times the nuke cloud spreads infection foliage. Default 4.")
                .defineInRange("spread_cycles", 4, 0, 1000);

        NUKE_CLOUD_SPREAD_INTERVAL_TICKS = builder
                .comment("Ticks between two spreads. Default 200.")
                .defineInRange("spread_interval_ticks", 200, 1, 100000);

        NUKE_CLOUD_ACID_DENSITY = builder
                .comment("Caustic variant only (the acid pools of its gas dome; the normal nuke cloud spreads infection foliage",
                        "instead): fraction (0.0-1.0) of the ground circle that gets an acid placement attempt every time",
                        "acid is spread. Default 0.10.")
                .defineInRange("acid_density", 0.10D, 0.0D, 1.0D);

        NUKE_CLOUD_EXTRA_EFFECTS = builder
                .comment("Additional effects the cloud applies to everything inside it (not the 'spore' team).",
                        "Default: none.")
                .defineListAllowEmpty(List.of("extra_effects"), () -> List.<String>of(), ConfigEffects::isValidEntry);

        builder.pop();

        builder.comment("Abyssal variant: a storm that rains mycelium and strikes enemies with lightning.")
                .push("abyssal_storm");

        STORM_DURATION_TICKS = builder
                .comment("How long the storm (and the temporary trident of the caster) lasts, in ticks. Default 3600 (3 min).")
                .defineInRange("duration_ticks", 3600, 20, 1000000);

        STORM_LIGHTNING_DAMAGE = builder
                .comment("Damage of each lightning strike. Default 50.0.")
                .defineInRange("lightning_damage", 50.0D, 0.0D, 1000000.0D);

        STORM_LIGHTNING_RADIUS = builder
                .comment("Radius in blocks around the caster in which lightning picks its target. Default 15.0.")
                .defineInRange("lightning_radius", 15.0D, 1.0D, 256.0D);

        STORM_RAIN_EFFECTS = builder
                .comment("Effects applied every second to everything under the open sky around the caster (not the",
                        "'spore' team). Default: Mycelium Infection VI for 30 s.")
                .defineListAllowEmpty(List.of("rain_effects"), () -> List.of("spore:mycelium_ef;600;5"),
                        ConfigEffects::isValidEntry);

        builder.pop();

        builder.comment("Caustic variant: a mound that surrounds itself with a growing dome of corrosive gas.")
                .push("caustic_dome");

        DOME_MAX_RADIUS = builder
                .comment("Radius in blocks the gas dome grows to. Default 50.0.")
                .defineInRange("max_radius", 50.0D, 1.0D, 512.0D);

        DOME_GROW_TICKS = builder
                .comment("Ticks the dome takes to grow to its full radius. Default 400 (20 s).")
                .defineInRange("grow_ticks", 400, 1, 1000000);

        DOME_SHRINK_TICKS_PER_BLOCK = builder
                .comment("Once grown, the dome shrinks by one block every this many ticks. Default 100.")
                .defineInRange("shrink_ticks_per_block", 100, 1, 1000000);

        DOME_MOUND_HEALTH = builder
                .comment("Max health of the mound the dome is anchored to. Default 200.0.")
                .defineInRange("mound_health", 200.0D, 1.0D, 1000000.0D);

        DOME_EFFECTS = builder
                .comment("Effects applied every half second to everything inside the dome.",
                        "Default: Dissolution II and Corrosion VI for 30 s.")
                .defineListAllowEmpty(List.of("effects"),
                        () -> List.of("sporeadd:dissolution;600;1", "spore:corrosion;600;5"),
                        ConfigEffects::isValidEntry);

        builder.pop();

        builder.comment("Gluttonous variant: the Famined state (the Gluttonous nuke costs no biomass).")
                .push("gluttonous");

        GLUTTONOUS_NUKE_FAMINED_TICKS = builder
                .comment("Duration in ticks of the Famined effect the caster gets. Default 2000.")
                .defineInRange("famined_ticks", 2000, 1, 1000000);

        GLUTTONOUS_NUKE_RADIUS = builder
                .comment("Radius in blocks in which nearby players receive the warning effects and sound. Default 100.0.")
                .defineInRange("warning_radius", 100.0D, 1.0D, 2000.0D);

        GLUTTONOUS_NUKE_EFFECTS = builder
                .comment("Effects applied to every player inside warning_radius (the caster included).",
                        "Default: Weakness for 10 s.")
                .defineListAllowEmpty(List.of("warning_effects"), () -> List.of("minecraft:weakness;200;0"),
                        ConfigEffects::isValidEntry);

        builder.pop();
        builder.pop();

        builder.pop();
    }

    // ================================================================== effect helpers

    /** Parses an effect list of this config at use time. */
    public static List<ConfigEffects.Entry> effects(ForgeConfigSpec.ConfigValue<List<? extends String>> value) {
        return ConfigEffects.parseAll(value.get());
    }

    /**
     * Like {@link #effects} but an entry whose duration is {@code 0} gets {@code autoTicks} instead (used by abilities
     * whose duration depends on the Kommandant level).
     */
    public static List<ConfigEffects.Entry> effectsScaled(ForgeConfigSpec.ConfigValue<List<? extends String>> value, int autoTicks) {
        List<ConfigEffects.Entry> out = new ArrayList<>();
        for (String raw : value.get()) {
            if (raw == null || raw.isBlank()) {
                continue;
            }
            String[] parts = raw.trim().split(";");
            if (parts.length >= 2 && parts[1].trim().equals("0")) {
                String amplifier = parts.length > 2 ? parts[2].trim() : "0";
                raw = parts[0].trim() + ";" + Math.max(1, autoTicks) + ";" + amplifier;
            }
            ConfigEffects.parse(raw).ifPresent(out::add);
        }
        return out;
    }

    /** Applies the entries without potion particles ({@code showIcon} tells whether the HUD icon is shown). */
    public static void applyQuiet(LivingEntity target, List<ConfigEffects.Entry> entries, boolean showIcon) {
        for (ConfigEffects.Entry e : entries) {
            target.addEffect(new MobEffectInstance(e.effect(), e.durationTicks(), e.amplifier(), false, false, showIcon));
        }
    }

    // ================================================================== list readers

    private static final int[] DEFAULT_HORDE_BUDGETS = {7, 7, 7, 7, 11, 15, 18, 22, 26, 30};
    private static final double[] DEFAULT_NAIAD_HEALTH = {20.0D, 25.0D, 30.0D, 40.0D, 50.0D, 65.0D, 80.0D, 100.0D, 120.0D, 150.0D};

    private static double numberAt(List<? extends Number> list, int index, double[] fallback) {
        if (list == null || list.isEmpty()) {
            return fallback[Math.max(0, Math.min(index, fallback.length - 1))];
        }
        Number n = list.get(Math.max(0, Math.min(index, list.size() - 1)));
        return n == null ? fallback[Math.max(0, Math.min(index, fallback.length - 1))] : n.doubleValue();
    }

    /** Biomass budget of the horde for a Kommandant level (levels past the end of the list use the last entry). */
    public static int hordeBudgetForLevel(int playerLevel) {
        double[] fallback = new double[DEFAULT_HORDE_BUDGETS.length];
        for (int i = 0; i < fallback.length; i++) {
            fallback[i] = DEFAULT_HORDE_BUDGETS[i];
        }
        return Math.max(0, (int) numberAt(HORDE_LEVEL_BUDGETS.get(), playerLevel, fallback));
    }

    /** Max health of an Abyssal Horde naiad for a Kommandant level. */
    public static double abyssalNaiadHealth(int playerLevel) {
        return Math.max(1.0D, numberAt(ABYSSAL_NAIAD_HEALTH.get(), playerLevel, DEFAULT_NAIAD_HEALTH));
    }

    // ================================================================== GUI accessors (tooltips, HUD)

    /**
     * Biomass the server takes for abilities 8-13 (also 10 = proto tracking). Use it instead of the hard-coded
     * 15 / 5 / 25 / 20 / 30 / 20 of the selector tooltips. Returns 0 for any other ability number.
     */
    public static int biomassCost(int ability) {
        return switch (ability) {
            case 8 -> HORDE_BIOMASS_COST.get();
            case 9 -> BURST_BIOMASS_COST.get();
            case 10 -> SONAR_BIOMASS_COST.get();
            case 11 -> VIGIL_BIOMASS_COST.get();
            case 12 -> ARMOR_BIOMASS_COST.get();
            case 13 -> NUKE_BIOMASS_COST.get();
            default -> 0;
        };
    }

    /** Verwas of a horde with the given number of Protohives alive (power 8 tooltip: "Base Verwas"). */
    public static int hordeVerwaCount(int protoCount) {
        int total = HORDE_BASE_VERWAS.get() + protoCount * HORDE_VERWAS_PER_PROTO.get();
        return Math.min(total, HORDE_MAX_VERWAS.get());
    }

    /** Shown by the power 8 tooltip as "Base Verwas". */
    public static int hordeBaseVerwas() {
        return HORDE_BASE_VERWAS.get();
    }

    /** Shown by the power 8 tooltip as "(Up to max N)". */
    public static int hordeMaxVerwas() {
        return HORDE_MAX_VERWAS.get();
    }

    /** Shown by the power 8 tooltip as "+N Verwa" per Protohive. */
    public static int hordeVerwasPerProto() {
        return HORDE_VERWAS_PER_PROTO.get();
    }

    /** Shown by the power 8 tooltip as "+N Budget" per Protohive. */
    public static int hordeBudgetPerProto() {
        return HORDE_BUDGET_PER_PROTO.get();
    }

    /** Shown by the power 8 tooltip as "(Or +N if Verwas maxed out)". */
    public static int hordeBudgetPerProtoMaxed() {
        return HORDE_BUDGET_PER_PROTO_MAXED.get();
    }

    /** Biomass the /vervarequest command (Verwa Transport of power 8) costs. */
    public static int transportBiomassCost() {
        return TRANSPORT_BIOMASS_COST.get();
    }

    /** Seconds of the Verwa Transport countdown. */
    public static int transportWaitSeconds() {
        return TRANSPORT_WAIT_TICKS.get() / 20;
    }

    /** Power 9 tooltip: radius of the standard burst. */
    public static double burstRadius() {
        return BURST_RADIUS.get();
    }

    /** Power 9 tooltip: seconds the level-scaled burst effects last at a Kommandant level. */
    public static int burstDurationSeconds(int playerLevel) {
        return BURST_DURATION_BASE_SECONDS.get() + Math.max(playerLevel, 0) * BURST_DURATION_PER_LEVEL_SECONDS.get();
    }

    /** Power 9 tooltip: health percentage (0-100) under which Exposed I executes. */
    public static int executionThresholdPercent() {
        return (int) Math.round(EXECUTION_BASE_THRESHOLD.get() * 100.0D);
    }

    /** Power 9 tooltip: percentage points added per Exposed level above I. */
    public static int executionThresholdPercentPerLevel() {
        return (int) Math.round(EXECUTION_THRESHOLD_PER_AMPLIFIER.get() * 100.0D);
    }

    /** Power 10 tooltip: base scan range per Protohive. */
    public static int sonarBaseRange() {
        return (int) Math.round(SONAR_PROTO_BASE_RANGE.get());
    }

    /** Power 10 tooltip: extra range per additional Protohive. */
    public static int sonarRangePerProto() {
        return (int) Math.round(SONAR_PROTO_RANGE_PER_PROTO.get());
    }

    /** Power 11 tooltip: max health of the Vigil with the given number of Protohives alive. */
    public static int vigilHealth(int protoCount) {
        return (int) (VIGIL_HEALTH.get() + protoCount * VIGIL_HEALTH_PER_PROTO.get());
    }

    /** Power 11 tooltip: radius of the first scan pulse. */
    public static int vigilScanInitialRadius() {
        return VIGIL_SCAN_INITIAL_RADIUS.get();
    }

    /** Power 11 tooltip: radius growth per pulse. */
    public static int vigilScanGrowth() {
        return VIGIL_SCAN_GROWTH.get();
    }

    /** Power 11 tooltip: maximum pulse radius. */
    public static int vigilScanMaxRadius() {
        return VIGIL_SCAN_MAX_RADIUS.get();
    }

    /** Power 11 tooltip: seconds between two scan pulses. */
    public static int vigilScanIntervalSeconds() {
        return VIGIL_SCAN_INTERVAL_TICKS.get() / 20;
    }

    /** Power 11 tooltip: chance in percent (0-100) that a pulse spawns a jamming mound on a machine. */
    public static int vigilJamChancePercent() {
        return (int) Math.round(VIGIL_JAM_CHANCE.get() * 100.0D);
    }

    /** Power 11 tooltip: seconds between two jams of the same machine. */
    public static int vigilJamIntervalSeconds() {
        return VIGIL_JAM_INTERVAL_TICKS.get() / 20;
    }

    /** Power 11 tooltip: freezing damage reduction in percent (0-100). */
    public static int vigilFreezeResistancePercent() {
        return (int) Math.round((1.0D - VIGIL_FREEZE_MULTIPLIER.get()) * 100.0D);
    }

    /** Power 12 tooltip: Armor HP regenerated per second by a plain / Caustic Kommandant. */
    public static int armorRegenPerSecond() {
        return ARMOR_REGEN_PER_SECOND.get();
    }

    /** Power 12 tooltip: knockback resistance in percent (0-100) while the armor lasts. */
    public static int armorKnockbackResistancePercent() {
        return (int) Math.round(ARMOR_KNOCKBACK_RESISTANCE.get() * 100.0D);
    }

    /** Power 13 tooltip: seconds between the call and the detonation. */
    public static int nukeDetonationDelaySeconds() {
        return NUKE_DETONATION_DELAY_TICKS.get() / 20;
    }

    /** Power 13 tooltip: blocks above the caster where the nuke appears. */
    public static int nukeSpawnHeight() {
        return NUKE_SPAWN_HEIGHT.get();
    }

    /** Power 13 tooltip: damage of the blast. */
    public static int nukeDamage() {
        return (int) Math.round(NUKE_DAMAGE.get());
    }

    /** Power 13 tooltip: radius of the blast damage. */
    public static int nukeDamageRadius() {
        return (int) Math.round(NUKE_DAMAGE_RADIUS.get());
    }

    /** Power 13 tooltip: seconds the toxic cloud lasts. */
    public static int nukeCloudSeconds() {
        return NUKE_CLOUD_LIFETIME_TICKS.get() / 20;
    }
}
