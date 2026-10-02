package com.sporeadds.sporeaddsmod.hive;

import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import net.neoforged.neoforge.event.tick.ServerTickEvent;

import net.neoforged.fml.common.EventBusSubscriber;

import com.sporeadds.sporeaddsmod.effects.effects;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

/**
 * Guardas de daño/muerte del modo downed de la colmena, tick de mantenimiento y limpieza.
 *
 * - La ENTRADA en downed se decide en {@link LivingDeathEvent} con prioridad LOWEST, de modo
 *   que tótems, delayed_defibrillation y cualquier otro handler que salve/gestione la muerte
 *   actúen antes. Solo si el jugador realmente iba a morir se intercepta.
 * - Un jugador ya downed no puede morir por ninguna causa externa (se cancela todo).
 * - El remate final del camino "perish" lo hace {@link HiveDownedManager} tras sacar al jugador
 *   del registro, por lo que estos guardas ya no aplican en ese instante.
 */
@EventBusSubscriber(modid = "sporeadd")
public final class HiveDownedEvents {

    private HiveDownedEvents() {
    }

    /**
     * Un jugador downed (o dentro de la ventana de gracia posterior) no puede ser adquirido como
     * objetivo por ningún mob, ni por IA de goals ni por IA de brain (el hook cubre ambas).
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onChangeTarget(LivingChangeTargetEvent event) {
        LivingEntity newTarget = event.getNewTarget();
        if (!(newTarget instanceof ServerPlayer player)) return;
        if (HiveDownedManager.isAggroProtected(player.getUUID(), player.level().getGameTime())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingAttack(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (blockIncoming(player, event.getSource())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingHurt(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (blockIncoming(player, event.getSource())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingDamage(LivingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (blockIncoming(player, event.getSource())) {
            event.setCanceled(true);
            if (HiveDownedManager.isDowned(player.getUUID()) && player.getHealth() < 1.0F) {
                player.setHealth(1.0F);
            }
        }
    }

    /**
     * true si hay que anular este daño entrante:
     *  - jugador downed: se anula TODO.
     *  - ventana de gracia tras salir de downed (p.ej. ya kommandant): sólo se anula el daño
     *    que venga de un mob, para que las entidades que le perseguían no lo rematen; el PvP,
     *    la caída, el fuego, etc. siguen aplicando.
     */
    private static boolean blockIncoming(ServerPlayer player, net.minecraft.world.damagesource.DamageSource source) {
        if (HiveDownedManager.isDowned(player.getUUID())) return true;
        return HiveDownedManager.isAggroProtected(player.getUUID(), player.level().getGameTime())
                && source != null
                && source.getEntity() instanceof net.minecraft.world.entity.Mob;
    }

    /**
     * Prioridad LOWEST: dejamos que todo lo demás (tótems, delayed_defibrillation, otros
     * handlers de muerte) actúe primero. Si la muerte sigue en pie y el jugador tiene el
     * efecto call_of_the_hive cerca de un proto, se cancela y entra en downed.
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        if (HiveDownedManager.isDowned(player.getUUID())) {
            event.setCanceled(true);
            player.setHealth(1.0F);
            return;
        }

        if (!HiveDownedManager.wouldEnterDowned(player)) return;

        event.setCanceled(true);
        player.setHealth(1.0F);
        HiveDownedManager.enterDowned(player, event.getSource());
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        if (server == null) return;
        HiveDownedManager.serverTickAll(server);
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        // El estado en memoria se descarta; el flag persistente en el NBT del jugador
        // (puesto en enterDowned) sobrevive y provoca "perish" en el próximo login.
        HiveDownedManager.forget(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        String pending = player.getPersistentData().getString(HiveDownedManager.HIVE_PENDING_TAG);
        if (pending.isEmpty()) return;

        if (HiveDownedManager.PENDING_SURRENDER.equals(pending)) {
            HiveDownedManager.loginSurrender(player);
        } else {
            HiveDownedManager.loginPerish(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        HiveDownedManager.forget(event.getEntity().getUUID());
    }
}
