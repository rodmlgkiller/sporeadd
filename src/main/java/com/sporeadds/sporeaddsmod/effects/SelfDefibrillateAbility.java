package com.sporeadds.sporeaddsmod.effects;

import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider;
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
    private static final int RESISTANCE_DURATION_TICKS = 20 * 4;
    private static final int COOLDOWN_TICKS = 20 * 50;

    private static final Set<UUID> ACTIVE_ATTEMPTS = new HashSet<>();
    private static final Map<UUID, Long> COOLDOWN_EXPIRY_TICK = new HashMap<>();

    private SelfDefibrillateAbility() {
    }

    public static void tryStart(ServerPlayer player) {
        if (!isMedic(player)) return;
        if (!player.isAlive()) return;
        if (player.getHealth() > player.getMaxHealth() * HEALTH_THRESHOLD_PERCENT) return;
        if (ACTIVE_ATTEMPTS.contains(player.getUUID())) return;
        if (isOnCooldown(player)) return;

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

        return player.level().getGameTime() < expiry;
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
        return SporeIdentifierProvider.SPORE_IDENTIFIER.get(player)
                .map(data -> "medic".equalsIgnoreCase(data.getIdentifier()))
                .orElse(false);
    }
}