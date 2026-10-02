package com.sporeadds.sporeaddsmod.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClassPopulationData extends SavedData {

    private static final String DATA_NAME = "sporeadd_class_population";

    private final Map<UUID, String> playerClasses = new HashMap<>();

    public static ClassPopulationData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(ClassPopulationData::new, (tag, registries) -> ClassPopulationData.load(tag), null),
                DATA_NAME
        );
    }

    public static ClassPopulationData load(CompoundTag tag) {
        ClassPopulationData data = new ClassPopulationData();

        ListTag entries = tag.getList("players", Tag.TAG_COMPOUND);
        for (int i = 0; i < entries.size(); i++) {
            CompoundTag entry = entries.getCompound(i);
            data.playerClasses.put(entry.getUUID("uuid"), entry.getString("classId"));
        }

        return data;
    }

    public void setPlayerClass(UUID playerUUID, String classId) {
        String normalized = classId == null ? "none" : classId.toLowerCase();

        if (normalized.equals("none")) {
            playerClasses.remove(playerUUID);
        } else {
            playerClasses.put(playerUUID, normalized);
        }

        setDirty();
    }

    public int countClass(String classId) {
        if (classId == null) {
            return 0;
        }
        String normalized = classId.toLowerCase();

        int count = 0;
        for (String value : playerClasses.values()) {
            if (value.equalsIgnoreCase(normalized)) {
                count++;
            }
        }
        return count;
    }

    @Override
    public CompoundTag save(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        ListTag entries = new ListTag();

        for (Map.Entry<UUID, String> entry : playerClasses.entrySet()) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putUUID("uuid", entry.getKey());
            entryTag.putString("classId", entry.getValue());
            entries.add(entryTag);
        }

        tag.put("players", entries);
        return tag;
    }
}
