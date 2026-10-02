package com.sporeadds.sporeaddsmod.effects;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Lógica del efecto {@link PunishmentEffect}:
 *  - reduce el daño infligido por la entidad castigada (-2 por nivel).
 *  - anula el salto por completo (sin importar el amplifier): ni impulso vertical ni horizontal.
 *    Para subir bloques el jugador usa el +1 de {@code step_height} del propio efecto.
 * (El overlay de pantalla y la reducción de sonido son de cliente.)
 *
 * <p>El bloqueo del salto usa el mismo patrón que {@link MangledEffectHandler}
 * ({@code LivingJumpEvent} + {@code hasImpulse}/{@code hurtMarked}), que es el que de verdad
 * "pega" en el jugador local. La diferencia es que aquí el multiplicador es 0 fijo y nunca
 * escala con el amplifier: escalarlo (como en Mangled) hacía que a niveles altos el
 * multiplicador se volviera negativo y el salto dejara de comportarse bien.
 */
@Mod.EventBusSubscriber(modid = "sporeadd")
public final class PunishmentEventHandler {

    private PunishmentEventHandler() {
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof LivingEntity attacker)) return;
        MobEffectInstance inst = attacker.getEffect(effects.PUNISHMENT.get());
        if (inst == null) return;

        // Amplifier 2 (nivel 3): el castigado no inflige ningún daño.
        if (inst.getAmplifier() >= 2) {
            event.setCanceled(true);
            return;
        }

        int level = inst.getAmplifier() + 1;
        float reduced = event.getAmount() - PunishmentEffect.OUTGOING_DAMAGE_PER_LEVEL * level;
        event.setAmount(Math.max(0.0F, reduced));
    }

    /** Anula el salto (vertical y horizontal) en el instante del despegue (jugadores y mobs). */
    @SubscribeEvent
    public static void onLivingJump(LivingEvent.LivingJumpEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.getEffect(effects.PUNISHMENT.get()) == null) return;

        double keepY = Math.min(0.0D, entity.getDeltaMovement().y);   // deja caer, nunca subir
        entity.setDeltaMovement(0.0D, keepY, 0.0D);
        entity.hasImpulse = true;
        entity.hurtMarked = true;
    }

    /**
     * Mientras el jugador siga en el aire tras el salto, el control aéreo volvería a acelerarlo
     * hacia delante tick a tick. Se vuelve a poner la velocidad horizontal a 0 al final de cada
     * tick (después de {@code travel()}), en cliente y servidor, con los mismos flags que el
     * evento de salto para que el cambio "pegue" en el jugador local.
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Player player = event.player;
        if (player.getEffect(effects.PUNISHMENT.get()) == null) return;
        if (player.isPassenger() || player.isInWater() || player.onClimbable()
                || player.isFallFlying() || player.getAbilities().flying) {
            return;
        }

        Vec3 v = player.getDeltaMovement();

        if (player.onGround()) {
            // En el suelo: mata cualquier impulso vertical residual (predicción del cliente
            // reintroduciendo el salto) sin tocar el movimiento normal.
            if (v.y > 0.0D) {
                player.setDeltaMovement(v.x, 0.0D, v.z);
                player.hasImpulse = true;
                player.hurtMarked = true;
            }
            return;
        }

        // En el aire: el control aéreo no debe reconstruir el avance horizontal.
        if (v.x != 0.0D || v.z != 0.0D) {
            player.setDeltaMovement(0.0D, v.y, 0.0D);
            player.hasImpulse = true;
            player.hurtMarked = true;
        }
    }
}
