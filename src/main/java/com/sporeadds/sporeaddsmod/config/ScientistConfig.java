package com.sporeadds.sporeaddsmod.config;

import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

/**
 * Scientist class settings: research notes (the "Data" drops), field research, expose weakness, weak points,
 * prestige, nano injector and research unit.
 *
 * <p>Defaults reproduce the behaviour that was hard-coded before these options existed. Read values with
 * {@code .get()} at USE time (never copy them into static finals: the config is not loaded yet while classes
 * initialise). Called from {@link SporeAddsConfig}'s static initialiser, before the spec is built.
 *
 * <p>Which side reads what: everything is decided on the server EXCEPT the options marked CLIENT in their comment
 * (they only change what the local player sees). The numbers that the Field Research book displays (prestige
 * requirements, weak point damage formula, cooldown) are sent by the server together with the book data
 * ({@link ResearchRules}), so the book is right even when the client's own config file differs.
 */
public final class ScientistConfig {
    private ScientistConfig() {
    }

    /** How the weak point (critical hit) bonus is added to the hit. */
    public enum DamageMode {
        FLAT,
        PERCENT
    }

    public static final int PRESTIGE_RARITIES = 4;

    // ---- research data ("Data" items dropped by the scalpel) ----
    public static ModConfigSpec.IntValue DATA_PER_HIT;
    public static ModConfigSpec.IntValue DATA_PER_KILL;
    public static ModConfigSpec.DoubleValue DATA_PICKUP_RADIUS;
    public static ModConfigSpec.IntValue DATA_LIFETIME_TICKS;

    // ---- field research ----
    public static ModConfigSpec.IntValue FIELD_RESEARCH_COOLDOWN_TICKS;
    public static ModConfigSpec.IntValue FIELD_RESEARCH_ENTRIES_PER_PAGE;

    // ---- expose weakness ----
    public static ModConfigSpec.IntValue EXPOSE_COOLDOWN_TICKS;
    public static ModConfigSpec.DoubleValue EXPOSE_RADIUS;
    public static ModConfigSpec.IntValue EXPOSE_DURATION_TICKS;

    // ---- weak points ----
    public static ModConfigSpec.IntValue WEAK_POINT_AMOUNT;
    public static ModConfigSpec.DoubleValue WEAK_POINT_HIT_RADIUS;
    public static ModConfigSpec.IntValue WEAK_POINT_MAX_CRITICAL_HITS;
    public static ModConfigSpec.EnumValue<DamageMode> DAMAGE_MODE;
    public static ModConfigSpec.DoubleValue DAMAGE_PERCENT_PER_POINT;
    public static ModConfigSpec.DoubleValue DAMAGE_DATA_PER_POINT;
    public static ModConfigSpec.IntValue DAMAGE_KILL_VALUE_IN_DATA;
    public static ModConfigSpec.IntValue DAMAGE_SOFT_CAP_BLOCK_SIZE;
    public static ModConfigSpec.DoubleValue DAMAGE_SOFT_CAP_DECAY;
    public static ModConfigSpec.DoubleValue DAMAGE_MAX_BONUS_VS_PLAYERS;

    // ---- prestige ----
    public static ModConfigSpec.DoubleValue PRESTIGE_LOOT_MULTIPLIER;
    public static final ModConfigSpec.IntValue[] PRESTIGE_KILLS = new ModConfigSpec.IntValue[PRESTIGE_RARITIES];
    public static final ModConfigSpec.IntValue[] PRESTIGE_DATA = new ModConfigSpec.IntValue[PRESTIGE_RARITIES];

    // ---- nano injector ----
    public static ModConfigSpec.IntValue NANO_MAX_CHARGES;
    public static ModConfigSpec.DoubleValue NANO_RANGE;
    public static ModConfigSpec.IntValue NANO_FREEZE_TICKS;
    public static ModConfigSpec.IntValue NANO_EFFECT_DURATION_TICKS;
    public static ModConfigSpec.DoubleValue NANO_VULNERABLE_MULTIPLIER;
    public static ModConfigSpec.ConfigValue<List<? extends String>> NANO_RELOAD_COST;

    // ---- research unit (scientist block) ----
    public static ModConfigSpec.IntValue RESEARCH_UNIT_TICKS_PER_BIOMASS;

    private static final int[] DEFAULT_PRESTIGE_KILLS = {20, 10, 5, 2};
    private static final int[] DEFAULT_PRESTIGE_DATA = {500, 250, 125, 50};

    static void define(ModConfigSpec.Builder builder) {
        builder.push("scientist");

        // ---------------------------------------------------------------- research data
        builder.comment("The 'Data' items (research notes) that the Scalpel makes drop from spore creatures.").push("research_data");

        DATA_PER_HIT = builder
                .comment("Data dropped by the Scalpel on each hit against a researchable spore entity or a Kommandant player.",
                        "Unit: items per hit (max 64). 0 = no Data on hit. Default 1.")
                .defineInRange("data_per_hit", 1, 0, 64);

        DATA_PER_KILL = builder
                .comment("Data dropped when a Scientist kills a researchable entity (or a Kommandant player) with the Scalpel.",
                        "Unit: items per kill (max 64). 0 = no Data on kill. Default 10.")
                .defineInRange("data_per_kill", 10, 0, 64);

        DATA_PICKUP_RADIUS = builder
                .comment("Pickup radius of Data drops, in blocks around the Scientist. Inside this radius the Data is collected",
                        "automatically (like a magnet), no need to touch it. 0 = vanilla pickup only: the Scientist has to walk",
                        "over it (about 1 block). Only Scientists can collect Data, whatever the radius. Default 0 (off).")
                .defineInRange("pickup_radius", 0.0D, 0.0D, 64.0D);

        DATA_LIFETIME_TICKS = builder
                .comment("How long a Data drop stays on the ground before it disappears. Unit: ticks (20 = 1 second). Default 400 (20 s).")
                .defineInRange("lifetime_ticks", 400, 20, 72000);

        builder.pop();

        // ---------------------------------------------------------------- field research
        builder.comment("Field Research: the book with every researched spore entity.").push("field_research");

        FIELD_RESEARCH_COOLDOWN_TICKS = builder
                .comment("Cooldown after opening Field Research from the action wheel. Unit: ticks (20 = 1 second).",
                        "0 = no cooldown, it opens any time (default 0). The server enforces it and the HUD shows the countdown.")
                .defineInRange("cooldown_ticks", 0, 0, 72000);

        FIELD_RESEARCH_ENTRIES_PER_PAGE = builder
                .comment("CLIENT SIDE (each player's own config file): how many entries each page of the Field Research book shows.",
                        "1 = the original layout, one large entry per page (default). Higher values draw smaller cards in a grid on each",
                        "page (2 = two side by side, 4 = a 2x2 grid, 9 = 3x3, ...) so you flip far fewer pages.")
                .defineInRange("entries_per_page", 1, 1, 12);

        builder.pop();

        // ---------------------------------------------------------------- expose weakness
        builder.comment("Expose Weakness: marks nearby researched entities with weak points.").push("expose_weakness");

        EXPOSE_COOLDOWN_TICKS = builder
                .comment("Cooldown of Expose Weakness. Unit: ticks (20 = 1 second). Default 300 (15 s).")
                .defineInRange("cooldown_ticks", 300, 0, 72000);

        EXPOSE_RADIUS = builder
                .comment("Radius of the scan around the Scientist. Unit: blocks. Default 32.")
                .defineInRange("radius", 32.0D, 1.0D, 256.0D);

        EXPOSE_DURATION_TICKS = builder
                .comment("How long the Exposed Weakness effect (and its weak points) lasts on each target. Unit: ticks (20 = 1 second).",
                        "Default 600 (30 s).")
                .defineInRange("duration_ticks", 600, 1, 1000000);

        builder.pop();

        // ---------------------------------------------------------------- weak points
        builder.comment("Weak points: the markers hitting which deals bonus damage to an exposed target.").push("weak_points");

        WEAK_POINT_AMOUNT = builder
                .comment("Number of weak points on each exposed target. Hitting any of them gives the critical bonus and only the",
                        "weak point that was hit moves to a new spot. Default 1.")
                .defineInRange("amount", 1, 1, 8);

        WEAK_POINT_HIT_RADIUS = builder
                .comment("Size of a weak point: how close the attacker's line of sight must pass to its centre to count as a hit.",
                        "Unit: blocks (radius). The marker drawn on screen grows or shrinks with it (the server tells the clients).",
                        "Default 0.3.")
                .defineInRange("hit_radius", 0.3D, 0.05D, 2.0D);

        WEAK_POINT_MAX_CRITICAL_HITS = builder
                .comment("How many critical hits a target takes before its Exposed Weakness effect is removed early.",
                        "0 = unlimited, the effect only ends when its duration runs out (default 0).")
                .defineInRange("max_critical_hits", 0, 0, 1000);

        builder.comment("How much a critical hit adds. The bonus 'points' come from the Data and kills of every online Scientist:",
                        "(Data + kills * kill_value_in_data) is soft-capped, then divided by data_per_damage_point.",
                        "The Field Research book shows the resulting number for each entity.").push("damage");

        DAMAGE_MODE = builder
                .comment("FLAT = the bonus points are added to the damage of the hit (default, the original rule).",
                        "PERCENT = the hit is multiplied by (1 + points * percent_per_point / 100).")
                .defineEnum("mode", DamageMode.FLAT);

        DAMAGE_PERCENT_PER_POINT = builder
                .comment("Only used when mode = PERCENT: percent of extra damage given by each bonus point.",
                        "Unit: percent (10 = +10% per point, so a bonus of 7.5 points is +75% damage). Default 10.")
                .defineInRange("percent_per_point", 10.0D, 0.0D, 1000.0D);

        DAMAGE_DATA_PER_POINT = builder
                .comment("Data points needed for 1 point of bonus damage (after the soft cap). Lower = stronger critical hits.",
                        "Default 20.")
                .defineInRange("data_per_damage_point", 20.0D, 0.01D, 100000.0D);

        DAMAGE_KILL_VALUE_IN_DATA = builder
                .comment("How many Data points each kill of an entity is worth when computing its weak point bonus. Default 20.")
                .defineInRange("kill_value_in_data", 20, 0, 100000);

        DAMAGE_SOFT_CAP_BLOCK_SIZE = builder
                .comment("Soft cap: the Data is counted in blocks of this size and each block counts less than the previous one",
                        "(see soft_cap_decay). Unit: Data points. Default 200.")
                .defineInRange("soft_cap_block_size", 200, 1, 1000000);

        DAMAGE_SOFT_CAP_DECAY = builder
                .comment("Soft cap: weight multiplier applied after each block (the first block counts 1.0, the second decay,",
                        "the third decay squared, ...). 0.5 = every block counts half of the previous one (default).",
                        "1.0 = no soft cap at all.")
                .defineInRange("soft_cap_decay", 0.5D, 0.0D, 1.0D);

        DAMAGE_MAX_BONUS_VS_PLAYERS = builder
                .comment("Maximum bonus points a weak point can give against a player (Kommandant). Default 15.")
                .defineInRange("max_bonus_vs_players", 15.0D, 0.0D, 1000.0D);

        builder.pop();
        builder.pop();

        // ---------------------------------------------------------------- prestige
        builder.comment("Prestige: an entity becomes Prestiged once the kills and Data of a Scientist reach the requirements of its",
                "rarity. Killing a Prestiged entity through a weak point multiplies its loot.").push("prestige");

        PRESTIGE_LOOT_MULTIPLIER = builder
                .comment("Loot multiplier when a Prestiged entity is killed through a weak point. Multiplies the quantity of every",
                        "drop. 2.0 = double loot (default), 1.0 = no extra loot, 3.0 = triple, ...")
                .defineInRange("loot_multiplier", 2.0D, 1.0D, 100.0D);

        for (int i = 0; i < PRESTIGE_RARITIES; i++) {
            int rarity = i + 1;
            builder.comment("Requirements to Prestige a rarity " + rarity + " entity. Both must be reached by the same Scientist.")
                    .push("rarity_" + rarity);

            PRESTIGE_KILLS[i] = builder
                    .comment("Kills of that entity needed. Default " + DEFAULT_PRESTIGE_KILLS[i] + ".")
                    .defineInRange("kills", DEFAULT_PRESTIGE_KILLS[i], 0, 1000000);

            PRESTIGE_DATA[i] = builder
                    .comment("Data of that entity needed. Default " + DEFAULT_PRESTIGE_DATA[i] + ".")
                    .defineInRange("data", DEFAULT_PRESTIGE_DATA[i], 0, 1000000);

            builder.pop();
        }

        builder.pop();

        // ---------------------------------------------------------------- nano injector
        builder.comment("Nano Injector: freezes a spore entity and makes it Vulnerable.").push("nano_injector");

        NANO_MAX_CHARGES = builder
                .comment("Maximum charges the Nano Injector can hold. Default 5.",
                        "The 'Charges: x / max' line of the item tooltip uses each client's own copy of this value, so keep it equal everywhere.")
                .defineInRange("max_charges", 5, 1, 64);

        NANO_RANGE = builder
                .comment("Range of the shot. Unit: blocks. Default 35.")
                .defineInRange("range", 35.0D, 1.0D, 256.0D);

        NANO_FREEZE_TICKS = builder
                .comment("Freeze time given to a hit spore entity (powder snow freezing). Unit: ticks (20 = 1 second). Default 800 (40 s).")
                .defineInRange("freeze_ticks", 800, 0, 1000000);

        NANO_EFFECT_DURATION_TICKS = builder
                .comment("Duration of the Glowing and Vulnerable effects on a hit target. Unit: ticks (20 = 1 second). Default 400 (20 s).")
                .defineInRange("effect_duration_ticks", 400, 1, 1000000);

        NANO_VULNERABLE_MULTIPLIER = builder
                .comment("Damage multiplier taken by an entity with the Vulnerable effect (from any source).",
                        "1.25 = +25% damage (default).")
                .defineInRange("vulnerable_damage_multiplier", 1.25D, 0.1D, 10.0D);

        NANO_RELOAD_COST = builder
                .comment("Items consumed for each charge when reloading (sneak + right click in the air). Format modid:item or modid:item;count.",
                        "Default: 1 packed ice per charge. An empty list makes reloading free.")
                .defineListAllowEmpty(List.of("reload_cost"), () -> List.of("minecraft:packed_ice;1"), () -> "", ConfigItems::isValidEntry);

        builder.pop();

        // ---------------------------------------------------------------- research unit
        builder.comment("Research Unit (scientist block).").push("research_unit");

        RESEARCH_UNIT_TICKS_PER_BIOMASS = builder
                .comment("Time the Research Unit needs to turn one frozen biomass into research progress.",
                        "Unit: ticks (20 = 1 second). Default 100 (5 s).")
                .defineInRange("ticks_per_biomass", 100, 1, 72000);

        builder.pop();

        builder.pop();
    }

    // ===================================================================================================== accessors

    public static int dataPerHit() {
        return DATA_PER_HIT.get();
    }

    public static int dataPerKill() {
        return DATA_PER_KILL.get();
    }

    /** Blocks; 0 = vanilla touch pickup only. */
    public static double dataPickupRadius() {
        return DATA_PICKUP_RADIUS.get();
    }

    public static int dataLifetimeTicks() {
        return DATA_LIFETIME_TICKS.get();
    }

    public static int fieldResearchCooldownTicks() {
        return FIELD_RESEARCH_COOLDOWN_TICKS.get();
    }

    /** CLIENT side preference: entries drawn on each page of the Field Research book. */
    public static int fieldResearchEntriesPerPage() {
        return FIELD_RESEARCH_ENTRIES_PER_PAGE.get();
    }

    public static int exposeWeaknessCooldownTicks() {
        return EXPOSE_COOLDOWN_TICKS.get();
    }

    public static double exposeWeaknessRadius() {
        return EXPOSE_RADIUS.get();
    }

    public static int exposeWeaknessDurationTicks() {
        return EXPOSE_DURATION_TICKS.get();
    }

    public static int weakPointAmount() {
        return WEAK_POINT_AMOUNT.get();
    }

    public static double weakPointHitRadius() {
        return WEAK_POINT_HIT_RADIUS.get();
    }

    /** 0 = unlimited. */
    public static int weakPointMaxCriticalHits() {
        return WEAK_POINT_MAX_CRITICAL_HITS.get();
    }

    public static double prestigeLootMultiplier() {
        return PRESTIGE_LOOT_MULTIPLIER.get();
    }

    public static int nanoInjectorMaxCharges() {
        return NANO_MAX_CHARGES.get();
    }

    public static double nanoInjectorRange() {
        return NANO_RANGE.get();
    }

    public static int nanoInjectorFreezeTicks() {
        return NANO_FREEZE_TICKS.get();
    }

    public static int nanoInjectorEffectDurationTicks() {
        return NANO_EFFECT_DURATION_TICKS.get();
    }

    /** Damage multiplier of the Vulnerable effect (1.25 by default). */
    public static float vulnerableDamageMultiplier() {
        return NANO_VULNERABLE_MULTIPLIER.get().floatValue();
    }

    public static List<? extends String> nanoInjectorReloadCostEntries() {
        return NANO_RELOAD_COST.get();
    }

    public static int researchUnitTicksPerBiomass() {
        return RESEARCH_UNIT_TICKS_PER_BIOMASS.get();
    }

    // ===================================================================================================== rules snapshot

    /**
     * The rules that decide the weak point bonus, the prestige requirements and the Field Research cooldown, as one
     * immutable object. The server builds it from its config ({@link #current()}) and sends it with the Field
     * Research book data; the client keeps the last one received so the book shows what the server really uses.
     */
    public static final class ResearchRules {
        public final int killValueInData;
        public final double dataPerDamagePoint;
        public final int softCapBlockSize;
        public final double softCapDecay;
        public final float maxBonusVsPlayers;
        public final boolean percentMode;
        public final float percentPerPoint;
        public final int fieldResearchCooldownTicks;
        private final int[] prestigeKills;
        private final int[] prestigeData;

        public ResearchRules(int killValueInData, double dataPerDamagePoint, int softCapBlockSize, double softCapDecay,
                             float maxBonusVsPlayers, boolean percentMode, float percentPerPoint,
                             int fieldResearchCooldownTicks, int[] prestigeKills, int[] prestigeData) {
            this.killValueInData = killValueInData;
            this.dataPerDamagePoint = dataPerDamagePoint;
            this.softCapBlockSize = softCapBlockSize;
            this.softCapDecay = softCapDecay;
            this.maxBonusVsPlayers = maxBonusVsPlayers;
            this.percentMode = percentMode;
            this.percentPerPoint = percentPerPoint;
            this.fieldResearchCooldownTicks = fieldResearchCooldownTicks;
            this.prestigeKills = prestigeKills;
            this.prestigeData = prestigeData;
        }

        /** Builds the snapshot from the config of this side. */
        public static ResearchRules current() {
            int[] kills = new int[PRESTIGE_RARITIES];
            int[] data = new int[PRESTIGE_RARITIES];
            for (int i = 0; i < PRESTIGE_RARITIES; i++) {
                kills[i] = PRESTIGE_KILLS[i].get();
                data[i] = PRESTIGE_DATA[i].get();
            }
            return new ResearchRules(
                    DAMAGE_KILL_VALUE_IN_DATA.get(),
                    DAMAGE_DATA_PER_POINT.get(),
                    DAMAGE_SOFT_CAP_BLOCK_SIZE.get(),
                    DAMAGE_SOFT_CAP_DECAY.get(),
                    DAMAGE_MAX_BONUS_VS_PLAYERS.get().floatValue(),
                    DAMAGE_MODE.get() == DamageMode.PERCENT,
                    DAMAGE_PERCENT_PER_POINT.get().floatValue(),
                    FIELD_RESEARCH_COOLDOWN_TICKS.get(),
                    kills,
                    data
            );
        }

        /** Kills needed to prestige an entity of that rarity (1-4); effectively unreachable for any other rarity. */
        public int prestigeKills(int rarity) {
            return rarity >= 1 && rarity <= prestigeKills.length ? prestigeKills[rarity - 1] : Integer.MAX_VALUE;
        }

        /** Data needed to prestige an entity of that rarity (1-4); effectively unreachable for any other rarity. */
        public int prestigeData(int rarity) {
            return rarity >= 1 && rarity <= prestigeData.length ? prestigeData[rarity - 1] : Integer.MAX_VALUE;
        }

        public void write(FriendlyByteBuf buf) {
            buf.writeVarInt(killValueInData);
            buf.writeDouble(dataPerDamagePoint);
            buf.writeVarInt(softCapBlockSize);
            buf.writeDouble(softCapDecay);
            buf.writeFloat(maxBonusVsPlayers);
            buf.writeBoolean(percentMode);
            buf.writeFloat(percentPerPoint);
            buf.writeVarInt(fieldResearchCooldownTicks);
            for (int i = 0; i < PRESTIGE_RARITIES; i++) {
                buf.writeVarInt(prestigeKills[i]);
                buf.writeVarInt(prestigeData[i]);
            }
        }

        public static ResearchRules read(FriendlyByteBuf buf) {
            int killValueInData = buf.readVarInt();
            double dataPerDamagePoint = buf.readDouble();
            int softCapBlockSize = buf.readVarInt();
            double softCapDecay = buf.readDouble();
            float maxBonusVsPlayers = buf.readFloat();
            boolean percentMode = buf.readBoolean();
            float percentPerPoint = buf.readFloat();
            int fieldResearchCooldownTicks = buf.readVarInt();
            int[] kills = new int[PRESTIGE_RARITIES];
            int[] data = new int[PRESTIGE_RARITIES];
            for (int i = 0; i < PRESTIGE_RARITIES; i++) {
                kills[i] = buf.readVarInt();
                data[i] = buf.readVarInt();
            }
            return new ResearchRules(killValueInData, dataPerDamagePoint, softCapBlockSize, softCapDecay,
                    maxBonusVsPlayers, percentMode, percentPerPoint, fieldResearchCooldownTicks, kills, data);
        }
    }
}
