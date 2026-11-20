package com.sporeadds.sporeaddsmod.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;
import java.util.Locale;

/**
 * Starting gear given to each class when it is chosen.
 *
 * <p>Defaults reproduce the behaviour that was hard-coded before these options existed (the Medic kit and the Scientist's
 * scalpel; the other classes have no kit). Read values with {@code .get()} at USE time (never copy them into static
 * finals: the config is not loaded yet while classes initialise). Called from {@link SporeAddsConfig}'s static
 * initialiser, before the spec is built.
 *
 * <p>The kit is given every time a player BECOMES the class (Training Book, {@code /class}, Origins, hive surrender for
 * the Kommandant) and, if {@code remove_on_class_change} is on, taken away again when they LEAVE it. The runtime logic is
 * in {@code ClassStartingGear}.
 */
public final class StartingGearConfig {
    /** The options of one class kit. The fields are assigned while the spec is built. */
    public static final class Kit {
        public ForgeConfigSpec.BooleanValue giveStartingGear;
        public ForgeConfigSpec.ConfigValue<List<? extends String>> items;
        public ForgeConfigSpec.BooleanValue removeOnClassChange;
        public ForgeConfigSpec.BooleanValue skipItemsAlreadyCarried;
    }

    public static final Kit KOMMANDANT = new Kit();
    public static final Kit GHOST = new Kit();
    public static final Kit MEDIC = new Kit();
    public static final Kit SCIENTIST = new Kit();
    public static final Kit BERSERKER = new Kit();

    /** Medic only: also put the "Insanity mask" (Spore gas mask) on the player's head. */
    public static ForgeConfigSpec.BooleanValue MEDIC_EQUIP_GAS_MASK;

    private StartingGearConfig() {
    }

    /** The kit of a class identifier ("kommandant", "ghost", "medic", "scientist", "berserker"), or null (e.g. "none"). */
    public static Kit forClass(String classId) {
        if (classId == null) {
            return null;
        }
        return switch (classId.toLowerCase(Locale.ROOT)) {
            case "kommandant" -> KOMMANDANT;
            case "ghost" -> GHOST;
            case "medic" -> MEDIC;
            case "scientist" -> SCIENTIST;
            case "berserker" -> BERSERKER;
            default -> null;
        };
    }

    static void define(ForgeConfigSpec.Builder builder) {
        builder.comment(
                "Starting gear of each class: the items a player receives when they BECOME that class (Training Book, /class",
                "command, Origins, or the hive surrender for the Kommandant).",
                "Item format: \"modid:item\" or \"modid:item;count\" (count defaults to 1). Items that do not fit in the inventory",
                "are dropped at the player's feet. Unknown items are skipped and reported once in the log."
        ).push("starting_gear");

        definePlain(builder, "kommandant", KOMMANDANT, List.of(), false,
                "Kommandant (hive surrender, Training Book or /class). The Kommandant never had a starting kit.");
        definePlain(builder, "ghost", GHOST, List.of(), false,
                "Ghost. The Ghost never had a starting kit.");

        builder.comment("Medic. The original kit: an Injector and 16 Throwable Bandages, plus the Insanity gas mask.").push("medic");
        defineKit(builder, MEDIC, List.of("sporeadd:injector", "sporeadd:throwable_bandages;16"), false);
        MEDIC_EQUIP_GAS_MASK = builder
                .comment("If true, the Medic is also equipped with the Insanity mask (a Spore gas mask) on the head, replacing whatever",
                        "the player wears there, and gets it back after every respawn (only while Origins is installed, as before).",
                        "Whether the mask has Curse of Binding is controlled by items_settings.medic_gas_mask_curse_of_binding.",
                        "If false, no mask is given or re-equipped and none is taken away. Default: true.")
                .define("equip_gas_mask", true);
        builder.pop();

        builder.comment("Scientist. The original kit: a Scalpel.").push("scientist");
        defineKit(builder, SCIENTIST, List.of("sporeadd:scalpel"), true);
        builder.pop();

        definePlain(builder, "berserker", BERSERKER, List.of(), false,
                "Berserker. The Berserker never had a starting kit.");

        builder.pop();
    }

    private static void definePlain(ForgeConfigSpec.Builder builder, String id, Kit kit, List<String> defaults,
                                    boolean skipCarried, String description) {
        builder.comment(description).push(id);
        defineKit(builder, kit, defaults, skipCarried);
        builder.pop();
    }

    /** Defines the four options common to every kit inside the section that is currently pushed. */
    private static void defineKit(ForgeConfigSpec.Builder builder, Kit kit, List<String> defaults, boolean skipCarried) {
        kit.giveStartingGear = builder
                .comment("Master switch. If true, the class gets its kit (below) when a player becomes it.",
                        "If false, nothing is given and nothing is taken away for this class. Default: true.")
                .define("give_starting_gear", true);
        kit.items = builder
                .comment("Items given when a player becomes this class. Format: \"modid:item\" or \"modid:item;count\".",
                        "Example: [\"minecraft:bread;16\", \"sporeadd:scalpel\"]. An empty list gives nothing.",
                        "Default: " + (defaults.isEmpty() ? "empty (no kit)." : String.join(", ", defaults) + "."))
                .defineListAllowEmpty(List.of("items"), () -> defaults, ConfigItems::isValidEntry);
        kit.removeOnClassChange = builder
                .comment("If true, when a player stops being this class every stack of the items in the list above (main inventory",
                        "and offhand) is removed from them again. WARNING: it removes ALL items of those types, including ones the",
                        "player picked up or crafted, so do not combine it with common items like bread unless you want that.",
                        "If false, the player keeps the kit. Default: true (the original Medic and Scientist behaviour).")
                .define("remove_on_class_change", true);
        kit.skipItemsAlreadyCarried = builder
                .comment("If true, an item of the list is not given when the player already carries at least one of it (inventory or",
                        "offhand), so changing class repeatedly never duplicates it.",
                        "Default: " + (skipCarried ? "true (the original Scientist scalpel rule)." : "false (the original Medic kit rule)."))
                .define("skip_items_already_carried", skipCarried);
    }
}
