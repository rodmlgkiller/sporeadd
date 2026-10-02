package com.sporeadds.mixin;

import com.sporeadds.sporeaddsmod.util.KommandantFoodHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerMixin {

    @Inject(method = "canEat", at = @At("HEAD"), cancellable = true)
    private void sporeadd$allowKommandantAlwaysEat(boolean ignoreHunger, CallbackInfoReturnable<Boolean> cir) {
        Player player = (Player) (Object) this;

        if (!KommandantFoodHelper.isKommandant(player)) {
            return;
        }

        ItemStack mainHand = player.getItemInHand(InteractionHand.MAIN_HAND);
        ItemStack offHand = player.getItemInHand(InteractionHand.OFF_HAND);
        ItemStack useItem = player.getUseItem();

        if (KommandantFoodHelper.isAllowedKommandantFood(useItem)
                || KommandantFoodHelper.isAllowedKommandantFood(mainHand)
                || KommandantFoodHelper.isAllowedKommandantFood(offHand)) {
            cir.setReturnValue(true);
        }
    }
}