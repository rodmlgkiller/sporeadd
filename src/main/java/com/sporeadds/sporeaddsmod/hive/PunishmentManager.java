package com.sporeadds.sporeaddsmod.hive;

import net.minecraft.core.Holder;

import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import net.neoforged.neoforge.event.tick.ServerTickEvent;

import net.neoforged.fml.common.EventBusSubscriber;

import net.minecraft.core.registries.BuiltInRegistries;

import com.Harbinger.Spore.Sentities.Organoids.Proto;
import com.sporeadds.sporeaddsmod.Damage.Damagetypes2;
import com.sporeadds.sporeaddsmod.config.SporeAddsConfig;
import com.sporeadds.sporeaddsmod.effects.effects;
import com.sporeadds.sporeaddsmod.particles.GoreParticleData;
import com.sporeadds.sporeaddsmod.particles.SporeaddParticleTypes;
import com.sporeadds.sporeaddsmod.util.SporeClassUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Evento "punishment" del Proto contra jugadores Kommandant. Solo actúa mientras haya al menos
 * un {@code spore:proto} vivo en la dimensión del jugador.
 *
 * <p>Infracciones (+1 punto cada una, con {@link PunishmentTracker} persistente):
 * <ol>
 *   <li>golpear a un {@code spore:proto} o a un {@code spore:reconstructor} (con cooldown), salvo
 *       que haya un humano no-Kommandant cerca del golpeado (posible escudo humano: se asume que
 *       el golpe iba dirigido a él).</li>
 *   <li>tras el aviso de un proto (ver {@code ProtoDetectionEvents}), no acercarse al proto ni
 *       una vez dentro de la ventana configurada.</li>
 * </ol>
 *
 * <p>Escalada:
 * <ul>
 *   <li>1 punto: punishment I durante 10 s + "[Proto]:What do you think you are doing?" + FX.</li>
 *   <li>2 puntos: punishment II durante 20 s + daño corriente y curable (con animación de golpe)
 *       que atraviesa Poder12 y solo baja al jugador hasta el suelo de corazones configurado
 *       (no puede matar) + "[Proto]: Dont fail me again, this is your last chance".</li>
 *   <li>3 puntos: punishment III durante 10 s drenando vida hasta la muerte, ignorando tótems
 *       + "[Proto]:You were just a waste of time and resources till the very end". Configurable si
 *       mata y si impide el respawn (modo espectador, estilo hardcore).</li>
 * </ul>
 *
 * <p>Los puntos también bajan solos: 1 punto por cada {@code punishment_decay_interval_ticks}
 * que un Kommandant conectado pase con puntos &gt; 0 (el reloj se pausa mientras está desconectado
 * o mientras tiene un drenaje letal del 3er strike en curso).
 */
@EventBusSubscriber(modid = "sporeadd")
public final class PunishmentManager {

    private static final ResourceLocation RECONSTRUCTOR_ID = ResourceLocation.fromNamespaceAndPath("spore", "reconstructor");

    /** Cooldown (gametime) del punto por golpear al proto/reconstructor, por jugador. */
    private static final Map<UUID, Long> LAST_HIT_POINT = new ConcurrentHashMap<>();
    /** Kommandants avisados por un proto y su fecha límite para acercarse. */
    private static final Map<UUID, ReturnDemand> RETURN_PENDING = new ConcurrentHashMap<>();
    /** Kommandants en drenaje letal por el 3er punto: uuid -> gametime en que termina. */
    private static final Map<UUID, Long> LETHAL_DRAIN = new ConcurrentHashMap<>();

    private PunishmentManager() {
    }

    private record ReturnDemand(UUID protoId, long deadline, ResourceKey<Level> dimension) {
    }

    // ------------------------------------------------------------------ API

    /**
     * Llamado desde {@code ProtoDetectionEvents} para cada Kommandant avisado por el proto.
     * Devuelve {@code true} solo si se ha abierto una ventana nueva (no la tenía ya).
     */
    public static boolean registerReturnDemand(ServerPlayer kommandant, Proto proto) {
        if (!SporeAddsConfig.PUNISHMENT_EVENT_ENABLED.get()) return false;
        if (!(kommandant.level() instanceof ServerLevel level)) return false;

        UUID id = kommandant.getUUID();
        if (RETURN_PENDING.containsKey(id)) return false; // ya está en el reloj

        long deadline = level.getGameTime() + SporeAddsConfig.PUNISHMENT_RETURN_WINDOW.get();
        RETURN_PENDING.put(id, new ReturnDemand(proto.getUUID(), deadline, level.dimension()));
        return true;
    }

    /** ¿Este Kommandant ya tiene una ventana de retorno abierta (llamado por algún proto)? */
    public static boolean isCalled(UUID kommandantId) {
        return RETURN_PENDING.containsKey(kommandantId);
    }

    /**
     * Cancela sin castigo todas las ventanas de retorno que apuntaban a ese proto (usado cuando
     * el proto ya se ha ocupado del intruso: "you can continue as normal").
     */
    public static void cancelReturnDemandsForProto(UUID protoId) {
        RETURN_PENDING.entrySet().removeIf(e -> e.getValue().protoId().equals(protoId));
    }

    // ------------------------------------------------------------------ infracción 1: golpear

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void onKommandantHitsHive(LivingDamageEvent.Pre event) {
        if (!SporeAddsConfig.PUNISHMENT_EVENT_ENABLED.get()) return;
        if (!(event.getSource().getEntity() instanceof ServerPlayer attacker)) return;
        if (!SporeClassUtil.hasClass(attacker, "kommandant")) return;

        LivingEntity victim = event.getEntity();
        boolean isHiveTarget = victim instanceof Proto
                || RECONSTRUCTOR_ID.equals(BuiltInRegistries.ENTITY_TYPE.getKey(victim.getType()));
        if (!isHiveTarget) return;

        if (!(attacker.level() instanceof ServerLevel level)) return;
        if (!ProtoProximity.anyProtoAliveInDimension(level)) return;

        // Protección contra "escudos humanos": si hay un humano cerca del proto/womb golpeado,
        // se asume que el Kommandant intentaba darle a él y no se castiga por el golpe fallido.
        if (hasNearbyHumanShield(victim, level)) return;

        long now = level.getGameTime();
        Long last = LAST_HIT_POINT.get(attacker.getUUID());
        if (last != null && now - last < SporeAddsConfig.PUNISHMENT_HIT_COOLDOWN.get()) return;
        LAST_HIT_POINT.put(attacker.getUUID(), now);

        addPoint(attacker);
    }

    /** ¿Hay algún humano (no-Kommandant, vivo, survival, no DBNO) cerca de la entidad golpeada? */
    private static boolean hasNearbyHumanShield(LivingEntity hiveEntity, ServerLevel level) {
        double radius = SporeAddsConfig.PUNISHMENT_SHIELD_CHECK_RADIUS.get();
        AABB box = hiveEntity.getBoundingBox().inflate(radius);
        return !level.getEntitiesOfClass(ServerPlayer.class, box,
                p -> p.isAlive() && !p.isSpectator() && !p.isCreative()
                        && !SporeClassUtil.hasClass(p, "kommandant")
                        && !HiveDownedManager.isDowned(p.getUUID())).isEmpty();
    }

    // ------------------------------------------------------------------ tick: ventanas + drenaje

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {

        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;

        if (!RETURN_PENDING.isEmpty()) tickReturnDemands(server);
        if (!LETHAL_DRAIN.isEmpty()) tickLethalDrain(server);
        tickPunishmentDecay(server);
    }

    /**
     * Baja 1 punto de castigo por cada {@code punishment_decay_interval_ticks} que un Kommandant
     * conectado pase con puntos > 0 (mientras no tenga un drenaje letal del 3er strike en curso).
     * Solo avanza estando online; el reloj se pausa mientras el jugador está desconectado.
     */
    private static void tickPunishmentDecay(MinecraftServer server) {
        long interval = SporeAddsConfig.PUNISHMENT_DECAY_INTERVAL_TICKS.get();
        if (interval <= 0 || server.getTickCount() % 20 != 0) return;

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (LETHAL_DRAIN.containsKey(player.getUUID())) continue;

            int points = PunishmentTracker.getPoints(player);
            if (points <= 0) {
                if (PunishmentTracker.getDecayCheckpoint(player) != 0L) {
                    PunishmentTracker.setDecayCheckpoint(player, 0L);
                }
                continue;
            }

            long now = player.level().getGameTime();
            long checkpoint = PunishmentTracker.getDecayCheckpoint(player);
            if (checkpoint <= 0L) {
                PunishmentTracker.setDecayCheckpoint(player, now);
                continue;
            }

            boolean changed = false;
            while (points > 0 && now - checkpoint >= interval) {
                points--;
                checkpoint += interval;
                changed = true;
            }

            if (changed) {
                PunishmentTracker.setPoints(player, points);
                PunishmentTracker.setDecayCheckpoint(player, points > 0 ? checkpoint : 0L);
            }
        }
    }

    private static void tickReturnDemands(MinecraftServer server) {
        Iterator<Map.Entry<UUID, ReturnDemand>> it = RETURN_PENDING.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, ReturnDemand> entry = it.next();
            ReturnDemand demand = entry.getValue();

            ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
            if (player == null || !player.isAlive()) {
                it.remove();
                continue;
            }

            ServerLevel protoDim = server.getLevel(demand.dimension());
            Entity protoEntity = protoDim == null ? null : protoDim.getEntity(demand.protoId());
            if (!(protoEntity instanceof Proto proto) || !proto.isAlive()) {
                it.remove(); // sin proto no hay castigo
                continue;
            }

            // ¿Entró en el rango del proto (el mismo con el que detecta jugadores)?
            double radius = SporeAddsConfig.PROTO_ENGAGEMENT_RADIUS.get();
            if (player.level() == protoDim && player.distanceToSqr(proto) <= radius * radius) {
                it.remove(); // demanda satisfecha
                message(player, "message.sporeadd.proto.hold_position");
                continue;
            }

            if (protoDim.getGameTime() >= demand.deadline()) {
                it.remove();
                // No se castiga si el proto ya se ha ocupado de los intrusos: solo cuenta como
                // falta si a la hora de comprobar SIGUE habiendo un humano dentro de su rango.
                if (protoHasIntruderInRange(proto, protoDim)) {
                    addPoint(player);
                } else {
                    message(player, "message.sporeadd.proto.all_clear.single");
                }
            }
        }
    }

    /** ¿Hay algún humano (no-Kommandant, vivo, survival, no DBNO) dentro del rango del proto? */
    private static boolean protoHasIntruderInRange(Proto proto, ServerLevel level) {
        double radius = SporeAddsConfig.PROTO_ENGAGEMENT_RADIUS.get();
        AABB box = proto.getBoundingBox().inflate(radius);
        return !level.getEntitiesOfClass(ServerPlayer.class, box,
                p -> p.isAlive() && !p.isSpectator() && !p.isCreative()
                        && !SporeClassUtil.hasClass(p, "kommandant")
                        && !HiveDownedManager.isDowned(p.getUUID())).isEmpty();
    }

    private static void tickLethalDrain(MinecraftServer server) {
        Iterator<Map.Entry<UUID, Long>> it = LETHAL_DRAIN.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Long> entry = it.next();
            ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());

            if (player == null || player.isRemoved()) {
                it.remove();
                continue;
            }

            long deadline = entry.getValue();
            long now = player.level().getGameTime();

            // Salvaguarda: si algo lleva 10 s cancelando la muerte, abandonamos el bucle.
            if (now > deadline + 200L) {
                it.remove();
                continue;
            }

            boolean timeUp = now >= deadline || player.getEffect(effects.PUNISHMENT) == null;
            float drain = Math.max(0.25F, player.getMaxHealth() / 160.0F);
            float next = player.getHealth() - drain;

            if (timeUp || next <= 0.5F) {
                // La entrada se limpia en onPunishmentDeath cuando la muerte se confirma.
                forceKill(player);
            } else {
                player.setHealth(next);
            }
        }
    }

    // ------------------------------------------------------------------ aplicación de puntos

    private static void addPoint(ServerPlayer player) {
        if (!SporeAddsConfig.PUNISHMENT_EVENT_ENABLED.get()) return;
        if (!SporeClassUtil.hasClass(player, "kommandant")) return;
        if (!(player.level() instanceof ServerLevel level)) return;
        if (!ProtoProximity.anyProtoAliveInDimension(level)) return;

        int points = PunishmentTracker.getPoints(player) + 1;
        PunishmentTracker.setPoints(player, points);

        Holder<MobEffect> punishment = effects.PUNISHMENT;

        if (points == 1) {
            player.addEffect(new MobEffectInstance(punishment, 200, 0, false, true, true));
            message(player, "message.sporeadd.punishment.strike1");
            burstAround(level, player, 0);
        } else if (points == 2) {
            player.addEffect(new MobEffectInstance(punishment, 400, 1, false, true, true));
            message(player, "message.sporeadd.punishment.strike2");
            // Daño corriente y curable (con animación de golpe), pero atraviesa Poder12 y no puede
            // matar: solo baja al jugador hasta el suelo de corazones configurado.
            float floor = SporeAddsConfig.PUNISHMENT_SECOND_STRIKE_FLOOR_HEARTS.get() * 2.0F;
            if (player.getHealth() > floor) {
                player.invulnerableTime = 0;
                player.hurt(Damagetypes2.punishment(player), player.getHealth() - floor);
                if (player.getHealth() > floor) {
                    player.setHealth(floor); // salvaguarda si algo aún lo absorbió
                }
            }
            burstAround(level, player, 1);
        } else {
            player.addEffect(new MobEffectInstance(punishment, 200, 2, false, true, true));
            message(player, "message.sporeadd.punishment.strike3");
            burstAround(level, player, 2);
            if (SporeAddsConfig.PUNISHMENT_THIRD_STRIKE_LETHAL.get()) {
                LETHAL_DRAIN.put(player.getUUID(), level.getGameTime() + 200L);
            }
        }

        playPunishmentSound(level, player, points);
    }

    private static void forceKill(ServerPlayer player) {
        // Daño "sporeadd:punishment": está en bypasses_armor/effects/invulnerability/cooldown, así
        // que ignora armadura, Poder12, resistencias y tótems, y su message_id da el mensaje de
        // muerte "X was executed for insubordination". Se usa hurt() (no die() directo) para que el
        // combat tracker registre la entrada y el mensaje salga correcto.
        player.invulnerableTime = 0;
        player.hurt(Damagetypes2.punishment(player), 1_000_000.0F);
        if (player.isAlive()) {
            // Último recurso si algo canceló el LivingDamageEvent.Pre/LivingDeathEvent.
            player.setHealth(0.0F);
            player.die(Damagetypes2.punishment(player));
        }
        // La limpieza de LETHAL_DRAIN + FX gore + flag de no-respawn se hacen en onPunishmentDeath.
    }

    // ------------------------------------------------------------------ muerte / respawn

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPunishmentDeath(LivingDeathEvent event) {
        if (event.isCanceled()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (LETHAL_DRAIN.remove(player.getUUID()) == null) return;

        if (player.level() instanceof ServerLevel level) {
            spawnGoreDeathBurst(level, player.getX(), player.getY() + player.getBbHeight() / 2.0D, player.getZ());
        }

        if (SporeAddsConfig.PUNISHMENT_THIRD_STRIKE_NO_RESPAWN.get()) {
            PunishmentTracker.setPermadead(player, true);
        }
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!PunishmentTracker.isPermadead(player)) return;
        if (!SporeAddsConfig.PUNISHMENT_THIRD_STRIKE_NO_RESPAWN.get()) return;

        player.setGameMode(GameType.SPECTATOR);
        message(player, "message.sporeadd.punishment.no_respawn");
    }

    // ------------------------------------------------------------------ FX

    private static void message(ServerPlayer player, String key) {
        player.sendSystemMessage(Component.translatable(key).withStyle(ChatFormatting.DARK_RED));
    }

    private static void playPunishmentSound(ServerLevel level, ServerPlayer player, int points) {
        SoundEvent sound = BuiltInRegistries.SOUND_EVENT.get(
                ResourceLocation.fromNamespaceAndPath("spore", points >= 3 ? "evolve_hurt" : "hyper_damage"));
        if (sound != null) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    sound, SoundSource.HOSTILE, 1.5F, 0.5F);
        }
    }

    /** FX "llamativos" alrededor del castigado; la intensidad crece con el tier (0..2). */
    private static void burstAround(ServerLevel level, ServerPlayer player, int tier) {
        int count = 30 + tier * 40;
        for (int i = 0; i < count; i++) {
            double angle = level.random.nextDouble() * Math.PI * 2.0D;
            double dist = level.random.nextDouble() * 1.3D;
            int variant = 1 + level.random.nextInt(9);
            level.sendParticles(
                    new GoreParticleData(SporeaddParticleTypes.GORE.get(), variant),
                    player.getX() + Math.cos(angle) * dist,
                    player.getY() + level.random.nextDouble() * player.getBbHeight(),
                    player.getZ() + Math.sin(angle) * dist,
                    1, 0.0D, 0.15D, 0.0D, 0.25D);
        }
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                player.getX(), player.getY() + 1.0D, player.getZ(),
                20 + tier * 20, 0.4D, 0.6D, 0.4D, 0.02D);
        level.sendParticles(ParticleTypes.CRIMSON_SPORE,
                player.getX(), player.getY() + 1.0D, player.getZ(),
                30 + tier * 20, 0.5D, 0.8D, 0.5D, 0.0D);
    }

    /** Explosión de gore al morir, al estilo de {@code gluttonousBiteAttackHandler}. */
    public static void spawnGoreDeathBurst(ServerLevel level, double x, double y, double z) {
        int special = level.random.nextBoolean() ? 10 : 11;
        level.sendParticles(new GoreParticleData(SporeaddParticleTypes.GORE.get(), special),
                x, y, z, 1, 0.0D, 0.25D, 0.0D, 0.18D);

        for (int i = 0; i < 50; i++) {
            int variant = level.random.nextFloat() < 0.2F
                    ? (6 + level.random.nextInt(4))
                    : (1 + level.random.nextInt(5));
            double mx = (level.random.nextDouble() - 0.5D) * 0.95D;
            double my = level.random.nextDouble() * 0.95D;
            double mz = (level.random.nextDouble() - 0.5D) * 0.95D;
            level.sendParticles(new GoreParticleData(SporeaddParticleTypes.GORE.get(), variant),
                    x, y, z, 1, mx, my, mz, 0.35D);
        }

        SoundEvent evolveHurt = BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath("spore", "evolve_hurt"));
        if (evolveHurt != null) {
            level.playSound(null, x, y, z, evolveHurt, SoundSource.HOSTILE, 1.0F, 0.5F);
        }
        SoundEvent hyperDamage = BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath("spore", "hyper_damage"));
        if (hyperDamage != null) {
            level.playSound(null, x, y, z, hyperDamage, SoundSource.HOSTILE, 1.0F, 0.5F);
        }
    }
}
