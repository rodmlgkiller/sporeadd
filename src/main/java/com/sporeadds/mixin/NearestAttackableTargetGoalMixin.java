package com.sporeadds.mixin;

import com.sporeadds.sporeaddsmod.config.SporeAddsConfig;
import com.sporeadds.sporeaddsmod.util.SporeFactionHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.Predicate;

@Mixin(NearestAttackableTargetGoal.class)
public class NearestAttackableTargetGoalMixin {

    @Shadow
    protected TargetingConditions targetConditions;

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Inject(
            method = "<init>(Lnet/minecraft/world/entity/Mob;Ljava/lang/Class;IZZLjava/util/function/Predicate;)V",
            at = @At("RETURN")
    )
    private void onInit(Mob pMob, Class pTargetType, int pRandomInterval, boolean pMustSee, boolean pMustReach, Predicate pTargetPredicate, CallbackInfo ci) {
        if (this.targetConditions == null || pTargetType != Player.class || !SporeAddsConfig.SPORE_FACTION_ENABLED.get()) {
            return;
        }

        List<? extends String> hostileList = SporeAddsConfig.EXTRA_HOSTILE_MOBS.get();
        List<? extends String> neutralList = SporeAddsConfig.EXTRA_NEUTRAL_MOBS.get();

        boolean isForceHostile = SporeFactionHelper.isEntityInList(pMob, hostileList);
        boolean isForceNeutral = SporeFactionHelper.isEntityInList(pMob, neutralList);

        Predicate<LivingEntity> selector = ((TargetingConditionsAccessor) this.targetConditions).getSelector();

        // Solo tocar mobs hostiles reales o los que tú fuerces como hostiles.
        // Si un mob está marcado como neutral, no se modifica aquí.
        if ((pMob instanceof Monster || isForceHostile) && !isForceNeutral) {
            Predicate<LivingEntity> sporeFilter = livingEntity -> {
                if (livingEntity instanceof Player player) {
                    return !SporeFactionHelper.isSporePlayer(player);
                }
                return true;
            };

            selector = (selector == null) ? sporeFilter : selector.and(sporeFilter);
            this.targetConditions = this.targetConditions.selector(selector);
        }
    }
}