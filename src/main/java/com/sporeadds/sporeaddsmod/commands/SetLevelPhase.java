package com.sporeadds.sporeaddsmod.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.sporeadds.sporeaddsmod.level.PlayerLevelProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class SetLevelPhase {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("setlevelphase")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("phase", IntegerArgumentType.integer(0, 9))
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            player.getCapability(PlayerLevelProvider.PLAYER_LVL).ifPresent(level -> {
                                level.setLevel(IntegerArgumentType.getInteger(context, "phase"));
                                context.getSource().sendSuccess(() -> Component.literal("§4Evolution level:" + level.getLevel()), true);
                            });
                            return Command.SINGLE_SUCCESS;
                        })));
    }
}
