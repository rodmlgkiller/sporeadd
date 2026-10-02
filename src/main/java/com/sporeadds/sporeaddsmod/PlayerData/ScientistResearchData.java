package com.sporeadds.sporeaddsmod.PlayerData;

import net.minecraft.nbt.CompoundTag;

import java.util.HashMap;
import java.util.Map;

public class ScientistResearchData {

    private final Map<String, Integer> killCounts = new HashMap<>();
    private final Map<String, Integer> dataAmounts = new HashMap<>();

    public int getKillCount(String entityId) {
        return killCounts.getOrDefault(entityId, 0);
    }

    public boolean hasKilled(String entityId) {
        return getKillCount(entityId) > 0;
    }

    public void incrementKill(String entityId) {
        killCounts.merge(entityId, 1, Integer::sum);
    }

    public Map<String, Integer> getAllKills() {
        return killCounts;
    }

    public int getDataAmount(String entityId) {
        return dataAmounts.getOrDefault(entityId, 0);
    }

    public void addData(String entityId, int amount) {
        dataAmounts.merge(entityId, amount, (oldVal, add) -> Math.max(0, oldVal + add));
    }

    public Map<String, Integer> getAllData() {
        return dataAmounts;
    }

    public void saveNBTData(CompoundTag tag) {
        CompoundTag killsTag = new CompoundTag();
        for (Map.Entry<String, Integer> entry : killCounts.entrySet()) {
            killsTag.putInt(entry.getKey(), entry.getValue());
        }
        tag.put("Kills", killsTag);

        CompoundTag dataTag = new CompoundTag();
        for (Map.Entry<String, Integer> entry : dataAmounts.entrySet()) {
            dataTag.putInt(entry.getKey(), entry.getValue());
        }
        tag.put("Data", dataTag);
    }

    public void loadNBTData(CompoundTag tag) {
        killCounts.clear();
        if (tag.contains("Kills")) {
            CompoundTag killsTag = tag.getCompound("Kills");
            for (String key : killsTag.getAllKeys()) {
                killCounts.put(key, killsTag.getInt(key));
            }
        }

        dataAmounts.clear();
        if (tag.contains("Data")) {
            CompoundTag dataTag = tag.getCompound("Data");
            for (String key : dataTag.getAllKeys()) {
                dataAmounts.put(key, dataTag.getInt(key));
            }
        }
    }
}