package com.sporeadds.sporeaddsmod.network;

import com.sporeadds.sporeaddsmod.client.screen.ImplantMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.NetworkEvent;
import net.neoforged.neoforge.network.NetworkHooks;

import java.util.UUID;
import java.util.function.Supplier;

public class OpenImplantGuiPacket {

    private final UUID targetPlayerUUID;

    public OpenImplantGuiPacket(UUID targetPlayerUUID) {
        this.targetPlayerUUID = targetPlayerUUID;
    }

    public OpenImplantGuiPacket(FriendlyByteBuf buf) {
        this.targetPlayerUUID = buf.readUUID();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(targetPlayerUUID);
    }

    public static OpenImplantGuiPacket decode(FriendlyByteBuf buf) {
        return new OpenImplantGuiPacket(buf);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer opener = ctx.getSender();
            if (opener != null) {
                // Encontrar el jugador objetivo
                ServerPlayer targetPlayer = opener.getServer().getPlayerList().getPlayer(targetPlayerUUID);

                if (targetPlayer != null) {
                    // Abrir GUI con el jugador objetivo
                    NetworkHooks.openScreen(opener, new ImplantMenuProvider(targetPlayer),
                            buf -> buf.writeUUID(targetPlayerUUID));
                } else {
                    opener.displayClientMessage(
                            Component.literal("§cTarget player not found or offline."),
                            true
                    );
                }
            }
        });
        return true;
    }

    // Menu Provider para crear el menu
    public static class ImplantMenuProvider implements net.minecraft.world.MenuProvider {
        private final ServerPlayer targetPlayer;

        public ImplantMenuProvider(ServerPlayer targetPlayer) {
            this.targetPlayer = targetPlayer;
        }

        @Override
        public Component getDisplayName() {
            return Component.literal("Implants: " + targetPlayer.getName().getString());
        }

        @Override
        public net.minecraft.world.inventory.AbstractContainerMenu createMenu(int windowId,
                                                                              net.minecraft.world.entity.player.Inventory playerInventory,
                                                                              net.minecraft.world.entity.player.Player player) {
            return new ImplantMenu(windowId, playerInventory, targetPlayer);
        }
    }
}
