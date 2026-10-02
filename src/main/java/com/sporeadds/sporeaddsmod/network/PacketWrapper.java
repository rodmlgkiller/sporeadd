package com.sporeadds.sporeaddsmod.network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Generic payload carrying one of the mod's legacy packet objects; each packet class has its own Type/id. */
public record PacketWrapper(CustomPacketPayload.Type<PacketWrapper> type, Object message) implements CustomPacketPayload {
}
