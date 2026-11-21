package com.sporeadds.sporeaddsmod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.sporeadds.sporeaddsmod.PlayerData.PlayerDataProvider;
import com.sporeadds.sporeaddsmod.Powers.*;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class SporeAddsTestCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("sporeaddstest")
                        .requires(source -> source.hasPermission(4))
                        .then(Commands.argument("ability", IntegerArgumentType.integer(1, 13))
                                .then(Commands.argument("target", EntityArgument.player())
                                        .executes(context -> {
                                            int valor = IntegerArgumentType.getInteger(context, "ability");
                                            ServerPlayer target = EntityArgument.getPlayer(context, "target");
                                            return executeAbility(context.getSource(), target, valor);
                                        })
                                )
                        )
        );
    }

    private static int executeAbility(CommandSourceStack source, ServerPlayer target, int valor) {
        if (isToggleAbility(valor)) {
            return toggleAbility(source, target, valor);
        }

        return triggerInstantAbility(source, target, valor);
    }

    private static boolean isToggleAbility(int valor) {
        return valor == 2 || valor == 4 || valor == 5 || valor == 6 || valor == 7;
    }

    private static int toggleAbility(CommandSourceStack source, ServerPlayer target, int valor) {
        int index = valor - 1;

        target.getCapability(PlayerDataProvider.PLAYER_DATA).ifPresent(data -> {
            String currentSwitch = data.getSwitch();

            if (currentSwitch == null || currentSwitch.isEmpty()) {
                currentSwitch = "0000000000000";
            }

            if (currentSwitch.length() < 13) {
                currentSwitch = padRightWithZeros(currentSwitch, 13);
            }

            char[] chars = currentSwitch.toCharArray();
            chars[index] = (chars[index] == '1') ? '0' : '1';

            String newSwitch = new String(chars);
            data.setSwitch(newSwitch);

            Component state = Component.translatable(chars[index] == '1'
                    ? "command.sporeadd.test.state.on" : "command.sporeadd.test.state.off");

            source.sendSuccess(() -> Component.translatable("command.sporeadd.test.toggle.success",
                    valor, target.getGameProfile().getName(), state), true);

            target.sendSystemMessage(Component.translatable("command.sporeadd.test.toggle.target", valor, state));
        });

        return 1;
    }

    private static int triggerInstantAbility(CommandSourceStack source, ServerPlayer target, int valor) {
        boolean executed = switch (valor) {
            case 1 -> {
                new Poder1().use(target);
                yield true;
            }
            case 3 -> {
                new Poder3().use(target);
                yield true;
            }
            case 8 -> {
                new Poder8().use(target);
                yield true;
            }
            case 9 -> {
                new Poder9().use(target);
                yield true;
            }
            case 10 -> {
                new Poder10().use(target);
                yield true;
            }
            case 11 -> {
                new Poder11().use(target);
                yield true;
            }
            case 12 -> {
                Poder12.activate(target);
                yield true;
            }
            case 13 -> {
                Poder13.activate(target.serverLevel(), target);
                yield true;
            }
            default -> false;
        };

        if (executed) {
            source.sendSuccess(() -> Component.translatable("command.sporeadd.test.execute.success",
                    valor, target.getGameProfile().getName()), true);

            target.sendSystemMessage(Component.translatable("command.sporeadd.test.execute.target", valor));

            return 1;
        }

        source.sendFailure(Component.translatable("command.sporeadd.test.execute.not_configured", valor));
        return 0;
    }

    private static String padRightWithZeros(String input, int length) {
        StringBuilder builder = new StringBuilder(input);
        while (builder.length() < length) {
            builder.append('0');
        }
        return builder.toString();
    }
}