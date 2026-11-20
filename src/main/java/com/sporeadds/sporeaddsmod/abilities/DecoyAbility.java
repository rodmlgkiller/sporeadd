package com.sporeadds.sporeaddsmod.abilities;

import com.sporeadds.sporeaddsmod.config.ConfigEffects;
import com.sporeadds.sporeaddsmod.config.GhostConfig;
import com.sporeadds.sporeaddsmod.entity.DecoyEntity;
import com.sporeadds.sporeaddsmod.entity.ModEntities;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = "sporeadd")
public class DecoyAbility {

    /** Old default only (50 s). The live value is configurable: see {@code GhostConfig.decoyCooldownTicks()}. */
    @Deprecated
    public static final int COOLDOWN_TICKS = 20 * 50;

    private static final Map<UUID, Long> COOLDOWN_UNTIL_TICK = new HashMap<>();
    private static final Map<UUID, DecoyEntity> ACTIVE_DECOYS = new HashMap<>();
    private static final Deque<DecoyEntity> DECOY_SPAWN_ORDER = new ArrayDeque<>();

    private DecoyAbility() {
    }

    public static long getCooldownRemainingTicks(ServerPlayer player) {
        long until = COOLDOWN_UNTIL_TICK.getOrDefault(player.getUUID(), 0L);
        long remaining = until - player.level().getGameTime();
        return Math.max(0L, remaining);
    }

    public static java.util.Collection<DecoyEntity> getAllActiveDecoys() {
        return ACTIVE_DECOYS.values();
    }

    private static void enforceDecoyCap() {
        int maxActiveDecoys = GhostConfig.decoyMaxActive();
        while (DECOY_SPAWN_ORDER.size() >= maxActiveDecoys) {
            DecoyEntity oldest = DECOY_SPAWN_ORDER.pollFirst();
            if (oldest == null) {
                break;
            }
            if (oldest.isAlive()) {
                oldest.discard();
            }
        }
    }

    public static void onDecoyRemoved(DecoyEntity decoy) {
        DECOY_SPAWN_ORDER.remove(decoy);

        UUID ownerUUID = decoy.getOwnerUUID();
        if (ownerUUID != null && ACTIVE_DECOYS.get(ownerUUID) == decoy) {
            ACTIVE_DECOYS.remove(ownerUUID);
        }
    }

    public static boolean tryActivate(ServerPlayer player) {
        UUID uuid = player.getUUID();

        if (getCooldownRemainingTicks(player) > 0) {
            return false;
        }

        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return false;
        }

        enforceDecoyCap();

        DecoyEntity decoy = ModEntities.DECOY.get().create(serverLevel);
        if (decoy == null) {
            return false;
        }

        decoy.setPos(player.getX(), player.getY(), player.getZ());
        decoy.setYRot(player.getYRot());
        decoy.setOwner(uuid);

        serverLevel.addFreshEntity(decoy);

        ACTIVE_DECOYS.put(uuid, decoy);
        DECOY_SPAWN_ORDER.addLast(decoy);

        int cooldownTicks = GhostConfig.decoyCooldownTicks();
        long cooldownUntil = player.level().getGameTime() + cooldownTicks;
        COOLDOWN_UNTIL_TICK.put(uuid, cooldownUntil);

        com.sporeadds.sporeaddsmod.network.NetworkHandle.INSTANCE.sendTo(
                new com.sporeadds.sporeaddsmod.network.SyncDecoyCooldownPacket(cooldownTicks),
                player.connection.connection,
                net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT
        );

        return true;
    }

    public static void onDecoyHurt(DecoyEntity decoy, DamageSource source) {
        Entity attackerEntity = source.getEntity();
        Entity directEntity = source.getDirectEntity();

        boolean isMelee = directEntity == attackerEntity;

        if (isMelee && attackerEntity instanceof ServerPlayer attacker) {
            ConfigEffects.apply(attacker, ConfigEffects.parseAll(GhostConfig.DECOY_ATTACKER_EFFECTS.get()));
        }

        decoy.discard();
    }
}