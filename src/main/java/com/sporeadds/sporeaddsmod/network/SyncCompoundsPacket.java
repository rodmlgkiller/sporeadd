package com.sporeadds.sporeaddsmod.network;

import com.sporeadds.sporeaddsmod.client.CompoundsClientState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import com.sporeadds.sporeaddsmod.network.NetworkEvent;

import java.util.function.Supplier;

/** S -> C (solo al jugador): los 6 items del inventario de Compounds, para el HUD. */
public class SyncCompoundsPacket {

    private final ItemStack[] stacks;

    public SyncCompoundsPacket(ItemStack[] stacks) {
        this.stacks = stacks;
    }

    public static void encode(SyncCompoundsPacket msg, FriendlyByteBuf buf) {
        for (int i = 0; i < CompoundsClientState.SIZE; i++) {
            ItemStack s = (msg.stacks != null && i < msg.stacks.length && msg.stacks[i] != null)
                    ? msg.stacks[i] : ItemStack.EMPTY;
            buf.writeItem(s);
        }
    }

    public static SyncCompoundsPacket decode(FriendlyByteBuf buf) {
        ItemStack[] arr = new ItemStack[CompoundsClientState.SIZE];
        for (int i = 0; i < CompoundsClientState.SIZE; i++) {
            arr[i] = buf.readItem();
        }
        return new SyncCompoundsPacket(arr);
    }

    public static void handle(SyncCompoundsPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> CompoundsClientState.set(msg.stacks));
        ctx.get().setPacketHandled(true);
    }
}
