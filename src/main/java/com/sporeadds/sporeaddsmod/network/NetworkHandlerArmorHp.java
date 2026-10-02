package com.sporeadds.sporeaddsmod.network;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.NetworkRegistry;
import net.neoforged.neoforge.network.simple.SimpleChannel;

public class NetworkHandlerArmorHp {

    private static final String PROTOCOL_VERSION = "1.0";
    private static SimpleChannel CHANNEL; // <-- NO más static final

    private static int packetId = 0;
    private static int nextId() { return packetId++; }

    // Debe llamarse SOLO 1 vez en el setup (FMLCommonSetupEvent)
    public static void register() {
        if (CHANNEL != null) return; // Evita doble inicialización

        CHANNEL = NetworkRegistry.newSimpleChannel(
                ResourceLocation.fromNamespaceAndPath("sporeadds", "armorhp"),
                () -> PROTOCOL_VERSION,
                PROTOCOL_VERSION::equals,
                PROTOCOL_VERSION::equals
        );

        CHANNEL.registerMessage(
                nextId(),
                ArmorHpSyncPacket.class,
                ArmorHpSyncPacket::encode,
                ArmorHpSyncPacket::decode,
                ArmorHpSyncPacket::handle
        );
    }

    public static SimpleChannel getChannel() {
        if (CHANNEL == null)
            throw new IllegalStateException("Canal de ArmorHp no inicializado: llama a NetworkHandlerArmorHp.register() en el setup mod.");
        return CHANNEL;
    }
}
