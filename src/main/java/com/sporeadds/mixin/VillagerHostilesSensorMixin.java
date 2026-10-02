package com.sporeadds.mixin;

import com.sporeadds.sporeaddsmod.config.SporeAddsConfig;
import com.sporeadds.sporeaddsmod.util.SporeFactionHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.sensing.VillagerHostilesSensor;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VillagerHostilesSensor.class)
public abstract class VillagerHostilesSensorMixin {

    /**
     * Este método del Vanilla se encarga de decidir si una entidad cercana es hostil para un aldeano.
     * Originalmente devuelve true para zombies, esqueletos, illagers, etc.
     * Aquí inyectamos nuestra lógica: si es un jugador de la facción Spore, ¡es hostil!
     */
    @Inject(method = "isHostile", at = @At("HEAD"), cancellable = true)
    private void onIsHostile(LivingEntity pEntity, CallbackInfoReturnable<Boolean> cir) {
        // Si el sistema de facciones está activado en la config
        if (SporeAddsConfig.SPORE_FACTION_ENABLED.get()) {

            // Si la entidad que el aldeano está mirando es un Jugador
            if (pEntity instanceof Player player) {

                // Si el jugador es del equipo Spore...
                if (SporeFactionHelper.isSporePlayer(player)) {
                    // Le decimos al cerebro del aldeano: "Sí, este jugador es hostil".
                    // Esto detonará automáticamente toda su IA de huida y pánico.
                    cir.setReturnValue(true);
                }
            }
        }
    }

    /**
     * Ajuste de distancia para la alerta del aldeano.
     * Si es un jugador, queremos que huya desde más lejos o con las reglas del Vanilla.
     */
    @Inject(method = "isClose", at = @At("HEAD"), cancellable = true)
    private void onIsClose(LivingEntity pAttacker, LivingEntity pTarget, CallbackInfoReturnable<Boolean> cir) {
        if (SporeAddsConfig.SPORE_FACTION_ENABLED.get() && pTarget instanceof Player) {
            // El aldeano se asustará si el jugador Spore está a 8 bloques o menos
            float distanceAlert = 8.0F;
            cir.setReturnValue(pTarget.distanceToSqr(pAttacker) <= (double)(distanceAlert * distanceAlert));
        }
    }
}