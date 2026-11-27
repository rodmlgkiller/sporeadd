package com.sporeadds.sporeaddsmod.hive;

import com.sporeadds.sporeaddsmod.config.SporeAddsConfig;
import com.sporeadds.sporeaddsmod.effects.effects;
import com.sporeadds.sporeaddsmod.network.HiveDialogueAdvancePacket;
import com.sporeadds.sporeaddsmod.network.NetworkHandle;
import com.sporeadds.sporeaddsmod.network.StartHiveCinematicPacket;
import com.sporeadds.sporeaddsmod.network.SyncHiveDownedPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Estado y transiciones del modo "downed but not out" de la colmena (Call of the Hive).
 *
 * Flujo:
 *  1. {@link HiveDownedEvents} intercepta el daño mortal de un jugador con el efecto
 *     {@code call_of_the_hive} y llama a {@link #enterDowned}.
 *  2. El cliente reproduce la cinemática ({@code HiveCinematicScreen}) y al final envía
 *     {@link com.sporeadds.sporeaddsmod.network.SubmitHiveChoicePacket} -> {@link #resolveChoice}.
 *  3. Rendición: {@link HiveSurrenderTask} mueve al jugador junto a un proto, lo pasa a
 *     kommandant (sin subclase) y lo mete en un cocoon de Poder1 (sin subir nivel).
 *     Rechazo: al recibir {@link com.sporeadds.sporeaddsmod.network.HiveCinematicDonePacket}
 *     ({@link #onCinematicDone}) se aplica {@code spore:mycelium_ef} y se remata con el daño original.
 */
public final class HiveDownedManager {

    /** Refresco del marcador call_of_the_hive mientras se está downed (evita que expire durante la cinemática). */
    public static final int MARK_REFRESH_TICKS = 20 * 30;

    /**
     * Flag en el NBT persistente del jugador: "está a mitad de la secuencia de la colmena".
     * Guarda el desenlace pendiente ({@link #PENDING_PERISH} / {@link #PENDING_SURRENDER}).
     * Se pone en {@link #enterDowned} y se limpia en cualquier desenlace normal. Si el jugador
     * cierra el juego con el flag puesto, en el siguiente login se resuelve: perish -> muere como
     * "give up normal" ({@link #loginPerish}); surrender -> se reproduce la secuencia de verwa
     * ({@link #loginSurrender}).
     */
    public static final String HIVE_PENDING_TAG = "SporeAdds_HivePendingDeath";
    public static final String PENDING_PERISH = "perish";
    public static final String PENDING_SURRENDER = "surrender";

    /** Red de seguridad: si el cliente nunca responde con una elección, se fuerza "perish". */
    private static final long CINEMATIC_TIMEOUT_TICKS = 20L * 300L;
    /** Red de seguridad: si se eligió "perish" pero el cliente nunca confirma el fin, se remata igualmente. */
    private static final long PERISH_FINALIZE_TIMEOUT_TICKS = 20L * 25L;

    private static final int ROOT_SLOWNESS_AMPLIFIER = 250;
    private static final int ROOT_REAPPLY_TICKS = 40;
    private static final float FINISHING_BLOW_DAMAGE = 100000.0F;

    private static final ResourceLocation MYCELIUM_EF_ID = new ResourceLocation("spore", "mycelium_ef");

    public enum Choice {
        NONE,
        SURRENDER,
        PERISH
    }

    public static final class DownedData {
        DamageSource originalSource;
        Choice choice = Choice.NONE;
        long enteredTick;
        long choiceTick;
        boolean perishFinished;
        Vec3 downPos;
        /** Inducción voluntaria (se eligió kommandant): subclase a conservar al terminar. */
        String voluntarySubclass;
    }

    public static Vec3 downPosOf(UUID playerId) {
        DownedData data = DOWNED.get(playerId);
        return data == null ? null : data.downPos;
    }

    public static String voluntarySubclassOf(UUID playerId) {
        DownedData data = DOWNED.get(playerId);
        return data == null ? null : data.voluntarySubclass;
    }

    private static final Map<UUID, DownedData> DOWNED = new HashMap<>();

    /** Radio en el que se le quita el aggro a los mobs mientras/tras el modo downed. */
    private static final double AGGRO_DROP_RADIUS = 64.0D;
    /** Ventana de gracia tras salir del modo downed en la que se sigue soltando el aggro. */
    private static final long POST_DOWNED_AGGRO_GRACE_TICKS = 20L * 40L;   // 40 s (cubre cocoon + emerger)

    /** playerId -> gameTime hasta el que el jugador no puede ser targeteado tras salir de downed. */
    private static final Map<UUID, Long> AGGRO_GRACE_UNTIL = new HashMap<>();

    private HiveDownedManager() {
    }

    public static boolean isDowned(UUID playerId) {
        return DOWNED.containsKey(playerId);
    }

    /**
     * true si este jugador, al recibir daño mortal AHORA, entraría en el modo "downed"
     * (o ya lo está). Es la misma precondición que usa {@link HiveDownedEvents#onLivingDeath}.
     * También la consulta el mixin de Spore para no generar un "InfectedPlayer" de un jugador
     * que en realidad no muere.
     */
    public static boolean wouldEnterDowned(ServerPlayer player) {
        if (player == null || player.level().isClientSide()) return false;
        if (DOWNED.containsKey(player.getUUID())) return true;
        if (player.getEffect(effects.CALL_OF_THE_HIVE.get()) == null) return false;
        if (SporeAddsConfig.CALL_OF_THE_HIVE_PERSISTS_WITHOUT_PROTO.get()) return true;
        return ProtoProximity.isProtoWithin(
                player.level(), player.position(), CallOfTheHiveHandler.PROXIMITY_RADIUS);
    }

    /** true si a este jugador no se le debe poder targetear (downed, o ventana de gracia posterior). */
    public static boolean isAggroProtected(UUID playerId, long gameTime) {
        if (DOWNED.containsKey(playerId)) return true;
        Long until = AGGRO_GRACE_UNTIL.get(playerId);
        return until != null && gameTime < until;
    }

    /** Abre (o renueva) la ventana de gracia anti-aggro y suelta el aggro actual de inmediato. */
    private static void beginAggroGrace(ServerPlayer player) {
        AGGRO_GRACE_UNTIL.put(player.getUUID(),
                player.level().getGameTime() + POST_DOWNED_AGGRO_GRACE_TICKS);
        dropNearbyAggro(player);
    }

    /** Quita al jugador como objetivo de todos los mobs cercanos y limpia su rencor / último daño. */
    public static void dropNearbyAggro(ServerPlayer player) {
        if (player.level().isClientSide) return;
        AABB box = player.getBoundingBox().inflate(AGGRO_DROP_RADIUS);
        for (Mob mob : player.level().getEntitiesOfClass(Mob.class, box)) {
            if (mob.getTarget() == player) {
                mob.setTarget(null);
            }
            if (mob.getLastHurtByMob() == player) {
                mob.setLastHurtByMob(null);
            }
            // Muchos mobs (vanilla y del mod Spore) usan un Brain vacío sin ATTACK_TARGET
            // registrado; getMemory() lanzaría IllegalStateException. checkMemory() no lanza.
            Brain<?> brain = mob.getBrain();
            if (brain.checkMemory(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT)
                    && brain.getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null) == player) {
                brain.eraseMemory(MemoryModuleType.ATTACK_TARGET);
            }
            if (mob instanceof NeutralMob neutral
                    && player.getUUID().equals(neutral.getPersistentAngerTarget())) {
                neutral.stopBeingAngry();
            }
        }
    }

    public static Choice choiceOf(UUID playerId) {
        DownedData data = DOWNED.get(playerId);
        return data == null ? Choice.NONE : data.choice;
    }

    // ---------------------------------------------------------------- enter

    public static void enterDowned(ServerPlayer player, DamageSource source) {
        UUID id = player.getUUID();
        if (DOWNED.containsKey(id)) return;

        boolean force = SporeAddsConfig.HIVE_FORCE_SURRENDER.get();

        DownedData data = new DownedData();
        data.originalSource = source;
        data.enteredTick = player.level().getGameTime();
        data.downPos = player.position();
        DOWNED.put(id, data);

        player.getPersistentData().putString(HIVE_PENDING_TAG, force ? PENDING_SURRENDER : PENDING_PERISH);
        player.setHealth(1.0F);
        if (player.getEffect(effects.CALL_OF_THE_HIVE.get()) == null) {
            player.addEffect(new MobEffectInstance(effects.CALL_OF_THE_HIVE.get(), MARK_REFRESH_TICKS, 0, false, false, true));
        }

        applyRoot(player);
        player.setSprinting(false);
        player.setDeltaMovement(Vec3.ZERO);
        player.hurtMarked = true;
        player.fallDistance = 0.0F;

        // En el instante de ser downed, dejar de ser objetivo de todo lo que le perseguía.
        dropNearbyAggro(player);

        NetworkHandle.INSTANCE.send(
                PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                new SyncHiveDownedPacket(player.getId(), true)
        );
        NetworkHandle.INSTANCE.send(
                PacketDistributor.PLAYER.with(() -> player),
                new StartHiveCinematicPacket(force)
        );
    }

    // ---------------------------------------------------------------- choice

    public static void resolveChoice(ServerPlayer player, boolean surrender) {
        DownedData data = DOWNED.get(player.getUUID());
        if (data == null || data.choice != Choice.NONE) return;

        data.choice = surrender ? Choice.SURRENDER : Choice.PERISH;
        data.choiceTick = player.level().getGameTime();

        if (surrender) {
            // A partir de aquí, si cierra el juego, en el próximo login se reproduce la secuencia de verwa.
            player.getPersistentData().putString(HIVE_PENDING_TAG, PENDING_SURRENDER);
            // "Very well..." se muestra ya y se mantiene hasta que el transporte termine ("Now arise...").
            NetworkHandle.INSTANCE.send(
                    PacketDistributor.PLAYER.with(() -> player),
                    new HiveDialogueAdvancePacket(HiveDialogueAdvancePacket.PHASE_VERY_WELL)
            );
            HiveSurrenderTask.start(player);
        } else {
            NetworkHandle.INSTANCE.send(
                    PacketDistributor.PLAYER.with(() -> player),
                    new HiveDialogueAdvancePacket(HiveDialogueAdvancePacket.PHASE_PERISH)
            );
        }
    }

    public static void onCinematicDone(ServerPlayer player) {
        DownedData data = DOWNED.get(player.getUUID());
        if (data == null) return;

        if (data.choice == Choice.PERISH) {
            finalizePerish(player, data);
        } else if (data.choice == Choice.SURRENDER) {
            // El mundo ya lo gestionó HiveSurrenderTask (verwa + clase + cocoon). Solo limpiamos.
            clearDowned(player, false);
        }
    }

    // ---------------------------------------------------------------- outcomes

    private static void finalizePerish(ServerPlayer player, DownedData data) {
        if (data.perishFinished) return;
        data.perishFinished = true;

        player.getPersistentData().remove(HIVE_PENDING_TAG);
        clearRoot(player);
        clearForcedPose(player);
        player.removeEffect(effects.CALL_OF_THE_HIVE.get());

        MobEffect mycelium = ForgeRegistries.MOB_EFFECTS.getValue(MYCELIUM_EF_ID);
        if (mycelium != null) {
            player.addEffect(new MobEffectInstance(mycelium, 600, 0, false, true));
        }

        DamageSource source = data.originalSource != null
                ? data.originalSource
                : player.damageSources().generic();

        NetworkHandle.INSTANCE.send(
                PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                new SyncHiveDownedPacket(player.getId(), false)
        );

        // Quitar del registro ANTES del golpe para que los guardas de HiveDownedEvents dejen pasar la muerte.
        DOWNED.remove(player.getUUID());

        player.setHealth(1.0F);
        player.hurt(source, FINISHING_BLOW_DAMAGE);
        if (player.isAlive()) {
            player.kill();
        }
    }

    /**
     * Llamado por {@link HiveSurrenderTask} cuando ya ha metido al jugador en el cocoon:
     * deja de renderizarlo tumbado y quita el rooteo, pero mantiene la entrada del mapa
     * hasta que el cliente confirme el fin de la cinemática ("Now arise...").
     */
    public static void onSurrenderCocoonStarted(ServerPlayer player) {
        player.getPersistentData().remove(HIVE_PENDING_TAG);
        clearRoot(player);
        clearForcedPose(player);
        beginAggroGrace(player);
        NetworkHandle.INSTANCE.send(
                PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                new SyncHiveDownedPacket(player.getId(), false)
        );
        NetworkHandle.INSTANCE.send(
                PacketDistributor.PLAYER.with(() -> player),
                new HiveDialogueAdvancePacket(HiveDialogueAdvancePacket.PHASE_ARISE)
        );
    }

    // ---------------------------------------------------------------- housekeeping (called from HiveDownedEvents)

    public static void serverTickAll(MinecraftServer server) {
        // Ventana de gracia anti-aggro tras salir del modo downed (p.ej. ya kommandant).
        if (!AGGRO_GRACE_UNTIL.isEmpty()) {
            long now = server.overworld().getGameTime();
            AGGRO_GRACE_UNTIL.entrySet().removeIf(e -> now >= e.getValue());
            if (now % 3L == 0L) {
                for (UUID id : new ArrayList<>(AGGRO_GRACE_UNTIL.keySet())) {
                    if (DOWNED.containsKey(id)) continue;
                    ServerPlayer p = server.getPlayerList().getPlayer(id);
                    if (p != null) {
                        dropNearbyAggro(p);
                    }
                }
            }
        }

        if (DOWNED.isEmpty()) return;
        for (UUID id : new ArrayList<>(DOWNED.keySet())) {
            ServerPlayer player = server.getPlayerList().getPlayer(id);
            if (player == null || !player.isAlive()) {
                DOWNED.remove(id);
                continue;
            }
            serverTick(player);
        }
    }

    private static void serverTick(ServerPlayer player) {
        DownedData data = DOWNED.get(player.getUUID());
        if (data == null) return;

        // Mientras dure el modo downed, mantener a raya a cualquier mob que intente targetearlo.
        if (player.level().getGameTime() % 3L == 0L) {
            dropNearbyAggro(player);
        }

        // Si ya es kommandant (rendición completada, o intervención externa como /class), cerrar limpio.
        boolean isKommandant = player.getCapability(
                        com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider.SPORE_IDENTIFIER)
                .map(id -> "kommandant".equalsIgnoreCase(id.getIdentifier()))
                .orElse(false);
        // Si se volvió kommandant por vía externa (/class fuera de ceremonia) estando genuinamente
        // downed (sin decisión, o eligió perecer), cerrar limpio. La rama de rendición/inducción
        // (choice == SURRENDER) la cierra onCinematicDone o el timeout de más abajo.
        if (isKommandant && data.choice != Choice.SURRENDER && !HiveSurrenderTask.isActive(player.getUUID())) {
            player.getPersistentData().remove(HIVE_PENDING_TAG);
            NetworkHandle.INSTANCE.send(
                    PacketDistributor.PLAYER.with(() -> player),
                    new HiveDialogueAdvancePacket(HiveDialogueAdvancePacket.PHASE_ABORT)
            );
            clearDowned(player, true);
            return;
        }

        // Red de seguridad de la rendición: si el cliente nunca confirma el fin y el transporte
        // ya no está activo, limpiar el estado un buen rato después de la decisión.
        if (data.choice == Choice.SURRENDER
                && !HiveSurrenderTask.isActive(player.getUUID())
                && player.level().getGameTime() - data.choiceTick > 20L * 60L) {
            clearDowned(player, true);
            return;
        }

        long now = player.level().getGameTime();

        // Mantener rooteado y con el marcador vivo (salvo si ya se rindió: el cocoon lo inmoviliza).
        if (!data.perishFinished && data.choice != Choice.SURRENDER) {
            if (now % ROOT_REAPPLY_TICKS == 0) {
                applyRoot(player);
            }
            if (player.getEffect(effects.CALL_OF_THE_HIVE.get()) == null) {
                player.addEffect(new MobEffectInstance(effects.CALL_OF_THE_HIVE.get(), MARK_REFRESH_TICKS, 0, false, false, true));
            }
            player.setDeltaMovement(player.getDeltaMovement().multiply(0.0D, 1.0D, 0.0D));
            player.setSprinting(false);
            player.fallDistance = 0.0F;

            // Re-broadcast periódico para que los clientes que empiecen a trackear al jugador
            // a mitad de la secuencia también lo dibujen tumbado.
            if (now % 40 == 0) {
                NetworkHandle.INSTANCE.send(
                        PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                        new SyncHiveDownedPacket(player.getId(), true)
                );
            }
        }

        if (data.choice == Choice.NONE && now - data.enteredTick > CINEMATIC_TIMEOUT_TICKS) {
            if (SporeAddsConfig.HIVE_FORCE_SURRENDER.get()) {
                // Se agotó el tiempo pero el modo fuerza rendición: se resuelve como rendición.
                resolveChoice(player, true);
            } else {
                data.choice = Choice.PERISH;
                data.choiceTick = now;
                NetworkHandle.INSTANCE.send(
                        PacketDistributor.PLAYER.with(() -> player),
                        new HiveDialogueAdvancePacket(HiveDialogueAdvancePacket.PHASE_ABORT)
                );
                finalizePerish(player, data);
            }
            return;
        }

        if (data.choice == Choice.PERISH
                && !data.perishFinished
                && now - data.choiceTick > PERISH_FINALIZE_TIMEOUT_TICKS) {
            finalizePerish(player, data);
        }
    }

    public static void clearDowned(ServerPlayer player, boolean notifyClients) {
        boolean had = DOWNED.remove(player.getUUID()) != null;
        player.getPersistentData().remove(HIVE_PENDING_TAG);
        clearRoot(player);
        clearForcedPose(player);
        beginAggroGrace(player);
        if (had || notifyClients) {
            NetworkHandle.INSTANCE.send(
                    PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                    new SyncHiveDownedPacket(player.getId(), false)
            );
        }
    }

    public static void forget(UUID playerId) {
        DOWNED.remove(playerId);
    }

    /**
     * El jugador cerró el juego durante la secuencia. En el siguiente login muere como
     * "give up normal": recibe {@code spore:mycelium_ef} y un golpe mortal genérico que
     * dispara la muerte normal (y por tanto el drop de loot).
     */
    public static void loginPerish(ServerPlayer player) {
        player.getPersistentData().remove(HIVE_PENDING_TAG);
        DOWNED.remove(player.getUUID());

        NetworkHandle.INSTANCE.send(
                PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                new SyncHiveDownedPacket(player.getId(), false)
        );

        // Diferir un tick: asegurarse de que el jugador está totalmente añadido al mundo.
        net.minecraft.server.MinecraftServer server = player.server;
        if (server == null) return;
        server.execute(() -> {
            if (!player.isAlive()) return;
            player.removeEffect(effects.CALL_OF_THE_HIVE.get());
            MobEffect mycelium = ForgeRegistries.MOB_EFFECTS.getValue(MYCELIUM_EF_ID);
            if (mycelium != null) {
                player.addEffect(new MobEffectInstance(mycelium, 600, 0, false, true));
            }
            player.setHealth(1.0F);
            player.hurt(player.damageSources().generic(), FINISHING_BLOW_DAMAGE);
            if (player.isAlive()) {
                player.kill();
            }
        });
    }

    /**
     * El jugador cerró el juego durante la secuencia y el desenlace pendiente es rendición
     * (modo fuerza-rendición, o ya había elegido "give up rojo"). En el próximo login se
     * reproduce toda la secuencia: cinemática + transporte de verwa + infección.
     */
    // ---------------------------------------------------------------- inducción voluntaria (se elige kommandant)

    /**
     * Llamado tras aplicar una clase. Si la clase elegida es kommandant (y no lo era ya), lanza
     * la ceremonia: pantalla oscura + "Very well..." + transporte de verwa + cocoon + "Now arise...".
     * NO se dispara desde el camino interno de la colmena (ese usa setIdentifierAndSync directamente).
     */
    public static void maybeBeginVoluntaryInduction(ServerPlayer player, String newIdentifier, String previousIdentifier) {
        if (player == null || player.server == null) return;
        if (!"kommandant".equalsIgnoreCase(newIdentifier)) return;
        if ("kommandant".equalsIgnoreCase(previousIdentifier)) return;

        UUID id = player.getUUID();
        if (DOWNED.containsKey(id) || HiveSurrenderTask.isActive(id)) return;
        if (player.isPassenger()) return;
        if (com.sporeadds.sporeaddsmod.Powers.Poder1.timers.containsKey(id)) return;

        player.server.execute(() -> beginVoluntaryInduction(player));
    }

    private static void beginVoluntaryInduction(ServerPlayer player) {
        UUID id = player.getUUID();
        if (DOWNED.containsKey(id) || HiveSurrenderTask.isActive(id)) return;
        if (!player.isAlive() || player.isPassenger()) return;
        if (com.sporeadds.sporeaddsmod.Powers.Poder1.timers.containsKey(id)) return;

        long now = player.level().getGameTime();

        DownedData data = new DownedData();
        data.choice = Choice.SURRENDER;          // salta directo a la rama de rendición
        data.enteredTick = now;
        data.choiceTick = now;
        data.downPos = player.position();
        data.originalSource = player.damageSources().generic();
        data.voluntarySubclass = player.getCapability(
                        com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider.SPORE_IDENTIFIER)
                .map(d -> d.getSubclass())
                .orElse("none");
        DOWNED.put(id, data);

        player.getPersistentData().putString(HIVE_PENDING_TAG, PENDING_SURRENDER);
        applyRoot(player);
        player.setSprinting(false);
        player.setDeltaMovement(Vec3.ZERO);

        NetworkHandle.INSTANCE.send(
                PacketDistributor.PLAYER.with(() -> player),
                new StartHiveCinematicPacket(true)
        );
        NetworkHandle.INSTANCE.send(
                PacketDistributor.PLAYER.with(() -> player),
                new HiveDialogueAdvancePacket(HiveDialogueAdvancePacket.PHASE_VERY_WELL)
        );

        HiveSurrenderTask.start(player);
    }

    public static void loginSurrender(ServerPlayer player) {
        player.getPersistentData().remove(HIVE_PENDING_TAG);
        DOWNED.remove(player.getUUID());

        net.minecraft.server.MinecraftServer server = player.server;
        if (server == null) return;
        server.execute(() -> {
            if (!player.isAlive()) return;
            enterDowned(player, player.damageSources().generic());
            resolveChoice(player, true);
        });
    }

    // ---------------------------------------------------------------- helpers

    private static void applyRoot(ServerPlayer player) {
        // El Screen abierto en el cliente ya bloquea el input; esto es refuerzo por si lo empujan.
        player.addEffect(new MobEffectInstance(
                MobEffects.MOVEMENT_SLOWDOWN, ROOT_REAPPLY_TICKS + 20, ROOT_SLOWNESS_AMPLIFIER, false, false, false));
    }

    private static void clearRoot(ServerPlayer player) {
        player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
    }

    private static void clearForcedPose(ServerPlayer player) {
        player.setSprinting(false);
        player.setSwimming(false);
    }
}
