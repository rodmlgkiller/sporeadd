package com.sporeadds.mixin;

import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentHelper.class)
public class AbyssalAquaAffinityMixin {

    @Inject(
            method = "hasAquaAffinity(Lnet/minecraft/world/entity/LivingEntity;)Z",
            at = @At("RETURN"),
            cancellable = true
    )
    private static void sporeadd$abyssalHasAquaAffinity(LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) {
            return;
        }

        if (!(entity instanceof Player player)) {
            return;
        }

        boolean abyssal = player.getCapability(SporeIdentifierProvider.SPORE_IDENTIFIER)
                .map(data ->
                        "kommandant".equalsIgnoreCase(data.getIdentifier()) &&
                                "abyssal".equalsIgnoreCase(data.getSubclass()))
                .orElse(false);

        if (abyssal) {
            cir.setReturnValue(true);
        }
    }
}