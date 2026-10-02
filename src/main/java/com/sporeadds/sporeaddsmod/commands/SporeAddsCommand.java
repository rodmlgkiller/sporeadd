package com.sporeadds.sporeaddsmod.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.sporeadds.sporeaddsmod.hive.PunishmentTracker;
import com.sporeadds.sporeaddsmod.level.PlayerLevelProvider;
import com.sporeadds.sporeaddsmod.spore.PlayerSporeProvider;
import com.sporeadds.sporeaddsmod.network.SyncLevelPacket;
import com.sporeadds.sporeaddsmod.network.NetworkHandle; // sustituye si tu clase de mensajería tiene otro nombre
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.stream.IntStream;

public class SporeAddsCommand {

    // Autocompletado para level y knowledge level (0 a 9)
    private static final SuggestionProvider<CommandSourceStack> LEVEL_SUGGESTIONS = (context, builder) ->
            SharedSuggestionProvider.suggest(
                    IntStream.rangeClosed(0, 9).mapToObj(String::valueOf).toArray(String[]::new),
                    builder
            );

    // Autocompletado para biomass (0 a 50)
    private static final SuggestionProvider<CommandSourceStack> BIOMASS_SUGGESTIONS = (context, builder) ->
            SharedSuggestionProvider.suggest(
                    IntStream.rangeClosed(0, 50).mapToObj(String::valueOf).toArray(String[]::new),
                    builder
            );

    // Autocompletado para punishment level (0 a 3)
    private static final SuggestionProvider<CommandSourceStack> PUNISHMENT_SUGGESTIONS = (context, builder) ->
            SharedSuggestionProvider.suggest(
                    IntStream.rangeClosed(0, 3).mapToObj(String::valueOf).toArray(String[]::new),
                    builder
            );

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("sporeadds")
                .requires(source -> source.hasPermission(2))

                // --- /sporeadds level <value> [target] ---
                .then(Commands.literal("level")
                        .then(Commands.argument("value", IntegerArgumentType.integer(0, 9))
                                .suggests(LEVEL_SUGGESTIONS)
                                .executes(context -> executeLevel(
                                        context,
                                        context.getSource().getPlayerOrException(),
                                        IntegerArgumentType.getInteger(context, "value")
                                ))
                                .then(Commands.argument("target", EntityArgument.player())
                                        .executes(context -> executeLevel(
                                                context,
                                                EntityArgument.getPlayer(context, "target"),
                                                IntegerArgumentType.getInteger(context, "value")
                                        ))
                                )
                        )
                )

                // --- /sporeadds knowledgelevel <value> [target] ---
                .then(Commands.literal("knowledgelevel")
                        .then(Commands.argument("value", IntegerArgumentType.integer(0, 9))
                                .suggests(LEVEL_SUGGESTIONS)
                                .executes(context -> executeKnowledgeLevel(
                                        context,
                                        context.getSource().getPlayerOrException(),
                                        IntegerArgumentType.getInteger(context, "value")
                                ))
                                .then(Commands.argument("target", EntityArgument.player())
                                        .executes(context -> executeKnowledgeLevel(
                                                context,
                                                EntityArgument.getPlayer(context, "target"),
                                                IntegerArgumentType.getInteger(context, "value")
                                        ))
                                )
                        )
                )

                // --- /sporeadds biomass <value> [target] ---
                .then(Commands.literal("biomass")
                        .then(Commands.argument("value", IntegerArgumentType.integer(0, 50))
                                .suggests(BIOMASS_SUGGESTIONS)
                                .executes(context -> executeBiomass(
                                        context,
                                        context.getSource().getPlayerOrException(),
                                        IntegerArgumentType.getInteger(context, "value")
                                ))
                                .then(Commands.argument("target", EntityArgument.player())
                                        .executes(context -> executeBiomass(
                                                context,
                                                EntityArgument.getPlayer(context, "target"),
                                                IntegerArgumentType.getInteger(context, "value")
                                        ))
                                )
                        )
                )

                // --- /sporeadds punishment <value> [target] ---
                .then(Commands.literal("punishment")
                        .then(Commands.argument("value", IntegerArgumentType.integer(0))
                                .suggests(PUNISHMENT_SUGGESTIONS)
                                .executes(context -> executePunishment(
                                        context,
                                        context.getSource().getPlayerOrException(),
                                        IntegerArgumentType.getInteger(context, "value")
                                ))
                                .then(Commands.argument("target", EntityArgument.player())
                                        .executes(context -> executePunishment(
                                                context,
                                                EntityArgument.getPlayer(context, "target"),
                                                IntegerArgumentType.getInteger(context, "value")
                                        ))
                                )
                        )
                )
        );
    }

    private static int executeLevel(CommandContext<CommandSourceStack> context, ServerPlayer target, int value) {
        target.getCapability(PlayerLevelProvider.PLAYER_LVL).ifPresent(level -> {
            level.setLevel(value);

            // Sincronizar con el cliente usando el mismo paquete que usa el provider.
            // PlayerLevelProvider.createPlayerLevel() ya llama a SyncLevelPacket.syncLevelToClient(attachedPlayer)
            // cuando el cambio se hace desde el objeto Level adjunto; sin embargo, el command modifica el capability
            // desde aquí: forzamos una sincronización explícita al jugador objetivo.
            SyncLevelPacket.syncLevelToClient(target);

            String targetName = target == context.getSource().getEntity()
                    ? ""
                    : " para " + target.getScoreboardName();

            context.getSource().sendSuccess(
                    () -> Component.literal("§4Evolution level" + targetName + ": " + level.getLevel()),
                    true
            );
        });

        return Command.SINGLE_SUCCESS;
    }

    private static int executeKnowledgeLevel(CommandContext<CommandSourceStack> context, ServerPlayer target, int value) {
        target.getCapability(PlayerLevelProvider.PLAYER_LVL).ifPresent(level -> {
            level.setKnowledgeLevel(value);

            // Forzamos sincronización del level + knowledge al cliente
            SyncLevelPacket.syncLevelToClient(target);

            String targetName = target == context.getSource().getEntity()
                    ? ""
                    : " para " + target.getScoreboardName();

            context.getSource().sendSuccess(
                    () -> Component.literal("§bKnowledge level" + targetName + ": " + level.getKnowledgeLevel()),
                    true
            );
        });

        return Command.SINGLE_SUCCESS;
    }

    private static int executePunishment(CommandContext<CommandSourceStack> context, ServerPlayer target, int value) {
        PunishmentTracker.setPoints(target, value);
        // value 0 = indulto completo: se limpia también el bloqueo de respawn.
        if (value == 0) {
            PunishmentTracker.setPermadead(target, false);
        }

        String targetName = target == context.getSource().getEntity()
                ? ""
                : " para " + target.getScoreboardName();

        context.getSource().sendSuccess(
                () -> Component.literal("§4Punishment level" + targetName + ": " + PunishmentTracker.getPoints(target)),
                true
        );

        return Command.SINGLE_SUCCESS;
    }

    private static int executeBiomass(CommandContext<CommandSourceStack> context, ServerPlayer target, int value) {
        target.getCapability(PlayerSporeProvider.PLAYER_CAP).ifPresent(spore -> {
            spore.setSpore(value);

            // Si tienes un packet específico para spore, úsalo; aquí asumo que existe:
            // PlayerSporeProvider.sync(target);
            // o bien: ModMessages.sendToPlayer(new SyncSporePacket(...), target);
            // Si no tienes uno, añade uno análogo a SyncLevelPacket.
            try {
                // intento usar una clase de sincronización si la tienes
                Class.forName("com.sporeadds.sporeaddsmod.network.SyncSporePacket");
                // si existe, llamamos a su método estático (si lo implementaste de forma similar)
                // SyncSporePacket.syncSporeToClient(target);
            } catch (ClassNotFoundException ignored) {
                // Si no existe SyncSporePacket, no hacemos nada aquí; añade tu packet y la llamada.
            }

            String targetName = target == context.getSource().getEntity()
                    ? ""
                    : " para " + target.getScoreboardName();

            context.getSource().sendSuccess(
                    () -> Component.literal("§4Current biomass" + targetName + ": " + spore.getSpore()),
                    true
            );
        });

        return Command.SINGLE_SUCCESS;
    }
}