package com.sporeadds.mixin;

import com.sporeadds.sporeaddsmod.effects.effects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Player.class)
public abstract class PlayerClippedWingsMixin {

    @Redirect(
            method = "tryToStartFallFlying()Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;canElytraFly(Lnet/minecraft/world/entity/LivingEntity;)Z"
            )
    )
    private boolean sporeadd$blockElytraStart(ItemStack stack, net.minecraft.world.entity.LivingEntity entity) {
        if (entity instanceof Player player && player.hasEffect(effects.CLIPPED_WINGS)) {
            return false;
        }

        return stack.canElytraFly(entity);
    }
}