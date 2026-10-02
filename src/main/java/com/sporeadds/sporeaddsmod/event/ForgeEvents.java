package com.sporeadds.sporeaddsmod.event;

import net.neoforged.neoforge.event.tick.ServerTickEvent;

import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import net.neoforged.fml.common.EventBusSubscriber;

import net.minecraft.core.registries.BuiltInRegistries;

import com.Harbinger.Spore.Sentities.Organoids.Verwa;
import com.Harbinger.Spore.Sentities.VariantKeeper;
import com.mojang.brigadier.CommandDispatcher;
import com.sporeadds.sporeaddsmod.ModItems;
import com.sporeadds.sporeaddsmod.PlayerData.PlayerDataProvider;
import com.sporeadds.sporeaddsmod.PlayerData.ScientistResearchProvider;
import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider;
import com.sporeadds.sporeaddsmod.capabilities.CompoundsCapability;
import com.sporeadds.sporeaddsmod.capabilities.PlayerImplantsCapability;
import com.sporeadds.sporeaddsmod.commands.*;
import com.sporeadds.sporeaddsmod.config.SporeAddsConfig;
import com.sporeadds.sporeaddsmod.effects.effects;
import com.sporeadds.sporeaddsmod.level.PlayerLevelProvider;
import com.sporeadds.sporeaddsmod.mound.MoundRemovalHelper;
import com.sporeadds.sporeaddsmod.network.NetworkHandle;
import com.sporeadds.sporeaddsmod.network.ServerToData;
import com.sporeadds.sporeaddsmod.network.SyncLevelPacket;
import com.sporeadds.sporeaddsmod.network.SyncMoundCountPacket;
import com.sporeadds.sporeaddsmod.network.SyncSporeIdentifierPacket;
import com.sporeadds.sporeaddsmod.network.SyncSporePacket;
import com.sporeadds.sporeaddsmod.spore.PlayerSporeProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingHurtEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import com.sporeadds.sporeaddsmod.network.PacketDistributor;

import java.util.List;
import java.util.UUID;

import static com.sporeadds.sporeaddsmod.Powers.Poder1.mountingMap;
import static com.sporeadds.sporeaddsmod.Powers.Poder1.timers;


@EventBusSubscriber(modid = "sporeadd", bus = EventBusSubscriber.Bus.GAME)
public class ForgeEvents {

    private static final long MOUND_CLEANUP_INTERVAL = 200L;

    private static final java.util.Map<java.util.UUID, Integer> pendingLoginPenalties = new java.util.concurrent.ConcurrentHashMap<>();

    private static void syncSporeIdentifier(ServerPlayer player) {
        SporeIdentifierProvider.SPORE_IDENTIFIER.get(player).ifPresent(data -> {
            NetworkHandle.INSTANCE.send(
                    PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                    new SyncSporeIdentifierPacket(
                            player.getId(),
                            data.getIdentifier(),
                            data.getSubclass()
                    )
            );
        });
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity().hasEffect(effects.VULNERABLE.get())) {
            event.setAmount(event.getAmount() * 1.25f);
        }
    }

    @SubscribeEvent
    public static void onAbyssalRiptideHurt(LivingHurtEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        if (!(event.getSource().getEntity() instanceof Player player)) return;

        var subjugation = BuiltInRegistries.MOB_EFFECT.get(ResourceLocation.fromNamespaceAndPath("sporeadd", "subjugation"));
        if (subjugation == null || !player.hasEffect(subjugation)) return;

        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.isEmpty() || !mainHand.hasTag() || !mainHand.getTag().getBoolean("AbyssalTempTrident")) return;

        if (!player.isAutoSpinAttack()) return;

        event.setAmount(event.getAmount() + 30.0F);
    }

    /**
     * Aplica el comportamiento de implantes por muerte, pero SOLO si la muerte se confirmó
     * (el jugador sigue muerto y no está en el modo "downed" de la Colmena). Se invoca de
     * forma diferida al final del tick desde {@link #onLivingDeath}.
     */
    private static void applyImplantDeathBehaviorIfDead(ServerPlayer player) {
        if (player.isAlive()) return;   // tótem / defibrillation / downed revirtieron la muerte
        if (com.sporeadds.sporeaddsmod.hive.HiveDownedManager.isDowned(player.getUUID())) return;

        PlayerImplantsCapability.PLAYER_IMPLANTS.get(player).ifPresent(store -> {
            CompoundTag tag = store.serializeNBT(player.level().registryAccess());
            String behavior = SporeAddsConfig.IMPLANT_DEATH_BEHAVIOR.get().toLowerCase();
            boolean known = "keep".equals(behavior) || "drop".equals(behavior) || "clear".equals(behavior);

            if ("keep".equals(behavior) || "drop".equals(behavior) || !known) {
                ImplantDeathCache.store(player.getUUID(), tag);
            }

            if ("drop".equals(behavior)) {
                for (PlayerImplantsCapability.ImplantType type : PlayerImplantsCapability.ImplantType.values()) {
                    ItemStack stack = store.getImplant(type);
                    if (!stack.isEmpty()) {
                        player.spawnAtLocation(stack.copy());
                    }
                }
                store.clearAllImplants();
            } else if ("clear".equals(behavior) || !known) {
                store.clearAllImplants();
            }
        });
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer victimPlayer) {
            // Los implantes se procesan al final del tick y SOLO si la muerte se confirma.
            // Tótems, delayed_defibrillation y el modo "downed" de la Colmena pueden revertir
            // la muerte DESPUÉS de este evento; en ese caso el jugador NO debe perder implantes.
            final ServerPlayer vp = victimPlayer;
            if (vp.server != null) {
                vp.server.execute(() -> applyImplantDeathBehaviorIfDead(vp));
            }

            if (event.getSource().getEntity() instanceof ServerPlayer killer && killer != victimPlayer) {
                boolean victimIsKommandant = SporeIdentifierProvider.SPORE_IDENTIFIER.get(victimPlayer)
                        .map(data -> "kommandant".equalsIgnoreCase(data.getIdentifier()))
                        .orElse(false);

                boolean killerIsScientist = SporeIdentifierProvider.SPORE_IDENTIFIER.get(killer)
                        .map(data -> "scientist".equalsIgnoreCase(data.getIdentifier()))
                        .orElse(false);

                if (victimIsKommandant && killerIsScientist) {
                    ScientistResearchProvider.SCIENTIST_RESEARCH.get(killer).ifPresent(research -> {
                        com.sporeadds.sporeaddsmod.research.ResearchEventHelper.recordKill(
                                killer, research, com.sporeadds.sporeaddsmod.research.TrackedEntities.KOMMANDANT_PLAYER_ID
                        );
                    });

                    com.sporeadds.sporeaddsmod.items.ScalpelItem.onScientistKillPlayer(killer, victimPlayer, killer.getMainHandItem());
                }
            }

            return;
        }

        if (!(event.getSource().getEntity() instanceof ServerPlayer killer)) return;

        boolean isScientist = SporeIdentifierProvider.SPORE_IDENTIFIER.get(killer)
                .map(data -> "scientist".equalsIgnoreCase(data.getIdentifier()))
                .orElse(false);

        if (!isScientist) return;

        Entity victim = event.getEntity();
        String entityId = BuiltInRegistries.ENTITY_TYPE.getKey(victim.getType()).toString();

        if (!com.sporeadds.sporeaddsmod.research.TrackedEntities.ENTITY_IDS.contains(entityId)) return;

        ScientistResearchProvider.SCIENTIST_RESEARCH.get(killer).ifPresent(research -> {
            com.sporeadds.sporeaddsmod.research.ResearchEventHelper.recordKill(killer, research, entityId);
        });

        com.sporeadds.sporeaddsmod.items.ScalpelItem.onScientistKill(killer, (net.minecraft.world.entity.LivingEntity) victim, killer.getMainHandItem());
    }

    @SubscribeEvent
    public static void onPlayerUseBed(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;

        if (player.level().getBlockState(event.getPos()).getBlock() instanceof BedBlock) {
            if (isPlayerInTeamSpore(player)) {
                event.setCanceled(true);
                player.sendSystemMessage(
                        Component.translatable("message.sporeadd.general.dont_waste_time")
                                .withStyle(ChatFormatting.DARK_RED)
                );
            }
        }
    }

    private static boolean isPlayerInTeamSpore(Player player) {
        return player.getTeam() != null && "spore".equalsIgnoreCase(player.getTeam().getName());
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        // Player data lives in data attachments; NeoForge copies them on clone (copyOnDeath decides what survives death).

        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerDataProvider.PLAYER_DATA.get(player).ifPresent(data -> {
                data.setArmorHpAndSync(data.getArmorHp(), player);
            });

            PlayerSporeProvider.PLAYER_CAP.get(player).ifPresent(spore -> {
                CompoundTag nbt = new CompoundTag();
                spore.saveNBTData(nbt);
                NetworkHandle.INSTANCE.send(
                        PacketDistributor.PLAYER.with(() -> player),
                        new SyncMoundCountPacket(nbt)
                );
            });

            syncSporeIdentifier(player);
            // La clase recién copiada es la buena: que la sync inversa origin->clase no la
            // degrade a "none" mientras Origins termina de restaurar su contenedor.
            com.sporeadds.sporeaddsmod.util.OriginSyncUtil.beginSettleGrace(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        com.sporeadds.sporeaddsmod.util.OriginSyncUtil.beginSettleGrace(player);

        if (SporeAddsConfig.shouldKeepImplantsOnDeath()) {
            CompoundTag cached = ImplantDeathCache.take(player.getUUID());
            if (cached != null) {
                PlayerImplantsCapability.PLAYER_IMPLANTS.get(player).ifPresent(newStore -> {
                    newStore.deserializeNBT(player.level().registryAccess(), cached);
                });
            }
        } else {
            PlayerImplantsCapability.PLAYER_IMPLANTS.get(player).ifPresent(newStore -> {
                newStore.clearAllImplants();
            });
        }

        PlayerDataProvider.PLAYER_DATA.get(player).ifPresent(data -> {
            data.setArmorHpAndSync(data.getArmorHp(), player);
        });

        PlayerSporeProvider.PLAYER_CAP.get(player).ifPresent(spore -> {
            CompoundTag nbt = new CompoundTag();
            spore.saveNBTData(nbt);
            NetworkHandle.INSTANCE.send(
                    PacketDistributor.PLAYER.with(() -> player),
                    new SyncMoundCountPacket(nbt)
            );
        });

        PlayerLevelProvider.PLAYER_LVL.get(player).ifPresent(level -> {
            SyncLevelPacket.syncLevelToClient(player);
        });

        syncSporeIdentifier(player);
    }

    @SubscribeEvent
    public static void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            com.sporeadds.sporeaddsmod.util.OriginSyncUtil.beginSettleGrace(player);

            PlayerDataProvider.PLAYER_DATA.get(player).ifPresent(data -> {
                data.setArmorHpAndSync(data.getArmorHp(), player);
            });

            PlayerSporeProvider.PLAYER_CAP.get(player).ifPresent(spore -> {
                CompoundTag nbt = new CompoundTag();
                spore.saveNBTData(nbt);
                NetworkHandle.INSTANCE.send(
                        PacketDistributor.PLAYER.with(() -> player),
                        new SyncMoundCountPacket(nbt)
                );
            });

            syncSporeIdentifier(player);
        }
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        SetSporePhase.register(dispatcher);
        SetLevelPhase.register(dispatcher);
        VervaRequestCommand.register(event.getDispatcher());
        VervaCommand.register(event.getDispatcher());
        SporeAddsCommand.register(dispatcher);
        EyesCommand.register(event.getDispatcher());
        BlomfungTweakCommand.register(dispatcher);
        PelletTweakCommand.register(dispatcher);
        SporeAddsTestCommand.register(dispatcher);

        com.sporeadds.sporeaddsmod.commands.MoundCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {

        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            PlayerSporeProvider.PLAYER_CAP.get(player).ifPresent(spore -> SyncSporePacket.syncManaToClient(player));
            PlayerLevelProvider.PLAYER_LVL.get(player).ifPresent(level -> SyncLevelPacket.syncLevelToClient(player));
        }

        long gameTime = event.getServer().overworld().getGameTime();
        if (gameTime % MOUND_CLEANUP_INTERVAL == 0) {
            MoundRemovalHelper.cleanupMissingMounds(event.getServer());
        }
    }

    @SubscribeEvent
    public static void onLivingSpawned(EntityJoinLevelEvent event) {
        if (event.getEntity() != null) {
            Entity entity = event.getEntity();

            if (entity instanceof VariantKeeper variantKeeper && !entity.level().isClientSide()) {
                AABB searchBox = entity.getBoundingBox().inflate(0.5D);
                List<Verwa> nearbyVerwas = entity.level().getEntitiesOfClass(Verwa.class, searchBox);

                for (Verwa verwa : nearbyVerwas) {
                    if (verwa.getPersistentData().contains("SporeAddsForceVariant")) {
                        int forcedVariant = verwa.getPersistentData().getInt("SporeAddsForceVariant");

                        if (forcedVariant != -1) {
                            variantKeeper.setVariant(forcedVariant);
                            verwa.getPersistentData().remove("SporeAddsForceVariant");
                            break;
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        PlayerDataProvider.PLAYER_DATA.get(event.getEntity()).ifPresent(data -> data.setSwitch("000000000"));
        NetworkHandle.INSTANCE.send(
                PacketDistributor.PLAYER.with(() -> (ServerPlayer) event.getEntity()),
                new ServerToData("000000000")
        );

        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerDataProvider.PLAYER_DATA.get(player).ifPresent(data -> {
                data.setArmorHpAndSync(data.getArmorHp(), player);
            });

            PlayerSporeProvider.PLAYER_CAP.get(player).ifPresent(spore -> {
                CompoundTag nbt = new CompoundTag();
                spore.saveNBTData(nbt);
                NetworkHandle.INSTANCE.send(
                        PacketDistributor.PLAYER.with(() -> player),
                        new SyncMoundCountPacket(nbt)
                );
            });

            syncSporeIdentifier(player);

            if (SporeAddsConfig.TRAINING_BOOK_GIVE_ON_FIRST_JOIN.get()) {
                SporeIdentifierProvider.SPORE_IDENTIFIER.get(player).ifPresent(data -> {
                    if (!data.hasReceivedTrainingBook()) {
                        data.setReceivedTrainingBook(true);
                        ItemStack initialBook = new ItemStack(ModItems.TRAINING_BOOK.get());
                        com.sporeadds.sporeaddsmod.items.TrainingBookItem.markInitial(initialBook);
                        player.getInventory().add(initialBook);
                    }
                });
            }

            var subjugation = BuiltInRegistries.MOB_EFFECT.get(ResourceLocation.fromNamespaceAndPath("sporeadd", "subjugation"));
            if (subjugation != null && player.hasEffect(subjugation)) {
                final int levelsToLose = SporeAddsConfig.NUKE_LEVEL_PENALTY.get() / 2;

                if (levelsToLose > 0) {
                    PlayerLevelProvider.PLAYER_LVL.get(player).ifPresent(levelData -> {
                        int newLevel = Math.max(0, levelData.getLevel() - levelsToLose);
                        levelData.setLevel(newLevel);
                    });
                    player.sendSystemMessage(Component.translatable("message.sporeadd.power13.grow_weaker").withStyle(ChatFormatting.RED));
                }

                player.removeEffect(subjugation);
            }

            var anticipation = BuiltInRegistries.MOB_EFFECT.get(ResourceLocation.fromNamespaceAndPath("sporeadd", "anticipation"));
            if (anticipation != null && player.hasEffect(anticipation)) {
                int levelsToLose = SporeAddsConfig.NUKE_LEVEL_PENALTY.get();

                if (levelsToLose > 0) {
                    PlayerLevelProvider.PLAYER_LVL.get(player).ifPresent(levelData -> {
                        int newLevel = Math.max(0, levelData.getLevel() - levelsToLose);
                        levelData.setLevel(newLevel);
                    });
                    player.sendSystemMessage(Component.translatable("message.sporeadd.power13.grow_weaker").withStyle(ChatFormatting.RED));
                }

                player.removeEffect(anticipation);
            }

            boolean interruptedCocoon = player.getPersistentData().getBoolean("SporeCocoonInterrupted");

            if (interruptedCocoon) {
                pendingLoginPenalties.put(player.getUUID(), 60);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLogOut(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        UUID uuid = player.getUUID();

        pendingLoginPenalties.remove(uuid);

        if (timers.containsKey(uuid)) {
            int timeLeft = timers.get(uuid);
            player.getPersistentData().putInt("SporeCocoonTimeLeft", timeLeft);
            player.getPersistentData().putBoolean("SporeCocoonInterrupted", true);

            timers.remove(uuid);
            Entity ridden = mountingMap.remove(uuid);
            if (ridden != null && ridden.isAlive()) {
                ridden.discard();
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTickDelay(PlayerTickEvent.Post event) {
        if (event.getEntity().level().isClientSide()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        java.util.UUID uuid = player.getUUID();

        if (pendingLoginPenalties.containsKey(uuid)) {
            int ticksLeft = pendingLoginPenalties.get(uuid);

            if (ticksLeft <= 0) {
                pendingLoginPenalties.remove(uuid);

                if (player.getPersistentData().getBoolean("SporeCocoonInterrupted")) {
                    player.sendSystemMessage(Component.translatable("message.sporeadd.power1.cocoon_interrupted"));
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 * 20, 1));

                    PlayerLevelProvider.PLAYER_LVL.get(player).ifPresent(levelCap -> {
                        if (levelCap.getLevel() >= 9) {
                            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 20 * 20, 2));
                        }
                    });

                    player.getPersistentData().remove("SporeCocoonInterrupted");
                    player.getPersistentData().remove("SporeCocoonTimeLeft");
                }

            } else {
                pendingLoginPenalties.put(uuid, ticksLeft - 1);
            }
        }
    }
}