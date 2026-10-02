package com.sporeadds.sporeaddsmod.loot;

import java.util.List;
import java.util.Map;

public final class KommandantLootTables {

    private KommandantLootTables() {}

    // Formato: "namespace:item|min|max"
    public static final List<List<String>> BASE = List.of(
            List.of("spore:mutated_heart|0|1", "spore:mutated_fiber|1|3"),
            List.of("spore:mutated_heart|0|1", "spore:mutated_fiber|2|5", "spore:claw_fragment|0|1"),
            List.of("spore:mutated_heart|0|1", "spore:mutated_fiber|3|7", "spore:claw_fragment|1|2", "spore:armor_fragment|0|1"),
            List.of("spore:mutated_heart|0|2", "spore:mutated_fiber|4|8", "spore:claw_fragment|2|3", "spore:armor_fragment|1|2"),
            List.of("spore:mutated_heart|1|2", "spore:mutated_fiber|5|9", "spore:claw_fragment|3|4", "spore:armor_fragment|2|3"),
            List.of("spore:mutated_heart|1|2", "spore:mutated_fiber|6|10", "spore:claw_fragment|4|5", "spore:armor_fragment|3|4", "spore:innards|0|1"),
            List.of("spore:mutated_heart|2|3", "spore:mutated_fiber|7|11", "spore:claw_fragment|5|6", "spore:armor_fragment|4|5", "spore:innards|1|2"),
            List.of("spore:mutated_heart|2|3", "spore:mutated_fiber|7|12", "spore:claw_fragment|6|7", "spore:armor_fragment|5|7", "spore:innards|1|3"),
            List.of("spore:mutated_heart|2|3", "spore:mutated_fiber|9|15", "spore:claw_fragment|8|11", "spore:armor_fragment|11|13", "spore:innards|2|4", "spore:reforged_biomass_t|2|3"),
            List.of("spore:mutated_heart|3|3", "spore:mutated_fiber|11|18", "spore:claw_fragment|11|13", "spore:armor_fragment|13|18", "spore:innards|3|6", "spore:reforged_biomass_t|3|4")
    );

    public static final List<List<String>> CAUSTIC = List.of(
            List.of("spore:sicken_tumor|0|1"),
            List.of("spore:sicken_tumor|1|1"),
            List.of("spore:sicken_tumor|1|2", "spore:corrosive_sack|0|1"),
            List.of("spore:sicken_tumor|1|3", "spore:corrosive_sack|0|1"),
            List.of("spore:sicken_tumor|2|4", "spore:corrosive_sack|1|2"),
            List.of("spore:sicken_tumor|3|5", "spore:corrosive_sack|2|3"),
            List.of("spore:sicken_tumor|4|6", "spore:corrosive_sack|2|3"),
            List.of("spore:sicken_tumor|6|8", "spore:corrosive_sack|3|4"),
            List.of("spore:sicken_tumor|7|9", "spore:corrosive_sack|4|5", "spore:acidic_gland|1|1"),
            List.of("spore:sicken_tumor|7|9", "spore:corrosive_sack|4|5", "spore:acidic_gland|1|1")
    );

    public static final List<List<String>> ABYSSAL = List.of(
            List.of("spore:tendons|0|1"),
            List.of("spore:tendons|0|1"),
            List.of("spore:tendons|1|2"),
            List.of("spore:tendons|1|2"),
            List.of("spore:tendons|1|3", "spore:organoid_membrane|0|1"),
            List.of("spore:tendons|2|4", "spore:organoid_membrane|1|2"),
            List.of("spore:tendons|3|5", "spore:organoid_membrane|1|3", "spore:reforged_biomass_w|1|1"),
            List.of("spore:tendons|4|6", "spore:organoid_membrane|2|4", "spore:reforged_biomass_w|1|2"),
            List.of("spore:tendons|5|7", "spore:organoid_membrane|3|5", "spore:ligaments|1|1", "spore:reforged_biomass_w|2|3"),
            List.of("spore:tendons|5|8", "spore:organoid_membrane|4|6", "spore:ligaments|1|1", "spore:reforged_biomass_w|3|4")
    );

    public static final List<List<String>> GLUTTONOUS = List.of(
            List.of("spore:innards|1|1"),
            List.of("spore:innards|1|2"),
            List.of("spore:innards|2|3"),
            List.of("spore:innards|2|4"),
            List.of("spore:innards|3|4", "spore:bile_tumor|1|1"),
            List.of("spore:innards|4|5", "spore:bile_tumor|1|2", "spore:fang|0|1"),
            List.of("spore:innards|4|6", "spore:bile_tumor|2|3", "spore:fang|1|2"),
            List.of("spore:innards|5|7", "spore:bile_tumor|4|5", "spore:fang|1|3"),
            List.of("spore:innards|7|9", "spore:bile_tumor|6|7", "spore:fang|2|4", "spore:hyperbolized_liver|1|1", "spore:reforged_biomass_t|1|3"),
            List.of("spore:innards|10|13", "spore:bile_tumor|5|7", "spore:fang|6|11", "spore:hyperbolized_liver|1|1", "spore:reforged_biomass_t|2|4")
    );

    private static final Map<String, List<List<String>>> SUBCLASS_TABLES = Map.of(
            "caustic", CAUSTIC,
            "abyssal", ABYSSAL,
            "gluttonous", GLUTTONOUS
    );

    public static List<String> getBaseForLevel(int level) {
        int clamped = Math.max(0, Math.min(level, BASE.size() - 1));
        return BASE.get(clamped);
    }

    public static List<String> getSubclassForLevel(String subclass, int level) {
        List<List<String>> table = SUBCLASS_TABLES.get(subclass == null ? "" : subclass.toLowerCase());
        if (table == null) return List.of();
        int clamped = Math.max(0, Math.min(level, table.size() - 1));
        return table.get(clamped);
    }
}