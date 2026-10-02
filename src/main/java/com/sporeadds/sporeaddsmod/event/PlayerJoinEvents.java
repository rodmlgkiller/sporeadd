package com.sporeadds.sporeaddsmod.event;

import com.sporeadds.sporeaddsmod.util.ItemNbt;

import net.neoforged.fml.common.EventBusSubscriber;

import com.sporeadds.sporeaddsmod.network.NetworkHandle;
import com.sporeadds.sporeaddsmod.network.SyncMoundCountPacket;
import com.sporeadds.sporeaddsmod.spore.PlayerSporeProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import com.sporeadds.sporeaddsmod.network.PacketDistributor;

@EventBusSubscriber(modid = "sporeaddsmod")
public class PlayerJoinEvents {

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        removeAbyssalTempTridents(player);

        PlayerSporeProvider.PLAYER_CAP.get(player).ifPresent(spore -> {
            spore.getMoundRegistry().cleanupDeadOrMissingMounds(player.serverLevel());
            CompoundTag nbt = new CompoundTag();
            spore.saveNBTData(nbt);
            NetworkHandle.INSTANCE.send(
                    PacketDistributor.PLAYER.with(() -> player),
                    new SyncMoundCountPacket(nbt)
            );
        });
    }

    private static void removeAbyssalTempTridents(ServerPlayer player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (isAbyssalTempTrident(stack)) {
                player.getInventory().setItem(i, ItemStack.EMPTY);
            }
        }

        for (int i = 0; i < player.getInventory().offhand.size(); i++) {
            ItemStack stack = player.getInventory().offhand.get(i);
            if (isAbyssalTempTrident(stack)) {
                player.getInventory().offhand.set(i, ItemStack.EMPTY);
            }
        }

        for (int i = 0; i < player.getInventory().armor.size(); i++) {
            ItemStack stack = player.getInventory().armor.get(i);
            if (isAbyssalTempTrident(stack)) {
                player.getInventory().armor.set(i, ItemStack.EMPTY);
            }
        }

        player.getInventory().setChanged();
        player.inventoryMenu.broadcastChanges();
    }

    private static boolean isAbyssalTempTrident(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (!ItemNbt.hasTag(stack)) return false;
        return ItemNbt.getTag(stack).getBoolean("AbyssalTempTrident");
    }
}