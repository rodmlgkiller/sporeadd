package com.sporeadds.sporeaddsmod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.sporeadds.sporeaddsmod.PlayerData.PlayerData;
import com.sporeadds.sporeaddsmod.PlayerData.PlayerDataProvider;
import com.sporeadds.sporeaddsmod.network.EyesDataSyncPacket;
import com.sporeadds.sporeaddsmod.network.NetworkHandle;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import com.sporeadds.sporeaddsmod.network.PacketDistributor;

public class EyesCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("eyes")
                .requires(source -> true)

                .then(Commands.literal("base")
                        .then(Commands.argument("value", IntegerArgumentType.integer(1, 4))
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    int value = IntegerArgumentType.getInteger(context, "value");

                                    PlayerData data = PlayerDataProvider.get(player);
                                    data.setEyeBaseType(value);
                                    syncEyes(player, data);

                                    context.getSource().sendSuccess(
                                            () -> Component.literal("Base eyes cambiada a " + value), false
                                    );
                                    return 1;
                                })))

                .then(Commands.literal("glow")
                        .then(Commands.argument("value", IntegerArgumentType.integer(1, 4))
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    int value = IntegerArgumentType.getInteger(context, "value");

                                    PlayerData data = PlayerDataProvider.get(player);
                                    data.setEyeGlowType(value);
                                    syncEyes(player, data);

                                    context.getSource().sendSuccess(
                                            () -> Component.literal("Glow eyes cambiada a " + value), false
                                    );
                                    return 1;
                                })))

                .then(Commands.literal("set")
                        .then(Commands.argument("base", IntegerArgumentType.integer(1, 4))
                                .then(Commands.argument("glow", IntegerArgumentType.integer(1, 4))
                                        .executes(context -> {
                                            ServerPlayer player = context.getSource().getPlayerOrException();
                                            int base = IntegerArgumentType.getInteger(context, "base");
                                            int glow = IntegerArgumentType.getInteger(context, "glow");

                                            PlayerData data = PlayerDataProvider.get(player);
                                            data.setEyeBaseType(base);
                                            data.setEyeGlowType(glow);
                                            syncEyes(player, data);

                                            context.getSource().sendSuccess(
                                                    () -> Component.literal("Ojos actualizados. Base=" + base + ", Glow=" + glow), false
                                            );
                                            return 1;
                                        })))));
    }

    private static void syncEyes(ServerPlayer player, PlayerData data) {
        NetworkHandle.INSTANCE.send(
                PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                new EyesDataSyncPacket(
                        player.getId(),
                        data.getEyeBaseType(),
                        data.getEyeGlowType(),
                        data.getGlowOffsetX(),
                        data.getGlowOffsetY()
                )
        );
    }
}