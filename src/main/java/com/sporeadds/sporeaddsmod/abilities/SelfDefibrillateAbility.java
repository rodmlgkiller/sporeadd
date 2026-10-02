package com.sporeadds.sporeaddsmod.abilities;

import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider;
import com.sporeadds.sporeaddsmod.effects.DelayedDefibrillationEffectHandler;
import com.sporeadds.sporeaddsmod.effects.SporeTeamCombatTracker;
import com.sporeadds.sporeaddsmod.network.NetworkHandle;
import com.sporeadds.sporeaddsmod.network.OpenSelfDefibrillateScreenPacket;
import com.sporeadds.sporeaddsmod.network.SyncSelfDefibrillateCooldownPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import com.sporeadds.sporeaddsmod.network.NetworkDirection;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class SelfDefibrillateAbility {

    private static final float HEALTH_THRESHOLD_PERCENT = 0.30F;

    private static final int RESISTANCE_AMPLIFIER = 3;
    private static final int RESISTANCE_DURATION_TICKS = 20 * 3;

    private static final int COOLDOWN_TICKS = 20 * 1;

    private static final Set<UUID> ACTIVE_ATTEMPTS = new HashSet<>();
    private static final Map<UUID, Long> COOLDOWN_EXPIRY_TICK = new HashMap<>();

    private SelfDefibrillateAbility() {
    }

    public static void tryStart(ServerPlayer player) {
        if (!isMedic(player)) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("[DEBUG] No es medic"));
            return;
        }
        if (!player.isAlive()) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("[DEBUG] No está vivo"));
            return;
        }
        if (player.getHealth() > player.getMaxHealth() * HEALTH_THRESHOLD_PERCENT) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("[DEBUG] Vida por encima del umbral"));
            return;
        }
        if (ACTIVE_ATTEMPTS.contains(player.getUUID())) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("[DEBUG] BLOQUEADO por ACTIVE_ATTEMPTS"));
            return;
        }
        if (isOnCooldown(player)) {
            long remaining = COOLDOWN_EXPIRY_TICK.getOrDefault(player.getUUID(), 0L) - player.level().getGameTime();
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("[DEBUG] BLOQUEADO por cooldown, ticks restantes: " + remaining));
            return;
        }

        ACTIVE_ATTEMPTS.add(player.getUUID());
        SporeTeamCombatTracker.clear(player.getUUID());

        startCooldown(player);

        player.addEffect(new MobEffectInstance(
                MobEffects.DAMAGE_RESISTANCE,
                RESISTANCE_DURATION_TICKS,
                RESISTANCE_AMPLIFIER,
                false,
                true,
                true
        ));

        NetworkHandle.INSTANCE.sendTo(
                new OpenSelfDefibrillateScreenPacket(),
                player.connection.connection,
                NetworkDirection.PLAY_TO_CLIENT
        );
    }

    /**
     * Llamado cuando el minijuego termina normalmente (acierto,
     * fallo por clic incorrecto, o tiempo agotado). Si el jugador
     * muere justo por el rayo de fallo, la limpieza final de
     * ACTIVE_ATTEMPTS la hace el listener de muerte unificado, no
     * este método — así evitamos la carrera entre "borrar el intento"
     * y "detectar que murió durante el intento".
     */
    public static void resolve(ServerPlayer player, boolean success) {
        if (!ACTIVE_ATTEMPTS.contains(player.getUUID())) return;
        if (!(player.level() instanceof ServerLevel serverLevel)) return;
        if (!player.isAlive()) return;

        if (success) {
            ACTIVE_ATTEMPTS.remove(player.getUUID());
            SporeTeamCombatTracker.clear(player.getUUID());
            DelayedDefibrillationEffectHandler.forceRevive(serverLevel, player);
        } else {
            DelayedDefibrillationEffectHandler.forceKillByLightning(serverLevel, player);

            if (player.isAlive()) {
                ACTIVE_ATTEMPTS.remove(player.getUUID());
                SporeTeamCombatTracker.clear(player.getUUID());
            }
        }
    }

    public static boolean isAttemptActive(UUID playerId) {
        return ACTIVE_ATTEMPTS.contains(playerId);
    }

    /**
     * Limpieza forzada del intento activo. Llamado únicamente desde
     * el listener unificado de muerte, tanto si murió por el fallo
     * propio como si lo mató un mob externo durante el minijuego.
     */
    public static void forceEndAttempt(ServerPlayer player) {
        ACTIVE_ATTEMPTS.remove(player.getUUID());
        SporeTeamCombatTracker.clear(player.getUUID());

        NetworkHandle.INSTANCE.sendTo(
                new com.sporeadds.sporeaddsmod.network.CloseSelfDefibrillateScreenPacket(),
                player.connection.connection,
                NetworkDirection.PLAY_TO_CLIENT
        );
    }

    private static boolean isOnCooldown(ServerPlayer player) {
        Long expiry = COOLDOWN_EXPIRY_TICK.get(player.getUUID());
        if (expiry == null) return false;

        long now = player.level().getGameTime();
        return now < expiry;
    }

    private static void startCooldown(ServerPlayer player) {
        long now = player.level().getGameTime();
        COOLDOWN_EXPIRY_TICK.put(player.getUUID(), now + COOLDOWN_TICKS);

        NetworkHandle.INSTANCE.sendTo(
                new SyncSelfDefibrillateCooldownPacket(COOLDOWN_TICKS),
                player.connection.connection,
                NetworkDirection.PLAY_TO_CLIENT
        );
    }

    private static boolean isMedic(ServerPlayer player) {
        return player.getCapability(SporeIdentifierProvider.SPORE_IDENTIFIER)
                .map(data -> "medic".equalsIgnoreCase(data.getIdentifier()))
                .orElse(false);
    }
}