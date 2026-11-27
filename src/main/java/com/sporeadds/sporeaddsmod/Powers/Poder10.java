package com.sporeadds.sporeaddsmod.Powers;

import com.sporeadds.sporeaddsmod.spore.PlayerSporeProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class Poder10 extends PowerBase {

    private static final int PHASE_COST = 25;
    private static final int MIN_VIGIL_DISTANCE = 15;
    private static final int MAX_VIGIL_DISTANCE = 28;
    private static final int MAX_SPAWN_ATTEMPTS = 32;
    private static final int[] VALID_VIGIL_VARIANTS = {0, 1, 2};

    private static final List<SoundTask> SOUND_TASKS = new ArrayList<>();

    private static class SoundTask {
        public final ServerLevel world;
        public final ServerPlayer player;
        public final ResourceLocation soundRL;
        public final float vol, pitch;
        public int ticksLeft;

        public SoundTask(ServerLevel world, ServerPlayer player, ResourceLocation soundRL, float vol, float pitch, int delayTicks) {
            this.world = world;
            this.player = player;
            this.soundRL = soundRL;
            this.vol = vol;
            this.pitch = pitch;
            this.ticksLeft = delayTicks;
        }
    }

    public static void onServerTick() {
        Iterator<SoundTask> iter = SOUND_TASKS.iterator();
        while (iter.hasNext()) {
            SoundTask task = iter.next();
            task.ticksLeft--;
            if (task.ticksLeft <= 0) {
                playServerSound(task.world, task.player, task.soundRL, task.vol, task.pitch);
                iter.remove();
            }
        }
    }

    public void use(ServerPlayer player) {
        Level level = player.level();
        if (!(level instanceof ServerLevel serverLevel)) return;

        MinecraftServer server = player.getServer();
        if (server == null) return;

        player.getCapability(PlayerSporeProvider.PLAYER_CAP).ifPresent(spore -> {
            int currentPhase = spore.getSpore();
            if (currentPhase < PHASE_COST) {
                player.sendSystemMessage(Component.translatable("message.sporeadd.power1.not_enough_biomass").withStyle(ChatFormatting.DARK_RED));
                return;
            }

            spore.setSpore(currentPhase - PHASE_COST);

            playServerSound(serverLevel, player, new ResourceLocation("spore", "calamity_spawn"), 5.0f, 0.5f);

            ServerPlayer nearestEnemy = null;
            double nearestDistance = Double.MAX_VALUE;

            for (ServerLevel levelLoop : server.getAllLevels()) {
                for (ServerPlayer other : levelLoop.players()) {
                    if (other.getUUID().equals(player.getUUID())) continue;

                    if (other.getTeam() != null && "spore".equals(other.getTeam().getName())) continue;

                    double dist = player.distanceToSqr(other);
                    if (dist < nearestDistance) {
                        nearestDistance = dist;
                        nearestEnemy = other;
                    }
                }
            }

            if (nearestEnemy != null) {
                player.sendSystemMessage(
                        Component.translatable(
                                "message.sporeadd.power10.nearest_player",
                                nearestEnemy.getName().getString(),
                                formatPos(nearestEnemy.blockPosition())
                        ).withStyle(ChatFormatting.DARK_RED)
                );

                applyEffectsToTarget(nearestEnemy, nearestEnemy.serverLevel());

                boolean spawnedVigil = spawnIndividualScanVigil(nearestEnemy);
                if (spawnedVigil) {
                    player.sendSystemMessage(
                            Component.translatable(
                                    "message.sporeadd.power10.vigil_spawned_near",
                                    nearestEnemy.getName()
                            ).withStyle(ChatFormatting.DARK_RED)
                    );
                } else {
                    player.sendSystemMessage(
                            Component.translatable(
                                    "message.sporeadd.power10.vigil_spawn_failed_near",
                                    nearestEnemy.getName()
                            ).withStyle(ChatFormatting.DARK_RED)
                    );
                }
            } else {
                player.sendSystemMessage(Component.translatable("message.sporeadd.power10.no_players").withStyle(ChatFormatting.DARK_RED));
            }

            Set<ServerPlayer> discoveredPlayers = new HashSet<>();

            EntityType<?> protoType = EntityType.byString("spore:proto").orElse(null);
            int protoCount = 0;
            double protoScanRange = 0.0;

            if (protoType != null) {
                List<Entity> protoEntities = new ArrayList<>();

                for (ServerLevel levelLoop : server.getAllLevels()) {
                    for (Entity entity : levelLoop.getAllEntities()) {
                        if (entity.getType().equals(protoType)) {
                            protoEntities.add(entity);
                        }
                    }
                }

                protoCount = protoEntities.size();
                protoScanRange = 250.0 + protoCount * 50.0;

                player.sendSystemMessage(Component.translatable("message.sporeadd.power10.proto_signals", protoCount).withStyle(ChatFormatting.DARK_RED));
                player.sendSystemMessage(Component.translatable("message.sporeadd.power10.scan_radius", protoScanRange).withStyle(ChatFormatting.DARK_RED));

                for (Entity proto : protoEntities) {
                    if (!(proto.level() instanceof ServerLevel protoLevel)) continue;

                    AABB protoBox = proto.getBoundingBox().inflate(protoScanRange);
                    List<ServerPlayer> boxCandidates = protoLevel.getEntitiesOfClass(ServerPlayer.class, protoBox);

                    for (ServerPlayer target : boxCandidates) {
                        if (target.equals(player)) continue;

                        // AÑADIR ESTA LÍNEA PARA IGNORAR AL EQUIPO SPORE
                        if (target.getTeam() != null && "spore".equals(target.getTeam().getName())) continue;

                        double dist = proto.position().distanceTo(target.position());
                        if (dist <= protoScanRange) {
                            applyEffectsToTarget(target, protoLevel);
                            discoveredPlayers.add(target);

                            player.sendSystemMessage(
                                    Component.translatable(
                                            "message.sporeadd.power10.proto_scan_result",
                                            target.getName().getString(),
                                            String.format("%.1f", dist),
                                            formatPos(proto.blockPosition())
                                    ).withStyle(ChatFormatting.DARK_RED)
                            );
                        }
                    }
                }
            } else {
                player.sendSystemMessage(Component.translatable("message.sporeadd.power10.no_protohives").withStyle(ChatFormatting.DARK_RED));
            }

            player.sendSystemMessage(
                    Component.translatable("message.sporeadd.power10.objectives_detected", discoveredPlayers.size())
                            .withStyle(ChatFormatting.DARK_RED)
            );

            if (!discoveredPlayers.isEmpty()) {
                player.sendSystemMessage(Component.translatable("message.sporeadd.power10.objectives_revealed").withStyle(ChatFormatting.DARK_RED));
                for (ServerPlayer p : discoveredPlayers) {
                    player.sendSystemMessage(
                            Component.translatable(
                                    "message.sporeadd.power10.objective_format",
                                    p.getName().getString(),
                                    formatPos(p.blockPosition())
                            ).withStyle(ChatFormatting.DARK_RED)
                    );
                }
            } else {
                player.sendSystemMessage(Component.translatable("message.sporeadd.power10.no_objectives").withStyle(ChatFormatting.DARK_RED));
            }
        });
    }

    private static boolean spawnIndividualScanVigil(ServerPlayer target) {
        ServerLevel level = target.serverLevel();
        EntityType<?> vigilType = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation("spore", "vigil"));
        if (vigilType == null) return false;

        Optional<BlockPos> spawnPosOpt = findValidVigilSpawn(level, target.blockPosition(), MIN_VIGIL_DISTANCE, MAX_VIGIL_DISTANCE);
        if (spawnPosOpt.isEmpty()) return false;

        Entity entity = vigilType.create(level);
        if (!(entity instanceof Mob vigil)) return false;

        BlockPos spawnPos = spawnPosOpt.get();

        vigil.moveTo(
                spawnPos.getX() + 0.5D,
                spawnPos.getY(),
                spawnPos.getZ() + 0.5D,
                level.random.nextFloat() * 360.0F,
                0.0F
        );

        if (!vigil.checkSpawnObstruction(level)) {
            return false;
        }

        vigil.finalizeSpawn(
                level,
                level.getCurrentDifficultyAt(spawnPos),
                MobSpawnType.MOB_SUMMONED,
                null,
                null
        );

        int variant = VALID_VIGIL_VARIANTS[level.random.nextInt(VALID_VIGIL_VARIANTS.length)];
        vigil.getPersistentData().putInt("Variant", variant);

        return level.addFreshEntity(vigil);
    }

    private static Optional<BlockPos> findValidVigilSpawn(ServerLevel level, BlockPos center, int minDistance, int maxDistance) {
        boolean isNether = level.dimension() == Level.NETHER;

        for (int i = 0; i < MAX_SPAWN_ATTEMPTS; i++) {
            double angle = level.random.nextDouble() * (Math.PI * 2.0);
            int distance = minDistance + level.random.nextInt(maxDistance - minDistance + 1);

            int x = center.getX() + (int) Math.round(Math.cos(angle) * distance);
            int z = center.getZ() + (int) Math.round(Math.sin(angle) * distance);

            if (!isNether) {
                BlockPos topPos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(x, center.getY(), z));
                if (isValidSpawnSurface(level, topPos, center, minDistance)) {
                    return Optional.of(topPos);
                }

                for (int dy = 1; dy <= 12; dy++) {
                    BlockPos lower = topPos.below(dy);
                    if (isValidSpawnSurface(level, lower, center, minDistance)) {
                        return Optional.of(lower);
                    }
                }
            } else {
                int minY = Math.max(level.getMinBuildHeight() + 1, center.getY() - 16);
                int maxY = Math.min(level.getMaxBuildHeight() - 2, center.getY() + 8);

                for (int y = maxY; y >= minY; y--) {
                    BlockPos candidate = new BlockPos(x, y, z);
                    if (isValidSpawnSurface(level, candidate, center, minDistance)) {
                        return Optional.of(candidate);
                    }
                }
            }
        }

        return Optional.empty();
    }

    private static boolean isValidSpawnSurface(ServerLevel level, BlockPos spawnPos, BlockPos targetPos, int minDistance) {
        if (spawnPos.distSqr(targetPos) < (double) (minDistance * minDistance)) {
            return false;
        }

        BlockPos below = spawnPos.below();
        BlockState belowState = level.getBlockState(below);
        BlockState state = level.getBlockState(spawnPos);
        BlockState above = level.getBlockState(spawnPos.above());

        if (belowState.isAir()) return false;
        if (!belowState.blocksMotion()) return false;
        if (!state.isAir()) return false;
        if (!above.isAir()) return false;
        if (!level.getWorldBorder().isWithinBounds(spawnPos)) return false;

        return true;
    }

    private static void applyEffectsToTarget(ServerPlayer target, ServerLevel serverLevel) {
        MobEffect glowing = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("minecraft", "glowing"));
        MobEffect marker = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("spore", "marker"));
        MobEffect uneasy = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("spore", "uneasy"));

        if (glowing != null) {
            target.addEffect(new MobEffectInstance(glowing, 2400, 0, false, true));
        }

        if (marker != null) {
            target.addEffect(new MobEffectInstance(marker, 2400, 2, false, true));
        }

        if (uneasy != null) {
            target.addEffect(new MobEffectInstance(uneasy, 6000, 0, false, true));
        }

        target.connection.send(new ClientboundSetTitlesAnimationPacket(20, 100, 20));
        target.connection.send(new ClientboundSetTitleTextPacket(
                Component.translatable("message.sporeadd.power10.found_title").withStyle(style -> style.withColor(0xAA0000))
        ));

        playServerSound(serverLevel, target, new ResourceLocation("spore", "sonar"), 1, 0.8f);
        playServerSound(serverLevel, target, new ResourceLocation("spore", "signal"), 1, 0.8f);
        scheduleDelayedSound(serverLevel, target, new ResourceLocation("spore", "signal"), 1, 0.8f, 32);
    }

    private static void playServerSound(ServerLevel world, ServerPlayer player, ResourceLocation soundRL, float vol, float pitch) {
        SoundEvent sound = ForgeRegistries.SOUND_EVENTS.getValue(soundRL);
        if (sound != null) {
            world.playSound(null, player.getX(), player.getY(), player.getZ(), sound, SoundSource.MASTER, vol, pitch);
        }
    }

    private static void scheduleDelayedSound(ServerLevel world, ServerPlayer player, ResourceLocation soundRL, float vol, float pitch, int delayTicks) {
        SOUND_TASKS.add(new SoundTask(world, player, soundRL, vol, pitch, delayTicks));
    }

    private static String formatPos(BlockPos pos) {
        return "(" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ")";
    }
}