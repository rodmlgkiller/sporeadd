package com.sporeadds.sporeaddsmod.client.renderer;

import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider;
import com.sporeadds.sporeaddsmod.config.SporeAddsConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class KommandantESPClient {

    private static final int COLOR_MARKER = 0xFF7373;
    private static final int COLOR_UNEASY = 0xA865C9;
    private static final int COLOR_WATER = 0x00FFFF;
    private static final int COLOR_SEASONED = 0xAA0000;
    private static final int COLOR_FALLBACK = 0x00FF00;
    private static final int COLOR_MEDIC_HEALTHY = 0x90EE90;
    private static final int COLOR_MEDIC_MEDIUM = 0xFFA500;
    private static final int COLOR_MEDIC_CRITICAL = 0xFF6666;

    private static final double MEDIC_RANGE = 32.0D;
    private static final double MEDIC_RANGE_SQR = MEDIC_RANGE * MEDIC_RANGE;
    private static final double UNEASY_RANGE = 64.0D;
    private static final double UNEASY_RANGE_SQR = UNEASY_RANGE * UNEASY_RANGE;

    private static final double WATER_PLAYER_RANGE = 64.0D;
    private static final double WATER_PLAYER_RANGE_SQR = WATER_PLAYER_RANGE * WATER_PLAYER_RANGE;

    private static final double SEASONED_RANGE = 42.0D;
    private static final double SEASONED_RANGE_SQR = SEASONED_RANGE * SEASONED_RANGE;

    private static final Map<Integer, SyncedEffectState> SYNCED_EFFECTS = new ConcurrentHashMap<>();

    private KommandantESPClient() {
    }

    public static boolean shouldGlow(Entity entity) {
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
            seasonedReveal = state.hasSeasoned() && distanceSqr <= SEASONED_RANGE_SQR;
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
                    && targetPlayer.distanceToSqr(viewer) <= WATER_PLAYER_RANGE_SQR;
        }

        boolean uneasyReveal = false;
        boolean markerReveal = false;

        if (state != null) {
            double distanceSqr = viewer.distanceToSqr(entity);

            uneasyReveal = state.hasUneasy() && distanceSqr <= UNEASY_RANGE_SQR;

            if (state.hasMarker()) {
                double markerRange = 24.0D + (12.0D * state.markerAmplifier());
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

        double distanceSqr = viewer.distanceToSqr(living);
        if (distanceSqr > MEDIC_RANGE_SQR) {
            return null;
        }

        float maxHealth = living.getMaxHealth();
        if (maxHealth <= 0.0F) {
            return null;
        }

        float healthPct = living.getHealth() / maxHealth;

        if (healthPct <= 0.90F && healthPct >= 0.60F) {
            return COLOR_MEDIC_HEALTHY;
        } else if (healthPct < 0.60F && healthPct >= 0.30F) {
            return COLOR_MEDIC_MEDIUM;
        } else if (healthPct < 0.30F) {
            return COLOR_MEDIC_CRITICAL;
        }

        return null;
    }

    public static int getOutlineColor(Entity entity) {
        if (entity == null) {
            return COLOR_FALLBACK;
        }

        Minecraft client = Minecraft.getInstance();
        if (client == null || client.player == null || client.level == null) {
            return COLOR_FALLBACK;
        }

        Player viewer = client.player;
        if (entity == viewer) {
            return COLOR_FALLBACK;
        }

        Integer medicColor = getMedicHealthColor(entity, viewer);
        if (medicColor != null) {
            return medicColor;
        }

        if (!isKommandant(viewer)) {
            return COLOR_FALLBACK;
        }

        if (entity instanceof Player targetPlayer && isKommandant(targetPlayer)) {
            return COLOR_FALLBACK;
        }

        SyncedEffectState state = SYNCED_EFFECTS.get(entity.getId());
        boolean seasonedReveal = false;

        if (state != null) {
            double distanceSqr = viewer.distanceToSqr(entity);
            seasonedReveal = state.hasSeasoned() && distanceSqr <= SEASONED_RANGE_SQR;
        }

        if (seasonedReveal) {
            return COLOR_SEASONED;
        }

        if (!isAllowedEspTarget(entity)) {
            return COLOR_FALLBACK;
        }

        boolean waterReveal = false;
        if (entity instanceof Player targetPlayer) {
            waterReveal = isAbyssal(viewer)
                    && targetPlayer.isInWater()
                    && targetPlayer.distanceToSqr(viewer) <= WATER_PLAYER_RANGE_SQR;
        }

        boolean uneasyReveal = false;
        boolean markerReveal = false;

        if (state != null) {
            double distanceSqr = viewer.distanceToSqr(entity);

            uneasyReveal = state.hasUneasy() && distanceSqr <= UNEASY_RANGE_SQR;

            if (state.hasMarker()) {
                double markerRange = 24.0D + (12.0D * state.markerAmplifier());
                double markerRangeSqr = markerRange * markerRange;
                markerReveal = distanceSqr <= markerRangeSqr;
            }
        }

        if (uneasyReveal) {
            return COLOR_UNEASY;
        }

        if (markerReveal) {
            return COLOR_MARKER;
        }

        if (waterReveal) {
            return COLOR_WATER;
        }

        return COLOR_FALLBACK;
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

        ResourceLocation entityKey = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
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
        return player.getCapability(SporeIdentifierProvider.SPORE_IDENTIFIER)
                .map(data -> "kommandant".equalsIgnoreCase(data.getIdentifier()))
                .orElse(false);
    }

    private static boolean isAbyssal(Player player) {
        return player.getCapability(SporeIdentifierProvider.SPORE_IDENTIFIER)
                .map(data ->
                        "kommandant".equalsIgnoreCase(data.getIdentifier()) &&
                                "abyssal".equalsIgnoreCase(data.getSubclass()))
                .orElse(false);
    }

    private static boolean isMedic(Player player) {
        return player.getCapability(SporeIdentifierProvider.SPORE_IDENTIFIER)
                .map(data -> "medic".equalsIgnoreCase(data.getIdentifier()))
                .orElse(false);
    }

    public record SyncedEffectState(boolean hasMarker, int markerAmplifier, boolean hasUneasy, boolean hasSeasoned) {
    }
}