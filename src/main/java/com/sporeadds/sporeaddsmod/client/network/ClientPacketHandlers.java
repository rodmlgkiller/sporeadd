package com.sporeadds.sporeaddsmod.client.network;

import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider;
import com.sporeadds.sporeaddsmod.client.gui.VervaGuiScreen;
import com.sporeadds.sporeaddsmod.client.renderer.ClientgluttonousCrosshairRenderState;
import com.sporeadds.sporeaddsmod.network.SyncgluttonousCrosshairRenderPacket;
import com.sporeadds.sporeaddsmod.network.SyncSporeIdentifierPacket;
import com.sporeadds.sporeaddsmod.network.SyncVervaGuiPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class ClientPacketHandlers {

    public static void handleSyncVervaGui(SyncVervaGuiPacket msg) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.setScreen(new VervaGuiScreen(msg.verwaMenu, msg.organoidMenu, msg.bombMenu));
        }
    }

    public static void handleSyncSporeIdentifier(SyncSporeIdentifierPacket msg) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        Entity entity = mc.level.getEntity(msg.getPlayerId());
        if (!(entity instanceof Player player)) return;

        SporeIdentifierProvider.SPORE_IDENTIFIER.get(player).ifPresent(data -> {
            data.setIdentifier(msg.getIdentifier());
            data.setSubclass(msg.getSubclass());
        });
    }

    public static void handleSyncgluttonousCrosshairRender(SyncgluttonousCrosshairRenderPacket msg) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        Entity entity = mc.level.getEntity(msg.getPlayerId());
        if (!(entity instanceof Player)) return;

        ClientgluttonousCrosshairRenderState.mark(msg.getPlayerId());
    }
    public static void handleSyncgluttonousCrosshairAttack(com.sporeadds.sporeaddsmod.network.SyncgluttonousCrosshairAttackPacket msg) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        Entity entity = mc.level.getEntity(msg.getPlayerId());
        if (!(entity instanceof Player)) return;

        ClientgluttonousCrosshairRenderState.markAttack(msg.getPlayerId());
    }
}