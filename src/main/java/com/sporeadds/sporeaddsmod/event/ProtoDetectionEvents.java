package com.sporeadds.sporeaddsmod.event;

import net.neoforged.fml.common.EventBusSubscriber;

import com.Harbinger.Spore.Sentities.Organoids.Proto;
import com.sporeadds.sporeaddsmod.commands.VervaTransportTask;
import com.sporeadds.sporeaddsmod.config.SporeAddsConfig;
import com.sporeadds.sporeaddsmod.hive.HiveDownedManager;
import com.sporeadds.sporeaddsmod.hive.PunishmentManager;
import com.sporeadds.sporeaddsmod.util.SporeClassUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cuando un {@code spore:proto} fija como objetivo a un humano (misma lógica de detección que
 * usa el propio Proto: {@code NearestAttackableTargetGoal} sobre jugadores con línea de visión
 * dentro de su follow range), la colmena reacciona:
 * <ul>
 *   <li>avisa por chat a los Kommandant llamados de su MISMA dimensión (los más cercanos, hasta
 *       un tope configurable), con distinto mensaje según haya 1 o varios humanos en rango y
 *       según el Kommandant ya esté o no dentro del rango del proto;</li>
 *   <li>a cada Kommandant llamado que aún deba desplazarse le abre la ventana de retorno del
 *       evento "punishment" y le envía una Verwa de transporte <b>única para él</b> que lo lleva
 *       hasta el proto (a diferencia del Vigil, que solo recoge Kommandants de nivel &ge; 7);</li>
 *   <li>si todos los intrusos mueren / se van antes de que expire la ventana, el proto avisa de
 *       que ya se ha ocupado de ellos, cancela los castigos pendientes y reinicia su cooldown.</li>
 * </ul>
 * Un Kommandant que ya ha sido llamado por otro proto (o ya va en una Verwa) no se vuelve a
 * llamar. La Verwa nunca es para el humano detectado. Reutiliza {@link VervaTransportTask}.
 */
@EventBusSubscriber(modid = "sporeadd")
public final class ProtoDetectionEvents {

    /** Última vez (game time) que cada Proto dio la alarma, para no spamear. */
    private static final Map<UUID, Long> LAST_FETCH = new ConcurrentHashMap<>();
    /** Alarmas en curso: protoId -> intrusos vigilados + Kommandants avisados. */
    private static final Map<UUID, IntruderWatch> WATCHED = new ConcurrentHashMap<>();

    private ProtoDetectionEvents() {
    }

    private static final class IntruderWatch {
        final Set<UUID> intruders;           // todos los intrusos conocidos (para el "all clear")
        int knownCount;                       // cuántos había cuando se dio el último aviso
        final ResourceKey<Level> dimension;
        final long expireTick;
        final Set<UUID> calledKommandants;
        boolean escalated;                    // ya se ha llamado a "combate abierto"

        IntruderWatch(Set<UUID> intruders, int knownCount, ResourceKey<Level> dimension,
                      long expireTick, Set<UUID> calledKommandants) {
            this.intruders = intruders;
            this.knownCount = knownCount;
            this.dimension = dimension;
            this.expireTick = expireTick;
            this.calledKommandants = calledKommandants;
        }
    }

    @SubscribeEvent
    public static void onProtoAcquireTarget(LivingChangeTargetEvent event) {
        if (!SporeAddsConfig.PROTO_FETCH_HUMANS.get()) return;

        LivingEntity entity = event.getEntity();
        if (!(entity instanceof Proto proto)) return;
        if (!(proto.level() instanceof ServerLevel protoLevel)) return;
        if (proto.isEmerging()) return;

        if (!(event.getNewTarget() instanceof ServerPlayer human)) return;
        if (human.isSpectator() || human.isCreative()) return;
        if (human.level() != protoLevel) return;

        // Los Kommandant son parte de la colmena: el Proto no los "detecta como humanos".
        if (SporeClassUtil.hasClass(human, "kommandant")) return;
        // Objetivo ya caído (DBNO) -> no vale la pena dar la alarma.
        if (HiveDownedManager.isDowned(human.getUUID())) return;

        MinecraftServer server = proto.getServer();
        if (server == null) return;

        long now = protoLevel.getGameTime();
        Long last = LAST_FETCH.get(proto.getUUID());
        long cooldown = SporeAddsConfig.PROTO_FETCH_HUMANS_COOLDOWN.get();
        if (last != null && now - last < cooldown) return;

        double engageRadius = SporeAddsConfig.PROTO_ENGAGEMENT_RADIUS.get();
        double engageRadiusSq = engageRadius * engageRadius;

        // Intrusos que hay en rango AHORA (el detectado siempre cuenta).
        Set<UUID> intruderIds = scanIntruders(proto, protoLevel, engageRadius);
        intruderIds.add(human.getUUID());

        // Aviso inicial: SIEMPRE en modo "1 humano" (el mensaje de varios humanos solo llega
        // después, por escalada, si aparece otro dentro de la ventana).
        Vec3 dropSpot = findDropSpotNear(protoLevel, proto);
        Set<UUID> calledKommandants = new HashSet<>();
        callKommandants(server, proto, protoLevel, engageRadiusSq, dropSpot,
                false, SporeAddsConfig.PUNISHMENT_MAX_CALLED_KOMMANDANTS.get(), calledKommandants);

        // Nadie a quien llamar en esta dimensión -> no quemamos el cooldown ni vigilamos.
        if (calledKommandants.isEmpty()) return;

        LAST_FETCH.put(proto.getUUID(), now);
        long expire = now + SporeAddsConfig.PUNISHMENT_RETURN_WINDOW.get() + 100L;
        WATCHED.put(proto.getUUID(),
                new IntruderWatch(intruderIds, intruderIds.size(), protoLevel.dimension(), expire, calledKommandants));
    }

    /**
     * Cada tick: si aparece otro intruso dentro de la ventana -> escalada a "combate abierto"
     * (mensaje + llamar a más Kommandants). Si ya no queda ninguno de los intrusos vigilados
     * (muertos / desconectados / cambiaron de dimensión) -> "all clear", sin castigo, cooldown
     * reiniciado.
     */
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || WATCHED.isEmpty()) return;

        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;

        Iterator<Map.Entry<UUID, IntruderWatch>> it = WATCHED.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, IntruderWatch> entry = it.next();
            UUID protoId = entry.getKey();
            IntruderWatch watch = entry.getValue();

            ServerLevel dim = server.getLevel(watch.dimension);
            Entity protoEntity = dim == null ? null : dim.getEntity(protoId);
            if (!(protoEntity instanceof Proto proto) || !proto.isAlive()) {
                it.remove();
                continue;
            }

            double engageRadius = SporeAddsConfig.PROTO_ENGAGEMENT_RADIUS.get();
            Set<UUID> current = scanIntruders(proto, dim, engageRadius);
            watch.intruders.addAll(current);

            // ¿Ha aparecido otro? -> escalada a combate abierto (una sola vez).
            if (!watch.escalated && current.size() > watch.knownCount) {
                watch.escalated = true;
                watch.knownCount = current.size();

                Component brace = Component.translatable("message.sporeadd.proto.multiple_humans")
                        .withStyle(ChatFormatting.DARK_RED);
                for (UUID kid : watch.calledKommandants) {
                    ServerPlayer k = server.getPlayerList().getPlayer(kid);
                    if (k != null) k.sendSystemMessage(brace);
                }
                // El proto intenta reunir a más Kommandants (tope de combate abierto).
                double engageRadiusSq = engageRadius * engageRadius;
                Vec3 dropSpot = findDropSpotNear(dim, proto);
                callKommandants(server, proto, dim, engageRadiusSq, dropSpot,
                        true, SporeAddsConfig.PUNISHMENT_MAX_CALLED_KOMMANDANTS_MULTI.get(), watch.calledKommandants);
            }

            boolean anyThreat = false;
            for (UUID iid : watch.intruders) {
                ServerPlayer intruder = server.getPlayerList().getPlayer(iid);
                if (intruder != null && intruder.isAlive()
                        && intruder.level().dimension().equals(watch.dimension)) {
                    anyThreat = true;
                    break;
                }
            }

            if (!anyThreat) {
                String key = watch.knownCount >= 2
                        ? "message.sporeadd.proto.all_clear.multiple"
                        : "message.sporeadd.proto.all_clear.single";
                Component msg = Component.translatable(key).withStyle(ChatFormatting.DARK_RED);
                for (UUID kid : watch.calledKommandants) {
                    ServerPlayer k = server.getPlayerList().getPlayer(kid);
                    if (k != null) k.sendSystemMessage(msg);
                }
                PunishmentManager.cancelReturnDemandsForProto(protoId);
                LAST_FETCH.remove(protoId); // reinicia el cooldown de aviso
                it.remove();
            } else if (dim.getGameTime() > watch.expireTick) {
                it.remove(); // las ventanas ya se han resuelto por su cuenta
            }
        }
    }

    /** UUIDs de humanos válidos (no-Kommandant, vivos, survival, no DBNO) dentro del rango del proto. */
    private static Set<UUID> scanIntruders(Proto proto, ServerLevel level, double radius) {
        Set<UUID> ids = new HashSet<>();
        AABB box = proto.getBoundingBox().inflate(radius);
        for (ServerPlayer p : level.getEntitiesOfClass(ServerPlayer.class, box,
                p -> p.isAlive() && !p.isSpectator() && !p.isCreative()
                        && !SporeClassUtil.hasClass(p, "kommandant")
                        && !HiveDownedManager.isDowned(p.getUUID()))) {
            ids.add(p.getUUID());
        }
        return ids;
    }

    /**
     * Llama a Kommandants de la dimensión del proto (los más cercanos que aún no estén en
     * {@code calledKommandants}, no llamados por otro proto y no viajando), hasta {@code cap}
     * (0 = sin límite; el tamaño de {@code calledKommandants} cuenta para el tope). A los que
     * aún deban desplazarse les abre la ventana de retorno y les manda una Verwa propia.
     */
    private static void callKommandants(MinecraftServer server, Proto proto, ServerLevel protoLevel,
                                        double engageRadiusSq, Vec3 dropSpot, boolean openCombat,
                                        int cap, Set<UUID> calledKommandants) {
        List<ServerPlayer> kommandants = new ArrayList<>();
        for (ServerPlayer online : server.getPlayerList().getPlayers()) {
            if (online.level() == protoLevel
                    && SporeClassUtil.hasClass(online, "kommandant")
                    && !calledKommandants.contains(online.getUUID())) {
                kommandants.add(online);
            }
        }
        kommandants.sort(Comparator.comparingDouble(p -> p.distanceToSqr(proto)));

        for (ServerPlayer k : kommandants) {
            if (cap > 0 && calledKommandants.size() >= cap) break;
            UUID kid = k.getUUID();
            if (PunishmentManager.isCalled(kid) || VervaTransportTask.isPlayerBeingTransported(kid)) {
                continue;
            }

            boolean alreadyInPosition = k.distanceToSqr(proto) <= engageRadiusSq;
            String key = openCombat
                    ? "message.sporeadd.proto.multiple_humans"
                    : (alreadyInPosition
                    ? "message.sporeadd.proto.human_approaching_in_range"
                    : "message.sporeadd.proto.human_detected");
            k.sendSystemMessage(Component.translatable(key).withStyle(ChatFormatting.DARK_RED));
            calledKommandants.add(kid);

            if (!alreadyInPosition) {
                PunishmentManager.registerReturnDemand(k, proto);
                if (!HiveDownedManager.isDowned(kid)) {
                    VervaTransportTask.startTask(k, k.position(), dropSpot, protoLevel, -100);
                }
            }
        }
    }

    /** Punto pisable más cercano al Proto donde soltar al jugador; si no hay, justo encima de él. */
    private static Vec3 findDropSpotNear(ServerLevel level, Proto proto) {
        BlockPos base = proto.blockPosition();

        for (int radius = 1; radius <= 5; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != radius) continue; // solo el anillo exterior
                    BlockPos feet = findStandable(level, base.offset(dx, 0, dz), 6);
                    if (feet != null) {
                        return new Vec3(feet.getX() + 0.5D, feet.getY(), feet.getZ() + 0.5D);
                    }
                }
            }
        }

        return new Vec3(proto.getX(), proto.getY() + 1.0D, proto.getZ());
    }

    /** Busca hacia abajo y luego hacia arriba una posición con 2 bloques de aire y suelo sólido. */
    private static BlockPos findStandable(ServerLevel level, BlockPos start, int vertRange) {
        for (int dy = 0; dy >= -vertRange; dy--) {
            BlockPos feet = start.offset(0, dy, 0);
            if (isStandable(level, feet)) return feet;
        }
        for (int dy = 1; dy <= vertRange; dy++) {
            BlockPos feet = start.offset(0, dy, 0);
            if (isStandable(level, feet)) return feet;
        }
        return null;
    }

    private static boolean isStandable(ServerLevel level, BlockPos feet) {
        return level.getBlockState(feet).getCollisionShape(level, feet).isEmpty()
                && level.getBlockState(feet.above()).getCollisionShape(level, feet.above()).isEmpty()
                && !level.getBlockState(feet.below()).getCollisionShape(level, feet.below()).isEmpty();
    }
}
