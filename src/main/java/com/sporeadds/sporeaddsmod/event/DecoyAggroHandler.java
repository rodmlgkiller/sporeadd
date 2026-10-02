package com.sporeadds.sporeaddsmod.event;

import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import net.neoforged.neoforge.event.tick.ServerTickEvent;

import net.neoforged.fml.common.EventBusSubscriber;

import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider;
import com.sporeadds.sporeaddsmod.abilities.DecoyAbility;
import com.sporeadds.sporeaddsmod.entity.DecoyEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = "sporeadd")
public class DecoyAggroHandler {

    private static final double MAX_RANGE = 20.0D;
    private static final double MAX_RANGE_SQR = MAX_RANGE * MAX_RANGE;
    private static final double ATTACK_RANGE_SQR = 3.0D * 3.0D;
    private static final double MOVE_SPEED = 1.0D;

    private static final Map<UUID, UUID> HOOKED_MOBS = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void onLivingChangeTarget(LivingChangeTargetEvent event) {
        LivingEntity mobEntity = event.getEntity();

        if (!(mobEntity instanceof Mob mob)) {
            return;
        }

        LivingEntity newTarget = event.getNewTarget();

        if (!(newTarget instanceof ServerPlayer player)) {
            return;
        }

        DecoyEntity decoy = findValidDecoyFor(mob, player);

        if (decoy != null) {
            hookMobToDecoy(mob, decoy);
            event.setNewTarget(decoy);
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingDamageEvent.Pre event) {
        if (!(event.getEntity() instanceof DecoyEntity decoy)) {
            return;
        }

        if (!decoy.isAlive()) {
            return;
        }

        AABB searchBox = decoy.getBoundingBox().inflate(MAX_RANGE);

        List<Mob> nearbyMobs = decoy.level().getEntitiesOfClass(
                Mob.class,
                searchBox,
                mob -> true
        );

        for (Mob mob : nearbyMobs) {
            hookMobToDecoy(mob, decoy);
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (HOOKED_MOBS.isEmpty()) return;

        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;

        for (Map.Entry<UUID, UUID> entry : HOOKED_MOBS.entrySet()) {
            UUID mobUUID = entry.getKey();
            UUID decoyUUID = entry.getValue();

            Mob mob = findEntityByUUID(server, mobUUID, Mob.class);
            DecoyEntity decoy = findEntityByUUID(server, decoyUUID, DecoyEntity.class);

            if (mob == null || !mob.isAlive() || decoy == null || !decoy.isAlive()) {
                HOOKED_MOBS.remove(mobUUID);
                continue;
            }

            double distanceSqr = mob.distanceToSqr(decoy);

            if (distanceSqr > MAX_RANGE_SQR) {
                HOOKED_MOBS.remove(mobUUID);
                continue;
            }

            mob.setTarget(decoy);
            mob.setLastHurtByMob(decoy);

            if (distanceSqr > ATTACK_RANGE_SQR) {
                mob.getNavigation().moveTo(decoy, MOVE_SPEED);
                mob.getLookControl().setLookAt(decoy, 30.0F, 30.0F);
            } else {
                mob.getLookControl().setLookAt(decoy, 30.0F, 30.0F);
                mob.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
                mob.doHurtTarget(decoy);
            }
        }
    }

    private static <T extends net.minecraft.world.entity.Entity> T findEntityByUUID(MinecraftServer server, UUID uuid, Class<T> clazz) {
        for (var level : server.getAllLevels()) {
            var entity = level.getEntity(uuid);
            if (clazz.isInstance(entity)) {
                return clazz.cast(entity);
            }
        }
        return null;
    }

    private static void hookMobToDecoy(Mob mob, DecoyEntity decoy) {
        HOOKED_MOBS.put(mob.getUUID(), decoy.getUUID());
    }

    private static DecoyEntity findValidDecoyFor(Mob mob, ServerPlayer player) {
        if (!isGhostAndInvisible(player)) {
            return null;
        }

        DecoyEntity closestDecoy = null;
        double closestDistanceSqr = MAX_RANGE_SQR;

        for (DecoyEntity decoy : DecoyAbility.getAllActiveDecoys()) {
            if (!decoy.isAlive()) continue;
            if (mob.level() != decoy.level()) continue;

            double distanceSqr = mob.distanceToSqr(decoy);
            if (distanceSqr > closestDistanceSqr) continue;

            closestDecoy = decoy;
            closestDistanceSqr = distanceSqr;
        }

        return closestDecoy;
    }

    private static boolean isGhostAndInvisible(ServerPlayer player) {
        boolean isGhost = SporeIdentifierProvider.SPORE_IDENTIFIER.get(player)
                .map(data -> "ghost".equalsIgnoreCase(data.getIdentifier()))
                .orElse(false);

        return isGhost && player.hasEffect(MobEffects.INVISIBILITY);
    }
}