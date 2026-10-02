package com.sporeadds.sporeaddsmod.network;

import com.sporeadds.sporeaddsmod.client.layer.KommandantSpriteLayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class SyncPelletTweakPacket {
    private final UUID playerUuid;
    private final int id;
    private final String property;
    private final float value;

    public SyncPelletTweakPacket(UUID playerUuid, int id, String property, float value) {
        this.playerUuid = playerUuid;
        this.id = id;
        this.property = property;
        this.value = value;
    }

    public static void encode(SyncPelletTweakPacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.playerUuid);
        buf.writeInt(msg.id);
        buf.writeUtf(msg.property);
        buf.writeFloat(msg.value);
    }

    public static SyncPelletTweakPacket decode(FriendlyByteBuf buf) {
        return new SyncPelletTweakPacket(buf.readUUID(), buf.readInt(), buf.readUtf(), buf.readFloat());
    }

    public static void handle(SyncPelletTweakPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // Buscamos los pellets del jugador y le pasamos el UUID al generador de defaults
            KommandantSpriteLayer.PelletConfig[] configs = KommandantSpriteLayer.playerPellets
                    .computeIfAbsent(msg.playerUuid, k -> KommandantSpriteLayer.getDefaultPellets(msg.playerUuid));

            int index = msg.id - 1; // ID 1 es índice 0
            if (index >= 0 && index < configs.length) {
                KommandantSpriteLayer.PelletConfig p = configs[index];

                switch (msg.property.toLowerCase()) {
                    case "offsetx" -> p.offsetX = msg.value;
                    case "offsety" -> p.offsetY = msg.value;
                    case "offsetz" -> p.offsetZ = msg.value;
                    case "rotx" -> p.rotX = msg.value;
                    case "roty" -> p.rotY = msg.value;
                    case "rotz" -> p.rotZ = msg.value;
                    case "scale" -> p.scale = msg.value;
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}