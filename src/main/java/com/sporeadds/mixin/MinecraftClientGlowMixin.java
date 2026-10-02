package com.sporeadds.mixin;

import com.sporeadds.sporeaddsmod.client.renderer.KommandantESPClient;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public class MinecraftClientGlowMixin {

    @Inject(
            at = @At("HEAD"),
            method = "shouldEntityAppearGlowing(Lnet/minecraft/world/entity/Entity;)Z",
            cancellable = true
    )
    public void onShouldEntityAppearGlowing(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (KommandantESPClient.shouldGlow(entity)) {
            cir.setReturnValue(true);
            cir.cancel();
        }
    }
}