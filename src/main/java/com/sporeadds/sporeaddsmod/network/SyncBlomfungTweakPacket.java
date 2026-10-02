package com.sporeadds.sporeaddsmod.network;

import com.sporeadds.sporeaddsmod.client.layer.KommandantBlockLayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class SyncBlomfungTweakPacket {

    private final UUID playerUuid;
    private final int id;
    private final String property;
    private final float value;

    public SyncBlomfungTweakPacket(UUID playerUuid, int id, String property, float value) {
        this.playerUuid = playerUuid;
        this.id = id;
        this.property = property;
        this.value = value;
    }

    public static void encode(SyncBlomfungTweakPacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.playerUuid);
        buf.writeInt(msg.id);
        buf.writeUtf(msg.property);
        buf.writeFloat(msg.value);
    }

    public static SyncBlomfungTweakPacket decode(FriendlyByteBuf buf) {
        return new SyncBlomfungTweakPacket(buf.readUUID(), buf.readInt(), buf.readUtf(), buf.readFloat());
    }

    public static void handle(SyncBlomfungTweakPacket msg, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        context.enqueueWork(() -> {
            // Buscamos o creamos la configuración específica del jugador que mandó el comando
            KommandantBlockLayer.BlomfungConfig[] configs = KommandantBlockLayer.playerConfigs
                    .computeIfAbsent(msg.playerUuid, k -> KommandantBlockLayer.getDefaultConfigs());

            int index = msg.id - 1;
            if (index >= 0 && index < configs.length) {
                KommandantBlockLayer.BlomfungConfig config = configs[index];

                switch (msg.property.toLowerCase()) {
                    case "offsetx" -> config.offsetX = msg.value;
                    case "offsety", "altura" -> config.offsetY = msg.value; // Acepta tanto offsety como altura
                    case "offsetz" -> config.offsetZ = msg.value;
                    case "rotx" -> config.rotX = msg.value;
                    case "roty" -> config.rotY = msg.value;
                    case "rotz" -> config.rotZ = msg.value;
                    case "scale", "escala" -> config.scale = msg.value; // Acepta tanto scale como escala
                }
            }
        });

        context.setPacketHandled(true);
    }
}