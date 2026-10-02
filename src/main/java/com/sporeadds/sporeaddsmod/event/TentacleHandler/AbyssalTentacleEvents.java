package com.sporeadds.sporeaddsmod.event.TentacleHandler;

import net.neoforged.fml.common.EventBusSubscriber;

import com.sporeadds.sporeaddsmod.network.AbyssalTentaclePacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

@EventBusSubscriber(modid = "sporeadd", bus = EventBusSubscriber.Bus.GAME)
public class AbyssalTentacleEvents {

    private static final String SKIP_RESTORE_ON_LOGIN_TAG = "sporeadds_skip_tentacle_restore_on_login";

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        player.getPersistentData().putBoolean(SKIP_RESTORE_ON_LOGIN_TAG, true);
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        CompoundTag data = player.getPersistentData();

        if (data.getBoolean(SKIP_RESTORE_ON_LOGIN_TAG)) {
            data.remove(SKIP_RESTORE_ON_LOGIN_TAG);
            return;
        }

        AbyssalTentaclePacket.normalizeSlotStates(data);

        restoreSlotIfNeeded(data, 1);
        restoreSlotIfNeeded(data, 2);
        restoreSlotIfNeeded(data, 3);
    }

    private static void restoreSlotIfNeeded(CompoundTag data, int slot) {
        String state = AbyssalTentaclePacket.getSlotState(data, slot);

        if (AbyssalTentaclePacket.STATE_DEPLOYED.equals(state)
                || AbyssalTentaclePacket.STATE_RETURNING.equals(state)) {
            AbyssalTentaclePacket.setSlotState(data, slot, AbyssalTentaclePacket.STATE_READY);
        }
    }
}