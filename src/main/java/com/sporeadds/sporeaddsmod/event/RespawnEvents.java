package com.sporeadds.sporeaddsmod.event;

import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider;
import com.sporeadds.sporeaddsmod.Powers.Levelstats;
import com.sporeadds.sporeaddsmod.commands.ClassCommand;
import com.sporeadds.sporeaddsmod.config.SporeAddsConfig;
import com.sporeadds.sporeaddsmod.level.PlayerLevelProvider;
import com.sporeadds.sporeaddsmod.spore.PlayerSporeProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameType;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Iterator;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = "sporeadd")
public class RespawnEvents {

    private static boolean hasKommandantClass(ServerPlayer player) {
        return player.getCapability(
                SporeIdentifierProvider.SPORE_IDENTIFIER
        ).map(data ->
                "kommandant".equalsIgnoreCase(data.getIdentifier())
        ).orElse(false);
    }

    private static boolean hasMedicClass(ServerPlayer player) {
        return player.getCapability(
                SporeIdentifierProvider.SPORE_IDENTIFIER
        ).map(data ->
                "medic".equalsIgnoreCase(data.getIdentifier())
        ).orElse(false);
    }

    private static void equipMedicMaskAfterRespawn(ServerPlayer player) {
        if (!ModList.get().isLoaded("origins")) {
            return;
        }

        if (!hasMedicClass(player)) {
            return;
        }

        ClassCommand.equipMedicMask(player);
    }

    private static void teleportToEntity(
            ServerPlayer player,
            ServerLevel targetLevel,
            Entity entity
    ) {
        player.serverLevel().getServer().execute(() -> {
            player.teleportTo(
                    targetLevel,
                    entity.getX(),
                    entity.getY(),
                    entity.getZ(),
                    entity.getYRot(),
                    entity.getXRot()
            );

            ResourceLocation soundId =
                    new ResourceLocation("spore", "bioblob");

            SoundEvent customSound =
                    ForgeRegistries.SOUND_EVENTS.getValue(soundId);

            if (customSound != null) {
                player.playSound(
                        customSound,
                        2.0F,
                        0.8F
                );
            }
        });
    }

    private static EntitySearchResult findEntityAcrossDimensions(
            MinecraftServer server,
            UUID uuid
    ) {
        for (ServerLevel level : server.getAllLevels()) {
            Entity entity = level.getEntity(uuid);

            if (entity != null) {
                return new EntitySearchResult(entity, level);
            }
        }

        return null;
    }

    @SubscribeEvent
    public static void onPlayerRespawn(
            PlayerEvent.PlayerRespawnEvent event
    ) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        /*
         * Solo vuelve a equipar la máscara.
         * No entrega de nuevo el injector ni las vendas.
         */
        equipMedicMaskAfterRespawn(player);

        if (!hasKommandantClass(player)) {
            return;
        }

        player.getCapability(
                PlayerLevelProvider.PLAYER_LVL
        ).ifPresent(levelData -> {
            int currentPhase = levelData.getLevel();

            int levelsLost =
                    SporeAddsConfig.KOMMANDANT_LEVELS_LOST_ON_DEATH.get();

            int newPhase =
                    Math.max(0, currentPhase - levelsLost);

            levelData.setLevel(newPhase);

            player.sendSystemMessage(
                    Component.translatable(
                            "message.sporeadd.general.evolution_reduced",
                            newPhase
                    ).withStyle(ChatFormatting.DARK_RED)
            );
        });

        player.getCapability(
                PlayerSporeProvider.PLAYER_CAP
        ).ifPresent(spore -> {
            MinecraftServer server = player.getServer();

            if (server == null) {
                return;
            }

            var registry = spore.getMoundRegistry();
            var list = registry.getList();
            boolean teleported = false;

            UUID preferredMound =
                    registry.getPreferredMound();

            if (preferredMound != null
                    && list.contains(preferredMound)) {

                EntitySearchResult preferredResult =
                        findEntityAcrossDimensions(
                                server,
                                preferredMound
                        );

                if (preferredResult != null
                        && preferredResult.entity().isAlive()) {

                    teleportToEntity(
                            player,
                            preferredResult.level(),
                            preferredResult.entity()
                    );

                    teleported = true;
                } else {
                    player.sendSystemMessage(
                            Component.literal(
                                    "§cNo se pudo localizar tu Mound elegida. Buscando otra disponible..."
                            )
                    );
                }
            }

            if (!teleported) {
                Iterator<UUID> it = list.iterator();

                while (it.hasNext()) {
                    UUID uuid = it.next();

                    EntitySearchResult result =
                            findEntityAcrossDimensions(
                                    server,
                                    uuid
                            );

                    if (result != null
                            && result.entity().isAlive()) {

                        teleportToEntity(
                                player,
                                result.level(),
                                result.entity()
                        );

                        teleported = true;
                        break;
                    }

                    /*
                     * No se elimina si result == null porque el chunk
                     * podría estar descargado o no cargado todavía.
                     */
                }
            }

            if (!teleported
                    && SporeAddsConfig.KOMMANDANT_PERMADEATH_NO_RESPAWN.get()) {
                player.setGameMode(GameType.SPECTATOR);
            }
        });
    }

    private record EntitySearchResult(
            Entity entity,
            ServerLevel level
    ) {
    }
}