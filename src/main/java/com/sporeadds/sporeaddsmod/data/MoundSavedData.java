package com.sporeadds.sporeaddsmod.data;

import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class MoundSavedData extends SavedData {

    private static final String DATA_NAME = "sporeadd_mounds";

    private final Map<UUID, LinkedList<MoundEntry>> playerMounds = new HashMap<>();

    /**
     * Mounds retirados mientras su dueño estaba desconectado (muerte del mound o limpieza).
     * En el siguiente login del dueño se restan de su lista y se consumen. Evita que la
     * fusión de listas al reconectar "resucite" un mound realmente destruido.
     */
    private final Map<UUID, Set<UUID>> offlineRemovals = new HashMap<>();

    public static MoundSavedData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(MoundSavedData::new, (tag, registries) -> MoundSavedData.load(tag), null),
                DATA_NAME
        );
    }

    public static MoundSavedData load(CompoundTag tag) {
        MoundSavedData data = new MoundSavedData();

        ListTag players = tag.getList("players", Tag.TAG_COMPOUND);
        for (int i = 0; i < players.size(); i++) {
            CompoundTag playerTag = players.getCompound(i);
            UUID ownerUUID = playerTag.getUUID("owner");

            LinkedList<MoundEntry> mounds = new LinkedList<>();
            ListTag moundList = playerTag.getList("mounds", Tag.TAG_COMPOUND);

            for (int j = 0; j < moundList.size(); j++) {
                CompoundTag moundTag = moundList.getCompound(j);
                UUID moundUUID = moundTag.getUUID("uuid");
                String dimensionId = moundTag.getString("dimension");
                int chunkX = moundTag.getInt("chunkX");
                int chunkZ = moundTag.getInt("chunkZ");

                mounds.add(new MoundEntry(moundUUID, dimensionId, chunkX, chunkZ));
            }

            data.playerMounds.put(ownerUUID, mounds);
        }

        ListTag removals = tag.getList("offlineRemovals", Tag.TAG_COMPOUND);
        for (int i = 0; i < removals.size(); i++) {
            CompoundTag ownerTag = removals.getCompound(i);
            UUID ownerUUID = ownerTag.getUUID("owner");
            Set<UUID> set = new HashSet<>();
            ListTag ids = ownerTag.getList("mounds", Tag.TAG_INT_ARRAY);
            for (int j = 0; j < ids.size(); j++) {
                set.add(net.minecraft.nbt.NbtUtils.loadUUID(ids.get(j)));
            }
            if (!set.isEmpty()) {
                data.offlineRemovals.put(ownerUUID, set);
            }
        }

        return data;
    }

    /** Marca un mound como retirado con su dueño offline (se consume en su próximo login). */
    public void noteOfflineRemoval(UUID ownerUUID, UUID moundUUID) {
        offlineRemovals.computeIfAbsent(ownerUUID, k -> new HashSet<>()).add(moundUUID);
        setDirty();
    }

    /** Devuelve y limpia los mounds retirados offline para este dueño. */
    public Set<UUID> drainOfflineRemovals(UUID ownerUUID) {
        Set<UUID> set = offlineRemovals.remove(ownerUUID);
        if (set == null || set.isEmpty()) {
            return Collections.emptySet();
        }
        setDirty();
        return set;
    }

    /** Fuerza el guardado inmediato en disco (no esperar al autosave del mundo). */
    public void persistNow(MinecraftServer server) {
        if (server != null) {
            server.overworld().getDataStorage().save();
        }
    }

    public void addMound(UUID ownerUUID, UUID moundUUID, ResourceKey<Level> levelKey, ChunkPos chunkPos) {
        playerMounds.computeIfAbsent(ownerUUID, k -> new LinkedList<>());
        LinkedList<MoundEntry> list = playerMounds.get(ownerUUID);

        list.removeIf(entry -> entry.moundUUID.equals(moundUUID));

        list.addLast(new MoundEntry(
                moundUUID,
                levelKey.location().toString(),
                chunkPos.x,
                chunkPos.z
        ));

        while (list.size() > 10) {
            list.removeFirst();
        }

        setDirty();
    }

    public boolean removeMound(UUID ownerUUID, UUID moundUUID) {
        LinkedList<MoundEntry> list = playerMounds.get(ownerUUID);
        if (list == null) return false;

        boolean removed = list.removeIf(entry -> entry.moundUUID.equals(moundUUID));
        if (removed) {
            if (list.isEmpty()) {
                playerMounds.remove(ownerUUID);
            }
            setDirty();
        }
        return removed;
    }

    public List<UUID> getMounds(UUID ownerUUID) {
        LinkedList<MoundEntry> list = playerMounds.get(ownerUUID);
        if (list == null) return Collections.emptyList();

        List<UUID> result = new ArrayList<>();
        for (MoundEntry entry : list) {
            result.add(entry.moundUUID);
        }
        return result;
    }

    public MoundEntry getMoundEntry(UUID ownerUUID, UUID moundUUID) {
        LinkedList<MoundEntry> list = playerMounds.get(ownerUUID);
        if (list == null) return null;

        for (MoundEntry entry : list) {
            if (entry.moundUUID.equals(moundUUID)) {
                return entry;
            }
        }
        return null;
    }

    public ChunkPos getMoundChunk(UUID ownerUUID, UUID moundUUID) {
        MoundEntry entry = getMoundEntry(ownerUUID, moundUUID);
        return entry == null ? null : new ChunkPos(entry.chunkX, entry.chunkZ);
    }

    public ServerLevel getMoundLevel(MinecraftServer server, UUID ownerUUID, UUID moundUUID) {
        MoundEntry entry = getMoundEntry(ownerUUID, moundUUID);
        if (entry == null || entry.dimensionId == null || entry.dimensionId.isBlank()) return null;

        ResourceKey<Level> levelKey = ResourceKey.create(
                Registries.DIMENSION,
                ResourceLocation.parse(entry.dimensionId)
        );

        return server.getLevel(levelKey);
    }

    public Map<UUID, List<MoundEntry>> getAllEntries() {
        Map<UUID, List<MoundEntry>> copy = new HashMap<>();
        for (Map.Entry<UUID, LinkedList<MoundEntry>> entry : playerMounds.entrySet()) {
            copy.put(entry.getKey(), List.copyOf(entry.getValue()));
        }
        return Collections.unmodifiableMap(copy);
    }

    @Override
    public CompoundTag save(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        ListTag players = new ListTag();

        for (Map.Entry<UUID, LinkedList<MoundEntry>> entry : playerMounds.entrySet()) {
            CompoundTag playerTag = new CompoundTag();
            playerTag.putUUID("owner", entry.getKey());

            ListTag moundList = new ListTag();
            for (MoundEntry mound : entry.getValue()) {
                CompoundTag moundTag = new CompoundTag();
                moundTag.putUUID("uuid", mound.moundUUID);
                moundTag.putString("dimension", mound.dimensionId);
                moundTag.putInt("chunkX", mound.chunkX);
                moundTag.putInt("chunkZ", mound.chunkZ);
                moundList.add(moundTag);
            }

            playerTag.put("mounds", moundList);
            players.add(playerTag);
        }

        tag.put("players", players);

        ListTag removals = new ListTag();
        for (Map.Entry<UUID, Set<UUID>> entry : offlineRemovals.entrySet()) {
            if (entry.getValue().isEmpty()) continue;
            CompoundTag ownerTag = new CompoundTag();
            ownerTag.putUUID("owner", entry.getKey());
            ListTag ids = new ListTag();
            for (UUID moundUUID : entry.getValue()) {
                ids.add(net.minecraft.nbt.NbtUtils.createUUID(moundUUID));
            }
            ownerTag.put("mounds", ids);
            removals.add(ownerTag);
        }
        tag.put("offlineRemovals", removals);

        return tag;
    }

    public static class MoundEntry {
        private final UUID moundUUID;
        private final String dimensionId;
        private final int chunkX;
        private final int chunkZ;

        public MoundEntry(UUID moundUUID, String dimensionId, int chunkX, int chunkZ) {
            this.moundUUID = moundUUID;
            this.dimensionId = dimensionId;
            this.chunkX = chunkX;
            this.chunkZ = chunkZ;
        }

        public UUID getMoundUUID() {
            return moundUUID;
        }

        public String getDimensionId() {
            return dimensionId;
        }

        public int getChunkX() {
            return chunkX;
        }

        public int getChunkZ() {
            return chunkZ;
        }
    }
}