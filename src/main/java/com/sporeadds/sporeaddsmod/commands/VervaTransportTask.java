package com.sporeadds.sporeaddsmod.commands;

import net.neoforged.neoforge.event.tick.ServerTickEvent;

import net.neoforged.fml.common.EventBusSubscriber;

import net.minecraft.core.registries.BuiltInRegistries;

import com.Harbinger.Spore.Sentities.Organoids.Verwa;
import com.sporeadds.sporeaddsmod.network.NetworkHandle;
import com.sporeadds.sporeaddsmod.network.SyncVervaCountdownPacket;
import com.sporeadds.sporeaddsmod.network.SyncVervaTravelPacket;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.EntityMountEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import com.sporeadds.sporeaddsmod.network.PacketDistributor;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = "sporeadd", bus = EventBusSubscriber.Bus.GAME)
public class VervaTransportTask {

    private static final List<TransportData> activeTransports = new ArrayList<>();
    private static final Set<UUID> transportVervas = new HashSet<>();
    /** Verwa de transporte -> UUID del ÚNICO jugador que puede montarla. */
    private static final Map<UUID, UUID> transportVervaOwner = new HashMap<>();
    private static final Set<UUID> clientTravelingPlayers = new HashSet<>();

    public static boolean isTransportVerva(UUID uuid) {
        return transportVervas.contains(uuid);
    }

    private static void registerTransportVerva(UUID vervaId, UUID ownerId) {
        transportVervas.add(vervaId);
        transportVervaOwner.put(vervaId, ownerId);
    }

    private static void unregisterTransportVerva(UUID vervaId) {
        transportVervas.remove(vervaId);
        transportVervaOwner.remove(vervaId);
    }

    /** Impide que cualquier jugador que no sea el destinatario monte una Verwa de transporte. */
    @SubscribeEvent
    public static void onEntityMount(EntityMountEvent event) {
        if (!event.isMounting()) return;
        Entity mount = event.getEntityBeingMounted();
        Entity rider = event.getEntityMounting();
        if (mount == null || rider == null) return;

        UUID owner = transportVervaOwner.get(mount.getUUID());
        if (owner != null && !owner.equals(rider.getUUID())) {
            event.setCanceled(true);
        }
    }

    public static void addClientTraveler(UUID playerUUID) {
        clientTravelingPlayers.add(playerUUID);
    }

    public static void removeClientTraveler(UUID playerUUID) {
        clientTravelingPlayers.remove(playerUUID);
    }

    public static void startTask(ServerPlayer player, Vec3 startPos, Vec3 targetPos, ServerLevel targetLevel) {
        startTask(player, startPos, targetPos, targetLevel, 0);
    }

    /** Igual, pero con prioridad de HUD: un número más bajo gana el cronómetro en pantalla. */
    public static void startTask(ServerPlayer player, Vec3 startPos, Vec3 targetPos, ServerLevel targetLevel, int priority) {
        TransportData data = new TransportData(player, startPos, targetPos, player.serverLevel(), targetLevel);
        data.priority = priority;
        activeTransports.add(data);
    }

    public static boolean isPlayerTraveling(UUID playerUUID) {
        return clientTravelingPlayers.contains(playerUUID);
    }

    /** Servidor: hay un transporte Verva activo (en cualquier fase) para este jugador. */
    public static boolean isPlayerBeingTransported(UUID playerUUID) {
        for (TransportData data : activeTransports) {
            if (data.player.getUUID().equals(playerUUID)) {
                return true;
            }
        }
        return false;
    }

    private static void spawnCircleParticles(ServerLevel level, Vec3 center, double radius) {
        DustParticleOptions particle = new DustParticleOptions(new Vector3f(1.0F, 0.0F, 0.0F), 1.5F);
        for (int i = 0; i < 36; i++) {
            double angle = 2 * Math.PI * i / 36;
            double x = center.x + radius * Math.cos(angle);
            double z = center.z + radius * Math.sin(angle);
            level.sendParticles(particle, x, center.y + 0.1, z, 1, 0, 0, 0, 0);
        }
    }

    private static Mob createVerva(ServerLevel level, Vec3 pos) {
        EntityType<?> vervaType = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.fromNamespaceAndPath("spore", "verva"));
        if (vervaType == null) return null;
        if (!(vervaType.create(level) instanceof Mob verva)) return null;

        verva.setPos(pos.x, pos.y, pos.z);
        verva.setPersistenceRequired();

        if (verva instanceof Verwa verwaEntity) {
            verwaEntity.setStoredMob("spore:scent");
        }

        return verva;
    }

    private static void sendCountdown(ServerPlayer player, TransportData data, int secondsRemaining, boolean active) {
        NetworkHandle.INSTANCE.send(
                PacketDistributor.PLAYER.with(() -> player),
                new SyncVervaCountdownPacket(data.transportId, secondsRemaining, data.priority, active)
        );
    }

    private static void cancelTransport(TransportData data, Iterator<TransportData> iterator, Component message) {
        data.player.removeEffect(MobEffects.GLOWING);

        if (data.verva != null && data.player.getVehicle() == data.verva) {
            data.player.stopRiding();
        }

        if (data.verva != null) {
            unregisterTransportVerva(data.verva.getUUID());
            if (data.verva.isAlive()) {
                data.verva.discard();
            }
        }

        sendCountdown(data.player, data, 0, false);

        if (message != null) {
            data.player.sendSystemMessage(message);
        }

        NetworkHandle.INSTANCE.send(
                PacketDistributor.ALL.noArg(),
                new SyncVervaTravelPacket(data.player.getUUID(), false)
        );

        iterator.remove();
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {

        Iterator<TransportData> iterator = activeTransports.iterator();

        while (iterator.hasNext()) {
            TransportData data = iterator.next();
            data.ticksElapsed++;

            ServerPlayer player = data.player;

            if (!player.isAlive() || player.hasDisconnected()) {
                cancelTransport(data, iterator, null);
                continue;
            }

            // Salvaguarda: una Verwa de transporte solo puede llevar a su destinatario.
            if (data.verva != null && data.verva.isAlive() && data.verva.isVehicle()) {
                for (Entity passenger : new ArrayList<>(data.verva.getPassengers())) {
                    if (!passenger.getUUID().equals(player.getUUID())) {
                        passenger.stopRiding();
                    }
                }
            }

            if (data.state == 0) {
                if (!data.appliedWaitingGlow) {
                    player.addEffect(new MobEffectInstance(MobEffects.GLOWING, 520, 0, false, false, true));
                    data.appliedWaitingGlow = true;
                }

                if (data.ticksElapsed % 5 == 0) {
                    spawnCircleParticles(data.originLevel, data.startPos, 5.0);
                }

                int ticksRemaining = 500 - data.ticksElapsed;
                int secondsRemaining = (int) Math.ceil(ticksRemaining / 20.0);

                if (secondsRemaining > 0 && secondsRemaining != data.lastCountdownSecond) {
                    data.lastCountdownSecond = secondsRemaining;
                    sendCountdown(player, data, secondsRemaining, true);
                }

                if (data.ticksElapsed >= 500) {
                    player.removeEffect(MobEffects.GLOWING);
                    data.appliedWaitingGlow = false;
                    sendCountdown(player, data, 0, false);

                    if (player.distanceToSqr(data.startPos) > 25.0) {
                        cancelTransport(data, iterator, Component.translatable("message.sporeadds.verva_transport.cancelled_outside_area").withStyle(net.minecraft.ChatFormatting.DARK_RED));
                        continue;
                    }

                    if (player.getVehicle() != null) {
                        cancelTransport(data, iterator, Component.translatable("message.sporeadds.verva_transport.cancelled_riding_other").withStyle(net.minecraft.ChatFormatting.DARK_RED));
                        continue;
                    }

                    data.verva = createVerva(data.originLevel, data.startPos);
                    if (data.verva != null) {
                        data.originLevel.addFreshEntity(data.verva);
                        registerTransportVerva(data.verva.getUUID(), data.player.getUUID());

                        if (data.verva instanceof Verwa verwaEntity) {
                            verwaEntity.tickEmerging();
                        }
                    } else {
                        cancelTransport(data, iterator, Component.translatable("message.sporeadds.verva_transport.cancelled_create_origin").withStyle(net.minecraft.ChatFormatting.DARK_RED));
                        continue;
                    }

                    data.state = 1;
                    data.ticksElapsed = 0;
                }
                continue;
            }

            if (data.state == 1) {
                if (data.verva == null || !data.verva.isAlive()) {
                    cancelTransport(data, iterator, Component.translatable("message.sporeadds.verva_transport.cancelled_destroyed").withStyle(net.minecraft.ChatFormatting.DARK_RED));
                    continue;
                }

                if (data.ticksElapsed >= 40) {
                    if (player.getVehicle() != null) {
                        cancelTransport(data, iterator, Component.translatable("message.sporeadds.verva_transport.cancelled_already_riding").withStyle(net.minecraft.ChatFormatting.DARK_RED));
                        continue;
                    }

                    boolean mounted = player.startRiding(data.verva, true);
                    if (!mounted || player.getVehicle() != data.verva) {
                        cancelTransport(data, iterator, Component.translatable("message.sporeadds.verva_transport.cancelled_mount_failed").withStyle(net.minecraft.ChatFormatting.DARK_RED));
                        continue;
                    }

                    NetworkHandle.INSTANCE.send(
                            PacketDistributor.ALL.noArg(),
                            new SyncVervaTravelPacket(player.getUUID(), true)
                    );

                    if (data.verva instanceof Verwa verwaEntity) {
                        verwaEntity.tickBurrowing();
                    }

                    data.state = 2;
                    data.ticksElapsed = 0;
                }
                continue;
            }

            if (data.state == 2) {
                if (data.verva == null || !data.verva.isAlive()) {
                    cancelTransport(data, iterator, Component.translatable("message.sporeadds.verva_transport.cancelled_first_destroyed").withStyle(net.minecraft.ChatFormatting.DARK_RED));
                    continue;
                }

                if (player.getVehicle() != data.verva) {
                    cancelTransport(data, iterator, Component.translatable("message.sporeadds.verva_transport.cancelled_left_verva").withStyle(net.minecraft.ChatFormatting.DARK_RED));
                    continue;
                }

                if (data.ticksElapsed >= 60) {
                    unregisterTransportVerva(data.verva.getUUID());
                    data.verva.discard();

                    player.teleportTo(data.targetLevel, data.targetPos.x, data.targetPos.y, data.targetPos.z, player.getYRot(), player.getXRot());

                    data.verva = createVerva(data.targetLevel, data.targetPos);
                    if (data.verva != null) {
                        data.targetLevel.addFreshEntity(data.verva);
                        registerTransportVerva(data.verva.getUUID(), data.player.getUUID());

                        player.startRiding(data.verva, true);

                        if (data.verva instanceof Verwa verwaEntity) {
                            verwaEntity.tickEmerging();
                        }
                    } else {
                        cancelTransport(data, iterator, Component.translatable("message.sporeadds.verva_transport.cancelled_create_destination").withStyle(net.minecraft.ChatFormatting.DARK_RED));
                        continue;
                    }

                    data.state = 3;
                    data.ticksElapsed = 0;
                }
                continue;
            }

            if (data.state == 3) {
                if (data.verva == null || !data.verva.isAlive()) {
                    NetworkHandle.INSTANCE.send(
                            PacketDistributor.ALL.noArg(),
                            new SyncVervaTravelPacket(player.getUUID(), false)
                    );
                    iterator.remove();
                    continue;
                }

                if (player.getVehicle() != data.verva && !data.playerDismountedEarly) {
                    NetworkHandle.INSTANCE.send(
                            PacketDistributor.ALL.noArg(),
                            new SyncVervaTravelPacket(player.getUUID(), false)
                    );
                    data.playerDismountedEarly = true;
                    player.sendSystemMessage(
                            Component.translatable("message.sporeadds.verva_transport.finished")
                                    .withStyle(net.minecraft.ChatFormatting.DARK_RED)
                    );
                }

                if (data.ticksElapsed >= 40) {
                    if (!data.playerDismountedEarly) {
                        player.stopRiding();
                        NetworkHandle.INSTANCE.send(
                                PacketDistributor.ALL.noArg(),
                                new SyncVervaTravelPacket(player.getUUID(), false)
                        );
                        player.sendSystemMessage(
                                Component.translatable("message.sporeadds.verva_transport.finished")
                                        .withStyle(net.minecraft.ChatFormatting.DARK_RED)
                        );
                    }

                    if (data.verva instanceof Verwa verwaEntity) {
                        verwaEntity.tickBurrowing();
                    }

                    data.state = 4;
                    data.ticksElapsed = 0;
                }
                continue;
            }

            if (data.state == 4) {
                if (!data.playerDismountedEarly && isPlayerTraveling(player.getUUID())) {
                    NetworkHandle.INSTANCE.send(
                            PacketDistributor.ALL.noArg(),
                            new SyncVervaTravelPacket(player.getUUID(), false)
                    );
                }

                if (data.ticksElapsed >= 60) {
                    if (data.verva != null) {
                        unregisterTransportVerva(data.verva.getUUID());
                        data.verva.discard();
                    }
                    iterator.remove();
                }
            }
        }
    }

    private static class TransportData {
        ServerPlayer player;
        Vec3 startPos;
        Vec3 targetPos;
        ServerLevel originLevel;
        ServerLevel targetLevel;
        Mob verva;
        int ticksElapsed;
        int state;
        int lastCountdownSecond;
        boolean appliedWaitingGlow;
        boolean playerDismountedEarly;
        UUID transportId;
        int priority;

        public TransportData(ServerPlayer player, Vec3 startPos, Vec3 targetPos, ServerLevel originLevel, ServerLevel targetLevel) {
            this.player = player;
            this.startPos = startPos;
            this.targetPos = targetPos;
            this.originLevel = originLevel;
            this.targetLevel = targetLevel;
            this.ticksElapsed = 0;
            this.state = 0;
            this.lastCountdownSecond = -1;
            this.appliedWaitingGlow = false;
            this.playerDismountedEarly = false;
            this.transportId = UUID.randomUUID();
            this.priority = 0;
        }
    }
}