package com.sporeadds.sporeaddsmod.network;

import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierData;
import com.sporeadds.sporeaddsmod.client.trainingbook.ClientTrainingBookData;
import com.sporeadds.sporeaddsmod.config.SporeAddsConfig;
import com.sporeadds.sporeaddsmod.data.ClassPopulationData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class SyncClassCountsPacket {

    public static class Entry {
        public final String classId;
        public final int count;
        public final int maxSlots;
        public final boolean enabled;

        public Entry(String classId, int count, int maxSlots, boolean enabled) {
            this.classId = classId;
            this.count = count;
            this.maxSlots = maxSlots;
            this.enabled = enabled;
        }
    }

    private final List<Entry> entries;

    public SyncClassCountsPacket(List<Entry> entries) {
        this.entries = entries;
    }

    public static void encode(SyncClassCountsPacket packet, FriendlyByteBuf buffer) {
        buffer.writeInt(packet.entries.size());
        for (Entry entry : packet.entries) {
            buffer.writeUtf(entry.classId);
            buffer.writeInt(entry.count);
            buffer.writeInt(entry.maxSlots);
            buffer.writeBoolean(entry.enabled);
        }
    }

    public static SyncClassCountsPacket decode(FriendlyByteBuf buffer) {
        int size = buffer.readInt();
        List<Entry> entries = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            entries.add(new Entry(buffer.readUtf(), buffer.readInt(), buffer.readInt(), buffer.readBoolean()));
        }
        return new SyncClassCountsPacket(entries);
    }

    public static void handle(SyncClassCountsPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                        ClientTrainingBookData.update(packet.entries)
                )
        );
        context.setPacketHandled(true);
    }

    private static List<Entry> buildEntries(MinecraftServer server) {
        List<Entry> entries = new ArrayList<>();
        ClassPopulationData population = ClassPopulationData.get(server);

        for (String classId : SporeIdentifierData.VALID_IDS) {
            if (classId.equalsIgnoreCase("none")) {
                continue;
            }

            entries.add(new Entry(
                    classId,
                    population.countClass(classId),
                    SporeAddsConfig.getTrainingBookMaxSlots(classId),
                    SporeAddsConfig.isClassEnabledInTrainingBook(classId)
            ));
        }

        return entries;
    }

    public static void sendTo(ServerPlayer player) {
        NetworkHandle.INSTANCE.send(
                PacketDistributor.PLAYER.with(() -> player),
                new SyncClassCountsPacket(buildEntries(player.getServer()))
        );
    }

    public static void broadcast(MinecraftServer server) {
        SyncClassCountsPacket packet = new SyncClassCountsPacket(buildEntries(server));
        NetworkHandle.INSTANCE.send(PacketDistributor.ALL.noArg(), packet);
    }
}
