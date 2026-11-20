package com.sporeadds.sporeaddsmod.config;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Ghost class settings: backstab, execution, disguise (camouflage), decoy, bucket of remains, critical wound, ambushed.
 *
 * <p>Defaults reproduce the behaviour that was hard-coded before these options existed. Read values with
 * {@code .get()} at USE time (never copy them into static finals: the config is not loaded yet while classes
 * initialise). Called from {@link SporeAddsConfig}'s static initialiser, before the spec is built.
 *
 * <p>Durations are in ticks (20 ticks = 1 second). The small static accessors at the bottom exist so that GUI /
 * tooltip code can show the configured numbers.
 */
public final class GhostConfig {

    /** How the backstab execution threshold is read. */
    public enum ExecutionMode {
        /** Execute when the target would be left below a fixed amount of hit points. */
        HP,
        /** Execute when the target would be left below a percentage of its max health. */
        PERCENT
    }

    /** What uses up a disguise stack. */
    public enum StackConsume {
        /** Stacks are never used (the old behaviour: the disguise ends only by toggling it off or taking damage). */
        NONE,
        /** Every entity the disguised Ghost damages. */
        ATTACK,
        /** Every backstab made while disguised. */
        BACKSTAB,
        /** Every entity the disguised Ghost kills (backstab executions included). */
        KILL
    }

    /** Old hard-coded Bucket of Remains duration (30 s); the tooltip keeps its fixed text while the config equals this. */
    public static final int DEFAULT_BUCKET_DURATION_TICKS = 600;

    /** Syntax only: {@code modid:attribute;amount[;add|multiply_base|multiply_total]}. Never checks the registry. */
    private static final Pattern ATTRIBUTE_ENTRY_SYNTAX = Pattern.compile(
            "^([a-z0-9_.\\-]+:)?[a-z0-9_./\\-]+\\s*;\\s*[-+]?\\d+(\\.\\d+)?\\s*(;\\s*(?i:add|multiply_base|multiply_total)\\s*)?$");

    // ghost.backstab
    public static ModConfigSpec.DoubleValue BACKSTAB_DAMAGE_MULTIPLIER;
    public static ModConfigSpec.DoubleValue BACKSTAB_MAX_DISTANCE;
    public static ModConfigSpec.DoubleValue BACKSTAB_ANGLE_THRESHOLD;
    public static ModConfigSpec.IntValue BACKSTAB_XP_EXECUTION;
    public static ModConfigSpec.IntValue BACKSTAB_XP_DISGUISED_HIT;
    public static ModConfigSpec.IntValue BACKSTAB_XP_DISGUISED_EXECUTION;

    // ghost.execution
    public static ModConfigSpec.EnumValue<ExecutionMode> EXECUTION_THRESHOLD_MODE;
    public static ModConfigSpec.DoubleValue EXECUTION_THRESHOLD_HP;
    public static ModConfigSpec.DoubleValue EXECUTION_THRESHOLD_PERCENT;
    public static ModConfigSpec.BooleanValue EXECUTION_CAN_EXECUTE_PLAYERS;

    // ghost.disguise
    public static ModConfigSpec.IntValue DISGUISE_COOLDOWN_TICKS;
    public static ModConfigSpec.IntValue DISGUISE_MAX_DURATION_TICKS;
    public static ModConfigSpec.DoubleValue DISGUISE_BACKSTAB_BONUS_MULTIPLIER;
    public static ModConfigSpec.DoubleValue DISGUISE_RANGED_DAMAGE_MULTIPLIER;
    public static ModConfigSpec.DoubleValue DISGUISE_RANGED_MIN_DISTANCE;
    public static ModConfigSpec.IntValue DISGUISE_MAX_STACKS;
    public static ModConfigSpec.EnumValue<StackConsume> DISGUISE_STACK_CONSUME_ON;

    // ghost.ambushed
    public static ModConfigSpec.IntValue AMBUSHED_DURATION_TICKS;
    public static ModConfigSpec.DoubleValue AMBUSHED_MOVEMENT_SPEED_MODIFIER;
    public static ModConfigSpec.DoubleValue AMBUSHED_OUTGOING_DAMAGE_MULTIPLIER;
    public static ModConfigSpec.DoubleValue AMBUSHED_INCOMING_DAMAGE_MULTIPLIER;

    // ghost.critical_wound
    public static ModConfigSpec.IntValue CRITICAL_WOUND_DURATION_TICKS;
    public static ModConfigSpec.DoubleValue CRITICAL_WOUND_MOVEMENT_SPEED_MODIFIER;
    public static ModConfigSpec.DoubleValue CRITICAL_WOUND_OUTGOING_DAMAGE_MULTIPLIER;
    public static ModConfigSpec.DoubleValue CRITICAL_WOUND_INCOMING_DAMAGE_MULTIPLIER;
    public static ModConfigSpec.BooleanValue CRITICAL_WOUND_ONLY_WHEN_DISGUISED;
    public static ModConfigSpec.ConfigValue<List<? extends String>> CRITICAL_WOUND_EXTRA_EFFECTS;
    public static ModConfigSpec.ConfigValue<List<? extends String>> CRITICAL_WOUND_ATTRIBUTE_MODIFIERS;

    // ghost.decoy
    public static ModConfigSpec.IntValue DECOY_COOLDOWN_TICKS;
    public static ModConfigSpec.IntValue DECOY_MAX_ACTIVE;
    public static ModConfigSpec.IntValue DECOY_LIFETIME_TICKS;
    public static ModConfigSpec.ConfigValue<List<? extends String>> DECOY_ATTACKER_EFFECTS;
    public static ModConfigSpec.DoubleValue DECOY_AGGRO_RANGE;
    public static ModConfigSpec.DoubleValue DECOY_ATTACK_RANGE;
    public static ModConfigSpec.DoubleValue DECOY_MOVE_SPEED;

    // ghost.bucket_of_remains
    public static ModConfigSpec.IntValue BUCKET_DURATION_TICKS;
    public static ModConfigSpec.DoubleValue BUCKET_PURSUER_CLEAR_RADIUS;

    private GhostConfig() {
    }

    static void define(ModConfigSpec.Builder builder) {
        builder.comment(
                "Ghost class balance. Every default reproduces the behaviour the mod had before these options existed.",
                "Durations are in ticks (20 ticks = 1 second) unless the key says otherwise.")
                .push("ghost");

        // ---------------------------------------------------------------- backstab
        builder.comment("Backstab: a hit on a target from behind, made by a Ghost with a weapon in hand.")
                .push("backstab");

        BACKSTAB_DAMAGE_MULTIPLIER = builder
                .comment("Damage multiplier of a backstab. Default 2.0 (x2).",
                        "While the Ghost is disguised the disguise bonus (ghost.disguise.backstab_bonus_multiplier) is applied on top.")
                .defineInRange("damage_multiplier", 2.0D, 0.0D, 1000.0D);

        BACKSTAB_MAX_DISTANCE = builder
                .comment("Maximum distance in blocks between the Ghost and the target for a hit to count as a backstab. Default 7.0.")
                .defineInRange("max_distance", 7.0D, 0.0D, 128.0D);

        BACKSTAB_ANGLE_THRESHOLD = builder
                .comment("How exactly 'from behind' the Ghost has to be (cosine of the allowed angle, range -1.0 to 1.0). Default 0.5 (within 60 degrees of straight behind).",
                        "Higher = stricter (1.0 is practically impossible), lower = more forgiving, -1.0 = any direction.")
                .defineInRange("angle_threshold", 0.5D, -1.0D, 1.0D);

        BACKSTAB_XP_EXECUTION = builder
                .comment("Experience points dropped as an orb when a backstab executes the target and the Ghost is NOT disguised. Default 8.")
                .defineInRange("xp_execution", 8, 0, 100000);

        BACKSTAB_XP_DISGUISED_HIT = builder
                .comment("Experience points dropped as an orb when a DISGUISED Ghost backstabs without executing the target. Default 20.")
                .defineInRange("xp_disguised_hit", 20, 0, 100000);

        BACKSTAB_XP_DISGUISED_EXECUTION = builder
                .comment("Experience points dropped as an orb when a DISGUISED Ghost backstab executes the target. Default 12.")
                .defineInRange("xp_disguised_execution", 12, 0, 100000);

        builder.pop();

        // ---------------------------------------------------------------- execution
        builder.comment("Execution: a backstab that would leave the target below the threshold kills it outright (one tick later).")
                .push("execution");

        EXECUTION_THRESHOLD_MODE = builder
                .comment("How the execution threshold is read. Default HP.",
                        "HP = execute when (target health - damage of the hit) is below threshold_hp hit points.",
                        "PERCENT = execute when that remaining health is below threshold_percent % of the target's max health.")
                .defineEnum("threshold_mode", ExecutionMode.HP);

        EXECUTION_THRESHOLD_HP = builder
                .comment("Remaining health in hit points (2 HP = 1 heart) below which a backstab executes the target. Only used when threshold_mode = HP. Default 20.0.",
                        "Note that with the default every backstab on a target of 20 max health or less executes it.")
                .defineInRange("threshold_hp", 20.0D, 0.0D, 1000000.0D);

        EXECUTION_THRESHOLD_PERCENT = builder
                .comment("Remaining health as a percentage (0-100) of the target's max health below which a backstab executes it. Only used when threshold_mode = PERCENT. Default 25.0.")
                .defineInRange("threshold_percent", 25.0D, 0.0D, 100.0D);

        EXECUTION_CAN_EXECUTE_PLAYERS = builder
                .comment("If false, backstabs never execute players (they still take the backstab damage). Default true (the old behaviour: players without active Armor HP can be executed like any other entity).")
                .define("can_execute_players", true);

        builder.pop();

        // ---------------------------------------------------------------- disguise
        builder.comment("Disguise (camouflage): the Ghost turns invisible behind a bush disguise. Toggle it from the Action Wheel.")
                .push("disguise");

        DISGUISE_COOLDOWN_TICKS = builder
                .comment("Cooldown in ticks that starts when the disguise ends (toggled off, broken by damage, stacks used up or max duration reached). Default 400 (20 s).",
                        "The server sends this value to the client HUD. 0 = no cooldown.")
                .defineInRange("cooldown_ticks", 400, 0, 1000000);

        DISGUISE_MAX_DURATION_TICKS = builder
                .comment("Maximum time in ticks the disguise can stay on before it ends by itself (and the cooldown starts). Default 0 = unlimited (the old behaviour: it lasts until the Ghost toggles it off or takes damage).")
                .defineInRange("max_duration_ticks", 0, 0, 1000000);

        DISGUISE_BACKSTAB_BONUS_MULTIPLIER = builder
                .comment("Extra damage multiplier applied to a backstab made while disguised, on top of ghost.backstab.damage_multiplier. Default 1.5 (so x2 becomes x3).")
                .defineInRange("backstab_bonus_multiplier", 1.5D, 0.0D, 1000.0D);

        DISGUISE_RANGED_DAMAGE_MULTIPLIER = builder
                .comment("Damage multiplier of attacks made while disguised from farther than ranged_min_distance blocks (arrows, tridents, thrown weapons...). Default 1.5 (+50%).")
                .defineInRange("ranged_damage_multiplier", 1.5D, 0.0D, 1000.0D);

        DISGUISE_RANGED_MIN_DISTANCE = builder
                .comment("Distance in blocks above which the disguised ranged damage multiplier applies. Default 7.0.")
                .defineInRange("ranged_min_distance", 7.0D, 0.0D, 256.0D);

        builder.comment("Disguise stacks: let the disguise survive a limited number of attacks or kills instead of lasting until it is toggled off.")
                .push("stacks");

        DISGUISE_MAX_STACKS = builder
                .comment("Number of disguise stacks given each time the disguise starts. When the trigger chosen in consume_on happens a stack is used up; when none are left the disguise ends (and the cooldown starts).",
                        "Only used when consume_on is not NONE. Default 1.")
                .defineInRange("max_stacks", 1, 1, 1000);

        DISGUISE_STACK_CONSUME_ON = builder
                .comment("What uses up one disguise stack. Default NONE (stacks are never used: the disguise lasts until it is toggled off or the Ghost takes damage, like before).",
                        "ATTACK = every entity the disguised Ghost damages (melee or ranged, each damaged entity counts).",
                        "BACKSTAB = every backstab made while disguised.",
                        "KILL = every entity the disguised Ghost kills (backstab executions included).",
                        "While max_stacks is above 1 the remaining stacks are shown on the action bar.")
                .defineEnum("consume_on", StackConsume.NONE);

        builder.pop();
        builder.pop();

        // ---------------------------------------------------------------- ambushed
        builder.comment("Ambushed: penalty effect a disguised Ghost gets when another entity hurts it (the disguise is broken at the same time).")
                .push("ambushed");

        AMBUSHED_DURATION_TICKS = builder
                .comment("Duration of the Ambushed effect in ticks. Default 200 (10 s). 0 = the effect is never applied.")
                .defineInRange("duration_ticks", 200, 0, 1000000);

        AMBUSHED_MOVEMENT_SPEED_MODIFIER = builder
                .comment("Movement speed change while Ambushed, as a fraction of the total speed: -0.6 = 60% slower, 0 = no change. Default -0.60.")
                .defineInRange("movement_speed_modifier", -0.60D, -1.0D, 10.0D);

        AMBUSHED_OUTGOING_DAMAGE_MULTIPLIER = builder
                .comment("Multiplier of the damage the Ambushed entity deals. Default 0.50 (-50%).")
                .defineInRange("outgoing_damage_multiplier", 0.50D, 0.0D, 100.0D);

        AMBUSHED_INCOMING_DAMAGE_MULTIPLIER = builder
                .comment("Multiplier of the damage the Ambushed entity receives. Default 2.0 (double damage).")
                .defineInRange("incoming_damage_multiplier", 2.0D, 0.0D, 100.0D);

        builder.pop();

        // ---------------------------------------------------------------- critical wound
        builder.comment("Critical Wound: debuff a disguised Ghost's backstab puts on its target.")
                .push("critical_wound");

        CRITICAL_WOUND_DURATION_TICKS = builder
                .comment("Duration of the Critical Wound effect in ticks. Default 600 (30 s).")
                .defineInRange("duration_ticks", 600, 1, 1000000);

        CRITICAL_WOUND_MOVEMENT_SPEED_MODIFIER = builder
                .comment("Movement speed change of the wounded entity, as a fraction of its total speed: -0.30 = 30% slower, 0 = no change. Default -0.30.")
                .defineInRange("movement_speed_modifier", -0.30D, -1.0D, 10.0D);

        CRITICAL_WOUND_OUTGOING_DAMAGE_MULTIPLIER = builder
                .comment("Multiplier of the damage the wounded entity deals. Default 0.60 (-40%).")
                .defineInRange("outgoing_damage_multiplier", 0.60D, 0.0D, 100.0D);

        CRITICAL_WOUND_INCOMING_DAMAGE_MULTIPLIER = builder
                .comment("Multiplier of the damage the wounded entity receives. Default 1.0 (no change); e.g. 1.25 makes it take 25% more damage.")
                .defineInRange("incoming_damage_multiplier", 1.0D, 0.0D, 100.0D);

        CRITICAL_WOUND_ONLY_WHEN_DISGUISED = builder
                .comment("If true (default, the old behaviour) only a backstab made while disguised applies Critical Wound.",
                        "If false every backstab applies it.")
                .define("only_when_disguised", true);

        CRITICAL_WOUND_EXTRA_EFFECTS = builder
                .comment("Extra potion effects applied to the target together with Critical Wound. Default empty (none).",
                        "Format per entry: modid:effect;durationTicks;amplifier (amplifier 0 = level I). Example: \"minecraft:slowness;200;1\".")
                .defineListAllowEmpty(List.of("extra_effects"), ArrayList::new, () -> "", ConfigEffects::isValidEntry);

        CRITICAL_WOUND_ATTRIBUTE_MODIFIERS = builder
                .comment("Extra attribute debuffs held by the target for as long as Critical Wound lasts. Default empty (none).",
                        "Format per entry: modid:attribute;amount;operation where operation is add, multiply_base or multiply_total (default multiply_total).",
                        "Example: \"minecraft:generic.attack_speed;-0.25;multiply_total\" (25% slower attacks). The movement speed debuff has its own key above.")
                .defineListAllowEmpty(List.of("attribute_modifiers"), ArrayList::new, () -> "", GhostConfig::isValidAttributeModifierEntry);

        builder.pop();

        // ---------------------------------------------------------------- decoy
        builder.comment("Decoy: a bush decoy that draws the aggro of mobs that detect a disguised Ghost.")
                .push("decoy");

        DECOY_COOLDOWN_TICKS = builder
                .comment("Cooldown in ticks after summoning a decoy. Default 1000 (50 s).",
                        "The server sends this value to the client HUD. 0 = no cooldown.")
                .defineInRange("cooldown_ticks", 1000, 0, 1000000);

        DECOY_MAX_ACTIVE = builder
                .comment("Maximum number of decoys that can exist at once on the whole server (shared by every Ghost). When it is exceeded the oldest decoy disappears. Default 20.",
                        "Note that mobs are only redirected to the newest decoy of each Ghost.")
                .defineInRange("max_active", 20, 1, 1000);

        DECOY_LIFETIME_TICKS = builder
                .comment("Ticks a decoy stays in the world before it disappears by itself. Default 0 = it stays until something hits it or the limit above removes it.")
                .defineInRange("lifetime_ticks", 0, 0, 10000000);

        DECOY_ATTACKER_EFFECTS = builder
                .comment("Effects applied to a player who hits a decoy in melee. Default: Poison III for 30 s. Empty list = no effect.",
                        "Format per entry: modid:effect;durationTicks;amplifier (amplifier 0 = level I).")
                .defineListAllowEmpty(List.of("attacker_effects"), () -> List.of("minecraft:poison;600;2"), () -> "", ConfigEffects::isValidEntry);

        DECOY_AGGRO_RANGE = builder
                .comment("Range in blocks within which a mob that detects a disguised Ghost switches to a decoy, and beyond which a hooked mob loses it. Default 20.0.")
                .defineInRange("aggro_range", 20.0D, 1.0D, 256.0D);

        DECOY_ATTACK_RANGE = builder
                .comment("Distance in blocks at which a hooked mob attacks the decoy instead of walking to it. Default 3.0.")
                .defineInRange("attack_range", 3.0D, 0.5D, 64.0D);

        DECOY_MOVE_SPEED = builder
                .comment("Speed multiplier a hooked mob uses to walk towards the decoy. Default 1.0.")
                .defineInRange("move_speed", 1.0D, 0.1D, 10.0D);

        builder.pop();

        // ---------------------------------------------------------------- bucket of remains
        builder.comment("Bucket of Remains: pouring it makes spore infected treat the user as an ally for a while.")
                .push("bucket_of_remains");

        BUCKET_DURATION_TICKS = builder
                .comment("Duration in ticks of the Camouflaged effect given by the bucket. Default 600 (30 s).",
                        "The item tooltip shows this value (read from the client's own config).")
                .defineInRange("duration_ticks", DEFAULT_BUCKET_DURATION_TICKS, 1, 10000000);

        BUCKET_PURSUER_CLEAR_RADIUS = builder
                .comment("Radius in blocks around the user in which infected that already chase them are made to forget them when the bucket is poured. Default 48.0.")
                .defineInRange("pursuer_clear_radius", 48.0D, 0.0D, 256.0D);

        builder.pop();

        builder.pop();
    }

    /** Validator for the attribute modifier list: checks the syntax only, never the registry. */
    public static boolean isValidAttributeModifierEntry(Object o) {
        return o instanceof String s && ATTRIBUTE_ENTRY_SYNTAX.matcher(s.trim()).matches();
    }

    // ------------------------------------------------------------------------------------------------ accessors

    public static double backstabDamageMultiplier() {
        return BACKSTAB_DAMAGE_MULTIPLIER.get().doubleValue();
    }

    public static double backstabMaxDistance() {
        return BACKSTAB_MAX_DISTANCE.get().doubleValue();
    }

    public static double backstabAngleThreshold() {
        return BACKSTAB_ANGLE_THRESHOLD.get().doubleValue();
    }

    public static ExecutionMode executionMode() {
        return EXECUTION_THRESHOLD_MODE.get();
    }

    public static double executionThresholdHp() {
        return EXECUTION_THRESHOLD_HP.get().doubleValue();
    }

    public static double executionThresholdPercent() {
        return EXECUTION_THRESHOLD_PERCENT.get().doubleValue();
    }

    /**
     * Remaining health (in HP) below which a backstab executes {@code target}: the fixed HP value, or the configured
     * percentage of the target's max health.
     */
    public static float executionThreshold(LivingEntity target) {
        if (EXECUTION_THRESHOLD_MODE.get() == ExecutionMode.PERCENT) {
            return (float) (EXECUTION_THRESHOLD_PERCENT.get().doubleValue() / 100.0D * target.getMaxHealth());
        }
        return EXECUTION_THRESHOLD_HP.get().floatValue();
    }

    public static int disguiseCooldownTicks() {
        return DISGUISE_COOLDOWN_TICKS.get().intValue();
    }

    public static int disguiseMaxDurationTicks() {
        return DISGUISE_MAX_DURATION_TICKS.get().intValue();
    }

    public static double disguiseBackstabBonusMultiplier() {
        return DISGUISE_BACKSTAB_BONUS_MULTIPLIER.get().doubleValue();
    }

    public static float disguiseRangedDamageMultiplier() {
        return DISGUISE_RANGED_DAMAGE_MULTIPLIER.get().floatValue();
    }

    public static double disguiseRangedMinDistance() {
        return DISGUISE_RANGED_MIN_DISTANCE.get().doubleValue();
    }

    public static int disguiseMaxStacks() {
        return DISGUISE_MAX_STACKS.get().intValue();
    }

    public static StackConsume disguiseStackConsumeOn() {
        return DISGUISE_STACK_CONSUME_ON.get();
    }

    public static int ambushedDurationTicks() {
        return AMBUSHED_DURATION_TICKS.get().intValue();
    }

    public static double ambushedMovementSpeedModifier() {
        return AMBUSHED_MOVEMENT_SPEED_MODIFIER.get().doubleValue();
    }

    public static float ambushedOutgoingDamageMultiplier() {
        return AMBUSHED_OUTGOING_DAMAGE_MULTIPLIER.get().floatValue();
    }

    public static float ambushedIncomingDamageMultiplier() {
        return AMBUSHED_INCOMING_DAMAGE_MULTIPLIER.get().floatValue();
    }

    public static int criticalWoundDurationTicks() {
        return CRITICAL_WOUND_DURATION_TICKS.get().intValue();
    }

    public static double criticalWoundMovementSpeedModifier() {
        return CRITICAL_WOUND_MOVEMENT_SPEED_MODIFIER.get().doubleValue();
    }

    public static float criticalWoundOutgoingDamageMultiplier() {
        return CRITICAL_WOUND_OUTGOING_DAMAGE_MULTIPLIER.get().floatValue();
    }

    public static float criticalWoundIncomingDamageMultiplier() {
        return CRITICAL_WOUND_INCOMING_DAMAGE_MULTIPLIER.get().floatValue();
    }

    public static int decoyCooldownTicks() {
        return DECOY_COOLDOWN_TICKS.get().intValue();
    }

    public static int decoyMaxActive() {
        return DECOY_MAX_ACTIVE.get().intValue();
    }

    public static int decoyLifetimeTicks() {
        return DECOY_LIFETIME_TICKS.get().intValue();
    }

    public static double decoyAggroRange() {
        return DECOY_AGGRO_RANGE.get().doubleValue();
    }

    public static double decoyAttackRange() {
        return DECOY_ATTACK_RANGE.get().doubleValue();
    }

    public static double decoyMoveSpeed() {
        return DECOY_MOVE_SPEED.get().doubleValue();
    }

    public static int bucketDurationTicks() {
        return BUCKET_DURATION_TICKS.get().intValue();
    }

    public static double bucketPursuerClearRadius() {
        return BUCKET_PURSUER_CLEAR_RADIUS.get().doubleValue();
    }
}
