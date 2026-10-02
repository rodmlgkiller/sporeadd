package com.sporeadds.mixin;

import com.Harbinger.Spore.Sentities.AI.BuffAlliesGoal;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = BuffAlliesGoal.class, remap = false)
public class BuffAlliesGoalMixin {

    @Inject(
            method = "getFreePartner()Lnet/minecraft/world/entity/Mob;",
            at = @At("RETURN"),
            cancellable = true,
            remap = false
    )
    private void includeSporePlayersAsAllies(CallbackInfoReturnable<Mob> cir) {
        Mob result = cir.getReturnValue();
        BuffAlliesGoal goal = (BuffAlliesGoal)(Object)this;
        Level level = goal.mob.level();

        // Encontrar todos los jugadores "spore" en el rango relevante
        List<Player> sporePlayers = level.players().stream()
                .filter(Player.class::isInstance) // Garantiza tipo Player
                .map(Player.class::cast)
                .filter(player -> player.isAlive() &&
                        player.getTeam() != null &&
                        "spore".equalsIgnoreCase(player.getTeam().getName()) &&
                        goal.mob.getBoundingBox().inflate(goal.mob.getAttributeBaseValue(Attributes.FOLLOW_RANGE)).contains(player.position())
                )
                .toList();

        if (!sporePlayers.isEmpty()) {
            // Aquí no puedes setear cir.setReturnValue(closest) porque Player no es Mob.
            // Si quieres aplicar el buff, hazlo en el mixin de tick().
            // Opcional: Puedes guardar referencia del jugador para usar luego en tick().
        }
        // Mantiene el valor original de cir si no hay jugador "spore" (o si no son Mob).
    }
}
