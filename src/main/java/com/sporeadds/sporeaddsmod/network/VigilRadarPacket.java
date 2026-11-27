package com.sporeadds.sporeaddsmod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import com.sporeadds.sporeaddsmod.client.ClientRadarHUD;

public class VigilRadarPacket {
    private final List<Component> lines;

    public VigilRadarPacket(List<Component> lines) {
        this.lines = lines;
    }

    public VigilRadarPacket(FriendlyByteBuf buf) {
        this.lines = new ArrayList<>();
        int size = buf.readInt();
        for (int i = 0; i < size; i++) {
            this.lines.add(buf.readComponent());
        }
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(lines.size());
        for (Component comp : lines) {
            buf.writeComponent(comp);
        }
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            // Se muestra durante 300 ticks (15 segundos)
            ClientRadarHUD.setReport(lines, 300);
        });
        context.setPacketHandled(true);
    }
}