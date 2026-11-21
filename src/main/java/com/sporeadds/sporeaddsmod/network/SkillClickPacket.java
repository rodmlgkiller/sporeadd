package com.sporeadds.sporeaddsmod.network;

import com.sporeadds.sporeaddsmod.Powers.SkillClick;
import com.sporeadds.sporeaddsmod.Powers.SkillClickDispatcher;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import com.sporeadds.sporeaddsmod.network.NetworkEvent;

import java.util.function.Supplier;

/** Client -> server: the player pressed the input of a click skill that has its own key (see {@link SkillClick}). */
public class SkillClickPacket {
    private final int skill;

    public SkillClickPacket(int skill) {
        this.skill = skill;
    }

    public static void encode(SkillClickPacket msg, FriendlyByteBuf buf) {
        buf.writeByte(msg.skill);
    }

    public static SkillClickPacket decode(FriendlyByteBuf buf) {
        return new SkillClickPacket(buf.readByte());
    }

    public static void handle(SkillClickPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                SkillClickDispatcher.handle(player, SkillClick.byOrdinal(msg.skill));
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
