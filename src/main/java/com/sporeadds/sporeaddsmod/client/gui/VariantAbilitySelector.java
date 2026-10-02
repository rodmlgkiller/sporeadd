package com.sporeadds.sporeaddsmod.client.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class VariantAbilitySelector {

    public static List<Component> getVariantDescription(int abilityNumber, String subclass, Minecraft mc, SporeAbilitySelector gui) {
        switch (subclass.toLowerCase()) {
            case "caustic":
                return getCausticDescription(abilityNumber, mc, gui);
            case "abyssal":
                return getAbyssalDescription(abilityNumber, mc, gui);
            case "gluttonous":
                return getGluttonousDescription(abilityNumber, mc, gui);
            default:
                return gui.getBaseDescription(abilityNumber);
        }
    }

    private static List<Component> getCausticDescription(int abilityNumber, Minecraft mc, SporeAbilitySelector gui) {
        List<Component> tooltip = new ArrayList<>();

        switch (abilityNumber) {
            case 6:
                tooltip.add(Component.translatable("tooltip.sporeadd.power6_caustic.title").withStyle(ChatFormatting.GREEN));
                if (mc.player != null) {
                    mc.player.getCapability(com.sporeadds.sporeaddsmod.level.PlayerLevelProvider.PLAYER_LVL).ifPresent(levelCap -> {
                        int rawLevel = levelCap.getLevel();
                        int sporeLevel = Math.max(rawLevel, 0);

                        int effectDuration = 20 + (sporeLevel * 3);
                        int effectPower = Math.min(5, (int) Math.floor(sporeLevel / 2.0));
                        int amplifier = Math.max(effectPower - 1, 0);
                        int extraHealth = 10 * rawLevel;

                        String strengthRoman = (amplifier == 0) ? "I" : (amplifier == 1) ? "II" : (amplifier == 2) ? "III" : (amplifier == 3) ? "IV" : "V";

                        tooltip.add(Component.literal(""));
                        tooltip.add(Component.translatable("tooltip.sporeadd.generic.active_execution"));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power6_caustic.active_desc1"));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power6_caustic.active_desc2"));
                        tooltip.add(Component.literal(""));

                        tooltip.add(Component.translatable("tooltip.sporeadd.power6.costs_header"));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power6.cost_max"));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power6.cost_discount1"));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power6.cost_discount2"));

                        tooltip.add(Component.literal(""));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power6.buffs_header", rawLevel));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power6.buff_points"));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power6.buff_hp", extraHealth));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power6.buff_res", "I", effectDuration));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power6.buff_str", strengthRoman, effectDuration));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power6.buff_spd", effectDuration));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power6.buff_glow", effectDuration));

                        tooltip.add(Component.literal(""));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power6.note1"));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power6.note2"));

                        tooltip.add(Component.literal(""));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power6_caustic.note1"));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power6_caustic.note2"));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power6_caustic.note3"));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power6_caustic.note4"));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power6_caustic.note5"));
                    });
                }
                break;

            case 9:
                tooltip.add(Component.translatable("tooltip.sporeadd.power9_caustic.title").withStyle(ChatFormatting.GREEN));
                if (mc.player != null) {
                    mc.player.getCapability(com.sporeadds.sporeaddsmod.spore.PlayerSporeProvider.PLAYER_CAP).ifPresent(sporeCap -> {
                        int cost = 5;
                        String biomassColor = (sporeCap.getSpore() >= cost) ? "§a" : "§c";
                        tooltip.add(Component.translatable("tooltip.sporeadd.generic.biomass", biomassColor, sporeCap.getSpore(), cost));

                        tooltip.add(Component.literal(""));
                        tooltip.add(Component.translatable("tooltip.sporeadd.generic.active_execution"));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power9_caustic.active_desc1"));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power9_caustic.active_desc2"));

                        tooltip.add(Component.literal(""));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power9_caustic.effects_header"));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power9_caustic.effect_spread"));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power9_caustic.effect_mycelium"));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power9_caustic.effect_exposed1"));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power9_caustic.effect_exposed2"));

                        tooltip.add(Component.literal(""));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power9_caustic.exposed_header"));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power9_caustic.exposed_desc1"));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power9_caustic.exposed_desc2"));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power9_caustic.exposed_desc3"));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power9_caustic.exposed_desc4"));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power9_caustic.exposed_desc5"));
                    });
                }
                break;

            case 12:
                tooltip.add(Component.translatable("tooltip.sporeadd.power12_caustic.title").withStyle(ChatFormatting.GREEN));

                if (mc.player != null) {
                    mc.player.getCapability(com.sporeadds.sporeaddsmod.spore.PlayerSporeProvider.PLAYER_CAP).ifPresent(sporeCap -> {
                        int cost = 30;
                        String biomassColor = (sporeCap.getSpore() >= cost) ? "§a" : "§c";
                        tooltip.add(Component.translatable("tooltip.sporeadd.generic.biomass", biomassColor, sporeCap.getSpore(), cost));
                    });
                }

                tooltip.add(Component.literal(""));
                tooltip.add(Component.translatable("tooltip.sporeadd.generic.active_execution"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power12_caustic.active_desc1"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power12_caustic.active_desc2"));

                tooltip.add(Component.literal(""));
                tooltip.add(Component.translatable("tooltip.sporeadd.power12_caustic.exo_header"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power12_caustic.exo_hp"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power12_caustic.exo_absorb1"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power12_caustic.exo_absorb2"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power12_caustic.exo_kb1"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power12_caustic.exo_kb2"));

                tooltip.add(Component.literal(""));
                tooltip.add(Component.translatable("tooltip.sporeadd.power12_caustic.corrosive_header"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power12_caustic.corrosive_desc1"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power12_caustic.corrosive_desc2"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power12_caustic.corrosive_desc3"));

                tooltip.add(Component.literal(""));
                tooltip.add(Component.translatable("tooltip.sporeadd.power12_caustic.regen_header"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power12_caustic.regen_rate"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power12_caustic.regen_res"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power12_caustic.regen_heal"));

                tooltip.add(Component.literal(""));
                tooltip.add(Component.translatable("tooltip.sporeadd.power12_caustic.warning1"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power12_caustic.warning2"));
                break;

            case 13:
                tooltip.add(Component.translatable("tooltip.sporeadd.power13_caustic.title").withStyle(ChatFormatting.GREEN));

                if (mc.player != null) {
                    mc.player.getCapability(com.sporeadds.sporeaddsmod.spore.PlayerSporeProvider.PLAYER_CAP).ifPresent(sporeCap -> {
                        int cost = 20;
                        String biomassColor = (sporeCap.getSpore() >= cost) ? "§a" : "§c";
                        tooltip.add(Component.translatable("tooltip.sporeadd.generic.biomass", biomassColor, sporeCap.getSpore(), cost));
                    });
                }

                tooltip.add(Component.literal(""));
                tooltip.add(Component.translatable("tooltip.sporeadd.generic.active_execution"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power13_caustic.active_desc1"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power13_caustic.active_desc2"));

                tooltip.add(Component.literal(""));
                tooltip.add(Component.translatable("tooltip.sporeadd.power13_caustic.emergence_header"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power13_caustic.emergence_dig1"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power13_caustic.emergence_dig2"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power13_caustic.emergence_size1"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power13_caustic.emergence_size2"));

                tooltip.add(Component.literal(""));
                tooltip.add(Component.translatable("tooltip.sporeadd.power13_caustic.dome_header"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power13_caustic.dome_grow1"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power13_caustic.dome_grow2"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power13_caustic.dome_shrink"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power13_caustic.dome_death1"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power13_caustic.dome_death2"));

                tooltip.add(Component.literal(""));
                tooltip.add(Component.translatable("tooltip.sporeadd.power13_caustic.effects_header"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power13_caustic.effects_blind1"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power13_caustic.effects_blind2"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power13_caustic.effects_weak1"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power13_caustic.effects_weak2"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power13_caustic.effects_spore_note1"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power13_caustic.effects_spore_note2"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power13_caustic.effects_exempt"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power13_caustic.effects_acid"));

                if (mc.level != null) {
                    int levelsToLose = com.sporeadds.sporeaddsmod.config.SporeAddsConfig.NUKE_LEVEL_PENALTY.get();
                    if (levelsToLose > 0) {
                        tooltip.add(Component.literal(""));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power13_caustic.sacrifice_line1"));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power13_caustic.sacrifice_line2", levelsToLose));
                    }
                }
                break;

            default:
                return gui.getBaseDescription(abilityNumber);
        }

        return tooltip;
    }

    private static List<Component> getAbyssalDescription(int abilityNumber, Minecraft mc, SporeAbilitySelector gui) {
        List<Component> tooltip = new ArrayList<>();

        switch (abilityNumber) {
            case 8:
                tooltip.add(Component.translatable("tooltip.sporeadd.power8_abyssal.title").withStyle(ChatFormatting.AQUA));

                if (mc.player != null) {
                    mc.player.getCapability(com.sporeadds.sporeaddsmod.spore.PlayerSporeProvider.PLAYER_CAP).ifPresent(sporeCap -> {
                        int cost = 15;
                        String biomassColor = (sporeCap.getSpore() >= cost) ? "§a" : "§c";
                        tooltip.add(Component.translatable("tooltip.sporeadd.generic.biomass", biomassColor, sporeCap.getSpore(), cost));
                    });
                }

                tooltip.add(Component.literal(""));
                tooltip.add(Component.translatable("tooltip.sporeadd.generic.active_execution"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power8_abyssal.active_desc1"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power8_abyssal.active_desc2"));

                tooltip.add(Component.literal(""));
                tooltip.add(Component.translatable("tooltip.sporeadd.power8_abyssal.effects_header"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power8_abyssal.effect_pull"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power8_abyssal.effect_water"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power8_abyssal.effect_control"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power8_abyssal.effect_mark"));

                tooltip.add(Component.literal(""));
                tooltip.add(Component.translatable("tooltip.sporeadd.power8_abyssal.note1"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power8_abyssal.note2"));
                break;

            case 9:
                tooltip.add(Component.translatable("tooltip.sporeadd.power9_abyssal.title").withStyle(ChatFormatting.AQUA));

                if (mc.player != null) {
                    mc.player.getCapability(com.sporeadds.sporeaddsmod.spore.PlayerSporeProvider.PLAYER_CAP).ifPresent(sporeCap -> {
                        int cost = 5;
                        String biomassColor = (sporeCap.getSpore() >= cost) ? "§a" : "§c";
                        tooltip.add(Component.translatable("tooltip.sporeadd.generic.biomass", biomassColor, sporeCap.getSpore(), cost));
                    });

                    mc.player.getCapability(com.sporeadds.sporeaddsmod.level.PlayerLevelProvider.PLAYER_LVL).ifPresent(levelCap -> {
                        int curLevel = levelCap.getLevel();
                        double swimPenalty = curLevel > 5 ? -0.5D - (0.1D * (curLevel - 5)) : -0.5D;

                        tooltip.add(Component.literal(""));
                        tooltip.add(Component.translatable("tooltip.sporeadd.generic.active_execution"));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power9_abyssal.active_desc1"));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power9_abyssal.active_desc2"));

                        tooltip.add(Component.literal(""));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power9_abyssal.effects_header"));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power9_abyssal.effect_vortex"));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power9_abyssal.effect_proto"));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power9_abyssal.effect_naiad"));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power9_abyssal.effect_swim_slow", String.format("%.1f", swimPenalty)));
                    });
                }
                break;

            case 12:
                tooltip.add(Component.translatable("tooltip.sporeadd.power12_abyssal.title").withStyle(ChatFormatting.AQUA));

                if (mc.player != null) {
                    mc.player.getCapability(com.sporeadds.sporeaddsmod.spore.PlayerSporeProvider.PLAYER_CAP).ifPresent(sporeCap -> {
                        int cost = 30;
                        String biomassColor = (sporeCap.getSpore() >= cost) ? "§a" : "§c";
                        tooltip.add(Component.translatable("tooltip.sporeadd.generic.biomass", biomassColor, sporeCap.getSpore(), cost));
                    });
                }

                tooltip.add(Component.literal(""));
                tooltip.add(Component.translatable("tooltip.sporeadd.generic.active_execution"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power12_abyssal.active_desc1"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power12_abyssal.active_desc2"));

                tooltip.add(Component.literal(""));
                tooltip.add(Component.translatable("tooltip.sporeadd.power12_abyssal.effects_header"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power12_abyssal.effect_trident"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power12_abyssal.effect_riptide"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power12_abyssal.effect_binding"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power12_abyssal.effect_storm"));

                tooltip.add(Component.literal(""));
                tooltip.add(Component.translatable("tooltip.sporeadd.power12_abyssal.note1"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power12_abyssal.note2"));
                break;

            case 13:
                tooltip.add(Component.translatable("tooltip.sporeadd.power13_abyssal.title").withStyle(ChatFormatting.AQUA));

                if (mc.player != null) {
                    mc.player.getCapability(com.sporeadds.sporeaddsmod.spore.PlayerSporeProvider.PLAYER_CAP).ifPresent(sporeCap -> {
                        int cost = 20;
                        String biomassColor = (sporeCap.getSpore() >= cost) ? "§a" : "§c";
                        tooltip.add(Component.translatable("tooltip.sporeadd.generic.biomass", biomassColor, sporeCap.getSpore(), cost));
                    });
                }

                tooltip.add(Component.literal(""));
                tooltip.add(Component.translatable("tooltip.sporeadd.generic.active_execution"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power13_abyssal.active_desc1"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power13_abyssal.active_desc2"));

                tooltip.add(Component.literal(""));
                tooltip.add(Component.translatable("tooltip.sporeadd.power13_abyssal.effects_header"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power13_abyssal.effect_summon"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power13_abyssal.effect_pressure"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power13_abyssal.effect_drag"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power13_abyssal.effect_rain"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power13_abyssal.effect_finish"));

                tooltip.add(Component.literal(""));
                tooltip.add(Component.translatable("tooltip.sporeadd.power13_abyssal.note1"));
                if (mc.level != null) {
                    int levelsToLose = com.sporeadds.sporeaddsmod.config.SporeAddsConfig.NUKE_LEVEL_PENALTY.get() / 2;
                tooltip.add(Component.translatable("tooltip.sporeadd.power13_abyssal.note2", levelsToLose));
                }
                break;

            default:
                return gui.getBaseDescription(abilityNumber);
        }

        return tooltip;
    }
    private static List<Component> getGluttonousDescription(int abilityNumber, Minecraft mc, SporeAbilitySelector gui) {
        List<Component> tooltip = new ArrayList<>();

        switch (abilityNumber) {
            case 4:
                tooltip.add(Component.translatable("tooltip.sporeadd.power4_gluttonous.title").withStyle(ChatFormatting.GOLD));

                tooltip.add(Component.literal(""));
                tooltip.add(Component.translatable("tooltip.sporeadd.generic.active_execution"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power4.active_desc"));

                tooltip.add(Component.literal(""));
                tooltip.add(Component.translatable("tooltip.sporeadd.power4.entities_header"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power4.entity_infected"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power4.entity_evolved"));

                tooltip.add(Component.literal(""));
                tooltip.add(Component.translatable("tooltip.sporeadd.power4.blocks_header"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power4.block_remains1"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power4.block_remains2"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power4.block_bulb1"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power4.block_bulb2"));

                tooltip.add(Component.literal(""));
                tooltip.add(Component.translatable("tooltip.sporeadd.power4_gluttonous.extra_header"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power4_gluttonous.extra_desc1"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power4_gluttonous.extra_desc2"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power4_gluttonous.extra_desc3"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power4_gluttonous.extra_desc4"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power4_gluttonous.extra_desc5"));
                break;

            case 6:
                tooltip.add(Component.translatable("tooltip.sporeadd.power6_gluttonous.title").withStyle(ChatFormatting.GOLD));
                tooltip.add(Component.literal(""));
                tooltip.add(Component.translatable("tooltip.sporeadd.generic.active_execution"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power6_gluttonous.active_desc1"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power6_gluttonous.active_desc2"));

                tooltip.add(Component.literal(""));
                tooltip.add(Component.translatable("tooltip.sporeadd.power6_gluttonous.effects_header"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power6_gluttonous.effect_transform"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power6_gluttonous.effect_priority"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power6_gluttonous.effect_feed"));

                tooltip.add(Component.literal(""));
                tooltip.add(Component.translatable("tooltip.sporeadd.power6_gluttonous.cuisine_header"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power6_gluttonous.cuisine_desc1"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power6_gluttonous.cuisine_desc2"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power6_gluttonous.cuisine_desc3"));

                tooltip.add(Component.literal(""));
                tooltip.add(Component.translatable("tooltip.sporeadd.power6_gluttonous.note1"));
                break;

            case 9:
                tooltip.add(Component.translatable("tooltip.sporeadd.power9_gluttonous.title").withStyle(ChatFormatting.GOLD));

                if (mc.player != null) {
                    mc.player.getCapability(com.sporeadds.sporeaddsmod.spore.PlayerSporeProvider.PLAYER_CAP).ifPresent(sporeCap -> {
                        int cost = 5;
                        String biomassColor = (sporeCap.getSpore() >= cost) ? "§a" : "§c";
                        tooltip.add(Component.translatable("tooltip.sporeadd.generic.biomass", biomassColor, sporeCap.getSpore(), cost));
                    });
                }

                tooltip.add(Component.literal(""));
                tooltip.add(Component.translatable("tooltip.sporeadd.generic.active_execution"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power9_gluttonous.active_desc1"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power9_gluttonous.active_desc2"));

                tooltip.add(Component.literal(""));
                tooltip.add(Component.translatable("tooltip.sporeadd.power9_gluttonous.effects_header"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power9_gluttonous.effect_spread"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power9_gluttonous.effect_mycelium"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power9_gluttonous.effect_exposed1"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power9_gluttonous.effect_exposed2"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power9_gluttonous.effect_exposed3"));

                tooltip.add(Component.literal(""));
                tooltip.add(Component.translatable("tooltip.sporeadd.power9_gluttonous.drop_header"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power9_gluttonous.drop_desc1"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power9_gluttonous.drop_desc2"));
                break;

            case 12:
                tooltip.add(Component.translatable("tooltip.sporeadd.power12_gluttonous.title").withStyle(ChatFormatting.GOLD));

                if (mc.player != null) {
                    mc.player.getCapability(com.sporeadds.sporeaddsmod.spore.PlayerSporeProvider.PLAYER_CAP).ifPresent(sporeCap -> {
                        int cost = 30;
                        String biomassColor = (sporeCap.getSpore() >= cost) ? "§a" : "§c";
                        tooltip.add(Component.translatable("tooltip.sporeadd.generic.biomass", biomassColor, sporeCap.getSpore(), cost));
                    });
                }

                tooltip.add(Component.literal(""));
                tooltip.add(Component.translatable("tooltip.sporeadd.generic.active_execution"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power12_gluttonous.active_desc1"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power12_gluttonous.active_desc2"));

                tooltip.add(Component.literal(""));
                tooltip.add(Component.translatable("tooltip.sporeadd.power12_gluttonous.exo_header"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power12_gluttonous.exo_hp"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power12_gluttonous.exo_absorb1"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power12_gluttonous.exo_absorb2"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power12_gluttonous.exo_kb1"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power12_gluttonous.exo_kb2"));

                tooltip.add(Component.literal(""));
                tooltip.add(Component.translatable("tooltip.sporeadd.power12_gluttonous.critical_header"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power12_gluttonous.critical_desc1"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power12_gluttonous.critical_desc2"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power12_gluttonous.critical_desc3"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power12_gluttonous.critical_desc4"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power12_gluttonous.critical_desc5"));

                tooltip.add(Component.literal(""));
                tooltip.add(Component.translatable("tooltip.sporeadd.power12_gluttonous.regen_header"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power12_gluttonous.regen_rate"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power12_gluttonous.regen_rate2"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power12_gluttonous.regen_res"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power12_gluttonous.regen_heal"));

                tooltip.add(Component.literal(""));
                tooltip.add(Component.translatable("tooltip.sporeadd.power12_gluttonous.scent_header"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power12_gluttonous.scent_desc1"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power12_gluttonous.scent_desc2"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power12_gluttonous.scent_desc3"));

                tooltip.add(Component.literal(""));
                tooltip.add(Component.translatable("tooltip.sporeadd.power12_gluttonous.warning1"));
                if (mc.level != null) {
                    int levelsToLose = com.sporeadds.sporeaddsmod.config.SporeAddsConfig.NUKE_LEVEL_PENALTY.get() / 2;
                    tooltip.add(Component.translatable("tooltip.sporeadd.power12_gluttonous.warning2", levelsToLose));
                }

                break;

            case 13:
                tooltip.add(Component.translatable("tooltip.sporeadd.power13_gluttonous.title").withStyle(ChatFormatting.GOLD));

                tooltip.add(Component.literal(""));
                tooltip.add(Component.translatable("tooltip.sporeadd.generic.active_execution"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power13_gluttonous.active_desc1"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power13_gluttonous.active_desc2"));

                tooltip.add(Component.literal(""));
                tooltip.add(Component.translatable("tooltip.sporeadd.power13_gluttonous.effects_header"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power13_gluttonous.effect_famined1"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power13_gluttonous.effect_famined2"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power13_gluttonous.effect_famined3"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power13_gluttonous.effect_famined4"));

                tooltip.add(Component.literal(""));
                tooltip.add(Component.translatable("tooltip.sporeadd.power13_gluttonous.bite_header"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power13_gluttonous.bite_desc1"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power13_gluttonous.bite_desc2"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power13_gluttonous.bite_desc3"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power13_gluttonous.bite_desc4"));

                tooltip.add(Component.literal(""));
                tooltip.add(Component.translatable("tooltip.sporeadd.power13_gluttonous.pressure_header"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power13_gluttonous.pressure_desc1"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power13_gluttonous.pressure_desc2"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power13_gluttonous.pressure_desc3"));

                tooltip.add(Component.literal(""));
                tooltip.add(Component.translatable("tooltip.sporeadd.power13_gluttonous.note1"));
                tooltip.add(Component.translatable("tooltip.sporeadd.power13_gluttonous.note2"));
                break;

            default:
                return gui.getBaseDescription(abilityNumber);
        }

        return tooltip;
    }
}