package com.sporeadds.sporeaddsmod.commands;

import com.Harbinger.Spore.Sentities.Organoids.Proto;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.sporeadds.sporeaddsmod.config.SporeAddsConfig;
import com.sporeadds.sporeaddsmod.level.PlayerLevelProvider;
import com.sporeadds.sporeaddsmod.spore.PlayerSporeProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

public class VervaRequestCommand {

    private static final int SPORE_COST = 5;

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("vervarequest")
                .then(Commands.argument("moundIndex", IntegerArgumentType.integer(1, 10))
                        .suggests(VervaRequestCommand::suggestMoundIndexes)
                        .executes(context -> {
                            CommandSourceStack source = context.getSource();
                            ServerPlayer player = source.getPlayerOrException();
                            ServerLevel currentLevel = source.getLevel();
                            MinecraftServer server = source.getServer();
                            int index = IntegerArgumentType.getInteger(context, "moundIndex") - 1;

                            AtomicBoolean hasLevel = new AtomicBoolean(false);
                            player.getCapability(PlayerLevelProvider.PLAYER_LVL).ifPresent(lvlCap -> {
                                if (lvlCap.getLevel() >= 4) {
                                    hasLevel.set(true);
                                }
                            });

                            if (!hasLevel.get()) {
                                source.sendFailure(Component.translatable("command.sporeadd.vervarequest.need_research"));
                                return 0;
                            }

                            if (player.getTeam() == null || !player.getTeam().getName().equals("spore")) {
                                source.sendFailure(Component.translatable("command.sporeadd.vervarequest.need_spore_team"));
                                return 0;
                            }

                            boolean protoExists = hasProtoAcrossDimensions(server);

                            if (!protoExists) {
                                source.sendFailure(Component.translatable("command.sporeadd.vervarequest.silence"));
                                return 0;
                            }

                            boolean isUnified = SporeAddsConfig.UNIFIED_MOUNDS_LIST.get();

                            player.getCapability(PlayerSporeProvider.PLAYER_CAP).ifPresent(spore -> {
                                List<UUID> rawList = new ArrayList<>(spore.getMoundRegistry().getDisplayList());

                                if (rawList.isEmpty()) {
                                    source.sendFailure(Component.translatable("command.sporeadd.vervarequest.need_core_mound"));
                                    return;
                                }

                                List<UUID> validList = new ArrayList<>();

                                if (isUnified) {
                                    for (UUID u : rawList) {
                                        if (findEntityAcrossDimensions(server, u) != null) {
                                            validList.add(u);
                                        }
                                    }
                                } else {
                                    for (UUID u : rawList) {
                                        if (currentLevel.getEntity(u) != null) {
                                            validList.add(u);
                                        }
                                    }
                                }

                                if (validList.isEmpty()) {
                                    source.sendFailure(Component.translatable("command.sporeadd.vervarequest.no_mounds_dimension"));
                                    return;
                                }

                                if (index >= validList.size()) {
                                    source.sendFailure(Component.translatable("command.sporeadd.vervarequest.unknown_value", validList.size()));
                                    return;
                                }

                                if (spore.getSpore() < SPORE_COST) {
                                    source.sendFailure(Component.translatable("command.sporeadd.vervarequest.need_biomass", SPORE_COST));
                                    return;
                                }

                                UUID targetMoundUUID = validList.get(index);
                                Entity moundEntity;
                                ServerLevel moundLevel;

                                if (isUnified) {
                                    EntitySearchResult result = findEntityWithLevelAcrossDimensions(server, targetMoundUUID);
                                    if (result == null) {
                                        source.sendFailure(Component.translatable("command.sporeadd.vervarequest.unable_to_connect"));
                                        return;
                                    }
                                    moundEntity = result.entity();
                                    moundLevel = result.level();
                                } else {
                                    moundEntity = currentLevel.getEntity(targetMoundUUID);
                                    moundLevel = currentLevel;

                                    if (moundEntity == null) {
                                        source.sendFailure(Component.translatable("command.sporeadd.vervarequest.unable_to_connect"));
                                        return;
                                    }
                                }

                                spore.setSpore(spore.getSpore() - SPORE_COST);

                                player.sendSystemMessage(Component.translatable("command.sporeadd.vervarequest.incoming"));

                                VervaTransportTask.startTask(player, player.position(), moundEntity.position(), moundLevel);
                            });

                            return 1;
                        })));
    }

    private static CompletableFuture<Suggestions> suggestMoundIndexes(
            CommandContext<CommandSourceStack> context,
            SuggestionsBuilder builder
    ) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();
            ServerLevel currentLevel = context.getSource().getLevel();
            MinecraftServer server = context.getSource().getServer();

            AtomicBoolean hasLevel = new AtomicBoolean(false);
            player.getCapability(PlayerLevelProvider.PLAYER_LVL).ifPresent(lvlCap -> {
                if (lvlCap.getLevel() >= 4) {
                    hasLevel.set(true);
                }
            });

            if (!hasLevel.get()) {
                return builder.buildFuture();
            }

            boolean isUnified = SporeAddsConfig.UNIFIED_MOUNDS_LIST.get();

            return player.getCapability(PlayerSporeProvider.PLAYER_CAP).map(spore -> {
                List<UUID> rawList = new ArrayList<>(spore.getMoundRegistry().getDisplayList());

                if (rawList.isEmpty()) {
                    return builder.buildFuture();
                }

                int validCount = 0;

                if (isUnified) {
                    for (UUID u : rawList) {
                        if (findEntityAcrossDimensions(server, u) != null) {
                            validCount++;
                        }
                    }
                } else {
                    for (UUID u : rawList) {
                        if (currentLevel.getEntity(u) != null) {
                            validCount++;
                        }
                    }
                }

                int max = Math.min(validCount, 10);
                for (int i = 1; i <= max; i++) {
                    builder.suggest(String.valueOf(i));
                }

                return builder.buildFuture();
            }).orElse(builder.buildFuture());

        } catch (Exception e) {
            return builder.buildFuture();
        }
    }

    private static boolean hasProtoAcrossDimensions(MinecraftServer server) {
        for (ServerLevel level : server.getAllLevels()) {
            for (Entity entity : level.getAllEntities()) {
                if (entity instanceof Proto) {
                    return true;
                }
            }
        }
        return false;
    }

    private static Entity findEntityAcrossDimensions(MinecraftServer server, UUID uuid) {
        for (ServerLevel level : server.getAllLevels()) {
            Entity entity = level.getEntity(uuid);
            if (entity != null) {
                return entity;
            }
        }
        return null;
    }

    private static EntitySearchResult findEntityWithLevelAcrossDimensions(MinecraftServer server, UUID uuid) {
        for (ServerLevel level : server.getAllLevels()) {
            Entity entity = level.getEntity(uuid);
            if (entity != null) {
                return new EntitySearchResult(entity, level);
            }
        }
        return null;
    }

    private record EntitySearchResult(Entity entity, ServerLevel level) {}
}