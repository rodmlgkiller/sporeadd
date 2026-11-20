package com.sporeadds.sporeaddsmod.client.renderer;

import net.minecraft.core.registries.BuiltInRegistries;

import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider;
import com.sporeadds.sporeaddsmod.config.KommandantPowersConfig;
import com.sporeadds.sporeaddsmod.config.MedicConfig;
import com.sporeadds.sporeaddsmod.config.SporeAddsConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class KommandantESPClient {

    // The Kommandant colours and ranges are kommandant_abilities.highlights (read from this client's own config file).
    private static final int COLOR_MEDIC_HEALTHY = 0x90EE90;
    private static final int COLOR_MEDIC_MEDIUM = 0xFFA500;
    private static final int COLOR_MEDIC_CRITICAL = 0xFF6666;
    private static final Map<Integer, SyncedEffectState> SYNCED_EFFECTS = new ConcurrentHashMap<>();

    /** Entities allowed to glow this tick when highlights.entity_limit is set (the nearest ones). */
    private static final Set<Integer> LIMITED_GLOW = new HashSet<>();
    private static long limitedGlowTick = Long.MIN_VALUE;

    private KommandantESPClient() {
    }

    private static int colorMarker() {
        return KommandantPowersConfig.colorRgb(KommandantPowersConfig.ESP_COLOR_ENTITY_MARKER, 0xFF7373);
    }

    private static int colorUneasy() {
        return KommandantPowersConfig.colorRgb(KommandantPowersConfig.ESP_COLOR_ENTITY_UNEASY, 0xA865C9);
    }

    private static int colorWater() {
        return KommandantPowersConfig.colorRgb(KommandantPowersConfig.ESP_COLOR_ENTITY_WATER, 0x00FFFF);
    }

    private static int colorSeasoned() {
        return KommandantPowersConfig.colorRgb(KommandantPowersConfig.ESP_COLOR_ENTITY_SEASONED, 0xAA0000);
    }

    private static int colorFallback() {
        return KommandantPowersConfig.colorRgb(KommandantPowersConfig.ESP_COLOR_ENTITY_FALLBACK, 0x00FF00);
    }

    private static double squared(double value) {
        return value * value;
    }

    public static boolean shouldGlow(Entity entity) {
        if (!shouldGlowUnlimited(entity)) {
            return false;
        }

        int limit = KommandantPowersConfig.ESP_ENTITY_LIMIT.get();
        if (limit <= 0) {
            return true;
        }

        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null || !isKommandant(client.player)) {
            return true;
        }

        // highlights.entity_limit: only the nearest `limit` entities glow; the choice is made once per game tick.
        long tick = client.level.getGameTime();
        if (tick != limitedGlowTick) {
            limitedGlowTick = tick;
            LIMITED_GLOW.clear();

            List<Entity> candidates = new ArrayList<>();
            for (Entity other : client.level.entitiesForRendering()) {
                if (shouldGlowUnlimited(other)) {
                    candidates.add(other);
                }
            }

            Player viewer = client.player;
            candidates.sort(Comparator.comparingDouble((Entity e) -> viewer.distanceToSqr(e)));
            for (int i = 0; i < Math.min(limit, candidates.size()); i++) {
                LIMITED_GLOW.add(candidates.get(i).getId());
            }
        }

        return LIMITED_GLOW.contains(entity.getId());
    }

    private static boolean shouldGlowUnlimited(Entity entity) {
        if (entity == null) {
            return false;
        }

        Minecraft client = Minecraft.getInstance();
        if (client == null || client.player == null || client.level == null) {
            return false;
        }

        Player viewer = client.player;
        if (entity == viewer) {
            return false;
        }

        if (getMedicHealthColor(entity, viewer) != null) {
            return true;
        }

        if (!isKommandant(viewer)) {
            return false;
        }

        if (entity instanceof Player targetPlayer && isKommandant(targetPlayer)) {
            return false;
        }

        SyncedEffectState state = SYNCED_EFFECTS.get(entity.getId());
        boolean seasonedReveal = false;

        if (state != null) {
            double distanceSqr = viewer.distanceToSqr(entity);
            seasonedReveal = state.hasSeasoned()
                    && distanceSqr <= squared(KommandantPowersConfig.ESP_SEASONED_RANGE.get());
        }

        if (seasonedReveal) {
            return true;
        }

        if (!isAllowedEspTarget(entity)) {
            return false;
        }

        boolean waterReveal = false;
        if (entity instanceof Player targetPlayer) {
            waterReveal = isAbyssal(viewer)
                    && targetPlayer.isInWater()
                    && targetPlayer.distanceToSqr(viewer) <= squared(KommandantPowersConfig.ESP_WATER_RANGE.get());
        }

        boolean uneasyReveal = false;
        boolean markerReveal = false;

        if (state != null) {
            double distanceSqr = viewer.distanceToSqr(entity);

            uneasyReveal = state.hasUneasy()
                    && distanceSqr <= squared(KommandantPowersConfig.ESP_UNEASY_RANGE.get());

            if (state.hasMarker()) {
                double markerRange = KommandantPowersConfig.ESP_MARKER_BASE_RANGE.get()
                        + (KommandantPowersConfig.ESP_MARKER_RANGE_PER_AMPLIFIER.get() * state.markerAmplifier());
                double markerRangeSqr = markerRange * markerRange;
                markerReveal = distanceSqr <= markerRangeSqr;
            }
        }

        return uneasyReveal || markerReveal || waterReveal;
    }

    private static Integer getMedicHealthColor(Entity entity, Player viewer) {
        if (!isMedic(viewer)) {
            return null;
        }

        if (!(entity instanceof net.minecraft.world.entity.LivingEntity living)) {
            return null;
        }

        if (living instanceof Player targetPlayer && isMedic(targetPlayer)) {
            return null;
        }

        // Triage range and health thresholds: medic.triage.* (read from this client's own config file).
        double triageRange = MedicConfig.triageRange();
        double distanceSqr = viewer.distanceToSqr(living);
        if (distanceSqr > triageRange * triageRange) {
            return null;
        }

        float maxHealth = living.getMaxHealth();
        if (maxHealth <= 0.0F) {
            return null;
        }

        float healthPct = living.getHealth() / maxHealth;

        float showBelow = MedicConfig.triageShowBelow();
        float mediumBelow = MedicConfig.triageMediumBelow();
        float criticalBelow = MedicConfig.triageCriticalBelow();

        if (healthPct <= showBelow && healthPct >= mediumBelow) {
            return COLOR_MEDIC_HEALTHY;
        } else if (healthPct < mediumBelow && healthPct >= criticalBelow) {
            return COLOR_MEDIC_MEDIUM;
        } else if (healthPct < criticalBelow) {
            return COLOR_MEDIC_CRITICAL;
        }

        return null;
    }

    public static int getOutlineColor(Entity entity) {
        if (entity == null) {
            return colorFallback();
        }

        Minecraft client = Minecraft.getInstance();
        if (client == null || client.player == null || client.level == null) {
            return colorFallback();
        }

        Player viewer = client.player;
        if (entity == viewer) {
            return colorFallback();
        }

        Integer medicColor = getMedicHealthColor(entity, viewer);
        if (medicColor != null) {
            return medicColor;
        }

        if (!isKommandant(viewer)) {
            return colorFallback();
        }

        if (entity instanceof Player targetPlayer && isKommandant(targetPlayer)) {
            return colorFallback();
        }

        SyncedEffectState state = SYNCED_EFFECTS.get(entity.getId());
        boolean seasonedReveal = false;

        if (state != null) {
            double distanceSqr = viewer.distanceToSqr(entity);
            seasonedReveal = state.hasSeasoned()
                    && distanceSqr <= squared(KommandantPowersConfig.ESP_SEASONED_RANGE.get());
        }

        if (seasonedReveal) {
            return colorSeasoned();
        }

        if (!isAllowedEspTarget(entity)) {
            return colorFallback();
        }

        boolean waterReveal = false;
        if (entity instanceof Player targetPlayer) {
            waterReveal = isAbyssal(viewer)
                    && targetPlayer.isInWater()
                    && targetPlayer.distanceToSqr(viewer) <= squared(KommandantPowersConfig.ESP_WATER_RANGE.get());
        }

        boolean uneasyReveal = false;
        boolean markerReveal = false;

        if (state != null) {
            double distanceSqr = viewer.distanceToSqr(entity);

            uneasyReveal = state.hasUneasy()
                    && distanceSqr <= squared(KommandantPowersConfig.ESP_UNEASY_RANGE.get());

            if (state.hasMarker()) {
                double markerRange = KommandantPowersConfig.ESP_MARKER_BASE_RANGE.get()
                        + (KommandantPowersConfig.ESP_MARKER_RANGE_PER_AMPLIFIER.get() * state.markerAmplifier());
                double markerRangeSqr = markerRange * markerRange;
                markerReveal = distanceSqr <= markerRangeSqr;
            }
        }

        if (uneasyReveal) {
            return colorUneasy();
        }

        if (markerReveal) {
            return colorMarker();
        }

        if (waterReveal) {
            return colorWater();
        }

        return colorFallback();
    }

    public static void updatePlayerEffects(int entityId, boolean hasMarker, int markerAmplifier, boolean hasUneasy, boolean hasSeasoned) {
        SYNCED_EFFECTS.put(entityId, new SyncedEffectState(hasMarker, markerAmplifier, hasUneasy, hasSeasoned));
    }

    public static void removePlayerEffects(int entityId) {
        SYNCED_EFFECTS.remove(entityId);
    }

    public static void clear() {
        SYNCED_EFFECTS.clear();
    }

    private static boolean isAllowedEspTarget(Entity entity) {
        List<? extends String> configured = SporeAddsConfig.KOMMANDANT_ESP_ENTITIES.get();
        if (configured == null || configured.isEmpty()) {
            return entity instanceof Player;
        }

        ResourceLocation entityKey = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        if (entityKey == null) {
            return false;
        }

        String entityId = entityKey.toString();

        for (String entry : configured) {
            if (entry != null && entityId.equals(entry.trim())) {
                return true;
            }
        }

        return false;
    }

    private static boolean isKommandant(Player player) {
        return SporeIdentifierProvider.SPORE_IDENTIFIER.get(player)
                .map(data -> "kommandant".equalsIgnoreCase(data.getIdentifier()))
                .orElse(false);
    }

    private static boolean isAbyssal(Player player) {
        return SporeIdentifierProvider.SPORE_IDENTIFIER.get(player)
                .map(data ->
                        "kommandant".equalsIgnoreCase(data.getIdentifier()) &&
                                "abyssal".equalsIgnoreCase(data.getSubclass()))
                .orElse(false);
    }

    private static boolean isMedic(Player player) {
        return SporeIdentifierProvider.SPORE_IDENTIFIER.get(player)
                .map(data -> "medic".equalsIgnoreCase(data.getIdentifier()))
                .orElse(false);
    }

    public record SyncedEffectState(boolean hasMarker, int markerAmplifier, boolean hasUneasy, boolean hasSeasoned) {
    }
}