package com.sporeadds.mixin;

import com.Harbinger.Spore.Sentities.BaseEntities.UtilityEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;
import java.util.List;

@Mixin(UtilityEntity.class)
public abstract class UtilityEntityMixin {

    @Inject(method = "getDropList", at = @At("RETURN"), cancellable = true, remap = false)
    private void sporeadds$neverReturnNullDropList(CallbackInfoReturnable<List<? extends String>> cir) {
        if (cir.getReturnValue() == null) {
            cir.setReturnValue(Collections.emptyList());
        }
    }
}