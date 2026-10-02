package com.sporeadds.sporeaddsmod.Powers;

import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import net.neoforged.fml.common.EventBusSubscriber;

import net.minecraft.core.registries.BuiltInRegistries;

import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider;
import com.sporeadds.sporeaddsmod.config.SporeAddsConfig;
import com.sporeadds.sporeaddsmod.level.PlayerLevelProvider;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import virtuoel.pehkui.api.ScaleType;
import virtuoel.pehkui.api.ScaleTypes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@EventBusSubscriber
public class Levelstats {

    private static final HashMap<UUID, Integer> lastLevelMap = new HashMap<>();
    private static final HashMap<UUID, Float> lastHealthMap = new HashMap<>();
    private static final HashMap<UUID, Float> lastExhaustionMap = new HashMap<>();
    private static final HashMap<UUID, Integer> lastFoodMap = new HashMap<>();
    private static final HashMap<UUID, Float> lastSatMap = new HashMap<>();

    private static final ResourceLocation SWIM_SPEED_ID = ResourceLocation.fromNamespaceAndPath("forge", "swim_speed");

    private static boolean isAbyssal(ServerPlayer player) {
        return SporeIdentifierProvider.SPORE_IDENTIFIER.get(player)
                .map(data -> "abyssal".equalsIgnoreCase(data.getSubclass()))
                .orElse(false);
    }

    private static AttributeInstance getSwimSpeedAttribute(ServerPlayer player) {
        return player.getAttribute(BuiltInRegistries.ATTRIBUTE.get(SWIM_SPEED_ID));
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity().level().isClientSide) return;
        if (!(event.getEntity() instanceof ServerPlayer tickPlayer)) return;

        if (!hasKommandantClass(tickPlayer)) {
            if (lastLevelMap.containsKey(tickPlayer.getUUID())) {
                resetToBaseStats(tickPlayer);
            }
            return;
        }

        PlayerLevelProvider.PLAYER_LVL.get(tickPlayer).ifPresent(level -> {
            int currentLevel = level.getLevel();
            int lastLevel = lastLevelMap.getOrDefault(tickPlayer.getUUID(), -1);

            if (currentLevel != lastLevel) {
                applyStatsForLevel(tickPlayer, currentLevel);
                lastLevelMap.put(tickPlayer.getUUID(), currentLevel);
            }

            Stats stats = getStatsForLevel(tickPlayer, currentLevel);
            UUID uuid = tickPlayer.getUUID();

            float currentHealth = tickPlayer.getHealth();
            float currentExhaustion = tickPlayer.getFoodData().getExhaustionLevel();
            int currentFood = tickPlayer.getFoodData().getFoodLevel();
            float currentSat = tickPlayer.getFoodData().getSaturationLevel();

            float lastHealth = lastHealthMap.getOrDefault(uuid, currentHealth);
            float lastExhaustion = lastExhaustionMap.getOrDefault(uuid, currentExhaustion);
            int lastFood = lastFoodMap.getOrDefault(uuid, currentFood);
            float lastSat = lastSatMap.getOrDefault(uuid, currentSat);

            boolean healed = currentHealth > lastHealth;

            float foodLost = Math.max(0, lastFood - currentFood);
            float satLost = Math.max(0, lastSat - currentSat);
            float exhaustionConsumed = (foodLost + satLost) * 4.0f;

            float exhaustionGained = (currentExhaustion - lastExhaustion) + exhaustionConsumed;

            if (exhaustionGained > 0.01f) {
                float multiplier = stats.hungerMultiplier;

                if (healed) {
                    if (currentLevel <= 1) {
                        multiplier = 0.0f;
                    } else if (currentLevel <= 3) {
                        multiplier = 0.25f;
                    } else {
                        multiplier = 0.5f;
                    }
                }

                if (multiplier <= 0.0f) {
                    float correctExhaustion = currentExhaustion - exhaustionGained;
                    tickPlayer.getFoodData().setExhaustion(Math.max(0, correctExhaustion));
                } else {
                    float extraExhaustion = exhaustionGained * (multiplier - 1.0f);

                    if (extraExhaustion > 0) {
                        tickPlayer.getFoodData().addExhaustion(extraExhaustion);
                    } else if (extraExhaustion < 0) {
                        float correctExhaustion = currentExhaustion + extraExhaustion;
                        tickPlayer.getFoodData().setExhaustion(Math.max(0, correctExhaustion));
                    }
                }
            }

            lastHealthMap.put(uuid, tickPlayer.getHealth());
            lastExhaustionMap.put(uuid, tickPlayer.getFoodData().getExhaustionLevel());
            lastFoodMap.put(uuid, tickPlayer.getFoodData().getFoodLevel());
            lastSatMap.put(uuid, tickPlayer.getFoodData().getSaturationLevel());
        });
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer respawnPlayer)) return;
        clearPlayerTrackers(respawnPlayer.getUUID());

        respawnPlayer.getServer().execute(() -> {
            if (!hasKommandantClass(respawnPlayer)) {
                resetToBaseStats(respawnPlayer);
                return;
            }
            PlayerLevelProvider.PLAYER_LVL.get(respawnPlayer).ifPresent(levelCap -> {
                int level = levelCap.getLevel();
                applyStatsForLevel(respawnPlayer, level);
                lastLevelMap.put(respawnPlayer.getUUID(), level);
            });
        });
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer loginPlayer)) return;
        clearPlayerTrackers(loginPlayer.getUUID());

        loginPlayer.getServer().execute(() -> {
            if (!hasKommandantClass(loginPlayer)) {
                resetToBaseStats(loginPlayer);
                return;
            }
            PlayerLevelProvider.PLAYER_LVL.get(loginPlayer).ifPresent(levelCap -> {
                int level = levelCap.getLevel();
                applyStatsForLevel(loginPlayer, level);
                lastLevelMap.put(loginPlayer.getUUID(), level);
            });
        });
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        clearPlayerTrackers(event.getEntity().getUUID());
    }

    private static void clearPlayerTrackers(UUID uuid) {
        lastLevelMap.remove(uuid);
        lastHealthMap.remove(uuid);
        lastExhaustionMap.remove(uuid);
        lastFoodMap.remove(uuid);
        lastSatMap.remove(uuid);
    }

    private static void forceSyncAttributes(ServerPlayer player, int level) {
        try {
            Stats stats = getStatsForLevel(player, level);

            AttributeInstance maxHearts = player.getAttribute(Attributes.MAX_HEALTH);
            if (maxHearts != null) maxHearts.setBaseValue(stats.hearts * 2.0D);

            AttributeInstance armorAttr = player.getAttribute(Attributes.ARMOR);
            if (armorAttr != null) armorAttr.setBaseValue(stats.armorPoints);

            AttributeInstance moveSpeed = player.getAttribute(Attributes.MOVEMENT_SPEED);
            if (moveSpeed != null) moveSpeed.setBaseValue(0.1D * stats.speedModifier);

            AttributeInstance baseDamage = player.getAttribute(Attributes.ATTACK_DAMAGE);
            if (baseDamage != null) baseDamage.setBaseValue(stats.damage);

            AttributeInstance swimSpeed = getSwimSpeedAttribute(player);
            if (swimSpeed != null) swimSpeed.setBaseValue(isAbyssal(player) ? 2.0D : 1.0D);

            applyPehkuiScales(player, level, stats);

            player.connection.send(new ClientboundUpdateAttributesPacket(
                    player.getId(),
                    player.getAttributes().getSyncableAttributes()
            ));

            float currentHealth = player.getHealth();
            float maxHealth = player.getMaxHealth();
            if (currentHealth > maxHealth) {
                player.setHealth(maxHealth);
            }

        } catch (Throwable e) {
        }
    }

    public static boolean hasKommandantClass(ServerPlayer player) {
        return SporeIdentifierProvider.SPORE_IDENTIFIER.get(player)
                .map(data -> "kommandant".equals(data.getIdentifier()))
                .orElse(false);
    }

    public static void applyStatsForLevel(ServerPlayer player, int level) {
        Stats stats = getStatsForLevel(player, level);

        AttributeInstance maxHearts = player.getAttribute(Attributes.MAX_HEALTH);
        if (maxHearts != null) maxHearts.setBaseValue(stats.hearts * 2.0D);

        AttributeInstance armorAttr = player.getAttribute(Attributes.ARMOR);
        if (armorAttr != null) armorAttr.setBaseValue(stats.armorPoints);

        AttributeInstance moveSpeed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (moveSpeed != null) moveSpeed.setBaseValue(0.1D * stats.speedModifier);

        AttributeInstance baseDamage = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (baseDamage != null) baseDamage.setBaseValue(stats.damage);

        AttributeInstance swimSpeed = getSwimSpeedAttribute(player);
        if (swimSpeed != null) swimSpeed.setBaseValue(isAbyssal(player) ? 2.0D : 1.0D);

        applyPehkuiScales(player, level, stats);

        player.getServer().execute(() -> {
            player.refreshDimensions();

            float currentHealth = player.getHealth();
            float maxHealth = player.getMaxHealth();
            if (currentHealth > maxHealth) {
                player.setHealth(maxHealth);
            }

            forceSyncAttributes(player, level);
        });
    }

    public static void forceReapplyStats(ServerPlayer player) {
        clearPlayerTrackers(player.getUUID());

        if (!hasKommandantClass(player)) {
            resetToBaseStats(player);
            return;
        }

        PlayerLevelProvider.PLAYER_LVL.get(player).ifPresent(levelCap -> {
            int level = levelCap.getLevel();
            applyStatsForLevel(player, level);
            lastLevelMap.put(player.getUUID(), level);
        });
    }

    /**
     * Equivalente en código al poder de Origins {@code sporeadd:originremovescale}
     * ("scale reset @s" al perder el origin de kommandant): devuelve todas las escalas
     * de Pehkui del jugador a su valor por defecto.
     */
    public static void resetPehkuiScales(ServerPlayer player) {
        if (!ModList.get().isLoaded("pehkui")) return;
        try {
            ScaleType[] types = {
                    ScaleTypes.BASE,
                    ScaleTypes.HEIGHT,
                    ScaleTypes.WIDTH,
                    ScaleTypes.MOTION,
                    ScaleTypes.REACH,
                    ScaleTypes.JUMP_HEIGHT,
                    ScaleTypes.ENTITY_REACH
            };
            for (ScaleType type : types) {
                type.getScaleData(player).resetScale();
            }
        } catch (Throwable ignored) {
        }
    }

    private static void applyPehkuiScales(ServerPlayer player, int level, Stats stats) {
        if (!ModList.get().isLoaded("pehkui")) return;

        try {
            ScaleType scaleType = getScaleType(stats.scaleType);
            if (scaleType != null) {
                scaleType.getScaleData(player).setScale(stats.scale);
            } else {
                ScaleTypes.BASE.getScaleData(player).setScale(stats.scale);
            }

            if (tableHasSmallScale(player) && (level == 0 || level == 1)) {
                ScaleTypes.JUMP_HEIGHT.getScaleData(player).setScale(1.3F);
                ScaleTypes.ENTITY_REACH.getScaleData(player).setScale(2.0F);
            } else {
                ScaleTypes.JUMP_HEIGHT.getScaleData(player).setScale(1.0F);
                ScaleTypes.ENTITY_REACH.getScaleData(player).setScale(1.0F);
            }
        } catch (Throwable e) {
        }
    }

    private static boolean tableHasSmallScale(ServerPlayer player) {
        Stats[] chosenTable = getStatsTable(player);

        for (Stats stats : chosenTable) {
            if (stats.scale <= 0.7F) {
                return true;
            }
        }

        return false;
    }

    private static ScaleType getScaleType(String type) {
        switch ((type != null ? type : "BASE").toUpperCase()) {
            case "BASE": return ScaleTypes.BASE;
            case "HEIGHT": return ScaleTypes.HEIGHT;
            case "WIDTH": return ScaleTypes.WIDTH;
            case "MOTION": return ScaleTypes.MOTION;
            case "REACH": return ScaleTypes.REACH;
            case "JUMP_HEIGHT": return ScaleTypes.JUMP_HEIGHT;
            case "ENTITY_REACH": return ScaleTypes.ENTITY_REACH;
            default: return null;
        }
    }

    public static class Stats {
        public int hearts;
        public int armorPoints;
        public float scale;
        public float speedModifier;
        public double damage;
        public float hungerMultiplier;
        public String scaleType;

        public Stats(int hearts, int armorPoints, float scale, float speedModifier, double damage, float hungerMultiplier, String scaleType) {
            this.hearts = hearts;
            this.armorPoints = armorPoints;
            this.scale = scale;
            this.speedModifier = speedModifier;
            this.damage = damage;
            this.hungerMultiplier = hungerMultiplier;
            this.scaleType = scaleType;
        }
    }

    public static final Stats[] LEVEL_STATS_DEFAULT = new Stats[] {
            new Stats(10, 0,  1.00F, 1.0F,  1.00D, 0.0F,  "BASE"),
            new Stats(11, 2,  1.06F, 1.01F, 1.22D, 0.05F, "BASE"),
            new Stats(12, 4,  1.11F, 1.01F, 1.44D, 0.07F, "BASE"),
            new Stats(13, 6,  1.17F, 1.02F, 1.67D, 0.10F, "BASE"),
            new Stats(14, 8,  1.22F, 1.03F, 1.89D, 0.13F, "BASE"),
            new Stats(16, 10, 1.28F, 1.03F, 2.11D, 0.16F, "BASE"),
            new Stats(17, 12, 1.33F, 1.04F, 2.33D, 0.20F, "BASE"),
            new Stats(18, 14, 1.39F, 1.05F, 2.56D, 0.25F, "BASE"),
            new Stats(19, 16, 1.44F, 1.06F, 2.78D, 0.30F, "BASE"),
            new Stats(20, 20, 1.50F, 1.07F, 3.00D, 0.35F, "BASE"),
    };

    public static final Stats[] LEVEL_STATS_GLUTTONOUS = new Stats[] {
            new Stats(10, 0,  1.00F, 1.0F,  1.00D, 0.0F,  "BASE"),
            new Stats(11, 3,  1.11F, 0.98F, 1.26D, 0.09F, "BASE"),
            new Stats(13, 5,  1.21F, 0.97F, 1.64D, 0.15F, "BASE"),
            new Stats(15, 7,  1.29F, 0.95F, 1.87D, 0.22F, "BASE"),
            new Stats(18, 10, 1.36F, 0.92F, 2.02D, 0.34F, "BASE"),
            new Stats(21, 12, 1.45F, 0.87F, 2.25D, 0.49F, "BASE"),
            new Stats(25, 15, 1.54F, 0.83F, 2.56D, 0.67F, "BASE"),
            new Stats(30, 19, 1.67F, 0.78F, 2.72D, 0.79F, "BASE"),
            new Stats(35, 24, 1.79F, 0.76F, 2.90D, 0.92F, "BASE"),
            new Stats(40, 28, 2.00F, 0.73F, 3.20D, 1.12F, "BASE"),
    };

    public static final Stats[] LEVEL_STATS_ABYSSAL = new Stats[] {
            new Stats(10, 0,  1.00F, 1.0F,  1.00D, 0.0F,  "BASE"),
            new Stats(11, 1,  1.05F, 1.01F, 1.14D, 0.06F, "BASE"),
            new Stats(11, 3,  1.10F, 1.01F, 1.29D, 0.09F, "BASE"),
            new Stats(12, 4,  1.15F, 1.02F, 1.43D, 0.12F, "BASE"),
            new Stats(13, 5,  1.20F, 1.03F, 1.58D, 0.15F, "BASE"),
            new Stats(13, 6,  1.25F, 1.03F, 1.72D, 0.20F, "BASE"),
            new Stats(14, 8,  1.30F, 1.04F, 1.87D, 0.25F, "BASE"),
            new Stats(15, 9,  1.35F, 1.05F, 2.01D, 0.30F, "BASE"),
            new Stats(15, 10, 1.40F, 1.06F, 2.16D, 0.35F, "BASE"),
            new Stats(16, 12, 1.45F, 1.07F, 2.30D, 0.40F, "BASE"),
    };

    public static final Stats[] LEVEL_STATS_CAUSTIC = new Stats[] {
            new Stats(10, 0,  1.00F, 1.0F,  1.00D, 0.0F,  "BASE"),
            new Stats(11, 1,  1.05F, 1.01F, 1.14D, 0.06F, "BASE"),
            new Stats(11, 3,  1.10F, 1.01F, 1.29D, 0.09F, "BASE"),
            new Stats(12, 4,  1.15F, 1.02F, 1.43D, 0.12F, "BASE"),
            new Stats(13, 5,  1.20F, 1.03F, 1.58D, 0.15F, "BASE"),
            new Stats(13, 6,  1.25F, 1.03F, 1.72D, 0.20F, "BASE"),
            new Stats(14, 8,  1.30F, 1.04F, 1.87D, 0.25F, "BASE"),
            new Stats(15, 9,  1.35F, 1.05F, 2.01D, 0.30F, "BASE"),
            new Stats(15, 10, 1.40F, 1.06F, 2.16D, 0.35F, "BASE"),
            new Stats(16, 12, 1.45F, 1.07F, 2.30D, 0.40F, "BASE"),
    };

    private static Stats[] parseStatsList(List<? extends String> rawList, Stats[] fallback) {
        if (rawList == null || rawList.isEmpty()) {
            return fallback;
        }

        List<Stats> parsed = new ArrayList<>();

        for (String line : rawList) {
            Stats stats = parseStatsLine(line);
            if (stats != null) {
                parsed.add(stats);
            }
        }

        if (parsed.isEmpty()) {
            return fallback;
        }

        return parsed.toArray(new Stats[0]);
    }

    private static Stats parseStatsLine(String line) {
        if (line == null || line.isBlank()) {
            return null;
        }

        String[] parts = line.split(";");
        if (parts.length < 7) {
            return null;
        }

        try {
            int hearts = Integer.parseInt(parts[0].trim());
            int armorPoints = Integer.parseInt(parts[1].trim());
            float scale = Float.parseFloat(parts[2].trim());
            float speedModifier = Float.parseFloat(parts[3].trim());
            double damage = Double.parseDouble(parts[4].trim());
            float hungerMultiplier = Float.parseFloat(parts[5].trim());
            String scaleType = parts[6].trim();

            return new Stats(hearts, armorPoints, scale, speedModifier, damage, hungerMultiplier, scaleType);
        } catch (Exception e) {
            return null;
        }
    }

    private static Stats[] getStatsTable(ServerPlayer player) {
        String subclass = SporeIdentifierProvider.SPORE_IDENTIFIER.get(player)
                .map(data -> data.getSubclass())
                .orElse("none");

        if ("abyssal".equalsIgnoreCase(subclass)) {
            return parseStatsList(
                    SporeAddsConfig.KOMMANDANT_LEVEL_STATS_ABYSSAL.get(),
                    LEVEL_STATS_ABYSSAL
            );
        }

        if ("caustic".equalsIgnoreCase(subclass)) {
            return parseStatsList(
                    SporeAddsConfig.KOMMANDANT_LEVEL_STATS_CAUSTIC.get(),
                    LEVEL_STATS_CAUSTIC
            );
        }

        if ("gluttonous".equalsIgnoreCase(subclass)) {
            return parseStatsList(
                    SporeAddsConfig.KOMMANDANT_LEVEL_STATS_GLUTTONOUS.get(),
                    LEVEL_STATS_GLUTTONOUS
            );
        }

        return parseStatsList(
                SporeAddsConfig.KOMMANDANT_LEVEL_STATS_DEFAULT.get(),
                LEVEL_STATS_DEFAULT
        );
    }

    public static Stats getStatsForLevel(ServerPlayer player, int level) {
        Stats[] chosenTable = getStatsTable(player);
        int idx = Math.max(0, Math.min(level, chosenTable.length - 1));
        return chosenTable[idx];
    }

    private static void resetToBaseStats(ServerPlayer player) {
        AttributeInstance maxHearts = player.getAttribute(Attributes.MAX_HEALTH);
        if (maxHearts != null) maxHearts.setBaseValue(20.0D);

        AttributeInstance armorAttr = player.getAttribute(Attributes.ARMOR);
        if (armorAttr != null) armorAttr.setBaseValue(0.0D);

        AttributeInstance moveSpeed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (moveSpeed != null) moveSpeed.setBaseValue(0.1D);

        AttributeInstance baseDamage = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (baseDamage != null) baseDamage.setBaseValue(1.0D);

        AttributeInstance swimSpeed = getSwimSpeedAttribute(player);
        if (swimSpeed != null) swimSpeed.setBaseValue(1.0D);

        if (ModList.get().isLoaded("pehkui")) {
            try {
                ScaleTypes.BASE.getScaleData(player).setScale(1.0F);
                ScaleTypes.JUMP_HEIGHT.getScaleData(player).setScale(1.0F);
                ScaleTypes.ENTITY_REACH.getScaleData(player).setScale(1.0F);
            } catch (Throwable e) {
            }
        }

        clearPlayerTrackers(player.getUUID());

        player.getServer().execute(() -> {
            player.refreshDimensions();

            float maxHealth = player.getMaxHealth();
            if (player.getHealth() > maxHealth) {
                player.setHealth(maxHealth);
            }

            player.connection.send(new ClientboundUpdateAttributesPacket(
                    player.getId(),
                    player.getAttributes().getSyncableAttributes()
            ));
        });
    }
}