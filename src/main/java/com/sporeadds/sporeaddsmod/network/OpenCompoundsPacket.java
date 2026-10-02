package com.sporeadds.sporeaddsmod.network;

import com.sporeadds.sporeaddsmod.client.screen.CompoundsMenu;
import com.sporeadds.sporeaddsmod.util.SporeClassUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import com.sporeadds.sporeaddsmod.network.NetworkEvent;
import com.sporeadds.sporeaddsmod.network.NetworkHooks;

import java.util.function.Supplier;

/** C -> S: el berserker abre el inventario portatil de Compounds desde el action wheel. */
public class OpenCompoundsPacket {

    public OpenCompoundsPacket() {
    }

    public static void encode(OpenCompoundsPacket msg, FriendlyByteBuf buf) {
    }

    public static OpenCompoundsPacket decode(FriendlyByteBuf buf) {
        return new OpenCompoundsPacket();
    }

    public static void handle(OpenCompoundsPacket msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;
            if (!SporeClassUtil.hasClass(player, "berserker")) {
                player.displayClientMessage(
                        Component.translatable("message.sporeadd.berserker.compounds.wrong_class")
                                .withStyle(ChatFormatting.GRAY), true);
                return;
            }
            NetworkHooks.openScreen(player, new SimpleMenuProvider(
                    (id, inv, p) -> new CompoundsMenu(id, inv, p),
                    Component.translatable("ability.sporeadds.berserker.compounds")));
        });
        context.setPacketHandled(true);
    }
}
