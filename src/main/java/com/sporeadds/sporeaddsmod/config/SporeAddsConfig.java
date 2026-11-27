package com.sporeadds.sporeaddsmod.config;

import com.sporeadds.sporeaddsmod.Powers.Levelstats;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModList;

import java.util.ArrayList;
import java.util.List;

public class SporeAddsConfig {

    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.IntValue KOMMANDANT_LEVELS_LOST_ON_DEATH;
    public static final ForgeConfigSpec.BooleanValue KOMMANDANT_PERMADEATH_NO_RESPAWN;

    public static final ForgeConfigSpec.BooleanValue HIDE_GHOST;
    public static final ForgeConfigSpec.BooleanValue HIDE_KOMMANDANT;
    public static final ForgeConfigSpec.BooleanValue HIDE_MEDIC;
    public static final ForgeConfigSpec.BooleanValue HIDE_SCIENTIST;
    public static final ForgeConfigSpec.BooleanValue HIDE_BERSERKER;
    public static final ForgeConfigSpec.BooleanValue SPORE_PLAYERS_CAN_PHASE_BIOMASS;
    public static final ForgeConfigSpec.BooleanValue PHASING_APPLIES_BLINDNESS;

    public static final ForgeConfigSpec.ConfigValue<String> EVOLUTION_COST_1;
    public static final ForgeConfigSpec.ConfigValue<String> EVOLUTION_COST_2;
    public static final ForgeConfigSpec.ConfigValue<String> EVOLUTION_COST_3;
    public static final ForgeConfigSpec.ConfigValue<String> EVOLUTION_COST_4;
    public static final ForgeConfigSpec.ConfigValue<String> EVOLUTION_COST_5;
    public static final ForgeConfigSpec.ConfigValue<String> EVOLUTION_COST_6;
    public static final ForgeConfigSpec.ConfigValue<String> EVOLUTION_COST_7;
    public static final ForgeConfigSpec.ConfigValue<String> EVOLUTION_COST_8;
    public static final ForgeConfigSpec.ConfigValue<String> EVOLUTION_COST_9;

    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> VERWA_HORDE_POOL;

    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> VERWA_SUMMONING_MENU;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> ORGANOID_SUMMONING_MENU;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> BOMB_SUMMONING_MENU;

    public static final ForgeConfigSpec.IntValue ORGANOID_MENU_UNLOCK_LEVEL;
    public static final ForgeConfigSpec.IntValue BOMB_MENU_UNLOCK_LEVEL;

    public static final ForgeConfigSpec.BooleanValue UNIFIED_MOUNDS_LIST;

    public static final ForgeConfigSpec.IntValue TERMINA_DURATION_MIN;
    public static final ForgeConfigSpec.IntValue TERMINA_DURATION_MAX;
    public static final ForgeConfigSpec.IntValue NUKE_LEVEL_PENALTY;
    public static final ForgeConfigSpec.IntValue NUKE_EXPOSED_LEVEL;
    public static final ForgeConfigSpec.IntValue NUKE_MYCELIUM_LEVEL;
    public static final ForgeConfigSpec.IntValue NUKE_TERMINA_LEVEL;

    public static final ForgeConfigSpec.BooleanValue VACCINE_CURES_TERMINA;
    public static final ForgeConfigSpec.BooleanValue BIOMASS_CORE_REQUIRES_ORIGIN;

    public static final ForgeConfigSpec.BooleanValue CORE_MOUND_LOCATOR_REQUIRES_ORIGIN;
    public static final ForgeConfigSpec.BooleanValue BUCKET_OF_REMAINS_REQUIRES_ORIGIN;
    public static final ForgeConfigSpec.BooleanValue IMPROVISED_LOCATOR_REQUIRES_ORIGIN;
    public static final ForgeConfigSpec.BooleanValue INJECTOR_REQUIRES_ORIGIN;
    public static final ForgeConfigSpec.BooleanValue NANO_INJECTOR_REQUIRES_ORIGIN;
    public static final ForgeConfigSpec.BooleanValue THROWABLE_BANDAGES_REQUIRES_ORIGIN;
    public static final ForgeConfigSpec.BooleanValue PROTO_LOCATOR_REQUIRES_ORIGIN;
    public static final ForgeConfigSpec.BooleanValue SURGICAL_IMPLANTATOR_REQUIRES_ORIGIN;
    public static final ForgeConfigSpec.BooleanValue VACCINE_REQUIRES_ORIGIN;
    public static final ForgeConfigSpec.BooleanValue SYRINGE_REQUIRES_ORIGIN;
    public static final ForgeConfigSpec.BooleanValue SCALPEL_REQUIRES_ORIGIN;
    public static final ForgeConfigSpec.BooleanValue REINFORCED_COMBAT_CHAINS_REQUIRES_ORIGIN;
    public static final ForgeConfigSpec.BooleanValue MEDIC_GAS_MASK_CURSE_OF_BINDING;

    public static final ForgeConfigSpec.ConfigValue<Boolean> SPORE_FACTION_ENABLED;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> EXTRA_NEUTRAL_MOBS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> EXTRA_HOSTILE_MOBS;

    public static final ForgeConfigSpec.BooleanValue KOMMANDANT_DIET_RESTRICTION;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> KOMMANDANT_EDIBLE_ITEMS;

    public static final ForgeConfigSpec.BooleanValue ARMOR_BOSSBAR_ENABLED;
    public static final ForgeConfigSpec.DoubleValue ARMOR_BOSSBAR_RANGE;

    public static final ForgeConfigSpec.ConfigValue<String> IMPLANT_DEATH_BEHAVIOR;

    public static final ForgeConfigSpec.BooleanValue FROZEN_TUMOR_NERF;

    public static final ForgeConfigSpec.BooleanValue KOMMANDANT_BLOMFUNG_EFFECTS;
    public static final ForgeConfigSpec.BooleanValue KOMMANDANT_PELLET_EFFECTS;

    public static final ForgeConfigSpec.BooleanValue DISSOLUTION_DAMAGE_SPORE_TEAM_ALWAYS;

    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> KOMMANDANT_LEVEL_STATS_DEFAULT;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> KOMMANDANT_LEVEL_STATS_CAUSTIC;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> KOMMANDANT_LEVEL_STATS_ABYSSAL;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> KOMMANDANT_LEVEL_STATS_GLUTTONOUS;

    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> KOMMANDANT_ESP_ENTITIES;

    public static final ForgeConfigSpec.BooleanValue ORGANITE_MARKER_FOR_BLACKLIST;

    public static final ForgeConfigSpec.DoubleValue KOMMANDANT_MUTAGENIC_COMPOUND_DROP_CHANCE;

    public static final ForgeConfigSpec.BooleanValue ENABLE_MUTAGENIC_COMPOUND_RECIPES;

    public static final ForgeConfigSpec.IntValue KOMMANDANT_VARIANT_CHANGE_LEVEL_PENALTY;

    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> TRAINING_BOOK_ENABLED_CLASSES;
    public static final ForgeConfigSpec.IntValue TRAINING_BOOK_MAX_USES;
    public static final ForgeConfigSpec.BooleanValue TRAINING_BOOK_READONLY_WITH_ORIGINS;
    public static final ForgeConfigSpec.BooleanValue TRAINING_BOOK_GIVE_ON_FIRST_JOIN;
    public static final ForgeConfigSpec.BooleanValue TRAINING_BOOK_KOMMANDANT_LOCKED;

    public static final ForgeConfigSpec.IntValue TRAINING_BOOK_SLOTS_KOMMANDANT;
    public static final ForgeConfigSpec.IntValue TRAINING_BOOK_SLOTS_GHOST;
    public static final ForgeConfigSpec.IntValue TRAINING_BOOK_SLOTS_MEDIC;
    public static final ForgeConfigSpec.IntValue TRAINING_BOOK_SLOTS_SCIENTIST;
    public static final ForgeConfigSpec.IntValue TRAINING_BOOK_SLOTS_BERSERKER;

    public static final ForgeConfigSpec.DoubleValue CALL_OF_THE_HIVE_CHANCE;
    public static final ForgeConfigSpec.IntValue HIVE_KOMMANDANT_PLAYER_LEVEL;
    public static final ForgeConfigSpec.IntValue HIVE_KOMMANDANT_KNOWLEDGE_LEVEL;
    public static final ForgeConfigSpec.BooleanValue HIVE_FORCE_SURRENDER;
    public static final ForgeConfigSpec.BooleanValue CALL_OF_THE_HIVE_PERSISTS_WITHOUT_PROTO;
    public static final ForgeConfigSpec.BooleanValue CALL_OF_THE_HIVE_APPLIES_WITHOUT_PROTO;

    public static final ForgeConfigSpec.BooleanValue PROTO_FETCH_HUMANS;
    public static final ForgeConfigSpec.IntValue PROTO_FETCH_HUMANS_COOLDOWN;
    public static final ForgeConfigSpec.IntValue PROTO_ENGAGEMENT_RADIUS;

    public static final ForgeConfigSpec.BooleanValue PUNISHMENT_EVENT_ENABLED;
    public static final ForgeConfigSpec.IntValue PUNISHMENT_HIT_COOLDOWN;
    public static final ForgeConfigSpec.IntValue PUNISHMENT_SHIELD_CHECK_RADIUS;
    public static final ForgeConfigSpec.IntValue PUNISHMENT_DECAY_INTERVAL_TICKS;
    public static final ForgeConfigSpec.IntValue PUNISHMENT_RETURN_WINDOW;
    public static final ForgeConfigSpec.IntValue PUNISHMENT_MAX_CALLED_KOMMANDANTS;
    public static final ForgeConfigSpec.IntValue PUNISHMENT_MAX_CALLED_KOMMANDANTS_MULTI;
    public static final ForgeConfigSpec.IntValue PUNISHMENT_SECOND_STRIKE_FLOOR_HEARTS;
    public static final ForgeConfigSpec.BooleanValue PUNISHMENT_THIRD_STRIKE_LETHAL;
    public static final ForgeConfigSpec.BooleanValue PUNISHMENT_THIRD_STRIKE_NO_RESPAWN;

    public static final ForgeConfigSpec.BooleanValue DEVOTION_EVENT_ENABLED;
    public static final ForgeConfigSpec.DoubleValue DEVOTION_REWARD_RADIUS;
    public static final ForgeConfigSpec.IntValue DEVOTION_DURATION_TICKS;
    public static final ForgeConfigSpec.IntValue DEVOTION_ARMOR_THRESHOLD;


    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("kommandant");

        KOMMANDANT_LEVELS_LOST_ON_DEATH = builder
                .comment("0-9 = kommandant levels lost on death")
                .defineInRange("levels_lost_on_death", 2, 0, 9);

        KOMMANDANT_PERMADEATH_NO_RESPAWN = builder
                .comment("permadeath when no available respawn points left for kommandant")
                .define("permadeath_when_no_available_respawn_points_left", true);

        KOMMANDANT_BLOMFUNG_EFFECTS = builder
                .comment("If true, Kommandant blomfung block effects will render.")
                .define("kommandant_blomfung_effects", true);

        KOMMANDANT_PELLET_EFFECTS = builder
                .comment("If true, Kommandant pellet sprite effects will render.")
                .define("kommandant_pellet_effects", true);

        KOMMANDANT_MUTAGENIC_COMPOUND_DROP_CHANCE = builder
                .comment("Chance for a Kommandant player to make killed entities drop a random Mutagenic Compound. 0.01 = 1%")
                .defineInRange("mutagenic_compound_drop_chance", 0.01D, 0.0D, 1.0D);

        ENABLE_MUTAGENIC_COMPOUND_RECIPES = builder
                .comment("Enable crafting recipes for Mutagenic Compound variants")
                .define("enable_mutagenic_compound_recipes", true);

        KOMMANDANT_VARIANT_CHANGE_LEVEL_PENALTY = builder
                .comment("Levels lost after finishing a cocoon when changing Kommandant variant with a Mutagenic Compound. Applied after normal level gain so knowledge levels register that level.")
                .defineInRange("variant_change_level_penalty", 0, 0, 9);

        builder.pop();

        builder.push("kommandant_level_stats");
        builder.comment(
                "Configurable stat tables for Kommandant subclasses.",
                "Format per entry: hearts;armor;scale;speedModifier;damage;hungerMultiplier;scaleType",
                "Example: 10;0;1.00;1.0;1.00;0.0;BASE",
                "The array index is the level: first line = level 0, second = level 1, ..."
        );

        KOMMANDANT_LEVEL_STATS_DEFAULT = builder
                .defineListAllowEmpty(List.of("default_table"), () -> List.of(
                        "10;0;1.00;1.0;1.00;0.0;BASE",
                        "11;2;1.06;1.01;1.22;0.05;BASE",
                        "12;4;1.11;1.01;1.44;0.07;BASE",
                        "13;6;1.17;1.02;1.67;0.10;BASE",
                        "14;8;1.22;1.03;1.89;0.13;BASE",
                        "16;10;1.28;1.03;2.11;0.16;BASE",
                        "17;12;1.33;1.04;2.33;0.20;BASE",
                        "18;14;1.39;1.05;2.56;0.25;BASE",
                        "19;16;1.44;1.06;2.78;0.30;BASE",
                        "20;20;1.50;1.07;3.00;0.35;BASE"
                ), obj -> obj instanceof String);

        builder.comment(
                "(changing the scale stats over/under 1.00 and 1.50 will cause visual bugs while using the tentacle skill)"
        );

        KOMMANDANT_LEVEL_STATS_ABYSSAL = builder
                .defineListAllowEmpty(List.of("abyssal_table"), () -> List.of(
                        "11;0;1.00;0.9;1.00;0.0;BASE",
                        "12;2;1.06;0.89;1.14;0.06;BASE",
                        "13;4;1.11;0.88;1.29;0.15;BASE",
                        "14;6;1.17;0.85;1.43;0.20;BASE",
                        "15;8;1.22;0.83;1.58;0.25;BASE",
                        "16;10;1.28;0.80;1.72;0.30;BASE",
                        "17;12;1.33;0.77;1.87;0.35;BASE",
                        "19;14;1.39;0.75;2.01;0.40;BASE",
                        "20;16;1.44;0.74;2.16;0.45;BASE",
                        "22;20;1.50;0.70;2.30;0.50;BASE"
                ), obj -> obj instanceof String);

        KOMMANDANT_LEVEL_STATS_CAUSTIC = builder
                .defineListAllowEmpty(List.of("caustic_table"), () -> List.of(
                        "10;0;1.00;1.0;1.00;0.0;BASE",
                        "11;1;1.05;1.01;1.14;0.06;BASE",
                        "11;3;1.10;1.01;1.29;0.09;BASE",
                        "12;4;1.15;1.02;1.43;0.12;BASE",
                        "13;5;1.20;1.03;1.58;0.15;BASE",
                        "13;6;1.25;1.03;1.72;0.20;BASE",
                        "14;8;1.30;1.04;1.87;0.25;BASE",
                        "15;9;1.35;1.05;2.01;0.30;BASE",
                        "15;10;1.40;1.06;2.16;0.35;BASE",
                        "16;12;1.45;1.07;2.30;0.40;BASE"
                ), obj -> obj instanceof String);

        KOMMANDANT_LEVEL_STATS_GLUTTONOUS = builder
                .defineListAllowEmpty(List.of("GLUTTONOUS_table"), () -> List.of(
                        "10;0;1.00;1.0;1.00;0.0;BASE",
                        "11;3;1.11;0.98;1.26;0.09;BASE",
                        "13;5;1.21;0.97;1.64;0.15;BASE",
                        "15;7;1.29;0.95;1.87;0.22;BASE",
                        "18;10;1.36;0.92;2.02;0.34;BASE",
                        "21;12;1.45;0.87;2.25;0.49;BASE",
                        "25;15;1.54;0.83;2.56;0.67;BASE",
                        "30;19;1.67;0.78;2.72;0.79;BASE",
                        "35;24;1.79;0.76;2.90;0.92;BASE",
                        "40;28;2.00;0.73;3.20;1.12;BASE"
                ), obj -> obj instanceof String);

        builder.pop();

        builder.push("origins_settings");
        builder.comment("If true, the origin will be hidden from the selection screen but can still be given via commands.");

        HIDE_GHOST = builder.define("hide_ghost", false);
        HIDE_KOMMANDANT = builder.define("hide_kommandant", false);
        HIDE_MEDIC = builder.define("hide_medic", false);
        HIDE_SCIENTIST = builder.define("hide_scientist", false);
        HIDE_BERSERKER = builder.define("hide_berserker", false);

        builder.pop();

        builder.push("evolution_costs");
        builder.comment(
                "Items and SporePhase (biomass) cost required to evolve to the next level.",
                "Pattern format: (item_id(amount:X)) cost:Y",
                "Use '/' to indicate substitute/alternative items (OR condition).",
                "Use ',' to indicate multiple required items (AND condition)."
        );

        EVOLUTION_COST_1 = builder.define("level_1", "() cost:20");
        EVOLUTION_COST_2 = builder.define("level_2", "(sporeadd:mutated_undead(amount:3)) cost:20");
        EVOLUTION_COST_3 = builder.define("level_3", "(sporeadd:individualist_mind(amount:1)) cost:25");
        EVOLUTION_COST_4 = builder.define("level_4", "(sporeadd:chemist_mind(amount:1))/(sporeadd:mycelial_network_cultive(amount:1)) cost:25");
        EVOLUTION_COST_5 = builder.define("level_5", "(sporeadd:aggressive_mixture(amount:1)) cost:35");
        EVOLUTION_COST_6 = builder.define("level_6", "(sporeadd:locator_organ(amount:1)) cost:35");
        EVOLUTION_COST_7 = builder.define("level_7", "(sporeadd:hive_eye(amount:1)) cost:40");
        EVOLUTION_COST_8 = builder.define("level_8", "(sporeadd:reinforced_organoid_tissue(amount:10)),(sporeadd:dangerous_mind(amount:1)) cost:40");
        EVOLUTION_COST_9 = builder.define("level_9", "(sporeadd:airborne_refinated_mycelium(amount:10)) cost:50");

        builder.pop();

        builder.push("verwa_horde_pool");
        builder.comment(
                "Configurable pool for Horde summoning (Poder 8).",
                "Format: \"modid:entity_id;base_cost\".",
                "Uses the same cost philosophy as the Verva GUI, but as a dedicated horde pool.",
                "Duplicate entity ids should be avoided."
        );

        VERWA_HORDE_POOL = builder.defineListAllowEmpty(List.of("horde_mobs_and_costs"), () -> List.of(
                "spore:bairn;1",
                "spore:inf_human;2",
                "spore:inf_husk;2",
                "spore:inf_player;2",
                "spore:inf_villager;2",
                "spore:inf_wanderer;2",
                "spore:inf_drowned;2",
                "spore:inf_hazmat;2",
                "spore:saugling;2",
                "spore:scamper;3",
                "spore:inf_diseased_villager;3",
                "spore:scavenger;3",
                "spore:bloater;3",
                "spore:naiad;3",
                "spore:griefer;3",
                "spore:chemist;4",
                "spore:jagd;4",
                "spore:knight;4",
                "spore:busser;4",
                "spore:gargoyle;4",
                "spore:plagued;5",
                "spore:inf_witch;5",
                "spore:lacerator;5",
                "spore:slasher;5",
                "spore:gorgon;5",
                "spore:spitter;5",
                "spore:thorn;5",
                "spore:nuclea;5",
                "spore:protector;5",
                "spore:gastgaber;5",
                "spore:conductor;5",
                "spore:braiomil;6",
                "spore:inebriater;6",
                "spore:brute;6",
                "spore:stalker;6",
                "spore:howler;6",
                "spore:inf_evoker;7",
                "spore:leaper;8",
                "spore:inquisitor;8",
                "spore:inf_vindicator;9",
                "spore:biobloob;9",
                "spore:brot;9",
                "spore:wendigo;10",
                "spore:inf_contruct;11",
                "spore:hevoker;12",
                "spore:grober;13",
                "spore:ogre;13",
                "spore:mephitic;13",
                "spore:volatile;13",
                "spore:hvindicator;13",
                "spore:reaper;15",
                "spore:specter;15",
                "spore:vanguard;20"
        ), obj -> obj instanceof String);

        builder.pop();

        builder.push("verwa_summoning_menu");
        builder.comment("Settings for the Verwa summoning GUI.");

        ORGANOID_MENU_UNLOCK_LEVEL = builder
                .comment("Spore Level required to access the Organoids tab (Default: 5).")
                .defineInRange("organoid_menu_unlock_level", 5, 0, 9);

        BOMB_MENU_UNLOCK_LEVEL = builder
                .comment("Spore Level required to access the Bombs tab (Default: 7).")
                .defineInRange("bomb_menu_unlock_level", 7, 0, 9);

        VERWA_SUMMONING_MENU = builder
                .comment(
                        "List of all entities available in the Verwa summoning GUI menu.",
                        "Format: \"modid:entity_id;base_cost;var0_cost,var1_cost,var2_cost...\".",
                        "Example: \"spore:inf_player;2;2,4,6\".",
                        "If variant costs are omitted, all variants inherit the base cost."
                )
                .defineListAllowEmpty(List.of("gui_mobs_and_costs"), () -> List.of(
                        "spore:bairn;3",
                        "spore:hevoker_arm;3",
                        "spore:inf_human;3",
                        "spore:inf_husk;3",
                        "spore:inf_player;3;3,3,3",
                        "spore:inf_villager;3",
                        "spore:inf_pillager;3;3,19",
                        "spore:inf_wanderer;3",
                        "spore:inf_drowned;3",
                        "spore:inf_hazmat;3",
                        "spore:saugling;3",
                        "spore:inf_diseased_villager;3",
                        "spore:scamper;4",
                        "spore:plagued;4",
                        "spore:chemist;4",
                        "spore:jagd;4",
                        "spore:scavenger;4",
                        "spore:bloater;4",
                        "spore:naiad;4;4,5",
                        "spore:inf_witch;5",
                        "spore:lacerator;6",
                        "spore:knight;6",
                        "spore:griefer;6",
                        "spore:braiomil;6",
                        "spore:busser;6",
                        "spore:leaper;6",
                        "spore:slasher;6",
                        "spore:inebriater;6",
                        "spore:brute;6",
                        "spore:stalker;6",
                        "spore:gargoyle;6",
                        "spore:inf_vindicator;7",
                        "spore:gorgon;8",
                        "spore:spitter;8",
                        "spore:howler;8",
                        "spore:thorn;8",
                        "spore:nuclea;8",
                        "spore:protector;8",
                        "spore:gastgaber;8",
                        "spore:conductor;8",
                        "spore:inf_evoker;9",
                        "spore:inquisitor;10",
                        "spore:wendigo;12",
                        "spore:biobloob;14",
                        "spore:brot;14",
                        "spore:inf_contruct;15",
                        "spore:grober;15",
                        "spore:ogre;15",
                        "spore:mephitic;15",
                        "spore:volatile;15",
                        "spore:hevoker;18",
                        "spore:hvindicator;20",
                        "spore:reaper;22",
                        "spore:specter;23",
                        "spore:vanguard;32"
                ), obj -> obj instanceof String);
        builder.pop();

        builder.push("mound_settings");
        UNIFIED_MOUNDS_LIST = builder
                .comment("If true, mounds from all dimensions are grouped into a single list (1-10) for Verva Request.")
                .define("unified_mounds_list", true);
        builder.pop();

        builder.push("organoid_summoning_menu");
        ORGANOID_SUMMONING_MENU = builder
                .comment(
                        "List of organoids available in the Verwa summoning GUI menu.",
                        "Format: \"modid:entity_id;base_cost\"."
                )
                .defineListAllowEmpty(List.of("gui_organoids_and_costs"), () -> List.of(
                        "spore:umarmed;8",
                        "spore:usurper;17",
                        "spore:braurei;15",
                        "spore:delusioner;14"
                ), obj -> obj instanceof String);
        builder.pop();

        builder.push("bomb_summoning_menu");
        BOMB_SUMMONING_MENU = builder
                .comment(
                        "List of bombs available in the Verwa summoning GUI menu.",
                        "Format: \"modid:entity_id;display_name;base_cost;bomb_type;damage;radius;carrier;scale\"."
                )
                .defineListAllowEmpty(List.of("gui_bombs_and_data"), () -> List.of(
                        "spore:flesh_bomb;Basic Bomb;5;0;10.0;5;false;1.0",
                        "spore:flesh_bomb;Flame Bomb;7;1;10.0;5;false;1.0",
                        "spore:flesh_bomb;Bile Bomb;7;2;10.0;5;false;1.0",
                        "spore:flesh_bomb;Acid Bomb;6;3;10.0;7;false;1.0",
                        "spore:flesh_bomb;Heavy Basic;7;0;15.0;6;false;2.0",
                        "spore:flesh_bomb;Heavy Flame;9;1;15.0;6;false;2.0",
                        "spore:flesh_bomb;Heavy Bile;9;2;15.0;6;false;2.0",
                        "spore:flesh_bomb;Heavy Acid;8;3;15.0;7;false;2.0",
                        "spore:flesh_bomb;Carrier Basic;9;0;12.0;5;true;1.25",
                        "spore:flesh_bomb;Carrier Flame;12;1;12.0;5;true;1.25",
                        "spore:flesh_bomb;Carrier Bile;12;2;12.0;5;true;1.25",
                        "spore:flesh_bomb;Carrier Acid;10;3;12.0;6;true;1.25",
                        "spore:flesh_bomb;V2Carr Basic;15;0;20.0;7;true;2.25",
                        "spore:flesh_bomb;V2Carr Flame;17;1;20.0;7;true;2.25",
                        "spore:flesh_bomb;V2Carr Bile;17;2;20.0;7;true;2.25",
                        "spore:flesh_bomb;V2Carr Acid;16;3;20.0;8;true;2.25"
                ), obj -> obj instanceof String);
        builder.pop();

        builder.push("nuclear_powers");
        builder.comment("Settings for the Tumoroid Nuke (Power 13). Time values are in ticks.");

        TERMINA_DURATION_MIN = builder
                .comment("Minimum duration of the Termina effect applied by the Nuke.")
                .defineInRange("termina_duration_min", 30000, 1, 1000000);

        TERMINA_DURATION_MAX = builder
                .comment("Maximum duration of the Termina effect applied by the Nuke.")
                .defineInRange("termina_duration_max", 42000, 1, 1000000);

        NUKE_LEVEL_PENALTY = builder
                .comment("Number of Kommandant levels lost after detonating the Nuke.")
                .defineInRange("nuke_level_penalty", 2, 0, 10);

        NUKE_EXPOSED_LEVEL = builder
                .comment("Level of the Exposed effect applied by the Nuke cloud.")
                .defineInRange("nuke_exposed_level", 4, 0, 10);

        NUKE_MYCELIUM_LEVEL = builder
                .comment("Level of the Mycelium effect applied by the Nuke cloud and blast.")
                .defineInRange("nuke_mycelium_level", 2, 0, 10);

        NUKE_TERMINA_LEVEL = builder
                .comment("Level of the Termina effect applied by the Nuke blast.")
                .defineInRange("nuke_termina_level", 0, 0, 10);

        builder.pop();

        builder.push("items_settings");

        VACCINE_CURES_TERMINA = builder
                .comment("If true, the Vaccine item can cure the Termina effect from players.")
                .define("vaccine_cures_termina", true);

        builder.comment("Settings to restrict custom items. If true, only players with the corresponding Origin can use the item.");

        BIOMASS_CORE_REQUIRES_ORIGIN = builder
                .comment("If true, ONLY the Kommandant origin can use the Biomass Core.")
                .define("biomass_core_requires_origin", true);

        CORE_MOUND_LOCATOR_REQUIRES_ORIGIN = builder.define("core_mound_locator_requires_origin", true);
        BUCKET_OF_REMAINS_REQUIRES_ORIGIN = builder.define("bucket_of_remains_requires_origin", true);
        IMPROVISED_LOCATOR_REQUIRES_ORIGIN = builder.define("improvised_locator_requires_origin", true);
        INJECTOR_REQUIRES_ORIGIN = builder.define("injector_requires_origin", true);
        NANO_INJECTOR_REQUIRES_ORIGIN = builder.define("nano_injector_requires_origin", true);
        THROWABLE_BANDAGES_REQUIRES_ORIGIN = builder.define("throwable_bandages_requires_origin", true);
        PROTO_LOCATOR_REQUIRES_ORIGIN = builder.define("proto_locator_requires_origin", true);
        SURGICAL_IMPLANTATOR_REQUIRES_ORIGIN = builder.define("surgical_implantator_requires_origin", true);
        VACCINE_REQUIRES_ORIGIN = builder.define("vaccine_requires_origin", true);
        SYRINGE_REQUIRES_ORIGIN = builder.define("syringe_requires_origin", true);
        SCALPEL_REQUIRES_ORIGIN = builder.define("scalpel_requires_origin", true);
        REINFORCED_COMBAT_CHAINS_REQUIRES_ORIGIN = builder
                .comment("If true, ONLY the Berserker class can use the Reinforced Combat Chains.")
                .define("reinforced_combat_chains_requires_origin", true);

        MEDIC_GAS_MASK_CURSE_OF_BINDING = builder
                .comment("If true, the Medic's gas mask has Curse of Binding (cannot be unequipped). Changing this",
                        "at runtime (config reload, or a player logging in) immediately adds/removes the enchant on",
                        "every gas mask a Medic already has equipped.")
                .define("medic_gas_mask_curse_of_binding", true);

        builder.pop();

        builder.push("mob_aggro_faction");

        SPORE_FACTION_ENABLED = builder
                .comment("If true, hostile mobs become neutral and certain neutral mobs become hostile towards Spore Team players.")
                .define("spore_faction_enabled", true);

        EXTRA_NEUTRAL_MOBS = builder
                .comment("List of mob IDs that will be neutral towards Spore Team players.")
                .defineListAllowEmpty(List.of("extra_neutral_mobs"), ArrayList::new, obj -> obj instanceof String);

        EXTRA_HOSTILE_MOBS = builder
                .comment("List of mob IDs that will be hostile towards Spore Team players.")
                .defineListAllowEmpty(List.of("extra_hostile_mobs"), ArrayList::new, obj -> obj instanceof String);

        builder.pop();

        builder.push("dissolution_settings");

        DISSOLUTION_DAMAGE_SPORE_TEAM_ALWAYS = builder
                .comment("If true, entities on the team 'spore' always take damage from Dissolution even if they still have armor.",
                        "If false, entities on team 'spore' never take the periodic acid damage from Dissolution.")
                .define("dissolution_damage_spore_team_always", true);

        builder.pop();

        builder.push("call_of_the_hive");

        CALL_OF_THE_HIVE_CHANCE = builder
                .comment("Chance (0.0 - 1.0) that a player hit by a spore-team entity, while within 300 blocks",
                        "of a spore:proto, gains the 'Call of the Hive' effect, when a player dies with this effect they will face a choice to become infected or die.")
                .defineInRange("call_of_the_hive_chance", 0.05D, 0.0D, 1.0D);

        HIVE_KOMMANDANT_PLAYER_LEVEL = builder
                .comment("When a player is turned into a Kommandant by surrendering to the hive, their player level is SET to this value.",
                        "Default 0 (reset). Range 0-9.")
                .defineInRange("hive_kommandant_player_level", 0, 0, 9);

        HIVE_KOMMANDANT_KNOWLEDGE_LEVEL = builder
                .comment("When a player is turned into a Kommandant by surrendering to the hive, their knowledge level is SET to this value.",
                        "Default 0 (reset). Range 0-9.")
                .defineInRange("hive_kommandant_knowledge_level", 0, 0, 9);

        HIVE_FORCE_SURRENDER = builder
                .comment("If true, the 'Call of the Hive' downed screen only offers the red 'give up' (surrender): the player",
                        "cannot choose to perish. A timeout or logging out no longer means death - the verwa + infection",
                        "sequence runs anyway (resumed on next login if they left).")
                .define("force_surrender", false);

        CALL_OF_THE_HIVE_PERSISTS_WITHOUT_PROTO = builder
                .comment("If true, the 'Call of the Hive' effect is NOT removed when the player leaves the range of a",
                        "spore:proto - it lasts anywhere, and dying with it triggers the downed choice anywhere.")
                .define("call_of_the_hive_persists_without_proto", false);

        CALL_OF_THE_HIVE_APPLIES_WITHOUT_PROTO = builder
                .comment("If true, spore entities can apply the 'Call of the Hive' effect even when there is no",
                        "spore:proto within range of the victim.")
                .define("call_of_the_hive_applies_without_proto", false);

        builder.pop();

        builder.push("proto_detection");

        PROTO_FETCH_HUMANS = builder
                .comment("When a spore:proto detects a (non-Kommandant) intruder, besides warning the called Kommandants",
                        "in chat, send each Kommandant that still has to travel a transport Verwa (unique to that",
                        "Kommandant) that carries them to the proto, regardless of their Spore level. The Verwa is",
                        "never for the detected intruder.")
                .define("proto_fetch_humans", true);

        PROTO_FETCH_HUMANS_COOLDOWN = builder
                .comment("Minimum ticks between two calls triggered by the same proto (20 ticks = 1 second).")
                .defineInRange("proto_fetch_humans_cooldown", 1200, 20, Integer.MAX_VALUE);

        PROTO_ENGAGEMENT_RADIUS = builder
                .comment("The proto's range, in blocks (same one it uses to detect players). Used to watch for extra",
                        "intruders during the warning window (another one showing up -> 'brace for open combat'), to",
                        "decide whether a called Kommandant is 'already in position' (no return demand), and as the",
                        "distance a warned Kommandant must close to satisfy the return demand.")
                .defineInRange("proto_engagement_radius", 128, 8, 512);

        builder.pop();

        builder.push("punishment_event");

        PUNISHMENT_EVENT_ENABLED = builder
                .comment("Master toggle for the Proto 'punishment' escalation against Kommandant players.",
                        "Each infraction (hitting a proto/reconstructor, or ignoring a proto's return demand for the",
                        "configured window) adds 1 punishment point while at least one proto is alive in the dimension.",
                        "1 point: punishment I for 10s + warning. 2 points: punishment II for 20s + ordinary healable",
                        "damage (through Poder12) that only brings the player down to the floor hearts, cannot kill.",
                        "3 points: punishment III for 10s that drains the player to death, ignoring totems.")
                .define("punishment_event_enabled", true);

        PUNISHMENT_HIT_COOLDOWN = builder
                .comment("Minimum ticks between two punishment points from a Kommandant hitting a proto/reconstructor.")
                .defineInRange("punishment_hit_cooldown", 100, 1, Integer.MAX_VALUE);

        PUNISHMENT_SHIELD_CHECK_RADIUS = builder
                .comment("Human-shield protection: if a non-Kommandant player is within this many blocks of the",
                        "proto/reconstructor a Kommandant just hit, no punishment point is given for that hit - it is",
                        "assumed they were aiming at that human (who could be using the hive entity as a shield) and",
                        "hit the hive entity by mistake.")
                .defineInRange("punishment_shield_check_radius", 10, 0, 64);

        PUNISHMENT_DECAY_INTERVAL_TICKS = builder
                .comment("A Kommandant's punishment level drops by 1 for every this many ticks (20 ticks = 1 second;",
                        "default 20000 = 1000 seconds) it stays above 0, while they are online. Only counts while a",
                        "proto's 3rd-strike lethal drain is not in progress for them. 0 = decay disabled.")
                .defineInRange("punishment_decay_interval_ticks", 20000, 0, Integer.MAX_VALUE);

        PUNISHMENT_RETURN_WINDOW = builder
                .comment("Ticks a warned Kommandant has to reach the proto after it calls them before it counts as an",
                        "infraction (20 ticks = 1 second).")
                .defineInRange("punishment_return_window", 1200, 20, Integer.MAX_VALUE);

        PUNISHMENT_MAX_CALLED_KOMMANDANTS = builder
                .comment("How many Kommandants a single proto can summon at once (the nearest ones). They are the only",
                        "players warned and put on the return clock, so this also caps how many are exposed to a",
                        "punishment point for ignoring that call. 0 = no limit (every online Kommandant).")
                .defineInRange("punishment_max_called_kommandants", 0, 0, 64);

        PUNISHMENT_MAX_CALLED_KOMMANDANTS_MULTI = builder
                .comment("Total cap once the alarm escalates to open combat (another intruder shows up while a proto's",
                        "warning window is still running). Usually higher than the single-intruder cap so the proto",
                        "rallies more Kommandants. 0 = no limit.")
                .defineInRange("punishment_max_called_kommandants_multi", 0, 0, 64);

        PUNISHMENT_SECOND_STRIKE_FLOOR_HEARTS = builder
                .comment("The 2nd punishment point deals ordinary, healable damage (through Poder12) that brings the",
                        "Kommandant down to this many hearts. It cannot take them below it and cannot kill.")
                .defineInRange("punishment_second_strike_floor_hearts", 5, 1, 100);

        PUNISHMENT_THIRD_STRIKE_LETHAL = builder
                .comment("If true, the 3rd punishment point drains the Kommandant to death (ignoring totems and anything",
                        "else that would prevent the death). If false, the 3rd point only applies punishment III + message.")
                .define("punishment_third_strike_lethal", true);

        PUNISHMENT_THIRD_STRIKE_NO_RESPAWN = builder
                .comment("If true, a Kommandant killed by the 3rd punishment point cannot respawn: on respawn they are",
                        "forced into spectator mode, hardcore-style.")
                .define("punishment_third_strike_no_respawn", false);

        builder.pop();

        builder.push("devotion_event");

        DEVOTION_EVENT_ENABLED = builder
                .comment("Master toggle for the 'devotion' reward: when a Kommandant kills a non-Kommandant player, or",
                        "is credited with their death (Minecraft's own last-hurt-by tracking, so it also covers",
                        "indirect deaths), while at least one proto is alive in that dimension, every Kommandant within",
                        "the reward radius of the victim at the moment of death gets the devotion effect, a punishment",
                        "point back, and a reward message from the proto.")
                .define("devotion_event_enabled", true);

        DEVOTION_REWARD_RADIUS = builder
                .comment("Kommandants within this many blocks of the victim at the moment of death receive devotion",
                        "(not just whoever is credited with the kill - everyone nearby is assumed to have helped).")
                .defineInRange("devotion_reward_radius", 30.0D, 1.0D, 512.0D);

        DEVOTION_DURATION_TICKS = builder
                .comment("Duration of each devotion application, in ticks (20 ticks = 1 second; default 12000 = 600s).",
                        "Re-applying always resets the duration back to this, even at max amplifier.")
                .defineInRange("devotion_duration_ticks", 12000, 20, Integer.MAX_VALUE);

        DEVOTION_ARMOR_THRESHOLD = builder
                .comment("If the victim's armor value at the moment of death is above this, the reward message uses",
                        "the 'tough armor' line pool instead of the normal one.")
                .defineInRange("devotion_armor_threshold", 25, 0, 100);

        builder.pop();

        builder.push("biomass_phasing");

        SPORE_PLAYERS_CAN_PHASE_BIOMASS = builder
                .comment("If true, players on team 'spore' can phase through biomass blocks.")
                .define("spore_players_can_phase_biomass", true);

        PHASING_APPLIES_BLINDNESS = builder
                .comment("If true, players receive blindness while phasing through biomass.")
                .define("phasing_applies_blindness", true);

        builder.pop();

        builder.push("diet_restrictions");

        KOMMANDANT_DIET_RESTRICTION = builder
                .comment("If true, the Kommandant can ONLY eat items specified in the list.")
                .define("kommandant_diet_restriction", true);

        KOMMANDANT_EDIBLE_ITEMS = builder
                .comment("List of item IDs that the Kommandant is allowed to eat.")
                .defineListAllowEmpty(List.of("kommandant_edible_items"), () -> List.of(
                        "spore:biomass"
                ), obj -> obj instanceof String);

        builder.pop();

        builder.push("bossbar_settings");

        ARMOR_BOSSBAR_ENABLED = builder
                .comment("If true, players with Spore Armor HP will display a boss bar to nearby players.")
                .define("armor_bossbar_enabled", true);

        ARMOR_BOSSBAR_RANGE = builder
                .comment("The range in blocks at which the armor boss bar is visible to other players.")
                .defineInRange("armor_bossbar_range", 100.0, 10.0, 500.0);

        builder.pop();

        builder.push("implants_settings");

        IMPLANT_DEATH_BEHAVIOR = builder
                .comment(
                        "Controls what happens to implants on player death.",
                        "Allowed values: 'drop', 'clear', 'keep'.",
                        "'drop' = implants are removed from the player and dropped on death.",
                        "'clear' = implants are removed and not dropped.",
                        "'keep' = implants are kept after respawn.",
                        "Default: drop."
                )
                .define("implant_death_behavior", "drop");

        builder.pop();

        builder.push("items_settings_extra");

        FROZEN_TUMOR_NERF = builder
                .comment("If true, frozen tumors can only apply Frostbite I maximum.")
                .define("frozen_tumor_nerf", true);

        builder.pop();

        KOMMANDANT_ESP_ENTITIES = builder
                .comment(
                        "Entity types visible through wall when having marker or uneasy effects to kommandants.",
                        "Format: modid:entity_name",
                        "Default: minecraft:player",
                        "Examples: minecraft:player, minecraft:villager, minecraft:zombie"
                )
                .defineListAllowEmpty(
                        List.of("esp_entities"),
                        () -> List.of("minecraft:player"),
                        obj -> obj instanceof String
                );

        builder.push("organite_settings");

        ORGANITE_MARKER_FOR_BLACKLIST = builder
                .comment("If true, Organite blocks will still apply the Marker effect to entities in the Spore blacklist (but Mycelium will still be prevented).")
                .define("organite_marker_for_blacklist", true);

        builder.pop();

        builder.push("training_book");
        builder.comment("Settings for the Training Book item, used by players to select their class.");

        TRAINING_BOOK_ENABLED_CLASSES = builder
                .comment("List of class identifiers that can be learned through the Training Book.")
                .defineListAllowEmpty(List.of("enabled_classes"), () -> List.of(
                        "kommandant", "ghost", "medic", "scientist", "berserker"
                ), obj -> obj instanceof String);

        TRAINING_BOOK_MAX_USES = builder
                .comment("Number of times a single Training Book item can be used to change class.",
                        "0 = the book cannot be used to change class at all.")
                .defineInRange("max_uses_per_book", 1, 0, 1000);

        TRAINING_BOOK_READONLY_WITH_ORIGINS = builder
                .comment("If true AND the Origins mod is present, the Training Book given on first join has 0 uses:",
                        "it can be opened to read about the classes, but not to change class (that is done via Origins).",
                        "Only affects that initial book - any other Training Book still uses max_uses_per_book.")
                .define("readonly_when_origins_present", true);

        TRAINING_BOOK_GIVE_ON_FIRST_JOIN = builder
                .comment("If true, players receive a Training Book automatically the first time they join the world.")
                .define("give_book_on_first_join", true);

        TRAINING_BOOK_KOMMANDANT_LOCKED = builder
                .comment("If true, players currently assigned to the Kommandant class cannot change class using the Training Book.")
                .define("kommandant_locked", true);

        builder.pop();

        builder.push("training_book_class_slots");
        builder.comment(
                "Maximum number of players allowed to have each class at the same time.",
                "This counts every player currently assigned to the class, online or offline.",
                "Use -1 for unlimited."
        );

        TRAINING_BOOK_SLOTS_KOMMANDANT = builder.defineInRange("kommandant", -1, -1, 100000);
        TRAINING_BOOK_SLOTS_GHOST = builder.defineInRange("ghost", 1, -1, 100000);
        TRAINING_BOOK_SLOTS_MEDIC = builder.defineInRange("medic", 1, -1, 100000);
        TRAINING_BOOK_SLOTS_SCIENTIST = builder.defineInRange("scientist", 1, -1, 100000);
        TRAINING_BOOK_SLOTS_BERSERKER = builder.defineInRange("berserker", 1, -1, 100000);

        builder.pop();

        SPEC = builder.build();
    }

    /** true = los training books solo sirven para leer (0 usos), porque Origins está gestionando las clases. */
    public static boolean isTrainingBookReadOnly() {
        return TRAINING_BOOK_READONLY_WITH_ORIGINS.get() && ModList.get().isLoaded("origins");
    }

    public static boolean isClassEnabledInTrainingBook(String classId) {
        if (classId == null) {
            return false;
        }
        for (String entry : TRAINING_BOOK_ENABLED_CLASSES.get()) {
            if (entry.equalsIgnoreCase(classId)) {
                return true;
            }
            // El nombre antiguo "slasher" ahora es "berserker": aceptar configs viejas.
            if ("berserker".equalsIgnoreCase(classId) && "slasher".equalsIgnoreCase(entry)) {
                return true;
            }
        }
        return false;
    }

    public static int getTrainingBookMaxSlots(String classId) {
        if (classId == null) {
            return 0;
        }
        return switch (classId.toLowerCase()) {
            case "kommandant" -> TRAINING_BOOK_SLOTS_KOMMANDANT.get();
            case "ghost" -> TRAINING_BOOK_SLOTS_GHOST.get();
            case "medic" -> TRAINING_BOOK_SLOTS_MEDIC.get();
            case "scientist" -> TRAINING_BOOK_SLOTS_SCIENTIST.get();
            case "berserker" -> TRAINING_BOOK_SLOTS_BERSERKER.get();
            default -> 0;
        };
    }

    public static boolean shouldKeepImplantsOnDeath() {
        return "keep".equalsIgnoreCase(IMPLANT_DEATH_BEHAVIOR.get());
    }

    public static boolean shouldDropImplantsOnDeath() {
        return "drop".equalsIgnoreCase(IMPLANT_DEATH_BEHAVIOR.get());
    }

    public static boolean shouldClearImplantsOnDeath() {
        return "clear".equalsIgnoreCase(IMPLANT_DEATH_BEHAVIOR.get());
    }
}