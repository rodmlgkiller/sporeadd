package com.sporeadds.sporeaddsmod.event;

import net.minecraft.nbt.CompoundTag;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ImplantDeathCache {
    private static final Map<UUID, CompoundTag> CACHE = new HashMap<>();

    public static void store(UUID uuid, CompoundTag tag) {
        CACHE.put(uuid, tag.copy());
    }

    public static CompoundTag take(UUID uuid) {
        return CACHE.remove(uuid);
    }
}