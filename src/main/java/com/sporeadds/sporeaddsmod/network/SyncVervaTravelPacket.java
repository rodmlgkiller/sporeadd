package com.sporeadds.sporeaddsmod.network;

import com.sporeadds.sporeaddsmod.commands.VervaTransportTask;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class SyncVervaTravelPacket {
    public final UUID playerUUID;
    public final boolean isTraveling;

    // Constructor general (El Servidor lo llama para enviar)
    public SyncVervaTravelPacket(UUID playerUUID, boolean isTraveling) {
        this.playerUUID = playerUUID;
        this.isTraveling = isTraveling;
    }

    // Constructor para decodificar (Cuando el Cliente lo recibe)
    public SyncVervaTravelPacket(FriendlyByteBuf buf) {
        this.playerUUID = buf.readUUID();
        this.isTraveling = buf.readBoolean();
    }

    // Codificar para enviar por red
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(playerUUID);
        buf.writeBoolean(isTraveling);
    }

    // Lo que hace el cliente cuando recibe el paquete
    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            // Esto se ejecuta estrictamente en el hilo del Cliente
            if (isTraveling) {
                VervaTransportTask.addClientTraveler(playerUUID);
            } else {
                VervaTransportTask.removeClientTraveler(playerUUID);
            }
        });
        context.setPacketHandled(true);
    }
}