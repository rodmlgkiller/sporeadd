package com.sporeadds.sporeaddsmod.hive;

import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import net.neoforged.fml.common.EventBusSubscriber;

import com.sporeadds.sporeaddsmod.config.SporeAddsConfig;
import com.sporeadds.sporeaddsmod.effects.SporeTeamCombatTracker;
import com.sporeadds.sporeaddsmod.effects.effects;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingHurtEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

/**
 * Aplica y mantiene el efecto {@code call_of_the_hive}.
 *
 * Aplicación: cada vez que un jugador elegible es golpeado por una entidad del equipo
 * spore que no es un jugador ghost, estando a menos de 300 bloques de un {@code spore:proto},
 * hay una probabilidad configurable (por defecto 5%) de recibir el efecto.
 *
 * Elegibles: jugadores que NO están en el equipo spore, y jugadores con clase ghost
 * (que sí pueden estar temporalmente en el equipo spore).
 *
 * Mantenimiento: si un jugador con el efecto deja de estar a menos de 300 bloques de un
 * proto, el efecto se retira. Mientras está downed, el efecto lo gestiona {@link HiveDownedManager}.
 */
@EventBusSubscriber(modid = "sporeadd")
public final class CallOfTheHiveHandler {

    public static final double PROXIMITY_RADIUS = 300.0D;
    private static final int PROXIMITY_CHECK_INTERVAL = 20;

    private CallOfTheHiveHandler() {
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer victim)) return;
        if (victim.level().isClientSide()) return;

        if (HiveDownedManager.isDowned(victim.getUUID())) return;
        if (victim.getEffect(effects.CALL_OF_THE_HIVE.get()) != null) return;

        Entity rawAttacker = event.getSource().getEntity();
        if (!(rawAttacker instanceof LivingEntity attacker)) return;
        if (!SporeTeamCombatTracker.isSporeTeamNonGhost(attacker)) return;

        // El propio jugador golpeado debe ser elegible: NO un jugador spore-no-ghost.
        if (SporeTeamCombatTracker.isSporeTeamNonGhost(victim)) return;

        double chance = SporeAddsConfig.CALL_OF_THE_HIVE_CHANCE.get();
        if (chance <= 0.0D || victim.getRandom().nextDouble() >= chance) return;

        // El escaneo de protos es lo más caro: solo tras pasar la tirada.
        if (!SporeAddsConfig.CALL_OF_THE_HIVE_APPLIES_WITHOUT_PROTO.get()
                && !ProtoProximity.isProtoWithin(victim.level(), victim.position(), PROXIMITY_RADIUS)) {
            return;
        }

        victim.addEffect(new MobEffectInstance(
                effects.CALL_OF_THE_HIVE.get(),
                HiveDownedManager.MARK_REFRESH_TICKS,
                0, false, false, true));
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.tickCount % PROXIMITY_CHECK_INTERVAL != 0) return;

        if (player.getEffect(effects.CALL_OF_THE_HIVE.get()) == null) return;
        if (HiveDownedManager.isDowned(player.getUUID())) return;

        if (!SporeAddsConfig.CALL_OF_THE_HIVE_PERSISTS_WITHOUT_PROTO.get()
                && !ProtoProximity.isProtoWithin(player.level(), player.position(), PROXIMITY_RADIUS)) {
            player.removeEffect(effects.CALL_OF_THE_HIVE.get());
            return;
        }

        // El efecto se mantiene: refrescar para que no expire.
        player.addEffect(new MobEffectInstance(
                effects.CALL_OF_THE_HIVE.get(),
                HiveDownedManager.MARK_REFRESH_TICKS,
                0, false, false, true));
    }
}
