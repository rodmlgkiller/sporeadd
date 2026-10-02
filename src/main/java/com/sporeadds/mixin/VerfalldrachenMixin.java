package com.sporeadds.mixin;

import com.Harbinger.Spore.Sentities.Calamities.Verfalldrachen;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Verfalldrachen.class, remap = false)
public abstract class VerfalldrachenMixin {

    @Inject(method = "updateFlying", at = @At("HEAD"), cancellable = true, remap = false)
    private void sporeadd$cancelAutoFlightWhenMounted(CallbackInfo ci) {
        Verfalldrachen dragon = (Verfalldrachen) (Object) this;

        if (dragon.isVehicle() && dragon.getFirstPassenger() instanceof Player) {
            ci.cancel();
        }
    }
}