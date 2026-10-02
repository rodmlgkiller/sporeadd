package com.sporeadds.sporeaddsmod.client;

import net.neoforged.neoforge.client.event.ClientTickEvent;

import net.neoforged.fml.common.EventBusSubscriber;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(modid = "sporeadd", value = Dist.CLIENT)
public final class CamouflageSoundHandler {

    private static final Map<Integer, Double> LAST_X = new HashMap<>();
    private static final Map<Integer, Double> LAST_Z = new HashMap<>();
    private static final Map<Integer, Integer> STEP_COOLDOWN = new HashMap<>();

    private CamouflageSoundHandler() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        BlockParticleOption grassParticle =
                new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SHORT_GRASS.defaultBlockState());

        for (var entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof AbstractClientPlayer player)) continue;
            if (!CamouflageClientState.isCamouflaged(player.getId())) continue;

            int id = player.getId();
            double dx = player.getX() - LAST_X.getOrDefault(id, player.getX());
            double dz = player.getZ() - LAST_Z.getOrDefault(id, player.getZ());
            double distMoved = Math.sqrt(dx * dx + dz * dz);

            LAST_X.put(id, player.getX());
            LAST_Z.put(id, player.getZ());

            int cooldown = STEP_COOLDOWN.getOrDefault(id, 0);
            if (cooldown > 0) {
                STEP_COOLDOWN.put(id, cooldown - 1);
                continue;
            }

            if (distMoved > 0.02 && player.onGround()) {
                mc.level.playLocalSound(
                        player.getX(), player.getY(), player.getZ(),
                        SoundEvents.GRASS_STEP,
                        SoundSource.PLAYERS,
                        0.5F,
                        0.9F + mc.level.random.nextFloat() * 0.2F,
                        false
                );

                for (int p = 0; p < 3; p++) {
                    mc.level.addParticle(
                            grassParticle,
                            player.getX() + (mc.level.random.nextDouble() - 0.5) * 0.6,
                            player.getY() + 0.2,
                            player.getZ() + (mc.level.random.nextDouble() - 0.5) * 0.6,
                            0.0, 0.02, 0.0
                    );
                }

                STEP_COOLDOWN.put(id, 5);
            }
        }
    }
}