package com.sporeadds.sporeaddsmod.network;

import com.sporeadds.sporeaddsmod.research.ResearchPopupType;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import com.sporeadds.sporeaddsmod.network.NetworkEvent;
import com.sporeadds.sporeaddsmod.research.ResearchPopupManager;
import java.util.function.Supplier;

public class ResearchPopupPacket {

    private final ResearchPopupType type;
    private final String entityId;
    private final int amount;

    public ResearchPopupPacket(ResearchPopupType type, String entityId, int amount) {
        this.type = type;
        this.entityId = entityId;
        this.amount = amount;
    }

    public ResearchPopupPacket(FriendlyByteBuf buf) {
        this.type = ResearchPopupType.values()[buf.readVarInt()];
        this.entityId = buf.readUtf();
        this.amount = buf.readVarInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeVarInt(type.ordinal());
        buf.writeUtf(entityId);
        buf.writeVarInt(amount);
    }

    public static void handle(ResearchPopupPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> com.sporeadds.sporeaddsmod.util.DistExecutor.unsafeRunWhenOn(net.neoforged.api.distmarker.Dist.CLIENT, () -> () -> com.sporeadds.sporeaddsmod.client.ClientPacketHooks.researchPopup(packet.type, packet.entityId, packet.amount)));
        context.setPacketHandled(true);
    }
}