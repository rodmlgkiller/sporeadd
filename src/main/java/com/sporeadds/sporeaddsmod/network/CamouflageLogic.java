package com.sporeadds.sporeaddsmod.network;

import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierData;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.Blocks;
import com.sporeadds.sporeaddsmod.network.PacketDistributor;

public final class CamouflageLogic {

    public static final int INVISIBILITY_PULSE_TICKS = 20;

    private CamouflageLogic() {
    }

    public static void activate(ServerPlayer player, SporeIdentifierData data) {
        data.setCamouflaged(true);

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
        data.setCamouflaged(false);
        data.setCamouflageCooldown(ActivateCamouflagePacket.CAMOUFLAGE_COOLDOWN_TICKS);

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
                new SyncCamouflageCooldownPacket(ActivateCamouflagePacket.CAMOUFLAGE_COOLDOWN_TICKS)
        );
    }

    public static void deactivateSilently(ServerPlayer player, SporeIdentifierData data) {
        data.setCamouflaged(false);
        player.removeEffect(MobEffects.INVISIBILITY);
    }
}