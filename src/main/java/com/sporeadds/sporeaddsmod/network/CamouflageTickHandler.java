package com.sporeadds.sporeaddsmod.network;

import net.neoforged.neoforge.event.tick.ServerTickEvent;

import net.neoforged.fml.common.EventBusSubscriber;

import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider;
import com.sporeadds.sporeaddsmod.config.GhostConfig;
import com.sporeadds.sporeaddsmod.effects.effects;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

@EventBusSubscriber(modid = "sporeadd")
public final class CamouflageTickHandler {

    private static final int PULSE_INTERVAL_TICKS = 10;

    private CamouflageTickHandler() {
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {

        var server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;

        long gameTime = server.overworld().getGameTime();
        boolean shouldPulse = gameTime % PULSE_INTERVAL_TICKS == 0;

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            SporeIdentifierProvider.SPORE_IDENTIFIER.get(player).ifPresent(data -> {
                if (data.isCamouflageOnCooldown()) {
                    data.tickCamouflageCooldown();
                }

                if (shouldPulse && data.isCamouflaged()) {
                    CamouflageLogic.applyInvisibilityPulse(player);
                }

                if (data.isCamouflaged()) {
                    int maxDurationTicks = GhostConfig.disguiseMaxDurationTicks();
                    if (maxDurationTicks > 0 && CamouflageLogic.activeTicks(player, gameTime) >= maxDurationTicks) {
                        CamouflageLogic.deactivate(player, data);
                    }
                }
            });
        }
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        SporeIdentifierProvider.SPORE_IDENTIFIER.get(player).ifPresent(data -> {
            if (data.isCamouflaged()) {
                CamouflageLogic.deactivate(player, data);

                boolean damagedByOtherEntity = event.getSource().getEntity() instanceof LivingEntity attacker
                        && attacker != player;

                int ambushedDurationTicks = GhostConfig.ambushedDurationTicks();

                if (damagedByOtherEntity && ambushedDurationTicks > 0) {
                    player.addEffect(new MobEffectInstance(
                            effects.AMBUSHED,
                            ambushedDurationTicks,
                            0,
                            false,
                            true,
                            true
                    ));
                }
            }
        });
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        SporeIdentifierProvider.SPORE_IDENTIFIER.get(player).ifPresent(data -> {
            if (data.isCamouflaged()) {
                CamouflageLogic.deactivateSilently(player, data);
            }
        });
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        SporeIdentifierProvider.SPORE_IDENTIFIER.get(player).ifPresent(data -> {
            if (data.isCamouflaged()) {
                CamouflageLogic.deactivateSilently(player, data);
            }
        });
    }
}