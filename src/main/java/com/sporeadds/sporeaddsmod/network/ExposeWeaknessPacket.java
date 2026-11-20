package com.sporeadds.sporeaddsmod.network;

import com.sporeadds.sporeaddsmod.PlayerData.ScientistResearchProvider;
import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider;
import com.sporeadds.sporeaddsmod.config.ScientistConfig;
import com.sporeadds.sporeaddsmod.effects.effects;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import com.sporeadds.sporeaddsmod.network.NetworkEvent;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class ExposeWeaknessPacket {

    // Radius, effect duration and cooldown: see ScientistConfig (scientist.expose_weakness.*).
    private static final java.util.Map<java.util.UUID, Long> lastUseGameTime = new java.util.HashMap<>();

    private static final int WAVE_RING_COUNT = 3;
    private static final double WAVE_MAX_RADIUS = 2.5D;
    private static final int PARTICLES_PER_RING = 24;

    private static boolean isScientist(ServerPlayer player) {
        return SporeIdentifierProvider.SPORE_IDENTIFIER.get(player)
                .map(data -> "scientist".equalsIgnoreCase(data.getIdentifier()))
                .orElse(false);
    }

    private static boolean isKommandant(ServerPlayer player) {
        return SporeIdentifierProvider.SPORE_IDENTIFIER.get(player)
                .map(data -> "kommandant".equalsIgnoreCase(data.getIdentifier()))
                .orElse(false);
    }

    private static int computeAggregatedValue(net.minecraft.server.MinecraftServer server, String entityId) {
        int total = 0;
        int killValue = ScientistConfig.DAMAGE_KILL_VALUE_IN_DATA.get();

        for (ServerPlayer online : server.getPlayerList().getPlayers()) {
            if (!isScientist(online)) {
                continue;
            }

            var research = ScientistResearchProvider.SCIENTIST_RESEARCH.get(online).orElse(null);
            if (research == null) {
                continue;
            }

            total += research.getDataAmount(entityId) + (research.getKillCount(entityId) * killValue);
        }

        return total;
    }

    private static int computePlayerAggregatedValue(net.minecraft.server.MinecraftServer server) {
        int total = 0;
        int killValue = ScientistConfig.DAMAGE_KILL_VALUE_IN_DATA.get();

        for (ServerPlayer online : server.getPlayerList().getPlayers()) {
            if (!isScientist(online)) {
                continue;
            }

            var research = ScientistResearchProvider.SCIENTIST_RESEARCH.get(online).orElse(null);
            if (research == null) {
                continue;
            }

            for (int value : research.getAllKills().values()) {
                total += value * killValue;
            }

            total += research.getAllKills().keySet().stream()
                    .mapToInt(research::getDataAmount)
                    .sum();
        }

        return total;
    }

    public ExposeWeaknessPacket() {
    }

    public ExposeWeaknessPacket(FriendlyByteBuf buf) {
    }

    public void toBytes(FriendlyByteBuf buf) {
    }

    public static void handle(ExposeWeaknessPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) {
                return;
            }

            if (!isScientist(player)) {
                return;
            }

            final long cooldownTicks = ScientistConfig.exposeWeaknessCooldownTicks();
            final double range = ScientistConfig.exposeWeaknessRadius();
            final int effectDurationTicks = ScientistConfig.exposeWeaknessDurationTicks();

            long now = player.level().getGameTime();
            Long lastUse = lastUseGameTime.get(player.getUUID());

            if (lastUse != null && (now - lastUse) < cooldownTicks) {
                return;
            }

            lastUseGameTime.put(player.getUUID(), now);

            ScientistResearchProvider.SCIENTIST_RESEARCH.get(player).ifPresent(research -> {
                Map<String, Integer> kills = research.getAllKills();

                AABB searchBox = player.getBoundingBox().inflate(range);
                List<LivingEntity> nearby = player.level().getEntitiesOfClass(LivingEntity.class, searchBox);

                for (LivingEntity entity : nearby) {
                    if (entity == player) {
                        continue;
                    }

                    boolean isTargetKommandantPlayer = entity instanceof ServerPlayer targetPlayer
                            && isKommandant(targetPlayer);

                    String entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
                    boolean hasKilledBefore = kills.getOrDefault(entityId, 0) > 0;

                    boolean shouldApply = (hasKilledBefore || isTargetKommandantPlayer)
                            && player.distanceTo(entity) <= range;

                    if (!shouldApply) {
                        continue;
                    }

                    entity.addEffect(new MobEffectInstance(
                            effects.EXPOSED_WEAKNESS,
                            effectDurationTicks,
                            0
                    ));

                    int aggregatedValue = isTargetKommandantPlayer
                            ? computePlayerAggregatedValue(player.server)
                            : computeAggregatedValue(player.server, entityId);

                    com.sporeadds.sporeaddsmod.combat.WeakPointManager.registerOrRefresh(
                            entity, aggregatedValue, isTargetKommandantPlayer
                    );

                    com.sporeadds.sporeaddsmod.network.NetworkHandle.INSTANCE.send(
                            com.sporeadds.sporeaddsmod.network.PacketDistributor.TRACKING_ENTITY.with(() -> entity),
                            com.sporeadds.sporeaddsmod.combat.WeakPointManager.syncPacketFor(entity.getId())
                    );
                }
            });

            net.minecraft.sounds.SoundEvent sound = com.sporeadds.sporeaddsmod.ModSounds.DETECTION.get();

            player.level().playSound(
                    null,
                    player.getX(), player.getY(), player.getZ(),
                    sound,
                    SoundSource.PLAYERS,
                    1.0F,
                    1.0F
            );

            spawnWaveParticles(player);

            com.sporeadds.sporeaddsmod.network.NetworkHandle.INSTANCE.sendTo(
                    new SyncExposeWeaknessCooldownPacket((int) cooldownTicks),
                    player.connection.getConnection(),
                    com.sporeadds.sporeaddsmod.network.NetworkDirection.PLAY_TO_CLIENT
            );
        });
        context.setPacketHandled(true);
    }

    private static void spawnWaveParticles(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        DustParticleOptions cyanDust = new DustParticleOptions(
                new org.joml.Vector3f(0.0F, 1.0F, 1.0F),
                1.0F
        );

        double feetY = player.getY() + 0.05D;

        for (int ring = 0; ring < WAVE_RING_COUNT; ring++) {
            double radius = WAVE_MAX_RADIUS * ((double) (ring + 1) / WAVE_RING_COUNT);

            for (int i = 0; i < PARTICLES_PER_RING; i++) {
                double angle = (Math.PI * 2 * i) / PARTICLES_PER_RING;

                double offsetX = Math.cos(angle) * radius;
                double offsetZ = Math.sin(angle) * radius;

                serverLevel.sendParticles(
                        cyanDust,
                        player.getX() + offsetX,
                        feetY,
                        player.getZ() + offsetZ,
                        1,
                        0.0D, 0.0D, 0.0D,
                        0.0D
                );
            }
        }
    }
}