package com.sporeadds.sporeaddsmod.network;

import com.sporeadds.sporeaddsmod.util.BufUtil;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import com.sporeadds.sporeaddsmod.network.NetworkEvent;
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
            this.lines.add(BufUtil.readComponent(buf));
        }
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(lines.size());
        for (Component comp : lines) {
            BufUtil.writeComponent(buf, comp);
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