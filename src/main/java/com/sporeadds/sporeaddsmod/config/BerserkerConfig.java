package com.sporeadds.sporeaddsmod.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.HashMap;
import java.util.Map;

/**
 * Berserker class settings: blood samples, reinforced combat chains, counter, claws.
 *
 * <p>Defaults reproduce the behaviour that was hard-coded before these options existed. Read values with
 * {@code .get()} at USE time (never copy them into static finals: the config is not loaded yet while classes
 * initialise). Called from {@link SporeAddsConfig}'s static initialiser, before the spec is built.
 *
 * <p>Layout: {@code berserker.blood_samples} (the 9 Compounds syringes), {@code berserker.chains} (Reinforced
 * Combat Chains + the Enchained effect), {@code berserker.counter} and {@code berserker.claws}.
 */
public final class BerserkerConfig {
    private BerserkerConfig() {
    }

    /** The Compounds kit has 6 slots (fixed by its GUI texture), so sample limits can only be lowered from here. */
    public static final int SAMPLE_SLOTS = 6;

    /** Durability of the Reinforced Combat Chains registered in code; also the fallback while the config is not loaded. */
    public static final int DEFAULT_CHAINS_MAX_DURABILITY = 25;

    // ------------------------------------------------------------------ blood samples (Compounds syringes)

    public static ForgeConfigSpec.IntValue SAMPLES_MAX_TOTAL;
    /** kind (lower-case {@code CompoundType} name) -> max_count of that kind. */
    private static final Map<String, ForgeConfigSpec.IntValue> SAMPLE_LIMITS = new HashMap<>();

    public static ForgeConfigSpec.DoubleValue REINFORCED_DAMAGE_REDUCTION;
    public static ForgeConfigSpec.DoubleValue SKELETAL_SPEED_BONUS;
    public static ForgeConfigSpec.DoubleValue SKELETAL_KNOCKBACK_RESISTANCE;
    public static ForgeConfigSpec.DoubleValue DROWNED_SWIM_BONUS;
    public static ForgeConfigSpec.DoubleValue VAMPIRIC_HEAL_PER_HIT;
    public static ForgeConfigSpec.IntValue CHARRED_FIRE_TICKS;
    public static ForgeConfigSpec.DoubleValue CALCIFIED_KNOCKBACK_BONUS;
    public static ForgeConfigSpec.DoubleValue BEZERK_UNARMED_DAMAGE;
    public static ForgeConfigSpec.DoubleValue BEZERK_EXTRA_DRAIN;
    public static ForgeConfigSpec.IntValue TOXIC_POISON_TICKS;
    public static ForgeConfigSpec.IntValue ROTTEN_WITHER_TICKS;

    // ------------------------------------------------------------------ reinforced combat chains

    public static ForgeConfigSpec.IntValue CHAINS_THROW_COOLDOWN_TICKS;
    public static ForgeConfigSpec.DoubleValue CHAINS_PROJECTILE_SPEED;
    public static ForgeConfigSpec.IntValue CHAINS_FLIGHT_TICKS;
    public static ForgeConfigSpec.DoubleValue CHAINS_HIT_DAMAGE;
    public static ForgeConfigSpec.DoubleValue CHAINS_LINK_DISTANCE;
    public static ForgeConfigSpec.DoubleValue CHAINS_MAX_LINK_DISTANCE;
    public static ForgeConfigSpec.DoubleValue ENCHAINED_PLAYER_DAMAGE_MULTIPLIER;
    public static ForgeConfigSpec.DoubleValue ENCHAINED_NON_PLAYER_DAMAGE_MULTIPLIER;
    public static ForgeConfigSpec.IntValue CHAINS_MAX_DURABILITY;
    public static ForgeConfigSpec.IntValue CHAINS_WEAR_PER_INTERVAL;
    public static ForgeConfigSpec.IntValue CHAINS_WEAR_INTERVAL_TICKS;

    // ------------------------------------------------------------------ counter

    public static ForgeConfigSpec.IntValue COUNTER_WINDOW_TICKS;
    public static ForgeConfigSpec.IntValue COUNTER_FAIL_COOLDOWN_TICKS;
    public static ForgeConfigSpec.IntValue COUNTER_MIN_SUCCESS_COOLDOWN_TICKS;
    public static ForgeConfigSpec.DoubleValue COUNTER_DAMAGE_PER_COOLDOWN_SECOND;
    public static ForgeConfigSpec.DoubleValue COUNTER_MELEE_REFLECT;
    public static ForgeConfigSpec.BooleanValue COUNTER_REFLECT_PROJECTILES;
    public static ForgeConfigSpec.DoubleValue COUNTER_PROJECTILE_SPEED_MULTIPLIER;

    // ------------------------------------------------------------------ claws of brutality

    public static ForgeConfigSpec.IntValue CLAWS_MAX_POINTS;
    public static ForgeConfigSpec.IntValue CLAWS_ACTIVATION_THRESHOLD;
    public static ForgeConfigSpec.IntValue CLAWS_COOLDOWN_TICKS;
    public static ForgeConfigSpec.DoubleValue CLAWS_BASE_DAMAGE;
    public static ForgeConfigSpec.DoubleValue CLAWS_CRIT_MULTIPLIER;
    public static ForgeConfigSpec.IntValue CLAWS_IDLE_TICKS_BEFORE_LOSS;
    public static ForgeConfigSpec.IntValue CLAWS_DECAY_STEP_TICKS;
    public static ForgeConfigSpec.DoubleValue CLAWS_IDLE_LOSS_PER_STEP;
    public static ForgeConfigSpec.DoubleValue CLAWS_ACTIVE_DRAIN_PER_STEP;
    public static ForgeConfigSpec.DoubleValue CLAWS_STOP_REFUND_FRACTION;
    public static ForgeConfigSpec.DoubleValue CLAWS_STOP_REFUND_TICKS_PER_POINT;

    static void define(ForgeConfigSpec.Builder builder) {
        builder.push("berserker");

        defineBloodSamples(builder);
        defineChains(builder);
        defineCounter(builder);
        defineClaws(builder);

        builder.pop();
    }

    // ------------------------------------------------------------------ blood samples

    private static void defineBloodSamples(ForgeConfigSpec.Builder builder) {
        builder.comment("Blood samples: the 9 syringe kinds (spore:*_syringe) a Berserker places in the 6-slot Compounds kit.",
                "Their effects only apply while Claws of Brutality is active. Every stat below is PER SYRINGE of that kind (they stack).")
                .push("blood_samples");

        SAMPLES_MAX_TOTAL = builder
                .comment("Maximum number of syringes (all kinds together) that can be placed in the Compounds kit and take effect. Default 6.",
                        "The kit always has 6 slots, so this can only be lowered. 0 turns the whole syringe system off.",
                        "Syringes over the limit can no longer be placed and, if they were already inside when the limit was lowered, have no effect.")
                .defineInRange("max_total", SAMPLE_SLOTS, 0, SAMPLE_SLOTS);

        builder.push("reinforced");
        sampleLimit(builder, "reinforced");
        REINFORCED_DAMAGE_REDUCTION = builder
                .comment("Incoming damage removed per reinforced syringe, as a 0-1 fraction (0.075 = 7.5%). Default 0.075.",
                        "Stacks additively; the total can never go below 0 damage taken.")
                .defineInRange("damage_reduction_per_sample", 0.075D, 0.0D, 1.0D);
        builder.pop();

        builder.push("skeletal");
        sampleLimit(builder, "skeletal");
        SKELETAL_SPEED_BONUS = builder
                .comment("Movement speed added per skeletal syringe, as a multiplier fraction (0.075 = +7.5%). Default 0.075.")
                .defineInRange("speed_bonus_per_sample", 0.075D, 0.0D, 10.0D);
        SKELETAL_KNOCKBACK_RESISTANCE = builder
                .comment("Knockback resistance added per skeletal syringe, 0-1 scale (0.25 = +25%, 1.0 = immune to knockback). Default 0.25.")
                .defineInRange("knockback_resistance_per_sample", 0.25D, 0.0D, 1.0D);
        builder.pop();

        builder.push("drowned");
        sampleLimit(builder, "drowned");
        DROWNED_SWIM_BONUS = builder
                .comment("Swim speed added per drowned syringe, as a multiplier fraction (0.1 = +10%). Default 0.1.",
                        "Water Breathing is granted as soon as one drowned syringe counts, whatever this value is.")
                .defineInRange("swim_speed_bonus_per_sample", 0.10D, 0.0D, 10.0D);
        builder.pop();

        builder.push("vampiric");
        sampleLimit(builder, "vampiric");
        VAMPIRIC_HEAL_PER_HIT = builder
                .comment("Health points healed per vampiric syringe each time the claws damage a creature (1.0 = half a heart). Default 1.0.")
                .defineInRange("heal_per_hit_per_sample", 1.0D, 0.0D, 1000.0D);
        builder.pop();

        builder.push("charred");
        sampleLimit(builder, "charred");
        CHARRED_FIRE_TICKS = builder
                .comment("Ticks of fire added to the target per charred syringe on every hit that damages it (20 ticks = 1 second). Default 20.",
                        "Fire Resistance is granted as soon as one charred syringe counts, whatever this value is.")
                .defineInRange("fire_ticks_per_hit_per_sample", 20, 0, 72000);
        builder.pop();

        builder.push("calcified");
        sampleLimit(builder, "calcified");
        CALCIFIED_KNOCKBACK_BONUS = builder
                .comment("Extra knockback strength per calcified syringe, as a multiplier fraction (0.35 = hits push 35% harder). Default 0.35.")
                .defineInRange("knockback_bonus_per_sample", 0.35D, 0.0D, 100.0D);
        builder.pop();

        builder.push("bezerk");
        sampleLimit(builder, "bezerk");
        BEZERK_UNARMED_DAMAGE = builder
                .comment("Flat damage added to every bare-handed claw hit per bezerk syringe. Default 2.0.")
                .defineInRange("unarmed_damage_per_sample", 2.0D, 0.0D, 100000.0D);
        BEZERK_EXTRA_DRAIN = builder
                .comment("Extra Brutality points drained per bezerk syringe on every drain step while the claws are active. Default 0.5.",
                        "A step is claws.decay.step_ticks long (default 10 ticks), so 0.5 per step = 1 extra point per second per syringe.")
                .defineInRange("extra_drain_per_sample", 0.5D, 0.0D, 100000.0D);
        builder.pop();

        builder.push("toxic");
        sampleLimit(builder, "toxic");
        TOXIC_POISON_TICKS = builder
                .comment("Ticks of Poison applied to the target per toxic syringe on every hit (200 ticks = 10 seconds). Default 200.",
                        "The Poison level also rises with the number of syringes (1 = Poison I, 2 = Poison II...). 0 = no poison.")
                .defineInRange("poison_ticks_per_sample", 200, 0, 72000);
        builder.pop();

        builder.push("rotten");
        sampleLimit(builder, "rotten");
        ROTTEN_WITHER_TICKS = builder
                .comment("Ticks of Wither applied to the target per rotten syringe on every hit (200 ticks = 10 seconds). Default 200.",
                        "The Wither level also rises with the number of syringes (1 = Wither I, 2 = Wither II...). 0 = no wither.")
                .defineInRange("wither_ticks_per_sample", 200, 0, 72000);
        builder.pop();

        builder.pop();
    }

    private static void sampleLimit(ForgeConfigSpec.Builder builder, String kind) {
        SAMPLE_LIMITS.put(kind, builder
                .comment("Maximum number of " + kind + " syringes that can be placed and take effect at the same time. Default 6 (no limit besides the 6 slots).",
                        "0 disables this kind. Syringes over the limit can no longer be placed and, if already inside, have no effect.")
                .defineInRange("max_count", SAMPLE_SLOTS, 0, SAMPLE_SLOTS));
    }

    // ------------------------------------------------------------------ chains

    private static void defineChains(ForgeConfigSpec.Builder builder) {
        builder.comment("Reinforced Combat Chains (thrown from the offhand) and the Enchained effect they put on owner and target.",
                "Who may use the chains is set by 'items_settings.reinforced_combat_chains_requires_origin'.")
                .push("chains");

        CHAINS_THROW_COOLDOWN_TICKS = builder
                .comment("Cooldown in ticks before the chains can be thrown again after a throw (20 ticks = 1 second). Default 20.")
                .defineInRange("throw_cooldown_ticks", 20, 0, 1728000);
        CHAINS_PROJECTILE_SPEED = builder
                .comment("Initial speed of the thrown chain in blocks per tick. Default 1.5.",
                        "Together with flight_ticks this sets how far the chain flies before it vanishes.")
                .defineInRange("projectile_speed", 1.5D, 0.1D, 20.0D);
        CHAINS_FLIGHT_TICKS = builder
                .comment("Ticks a thrown chain keeps flying before it vanishes if it hit nothing (20 ticks = 1 second). Default 100.")
                .defineInRange("flight_ticks", 100, 1, 2400);
        CHAINS_HIT_DAMAGE = builder
                .comment("Damage (health points, 2 = one heart) the chain deals to the entity it hits. Default 3.0.")
                .defineInRange("hit_damage", 3.0D, 0.0D, 1000.0D);
        CHAINS_LINK_DISTANCE = builder
                .comment("Chain range in blocks: once owner and target are further apart than this, they are pulled back towards each other. Default 15.0.")
                .defineInRange("link_distance", 15.0D, 1.0D, 1000.0D);
        CHAINS_MAX_LINK_DISTANCE = builder
                .comment("Distance in blocks at which the link snaps and the chains break. Default 80.0.",
                        "Keep it above link_distance, otherwise the link breaks before it can pull.")
                .defineInRange("max_link_distance", 80.0D, 2.0D, 2000.0D);

        builder.push("enchained");
        ENCHAINED_PLAYER_DAMAGE_MULTIPLIER = builder
                .comment("Multiplier applied to the damage dealt by an Enchained PLAYER (0.85 = 15% less damage, 1.0 = no reduction). Default 0.85.")
                .defineInRange("player_damage_multiplier", 0.85D, 0.0D, 10.0D);
        ENCHAINED_NON_PLAYER_DAMAGE_MULTIPLIER = builder
                .comment("Multiplier applied to the damage dealt by an Enchained NON-PLAYER entity (0.35 = 65% less damage, 1.0 = no reduction). Default 0.35.")
                .defineInRange("non_player_damage_multiplier", 0.35D, 0.0D, 10.0D);
        builder.pop();

        builder.push("durability");
        CHAINS_MAX_DURABILITY = builder
                .comment("Maximum durability of the Reinforced Combat Chains item. Default 25.",
                        "It is read when the item is used, so it also applies to chains that already exist (damage already taken is kept;",
                        "lowering it below the damage a stack already has breaks that stack on its next wear step).",
                        "On a dedicated server each client draws the durability bar from its own copy of this file.")
                .defineInRange("max_durability", DEFAULT_CHAINS_MAX_DURABILITY, 1, 100000);
        CHAINS_WEAR_PER_INTERVAL = builder
                .comment("Durability points the chains lose on every wear step while linked (multiplied by the number of chains attached to the same target). Default 1.",
                        "0 = the chains never wear out.")
                .defineInRange("wear_per_interval", 1, 0, 1000);
        CHAINS_WEAR_INTERVAL_TICKS = builder
                .comment("Ticks between two wear steps while linked (20 ticks = 1 second). Default 21 (the original code wore the chains every 21 ticks).")
                .defineInRange("wear_interval_ticks", 21, 1, 72000);
        builder.pop();

        builder.pop();
    }

    // ------------------------------------------------------------------ counter

    private static void defineCounter(ForgeConfigSpec.Builder builder) {
        builder.comment("Counter: brace for a short window; the next entity attack is negated and turned back on the attacker.")
                .push("counter");

        COUNTER_WINDOW_TICKS = builder
                .comment("Duration in ticks of the counter window after activating it (10 ticks = 0.5 seconds). Default 10.")
                .defineInRange("window_ticks", 10, 1, 1200);
        COUNTER_FAIL_COOLDOWN_TICKS = builder
                .comment("Cooldown in ticks when the window expires without repelling anything (20 ticks = 1 second). Default 100 (5 seconds).")
                .defineInRange("fail_cooldown_ticks", 100, 0, 1728000);
        COUNTER_MIN_SUCCESS_COOLDOWN_TICKS = builder
                .comment("Minimum cooldown in ticks after a successful counter (20 ticks = 1 second). Default 20.",
                        "The real cooldown is (repelled damage / damage_per_cooldown_second) seconds rounded up, but never below this.")
                .defineInRange("min_success_cooldown_ticks", 20, 0, 1728000);
        COUNTER_DAMAGE_PER_COOLDOWN_SECOND = builder
                .comment("Damage points of the repelled hit that add one second of cooldown after a successful counter. Default 4.0 (a 20 damage hit = 5 seconds).")
                .defineInRange("damage_per_cooldown_second", 4.0D, 0.1D, 100000.0D);
        COUNTER_MELEE_REFLECT = builder
                .comment("Fraction of a countered melee hit's damage dealt back to the attacker, 0-1 scale (0.5 = 50%). Default 0.5.",
                        "Values above 1 reflect more damage than was received; 0 only negates the hit.")
                .defineInRange("melee_reflect_fraction", 0.5D, 0.0D, 10.0D);
        COUNTER_REFLECT_PROJECTILES = builder
                .comment("If true (default) a projectile hitting the player during the counter window is sent back at its shooter.",
                        "If false the counter only works on melee attacks and projectiles hit normally.")
                .define("reflect_projectiles", true);
        COUNTER_PROJECTILE_SPEED_MULTIPLIER = builder
                .comment("Speed of a reflected projectile compared with the speed it arrived with (1.0 = same speed). Default 1.0.",
                        "Faster arrows and tridents hit harder.")
                .defineInRange("projectile_speed_multiplier", 1.0D, 0.1D, 10.0D);

        builder.pop();
    }

    // ------------------------------------------------------------------ claws

    private static void defineClaws(ForgeConfigSpec.Builder builder) {
        builder.comment("Claws of Brutality: a damage meter ('Brutality') that fills as the Berserker deals damage and, once activated, grows bone claws.")
                .push("claws");

        CLAWS_MAX_POINTS = builder
                .comment("Maximum Brutality points the meter can hold. Default 200.",
                        "The meter fills with the damage dealt to other creatures (overkill excluded).")
                .defineInRange("max_points", 200, 1, 1000000);
        CLAWS_ACTIVATION_THRESHOLD = builder
                .comment("Brutality points needed to activate the claws. Default 200 (a full meter with the default max_points).",
                        "A value above max_points behaves as max_points.")
                .defineInRange("activation_threshold", 200, 1, 1000000);
        CLAWS_COOLDOWN_TICKS = builder
                .comment("Cooldown in ticks after the claws run out or are stopped (20 ticks = 1 second). Default 2400 (120 seconds).")
                .defineInRange("cooldown_ticks", 2400, 0, 1728000);

        builder.push("damage");
        CLAWS_BASE_DAMAGE = builder
                .comment("Flat damage of a bare-handed hit while the claws are active. Default 12.0.",
                        "The player's attack attribute minus 1 is added on top, so Strength and Weakness still matter. A weapon in the main hand gets no bonus.")
                .defineInRange("base_damage", 12.0D, 0.0D, 100000.0D);
        CLAWS_CRIT_MULTIPLIER = builder
                .comment("Damage multiplier of a critical bare-handed hit while the claws are active. Default 0.0 = use the vanilla critical multiplier (normally 1.5).",
                        "Any value above 0 replaces it, e.g. 2.0 doubles the claw damage on critical hits.")
                .defineInRange("crit_multiplier", 0.0D, 0.0D, 100.0D);
        builder.pop();

        builder.push("decay");
        CLAWS_IDLE_TICKS_BEFORE_LOSS = builder
                .comment("Ticks without dealing damage before the meter starts losing points (20 ticks = 1 second). Default 400 (20 seconds).",
                        "Only applies while the claws are not active.")
                .defineInRange("idle_ticks_before_loss", 400, 0, 1728000);
        CLAWS_DECAY_STEP_TICKS = builder
                .comment("Length in ticks of one decay step: points are lost once per step, both while idle and while the claws are active. Default 10 (0.5 seconds).")
                .defineInRange("step_ticks", 10, 1, 1200);
        CLAWS_IDLE_LOSS_PER_STEP = builder
                .comment("Brutality points lost per decay step once the idle time has passed and the claws are not active. Default 1.0.")
                .defineInRange("idle_loss_per_step", 1.0D, 0.0D, 100000.0D);
        CLAWS_ACTIVE_DRAIN_PER_STEP = builder
                .comment("Brutality points drained per decay step while the claws are active (bezerk syringes drain extra). Default 1.0.",
                        "With the defaults a full meter lasts 200 points / 2 per second = 100 seconds.",
                        "0 = the claws never run out by themselves (the player can still stop them).")
                .defineInRange("active_drain_per_step", 1.0D, 0.0D, 100000.0D);
        builder.pop();

        builder.push("early_stop");
        CLAWS_STOP_REFUND_FRACTION = builder
                .comment("When the claws are stopped early (using the skill again), this fraction of the remaining Brutality points is turned into a cooldown reduction and the rest is kept. 0-1 scale. Default 0.5.")
                .defineInRange("refund_fraction", 0.5D, 0.0D, 1.0D);
        CLAWS_STOP_REFUND_TICKS_PER_POINT = builder
                .comment("Ticks removed from the cooldown for every refunded Brutality point when the claws are stopped early (20 ticks = 1 second). Default 20.")
                .defineInRange("refund_ticks_per_point", 20.0D, 0.0D, 100000.0D);
        builder.pop();

        builder.pop();
    }

    // ------------------------------------------------------------------ accessors (read the config at USE time)

    /** Total syringes that may count / be placed at once (0-6). */
    public static int sampleMaxTotal() {
        return SAMPLES_MAX_TOTAL.get();
    }

    /** Max syringes of one kind ({@code kind} = lower-case {@code CompoundType} name) that may count / be placed (0-6). */
    public static int sampleLimit(String kind) {
        ForgeConfigSpec.IntValue limit = SAMPLE_LIMITS.get(kind);
        return limit == null ? SAMPLE_SLOTS : limit.get();
    }

    // chains

    public static int chainsThrowCooldownTicks() {
        return CHAINS_THROW_COOLDOWN_TICKS.get();
    }

    public static float chainsProjectileSpeed() {
        return CHAINS_PROJECTILE_SPEED.get().floatValue();
    }

    public static int chainsFlightTicks() {
        return CHAINS_FLIGHT_TICKS.get();
    }

    public static float chainsHitDamage() {
        return CHAINS_HIT_DAMAGE.get().floatValue();
    }

    /** Chain range (blocks) at which the pull starts. */
    public static double chainsLinkDistance() {
        return CHAINS_LINK_DISTANCE.get();
    }

    /** Distance (blocks) at which the link breaks. */
    public static double chainsMaxLinkDistance() {
        return CHAINS_MAX_LINK_DISTANCE.get();
    }

    public static float enchainedPlayerDamageMultiplier() {
        return ENCHAINED_PLAYER_DAMAGE_MULTIPLIER.get().floatValue();
    }

    public static float enchainedNonPlayerDamageMultiplier() {
        return ENCHAINED_NON_PLAYER_DAMAGE_MULTIPLIER.get().floatValue();
    }

    /**
     * Maximum durability of the chains. Safe to call at any time: falls back to the registered value (25) while the
     * config is not loaded yet (an item's max damage can be queried during startup).
     */
    public static int chainsMaxDurability() {
        try {
            return CHAINS_MAX_DURABILITY.get();
        } catch (RuntimeException e) {
            return DEFAULT_CHAINS_MAX_DURABILITY;
        }
    }

    public static int chainsWearPerInterval() {
        return CHAINS_WEAR_PER_INTERVAL.get();
    }

    public static int chainsWearIntervalTicks() {
        return CHAINS_WEAR_INTERVAL_TICKS.get();
    }

    // counter

    public static int counterWindowTicks() {
        return COUNTER_WINDOW_TICKS.get();
    }

    public static int counterFailCooldownTicks() {
        return COUNTER_FAIL_COOLDOWN_TICKS.get();
    }

    public static int counterMinSuccessCooldownTicks() {
        return COUNTER_MIN_SUCCESS_COOLDOWN_TICKS.get();
    }

    public static float counterDamagePerCooldownSecond() {
        return COUNTER_DAMAGE_PER_COOLDOWN_SECOND.get().floatValue();
    }

    public static float counterMeleeReflect() {
        return COUNTER_MELEE_REFLECT.get().floatValue();
    }

    public static boolean counterReflectProjectiles() {
        return COUNTER_REFLECT_PROJECTILES.get();
    }

    public static double counterProjectileSpeedMultiplier() {
        return COUNTER_PROJECTILE_SPEED_MULTIPLIER.get();
    }

    // claws

    /** Maximum Brutality points (the HUD bar's full scale; the server also sends it to the client). */
    public static float clawsMaxPoints() {
        return CLAWS_MAX_POINTS.get().floatValue();
    }

    /** Brutality points needed to activate the claws (never above {@link #clawsMaxPoints()}). */
    public static float clawsActivationThreshold() {
        return (float) Math.min(CLAWS_ACTIVATION_THRESHOLD.get().intValue(), CLAWS_MAX_POINTS.get().intValue());
    }

    public static int clawsCooldownTicks() {
        return CLAWS_COOLDOWN_TICKS.get();
    }

    /** Flat damage of a bare-handed claw hit (before bezerk syringes and the attack attribute). */
    public static float clawsBaseDamage() {
        return CLAWS_BASE_DAMAGE.get().floatValue();
    }

    /** Crit multiplier override; 0 = keep the vanilla one. */
    public static float clawsCritMultiplier() {
        return CLAWS_CRIT_MULTIPLIER.get().floatValue();
    }

    public static int clawsIdleTicksBeforeLoss() {
        return CLAWS_IDLE_TICKS_BEFORE_LOSS.get();
    }

    public static int clawsDecayStepTicks() {
        return CLAWS_DECAY_STEP_TICKS.get();
    }

    public static float clawsIdleLossPerStep() {
        return CLAWS_IDLE_LOSS_PER_STEP.get().floatValue();
    }

    public static float clawsActiveDrainPerStep() {
        return CLAWS_ACTIVE_DRAIN_PER_STEP.get().floatValue();
    }

    public static float clawsStopRefundFraction() {
        return CLAWS_STOP_REFUND_FRACTION.get().floatValue();
    }

    public static float clawsStopRefundTicksPerPoint() {
        return CLAWS_STOP_REFUND_TICKS_PER_POINT.get().floatValue();
    }
}
