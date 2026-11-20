package com.sporeadds.sporeaddsmod.network;

import com.sporeadds.sporeaddsmod.client.FieldResearchClientData;
import com.sporeadds.sporeaddsmod.config.ScientistConfig;
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
    /** Prestige requirements, weak point damage rules and field research cooldown of the server that sent this. */
    private final ScientistConfig.ResearchRules rules;

    public SyncScientistResearchPacket(Map<String, Integer> kills, Map<String, Integer> data) {
        this(kills, data, ScientistConfig.ResearchRules.current());
    }

    public SyncScientistResearchPacket(Map<String, Integer> kills, Map<String, Integer> data, ScientistConfig.ResearchRules rules) {
        this.kills = kills;
        this.data = data;
        this.rules = rules;
    }

    public SyncScientistResearchPacket(FriendlyByteBuf buf) {
        this.kills = readMap(buf);
        this.data = readMap(buf);
        this.rules = ScientistConfig.ResearchRules.read(buf);
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
        rules.write(buf);
    }

    public static void handle(SyncScientistResearchPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> com.sporeadds.sporeaddsmod.util.DistExecutor.unsafeRunWhenOn(net.neoforged.api.distmarker.Dist.CLIENT, () -> () -> com.sporeadds.sporeaddsmod.client.ClientPacketHooks.scientistResearch(packet.kills, packet.data, packet.rules)));
        context.setPacketHandled(true);
    }

}
