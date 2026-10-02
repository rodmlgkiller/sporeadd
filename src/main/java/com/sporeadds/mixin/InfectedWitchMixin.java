package com.sporeadds.mixin;

import com.Harbinger.Spore.core.SConfig;
import com.Harbinger.Spore.Sentities.BasicInfected.InfectedWitch;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.EnumSet;
import java.util.List;

@Mixin(InfectedWitch.class)
public class InfectedWitchMixin {

    @Inject(method = "m_8099_()V", at = @At("TAIL"), remap = false)
    private void sporeadds$addPlayerBuffGoal(CallbackInfo ci) {
        InfectedWitch witch = (InfectedWitch) (Object) this;

        // Añadimos un Goal personalizado (Prioridad 3)
        witch.goalSelector.addGoal(3, new Goal() {
            private Player targetPlayer;
            private int cooldown = 0;

            // Inicializamos los bloqueos (no puede caminar a dos sitios a la vez)
            {
                this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
            }

            @Override
            public boolean canUse() {
                // 1. ¿Pociones habilitadas?
                if (!(Boolean) SConfig.SERVER.use_potions.get()) return false;

                // 2. ¿La bruja está ocupada atacando a alguien?
                if (witch.getTarget() != null) return false;

                // 3. ¿El cooldown nos permite tirar pociones ya?
                if (this.cooldown > 0) {
                    this.cooldown--;
                    return false;
                }

                // 4. Buscar a un jugador del team 'spore' cerca (radio 16 bloques)
                List<Player> players = witch.level().getEntitiesOfClass(
                        Player.class,
                        witch.getBoundingBox().inflate(16.0D)
                );

                for (Player player : players) {
                    if (player.getTeam() != null && player.getTeam().getName().equals("spore")) {
                        this.targetPlayer = player;
                        return true;
                    }
                }

                return false;
            }

            @Override
            public boolean canContinueToUse() {
                return this.targetPlayer != null
                        && this.targetPlayer.isAlive()
                        && witch.distanceToSqr(this.targetPlayer) < 256.0D;
            }

            @Override
            public void stop() {
                this.targetPlayer = null;
                witch.getNavigation().stop();
            }

            @Override
            public void tick() {
                if (this.targetPlayer != null) {
                    // Mirar al jugador
                    witch.getLookControl().setLookAt(this.targetPlayer, 30.0F, 30.0F);

                    double distanceSqr = witch.distanceToSqr(this.targetPlayer);

                    // Si está lejos (más de 3 bloques), acercarse (velocidad 1.3)
                    if (distanceSqr > 9.0D) {
                        witch.getNavigation().moveTo(this.targetPlayer, 1.3D);
                    }
                    // Si está cerca, ¡Tirar poción!
                    else {
                        witch.getNavigation().stop();

                        // Llamamos al método nativo de la bruja para tirar el buff
                        witch.performRangedBuff(this.targetPlayer, 1.0F);

                        // Reiniciamos el cooldown según la configuración
                        this.cooldown = (Integer) SConfig.SERVER.buff_potion_meter.get();

                        // Soltamos el target temporalmente
                        this.targetPlayer = null;
                    }
                }
            }
        });
    }
}