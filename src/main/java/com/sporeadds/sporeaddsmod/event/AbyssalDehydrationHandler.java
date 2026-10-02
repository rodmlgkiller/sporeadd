package com.sporeadds.sporeaddsmod.event;

import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import net.neoforged.fml.common.EventBusSubscriber;

import com.sporeadds.sporeaddsmod.PlayerData.PlayerDataProvider;
import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider;
import com.sporeadds.sporeaddsmod.effects.DehydrationEffect;
import com.sporeadds.sporeaddsmod.effects.effects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = "sporeadd", bus = EventBusSubscriber.Bus.GAME)
public class AbyssalDehydrationHandler {

    private static final Map<UUID, Integer> OUT_OF_WATER_TICKS = new HashMap<>();
    private static final Set<UUID> INTERNAL_REAPPLY = new HashSet<>();

    private static final int TICKS_FOR_AMP_0 = 30 * 20;
    private static final int TICKS_FOR_AMP_1 = 59 * 20;
    private static final int TICKS_FOR_AMP_2 = 88 * 20;

    private static final int EFFECT_DURATION = 30 * 20;

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }

        Player player = event.getEntity();
        UUID playerId = player.getUUID();

        boolean isAbyssal = player.getCapability(SporeIdentifierProvider.SPORE_IDENTIFIER)
                .map(data -> "kommandant".equals(data.getIdentifier()) && "abyssal".equals(data.getSubclass()))
                .orElse(false);

        if (!isAbyssal) {
            clearAllTimers(playerId);
            INTERNAL_REAPPLY.remove(playerId);
            return;
        }

        boolean hasArmorHpActive = player.getCapability(PlayerDataProvider.PLAYER_DATA)
                .map(data -> data.getArmorHp() > 0)
                .orElse(false);

        if (hasArmorHpActive) {
            clearAllTimers(playerId);
            MobEffectInstance current = player.getEffect(effects.DEHYDRATION.get());
            if (current != null) {
                player.removeEffect(effects.DEHYDRATION.get());
            }
            return;
        }

        Level level = player.level();
        BlockPos feetPos = player.blockPosition();
        BlockPos bodyPos = BlockPos.containing(player.getX(), player.getBoundingBox().minY + 0.5D, player.getZ());

        boolean inRain = level.isRaining() && level.canSeeSky(feetPos) && level.getBiome(feetPos).value().getPrecipitationAt(feetPos) == Biome.Precipitation.RAIN;
        boolean inCauldron = isWaterCauldronAt(level, feetPos) || isWaterCauldronAt(level, bodyPos);

        boolean inWaterSource = false;
        BlockPos sourcePosToConsume = null;

        if (isWaterSourceAt(level, bodyPos)) {
            inWaterSource = true;
            sourcePosToConsume = bodyPos;
        } else if (isWaterSourceAt(level, feetPos)) {
            inWaterSource = true;
            sourcePosToConsume = feetPos;
        }

        // Si toca agua fuente real, lluvia o calderos
        if (inRain || inCauldron || inWaterSource) {
            clearAllTimers(playerId);
            MobEffectInstance current = player.getEffect(effects.DEHYDRATION.get());

            if (current != null) {
                // Sólo seca el bloque si es agua fuente Y el jugador tenía el efecto
                if (inWaterSource && sourcePosToConsume != null) {
                    level.setBlock(sourcePosToConsume, Blocks.AIR.defaultBlockState(), 3);
                }
                player.removeEffect(effects.DEHYDRATION.get());
            }
            return;
        }

        int ticksOutside = OUT_OF_WATER_TICKS.getOrDefault(playerId, 0) + 1;

        if (ticksOutside > TICKS_FOR_AMP_2 + 20) {
            ticksOutside = TICKS_FOR_AMP_2 + 20;
        }

        OUT_OF_WATER_TICKS.put(playerId, ticksOutside);

        spawnDryingParticles(player, ticksOutside);

        if (ticksOutside == TICKS_FOR_AMP_0) {
            applyDehydration(player, 0);
        } else if (ticksOutside == TICKS_FOR_AMP_1) {
            applyDehydration(player, 1);
        } else if (ticksOutside >= TICKS_FOR_AMP_2) {
            MobEffectInstance current = player.getEffect(effects.DEHYDRATION.get());
            if (current == null || current.getAmplifier() < 2 || current.getDuration() <= 20) {
                applyDehydration(player, 2);
            }
        }
    }

    @SubscribeEvent
    public static void onEffectRemoved(MobEffectEvent.Remove event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }

        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (event.getEffect() != effects.DEHYDRATION.get()) {
            return;
        }

        UUID playerId = player.getUUID();

        if (INTERNAL_REAPPLY.remove(playerId)) {
            return;
        }

        clearAllTimers(playerId);
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID playerId = event.getEntity().getUUID();
        clearAllTimers(playerId);
        INTERNAL_REAPPLY.remove(playerId);
    }

    public static void resetHydrationTimers(Player player) {
        clearAllTimers(player.getUUID());
    }

    private static void clearAllTimers(UUID playerId) {
        OUT_OF_WATER_TICKS.put(playerId, 0);
    }

    private static void applyDehydration(Player player, int amplifier) {
        UUID playerId = player.getUUID();
        MobEffectInstance current = player.getEffect(effects.DEHYDRATION.get());

        if (current != null) {
            int currentAmp = current.getAmplifier();
            int currentDuration = current.getDuration();

            if (currentAmp > amplifier) {
                return;
            }

            if (currentAmp == amplifier && currentDuration > 20) {
                return;
            }

            INTERNAL_REAPPLY.add(playerId);
            DehydrationEffect.markNextRemovalSilent(player);
            player.removeEffect(effects.DEHYDRATION.get());
        }

        player.addEffect(new MobEffectInstance(
                effects.DEHYDRATION.get(),
                EFFECT_DURATION,
                amplifier,
                false,
                false,
                true
        ));
    }

    private static void spawnDryingParticles(Player player, int ticksOutside) {
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        if (player.tickCount % 6 != 0) {
            return;
        }

        int count;
        if (ticksOutside < TICKS_FOR_AMP_0) {
            count = 1;
        } else if (ticksOutside < TICKS_FOR_AMP_1) {
            count = 2;
        } else {
            count = 3;
        }

        double width = player.getBbWidth() * 0.6D;
        double height = player.getBbHeight();

        for (int i = 0; i < count; i++) {
            double x = player.getX() + (player.getRandom().nextDouble() - 0.5D) * width;
            double y = player.getY() + 0.2D + player.getRandom().nextDouble() * (height * 0.8D);
            double z = player.getZ() + (player.getRandom().nextDouble() - 0.5D) * width;

            serverLevel.sendParticles(
                    ParticleTypes.DRIPPING_WATER,
                    x, y, z,
                    1,
                    0.0D, 0.0D, 0.0D,
                    0.0D
            );

            if (ticksOutside >= TICKS_FOR_AMP_1 && player.getRandom().nextFloat() < 0.35F) {
                serverLevel.sendParticles(
                        ParticleTypes.FALLING_WATER,
                        x, y - 0.15D, z,
                        1,
                        0.02D, 0.0D, 0.02D,
                        0.0D
                );
            }
        }
    }

    private static boolean isWaterSourceAt(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        FluidState fluidState = level.getFluidState(pos);
        return state.is(Blocks.WATER) && fluidState.is(FluidTags.WATER) && fluidState.isSource();
    }

    private static boolean isWaterCauldronAt(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.is(Blocks.WATER_CAULDRON)) {
            if (state.hasProperty(LayeredCauldronBlock.LEVEL)) {
                return state.getValue(LayeredCauldronBlock.LEVEL) > 0;
            }
            return true;
        }
        return false;
    }
}