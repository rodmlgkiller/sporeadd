package com.sporeadds.mixin;

import com.Harbinger.Spore.Sentities.Organoids.Brauerei;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.Difficulty;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.List;

@Mixin(value = Brauerei.class, remap = false)
public class BrauereiSpreadBuffsMixin {

    @Inject(
            method = "spreadBuffs(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/core/Holder;)V",
            at = @At("TAIL"),
            remap = false
    )
    private void includeSporeTeamBuffs(LivingEntity entity, net.minecraft.core.Holder<MobEffect> effect, CallbackInfo ci) {
        AABB aabb = entity.getBoundingBox().inflate(32);
        // Busca todos los jugadores "spore" en el área y les aplica el buff
        List<Player> sporePlayers = entity.level().players().stream()
                .filter(Player.class::isInstance)
                .map(Player.class::cast)
                .filter(player -> player.isAlive() &&
                        player.getTeam() != null &&
                        "spore".equalsIgnoreCase(player.getTeam().getName()) &&
                        aabb.contains(player.position()))
                .toList();

        for (Player spore : sporePlayers) {
            int level = entity.level().getDifficulty() == Difficulty.HARD ? 1 : 0;
            spore.addEffect(new MobEffectInstance(effect, 600, level));
        }
    }

    @Inject(
            method = "spreadDeBuffs(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/core/Holder;)V",
            at = @At("TAIL"),
            remap = false
    )
    private void excludeSporeTeamDebuffs(LivingEntity entity, net.minecraft.core.Holder<MobEffect> effect, CallbackInfo ci) {
        AABB aabb = entity.getBoundingBox().inflate(32);
        // Busca todos los jugadores en el área que NO son "spore"
        List<Player> notSporePlayers = entity.level().players().stream()
                .filter(Player.class::isInstance)
                .map(Player.class::cast)
                .filter(player -> player.isAlive() &&
                        (player.getTeam() == null || !"spore".equalsIgnoreCase(player.getTeam().getName())) &&
                        aabb.contains(player.position()))
                .toList();

        for (Player player : notSporePlayers) {
            int level = entity.level().getDifficulty() == Difficulty.HARD ? 1 : 0;
            player.addEffect(new MobEffectInstance(effect, 600, level));
        }
    }
}
