package com.sporeadds.sporeaddsmod.network;

import com.sporeadds.sporeaddsmod.blocks.blocks_entity.RaidControlerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import com.sporeadds.sporeaddsmod.network.NetworkEvent;

import java.util.function.Supplier;

public class StartRaidPacket {

    private final BlockPos pos;

    public StartRaidPacket(BlockPos pos) {
        this.pos = pos;
    }

    public StartRaidPacket(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) {
                return;
            }

            BlockEntity blockEntity = player.level().getBlockEntity(this.pos);
            if (blockEntity instanceof RaidControlerBlockEntity raidControler) {
                raidControler.tryStartRaid();
            }
        });

        context.setPacketHandled(true);
    }
}