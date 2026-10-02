package com.sporeadds.mixin;

import com.Harbinger.Spore.Fluids.BileLiquid;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = BileLiquid.class, remap = false)
public class BileLiquidMixin {

    // Inyectamos al principio (HEAD) del método move.
    // Como la clase BileLiquid hereda de FluidType (que es de Forge), el método move no se ofusca,
    // su nombre siempre será "move" independientemente de los mapeos.
    @Inject(method = "move", at = @At("HEAD"), cancellable = true, remap = false)
    private void sporeadds$protectTeamSporeFromBile(FluidState state, LivingEntity entity, Vec3 movementVector, double gravity, CallbackInfoReturnable<Boolean> cir) {

        // Comprobamos si es un jugador del team "spore"
        if (entity instanceof Player player && player.getTeam() != null && player.getTeam().getName().equals("spore")) {

            // Replicamos el código EXACTO del bloque "else" del mod Spore para los infectados:

            // 1. Velocidad de nado aumentada (1.2)
            movementVector.multiply(1.2D, 1.2D, 1.2D);

            // 2. Flotar suavemente hacia arriba
            entity.setDeltaMovement(entity.getDeltaMovement().add(0.0D, 0.01D, 0.0D));

            // 3. Darle efecto de Regeneración
            entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, 2));

            // 4. Ignorar la gravedad (el mod original hace gravity = 0.0F,
            // pero como los parámetros de Java pasan por valor, nosotros simplemente llamamos al "super" de FluidType
            // pasándole 0.0D en la gravedad)

            // En Forge 1.20.1, el método por defecto de FluidType simplemente devuelve false,
            // pero para imitar exactamente lo que hace el mod, devolvemos lo que devolvería la ejecución normal
            // con la gravedad a 0.
            cir.setReturnValue(false); // FluidType#move por defecto devuelve false.
        }
    }
}