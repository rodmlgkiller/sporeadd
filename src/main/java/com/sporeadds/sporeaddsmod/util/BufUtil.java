package com.sporeadds.sporeaddsmod.util;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.world.item.ItemStack;

/** Registry-aware buffer helpers for the packets that used FriendlyByteBuf#writeItem / writeComponent. */
public final class BufUtil {

    private BufUtil() {
    }

    private static RegistryFriendlyByteBuf registry(FriendlyByteBuf buf) {
        if (buf instanceof RegistryFriendlyByteBuf rbuf) {
            return rbuf;
        }
        throw new IllegalStateException("Packet buffer has no registry access: " + buf.getClass().getName());
    }

    public static void writeItem(FriendlyByteBuf buf, ItemStack stack) {
        ItemStack.OPTIONAL_STREAM_CODEC.encode(registry(buf), stack);
    }

    public static ItemStack readItem(FriendlyByteBuf buf) {
        return ItemStack.OPTIONAL_STREAM_CODEC.decode(registry(buf));
    }

    public static void writeComponent(FriendlyByteBuf buf, Component component) {
        ComponentSerialization.STREAM_CODEC.encode(registry(buf), component);
    }

    public static Component readComponent(FriendlyByteBuf buf) {
        return ComponentSerialization.STREAM_CODEC.decode(registry(buf));
    }
}
