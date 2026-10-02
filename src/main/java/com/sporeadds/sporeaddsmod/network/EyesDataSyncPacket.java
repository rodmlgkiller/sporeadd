package com.sporeadds.sporeaddsmod.network;

import com.sporeadds.sporeaddsmod.PlayerData.PlayerData;
import com.sporeadds.sporeaddsmod.PlayerData.PlayerDataProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import com.sporeadds.sporeaddsmod.network.NetworkEvent;

import java.util.function.Supplier;

public class EyesDataSyncPacket {

    private final int playerId;
    private final int eyeBaseType;
    private final int eyeGlowType;
    private final int glowOffsetX;
    private final int glowOffsetY;

    public EyesDataSyncPacket(int playerId, int eyeBaseType, int eyeGlowType, int glowOffsetX, int glowOffsetY) {
        this.playerId = playerId;
        this.eyeBaseType = eyeBaseType;
        this.eyeGlowType = eyeGlowType;
        this.glowOffsetX = glowOffsetX;
        this.glowOffsetY = glowOffsetY;
    }

    public EyesDataSyncPacket(FriendlyByteBuf buf) {
        this.playerId = buf.readInt();
        this.eyeBaseType = buf.readInt();
        this.eyeGlowType = buf.readInt();
        this.glowOffsetX = buf.readInt();
        this.glowOffsetY = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(playerId);
        buf.writeInt(eyeBaseType);
        buf.writeInt(eyeGlowType);
        buf.writeInt(glowOffsetX);
        buf.writeInt(glowOffsetY);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        context.enqueueWork(() -> com.sporeadds.sporeaddsmod.util.DistExecutor.unsafeRunWhenOn(net.neoforged.api.distmarker.Dist.CLIENT, () -> () -> com.sporeadds.sporeaddsmod.client.ClientPacketHooks.eyesData(playerId, eyeBaseType, eyeGlowType, glowOffsetX, glowOffsetY)));

        context.setPacketHandled(true);
        return true;
    }
}