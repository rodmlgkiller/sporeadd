package com.sporeadds.sporeaddsmod.event;

import net.neoforged.fml.common.EventBusSubscriber;

import com.sporeadds.sporeaddsmod.PlayerData.PlayerData;
import com.sporeadds.sporeaddsmod.PlayerData.PlayerDataProvider;
import com.sporeadds.sporeaddsmod.data.MoundSavedData;
import com.sporeadds.sporeaddsmod.mound.MoundRegistry;
import com.sporeadds.sporeaddsmod.network.EyesDataSyncPacket;
import com.sporeadds.sporeaddsmod.network.NetworkHandle;
import com.sporeadds.sporeaddsmod.network.SyncMoundCountPacket;
import com.sporeadds.sporeaddsmod.spore.PlayerSporeProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import com.sporeadds.sporeaddsmod.network.PacketDistributor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = "sporeadd", bus = EventBusSubscriber.Bus.GAME)
public class CapabilityEvents {

    // La copia de capabilities en PlayerEvent.Clone (muerte y regreso del End) la hace
    // ForgeEvents.onPlayerClone de forma centralizada para todas las capabilities del mod.

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        syncEyesToTrackingAndSelf(player);
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        syncMoundsFromSavedData(player);
        syncEyesToSelf(player);
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        syncMoundsFromSavedData(player);
        syncEyesToSelf(player);
        syncEyesToTrackingAndSelf(player);
    }

    private static void syncEyesToSelf(ServerPlayer player) {
        PlayerData data = PlayerDataProvider.get(player);

        NetworkHandle.INSTANCE.send(
                PacketDistributor.PLAYER.with(() -> player),
                new EyesDataSyncPacket(
                        player.getId(),
                        data.getEyeBaseType(),
                        data.getEyeGlowType(),
                        data.getGlowOffsetX(),
                        data.getGlowOffsetY()
                )
        );
    }

    private static void syncEyesToTrackingAndSelf(ServerPlayer player) {
        PlayerData data = PlayerDataProvider.get(player);

        NetworkHandle.INSTANCE.send(
                PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                new EyesDataSyncPacket(
                        player.getId(),
                        data.getEyeBaseType(),
                        data.getEyeGlowType(),
                        data.getGlowOffsetX(),
                        data.getGlowOffsetY()
                )
        );
    }

    /**
     * Reconcilia la lista de mounds del jugador entre sus DOS almacenes:
     *  - {@link MoundRegistry} en la capability del jugador (se guarda en su NBT en cada logout).
     *  - {@link MoundSavedData} a nivel de mundo (solo se guarda en autosave / apagado limpio).
     *
     * Antes este método SUSTITUÍA el registro del jugador por lo que hubiera en MoundSavedData;
     * si ese archivo estaba desfasado (crash del servidor, primer arranque, etc.) el jugador
     * perdía toda su lista de mounds al reconectar. Ahora se hace una UNIÓN que sana ambos
     * almacenes en lugar de descartar datos.
     */
    private static void syncMoundsFromSavedData(ServerPlayer player) {
        MoundSavedData savedData = MoundSavedData.get(player.server);
        UUID playerUUID = player.getUUID();

        player.getCapability(PlayerSporeProvider.PLAYER_CAP).ifPresent(spore -> {
            MoundRegistry registry = spore.getMoundRegistry();

            List<UUID> registryOrder = registry.getDisplayList();   // lo que venía del NBT del jugador
            Map<UUID, String> names = new HashMap<>();
            Map<UUID, MoundRegistry.MoundLocation> locations = new HashMap<>();
            for (UUID uuid : registryOrder) {
                String name = registry.getName(uuid);
                if (name != null && !name.isBlank()) {
                    names.put(uuid, name);
                }
                MoundRegistry.MoundLocation location = registry.getLastKnownLocation(uuid);
                if (location != null) {
                    locations.put(uuid, location);
                }
            }
            UUID preferred = registry.getPreferredMound();

            List<UUID> savedOrder = savedData.getMounds(playerUUID);
            Set<UUID> savedSet = new HashSet<>(savedOrder);

            // Mounds retirados mientras el jugador estaba offline: no deben volver a la lista.
            Set<UUID> offlineRemoved = savedData.drainOfflineRemovals(playerUUID);

            // Sanar MoundSavedData: reañadir los mounds que el jugador tenía en su NBT y que
            // el almacén de mundo ha perdido (solo si conservamos su ubicación).
            for (UUID uuid : registryOrder) {
                if (savedSet.contains(uuid) || offlineRemoved.contains(uuid)) {
                    continue;
                }
                MoundRegistry.MoundLocation loc = locations.get(uuid);
                if (loc == null || loc.getDimension() == null || loc.getDimension().isBlank()) {
                    continue;
                }
                ResourceKey<Level> dimKey = ResourceKey.create(
                        Registries.DIMENSION, ResourceLocation.parse(loc.getDimension()));
                savedData.addMound(playerUUID, uuid, dimKey,
                        new ChunkPos(new BlockPos(loc.getX(), loc.getY(), loc.getZ())));
            }

            // Registro final: primero la lista del jugador (su NBT es la autoridad), luego se
            // rellena con extras de MoundSavedData hasta el tope de 10; se excluyen los mounds
            // retirados mientras estaba offline.
            LinkedHashSet<UUID> merged = new LinkedHashSet<>();
            for (UUID uuid : registryOrder) {
                if (!offlineRemoved.contains(uuid)) {
                    merged.add(uuid);
                }
            }
            for (UUID uuid : savedOrder) {
                if (merged.size() >= 10) break;
                if (!offlineRemoved.contains(uuid)) {
                    merged.add(uuid);
                }
            }
            for (UUID removed : offlineRemoved) {
                savedData.removeMound(playerUUID, removed);
            }

            registry.clear();
            for (UUID uuid : merged) {
                registry.add(uuid);

                String name = names.get(uuid);
                if (name != null && !name.isBlank()) {
                    registry.setName(uuid, name);
                }

                MoundRegistry.MoundLocation location = locations.get(uuid);
                if (location != null) {
                    registry.updateLastKnownLocation(
                            uuid,
                            location.getDimension(),
                            location.getX(),
                            location.getY(),
                            location.getZ()
                    );
                }
            }

            if (preferred != null && registry.getList().contains(preferred)) {
                registry.setPreferredMound(preferred);
            }

            savedData.persistNow(player.server);

            CompoundTag nbt = new CompoundTag();
            spore.saveNBTData(nbt);

            NetworkHandle.INSTANCE.send(
                    PacketDistributor.PLAYER.with(() -> player),
                    new SyncMoundCountPacket(nbt)
            );
        });
    }
}