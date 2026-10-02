package com.sporeadds.sporeaddsmod.network;

import com.sporeadds.sporeaddsmod.client.network.ClientPacketHandlers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;


import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class SyncVervaGuiPacket {
    public final List<String> verwaMenu;
    public final List<String> organoidMenu;
    public final List<String> bombMenu;

    public SyncVervaGuiPacket(List<String> verwaMenu, List<String> organoidMenu, List<String> bombMenu) {
        this.verwaMenu = verwaMenu;
        this.organoidMenu = organoidMenu;
        this.bombMenu = bombMenu;
    }

    public static void encode(SyncVervaGuiPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.verwaMenu.size());
        for (String s : msg.verwaMenu) buf.writeUtf(s);

        buf.writeInt(msg.organoidMenu.size());
        for (String s : msg.organoidMenu) buf.writeUtf(s);

        buf.writeInt(msg.bombMenu.size());
        for (String s : msg.bombMenu) buf.writeUtf(s);
    }

    public static SyncVervaGuiPacket decode(FriendlyByteBuf buf) {
        int verwaSize = buf.readInt();
        List<String> verwa = new ArrayList<>();
        for (int i = 0; i < verwaSize; i++) verwa.add(buf.readUtf());

        int organoidSize = buf.readInt();
        List<String> organoid = new ArrayList<>();
        for (int i = 0; i < organoidSize; i++) organoid.add(buf.readUtf());

        int bombSize = buf.readInt();
        List<String> bomb = new ArrayList<>();
        for (int i = 0; i < bombSize; i++) bomb.add(buf.readUtf());

        return new SyncVervaGuiPacket(verwa, organoid, bomb);
    }

    public static void handle(SyncVervaGuiPacket msg, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPacketHandlers.handleSyncVervaGui(msg))
        );
        context.setPacketHandled(true);
    }
}