package com.sporeadds.sporeaddsmod.hive;

import net.neoforged.neoforge.event.tick.ServerTickEvent;

import net.neoforged.fml.common.EventBusSubscriber;

import net.minecraft.core.registries.BuiltInRegistries;

import com.Harbinger.Spore.Sentities.Organoids.Proto;
import com.Harbinger.Spore.Sentities.Organoids.Verwa;
import com.sporeadds.sporeaddsmod.Powers.Levelstats;
import com.sporeadds.sporeaddsmod.Powers.Poder1;
import com.sporeadds.sporeaddsmod.config.SporeAddsConfig;
import com.sporeadds.sporeaddsmod.level.PlayerLevelProvider;
import com.sporeadds.sporeaddsmod.network.NetworkHandle;
import com.sporeadds.sporeaddsmod.network.SyncVervaTravelPacket;
import com.sporeadds.sporeaddsmod.util.OriginSyncUtil;
import com.sporeadds.sporeaddsmod.util.SporeIdentifierUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.EntityMountEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import com.sporeadds.sporeaddsmod.network.PacketDistributor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Camino "give up rojo oscuro" (rendición ante la colmena).
 *
 * Reproduce el mismo formato de transporte que {@code VervaTransportTask}:
 *
 *  0 EMERGE_ORIGIN : nace la verwa de origen y hace su animación de emerger.
 *  1 BURROW_ORIGIN : la verwa recoge al jugador (montado + NO renderizado) y se entierra;
 *                    se espera a que termine esa animación antes de teletransportar.
 *  2 EMERGE_DEST   : ya teletransportado, nace una segunda verwa en el destino y emerge
 *                    (el jugador sigue montado y sin renderizar).
 *  3 BURROW_DEST   : al terminar de emerger la segunda verwa suelta al jugador, ya renderizado,
 *                    se le cambia la clase a kommandant y se le mete en el cocoon; la verwa
 *                    se entierra y se descarta.
 *
 * Durante todo el proceso el jugador NO puede bajarse con shift (se cancela el dismount).
 */
@EventBusSubscriber(modid = "sporeadd", bus = EventBusSubscriber.Bus.GAME)
public final class HiveSurrenderTask {

    private static final String NO_DESPAWN_TAG = "SporeAdds_NoHardFloorDespawn";
    private static final ResourceLocation VERWA_ID = ResourceLocation.fromNamespaceAndPath("spore", "verva");

    private static final double PROTO_SEARCH_RADIUS = 512.0D;
    /** Distancia máxima del cocoon al centro del proto. */
    private static final double DROP_MAX = 10.0D;
    /** Separación mínima libre entre el cuerpo del proto y el cocoon. */
    private static final double DROP_CLEARANCE = 3.0D;
    private static final int COCOON_TICKS = 15 * 20;

    /** Duración de la animación de emerger / enterrarse de la verwa (igual que VervaTransportTask). */
    private static final int EMERGE_TICKS = 40;
    private static final int BURROW_TICKS = 60;

    private static final int STATE_EMERGE_ORIGIN = 0;
    private static final int STATE_BURROW_ORIGIN = 1;
    private static final int STATE_EMERGE_DEST = 2;
    private static final int STATE_BURROW_DEST = 3;

    private static final List<Data> ACTIVE = new ArrayList<>();
    /** Jugadores que ahora mismo NO pueden desmontar (shift bloqueado) durante el transporte. */
    private static final Set<UUID> LOCKED_PLAYERS = ConcurrentHashMap.newKeySet();

    private HiveSurrenderTask() {
    }

    public static boolean isActive(UUID playerId) {
        for (Data d : ACTIVE) {
            if (d.player != null && d.player.getUUID().equals(playerId)) return true;
        }
        return false;
    }

    public static void start(ServerPlayer player) {
        for (Data d : ACTIVE) {
            if (d.player == player) return;
        }

        Data data = new Data(player);
        if (player.level() instanceof ServerLevel level) {
            data.verwa = spawnVerwa(level, player.position());
        }

        if (data.verwa == null) {
            // No se pudo crear la verwa: completar la rendición en el sitio.
            finishSurrender(player);
            return;
        }

        ACTIVE.add(data);
    }

    // ------------------------------------------------------------------ shift-lock

    @SubscribeEvent
    public static void onEntityMount(EntityMountEvent event) {
        if (!event.isDismounting()) return;
        if (!(event.getEntityMounting() instanceof Player player)) return;
        if (LOCKED_PLAYERS.contains(player.getUUID())) {
            event.setCanceled(true);
        }
    }

    // ------------------------------------------------------------------ state machine

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (ACTIVE.isEmpty()) return;

        Iterator<Data> it = ACTIVE.iterator();
        while (it.hasNext()) {
            Data data = it.next();
            ServerPlayer player = data.player;

            boolean gone = player == null
                    || player.hasDisconnected()
                    || !player.isAlive()
                    || !(player.level() instanceof ServerLevel);

            // Antes de state 3 el jugador tiene que seguir "downed"; en state 3 ya se le cambió la clase.
            boolean lostDowned = !gone
                    && data.state < STATE_BURROW_DEST
                    && !HiveDownedManager.isDowned(player.getUUID());

            boolean verwaLost = data.verwa == null || !data.verwa.isAlive();

            if (gone || lostDowned || verwaLost) {
                // Fallback: si perdimos la verwa a mitad pero el jugador sigue vivo y downed,
                // completar la rendición en el sitio para que la secuencia no quede colgada.
                if (!gone && !lostDowned && verwaLost
                        && data.state >= STATE_BURROW_ORIGIN && data.state < STATE_BURROW_DEST
                        && HiveDownedManager.isDowned(player.getUUID())) {
                    unlockAndDismount(player);
                    setHidden(data, player, false);
                    finishSurrender(player);
                }
                cleanup(data);
                it.remove();
                continue;
            }

            ServerLevel level = (ServerLevel) player.level();
            data.ticks++;

            // El modelo/mano del jugador no se renderizan durante los estados montado.
            setHidden(data, player, data.state == STATE_BURROW_ORIGIN || data.state == STATE_EMERGE_DEST);

            switch (data.state) {
                case STATE_EMERGE_ORIGIN -> {
                    if (data.ticks >= EMERGE_TICKS) {
                        // la verwa recoge al jugador y empieza a enterrarse
                        player.startRiding(data.verwa, true);
                        LOCKED_PLAYERS.add(player.getUUID());
                        setHidden(data, player, true);
                        triggerBurrow(data.verwa);
                        data.state = STATE_BURROW_ORIGIN;
                        data.ticks = 0;
                    }
                }
                case STATE_BURROW_ORIGIN -> {
                    if (data.ticks >= BURROW_TICKS) {
                        Vec3 target = computeTarget(level, player);

                        unlockAndDismount(player);
                        data.verwa.discard();
                        data.verwa = null;

                        player.teleportTo(level, target.x, target.y, target.z, player.getYRot(), player.getXRot());

                        Mob dest = spawnVerwa(level, target);
                        if (dest == null) {
                            // no se pudo crear la de destino: terminar en el sitio
                            setHidden(data, player, false);
                            finishSurrender(player);
                            cleanup(data);
                            it.remove();
                            continue;
                        }

                        data.verwa = dest;
                        player.startRiding(dest, true);
                        LOCKED_PLAYERS.add(player.getUUID());
                        setHidden(data, player, true);
                        triggerEmerge(dest);

                        data.state = STATE_EMERGE_DEST;
                        data.ticks = 0;
                    }
                }
                case STATE_EMERGE_DEST -> {
                    if (data.ticks >= EMERGE_TICKS) {
                        // al terminar de emerger, suelta al jugador ya renderizado
                        unlockAndDismount(player);
                        setHidden(data, player, false);
                        finishSurrender(player);
                        triggerBurrow(data.verwa);
                        data.state = STATE_BURROW_DEST;
                        data.ticks = 0;
                    }
                }
                case STATE_BURROW_DEST -> {
                    if (data.ticks >= BURROW_TICKS) {
                        cleanup(data);
                        it.remove();
                    }
                }
                default -> {
                    cleanup(data);
                    it.remove();
                }
            }
        }
    }

    // ------------------------------------------------------------------ steps

    private static Vec3 computeTarget(ServerLevel level, ServerPlayer player) {
        Vec3 origin = player.position();
        Proto proto = ProtoProximity.nearestProto(level, origin, PROTO_SEARCH_RADIUS);

        if (proto == null) {
            Vec3 deathPos = HiveDownedManager.downPosOf(player.getUUID());
            if (deathPos == null) deathPos = origin;
            return computeNoProtoTarget(level, deathPos);
        }

        double minDist = DROP_CLEARANCE + proto.getBbWidth();
        double maxDist = Math.max(minDist + 1.0D, DROP_MAX);
        double angle = level.random.nextDouble() * Math.PI * 2.0D;
        double dist = minDist + level.random.nextDouble() * (maxDist - minDist);

        double tx = proto.getX() + Math.cos(angle) * dist;
        double tz = proto.getZ() + Math.sin(angle) * dist;
        double ty = findGroundY(level, tx, proto.getY(), tz);
        return new Vec3(tx, ty, tz);
    }

    /**
     * Sin proto: se lleva al jugador a la estructura {@code spore:mass_grave}; si no hay,
     * a una posición disponible a más de 800 bloques de donde murió.
     */
    private static Vec3 computeNoProtoTarget(ServerLevel level, Vec3 deathPos) {
        Vec3 massGrave = findMassGraveSpawn(level);
        if (massGrave != null) {
            return massGrave;
        }
        return farPositionFrom(level, deathPos);
    }

    /**
     * Réplica de {@code origins:modify_player_spawn} con {@code "spawn_strategy": "center"}:
     * localiza {@code spore:mass_grave} (desde 0/70/0, radio 100 chunks), coge el CENTRO de su
     * caja de estructura y busca ahí una posición donde el jugador quepa de forma segura
     * ({@code DismountHelper.findSafeDismountLocation}).
     */
    private static Vec3 findMassGraveSpawn(ServerLevel level) {
        try {
            if (level.getServer() == null
                    || !level.getServer().getWorldData().worldGenOptions().generateStructures()) {
                return null;
            }

            net.minecraft.core.Registry<net.minecraft.world.level.levelgen.structure.Structure> registry =
                    level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.STRUCTURE);

            var holderOpt = registry.getHolder(net.minecraft.resources.ResourceKey.create(
                    net.minecraft.core.registries.Registries.STRUCTURE,
                    ResourceLocation.fromNamespaceAndPath("spore", "mass_grave")
            ));
            if (holderOpt.isEmpty()) return null;

            // Igual que Origins: búsqueda desde (0, 70, 0), radio 100 chunks.
            BlockPos searchFrom = new BlockPos(0, 70, 0);
            var pair = level.getChunkSource().getGenerator().findNearestMapStructure(
                    level,
                    net.minecraft.core.HolderSet.direct(holderOpt.get()),
                    searchFrom,
                    100,
                    false
            );
            if (pair == null) return null;

            BlockPos found = pair.getFirst();
            net.minecraft.world.level.levelgen.structure.Structure structure = pair.getSecond().value();

            net.minecraft.world.level.levelgen.structure.StructureStart start =
                    level.structureManager().getStartForStructure(
                            net.minecraft.core.SectionPos.of(new net.minecraft.world.level.ChunkPos(found), 0),
                            structure,
                            level.getChunk(found.getX() >> 4, found.getZ() >> 4)
                    );

            BlockPos center = (start != null && start.isValid())
                    ? new BlockPos(start.getBoundingBox().getCenter())
                    : found;

            Vec3 safe = findValidSpawn(level, center, 16);
            if (safe != null) {
                return safe;
            }

            double y = findGroundY(level, center.getX() + 0.5D, center.getY() + 8, center.getZ() + 0.5D);
            return new Vec3(center.getX() + 0.5D, y, center.getZ() + 0.5D);
        } catch (Throwable t) {
            return null;
        }
    }

    /** Busca en anillos alrededor de {@code center} una posición segura para el jugador. */
    private static Vec3 findValidSpawn(ServerLevel level, BlockPos center, int range) {
        for (int r = 0; r <= range; r++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (r > 0 && Math.abs(dx) != r && Math.abs(dz) != r) continue;   // solo el borde del anillo

                    int x = center.getX() + dx;
                    int z = center.getZ() + dz;
                    level.getChunk(x >> 4, z >> 4);

                    int surface = level.getHeight(
                            net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
                    int top = Math.min(surface + 2, level.getMaxBuildHeight() - 3);
                    int bottom = Math.max(surface - 4, level.getMinBuildHeight() + 1);

                    for (int y = top; y >= bottom; y--) {
                        Vec3 v = net.minecraft.world.entity.vehicle.DismountHelper.findSafeDismountLocation(
                                net.minecraft.world.entity.EntityType.PLAYER, level,
                                new BlockPos(x, y, z), true);
                        if (v != null) return v;
                    }
                }
            }
        }
        return null;
    }

    private static Vec3 farPositionFrom(ServerLevel level, Vec3 deathPos) {
        // Cada intento puede forzar worldgen de un chunk lejano; se mantiene bajo el nº de intentos.
        for (int attempt = 0; attempt < 6; attempt++) {
            double angle = level.random.nextDouble() * Math.PI * 2.0D;
            double dist = 850.0D + level.random.nextDouble() * 450.0D;   // 850..1300
            int tx = Mth.floor(deathPos.x + Math.cos(angle) * dist);
            int tz = Mth.floor(deathPos.z + Math.sin(angle) * dist);

            level.getChunk(tx >> 4, tz >> 4);   // fuerza la carga del chunk
            int ty = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, tx, tz);

            if (ty <= level.getMinBuildHeight() + 1 || ty >= level.getMaxBuildHeight() - 2) continue;

            BlockPos ground = new BlockPos(tx, ty - 1, tz);
            if (!level.getFluidState(ground).isEmpty()) continue;

            return new Vec3(tx + 0.5D, ty, tz + 0.5D);
        }

        // Último recurso: recto al este, a 900 bloques, sobre el heightmap.
        int tx = Mth.floor(deathPos.x) + 900;
        int tz = Mth.floor(deathPos.z);
        level.getChunk(tx >> 4, tz >> 4);
        int ty = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, tx, tz);
        return new Vec3(tx + 0.5D, ty, tz + 0.5D);
    }

    private static Mob spawnVerwa(ServerLevel level, Vec3 pos) {
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(VERWA_ID);
        if (type == null || !(type.create(level) instanceof Mob verwa)) return null;

        verwa.setPos(pos.x, pos.y, pos.z);
        verwa.setPersistenceRequired();
        verwa.setInvulnerable(true);
        verwa.addTag(NO_DESPAWN_TAG);
        verwa.getPersistentData().putBoolean(NO_DESPAWN_TAG, true);

        if (verwa instanceof Verwa verwaEntity) {
            verwaEntity.setStoredMob("spore:scent");
        }

        level.addFreshEntity(verwa);
        triggerEmerge(verwa);
        return verwa;
    }

    private static void triggerEmerge(Entity verwa) {
        if (verwa instanceof Verwa v) v.tickEmerging();
    }

    private static void triggerBurrow(Entity verwa) {
        if (verwa instanceof Verwa v) v.tickBurrowing();
    }

    private static void unlockAndDismount(ServerPlayer player) {
        LOCKED_PLAYERS.remove(player.getUUID());
        if (player.isPassenger()) {
            player.stopRiding();
        }
    }

    private static void finishSurrender(ServerPlayer player) {
        // En una inducción voluntaria (/class kommandant caustic, etc.) se conserva la subclase elegida.
        String keepSubclass = HiveDownedManager.voluntarySubclassOf(player.getUUID());

        SporeIdentifierUtil.setIdentifierAndSync(player, "kommandant");
        SporeIdentifierUtil.setSubclassAndSync(player,
                (keepSubclass != null && !keepSubclass.isEmpty()) ? keepSubclass : "none");

        if (player.getServer() != null) {
            var scoreboard = player.getServer().getScoreboard();
            var sporeTeam = scoreboard.getPlayerTeam("spore");
            if (sporeTeam == null) {
                sporeTeam = scoreboard.addPlayerTeam("spore");
            }
            scoreboard.removePlayerFromTeam(player.getScoreboardName());
            scoreboard.addPlayerToTeam(player.getScoreboardName(), sporeTeam);
        }

        int targetLevel = SporeAddsConfig.HIVE_KOMMANDANT_PLAYER_LEVEL.get();
        int targetKnowledge = SporeAddsConfig.HIVE_KOMMANDANT_KNOWLEDGE_LEVEL.get();
        PlayerLevelProvider.PLAYER_LVL.get(player).ifPresent(cap -> {
            cap.setLevel(targetLevel);
            cap.setKnowledgeLevel(targetKnowledge);
        });

        Levelstats.forceReapplyStats(player);
        OriginSyncUtil.applyForIdentifier(player, "kommandant");

        Poder1.startPlainCocoon(player, COCOON_TICKS);

        HiveDownedManager.onSurrenderCocoonStarted(player);
    }

    /** Escanea hacia abajo desde {@code startY + 4} buscando un bloque sólido con 2 de aire encima. */
    private static double findGroundY(ServerLevel level, double x, double startY, double z) {
        int bx = Mth.floor(x);
        int bz = Mth.floor(z);
        int top = Mth.floor(startY) + 4;
        int bottom = top - 32;

        for (int y = top; y >= bottom; y--) {
            BlockPos pos = new BlockPos(bx, y, bz);
            boolean solid = level.getBlockState(pos).blocksMotion();
            boolean airAbove = !level.getBlockState(pos.above()).blocksMotion()
                    && !level.getBlockState(pos.above(2)).blocksMotion();
            if (solid && airAbove) {
                return y + 1;
            }
        }
        return startY;
    }

    private static void setHidden(Data data, ServerPlayer player, boolean hidden) {
        if (player == null || hidden == data.hidden) return;
        data.hidden = hidden;
        NetworkHandle.INSTANCE.send(
                PacketDistributor.ALL.noArg(),
                new SyncVervaTravelPacket(player.getUUID(), hidden)
        );
    }

    private static void cleanup(Data data) {
        if (data == null) return;
        if (data.player != null) {
            LOCKED_PLAYERS.remove(data.player.getUUID());
        }
        setHidden(data, data.player, false);
        if (data.verwa != null && data.verwa.isAlive()) {
            data.verwa.discard();
        }
    }

    private static final class Data {
        final ServerPlayer player;
        Entity verwa;
        int state;
        int ticks;
        boolean hidden;

        Data(ServerPlayer player) {
            this.player = player;
        }
    }
}
