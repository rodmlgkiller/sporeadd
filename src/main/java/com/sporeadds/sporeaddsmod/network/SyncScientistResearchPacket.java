package com.sporeadds.sporeaddsmod.network;

import com.sporeadds.sporeaddsmod.client.FieldResearchClientData;
import com.sporeadds.sporeaddsmod.config.ScientistConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

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
        context.enqueueWork(() -> onClient(packet.kills, packet.data, packet.rules));
        context.setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void onClient(Map<String, Integer> kills, Map<String, Integer> data, ScientistConfig.ResearchRules rules) {
        playFirstKillSoundsIfApplicable(kills);

        FieldResearchClientData.setKills(kills);
        FieldResearchClientData.setDataAmounts(data);
        FieldResearchClientData.setRules(rules);
        com.sporeadds.sporeaddsmod.client.FieldResearchCooldownClientState.setCooldown(rules.fieldResearchCooldownTicks);
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