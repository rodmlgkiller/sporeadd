package com.sporeadds.sporeaddsmod.network;

import com.sporeadds.sporeaddsmod.capabilities.PlayerImplantsCapability;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import com.sporeadds.sporeaddsmod.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class SyncImplantPacket {

    private final UUID playerUUID;
    private final CompoundTag implantData;

    public SyncImplantPacket(UUID playerUUID, CompoundTag implantData) {
        this.playerUUID = playerUUID;
        this.implantData = implantData;
    }

    public SyncImplantPacket(FriendlyByteBuf buf) {
        this.playerUUID = buf.readUUID();
        this.implantData = buf.readNbt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(playerUUID);
        buf.writeNbt(implantData);
    }

    public static SyncImplantPacket decode(FriendlyByteBuf buf) {
        return new SyncImplantPacket(buf);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            if (ctx.getDirection().getReceptionSide().isClient()) {
                com.sporeadds.sporeaddsmod.util.DistExecutor.unsafeRunWhenOn(net.neoforged.api.distmarker.Dist.CLIENT, () -> () -> com.sporeadds.sporeaddsmod.client.ClientPacketHooks.implantSync(playerUUID, implantData));
            }
        });
        return true;
    }
}
