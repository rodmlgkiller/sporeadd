package com.sporeadds.sporeaddsmod.network;

import com.sporeadds.sporeaddsmod.client.FieldResearchClientData;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.NetworkEvent;

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
        context.enqueueWork(() -> onClient(packet.kills, packet.data));
        context.setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void onClient(Map<String, Integer> kills, Map<String, Integer> data) {
        playFirstKillSoundsIfApplicable(kills);

        FieldResearchClientData.setKills(kills);
        FieldResearchClientData.setDataAmounts(data);
        FieldResearchClientData.openBookWhenReady();
    }

    @OnlyIn(Dist.CLIENT)
    private static void playFirstKillSoundsIfApplicable(Map<String, Integer> newKills) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return;
        }

        boolean isScientist = mc.player.getCapability(
                com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider.SPORE_IDENTIFIER
        ).map(d -> "scientist".equalsIgnoreCase(d.getIdentifier())).orElse(false);

        if (!isScientist) {
            return;
        }

        for (Map.Entry<String, Integer> entry : newKills.entrySet()) {
            String entityId = entry.getKey();
            int newCount = entry.getValue();
            int oldCount = FieldResearchClientData.getKillCount(entityId);

            if (oldCount == 0 && newCount > 0) {
                mc.level.playLocalSound(
                        mc.player.getX(), mc.player.getY(), mc.player.getZ(),
                        SoundEvents.BOOK_PUT,
                        SoundSource.PLAYERS,
                        1.0F, 1.0F, false
                );
            }
        }
    }
}