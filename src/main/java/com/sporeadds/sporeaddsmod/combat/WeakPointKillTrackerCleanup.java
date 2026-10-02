package com.sporeadds.sporeaddsmod.combat;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;;

@Mod.EventBusSubscriber(modid = "sporeadd")
public final class WeakPointKillTrackerCleanup {

    private WeakPointKillTrackerCleanup() {
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

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