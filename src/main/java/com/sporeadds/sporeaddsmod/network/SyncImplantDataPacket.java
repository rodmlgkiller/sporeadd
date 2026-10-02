package com.sporeadds.sporeaddsmod.network;

import com.sporeadds.sporeaddsmod.capabilities.PlayerImplantsCapability;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class SyncImplantDataPacket {

    private final UUID targetPlayerUUID;
    private final CompoundTag implantData;

    public SyncImplantDataPacket(UUID targetPlayerUUID, CompoundTag implantData) {
        this.targetPlayerUUID = targetPlayerUUID;
        this.implantData = implantData;
    }

    public SyncImplantDataPacket(FriendlyByteBuf buf) {
        this.targetPlayerUUID = buf.readUUID();
        this.implantData = buf.readNbt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(targetPlayerUUID);
        buf.writeNbt(implantData);
    }

    public static SyncImplantDataPacket decode(FriendlyByteBuf buf) {
        return new SyncImplantDataPacket(buf);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        // Deshabilitado a propósito: este paquete permitía que un cliente sobrescribiera los
        // implantes (capability) de CUALQUIER jugador con datos arbitrarios. No se envía desde
        // ningún sitio; se mantiene la clase solo para no alterar los IDs de paquete.
        supplier.get().setPacketHandled(true);
        return true;
    }
}