package com.sporeadds.sporeaddsmod.network;

import com.sporeadds.sporeaddsmod.blocks.blocks_entity.ScientistBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import com.sporeadds.sporeaddsmod.network.NetworkEvent;

import java.util.function.Supplier;

public record ResearchRequestPacket(BlockPos pos, int level) {
    public static void encode(ResearchRequestPacket pkt, FriendlyByteBuf buf) {
        buf.writeBlockPos(pkt.pos());
        buf.writeInt(pkt.level());
    }

    public static ResearchRequestPacket decode(FriendlyByteBuf buf) {
        return new ResearchRequestPacket(buf.readBlockPos(), buf.readInt());
    }

    public static void handle(ResearchRequestPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();

        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) {
                return;
            }

            Level level = player.level();
            BlockPos pos = pkt.pos();

            if (!level.hasChunkAt(pos)) {
                return;
            }

            if (level.getBlockEntity(pos) instanceof ScientistBlockEntity scientist) {
                scientist.createResearchItem(pkt.level());
            }
        });

        context.setPacketHandled(true);
    }
}