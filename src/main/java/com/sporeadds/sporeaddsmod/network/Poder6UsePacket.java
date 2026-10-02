package com.sporeadds.sporeaddsmod.network;

import com.sporeadds.sporeaddsmod.Powers.Poder7;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.NetworkEvent;

import java.util.function.Supplier;

public class Poder6UsePacket {
    public final float forward;
    public final float strafing;
    public final boolean isAscending;
    public final boolean isFirstPress;

    public Poder6UsePacket(float forward, float strafing, boolean isAscending, boolean isFirstPress) {
        this.forward = forward;
        this.strafing = strafing;
        this.isAscending = isAscending;
        this.isFirstPress = isFirstPress;
    }

    public static void encode(Poder6UsePacket pkt, FriendlyByteBuf buf) {
        buf.writeFloat(pkt.forward);
        buf.writeFloat(pkt.strafing);
        buf.writeBoolean(pkt.isAscending);
        buf.writeBoolean(pkt.isFirstPress);
    }

    public static Poder6UsePacket decode(FriendlyByteBuf buf) {
        return new Poder6UsePacket(
                buf.readFloat(),
                buf.readFloat(),
                buf.readBoolean(),
                buf.readBoolean()
        );
    }

    public static void handle(Poder6UsePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null && player.getVehicle() instanceof LivingEntity riddenMob) {
                // Manejamos el movimiento horizontal
                Poder7.handleMountMovement(player, riddenMob, msg.forward, msg.strafing);

                // Manejamos el salto/vuelo
                Poder7.handleMountJumpInput(player, riddenMob, msg.isAscending, msg.isFirstPress);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}