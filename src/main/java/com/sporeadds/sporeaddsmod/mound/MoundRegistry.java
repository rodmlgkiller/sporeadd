package com.sporeadds.sporeaddsmod.mound;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MoundRegistry {
    private final LinkedList<UUID> moundUUIDs = new LinkedList<>();
    private final Map<UUID, String> moundNames = new HashMap<>();
    private final Map<UUID, MoundLocation> moundLocations = new HashMap<>();
    private static final int MAX_SIZE = 10;

    private UUID preferredMound = null;

    public static class MoundLocation {
        private final String dimension;
        private final int x;
        private final int y;
        private final int z;

        public MoundLocation(String dimension, int x, int y, int z) {
            this.dimension = dimension;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public String getDimension() {
            return dimension;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public int getZ() {
            return z;
        }

        public String toDisplayString() {
            return dimension + " [" + x + ", " + y + ", " + z + "]";
        }
    }

    public void add(UUID uuid) {
        if (!moundUUIDs.contains(uuid)) {
            moundUUIDs.addLast(uuid);

            if (moundUUIDs.size() > MAX_SIZE) {
                UUID removed = moundUUIDs.removeFirst();
                if (removed != null) {
                    moundNames.remove(removed);
                    moundLocations.remove(removed);
                    if (removed.equals(preferredMound)) {
                        preferredMound = null;
                    }
                }
            }
        }
    }

    public boolean remove(UUID uuid) {
        boolean wasRemoved = moundUUIDs.remove(uuid);
        if (wasRemoved) {
            moundNames.remove(uuid);
            moundLocations.remove(uuid);
            if (uuid != null && uuid.equals(preferredMound)) {
                preferredMound = null;
            }
        }
        return wasRemoved;
    }

    public void cleanupDeadOrMissingMounds(ServerLevel level) {
        Iterator<UUID> it = moundUUIDs.iterator();
        while (it.hasNext()) {
            UUID uuid = it.next();
            Entity entity = level.getEntity(uuid);

            if (entity instanceof LivingEntity living) {
                if (!living.isAlive() || living.isRemoved()) {
                    it.remove();
                    moundNames.remove(uuid);
                    moundLocations.remove(uuid);
                    if (uuid.equals(preferredMound)) {
                        preferredMound = null;
                    }
                }
            }
        }
    }

    public List<UUID> getList() {
        return moundUUIDs;
    }

    public List<UUID> getDisplayList() {
        return new ArrayList<>(moundUUIDs);
    }

    public String getName(UUID uuid) {
        return uuid == null ? null : moundNames.get(uuid);
    }

    public boolean setName(UUID uuid, String name) {
        if (uuid == null || !moundUUIDs.contains(uuid)) return false;

        String normalized = normalizeName(name);
        if (normalized == null) return false;

        for (Map.Entry<UUID, String> entry : moundNames.entrySet()) {
            if (!entry.getKey().equals(uuid) && entry.getValue().equalsIgnoreCase(normalized)) {
                return false;
            }
        }

        moundNames.put(uuid, normalized);
        return true;
    }

    public boolean hasName(String name) {
        String normalized = normalizeName(name);
        if (normalized == null) return false;

        for (String existing : moundNames.values()) {
            if (existing.equalsIgnoreCase(normalized)) {
                return true;
            }
        }
        return false;
    }

    public UUID findByName(String name) {
        String normalized = normalizeName(name);
        if (normalized == null) return null;

        for (UUID uuid : moundUUIDs) {
            String existing = moundNames.get(uuid);
            if (existing != null && existing.equalsIgnoreCase(normalized)) {
                return uuid;
            }
        }
        return null;
    }

    public UUID findBySlot(int slot) {
        if (slot < 1 || slot > moundUUIDs.size()) return null;
        return moundUUIDs.get(slot - 1);
    }

    public Integer getSlotOf(UUID uuid) {
        int index = moundUUIDs.indexOf(uuid);
        return index >= 0 ? index + 1 : null;
    }

    public void updateLastKnownLocation(UUID uuid, String dimension, int x, int y, int z) {
        if (uuid == null || !moundUUIDs.contains(uuid)) return;

        String normalizedDim = normalizeName(dimension);
        if (normalizedDim == null) return;

        moundLocations.put(uuid, new MoundLocation(normalizedDim, x, y, z));
    }

    public MoundLocation getLastKnownLocation(UUID uuid) {
        return uuid == null ? null : moundLocations.get(uuid);
    }

    private String normalizeName(String name) {
        if (name == null) return null;
        String normalized = name.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    public void clear() {
        moundUUIDs.clear();
        moundNames.clear();
        moundLocations.clear();
        preferredMound = null;
    }

    public void setPreferredMound(UUID uuid) {
        this.preferredMound = uuid;
    }

    public UUID getPreferredMound() {
        return this.preferredMound;
    }

    public void saveNBT(CompoundTag nbt) {
        ListTag list = new ListTag();
        for (UUID uuid : moundUUIDs) {
            CompoundTag uuidTag = new CompoundTag();
            uuidTag.putUUID("uuid", uuid);

            String name = moundNames.get(uuid);
            if (name != null && !name.isBlank()) {
                uuidTag.putString("name", name);
            }

            MoundLocation location = moundLocations.get(uuid);
            if (location != null) {
                uuidTag.putString("dimension", location.getDimension());
                uuidTag.putInt("x", location.getX());
                uuidTag.putInt("y", location.getY());
                uuidTag.putInt("z", location.getZ());
            }

            list.add(uuidTag);
        }
        nbt.put("moundUuids", list);

        if (preferredMound != null) {
            nbt.putUUID("preferredMound", preferredMound);
        }
    }

    public void loadNBT(CompoundTag nbt) {
        moundUUIDs.clear();
        moundNames.clear();
        moundLocations.clear();

        ListTag list = nbt.getList("moundUuids", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag uuidTag = list.getCompound(i);
            UUID uuid = uuidTag.getUUID("uuid");
            moundUUIDs.add(uuid);

            if (uuidTag.contains("name", Tag.TAG_STRING)) {
                String name = uuidTag.getString("name").trim();
                if (!name.isEmpty()) {
                    moundNames.put(uuid, name);
                }
            }

            if (uuidTag.contains("dimension", Tag.TAG_STRING)) {
                String dimension = uuidTag.getString("dimension").trim();
                int x = uuidTag.getInt("x");
                int y = uuidTag.getInt("y");
                int z = uuidTag.getInt("z");

                if (!dimension.isEmpty()) {
                    moundLocations.put(uuid, new MoundLocation(dimension, x, y, z));
                }
            }
        }

        if (nbt.hasUUID("preferredMound")) {
            preferredMound = nbt.getUUID("preferredMound");
        } else {
            preferredMound = null;
        }
    }
}