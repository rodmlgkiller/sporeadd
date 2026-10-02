package com.sporeadds.sporeaddsmod.combat;

import net.neoforged.neoforge.event.tick.ServerTickEvent;

import net.neoforged.fml.common.EventBusSubscriber;

import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.server.ServerLifecycleHooks;;

@EventBusSubscriber(modid = "sporeadd")
public final class WeakPointKillTrackerCleanup {

    private WeakPointKillTrackerCleanup() {
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {

        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return;
        }

        long gameTime = server.overworld().getGameTime();

        if (gameTime % 100 == 0) {
            WeakPointKillTracker.purgeStale(gameTime);
        }
    }
}