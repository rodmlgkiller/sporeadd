package com.sporeadds.sporeaddsmod.util;

import com.Harbinger.Spore.core.Sentities;
import com.Harbinger.Spore.Sentities.Utility.ArenaEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public final class RaidSpawner {

    public static int DEFAULT_WAVE_SIZE = 10;
    public static int DEFAULT_WAVE_LEVEL = 2;
    public static int DEFAULT_SPECIAL_SPAWNS = 3;
    public static double DEFAULT_Y_OFFSET = 0.0D;
    public static boolean USE_STAGED_RAID_TAG = true;

    private RaidSpawner() {
    }

    public static ArenaEntity spawnCustomRaid(ServerLevel level, BlockPos pos) {
        return spawnCustomRaid(
                level,
                pos,
                DEFAULT_WAVE_SIZE,
                DEFAULT_WAVE_LEVEL,
                DEFAULT_SPECIAL_SPAWNS,
                DEFAULT_Y_OFFSET,
                USE_STAGED_RAID_TAG
        );
    }

    public static ArenaEntity spawnCustomRaid(ServerLevel level, BlockPos pos, int waveSize, int waveLevel, int specialSpawns, double yOffset, boolean stagedRaid) {
        int safeWaveSize = Math.max(0, waveSize);
        int safeWaveLevel = Math.max(0, waveLevel);
        int safeSpecialSpawns = Math.max(0, specialSpawns);

        ArenaEntity arena = new ArenaEntity(Sentities.ARENA_TENDRIL.get(), level);
        arena.setPos(pos.getX() + 0.5D, pos.getY() + yOffset, pos.getZ() + 0.5D);

        if (stagedRaid) {
            arena.getPersistentData().putBoolean("staged_raid", true);
        }

        arena.setWaveSize(safeWaveSize);
        arena.setWaveLevel(safeWaveLevel);
        arena.setAmountOfSpecialSpawns(safeSpecialSpawns);
        arena.startWave(true);

        level.addFreshEntity(arena);
        return arena;
    }

    public static void applyManualValues(ArenaEntity arena, int waveSize, int waveLevel, int specialSpawns) {
        if (arena != null) {
            arena.setWaveSize(Math.max(0, waveSize));
            arena.setWaveLevel(Math.max(0, waveLevel));
            arena.setAmountOfSpecialSpawns(Math.max(0, specialSpawns));
        }
    }

    public static void markAsStagedRaid(ArenaEntity arena) {
        if (arena != null) {
            arena.getPersistentData().putBoolean("staged_raid", true);
        }
    }
}