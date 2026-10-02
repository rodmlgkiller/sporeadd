package com.sporeadds.sporeaddsmod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SubmitSelfDefibrillateChoicePacket {

    private final boolean success;

    public SubmitSelfDefibrillateChoicePacket(boolean success) {
        this.success = success;
    }

    public static void encode(SubmitSelfDefibrillateChoicePacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.success);
    }

    public static SubmitSelfDefibrillateChoicePacket decode(FriendlyByteBuf buf) {
        return new SubmitSelfDefibrillateChoicePacket(buf.readBoolean());
    }

    public static void handle(SubmitSelfDefibrillateChoicePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer player = ctx.get().getSender();
        if (player == null) {
            ctx.get().setPacketHandled(true);
            return;
        }

        boolean success = msg.success;

        ctx.get().enqueueWork(() -> {
            com.sporeadds.sporeaddsmod.effects.SelfDefibrillateAbility.resolve(player, success);
        });
        ctx.get().setPacketHandled(true);
    }
}