package com.sporeadds.mixin;

import com.sporeadds.sporeaddsmod.util.gluttonousFaminedHelper;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerAttackAnimMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void sporeadd$resetAttackAnim(CallbackInfo ci) {
        Player player = (Player) (Object) this;

        if (gluttonousFaminedHelper.hasgluttonousFaminedAttack(player)) {
            player.attackAnim = 0.0F;
            player.oAttackAnim = 0.0F;
            player.swingTime = 0;
            player.swinging = false;
        }
    }
}