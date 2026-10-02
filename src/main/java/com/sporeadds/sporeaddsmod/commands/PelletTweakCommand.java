package com.sporeadds.sporeaddsmod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.sporeadds.sporeaddsmod.network.NetworkHandle;
import com.sporeadds.sporeaddsmod.network.SyncPelletTweakPacket;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

public class PelletTweakCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("pellettweak")
                .requires(source -> source.hasPermission(2));

        RequiredArgumentBuilder<CommandSourceStack, Integer> idArg =
                Commands.argument("id", IntegerArgumentType.integer(1, 8));

        String[] propiedades = {"offsetx", "offsety", "offsetz", "rotx", "roty", "rotz", "scale"};

        for (String prop : propiedades) {
            idArg.then(Commands.literal(prop)
                    .then(Commands.argument("valor", FloatArgumentType.floatArg())
                            .executes(context -> {
                                int id = IntegerArgumentType.getInteger(context, "id");
                                float valor = FloatArgumentType.getFloat(context, "valor");

                                // Sacamos al jugador para afectar solo a su layer
                                ServerPlayer player = context.getSource().getPlayerOrException();

                                NetworkHandle.INSTANCE.send(
                                        PacketDistributor.ALL.noArg(),
                                        new SyncPelletTweakPacket(player.getUUID(), id, prop, valor)
                                );

                                context.getSource().sendSuccess(
                                        () -> Component.literal("§aPellet " + id + " -> " + prop + " seteado a " + valor),
                                        true
                                );
                                return 1;
                            })));
        }

        command.then(idArg);
        dispatcher.register(command);
    }
}