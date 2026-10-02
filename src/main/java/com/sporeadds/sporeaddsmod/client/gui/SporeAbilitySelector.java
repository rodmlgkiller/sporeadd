package com.sporeadds.sporeaddsmod.client.gui;

import net.minecraft.core.registries.BuiltInRegistries;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.sporeadds.sporeaddsmod.PlayerData.PlayerDataProvider;
import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider;
import com.sporeadds.sporeaddsmod.config.SporeAddsConfig;
import com.sporeadds.sporeaddsmod.items.MutagenicCompoundItem;
import com.sporeadds.sporeaddsmod.items.MutagenicCompoundVariant;
import com.sporeadds.sporeaddsmod.level.PlayerLevelProvider;
import com.sporeadds.sporeaddsmod.network.*;
import com.sporeadds.sporeaddsmod.spore.PlayerSporeProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.sporeadds.sporeaddsmod.Powers.Poder3.MOUND_CACHE;

public class SporeAbilitySelector extends Screen {

    private int AbilityHovered;
    private int AbilityClicked;
    private String Switches;
    private int Spore;
    private int Sporelvl;
    private static SporeAbilitySelector instance;
    private List<AbilityButtonArea> abilities = new ArrayList<>();

    // CONFIGURACIÓN DE ESCALA FIJA
    private static final float FIXED_SCALE = 2.0f;
    private final Minecraft mc = Minecraft.getInstance();

    int x;
    int y;
    int spriteW = 194 * 2;
    int spriteH = 216 * 2;

    public SporeAbilitySelector() {
        super(Component.literal(""));
        instance = this;
    }

    public static SporeAbilitySelector getInstance() {
        return instance;
    }

    public int getAbilityClicked() {
        return this.AbilityClicked;
    }

    public void setAbilityClicked(int abilityClicked) {
        this.AbilityClicked = abilityClicked;
    }

    private static class AbilityButtonArea extends ButtonArea {
        private final int abilityNumber;
        public AbilityButtonArea(int x, int y, int w, int h, int abilityNumber, Runnable onClick) {
            super(x, y, w, h, onClick);
            this.abilityNumber = abilityNumber;
        }
        public int getAbilityNumber() {
            return abilityNumber;
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (com.sporeadds.sporeaddsmod.client.SporeKeyMapping.OPEN_MENU.isActiveAndMatches(
                InputConstants.getKey(keyCode, scanCode))) {
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    protected void init() {
        assert mc.player != null;
        PlayerLevelProvider.PLAYER_LVL.get(mc.player).ifPresent(lvl -> {
            Sporelvl = lvl.getLevel() + 4;
        });
        PlayerDataProvider.PLAYER_DATA.get(mc.player).ifPresent(data -> {
            Switches = data.getSwitch();
        });

        // Cálculo de posición basado en la resolución real de la ventana
        int screenW = mc.getWindow().getWidth();
        int screenH = mc.getWindow().getHeight();

        x = (int) ((screenW / FIXED_SCALE - spriteW) / 2);
        y = (int) ((screenH / FIXED_SCALE - spriteH) / 2);

        int buttonWidth = 38 * 2;
        int buttonHeight = 38 * 2;
        int spacing = 6;

        this.clearWidgets();
        abilities.clear();

        for (int i = 0; i < Sporelvl; i++) {
            int col = i % 4;
            int row = i / 4;
            int buttonX = x + 17 * 2 + (col * (buttonWidth + spacing));
            int buttonY = y + 35 * 2 + (row * (buttonHeight + spacing));
            int abilityNumber = i + 1;

            if (i < 12) {
                abilities.add(new AbilityButtonArea(buttonX, buttonY, buttonWidth, buttonHeight, abilityNumber, () -> {
                    setAbilityClicked(abilityNumber);
                    sendAbilityPacket(abilityNumber);
                    switch (abilityNumber) {
                        case 2, 4, 5, 6, 7 -> {
                            char[] chars = Switches.toCharArray();
                            if (Switches.charAt(abilityNumber) == '0') {
                                chars[abilityNumber] = '1';
                            } else {
                                chars[abilityNumber] = '0';
                            }
                            Switches = new String(chars);
                            NetworkHandle.INSTANCE.sendToServer(new DataToServer(Switches));
                            PlayerDataProvider.PLAYER_DATA.get(mc.player).ifPresent(data -> data.setSwitch(Switches));
                        }
                    }
                }));
            }
        }

        if (Sporelvl == 13) {
            abilities.add(new AbilityButtonArea(x + 77 * 2, y + 159 * 2, 40 * 2, 39 * 2, 13, () -> {
                setAbilityClicked(13);
                sendAbilityPacket(13);
            }));
        }

        this.abilities.forEach(this::addRenderableWidget);
    }

    private void sendAbilityPacket(int abilityNumber) {
        switch (abilityNumber) {
            case 12 -> NetworkHandle.INSTANCE.sendToServer(new Poder12ActivatePacket());
            case 13 -> NetworkHandle.INSTANCE.sendToServer(new Poder13UsePacket());
        }
        NetworkHandle.INSTANCE.sendToServer(new PowerUseHandler(abilityNumber));
    }

    private List<Component> getAbilityDescription(int abilityNumber) {
        if (mc.player != null) {
            String subclass = "none";
            var cap = com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider.SPORE_IDENTIFIER.get(mc.player);
            if (cap.isPresent()) {
                subclass = cap.orElseThrow(IllegalStateException::new).getSubclass();
            }

            if (!"none".equalsIgnoreCase(subclass)) {
                // Pasas 'this' a la clase Variant para que pueda llamar a getBaseDescription si lo necesita
                return VariantAbilitySelector.getVariantDescription(abilityNumber, subclass, mc, this);
            }
        }

        return getBaseDescription(abilityNumber);
    }

    // 2. ESTE ES TU CÓDIGO GIGANTE ORIGINAL (solo le cambié el nombre de getAbilityDescription a getBaseDescription)
    // NO ES STATIC, así que todas tus variables funcionan igual que antes.
    public List<Component> getBaseDescription(int abilityNumber) {
        List<Component> tooltip = new ArrayList<>();
        switch (abilityNumber) {
            case 1:
                tooltip.add(Component.translatable("tooltip.sporeadd.power1.title"));
                if (mc.player != null) {
                    PlayerLevelProvider.PLAYER_LVL.get(mc.player).ifPresent(levelCap -> {
                        int curLevel = levelCap.getLevel();
                        int curKnowledge = levelCap.getKnowledgeLevel();

                        final boolean[] hasValidCompound = {false};

                        ItemStack main = mc.player.getMainHandItem();
                        ItemStack off = mc.player.getOffhandItem();

                        SporeIdentifierProvider.SPORE_IDENTIFIER.get(mc.player).ifPresent(data -> {
                            if ("kommandant".equalsIgnoreCase(data.getIdentifier())) {
                                if (main.getItem() instanceof MutagenicCompoundItem) {
                                    MutagenicCompoundVariant mainVariant = MutagenicCompoundItem.getVariant(main);
                                    if (!mainVariant.getSubclassId().equalsIgnoreCase(data.getSubclass())) {
                                        hasValidCompound[0] = true;
                                    }
                                }

                                if (!hasValidCompound[0] && off.getItem() instanceof MutagenicCompoundItem) {
                                    MutagenicCompoundVariant offVariant = MutagenicCompoundItem.getVariant(off);
                                    if (!offVariant.getSubclassId().equalsIgnoreCase(data.getSubclass())) {
                                        hasValidCompound[0] = true;
                                    }
                                }
                            }
                        });

                        tooltip.add(Component.literal(""));
                        tooltip.add(Component.translatable("tooltip.sporeadd.generic.active_execution"));

                        if (curLevel >= 9) {
                            tooltip.add(Component.translatable("tooltip.sporeadd.power1.maxed_title"));
                            tooltip.add(Component.translatable("tooltip.sporeadd.power1.maxed_desc"));

                            tooltip.add(Component.literal(""));
                            tooltip.add(Component.translatable("tooltip.sporeadd.generic.details_header"));

                            boolean maxLevelNoVariant = !hasValidCompound[0];
                            int cocoonHp = maxLevelNoVariant ? 300 : (int) ((curLevel + 1) * 100.0);
                            int cocoonSeconds = maxLevelNoVariant ? 30 : 30 + (30 * (curLevel / 3));

                            tooltip.add(Component.translatable("tooltip.sporeadd.power1.cocoon_hp", cocoonHp));
                            tooltip.add(Component.translatable("tooltip.sporeadd.power1.cocoon_time", cocoonSeconds));

                            tooltip.add(Component.literal(""));
                            tooltip.add(Component.translatable("tooltip.sporeadd.power1.reinforcement_title"));
                            tooltip.add(Component.translatable("tooltip.sporeadd.power1.reinforcement_line1"));
                            tooltip.add(Component.translatable("tooltip.sporeadd.power1.reinforcement_line2"));
                            tooltip.add(Component.translatable("tooltip.sporeadd.power1.reinforcement_line3"));
                            tooltip.add(Component.translatable("tooltip.sporeadd.power1.warning_broken"));
                            return;
                        }

                        int nextLevel = curLevel + 1;
                        String configString = getEvolutionCostString(nextLevel);

                        int phaseCost = 50;
                        if (configString.contains("cost:")) {
                            try {
                                String costStr = configString.substring(configString.indexOf("cost:") + 5).trim();
                                phaseCost = Integer.parseInt(costStr);
                            } catch (Exception ignored) {}
                        }

                        tooltip.add(Component.translatable("tooltip.sporeadd.power1.req_stage", nextLevel));

                        String biomassColor = (Spore >= phaseCost) ? "§a" : "§c";
                        tooltip.add(Component.translatable("tooltip.sporeadd.power1.req_biomass", biomassColor, Spore, phaseCost));

                        boolean requiresItems = (nextLevel > curKnowledge);

                        if (requiresItems) {
                            String itemsPart = configString.split("cost:")[0].trim();
                            if (!itemsPart.equals("()") && !itemsPart.isEmpty()) {
                                String[] andGroups = itemsPart.split(",");

                                for (String group : andGroups) {
                                    String[] orOptions = group.split("/");
                                    boolean groupSatisfied = false;

                                    for (String option : orOptions) {
                                        String cleanOption = option.replaceAll("[()]", "").trim();
                                        if (cleanOption.isEmpty()) continue;
                                        String[] parts = cleanOption.split("amount:");
                                        if (parts.length == 2) {
                                            String itemIdStr = parts[0].trim();
                                            int requiredAmount = Integer.parseInt(parts[1].trim());
                                            int countInInventory = countItemInInventory(itemIdStr);
                                            if (countInInventory >= requiredAmount) {
                                                groupSatisfied = true;
                                                break;
                                            }
                                        }
                                    }

                                    String firstOption = orOptions[0].replaceAll("[()]", "").trim();
                                    String[] parts = firstOption.split("amount:");
                                    if (parts.length == 2) {
                                        String itemIdStr = parts[0].trim();
                                        int requiredAmount = Integer.parseInt(parts[1].trim());
                                        int countInInventory = countItemInInventory(itemIdStr);

                                        String[] idParts = itemIdStr.split(":");
                                        String itemName = idParts.length > 1 ? idParts[1] : itemIdStr;
                                        itemName = java.util.Arrays.stream(itemName.split("_"))
                                                .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1))
                                                .collect(java.util.stream.Collectors.joining(" "));

                                        String itemColor = groupSatisfied ? "§a" : "§c";
                                        tooltip.add(Component.literal("- " + itemName + ": " + itemColor + Math.min(countInInventory, requiredAmount) + "/" + requiredAmount));
                                    }
                                }
                            }
                        } else {
                            tooltip.add(Component.translatable("tooltip.sporeadd.power1.items_not_required"));
                        }



                        tooltip.add(Component.literal(""));
                        tooltip.add(Component.translatable("tooltip.sporeadd.generic.details_header"));

                        int cocoonHp = (int) ((curLevel + 1) * 100.0);
                        int cocoonSeconds = 30 + (30 * (curLevel / 3));

                        tooltip.add(Component.translatable("tooltip.sporeadd.power1.cocoon_hp", cocoonHp));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power1.cocoon_time", cocoonSeconds));
                    });
                }
                break;

            case 2:
                tooltip.add(Component.translatable("tooltip.sporeadd.power2.title"));

                if (mc.player != null) {
                    PlayerLevelProvider.PLAYER_LVL.get(mc.player).ifPresent(levelCap -> {
                        int level = levelCap.getLevel();

                        boolean isActive = false;
                        if (Switches != null && Switches.length() > 2) {
                            isActive = Switches.charAt(2) == '1';
                        }
                        String statusStr = isActive ? "§a[ON]" : "§c[OFF]";
                        tooltip.add(Component.translatable("tooltip.sporeadd.generic.status", statusStr));

                        tooltip.add(Component.literal(""));
                        tooltip.add(Component.translatable("tooltip.sporeadd.generic.passive_effect"));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power2.passive_desc"));

                        int dur = Math.max(level * 5, 5);
                        int amp = level / 3;
                        String romanNum = (amp == 0) ? "I" : (amp == 1) ? "II" : (amp == 2) ? "III" : "IV";
                        tooltip.add(Component.translatable("tooltip.sporeadd.power2.passive_stats", dur, romanNum));

                        tooltip.add(Component.literal(""));
                        tooltip.add(Component.translatable("tooltip.sporeadd.generic.active_execution"));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power2.active_desc"));

                        tooltip.add(Component.literal(""));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power2.outcomes_header"));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power2.outcome_players"));

                        tooltip.add(Component.translatable("tooltip.sporeadd.power2.outcome_infected_adv1"));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power2.outcome_infected_adv2"));

                        tooltip.add(Component.translatable("tooltip.sporeadd.power2.outcome_villagers"));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power2.outcome_zombie_villagers"));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power2.outcome_witch"));

                        int zProb = 20 + (level * 5);
                        tooltip.add(Component.translatable("tooltip.sporeadd.power2.outcome_undead", zProb));

                        tooltip.add(Component.translatable("tooltip.sporeadd.power2.outcome_enderman"));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power2.outcome_wither"));

                        tooltip.add(Component.literal(""));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power2.warning1"));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power2.warning2"));
                    });
                }
                break;
            case 3:
                tooltip.add(Component.translatable("tooltip.sporeadd.power3.title"));

                if (mc.player != null) {
                    PlayerSporeProvider.PLAYER_CAP.get(mc.player).ifPresent(spore -> {
                        int cost = 15;
                        String biomassColor = (Spore >= cost) ? "§a" : "§c";
                        tooltip.add(Component.translatable("tooltip.sporeadd.generic.biomass", biomassColor, Spore, cost));

                        tooltip.add(Component.literal(""));
                        tooltip.add(Component.translatable("tooltip.sporeadd.generic.active_execution"));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power3.active_desc1"));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power3.active_desc2"));

                        List<java.util.UUID> mounds = spore.getMoundRegistry().getDisplayList();

                        tooltip.add(Component.literal(""));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power3.mounds_header", mounds.size()));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power3.mounds_command_info"));

                        java.util.UUID preferred = spore.getMoundRegistry().getPreferredMound();

                        for (int i = 0; i < 10; i++) {
                            int slotNumber = i + 1;

                            if (i < mounds.size()) {
                                java.util.UUID moundId = mounds.get(i);

                                boolean isPreferred = (preferred != null && preferred.equals(moundId));
                                String prefix = isPreferred ? "§e[★] §r" : "";

                                String customName = spore.getMoundRegistry().getName(moundId);
                                String slotLabel = (customName != null && !customName.isBlank())
                                        ? (slotNumber + " [" + customName + "]")
                                        : String.valueOf(slotNumber);

                                net.minecraft.world.entity.Entity entity = null;
                                if (mc.level != null) {
                                    for (net.minecraft.world.entity.Entity e : mc.level.entitiesForRendering()) {
                                        if (e.getUUID().equals(moundId)) {
                                            entity = e;
                                            break;
                                        }
                                    }
                                }

                                if (entity != null && mc.level != null) {
                                    String dim = mc.level.dimension().location().getPath();
                                    dim = dim.substring(0, 1).toUpperCase() + dim.substring(1);

                                    int mx = (int) entity.getX();
                                    int my = (int) entity.getY();
                                    int mz = (int) entity.getZ();

                                    com.sporeadds.sporeaddsmod.mound.MoundRegistry.MoundLocation known =
                                            spore.getMoundRegistry().getLastKnownLocation(moundId);

                                    boolean changed = known == null
                                            || !known.getDimension().equals(dim)
                                            || known.getX() != mx
                                            || known.getY() != my
                                            || known.getZ() != mz;

                                    if (changed) {
                                        NetworkHandle.INSTANCE.sendToServer(
                                                new UpdateMoundLocationPacket(moundId, dim, mx, my, mz)
                                        );
                                    }

                                    String locationData = dim + " [" + mx + ", " + my + ", " + mz + "]";
                                    tooltip.add(Component.literal(prefix)
                                            .append(Component.translatable("tooltip.sporeadd.power3.mound_known", slotLabel, locationData)));
                                } else {
                                    com.sporeadds.sporeaddsmod.mound.MoundRegistry.MoundLocation location =
                                            spore.getMoundRegistry().getLastKnownLocation(moundId);

                                    if (location != null) {
                                        tooltip.add(Component.literal(prefix)
                                                .append(Component.translatable(
                                                        "tooltip.sporeadd.power3.mound_known",
                                                        slotLabel,
                                                        location.toDisplayString()
                                                )));
                                    } else {
                                        String shortId = moundId.toString().substring(0, 8).toUpperCase();
                                        tooltip.add(Component.literal(prefix)
                                                .append(Component.translatable("tooltip.sporeadd.power3.mound_connecting", slotLabel, shortId)));
                                    }
                                }
                            } else {
                                tooltip.add(Component.translatable("tooltip.sporeadd.power3.mound_empty", slotNumber));
                            }
                        }

                        tooltip.add(Component.literal(""));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power3.warning_max"));
                    });
                }
                break;

            case 4:
                tooltip.add(Component.translatable("tooltip.sporeadd.power4.title"));

                if (mc.player != null) {
                    boolean isActive = false;
                    if (Switches != null && Switches.length() > 4) {
                        isActive = Switches.charAt(4) == '1';
                    }
                    String statusStr = isActive ? "§a[ON]" : "§c[OFF]";
                    tooltip.add(Component.translatable("tooltip.sporeadd.generic.status", statusStr));

                    tooltip.add(Component.literal(""));
                    tooltip.add(Component.translatable("tooltip.sporeadd.generic.active_effect"));
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
                }
                break;

            case 5:
                tooltip.add(Component.translatable("tooltip.sporeadd.power5.title"));

                if (mc.player != null) {
                    boolean isActive = false;
                    if (Switches != null && Switches.length() > 5) {
                        isActive = Switches.charAt(5) == '1';
                    }
                    String statusStr = isActive ? "§a[ON]" : "§c[OFF]";
                    tooltip.add(Component.translatable("tooltip.sporeadd.generic.status", statusStr));

                    tooltip.add(Component.literal(""));
                    tooltip.add(Component.translatable("tooltip.sporeadd.generic.active_effects"));
                    tooltip.add(Component.translatable("tooltip.sporeadd.power5.active_desc"));

                    tooltip.add(Component.literal(""));
                    tooltip.add(Component.translatable("tooltip.sporeadd.power5.harvest_header"));
                    tooltip.add(Component.translatable("tooltip.sporeadd.power5.harvest_line1"));
                    tooltip.add(Component.translatable("tooltip.sporeadd.power5.harvest_line2"));
                    tooltip.add(Component.translatable("tooltip.sporeadd.power5.harvest_line3"));
                    tooltip.add(Component.translatable("tooltip.sporeadd.power5.harvest_line4"));

                    tooltip.add(Component.literal(""));
                    tooltip.add(Component.translatable("tooltip.sporeadd.power5.feeding_header"));
                    tooltip.add(Component.translatable("tooltip.sporeadd.power5.feeding_line1"));
                    tooltip.add(Component.translatable("tooltip.sporeadd.power5.feeding_line2"));
                    tooltip.add(Component.translatable("tooltip.sporeadd.power5.feeding_cost"));
                    tooltip.add(Component.translatable("tooltip.sporeadd.power5.feeding_supported"));
                }
                break;
            case 6:
                tooltip.add(Component.translatable("tooltip.sporeadd.power6.title"));

                if (mc.player != null) {
                    boolean isActive = false;
                    if (Switches != null && Switches.length() > 6) {
                        isActive = Switches.charAt(6) == '1';
                    }
                    String statusStr = isActive ? "§a[ON]" : "§c[OFF]";
                    tooltip.add(Component.translatable("tooltip.sporeadd.generic.status", statusStr));

                    tooltip.add(Component.literal(""));
                    tooltip.add(Component.translatable("tooltip.sporeadd.generic.active_execution"));
                    tooltip.add(Component.translatable("tooltip.sporeadd.power6.active_desc1"));
                    tooltip.add(Component.translatable("tooltip.sporeadd.power6.active_desc2"));

                    tooltip.add(Component.literal(""));
                    tooltip.add(Component.translatable("tooltip.sporeadd.power6.costs_header"));
                    tooltip.add(Component.translatable("tooltip.sporeadd.power6.cost_max"));
                    tooltip.add(Component.translatable("tooltip.sporeadd.power6.cost_discount1"));
                    tooltip.add(Component.translatable("tooltip.sporeadd.power6.cost_discount2"));

                    PlayerLevelProvider.PLAYER_LVL.get(mc.player).ifPresent(levelCap -> {
                        int rawLevel = levelCap.getLevel();
                        int sporeLevel = Math.max(rawLevel, 0);

                        int effectDuration = 20 + sporeLevel * 3;
                        int effectPower = Math.min(5, (int) Math.floor(sporeLevel / 2.0));
                        int amplifier = Math.max(effectPower - 1, 0);
                        int extraHealth = 10 * rawLevel;

                        String strengthRoman = (amplifier == 0) ? "I"
                                : (amplifier == 1) ? "II"
                                : (amplifier == 2) ? "III"
                                : (amplifier == 3) ? "IV"
                                : "V";

                        tooltip.add(Component.literal(""));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power6.buffs_header", rawLevel));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power6.buff_points"));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power6.buff_hp", extraHealth));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power6.buff_res", "I", effectDuration));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power6.buff_str", strengthRoman, effectDuration));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power6.buff_spd", effectDuration));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power6.buff_glow", effectDuration));
                    });

                    tooltip.add(Component.literal(""));
                    tooltip.add(Component.translatable("tooltip.sporeadd.power6.note1"));
                    tooltip.add(Component.translatable("tooltip.sporeadd.power6.note2"));
                }
                break;

            case 7:
                tooltip.add(Component.translatable("tooltip.sporeadd.power7.title"));

                if (mc.player != null) {
                    boolean isActive = false;
                    if (Switches != null && Switches.length() > 7) {
                        isActive = Switches.charAt(7) == '1';
                    }
                    String statusStr = isActive ? "§a[ON]" : "§c[OFF]";
                    tooltip.add(Component.translatable("tooltip.sporeadd.generic.status", statusStr));

                    tooltip.add(Component.literal(""));
                    tooltip.add(Component.translatable("tooltip.sporeadd.generic.active_execution"));
                    tooltip.add(Component.translatable("tooltip.sporeadd.power7.active_desc1"));
                    tooltip.add(Component.translatable("tooltip.sporeadd.power7.active_desc2"));

                    tooltip.add(Component.literal(""));
                    tooltip.add(Component.translatable("tooltip.sporeadd.power7.controls_header"));
                    tooltip.add(Component.translatable("tooltip.sporeadd.power7.control_wasd"));
                    tooltip.add(Component.translatable("tooltip.sporeadd.power7.control_space"));
                    tooltip.add(Component.translatable("tooltip.sporeadd.power7.control_ctrl"));

                    tooltip.add(Component.literal(""));
                    tooltip.add(Component.translatable("tooltip.sporeadd.power7.synergy_header"));
                    tooltip.add(Component.translatable("tooltip.sporeadd.power7.synergy_res"));
                    tooltip.add(Component.translatable("tooltip.sporeadd.power7.synergy_aqua"));
                    tooltip.add(Component.translatable("tooltip.sporeadd.power7.synergy_fly"));
                }
                break;

            case 8:
                tooltip.add(Component.translatable("tooltip.sporeadd.power8.title"));

                if (mc.player != null) {
                    PlayerLevelProvider.PLAYER_LVL.get(mc.player).ifPresent(levelCap -> {
                        int curLevel = levelCap.getLevel();
                        int cost = 15;
                        String biomassColor = (Spore >= cost) ? "§a" : "§c";

                        tooltip.add(Component.translatable("tooltip.sporeadd.generic.biomass", biomassColor, Spore, cost));
                        tooltip.add(Component.literal(""));

                        if (curLevel < 4) {
                            tooltip.add(Component.translatable("tooltip.sporeadd.power8.req_level"));
                            return;
                        }

                        tooltip.add(Component.translatable("tooltip.sporeadd.power8.summoning_header"));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power8.summoning_desc1"));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power8.summoning_desc2"));
                        tooltip.add(Component.literal(""));

                        tooltip.add(Component.literal(""));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power8.transport_header"));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power8.transport_desc1"));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power8.transport_desc2"));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power8.transport_desc3"));

                        tooltip.add(Component.literal(""));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power8.note1"));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power8.note2"));

                        tooltip.add(Component.translatable("tooltip.sporeadd.generic.active_execution"));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power8.active_desc1"));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power8.active_desc2"));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power8.active_desc3"));

                        int hordeBiomass;
                        switch (curLevel) {
                            case 4 -> hordeBiomass = 7;
                            case 5 -> hordeBiomass = 10;
                            case 6 -> hordeBiomass = 12;
                            case 7 -> hordeBiomass = 15;
                            case 8 -> hordeBiomass = 18;
                            case 9 -> hordeBiomass = 22;
                            default -> {
                                if (curLevel > 9) hordeBiomass = 25;
                                else hordeBiomass = 7;
                            }
                        }

                        tooltip.add(Component.literal(""));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power8.horde_header", curLevel));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power8.horde_verwas"));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power8.horde_budget", hordeBiomass));

                        tooltip.add(Component.literal(""));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power8.proto_header"));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power8.proto_adds"));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power8.proto_verwa"));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power8.proto_budget"));

                        tooltip.add(Component.literal(""));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power8.note1"));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power8.note2"));
                    });
                }
                break;
            case 9:
                tooltip.add(Component.translatable("tooltip.sporeadd.power9.title"));

                if (mc.player != null) {
                    PlayerLevelProvider.PLAYER_LVL.get(mc.player).ifPresent(levelCap -> {
                        int curLevel = levelCap.getLevel();
                        int cost = 5;
                        String biomassColor = (Spore >= cost) ? "§a" : "§c";

                        tooltip.add(Component.translatable("tooltip.sporeadd.generic.biomass", biomassColor, Spore, cost));
                        tooltip.add(Component.literal(""));

                        tooltip.add(Component.translatable("tooltip.sporeadd.generic.active_execution"));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power9.active_desc1"));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power9.active_desc2"));

                        int durationSeconds = 30 + Math.max(curLevel, 0) * 5;

                        tooltip.add(Component.literal(""));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power9.effects_header"));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power9.effect_spread"));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power9.effect_mycelium"));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power9.effect_exposed1"));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power9.effect_exposed2", durationSeconds));

                        tooltip.add(Component.literal(""));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power9.exposed_header"));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power9.exposed_desc1"));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power9.exposed_desc2"));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power9.exposed_desc3"));
                    });
                }
                break;

            case 10:
                tooltip.add(Component.translatable("tooltip.sporeadd.power10.title"));

                if (mc.player != null) {
                    int cost = 25;
                    String biomassColor = (Spore >= cost) ? "§a" : "§c";

                    tooltip.add(Component.translatable("tooltip.sporeadd.generic.biomass", biomassColor, Spore, cost));
                    tooltip.add(Component.literal(""));

                    tooltip.add(Component.translatable("tooltip.sporeadd.generic.active_execution"));
                    tooltip.add(Component.translatable("tooltip.sporeadd.power10.active_desc1"));
                    tooltip.add(Component.translatable("tooltip.sporeadd.power10.active_desc2"));
                    tooltip.add(Component.translatable("tooltip.sporeadd.power10.active_desc3"));

                    tooltip.add(Component.literal(""));
                    tooltip.add(Component.translatable("tooltip.sporeadd.power10.tracking_header"));
                    tooltip.add(Component.translatable("tooltip.sporeadd.power10.tracking_line1"));
                    tooltip.add(Component.translatable("tooltip.sporeadd.power10.tracking_line2"));
                    tooltip.add(Component.translatable("tooltip.sporeadd.power10.tracking_line3"));
                    tooltip.add(Component.translatable("tooltip.sporeadd.power10.tracking_line4"));

                    tooltip.add(Component.literal(""));
                    tooltip.add(Component.translatable("tooltip.sporeadd.power10.proto_header"));
                    tooltip.add(Component.translatable("tooltip.sporeadd.power10.proto_line1"));
                    tooltip.add(Component.translatable("tooltip.sporeadd.power10.proto_line2"));
                    tooltip.add(Component.translatable("tooltip.sporeadd.power10.proto_line3"));
                    tooltip.add(Component.translatable("tooltip.sporeadd.power10.proto_line4"));

                    tooltip.add(Component.literal(""));
                    tooltip.add(Component.translatable("tooltip.sporeadd.power10.warning1"));
                    tooltip.add(Component.translatable("tooltip.sporeadd.power10.warning2"));
                }
                break;

            case 11:
                tooltip.add(Component.translatable("tooltip.sporeadd.power11.title"));

                if (mc.player != null) {
                    PlayerLevelProvider.PLAYER_LVL.get(mc.player).ifPresent(levelCap -> {
                        int curLevel = levelCap.getLevel();
                        int reqLevel = 7;
                        int cost = 20;
                        String biomassColor = (Spore >= cost) ? "§a" : "§c";

                        tooltip.add(Component.translatable("tooltip.sporeadd.generic.biomass", biomassColor, Spore, cost));
                        tooltip.add(Component.literal(""));

                        if (curLevel < reqLevel) {
                            tooltip.add(Component.translatable("tooltip.sporeadd.generic.req_level", reqLevel));
                            return;
                        }

                        tooltip.add(Component.translatable("tooltip.sporeadd.generic.active_execution"));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power11.active_desc1"));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power11.active_desc2"));

                        int protoCount = 0;
                        if (mc.level != null) {
                            for (Entity entity : mc.level.entitiesForRendering()) {
                                if (entity.getType().builtInRegistryHolder().key().location().toString().equals("spore:proto")) {
                                    protoCount++;
                                }
                            }
                        }
                        int hp = 120 + (protoCount * 50);

                        tooltip.add(Component.literal(""));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power11.vigil_header"));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power11.vigil_hp", hp));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power11.vigil_jam"));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power11.vigil_res"));

                        tooltip.add(Component.literal(""));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power11.scan_header"));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power11.scan_init"));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power11.scan_grow"));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power11.scan_effect"));

                        tooltip.add(Component.literal(""));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power11.sabotage_header"));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power11.sabotage_line1"));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power11.sabotage_line2"));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power11.sabotage_line3"));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power11.sabotage_line4"));
                        tooltip.add(Component.translatable("tooltip.sporeadd.power11.sabotage_line5"));
                    });
                }
                break;
            case 12:
                tooltip.add(Component.translatable("tooltip.sporeadd.power12.title"));

                if (mc.player != null) {
                    int cost = 30;
                    String biomassColor = (Spore >= cost) ? "§a" : "§c";

                    tooltip.add(Component.translatable("tooltip.sporeadd.generic.biomass", biomassColor, Spore, cost));
                    tooltip.add(Component.literal(""));

                    tooltip.add(Component.translatable("tooltip.sporeadd.generic.active_execution"));
                    tooltip.add(Component.translatable("tooltip.sporeadd.power12.active_desc1"));
                    tooltip.add(Component.translatable("tooltip.sporeadd.power12.active_desc2"));

                    tooltip.add(Component.literal(""));
                    tooltip.add(Component.translatable("tooltip.sporeadd.power12.exo_header"));
                    tooltip.add(Component.translatable("tooltip.sporeadd.power12.exo_hp"));
                    tooltip.add(Component.translatable("tooltip.sporeadd.power12.exo_absorb1"));
                    tooltip.add(Component.translatable("tooltip.sporeadd.power12.exo_absorb2"));
                    tooltip.add(Component.translatable("tooltip.sporeadd.power12.exo_kb1"));
                    tooltip.add(Component.translatable("tooltip.sporeadd.power12.exo_kb2"));

                    tooltip.add(Component.literal(""));
                    tooltip.add(Component.translatable("tooltip.sporeadd.power12.regen_header"));
                    tooltip.add(Component.translatable("tooltip.sporeadd.power12.regen_rate"));
                    tooltip.add(Component.translatable("tooltip.sporeadd.power12.regen_res"));
                    tooltip.add(Component.translatable("tooltip.sporeadd.power12.regen_heal"));

                    tooltip.add(Component.literal(""));
                    tooltip.add(Component.translatable("tooltip.sporeadd.power12.warning1"));
                    tooltip.add(Component.translatable("tooltip.sporeadd.power12.warning2"));
                }
                break;

            case 13:
                tooltip.add(Component.translatable("tooltip.sporeadd.power13.title"));

                if (mc.player != null) {
                    int cost = 20;
                    String biomassColor = (Spore >= cost) ? "§a" : "§c";

                    tooltip.add(Component.translatable("tooltip.sporeadd.generic.biomass", biomassColor, Spore, cost));
                    tooltip.add(Component.literal(""));

                    tooltip.add(Component.translatable("tooltip.sporeadd.generic.active_execution"));
                    tooltip.add(Component.translatable("tooltip.sporeadd.power13.active_desc1"));
                    tooltip.add(Component.translatable("tooltip.sporeadd.power13.active_desc2"));

                    tooltip.add(Component.literal(""));
                    tooltip.add(Component.translatable("tooltip.sporeadd.power13.detonation_header"));
                    tooltip.add(Component.translatable("tooltip.sporeadd.power13.detonation_spawn"));
                    tooltip.add(Component.translatable("tooltip.sporeadd.power13.detonation_res1"));
                    tooltip.add(Component.translatable("tooltip.sporeadd.power13.detonation_res2"));
                    tooltip.add(Component.translatable("tooltip.sporeadd.power13.detonation_explode"));

                    tooltip.add(Component.literal(""));
                    tooltip.add(Component.translatable("tooltip.sporeadd.power13.effects_header"));
                    tooltip.add(Component.translatable("tooltip.sporeadd.power13.effects_dmg"));
                    tooltip.add(Component.translatable("tooltip.sporeadd.power13.effects_cloud"));
                    tooltip.add(Component.translatable("tooltip.sporeadd.power13.effects_inflict1"));
                    tooltip.add(Component.translatable("tooltip.sporeadd.power13.effects_inflict2"));

                    tooltip.add(Component.literal(""));
                    tooltip.add(Component.translatable("tooltip.sporeadd.power13.termina_header"));
                    tooltip.add(Component.translatable("tooltip.sporeadd.power13.termina_duration1"));
                    tooltip.add(Component.translatable("tooltip.sporeadd.power13.termina_duration2"));
                    tooltip.add(Component.translatable("tooltip.sporeadd.power13.termina_hp"));
                    tooltip.add(Component.translatable("tooltip.sporeadd.power13.termina_dmg1"));
                    tooltip.add(Component.translatable("tooltip.sporeadd.power13.termina_dmg2"));
                    tooltip.add(Component.translatable("tooltip.sporeadd.power13.termina_kill1"));
                    tooltip.add(Component.translatable("tooltip.sporeadd.power13.termina_kill2"));

                    tooltip.add(Component.literal(""));
                    tooltip.add(Component.translatable("tooltip.sporeadd.power13.corruption_header"));
                    tooltip.add(Component.translatable("tooltip.sporeadd.power13.corruption_line1"));
                    tooltip.add(Component.translatable("tooltip.sporeadd.power13.corruption_line2"));

                    if (mc.level != null) {
                        int levelsToLose = SporeAddsConfig.NUKE_LEVEL_PENALTY.get();
                        if (levelsToLose > 0) {
                            tooltip.add(Component.literal(""));
                            tooltip.add(Component.translatable("tooltip.sporeadd.power13.sacrifice_line1"));
                            tooltip.add(Component.translatable("tooltip.sporeadd.power13.sacrifice_line2", levelsToLose));
                        }
                    }
                }
                break;

            default:
                tooltip.add(Component.translatable("tooltip.sporeadd.generic.unknown_skill"));
                break;
        }

        return tooltip;
    }

    private String getEvolutionCostString(int levelTarget) {
        switch (levelTarget) {
            case 1: return com.sporeadds.sporeaddsmod.config.SporeAddsConfig.EVOLUTION_COST_1.get();
            case 2: return com.sporeadds.sporeaddsmod.config.SporeAddsConfig.EVOLUTION_COST_2.get();
            case 3: return com.sporeadds.sporeaddsmod.config.SporeAddsConfig.EVOLUTION_COST_3.get();
            case 4: return com.sporeadds.sporeaddsmod.config.SporeAddsConfig.EVOLUTION_COST_4.get();
            case 5: return com.sporeadds.sporeaddsmod.config.SporeAddsConfig.EVOLUTION_COST_5.get();
            case 6: return com.sporeadds.sporeaddsmod.config.SporeAddsConfig.EVOLUTION_COST_6.get();
            case 7: return com.sporeadds.sporeaddsmod.config.SporeAddsConfig.EVOLUTION_COST_7.get();
            case 8: return com.sporeadds.sporeaddsmod.config.SporeAddsConfig.EVOLUTION_COST_8.get();
            case 9: return com.sporeadds.sporeaddsmod.config.SporeAddsConfig.EVOLUTION_COST_9.get();
            default: return "() cost:50";
        }
    }

    private int countItemInInventory(String itemIdStr) {
        if (mc.player == null) return 0;

        int count = 0;
        for (int i = 0; i < mc.player.getInventory().getContainerSize(); i++) {
            net.minecraft.world.item.ItemStack stack = mc.player.getInventory().getItem(i);
            if (!stack.isEmpty()) {
                ResourceLocation key = BuiltInRegistries.ITEM.getKey(stack.getItem());
                if (key != null && key.toString().equals(itemIdStr)) {
                    count += stack.getCount();
                }
            }
        }
        return count;
    }

    /** The vanilla blur would be drawn over everything these screens paint before super.render(). */
    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderTransparentBackground(guiGraphics);

        float currentGuiScale = (float) mc.getWindow().getGuiScale();

        // --- INICIO ESCALA ---
        guiGraphics.pose().pushPose();
        float finalScale = FIXED_SCALE / currentGuiScale;
        guiGraphics.pose().scale(finalScale, finalScale, 1.0f);

        // Ratón corregido para la escala
        int mX = (int) (mouseX * (currentGuiScale / FIXED_SCALE));
        int mY = (int) (mouseY * (currentGuiScale / FIXED_SCALE));

        // Obtenemos la subclase del jugador para elegir la textura base
        String subclass = "none";
        if (mc.player != null) {
            var cap = com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider.SPORE_IDENTIFIER.get(mc.player);
            if (cap.isPresent()) {
                subclass = cap.orElseThrow(IllegalStateException::new).getSubclass();
            }
        }

        String texturePath;
        if ("abyssal".equalsIgnoreCase(subclass)) {
            texturePath = "textures/gui/ability_selector_abyssal.png";
        } else if ("caustic".equalsIgnoreCase(subclass)) {
            texturePath = "textures/gui/ability_selector_caustic.png";
        } else if ("gluttonous".equalsIgnoreCase(subclass)) {
            texturePath = "textures/gui/ability_selector_gluttonous.png";
        } else {
            texturePath = "textures/gui/ability_selector.png";
        }

        final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("sporeadd", texturePath);

        /// Fondo
        RenderSystem.setShaderTexture(0, TEXTURE);
        guiGraphics.blit(TEXTURE, x, y, 0, 0, spriteW, spriteH, 256 * 2, 256 * 2 );

// Icono de subclase
        KommandantSubclassIconRenderer.render(guiGraphics, x + 152 * 2 - 2, y + 170 * 2 - 2, 2.0F);

        KommandantSubclassIconRenderer.renderTooltip(
                guiGraphics,
                x + 152 * 2 - 2,
                y + 170 * 2 - 2,
                mouseX,
                mouseY,
                2.0F
        );
// Datos de jugador
        assert mc.player != null;
        PlayerSporeProvider.PLAYER_CAP.get(mc.player).ifPresent(cap -> {
            Spore = cap.getSpore();
        });

        AbilitySelectorRenderer.renderSporeValue(guiGraphics, mc, x, y, Spore);

        // Detección de Hover
        AbilityHovered = 0;
        for (AbilityButtonArea button : abilities) {
            if (button.isMouseOver(mX, mY)) {
                AbilityHovered = button.getAbilityNumber();
                break;
            }
        }

        // Switches de habilidades activas
        for (int i = 0; i < Sporelvl; i++) {
            if (i <= 7 && Switches.charAt(i + 1) == '1') {
                int col = i % 4;
                int row = i / 4;
                int XA = x + 17 * 2 + (col * (38 * 2 + 6));
                int YA = y + 35 * 2 + (row * (38 * 2 + 6));
                guiGraphics.blit(TEXTURE, XA, YA, 218 * 2, 64 * 2, 38 * 2, 38 * 2, 256 * 2, 256 * 2);
            }
        }

        // Overlays de Hover
        if (AbilityHovered != 0 && AbilityHovered != 13) {
            int col = (AbilityHovered - 1) % 4;
            int row = (AbilityHovered - 1) / 4;
            int XA = x + 17 * 2 + (col * (38 * 2 + 6));
            int YA = y + 35 * 2 + (row * (38 * 2 + 6));

            if (AbilityHovered <= 7) {
                int texY = (Switches.charAt(AbilityHovered) == '1') ? 25 * 2 : 103 * 2;
                guiGraphics.blit(TEXTURE, XA, YA, 218 * 2, texY, 38 * 2, 38 * 2, 256 * 2, 256 * 2);
            } else {
                guiGraphics.blit(TEXTURE, XA, YA, 218 * 2, 103 * 2, 38 * 2, 38 * 2, 256 * 2, 256 * 2);
            }
        } else if (AbilityHovered == 13) {
            guiGraphics.blit(TEXTURE, x + 77 * 2, y + 159 * 2, 0, 217 * 2, 40 * 2, 39 * 2, 256 * 2, 256 * 2);
        }

        // Habilidades bloqueadas
        int locked = Math.max(0, 12 - Sporelvl);
        for (int i = 0; i < 9; i++) {
            if (i < locked) {
                int col = i % 4;
                int row = i / 4;
                int XL = x + 140 * 2 - (col * (38 * 2 + 6));
                int YL = y + 117 * 2 - (row * (38 * 2 + 6));
                guiGraphics.blit(TEXTURE, XL, YL, 218 * 2, 218 * 2, 38 * 2, 38 * 2, 256 * 2, 256 * 2);
            }
        }

        // Botón 13 bloqueado
        if (Sporelvl != 13) {
            guiGraphics.blit(TEXTURE, x + 77 * 2, y + 159 * 2, 82 * 2, 217 * 2, 40 * 2, 39 * 2, 256 * 2, 256 * 2);
        }

        // Render widgets (botones)
        super.render(guiGraphics, mX, mY, partialTicks);

        // Tooltip
        if (AbilityHovered != 0) {
            List<Component> tooltipLines = getAbilityDescription(AbilityHovered);
            guiGraphics.renderTooltip(this.font, tooltipLines, Optional.empty(), mX, mY);
        }

        guiGraphics.pose().popPose();
        // --- FIN ESCALA ---
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        float currentGuiScale = (float) mc.getWindow().getGuiScale();
        double correctedX = mouseX * (currentGuiScale / FIXED_SCALE);
        double correctedY = mouseY * (currentGuiScale / FIXED_SCALE);
        return super.mouseClicked(correctedX, correctedY, button);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}