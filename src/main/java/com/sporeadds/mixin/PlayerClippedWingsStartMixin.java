package com.sporeadds.mixin;

import com.sporeadds.sporeaddsmod.effects.effects;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerClippedWingsStartMixin {

    @Inject(method = "startFallFlying()V", at = @At("HEAD"), cancellable = true)
    private void sporeadd$cancelStartFallFlying(CallbackInfo ci) {
        Player self = (Player) (Object) this;

        if (self.hasEffect(effects.CLIPPED_WINGS.get())) {
            ci.cancel();
        }
    }
}