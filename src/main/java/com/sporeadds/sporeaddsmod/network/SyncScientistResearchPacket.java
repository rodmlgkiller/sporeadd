package com.sporeadds.sporeaddsmod.network;

import com.sporeadds.sporeaddsmod.client.FieldResearchClientData;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import com.sporeadds.sporeaddsmod.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class SyncScientistResearchPacket {

    private final Map<String, Integer> kills;
    private final Map<String, Integer> data;

    public SyncScientistResearchPacket(Map<String, Integer> kills, Map<String, Integer> data) {
        this.kills = kills;
        this.data = data;
    }

    public SyncScientistResearchPacket(FriendlyByteBuf buf) {
        this.kills = readMap(buf);
        this.data = readMap(buf);
    }

    private static Map<String, Integer> readMap(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < size; i++) {
            String id = buf.readUtf();
            int count = buf.readVarInt();
            map.put(id, count);
        }
        return map;
    }

    private static void writeMap(FriendlyByteBuf buf, Map<String, Integer> map) {
        buf.writeVarInt(map.size());
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            buf.writeUtf(entry.getKey());
            buf.writeVarInt(entry.getValue());
        }
    }

    public void toBytes(FriendlyByteBuf buf) {
        writeMap(buf, kills);
        writeMap(buf, data);
    }

    public static void handle(SyncScientistResearchPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> com.sporeadds.sporeaddsmod.util.DistExecutor.unsafeRunWhenOn(net.neoforged.api.distmarker.Dist.CLIENT, () -> () -> com.sporeadds.sporeaddsmod.client.ClientPacketHooks.scientistResearch(packet.kills, packet.data)));
        context.setPacketHandled(true);
    }

}
