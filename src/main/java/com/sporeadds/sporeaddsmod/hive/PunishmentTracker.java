package com.sporeadds.sporeaddsmod.hive;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

/**
 * Estado persistente del evento "punishment" del Proto, por jugador. Se guarda bajo
 * {@link Player#PERSISTED_NBT_TAG} ("PlayerPersisted"), que Forge copia automáticamente al
 * respawnear, así que sobrevive a la muerte sin depender de capabilities.
 */
public final class PunishmentTracker {

    private static final String ROOT = "sporeadd_punishment";
    private static final String POINTS = "points";
    private static final String PERMADEAD = "permadead";
    private static final String DECAY_CHECKPOINT = "decay_checkpoint";

    private PunishmentTracker() {
    }

    private static CompoundTag root(Player player) {
        return player.getPersistentData()
                .getCompound(Player.PERSISTED_NBT_TAG)
                .getCompound(ROOT);
    }

    private static void store(Player player, CompoundTag root) {
        CompoundTag persisted = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        persisted.put(ROOT, root);
        player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persisted);
    }

    public static int getPoints(Player player) {
        return root(player).getInt(POINTS);
    }

    public static void setPoints(Player player, int points) {
        CompoundTag root = root(player);
        root.putInt(POINTS, Math.max(0, points));
        store(player, root);
    }

    public static boolean isPermadead(Player player) {
        return root(player).getBoolean(PERMADEAD);
    }

    public static void setPermadead(Player player, boolean value) {
        CompoundTag root = root(player);
        root.putBoolean(PERMADEAD, value);
        store(player, root);
    }

    /** Game time (mundo) desde la que se cuenta la próxima bajada de nivel por inactividad. 0 = sin fijar. */
    public static long getDecayCheckpoint(Player player) {
        return root(player).getLong(DECAY_CHECKPOINT);
    }

    public static void setDecayCheckpoint(Player player, long gameTime) {
        CompoundTag root = root(player);
        root.putLong(DECAY_CHECKPOINT, gameTime);
        store(player, root);
    }
}
