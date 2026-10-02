package com.sporeadds.sporeaddsmod.Powers;

import com.sporeadds.sporeaddsmod.config.SporeAddsConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderBlockScreenEffectEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

public class AdaptedPhysiologyEvents {

    private static final String PHASING_TAG = "SporePhasingState";
    private static final String NO_FALL_TAG = "SporeNoFallTicks";
    private static final String FLIGHT_GRANTED_TAG = "SporeGrantedFlight";
    private static final String PHASING_BLINDNESS_TAG = "SporePhasingBlindnessApplied";

    @Mod.EventBusSubscriber(modid = "sporeadd", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ClientEvents {

        @SubscribeEvent
        public static void onBlockOverlay(RenderBlockScreenEffectEvent event) {
            if (event.getOverlayType() != RenderBlockScreenEffectEvent.OverlayType.BLOCK) {
                return;
            }

            Player player = event.getPlayer();
            if (player == null) {
                return;
            }

            BlockPos eyePos = BlockPos.containing(player.getEyePosition());
            BlockState state = player.level().getBlockState(eyePos);

            if (ForgeRegistries.BLOCKS.getKey(state.getBlock()) == null) {
                return;
            }

            String blockId = ForgeRegistries.BLOCKS.getKey(state.getBlock()).toString();

            if (AdaptedPhysiologyPower.isPhaseableBlock(blockId) && AdaptedPhysiologyPower.canPhase(player)) {
                event.setCanceled(true);
            }
        }
    }

    @Mod.EventBusSubscriber(modid = "sporeadd", bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ServerEvents {

        @SubscribeEvent
        public static void onLivingFall(LivingFallEvent event) {
            if (!(event.getEntity() instanceof Player player)) {
                return;
            }

            if (player.getPersistentData().getInt(NO_FALL_TAG) > 0) {
                event.setCanceled(true);
                event.setDistance(0.0F);
                player.fallDistance = 0.0F;
            }
        }

        @SubscribeEvent
        public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
            if (event.phase != TickEvent.Phase.END) {
                return;
            }

            Player player = event.player;
            if (player.level().isClientSide) {
                return;
            }

            CompoundTag data = player.getPersistentData();

            int noFallTicks = data.getInt(NO_FALL_TAG);
            if (noFallTicks > 0) {
                data.putInt(NO_FALL_TAG, noFallTicks - 1);
                player.fallDistance = 0.0F;
            }

            boolean canPhase = AdaptedPhysiologyPower.canPhase(player);
            boolean isInsideBiomass = canPhase && isPlayerInsidePhaseableBlock(player);

            data.putBoolean(PHASING_TAG, isInsideBiomass);

            // Verificamos si es creativo o espectador
            boolean isCreativeOrSpectator = player.isCreative() || player.isSpectator();

            if (isInsideBiomass) {
                player.fallDistance = 0.0F;

                // Solo damos mayfly/flying si NO es creativo ni espectador
                if (!data.getBoolean(FLIGHT_GRANTED_TAG)) {
                    data.putBoolean(FLIGHT_GRANTED_TAG, true);
                    if (!isCreativeOrSpectator) {
                        player.getAbilities().mayfly = true;
                        player.getAbilities().flying = true;
                        player.onUpdateAbilities();
                    }
                }

                if (SporeAddsConfig.PHASING_APPLIES_BLINDNESS.get()) {
                    MobEffectInstance current = player.getEffect(MobEffects.BLINDNESS);

                    if (current == null || current.getDuration() <= 40) {
                        boolean applied = player.addEffect(
                                new MobEffectInstance(MobEffects.BLINDNESS, 60, 0, false, false, false)
                        );

                        if (applied || current != null) {
                            data.putBoolean(PHASING_BLINDNESS_TAG, true);
                        }
                    } else {
                        data.putBoolean(PHASING_BLINDNESS_TAG, true);
                    }
                } else if (data.getBoolean(PHASING_BLINDNESS_TAG)) {
                    data.putBoolean(PHASING_BLINDNESS_TAG, false);
                }

            } else {
                if (data.getBoolean(FLIGHT_GRANTED_TAG)) {
                    data.putBoolean(FLIGHT_GRANTED_TAG, false);

                    // Solo quitamos mayfly/flying si NO es creativo ni espectador
                    if (!isCreativeOrSpectator) {
                        player.getAbilities().flying = false;
                        player.getAbilities().mayfly = false;
                        player.onUpdateAbilities();
                    }
                }

                if (data.getBoolean(PHASING_BLINDNESS_TAG)) {
                    MobEffectInstance current = player.getEffect(MobEffects.BLINDNESS);

                    if (current != null && current.getAmplifier() == 0) {
                        player.removeEffect(MobEffects.BLINDNESS);
                    }

                    data.putBoolean(PHASING_BLINDNESS_TAG, false);
                }
            }
        }

        private static boolean isPlayerInsidePhaseableBlock(Player player) {
            AABB box = player.getBoundingBox().deflate(0.1D);
            BlockPos min = BlockPos.containing(box.minX, box.minY, box.minZ);
            BlockPos max = BlockPos.containing(box.maxX, box.maxY, box.maxZ);

            for (int x = min.getX(); x <= max.getX(); x++) {
                for (int y = min.getY(); y <= max.getY(); y++) {
                    for (int z = min.getZ(); z <= max.getZ(); z++) {
                        BlockPos pos = new BlockPos(x, y, z);
                        BlockState state = player.level().getBlockState(pos);

                        if (ForgeRegistries.BLOCKS.getKey(state.getBlock()) == null) {
                            continue;
                        }

                        String blockId = ForgeRegistries.BLOCKS.getKey(state.getBlock()).toString();
                        if (AdaptedPhysiologyPower.isPhaseableBlock(blockId)) {
                            return true;
                        }
                    }
                }
            }

            return false;
        }
    }
}