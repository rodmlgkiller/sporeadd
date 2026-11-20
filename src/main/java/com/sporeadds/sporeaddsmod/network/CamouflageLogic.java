package com.sporeadds.sporeaddsmod.network;

import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierData;
import com.sporeadds.sporeaddsmod.abilities.GhostDisguiseStacks;
import com.sporeadds.sporeaddsmod.config.GhostConfig;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class CamouflageLogic {

    public static final int INVISIBILITY_PULSE_TICKS = 20;

    /** Game time at which each currently disguised player's disguise started (server side, only used for the optional max duration). */
    private static final Map<UUID, Long> ACTIVE_SINCE = new HashMap<>();

    private CamouflageLogic() {
    }

    /** Ticks the player's current disguise has been on, measured against {@code now}. Starts counting if it was not being tracked. */
    public static long activeTicks(ServerPlayer player, long now) {
        Long since = ACTIVE_SINCE.get(player.getUUID());
        if (since == null) {
            ACTIVE_SINCE.put(player.getUUID(), now);
            return 0L;
        }
        return now - since;
    }

    public static void activate(ServerPlayer player, SporeIdentifierData data) {
        data.setCamouflaged(true);
        ACTIVE_SINCE.put(player.getUUID(), player.level().getGameTime());
        GhostDisguiseStacks.onDisguiseStart(player);

        applyInvisibilityPulse(player);

        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.playSound(
                    null,
                    player.getX(), player.getY(), player.getZ(),
                    SoundEvents.SWEET_BERRY_BUSH_PLACE,
                    SoundSource.PLAYERS,
                    0.8F, 0.9F
            );

            serverLevel.sendParticles(
                    new BlockParticleOption(ParticleTypes.BLOCK, Blocks.FERN.defaultBlockState()),
                    player.getX(), player.getY() + 1.0, player.getZ(),
                    30, 0.4, 0.6, 0.4, 0.05
            );
        }

        NetworkHandle.INSTANCE.send(
                PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                new SyncCamouflagePacket(player.getId(), true)
        );
    }

    public static void applyInvisibilityPulse(ServerPlayer player) {
        player.addEffect(new MobEffectInstance(
                MobEffects.INVISIBILITY,
                INVISIBILITY_PULSE_TICKS,
                0,
                true,
                false,
                false
        ));
    }

    public static void deactivate(ServerPlayer player, SporeIdentifierData data) {
        int cooldownTicks = GhostConfig.disguiseCooldownTicks();

        data.setCamouflaged(false);
        data.setCamouflageCooldown(cooldownTicks);
        ACTIVE_SINCE.remove(player.getUUID());
        GhostDisguiseStacks.onDisguiseEnd(player);

        player.removeEffect(MobEffects.INVISIBILITY);

        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.playSound(
                    null,
                    player.getX(), player.getY(), player.getZ(),
                    SoundEvents.GRASS_BREAK,
                    SoundSource.PLAYERS,
                    0.8F, 1.0F
            );

            serverLevel.sendParticles(
                    new BlockParticleOption(ParticleTypes.BLOCK, Blocks.TALL_GRASS.defaultBlockState()),
                    player.getX(), player.getY() + 1.0, player.getZ(),
                    25, 0.4, 0.6, 0.4, 0.08
            );
        }

        NetworkHandle.INSTANCE.send(
                PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                new SyncCamouflagePacket(player.getId(), false)
        );

        NetworkHandle.INSTANCE.send(
                PacketDistributor.PLAYER.with(() -> player),
                new SyncCamouflageCooldownPacket(cooldownTicks)
        );
    }

    public static void deactivateSilently(ServerPlayer player, SporeIdentifierData data) {
        data.setCamouflaged(false);
        ACTIVE_SINCE.remove(player.getUUID());
        GhostDisguiseStacks.onDisguiseEnd(player);
        player.removeEffect(MobEffects.INVISIBILITY);
    }
}