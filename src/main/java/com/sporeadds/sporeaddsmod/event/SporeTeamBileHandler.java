package com.sporeadds.sporeaddsmod.event;

import com.Harbinger.Spore.core.Seffects;
import com.Harbinger.Spore.core.Sfluids;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Players in the "spore" team treat Spore's bile like the infected do: no Biled effect, they float up gently
 * and regenerate. (On 1.20.1 this was a mixin on BileLiquid#move; Spore 1.21.1 handles bile in its tick event.)
 */
@EventBusSubscriber(modid = "sporeadd")
public final class SporeTeamBileHandler {

    private SporeTeamBileHandler() {
    }

    private static boolean isSporeTeam(Player player) {
        return player.getTeam() != null && "spore".equals(player.getTeam().getName());
    }

    @SubscribeEvent
    public static void onEffectApplicable(MobEffectEvent.Applicable event) {
        if (event.getEntity() instanceof Player player
                && isSporeTeam(player)
                && event.getEffectInstance().getEffect().is(Seffects.BILED)) {
            event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide() || !isSporeTeam(player)) {
            return;
        }
        if (!player.isInFluidType(Sfluids.BILE_FLUID_TYPE)) {
            return;
        }

        player.setDeltaMovement(player.getDeltaMovement().add(0.0D, 0.01D, 0.0D));
        if (player.tickCount % 20 == 0) {
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, 2));
        }
    }
}
