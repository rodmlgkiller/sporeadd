package com.sporeadds.sporeaddsmod.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.sporeadds.sporeaddsmod.spore.PlayerSporeProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class SetSporePhase {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("setsporephase")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("phase", IntegerArgumentType.integer(0, 50))
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            PlayerSporeProvider.PLAYER_CAP.get(player).ifPresent(spore -> {
                                spore.setSpore(IntegerArgumentType.getInteger(context, "phase"));
                                context.getSource().sendSuccess(() -> Component.literal("§4Current biomass:" + spore.getSpore()), true);
                            });
                            return Command.SINGLE_SUCCESS;
                        })));
    }
}//
