package com.sporeadds.mixin;

import com.sporeadds.sporeaddsmod.client.KommandantBrightnessState;
import net.minecraft.client.OptionInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(OptionInstance.class)
public abstract class OptionInstanceMixin<T> {

    @Shadow
    @Final
    Component caption;

    @Inject(method = "get", at = @At("HEAD"), cancellable = true)
    private void sporeadd$getGammaOverride(CallbackInfoReturnable<T> cir) {
        if (!isGammaOption()) return;
        if (!KommandantBrightnessState.isForcedByKommandant()) return;

        cir.setReturnValue((T) Double.valueOf(KommandantBrightnessState.getCurrentGamma()));
    }

    @Inject(method = "set", at = @At("HEAD"), cancellable = true)
    private void sporeadd$setGammaOverride(T value, CallbackInfo ci) {
        if (!isGammaOption()) return;
        if (!(value instanceof Double d)) return;

        if (KommandantBrightnessState.isForcedByKommandant()) {
            KommandantBrightnessState.setCurrentGamma(d);
            ci.cancel();
        }
    }

    @Unique
    private boolean isGammaOption() {
        if (this.caption.getContents() instanceof TranslatableContents translatable) {
            return "options.gamma".equals(translatable.getKey());
        }
        return false;
    }
}