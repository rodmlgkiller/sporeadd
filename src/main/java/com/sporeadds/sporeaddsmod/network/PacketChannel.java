package com.sporeadds.sporeaddsmod.network;

import net.minecraft.network.Connection;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Replacement for the old Forge SimpleChannel API: wraps the mod's packet objects in NeoForge payloads. */
public final class PacketChannel {

    private final Map<Class<?>, CustomPacketPayload.Type<PacketWrapper>> types = new ConcurrentHashMap<>();

    void bind(Class<?> packetClass, CustomPacketPayload.Type<PacketWrapper> type) {
        types.put(packetClass, type);
    }

    private PacketWrapper wrap(Object message) {
        CustomPacketPayload.Type<PacketWrapper> type = types.get(message.getClass());
        if (type == null) {
            throw new IllegalStateException("Packet not registered: " + message.getClass().getName());
        }
        return new PacketWrapper(type, message);
    }

    public void send(PacketDistributor.Target target, Object message) {
        target.send(wrap(message));
    }

    public void sendToServer(Object message) {
        com.sporeadds.sporeaddsmod.network.PacketDistributor.sendToServer(wrap(message));
    }

    public void sendTo(Object message, Connection connection, NetworkDirection direction) {
        PacketWrapper wrapped = wrap(message);
        if (direction == NetworkDirection.PLAY_TO_CLIENT) {
            connection.send(new ClientboundCustomPayloadPacket(wrapped));
        } else {
            connection.send(new ServerboundCustomPayloadPacket(wrapped));
        }
    }
}
