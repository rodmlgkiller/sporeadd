package com.sporeadds.sporeaddsmod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.sporeadds.sporeaddsmod.network.NetworkHandle;
import com.sporeadds.sporeaddsmod.network.SyncBlomfungTweakPacket;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;

public class BlomfungTweakCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("blomfungtweak")
                .requires(source -> source.hasPermission(2));

        // Argumento ID (1 para el de la cabeza, 2 para el del brazo)
        RequiredArgumentBuilder<CommandSourceStack, Integer> idArg =
                Commands.argument("id", IntegerArgumentType.integer(1, 2));

        String[] propiedades = {"offsetx", "offsety", "offsetz", "rotx", "roty", "rotz", "scale"};

        for (String prop : propiedades) {
            idArg.then(Commands.literal(prop)
                    .then(Commands.argument("valor", FloatArgumentType.floatArg())
                            .executes(context -> {
                                int id = IntegerArgumentType.getInteger(context, "id");
                                float valor = FloatArgumentType.getFloat(context, "valor");

                                // Obtenemos el jugador que ejecuta el comando
                                ServerPlayer player = context.getSource().getPlayerOrException();

                                // Mandamos paquete a todos informando qué jugador cambió qué cosa
                                NetworkHandle.INSTANCE.send(
                                        PacketDistributor.ALL.noArg(),
                                        new SyncBlomfungTweakPacket(player.getUUID(), id, prop, valor)
                                );

                                context.getSource().sendSuccess(
                                        () -> Component.literal("§aBlomfung " + id + " -> " + prop + " seteado a " + valor),
                                        true
                                );
                                return 1;
                            })
                    )
            );
        }

        command.then(idArg);
        dispatcher.register(command);
    }
}