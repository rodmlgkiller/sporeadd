package com.sporeadds.mixin;

import com.Harbinger.Spore.Sentities.Organoids.Verwa;
import com.sporeadds.sporeaddsmod.commands.VervaTransportTask;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Verwa.class, remap = false)
public abstract class VerwaMixin {

    @Inject(method = "TickTimer", at = @At("HEAD"), cancellable = true, remap = false)
    private void sporeadds$preventTickTimer(CallbackInfo ci) {
        Verwa verwa = (Verwa) (Object) this;
        if (VervaTransportTask.isTransportVerva(verwa.getUUID())) {
            ci.cancel();
        }
    }

    @Inject(method = "SummonStoredEntity", at = @At("HEAD"), cancellable = true, remap = false)
    private void sporeadds$preventSummon(CallbackInfo ci) {
        Verwa verwa = (Verwa) (Object) this;
        if (VervaTransportTask.isTransportVerva(verwa.getUUID())) {
            ci.cancel();
        }
    }
}