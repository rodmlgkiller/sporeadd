package com.sporeadds.sporeaddsmod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.sporeadds.sporeaddsmod.config.KommandantPowersConfig;
import com.sporeadds.sporeaddsmod.network.NetworkHandle;
import com.sporeadds.sporeaddsmod.network.SyncMoundCountPacket;
import com.sporeadds.sporeaddsmod.spore.PlayerSpore;
import com.sporeadds.sporeaddsmod.spore.PlayerSporeProvider;
import com.sporeadds.sporeaddsmod.util.SporeFactionHelper;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import com.sporeadds.sporeaddsmod.network.PacketDistributor;

import java.util.List;
import java.util.UUID;

public class MoundCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("mound")
                .requires(cs -> cs.getEntity() instanceof ServerPlayer player && SporeFactionHelper.isSporePlayer(player))
                .then(Commands.argument("target", StringArgumentType.word())
                        .suggests((context, builder) -> {
                            if (context.getSource().getEntity() instanceof ServerPlayer player) {
                                PlayerSporeProvider.PLAYER_CAP.get(player).ifPresent(spore -> {
                                    List<UUID> list = spore.getMoundRegistry().getDisplayList();

                                    for (int i = 0; i < list.size(); i++) {
                                        builder.suggest(String.valueOf(i + 1));
                                    }

                                    for (UUID uuid : list) {
                                        String name = spore.getMoundRegistry().getName(uuid);
                                        if (name != null && !name.isBlank()) {
                                            builder.suggest(name);
                                        }
                                    }
                                });
                            }
                            return builder.buildFuture();
                        })
                        .then(Commands.literal("spawn")
                                .executes(context -> selectPreferred(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "target")
                                )))
                        .then(Commands.literal("remove")
                                .executes(context -> removeMound(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "target")
                                )))
                        .then(Commands.literal("rename")
                                .then(Commands.argument("name", StringArgumentType.word())
                                        .executes(context -> renameMound(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "target"),
                                                StringArgumentType.getString(context, "name")
                                        ))))));
    }

    private static int selectPreferred(CommandSourceStack source, String target) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();

        PlayerSporeProvider.PLAYER_CAP.get(player).ifPresent(spore -> {
            UUID chosenMound = resolveTarget(spore, target);

            if (chosenMound == null) {
                source.sendFailure(Component.translatable("command.sporeadd.mound.not_found"));
                return;
            }

            Integer slot = spore.getMoundRegistry().getSlotOf(chosenMound);
            String name = spore.getMoundRegistry().getName(chosenMound);

            spore.getMoundRegistry().setPreferredMound(chosenMound);
            sync(player, spore);

            if (name != null && !name.isBlank()) {
                source.sendSuccess(
                        () -> Component.translatable("command.sporeadd.mound.spawn.named", slot, name),
                        false
                );
            } else {
                source.sendSuccess(
                        () -> Component.translatable("command.sporeadd.mound.spawn.unnamed", slot),
                        false
                );
            }
        });

        return 1;
    }

    private static int removeMound(CommandSourceStack source, String target) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();

        PlayerSporeProvider.PLAYER_CAP.get(player).ifPresent(spore -> {
            UUID chosenMound = resolveTarget(spore, target);

            if (chosenMound == null) {
                source.sendFailure(Component.translatable("command.sporeadd.mound.not_found"));
                return;
            }

            Integer slot = spore.getMoundRegistry().getSlotOf(chosenMound);
            String name = spore.getMoundRegistry().getName(chosenMound);

            spore.getMoundRegistry().remove(chosenMound);

            // También quitarlo del almacén de mundo para que no reaparezca al reconectar.
            if (player.getServer() != null) {
                com.sporeadds.sporeaddsmod.data.MoundSavedData worldData =
                        com.sporeadds.sporeaddsmod.data.MoundSavedData.get(player.getServer());

                // power_3_mounds.remove_command_destroys_mound: the mound itself goes away too (default: it stays in the world).
                if (KommandantPowersConfig.MOUND_REMOVE_DESTROYS.get()) {
                    destroyMound(player.getServer(), worldData, player.getUUID(), chosenMound);
                }

                worldData.removeMound(player.getUUID(), chosenMound);
                worldData.persistNow(player.getServer());
            }

            sync(player, spore);

            if (name != null && !name.isBlank()) {
                source.sendSuccess(
                        () -> Component.translatable("command.sporeadd.mound.remove.named", slot, name),
                        false
                );
            } else {
                source.sendSuccess(
                        () -> Component.translatable("command.sporeadd.mound.remove.unnamed", slot),
                        false
                );
            }
        });

        return 1;
    }

    private static int renameMound(CommandSourceStack source, String target, String newName) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();

        PlayerSporeProvider.PLAYER_CAP.get(player).ifPresent(spore -> {
            UUID chosenMound = resolveTarget(spore, target);

            if (chosenMound == null) {
                source.sendFailure(Component.translatable("command.sporeadd.mound.not_found"));
                return;
            }

            String normalized = newName == null ? "" : newName.trim();
            if (normalized.isEmpty()) {
                source.sendFailure(Component.translatable("command.sporeadd.mound.invalid_name"));
                return;
            }

            boolean renamed = spore.getMoundRegistry().setName(chosenMound, normalized);
            if (!renamed) {
                source.sendFailure(Component.translatable("command.sporeadd.mound.name_in_use"));
                return;
            }

            Integer slot = spore.getMoundRegistry().getSlotOf(chosenMound);
            sync(player, spore);

            source.sendSuccess(
                    () -> Component.translatable("command.sporeadd.mound.rename.success", slot, normalized),
                    false
            );
        });

        return 1;
    }

    /** Removes the mound entity from the world and frees the chunk the mod keeps loaded for it. Call BEFORE the saved-data entry is removed. */
    private static void destroyMound(MinecraftServer server, com.sporeadds.sporeaddsmod.data.MoundSavedData worldData,
                                     UUID ownerUUID, UUID moundUUID) {
        ServerLevel savedLevel = worldData.getMoundLevel(server, ownerUUID, moundUUID);
        ChunkPos savedChunk = worldData.getMoundChunk(ownerUUID, moundUUID);

        for (ServerLevel level : server.getAllLevels()) {
            Entity mound = level.getEntity(moundUUID);
            if (mound != null) {
                ChunkPos where = new ChunkPos(mound.blockPosition());
                mound.discard();
                level.setChunkForced(where.x, where.z, false);
                break;
            }
        }

        if (savedLevel != null && savedChunk != null) {
            savedLevel.setChunkForced(savedChunk.x, savedChunk.z, false);
        }
    }

    private static UUID resolveTarget(PlayerSpore spore, String target) {
        if (target == null || target.isBlank()) return null;

        try {
            int slot = Integer.parseInt(target);
            return spore.getMoundRegistry().findBySlot(slot);
        } catch (NumberFormatException ignored) {
        }

        return spore.getMoundRegistry().findByName(target);
    }

    private static void sync(ServerPlayer player, PlayerSpore spore) {
        CompoundTag nbt = new CompoundTag();
        spore.saveNBTData(nbt);
        NetworkHandle.INSTANCE.send(
                PacketDistributor.PLAYER.with(() -> player),
                new SyncMoundCountPacket(nbt)
        );
    }
}