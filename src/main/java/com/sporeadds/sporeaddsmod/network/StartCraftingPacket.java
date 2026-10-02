package com.sporeadds.sporeaddsmod.network;

import com.sporeadds.sporeaddsmod.blocks.blocks_entity.MedicBlockCrafterEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.NetworkEvent;

import java.util.function.Supplier;

public record StartCraftingPacket(BlockPos pos) {

    public static void encode(StartCraftingPacket pkt, FriendlyByteBuf buf) {
        buf.writeBlockPos(pkt.pos);
    }

    public static StartCraftingPacket decode(FriendlyByteBuf buf) {
        return new StartCraftingPacket(buf.readBlockPos());
    }

    public static void handle(StartCraftingPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Level level = ctx.get().getSender().level(); // Servidor
            if (level.getBlockEntity(pkt.pos) instanceof MedicBlockCrafterEntity be) {
                be.startCrafting();
            }
        });
        ctx.get().setPacketHandled(true);
    }
}

