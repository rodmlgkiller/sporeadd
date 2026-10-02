package com.sporeadds.mixin;

import com.Harbinger.Spore.Sentities.Calamities.Grakensenker;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Team;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Grakensenker.class)
public class GrakensenkerMixin {

    // 1. EL MIXIN QUE YA TENÍAS PARA EVITAR SER CONSUMIDO
    @Inject(method = "shouldConsumeEntity", at = @At("HEAD"), cancellable = true, remap = false)
    private void sporeadds$preventSporeTeamConsumption(Entity entity, Vec3 baseCenter, CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof Player player) {
            Team team = player.getTeam();
            if (team != null && team.getName().equals("spore")) {
                cir.setReturnValue(false);
            }
        }
    }

    // 2. NUEVO MIXIN PARA EVITAR EL SONIDO DE MORDIDA Y EL ATAQUE CUERPO A CUERPO
    // En las mappings de Forge 1.20.1, doHurtTarget se mapea como m_7327_ u onHurtTarget.
    // Lo más seguro es apuntar al nombre "doHurtTarget" con remap = true (o al de Mojang si usas Mojmaps).
    // 2. NUEVO MIXIN PARA EVITAR EL SONIDO DE MORDIDA Y EL ATAQUE CUERPO A CUERPO
    // Apuntamos al método ofuscado m_7327_ (que es doHurtTarget) y desactivamos el remap
    @Inject(method = "m_7327_", at = @At("HEAD"), cancellable = true, remap = false)
    private void sporeadds$preventSporeTeamBiteSound(Entity target, CallbackInfoReturnable<Boolean> cir) {
        // Comprobamos si el que va a recibir el ataque es un jugador
        if (target instanceof Player player) {
            // Obtenemos su equipo
            Team team = player.getTeam();

            // Si es del equipo "spore", cancelamos el ataque antes de que suene el mordisco
            if (team != null && team.getName().equals("spore")) {
                // Devolvemos false para detener el ataque cuerpo a cuerpo y el sonido
                cir.setReturnValue(false);
            }
        }
    }
}