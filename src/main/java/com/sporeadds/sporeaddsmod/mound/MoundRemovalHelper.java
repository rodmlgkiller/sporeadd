package com.sporeadds.sporeaddsmod.mound;

import com.sporeadds.sporeaddsmod.data.MoundSavedData;
import com.sporeadds.sporeaddsmod.network.NetworkHandle;
import com.sporeadds.sporeaddsmod.network.SyncMoundCountPacket;
import com.sporeadds.sporeaddsmod.spore.PlayerSporeProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import com.sporeadds.sporeaddsmod.network.PacketDistributor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Limpieza de registros de mounds cuyo mound físico ya no existe.
 *
 * IMPORTANTE: nunca se borra un mound por el simple hecho de que su entidad no esté cargada
 * (chunk descargado, arranque del servidor con carga forzada aún pendiente, etc.). Solo se
 * elimina cuando el chunk del mound ESTÁ cargado y aun así la entidad no aparece durante
 * varios pases seguidos. Si el chunk no está cargado, se re-fuerza su carga y se reintenta.
 */
public class MoundRemovalHelper {

    /** Pases consecutivos con el chunk cargado y sin entidad antes de dar el mound por perdido. */
    private static final int MISSES_BEFORE_REMOVAL = 6;

    /** moundUUID -> nº de fallos confirmados consecutivos. En memoria (se reinicia al reiniciar). */
    private static final Map<UUID, Integer> MISS_STREAK = new HashMap<>();

    private MoundRemovalHelper() {
    }

    public static void cleanupMissingMounds(MinecraftServer server) {
        if (server == null) return;

        MoundSavedData savedData = MoundSavedData.get(server);
        Set<UUID> stillTracked = new HashSet<>();
        boolean anyRemoved = false;

        for (Map.Entry<UUID, List<MoundSavedData.MoundEntry>> ownerEntry : savedData.getAllEntries().entrySet()) {
            UUID ownerUUID = ownerEntry.getKey();
            List<UUID> confirmedGone = new ArrayList<>();

            for (MoundSavedData.MoundEntry moundEntry : ownerEntry.getValue()) {
                UUID moundUUID = moundEntry.getMoundUUID();
                stillTracked.add(moundUUID);

                Entity entity = findEntityAcrossDimensions(server, moundUUID);
                if (entity != null && !entity.isRemoved()) {
                    MISS_STREAK.remove(moundUUID);
                    continue;
                }

                ServerLevel level = savedData.getMoundLevel(server, ownerUUID, moundUUID);
                ChunkPos chunkPos = savedData.getMoundChunk(ownerUUID, moundUUID);

                if (level == null || chunkPos == null) {
                    // No hay forma de verificar -> jamás borrar por esto.
                    MISS_STREAK.remove(moundUUID);
                    continue;
                }

                if (!level.hasChunk(chunkPos.x, chunkPos.z)) {
                    // Chunk descargado: re-forzar su carga y esperar al siguiente pase.
                    level.setChunkForced(chunkPos.x, chunkPos.z, true);
                    MISS_STREAK.remove(moundUUID);
                    continue;
                }

                // Chunk cargado y sin entidad viva: contar un fallo confirmado.
                int streak = MISS_STREAK.merge(moundUUID, 1, Integer::sum);
                if (streak >= MISSES_BEFORE_REMOVAL) {
                    confirmedGone.add(moundUUID);
                }
            }

            if (confirmedGone.isEmpty()) continue;
            anyRemoved = true;

            ServerPlayer owner = server.getPlayerList().getPlayer(ownerUUID);

            for (UUID moundUUID : confirmedGone) {
                ServerLevel level = savedData.getMoundLevel(server, ownerUUID, moundUUID);
                ChunkPos chunkPos = savedData.getMoundChunk(ownerUUID, moundUUID);
                if (level != null && chunkPos != null) {
                    level.setChunkForced(chunkPos.x, chunkPos.z, false);
                }

                savedData.removeMound(ownerUUID, moundUUID);
                MISS_STREAK.remove(moundUUID);

                // Si el dueño está offline, dejar constancia para que su lista no lo resucite al reconectar.
                if (owner == null) {
                    savedData.noteOfflineRemoval(ownerUUID, moundUUID);
                }
            }

            if (owner != null) {
                PlayerSporeProvider.PLAYER_CAP.get(owner).ifPresent(spore -> {
                    boolean changed = false;
                    for (UUID moundUUID : confirmedGone) {
                        changed |= spore.getMoundRegistry().remove(moundUUID);
                    }
                    if (changed) {
                        CompoundTag nbt = new CompoundTag();
                        spore.saveNBTData(nbt);
                        NetworkHandle.INSTANCE.send(
                                PacketDistributor.PLAYER.with(() -> owner),
                                new SyncMoundCountPacket(nbt)
                        );
                    }
                });
            }
        }

        // Descartar streaks de mounds que ya no figuran en el almacén.
        MISS_STREAK.keySet().retainAll(stillTracked);

        if (anyRemoved) {
            savedData.persistNow(server);
        }
    }

    private static Entity findEntityAcrossDimensions(MinecraftServer server, UUID uuid) {
        for (ServerLevel level : server.getAllLevels()) {
            Entity entity = level.getEntity(uuid);
            if (entity != null) {
                return entity;
            }
        }
        return null;
    }
}
