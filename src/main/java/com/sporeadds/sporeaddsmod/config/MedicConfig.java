package com.sporeadds.sporeaddsmod.config;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Medic class settings: injector, self-injection, throwable bandages, defibrillation, self-defibrillation
 * (including its minigame), medical interface and triage.
 *
 * <p>Defaults reproduce the behaviour that was hard-coded before these options existed. Read values with
 * {@code .get()} at USE time (never copy them into static finals: the config is not loaded yet while classes
 * initialise). Called from {@link SporeAddsConfig}'s static initialiser, before the spec is built.
 *
 * <p>Which side reads what: everything is decided on the server EXCEPT the options marked CLIENT in their comment
 * (action wheel entry and triage highlight). The Self Defibrillate minigame is drawn on the client but its settings
 * (time limit, icons) are read on the server and sent to the client when the minigame opens, so a dedicated server's
 * config is the one that counts.
 */
public final class MedicConfig {
    private MedicConfig() {
    }

    // ---- injector ----
    public static ForgeConfigSpec.BooleanValue INJECTOR_ALLOW_SELF_INJECTION;
    public static ForgeConfigSpec.BooleanValue INJECTOR_SELF_INJECTION_REQUIRES_SNEAK;
    public static ForgeConfigSpec.IntValue INJECTOR_SELF_INJECTION_COOLDOWN_TICKS;
    public static ForgeConfigSpec.IntValue INJECTOR_CHARGE_COST;
    public static ForgeConfigSpec.IntValue INJECTOR_XP_REFILL;

    /** Order = the order of the injector modes (instant health, haste, speed, resistance, regeneration, strength, invisibility, water breathing). */
    private static final String[] INJECTOR_EFFECT_KEYS = {
            "instant_health", "haste", "speed", "resistance", "regeneration", "strength", "invisibility", "water_breathing"
    };
    private static final String[] INJECTOR_EFFECT_NAMES = {
            "Instant Health", "Haste", "Speed", "Resistance", "Regeneration", "Strength", "Invisibility", "Water Breathing"
    };
    private static final int[] INJECTOR_DEFAULT_LEVELS = {1, 2, 2, 2, 2, 2, 1, 1};
    private static final int[] INJECTOR_DEFAULT_DURATIONS = {20, 600, 400, 400, 1000, 400, 600, 3000};
    public static final ForgeConfigSpec.IntValue[] INJECTOR_EFFECT_LEVEL = new ForgeConfigSpec.IntValue[INJECTOR_EFFECT_KEYS.length];
    public static final ForgeConfigSpec.IntValue[] INJECTOR_EFFECT_DURATION = new ForgeConfigSpec.IntValue[INJECTOR_EFFECT_KEYS.length];

    // ---- throwable bandages ----
    public static ForgeConfigSpec.IntValue BANDAGES_SELF_HEAL_USE_TICKS;
    public static ForgeConfigSpec.IntValue BANDAGES_SELF_HEAL_REGEN_TICKS;
    public static ForgeConfigSpec.IntValue BANDAGES_SELF_HEAL_REGEN_LEVEL;
    public static ForgeConfigSpec.IntValue BANDAGES_THROWN_HEAL_LEVEL;
    public static ForgeConfigSpec.IntValue BANDAGES_XP_ORBS;
    public static ForgeConfigSpec.IntValue BANDAGES_XP_ORB_VALUE;

    // ---- defibrillate (delayed defibrillation) ----
    public static ForgeConfigSpec.IntValue DEFIB_COOLDOWN_TICKS;
    public static ForgeConfigSpec.DoubleValue DEFIB_SEARCH_RADIUS;
    public static ForgeConfigSpec.IntValue DEFIB_MARK_DURATION_TICKS;
    public static ForgeConfigSpec.IntValue DEFIB_REVIVE_HEAL_LEVEL;
    public static ForgeConfigSpec.DoubleValue DEFIB_SHOCKWAVE_RADIUS;
    public static ForgeConfigSpec.DoubleValue DEFIB_SHOCKWAVE_DAMAGE;
    public static ForgeConfigSpec.DoubleValue DEFIB_KNOCKBACK_HORIZONTAL;
    public static ForgeConfigSpec.DoubleValue DEFIB_KNOCKBACK_VERTICAL;
    public static ForgeConfigSpec.IntValue POSTMORTEM_DURATION_TICKS;
    public static ForgeConfigSpec.DoubleValue POSTMORTEM_SPEED_REDUCTION;
    public static ForgeConfigSpec.DoubleValue POSTMORTEM_ATTACK_DAMAGE_REDUCTION;
    public static ForgeConfigSpec.DoubleValue POSTMORTEM_STEP_HEIGHT_BONUS;
    public static ForgeConfigSpec.DoubleValue POSTMORTEM_JUMP_REDUCTION;

    // ---- self defibrillate ----
    public static ForgeConfigSpec.DoubleValue SELF_DEFIB_HEALTH_THRESHOLD;
    public static ForgeConfigSpec.IntValue SELF_DEFIB_COOLDOWN_TICKS;
    public static ForgeConfigSpec.IntValue SELF_DEFIB_RESISTANCE_LEVEL;
    public static ForgeConfigSpec.IntValue SELF_DEFIB_RESISTANCE_DURATION_TICKS;
    public static ForgeConfigSpec.DoubleValue SELF_DEFIB_FAILURE_DAMAGE;
    public static ForgeConfigSpec.IntValue SELF_DEFIB_TIME_LIMIT_TICKS;
    public static ForgeConfigSpec.IntValue SELF_DEFIB_ICON_COUNT;
    public static ForgeConfigSpec.IntValue SELF_DEFIB_ICON_SIZE;
    public static ForgeConfigSpec.BooleanValue SELF_DEFIB_ICONS_MOVE;
    public static ForgeConfigSpec.DoubleValue SELF_DEFIB_ICON_SPEED;
    public static ForgeConfigSpec.IntValue CONDUCTOR_DEFIB_TICKS;
    public static ForgeConfigSpec.IntValue CONDUCTOR_RESISTANCE_TICKS;
    public static ForgeConfigSpec.IntValue CONDUCTOR_RESISTANCE_LEVEL;

    // ---- medical interface (medic block) ----
    public static ForgeConfigSpec.IntValue MEDIC_BLOCK_ANALYSIS_TICKS;

    // ---- triage (client) ----
    public static ForgeConfigSpec.DoubleValue TRIAGE_RANGE;
    public static ForgeConfigSpec.DoubleValue TRIAGE_SHOW_BELOW;
    public static ForgeConfigSpec.DoubleValue TRIAGE_MEDIUM_BELOW;
    public static ForgeConfigSpec.DoubleValue TRIAGE_CRITICAL_BELOW;

    static void define(ForgeConfigSpec.Builder builder) {
        builder.push("medic");

        // ---------------------------------------------------------------- injector
        builder.comment("Injector: hit a target to apply the selected effect to it.").push("injector");

        INJECTOR_ALLOW_SELF_INJECTION = builder
                .comment("If true the Medic can also inject the selected effect into themselves: right click while holding the",
                        "Injector (see self_injection_requires_sneak). It costs the same charge as hitting someone.",
                        "If false (default) the Injector only works by hitting another entity, as it always did.")
                .define("allow_self_injection", false);

        INJECTOR_SELF_INJECTION_REQUIRES_SNEAK = builder
                .comment("Only used when allow_self_injection is true: if true the Medic has to sneak while right clicking,",
                        "like the Syringe and the Bandages (default true). If false a plain right click injects.")
                .define("self_injection_requires_sneak", true);

        INJECTOR_SELF_INJECTION_COOLDOWN_TICKS = builder
                .comment("Only used when allow_self_injection is true: cooldown of the Injector after a self injection.",
                        "Unit: ticks (20 = 1 second). Default 20.")
                .defineInRange("self_injection_cooldown_ticks", 20, 0, 72000);

        INJECTOR_CHARGE_COST = builder
                .comment("Charge used by each injection. The Injector holds 100 charge points, so 5 gives 20 injections.",
                        "Unit: charge points. 0 = injections are free. Default 5.")
                .defineInRange("charge_cost_per_use", 5, 0, 100);

        INJECTOR_XP_REFILL = builder
                .comment("Charge points the Injector recovers each time its holder picks up an experience orb.",
                        "Unit: charge points. 0 = it never recharges. Default 1.")
                .defineInRange("xp_refill_per_orb", 1, 0, 100);

        builder.comment("Level and duration of each Injector mode. Level 1 = potion level I, 2 = II, ...",
                "The names shown in the action wheel and tooltip keep saying the original level.").push("effects");
        for (int i = 0; i < INJECTOR_EFFECT_KEYS.length; i++) {
            builder.comment(INJECTOR_EFFECT_NAMES[i] + " mode of the Injector.").push(INJECTOR_EFFECT_KEYS[i]);

            INJECTOR_EFFECT_LEVEL[i] = builder
                    .comment("Potion level of the effect. Default " + INJECTOR_DEFAULT_LEVELS[i] + ".")
                    .defineInRange("level", INJECTOR_DEFAULT_LEVELS[i], 1, 10);

            INJECTOR_EFFECT_DURATION[i] = builder
                    .comment("Duration of the effect. Unit: ticks (20 = 1 second). Default " + INJECTOR_DEFAULT_DURATIONS[i] + ".")
                    .defineInRange("duration_ticks", INJECTOR_DEFAULT_DURATIONS[i], 1, 1000000);

            builder.pop();
        }
        builder.pop();

        builder.pop();

        // ---------------------------------------------------------------- bandages
        builder.comment("Throwable Bandages.").push("bandages");

        BANDAGES_SELF_HEAL_USE_TICKS = builder
                .comment("How long the Medic must keep sneaking with the Bandages in hand to bandage themselves.",
                        "Unit: ticks (20 = 1 second). Default 20.",
                        "The server applies the heal, but each client also ends the hold using its own copy of this value, so keep it equal everywhere.")
                .defineInRange("self_heal_use_ticks", 20, 1, 1200);

        BANDAGES_SELF_HEAL_REGEN_TICKS = builder
                .comment("Duration of the Regeneration given by bandaging yourself. Unit: ticks (20 = 1 second). Default 20.")
                .defineInRange("self_heal_regeneration_ticks", 20, 1, 72000);

        BANDAGES_SELF_HEAL_REGEN_LEVEL = builder
                .comment("Potion level of the Regeneration given by bandaging yourself (1 = I, 5 = V). Default 5.")
                .defineInRange("self_heal_regeneration_level", 5, 1, 10);

        BANDAGES_THROWN_HEAL_LEVEL = builder
                .comment("Instant Health level applied to whoever a thrown bandage hits (1 = heals 2 hearts, 2 = 4 hearts, 3 = 8 hearts, ...).",
                        "Default 1.")
                .defineInRange("thrown_heal_level", 1, 1, 10);

        BANDAGES_XP_ORBS = builder
                .comment("Experience orbs the thrower receives each time a thrown bandage hits someone (they recharge the Injector).",
                        "Unit: orbs. 0 = none. Default 3.")
                .defineInRange("xp_orbs_on_hit", 3, 0, 20);

        BANDAGES_XP_ORB_VALUE = builder
                .comment("Experience points inside each of those orbs. Default 11.")
                .defineInRange("xp_orb_value", 11, 0, 1000);

        builder.pop();

        // ---------------------------------------------------------------- defibrillate
        builder.comment("Defibrillate (the delayed defibrillation skill used on others).").push("defibrillate");

        DEFIB_COOLDOWN_TICKS = builder
                .comment("Cooldown of Defibrillate. Unit: ticks (20 = 1 second). Default 1600 (80 s).")
                .defineInRange("cooldown_ticks", 1600, 0, 1000000);

        DEFIB_SEARCH_RADIUS = builder
                .comment("Radius around the Medic in which the target (lowest health non-spore entity) is looked for. Unit: blocks. Default 4.")
                .defineInRange("search_radius", 4.0D, 0.5D, 64.0D);

        DEFIB_MARK_DURATION_TICKS = builder
                .comment("How long the target stays marked: if it would die during this time the shockwave revives it instead.",
                        "Unit: ticks (20 = 1 second). Default 300 (15 s).")
                .defineInRange("mark_duration_ticks", 300, 1, 1000000);

        DEFIB_REVIVE_HEAL_LEVEL = builder
                .comment("Instant Health level applied on revive (5 = a full heal for any normal health bar). Default 5.")
                .defineInRange("revive_heal_level", 5, 1, 10);

        DEFIB_SHOCKWAVE_RADIUS = builder
                .comment("Radius of the shockwave that hurts everything around the revived entity. Unit: blocks. Default 4.")
                .defineInRange("shockwave_radius", 4.0D, 0.0D, 64.0D);

        DEFIB_SHOCKWAVE_DAMAGE = builder
                .comment("Lightning damage the shockwave deals to every other living entity inside its radius.",
                        "Unit: health points (2 = 1 heart). Default 40.")
                .defineInRange("shockwave_damage", 40.0D, 0.0D, 10000.0D);

        DEFIB_KNOCKBACK_HORIZONTAL = builder
                .comment("Horizontal knockback of the shockwave. Unit: blocks per tick of initial speed. Default 1.4.")
                .defineInRange("shockwave_knockback_horizontal", 1.4D, 0.0D, 20.0D);

        DEFIB_KNOCKBACK_VERTICAL = builder
                .comment("Vertical knockback of the shockwave. Unit: blocks per tick of initial speed. Default 0.6.")
                .defineInRange("shockwave_knockback_vertical", 0.6D, 0.0D, 20.0D);

        POSTMORTEM_DURATION_TICKS = builder
                .comment("Duration of Postmortem, the debuff left on the revived entity. Unit: ticks (20 = 1 second). Default 3000 (150 s).")
                .defineInRange("postmortem_duration_ticks", 3000, 1, 1000000);

        builder.comment("Strength of the Postmortem debuff applied to the revived entity.").push("postmortem");

        POSTMORTEM_SPEED_REDUCTION = builder
                .comment("Movement speed removed. Unit: fraction 0-1 (0.6 = -60% speed). Default 0.6.")
                .defineInRange("speed_reduction", 0.60D, 0.0D, 1.0D);

        POSTMORTEM_ATTACK_DAMAGE_REDUCTION = builder
                .comment("Attack damage removed. Unit: health points (2 = 1 heart). Default 5.")
                .defineInRange("attack_damage_reduction", 5.0D, 0.0D, 1000.0D);

        POSTMORTEM_STEP_HEIGHT_BONUS = builder
                .comment("Extra step height (how high it can walk up without jumping). Unit: blocks. Default 1.")
                .defineInRange("step_height_bonus", 1.0D, 0.0D, 10.0D);

        POSTMORTEM_JUMP_REDUCTION = builder
                .comment("How much weaker every jump is. Unit: fraction 0-1 (0.5 = jumps reach half as high). Default 0.5.",
                        "Jumps are predicted by the client, so keep this value equal on the server and on the clients.")
                .defineInRange("jump_reduction", 0.50D, 0.0D, 0.9D);

        builder.pop();

        builder.pop();

        // ---------------------------------------------------------------- self defibrillate
        builder.comment("Self Defibrillate: the last-resort minigame of a nearly dead Medic.").push("self_defibrillate");

        SELF_DEFIB_HEALTH_THRESHOLD = builder
                .comment("Health below which Self Defibrillate can be used. Unit: fraction of max health, 0-1 (0.3 = 30%). Default 0.3.",
                        "The action wheel entry is shown by each client using its own copy of this value, so keep it equal everywhere.")
                .defineInRange("health_threshold", 0.30D, 0.0D, 1.0D);

        SELF_DEFIB_COOLDOWN_TICKS = builder
                .comment("Cooldown of Self Defibrillate, counted from the moment the minigame starts. Unit: ticks (20 = 1 second).",
                        "Default 1000 (50 s).")
                .defineInRange("cooldown_ticks", 1000, 0, 1000000);

        SELF_DEFIB_RESISTANCE_LEVEL = builder
                .comment("Damage reduction while the minigame runs, as a Resistance potion level: each level removes 20% of the",
                        "damage (4 = -80%, default; 5 = immune). 0 = no protection.")
                .defineInRange("resistance_level", 4, 0, 5);

        SELF_DEFIB_RESISTANCE_DURATION_TICKS = builder
                .comment("How long that protection lasts. Unit: ticks (20 = 1 second). Default 80 (4 s).",
                        "Keep it at least time_limit_ticks + 20 or the Medic is unprotected at the end of a long minigame.")
                .defineInRange("resistance_duration_ticks", 80, 1, 12000);

        SELF_DEFIB_FAILURE_DAMAGE = builder
                .comment("Lightning damage the Medic takes when the minigame fails (wrong icon or time out). Unit: health points. Default 1000 (certain death).")
                .defineInRange("failure_damage", 1000.0D, 0.0D, 100000.0D);

        builder.comment("The click-the-real-icon minigame. Sent by the server to the player's client when it opens.").push("minigame");

        SELF_DEFIB_TIME_LIMIT_TICKS = builder
                .comment("Time the Medic has to click the real icon. Unit: ticks (20 = 1 second). Default 60 (3 s).")
                .defineInRange("time_limit_ticks", 60, 20, 2400);

        SELF_DEFIB_ICON_COUNT = builder
                .comment("Icons on screen (1 real + the rest fakes). If they do not fit on the screen fewer are drawn. Default 75.")
                .defineInRange("icon_count", 75, 1, 500);

        SELF_DEFIB_ICON_SIZE = builder
                .comment("Size of each icon. Unit: GUI pixels (the minigame forces GUI scale 2). Default 50.")
                .defineInRange("icon_size", 50, 8, 200);

        SELF_DEFIB_ICONS_MOVE = builder
                .comment("If true the icons drift around the screen and bounce off its edges (they can pass over each other).",
                        "If false (default) they stay where they are placed.")
                .define("icons_move", false);

        SELF_DEFIB_ICON_SPEED = builder
                .comment("Only used when icons_move is true: speed of the icons. Unit: GUI pixels per tick (20 ticks = 1 second). Default 2.")
                .defineInRange("icon_speed", 2.0D, 0.0D, 50.0D);

        builder.pop();

        builder.comment("What happens when a Medic dies during the minigame after being hit by the spore team: they turn into a Conductor.")
                .push("conductor");

        CONDUCTOR_DEFIB_TICKS = builder
                .comment("Delayed Defibrillation given to the Conductor that replaces the Medic. Unit: ticks (20 = 1 second). Default 1200 (60 s).")
                .defineInRange("delayed_defibrillation_ticks", 1200, 1, 1000000);

        CONDUCTOR_RESISTANCE_TICKS = builder
                .comment("Resistance given to that Conductor when it spawns. Unit: ticks (20 = 1 second). Default 100 (5 s).")
                .defineInRange("resistance_ticks", 100, 1, 1000000);

        CONDUCTOR_RESISTANCE_LEVEL = builder
                .comment("Potion level of that Resistance (3 = -60% damage). Default 3.")
                .defineInRange("resistance_level", 3, 1, 5);

        builder.pop();

        builder.pop();

        // ---------------------------------------------------------------- medic block
        builder.comment("Medical interface (medic block).").push("medic_block");

        MEDIC_BLOCK_ANALYSIS_TICKS = builder
                .comment("Time the medical interface needs to turn a filled syringe into a vaccine. Unit: ticks (20 = 1 second).",
                        "Default 5000 (about 4 minutes 10 seconds).")
                .defineInRange("analysis_ticks", 5000, 1, 1000000);

        builder.pop();

        // ---------------------------------------------------------------- triage
        builder.comment("CLIENT SIDE (each player's own config file): Triage, the Medic's wall-hack highlight of hurt entities.").push("triage");

        TRIAGE_RANGE = builder
                .comment("Distance within which hurt entities are highlighted through walls. Unit: blocks. Default 32.")
                .defineInRange("range", 32.0D, 1.0D, 256.0D);

        TRIAGE_SHOW_BELOW = builder
                .comment("Entities with MORE health than this are not highlighted. Unit: fraction of max health, 0-1. Default 0.9.")
                .defineInRange("show_below", 0.90D, 0.0D, 1.0D);

        TRIAGE_MEDIUM_BELOW = builder
                .comment("Below this health the highlight turns orange (between this and show_below it is green).",
                        "Unit: fraction of max health, 0-1. Default 0.6.")
                .defineInRange("medium_below", 0.60D, 0.0D, 1.0D);

        TRIAGE_CRITICAL_BELOW = builder
                .comment("Below this health the highlight turns red. Unit: fraction of max health, 0-1. Default 0.3.")
                .defineInRange("critical_below", 0.30D, 0.0D, 1.0D);

        builder.pop();

        builder.pop();
    }

    // ===================================================================================================== accessors

    public static boolean injectorAllowSelfInjection() {
        return INJECTOR_ALLOW_SELF_INJECTION.get();
    }

    public static boolean injectorSelfInjectionRequiresSneak() {
        return INJECTOR_SELF_INJECTION_REQUIRES_SNEAK.get();
    }

    public static int injectorSelfInjectionCooldownTicks() {
        return INJECTOR_SELF_INJECTION_COOLDOWN_TICKS.get();
    }

    public static int injectorChargeCost() {
        return INJECTOR_CHARGE_COST.get();
    }

    public static int injectorXpRefill() {
        return INJECTOR_XP_REFILL.get();
    }

    /** Amplifier (level - 1) of injector mode {@code index}; {@code fallback} if the index has no setting. */
    public static int injectorEffectAmplifier(int index, int fallback) {
        if (index < 0 || index >= INJECTOR_EFFECT_LEVEL.length) {
            return fallback;
        }
        return INJECTOR_EFFECT_LEVEL[index].get() - 1;
    }

    /** Duration in ticks of injector mode {@code index}; {@code fallback} if the index has no setting. */
    public static int injectorEffectDurationTicks(int index, int fallback) {
        if (index < 0 || index >= INJECTOR_EFFECT_DURATION.length) {
            return fallback;
        }
        return INJECTOR_EFFECT_DURATION[index].get();
    }

    public static int bandagesSelfHealUseTicks() {
        return BANDAGES_SELF_HEAL_USE_TICKS.get();
    }

    public static int bandagesSelfHealRegenerationTicks() {
        return BANDAGES_SELF_HEAL_REGEN_TICKS.get();
    }

    public static int bandagesSelfHealRegenerationAmplifier() {
        return BANDAGES_SELF_HEAL_REGEN_LEVEL.get() - 1;
    }

    public static int bandagesThrownHealAmplifier() {
        return BANDAGES_THROWN_HEAL_LEVEL.get() - 1;
    }

    public static int bandagesXpOrbCount() {
        return BANDAGES_XP_ORBS.get();
    }

    public static int bandagesXpOrbValue() {
        return BANDAGES_XP_ORB_VALUE.get();
    }

    /** Cooldown of Defibrillate in ticks (also what the HUD shows: it is sent by the server). */
    public static int defibrillateCooldownTicks() {
        return DEFIB_COOLDOWN_TICKS.get();
    }

    public static double defibrillateSearchRadius() {
        return DEFIB_SEARCH_RADIUS.get();
    }

    public static int defibrillateMarkDurationTicks() {
        return DEFIB_MARK_DURATION_TICKS.get();
    }

    public static int defibrillateReviveHealAmplifier() {
        return DEFIB_REVIVE_HEAL_LEVEL.get() - 1;
    }

    public static double defibrillateShockwaveRadius() {
        return DEFIB_SHOCKWAVE_RADIUS.get();
    }

    public static float defibrillateShockwaveDamage() {
        return DEFIB_SHOCKWAVE_DAMAGE.get().floatValue();
    }

    public static float defibrillateKnockbackHorizontal() {
        return DEFIB_KNOCKBACK_HORIZONTAL.get().floatValue();
    }

    public static float defibrillateKnockbackVertical() {
        return DEFIB_KNOCKBACK_VERTICAL.get().floatValue();
    }

    public static int postmortemDurationTicks() {
        return POSTMORTEM_DURATION_TICKS.get();
    }

    /** Fraction 0-1 of movement speed removed (0.6 by default). */
    public static double postmortemSpeedReduction() {
        return POSTMORTEM_SPEED_REDUCTION.get();
    }

    public static double postmortemAttackDamageReduction() {
        return POSTMORTEM_ATTACK_DAMAGE_REDUCTION.get();
    }

    public static double postmortemStepHeightBonus() {
        return POSTMORTEM_STEP_HEIGHT_BONUS.get();
    }

    /** Fraction 0-1 of the jump removed (0.5 by default). */
    public static double postmortemJumpReduction() {
        return POSTMORTEM_JUMP_REDUCTION.get();
    }

    /** Health fraction (0-1) under which Self Defibrillate is available. Used by the server AND by each client's action wheel. */
    public static float selfDefibrillateHealthThreshold() {
        return SELF_DEFIB_HEALTH_THRESHOLD.get().floatValue();
    }

    public static int selfDefibrillateCooldownTicks() {
        return SELF_DEFIB_COOLDOWN_TICKS.get();
    }

    /** Resistance amplifier (level - 1) given while the minigame runs; -1 = no protection. */
    public static int selfDefibrillateResistanceAmplifier() {
        return SELF_DEFIB_RESISTANCE_LEVEL.get() - 1;
    }

    public static int selfDefibrillateResistanceDurationTicks() {
        return SELF_DEFIB_RESISTANCE_DURATION_TICKS.get();
    }

    public static float selfDefibrillateFailureDamage() {
        return SELF_DEFIB_FAILURE_DAMAGE.get().floatValue();
    }

    public static int selfDefibrillateTimeLimitTicks() {
        return SELF_DEFIB_TIME_LIMIT_TICKS.get();
    }

    public static int selfDefibrillateIconCount() {
        return SELF_DEFIB_ICON_COUNT.get();
    }

    public static int selfDefibrillateIconSize() {
        return SELF_DEFIB_ICON_SIZE.get();
    }

    public static boolean selfDefibrillateIconsMove() {
        return SELF_DEFIB_ICONS_MOVE.get();
    }

    public static float selfDefibrillateIconSpeed() {
        return SELF_DEFIB_ICON_SPEED.get().floatValue();
    }

    public static int conductorDefibrillationTicks() {
        return CONDUCTOR_DEFIB_TICKS.get();
    }

    public static int conductorResistanceTicks() {
        return CONDUCTOR_RESISTANCE_TICKS.get();
    }

    public static int conductorResistanceAmplifier() {
        return CONDUCTOR_RESISTANCE_LEVEL.get() - 1;
    }

    public static int medicBlockAnalysisTicks() {
        return MEDIC_BLOCK_ANALYSIS_TICKS.get();
    }

    public static double triageRange() {
        return TRIAGE_RANGE.get();
    }

    public static float triageShowBelow() {
        return TRIAGE_SHOW_BELOW.get().floatValue();
    }

    public static float triageMediumBelow() {
        return TRIAGE_MEDIUM_BELOW.get().floatValue();
    }

    public static float triageCriticalBelow() {
        return TRIAGE_CRITICAL_BELOW.get().floatValue();
    }
}
