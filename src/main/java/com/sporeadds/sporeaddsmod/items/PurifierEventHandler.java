package com.sporeadds.sporeaddsmod.items;

import net.neoforged.neoforge.event.tick.LevelTickEvent;

import net.neoforged.fml.common.EventBusSubscriber;

import com.Harbinger.Spore.core.Sparticles;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

@EventBusSubscriber(modid = "sporeadd")
public class PurifierEventHandler {

    private static final CopyOnWriteArrayList<AbsorptionInstance> ABSORPTIONS = new CopyOnWriteArrayList<>();
    private static final int ABSORPTION_DURATION_TICKS = 20;

    public static void startAbsorption(ServerLevel level, double startX, double startY, double startZ,
                                       UUID targetId, boolean overcharged, int xpAmount) {
        ABSORPTIONS.add(new AbsorptionInstance(
                level.dimension().location().toString(),
                startX, startY, startZ,
                targetId,
                ABSORPTION_DURATION_TICKS,
                ABSORPTION_DURATION_TICKS,
                overcharged,
                xpAmount
        ));
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {

        Level rawLevel = event.getLevel();
        if (!(rawLevel instanceof ServerLevel level)) {
            return;
        }

        String currentDimensionId = level.dimension().location().toString();

        for (AbsorptionInstance absorption : ABSORPTIONS) {
            if (!absorption.dimensionId.equals(currentDimensionId)) {
                continue;
            }

            Player player = level.getPlayerByUUID(absorption.targetId);
            if (player == null || !player.isAlive()) {
                ABSORPTIONS.remove(absorption);
                continue;
            }

            float progress = 1.0F - (float) absorption.ticksLeft / (float) absorption.totalTicks;

            double endX = player.getX();
            double endY = player.getY() + player.getBbHeight() * 0.6;
            double endZ = player.getZ();

            double currentX = absorption.startX + (endX - absorption.startX) * progress;
            double currentY = absorption.startY + (endY - absorption.startY) * progress;
            double currentZ = absorption.startZ + (endZ - absorption.startZ) * progress;

            if (!absorption.overcharged) {
                level.sendParticles((SimpleParticleType) Sparticles.SPORE_PARTICLE.get(),
                        currentX, currentY, currentZ,
                        6, 0.36, 0.36, 0.36, 0.0);
            } else {
                level.sendParticles((SimpleParticleType) Sparticles.SPORE_PARTICLE.get(),
                        currentX, currentY, currentZ,
                        4, 0.36, 0.36, 0.36, 0.0);
                level.sendParticles((SimpleParticleType) Sparticles.BLOOD_PARTICLE.get(),
                        currentX, currentY, currentZ,
                        2, 0.36, 0.36, 0.36, 0.0);
            }

            absorption.ticksLeft--;

            if (absorption.ticksLeft <= 0 || player.distanceToSqr(currentX, currentY, currentZ) < 1.0D) {
                int totalXp = absorption.overcharged ? absorption.xpAmount * 3 : absorption.xpAmount;

                if (player instanceof ServerPlayer serverPlayer) {
                    serverPlayer.playNotifySound(
                            SoundEvents.EXPERIENCE_ORB_PICKUP,
                            SoundSource.MASTER,
                            1.0F,
                            1.2F + level.random.nextFloat() * 0.4F
                    );
                }

                spawnMaxXpOrbs(level, currentX, currentY, currentZ, totalXp);
                ABSORPTIONS.remove(absorption);
            }
        }
    }

    private static void spawnMaxXpOrbs(ServerLevel level, double x, double y, double z, int totalXp) {
        for (int i = 0; i < totalXp; i++) {
            level.addFreshEntity(new ExperienceOrb(level, x, y, z, 1));
        }
    }

    private static class AbsorptionInstance {
        private final String dimensionId;
        private final double startX;
        private final double startY;
        private final double startZ;
        private final UUID targetId;
        private final int totalTicks;
        private int ticksLeft;
        private final boolean overcharged;
        private final int xpAmount;

        private AbsorptionInstance(String dimensionId, double startX, double startY, double startZ,
                                   UUID targetId, int totalTicks, int ticksLeft,
                                   boolean overcharged, int xpAmount) {
            this.dimensionId = dimensionId;
            this.startX = startX;
            this.startY = startY;
            this.startZ = startZ;
            this.targetId = targetId;
            this.totalTicks = totalTicks;
            this.ticksLeft = ticksLeft;
            this.overcharged = overcharged;
            this.xpAmount = xpAmount;
        }
    }
}