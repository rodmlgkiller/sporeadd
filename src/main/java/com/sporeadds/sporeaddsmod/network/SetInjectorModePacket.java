package com.sporeadds.sporeaddsmod.network;

import com.sporeadds.sporeaddsmod.items.InjectorItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SetInjectorModePacket {

    private final int index;

    public SetInjectorModePacket(int index) {
        this.index = index;
    }

    public static void encode(SetInjectorModePacket packet, FriendlyByteBuf buffer) {
        buffer.writeInt(packet.index);
    }

    public static SetInjectorModePacket decode(FriendlyByteBuf buffer) {
        return new SetInjectorModePacket(buffer.readInt());
    }

    public static void handle(SetInjectorModePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;

            ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
            if (stack.getItem() instanceof InjectorItem) {
                InjectorItem.setSelectedEffectIndex(stack, packet.index);
            }
        });
        context.setPacketHandled(true);
    }
}