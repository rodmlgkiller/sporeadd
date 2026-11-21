package com.sporeadds.sporeaddsmod.network;

import com.sporeadds.sporeaddsmod.Powers.SkillBindings;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Client -> server: what every click skill is bound to (one {@link com.sporeadds.sporeaddsmod.Powers.SkillClick} mode per skill).
 * Sent when the player joins and whenever the Controls change it.
 */
public class SkillBindingsPacket {
    private final byte[] modes;

    public SkillBindingsPacket(byte[] modes) {
        this.modes = modes;
    }

    public static void encode(SkillBindingsPacket msg, FriendlyByteBuf buf) {
        buf.writeByteArray(msg.modes);
    }

    public static SkillBindingsPacket decode(FriendlyByteBuf buf) {
        return new SkillBindingsPacket(buf.readByteArray(32));
    }

    public static void handle(SkillBindingsPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                SkillBindings.set(player.getUUID(), msg.modes);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
