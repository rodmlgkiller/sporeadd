package com.sporeadds.sporeaddsmod.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class SporeAddsClientConfig {
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.IntValue SPORE_BAR_X;
    public static final ModConfigSpec.IntValue SPORE_BAR_Y;
    public static final ModConfigSpec.DoubleValue SPORE_BAR_OPACITY;

    public static final ModConfigSpec.IntValue ARMOR_BAR_X;
    public static final ModConfigSpec.IntValue ARMOR_BAR_Y;
    public static final ModConfigSpec.DoubleValue ARMOR_BAR_OPACITY;

    public static final ModConfigSpec.IntValue ABILITY_CHARGE_BAR_X;
    public static final ModConfigSpec.IntValue ABILITY_CHARGE_BAR_Y;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("hud");

        SPORE_BAR_X = builder
                .comment("Horizontal offset for the spore bar.")
                .defineInRange("spore_bar_x", -6, -1000, 1000);

        SPORE_BAR_Y = builder
                .comment("Vertical offset for the spore bar.")
                .defineInRange("spore_bar_y", -72, -1000, 1000);

        SPORE_BAR_OPACITY = builder
                .comment("Opacity of the spore bar. 0.0 = invisible, 1.0 = fully visible.")
                .defineInRange("spore_bar_opacity", 1.0, 0.0, 1.0);

        ARMOR_BAR_X = builder
                .comment("Horizontal offset for the armor bar.")
                .defineInRange("armor_bar_x", 0, -1000, 1000);

        ARMOR_BAR_Y = builder
                .comment("Vertical offset for the armor bar.")
                .defineInRange("armor_bar_y", -95, -1000, 1000);

        ARMOR_BAR_OPACITY = builder
                .comment("Opacity of the armor bar. 0.0 = invisible, 1.0 = fully visible.")
                .defineInRange("armor_bar_opacity", 1.0, 0.0, 1.0);

        ABILITY_CHARGE_BAR_X = builder
                .comment("Horizontal offset for the Caustic/Gluttonous ability charge bar (relative to its default position above the spore bar).")
                .defineInRange("ability_charge_bar_x", 0, -1000, 1000);

        ABILITY_CHARGE_BAR_Y = builder
                .comment("Vertical offset for the Caustic/Gluttonous ability charge bar (relative to its default position above the spore bar).")
                .defineInRange("ability_charge_bar_y", 0, -1000, 1000);

        builder.pop();

        SPEC = builder.build();
    }
}